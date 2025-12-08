

BEGIN;


SET search_path TO public;


CREATE TABLE IF NOT EXISTS patients (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE patients IS 'Master list of patients registered in the clinic.';
COMMENT ON COLUMN patients.phone_number IS 'Preferred contact phone number for the patient.';
COMMENT ON COLUMN patients.email IS 'Primary email address for the patient.';

CREATE TABLE IF NOT EXISTS appointments (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT ck_appointments_status
        CHECK (status IN ('scheduled', 'completed', 'cancelled', 'no_show'))
);

COMMENT ON TABLE appointments IS 'Scheduled appointments for patients.';
COMMENT ON COLUMN appointments.status IS 'Lifecycle status of the appointment.';

CREATE TABLE IF NOT EXISTS visits (
    id SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL,
    visit_date TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visits_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES appointments (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

COMMENT ON TABLE visits IS 'Clinical visit records linked to appointments.';

-- =========================
-- Medical History Tables
-- =========================
CREATE TABLE IF NOT EXISTS medical_conditions (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    condition_name VARCHAR(255) NOT NULL,
    diagnosis_date DATE,
    status VARCHAR(50) DEFAULT 'active',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_conditions_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

COMMENT ON TABLE medical_conditions IS 'Medical conditions and diagnoses for patients.';

CREATE TABLE IF NOT EXISTS allergies (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    allergen VARCHAR(255) NOT NULL,
    reaction VARCHAR(255),
    severity VARCHAR(50) DEFAULT 'moderate',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_allergies_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

COMMENT ON TABLE allergies IS 'Known allergies and reactions for patients.';

CREATE TABLE IF NOT EXISTS medications (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) DEFAULT 'active',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medications_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

COMMENT ON TABLE medications IS 'Current and historical medications for patients.';

-- =========================
-- Prescription Management Tables
-- =========================
CREATE TABLE IF NOT EXISTS prescriptions (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    quantity INTEGER,
    frequency VARCHAR(100),
    duration_days INTEGER,
    prescribed_date DATE NOT NULL,
    refill_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescriptions_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

COMMENT ON TABLE prescriptions IS 'Patient prescriptions with refill tracking.';

-- =========================
-- Visit Notes/Clinical Records Tables
-- =========================
ALTER TABLE visits ADD COLUMN IF NOT EXISTS clinical_notes TEXT;
ALTER TABLE visits ADD COLUMN IF NOT EXISTS diagnosis TEXT;
ALTER TABLE visits ADD COLUMN IF NOT EXISTS treatment TEXT;
ALTER TABLE visits ADD COLUMN IF NOT EXISTS follow_up_notes TEXT;

-- =========================
--  Indexes for query performance
-- =========================
CREATE INDEX IF NOT EXISTS idx_patients_name ON patients (last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_patients_email ON patients (email);
CREATE INDEX IF NOT EXISTS idx_patients_phone ON patients (phone_number);

CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments (appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments (patient_id);

CREATE INDEX IF NOT EXISTS idx_visits_date ON visits (visit_date);
CREATE INDEX IF NOT EXISTS idx_visits_appointment ON visits (appointment_id);

CREATE INDEX IF NOT EXISTS idx_medical_conditions_patient ON medical_conditions (patient_id);
CREATE INDEX IF NOT EXISTS idx_allergies_patient ON allergies (patient_id);
CREATE INDEX IF NOT EXISTS idx_medications_patient ON medications (patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions (patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_refill ON prescriptions (refill_date);

--  Updated-at maintenance triggers
-- =========================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_patients_updated_at ON patients;
CREATE TRIGGER trg_patients_updated_at
    BEFORE UPDATE ON patients
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_appointments_updated_at ON appointments;
CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_visits_updated_at ON visits;
CREATE TRIGGER trg_visits_updated_at
    BEFORE UPDATE ON visits
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMIT;
