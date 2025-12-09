## Database Setup Instructions

To activate all the new features (Medical History, Prescriptions, and Advanced Search), you need to update your database schema.

### Option 1: Using PostgreSQL CLI (Recommended)

Run this command in your terminal:

```powershell
psql -U postgres -d clinicmanager -f schema.sql
```

This will create all the new tables and columns needed for:
- Medical Conditions tracking
- Allergies management  
- Medications tracking
- Prescription management with refill dates
- Enhanced Visit notes with clinical details

### Option 2: Using pgAdmin

1. Open pgAdmin
2. Connect to your database
3. Open the Query Tool
4. Copy the entire contents of `schema.sql`
5. Paste it into the Query Tool
6. Click Execute

### What Gets Created

The following new tables will be created:
- `medical_conditions` - Store patient medical conditions/diagnoses
- `allergies` - Track known allergies and reactions
- `medications` - Current and historical medications
- `prescriptions` - Track prescriptions with refill dates
- Enhanced `visits` table with clinical note fields

### Verification

After running the schema, you can verify the tables were created by running:

```sql
\dt
```

You should see all the new tables listed.

### After Schema Update

Once the schema is updated:
1. Restart the application
2. You'll see data from the new features in:
   - **Medical History tab** - View conditions, allergies, medications
   - **Prescriptions tab** - View and track patient prescriptions
   - **Search tab** - Advanced patient search with appointment date filters
