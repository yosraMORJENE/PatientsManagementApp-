-- Schema Fixes for Logical Issues
-- This migration script fixes the identified logical problems while maintaining backward compatibility

BEGIN;

-- ================================================================
-- FIX 1: Make Visit reference Patient directly (not just Appointment)
-- Allows walk-in visits and better reflects real-world workflow
-- ================================================================

-- Add patient_id to visits table (making appointment_id optional)
ALTER TABLE visits ADD COLUMN IF NOT EXISTS patient_id INTEGER;

-- Add foreign key constraint for patient_id
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_visits_patient'
    ) THEN
        ALTER TABLE visits ADD CONSTRAINT fk_visits_patient
            FOREIGN KEY (patient_id)
            REFERENCES patients (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;
    END IF;
END $$;

-- Populate patient_id from existing appointments (backward compatibility)
UPDATE visits v
SET patient_id = a.patient_id
FROM appointments a
WHERE v.appointment_id = a.id
AND v.patient_id IS NULL;

-- Make appointment_id optional (allow NULL for walk-in visits)
ALTER TABLE visits ALTER COLUMN appointment_id DROP NOT NULL;

-- Add visit status tracking
ALTER TABLE visits ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'completed';

-- Add CHECK constraint for visit status
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_visits_status'
    ) THEN
        ALTER TABLE visits ADD CONSTRAINT ck_visits_status
            CHECK (status IN ('in-progress', 'completed', 'cancelled'));
    END IF;
END $$;

COMMENT ON COLUMN visits.patient_id IS 'Direct reference to patient - supports walk-in visits';
COMMENT ON COLUMN visits.appointment_id IS 'Optional link to appointment if visit was scheduled';
COMMENT ON COLUMN visits.status IS 'Status of the visit: in-progress, completed, or cancelled';

-- ================================================================
-- FIX 2: Link Prescriptions to Visits
-- Tracks which visit generated each prescription
-- ================================================================

-- Add visit_id to prescriptions
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS visit_id INTEGER;

-- Add foreign key constraint for visit_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_prescriptions_visit'
    ) THEN
        ALTER TABLE prescriptions ADD CONSTRAINT fk_prescriptions_visit
            FOREIGN KEY (visit_id)
            REFERENCES visits (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;
    END IF;
END $$;

COMMENT ON COLUMN prescriptions.visit_id IS 'Optional reference to the visit where prescription was written';

-- ================================================================
-- FIX 3: Add resolved_date to Medical Conditions
-- Tracks when conditions were resolved
-- ================================================================

ALTER TABLE medical_conditions ADD COLUMN IF NOT EXISTS resolved_date DATE;

COMMENT ON COLUMN medical_conditions.resolved_date IS 'Date when the condition was resolved (if status is inactive/resolved)';

-- ================================================================
-- FIX 4: Add CHECK constraint for Allergy Severity
-- Standardizes severity values
-- ================================================================

-- First update any non-standard values to 'moderate'
UPDATE allergies 
SET severity = 'moderate' 
WHERE severity NOT IN ('mild', 'moderate', 'severe', 'life-threatening');

-- Add CHECK constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_allergies_severity'
    ) THEN
        ALTER TABLE allergies ADD CONSTRAINT ck_allergies_severity
            CHECK (severity IN ('mild', 'moderate', 'severe', 'life-threatening'));
    END IF;
END $$;

-- Update default to use constrained values
ALTER TABLE allergies ALTER COLUMN severity SET DEFAULT 'moderate';

-- ================================================================
-- FIX 5: Expand Appointment Status Options
-- Adds 'arrived' and 'in-progress' states
-- ================================================================

-- Drop old constraint if exists
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS ck_appointments_status;

-- Add new constraint with expanded options
ALTER TABLE appointments ADD CONSTRAINT ck_appointments_status
    CHECK (status IN ('scheduled', 'arrived', 'in-progress', 'completed', 'cancelled', 'no_show'));

COMMENT ON COLUMN appointments.status IS 'Status: scheduled, arrived, in-progress, completed, cancelled, no_show';

-- ================================================================
-- FIX 6: Add indexes for new foreign keys
-- ================================================================

CREATE INDEX IF NOT EXISTS idx_visits_patient ON visits (patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_visit ON prescriptions (visit_id);
CREATE INDEX IF NOT EXISTS idx_medical_conditions_status ON medical_conditions (status, resolved_date);

-- ================================================================
-- Documentation Comments
-- ================================================================

COMMENT ON TABLE visits IS 'Clinical visit records - can be linked to appointments or recorded as walk-ins';
COMMENT ON TABLE prescriptions IS 'Patient prescriptions with optional link to originating visit';

COMMIT;

-- ================================================================
-- Verification Queries (run these after migration to verify)
-- ================================================================

-- Verify visits have patient_id populated
-- SELECT COUNT(*) as visits_with_patient FROM visits WHERE patient_id IS NOT NULL;

-- Verify visits without appointments (walk-ins)
-- SELECT COUNT(*) as walk_in_visits FROM visits WHERE appointment_id IS NULL;

-- Verify allergy severity values
-- SELECT DISTINCT severity FROM allergies;

-- Verify appointment status values
-- SELECT DISTINCT status FROM appointments;
