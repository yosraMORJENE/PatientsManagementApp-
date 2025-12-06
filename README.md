# Patients Management App - Setup and Run Guide

This is a Java Swing application for managing patients, appointments, and visits in a clinic.

## Prerequisites

1. **Java Development Kit (JDK)** - Version 8 or higher
   - Check if installed: `java -version`
   - Download from: https://www.oracle.com/java/technologies/downloads/

2. **PostgreSQL Database** - Version 10 or higher
   - Download from: https://www.postgresql.org/download/
   - Make sure PostgreSQL service is running

3. **PostgreSQL JDBC Driver**
   - Download from: https://jdbc.postgresql.org/download/
   - You'll need the JAR file (e.g., `postgresql-42.7.1.jar`)

## Setup Instructions

### Step 1: Set Up PostgreSQL Database

1. Open PostgreSQL (pgAdmin or psql command line)

2. Create the database:
   ```sql
   CREATE DATABASE clinicmanager;
   ```

3. (Optional) Run the schema file to set up tables:
   ```bash
   psql -U postgres -d clinicmanager -f schema.sql
   ```
   Or manually execute the SQL commands from `schema.sql`

   **Note:** The application will automatically create tables if they don't exist, so this step is optional.

### Step 2: Configure Database Connection

Edit `config/database.properties` and update if needed:
- `db.host=localhost` (default)
- `db.port=5432` (default PostgreSQL port)
- `db.name=clinicmanager` (database name)
- `db.username=postgres` (your PostgreSQL username)
- `db.password=salsa` (your PostgreSQL password - **change this!**)

### Step 3: Download PostgreSQL JDBC Driver

1. Download the PostgreSQL JDBC driver JAR file from:
   https://jdbc.postgresql.org/download/

2. Place it in a `lib` folder in your project root:
   ```
   PatientsManagementApp-/
   ├── lib/
   │   └── postgresql-42.7.1.jar  (or your version)
   ├── config/
   ├── src/
   └── ...
   ```

### Step 4: Compile the Application

Open a terminal/command prompt in the project root directory and run:

**Windows (PowerShell):**
```powershell
# Create output directory
New-Item -ItemType Directory -Force -Path "out"

# Compile all Java files (adjust the postgresql jar path as needed)
javac -d out -cp "lib\postgresql-42.7.1.jar" -sourcepath src src\clinicmanager\*.java src\clinicmanager\**\*.java
```

**Windows (Command Prompt):**
```cmd
mkdir out
javac -d out -cp "lib\postgresql-42.7.1.jar" -sourcepath src src\clinicmanager\*.java src\clinicmanager\dao\*.java src\clinicmanager\database\*.java src\clinicmanager\gui\*.java src\clinicmanager\models\*.java src\clinicmanager\util\*.java
```

**Linux/Mac:**
```bash
mkdir -p out
javac -d out -cp "lib/postgresql-42.7.1.jar" -sourcepath src src/clinicmanager/**/*.java
```

### Step 5: Copy Configuration Files

Copy the config directory to the output directory so the application can find it:

**Windows (PowerShell):**
```powershell
Copy-Item -Recurse -Force config out\
```

**Windows (Command Prompt):**
```cmd
xcopy /E /I /Y config out\config
```

**Linux/Mac:**
```bash
cp -r config out/
```

### Step 6: Run the Application

**Windows:**
```powershell
java -cp "out;lib\postgresql-42.7.1.jar" clinicmanager.App
```

**Windows (Command Prompt):**
```cmd
java -cp "out;lib\postgresql-42.7.1.jar" clinicmanager.App
```

**Linux/Mac:**
```bash
java -cp "out:lib/postgresql-42.7.1.jar" clinicmanager.App
```

## Quick Start Script (Windows)

Create a file `run.bat` in the project root:

```batch
@echo off
echo Compiling...
if not exist out mkdir out
javac -d out -cp "lib\postgresql-42.7.1.jar" -sourcepath src src\clinicmanager\*.java src\clinicmanager\dao\*.java src\clinicmanager\database\*.java src\clinicmanager\gui\*.java src\clinicmanager\models\*.java src\clinicmanager\util\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Copying config files...
xcopy /E /I /Y config out\config >nul

echo Running application...
java -cp "out;lib\postgresql-42.7.1.jar" clinicmanager.App

pause
```

Then just double-click `run.bat` to compile and run!

## Troubleshooting

### "PostgreSQL JDBC driver not found"
- Make sure you downloaded the PostgreSQL JDBC driver JAR file
- Place it in the `lib` folder
- Update the classpath in the compile and run commands to match your JAR filename

### "Database connection error"
- Verify PostgreSQL is running
- Check database credentials in `config/database.properties`
- Ensure the database `clinicmanager` exists
- Test connection: `psql -U postgres -d clinicmanager`

### "ClassNotFoundException"
- Make sure the classpath includes both the `out` directory and the PostgreSQL JAR file
- On Windows, use semicolon (`;`) to separate classpath entries
- On Linux/Mac, use colon (`:`) to separate classpath entries

### Tables not created automatically
- Run `schema.sql` manually in PostgreSQL
- Or check database connection and permissions

## Application Features

- **Patients Tab**: Add, update, delete, and search patients
- **Appointments Tab**: Schedule and manage appointments
- **Visits Tab**: Record and track patient visits

## Notes

- The application will automatically create database tables on first run if they don't exist
- HikariCP connection pooling is optional (the app works without it)
- Make sure to change the default database password in production!

