# Hostel Management System (Java Swing + MySQL)

## Requirements
- Java 17+
- MySQL 8+
- JDBC MySQL Driver (mysql-connector-j)
- IDE: IntelliJ / Eclipse 

## Setup
1. Import the project folder `HostelManagementSystem` into your IDE.
2. Create the database and sample data:
   - Open `sql/hostel_db.sql` and execute it in MySQL (Workbench or CLI).
3. Configure DB credentials:
   - Open `src/DBConnection.java` and set `PASSWORD` to your MySQL root password (or user you prefer).
4. Ensure MySQL Connector/J is on the classpath:
   - IntelliJ: File > Project Structure > Libraries > Add `mysql-connector-j-8.x.x.jar`.
5. Run:
   - Execute `src/Main.java`.
   - Login with username: `admin`, password: `admin123` (change in DB).

## Features
- Admin Login
- Dashboard stats and navigation
- Students CRUD with auto room allocation and CSV export
- Rooms CRUD with availability
- Fees CRUD and paid/pending tracking
- Staff CRUD
- Complaints filing and resolution
- Basic report exports to CSV

## Notes
- In production, store admin password hashed (e.g., BCrypt). Here it's plain for simplicity.
- Auto-availability is computed in DB for rooms via a generated column.
- Update connection URL in `DBConnection.java` if your MySQL settings differ.

## Troubleshooting
- Communications link failure: verify MySQL is running and credentials are correct.
- Access denied: update `USER`/`PASSWORD` in `DBConnection.java`.
- Timezone warnings: the URL includes `serverTimezone=UTC`.
"# Hostel-Management-System" 
"# Hostel-Management-System" 

