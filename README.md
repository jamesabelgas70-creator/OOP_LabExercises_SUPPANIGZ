>>>>>>> origin/main
# AidSync - Relief Distribution Management System

AidSync is a desktop application for Philippine LGUs and barangays to manage relief distribution during disasters. It replaces paper lists with a digital system that prevents duplication and speeds up the process.

## Features

- **User Management**: Admin and Staff roles with appropriate permissions
- **Beneficiary Management**: Register and manage relief recipients with complete information
- **Barangay/Purok System**: 26 pre-loaded barangays with dynamic purok selection
- **Distribution Tracking**: Record relief package distributions with inventory management
- **Inventory Management**: Track relief items with low stock alerts
- **Data Persistence**: All data automatically saved to SQLite database
- **Search & Filter**: Quick search functionality for beneficiaries

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

1. Clone or download this project
2. Open a terminal in the project root directory
3. Build the project using Maven:

```bash
mvn clean compile
```

To create an executable JAR:

```bash
mvn clean package
```

The JAR file will be created in `target/aidSync-1.0.0.jar`

## Running the Application

### Using Maven:

```bash
mvn exec:java -Dexec.mainClass="com.aidsync.Main"
```

### Using the JAR file:

```bash
java -jar target/aidSync-1.0.0.jar
```

## Default Login Credentials

- **Username**: `admin`
- **Password**: `admin123`

**Note**: Change the default password immediately after first login for security.

## Project Structure

```
aidSync/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── aidsync/
│                   ├── Main.java                    # Application entry point
│                   ├── dao/                          # Data Access Objects
│                   │   ├── BeneficiaryDAO.java
│                   │   ├── DistributionDAO.java
│                   │   ├── InventoryDAO.java
│                   │   └── UserDAO.java
│                   ├── model/                        # Data models
│                   │   ├── Beneficiary.java
│                   │   ├── Distribution.java
│                   │   ├── DistributionItem.java
│                   │   ├── InventoryItem.java
│                   │   └── User.java
│                   ├── service/                      # Business logic layer
│                   │   ├── BeneficiaryService.java
│                   │   ├── DistributionService.java
│                   │   ├── InventoryService.java
│                   │   └── UserService.java
│                   ├── ui/                           # User interface components
│                   │   ├── BeneficiaryDialog.java
│                   │   ├── BeneficiaryManagementFrame.java
│                   │   ├── CreateAccountDialog.java
│                   │   ├── DashboardFrame.java
│                   │   ├── DistributionFrame.java
│                   │   ├── InventoryDialog.java
│                   │   ├── InventoryFrame.java
│                   │   └── LoginFrame.java
│                   └── util/                         # Utilities
│                       ├── BarangayData.java
│                       └── DatabaseManager.java
├── pom.xml                                           # Maven configuration
└── README.md                                         # This file
```

## Database

The application uses SQLite database (`aidsync.db`) which is automatically created in the project root directory on first run. The database includes:

- Users table
- Beneficiaries table
- Inventory table
- Distributions table
- Distribution items table
- Activity log table

All data is automatically persisted. No manual save operations required.

## Barangay System

The system includes 26 pre-loaded barangays with their respective puroks:

1. BADAS
2. BOBON
3. BUSO
4. CABUAYA
5. CENTRAL (POBLACION)
6. CULIAN
7. DAHICAN
8. DANAO
9. DAWAN
10. DON ENRIQUE LOPEZ
11. DON MARTIN MARUNDAN
12. DON SALVADOR LOPEZ SR.
13. LANGKA
14. LAWIGAN
15. LIBUDON
16. LUBAN
17. MACAMBOL
18. MAMALI
19. MATIAO
20. MAYO
21. SAINZ
22. SANGHAY
23. TAGABAKID
24. TAGBINONGA
25. TAGUIBO
26. TAMISAN

Users must select barangay and purok from dropdowns only (no typing allowed).

## User Roles

### Admin
- Create staff accounts
- View all reports and system data
- Reset user passwords
- Backup and restore system data
- Configure system settings

### Staff
- Register new beneficiaries
- Record relief distributions
- Search and view beneficiary information
- View basic reports
- Update beneficiary status

## Key Features

### Beneficiary Registration
- Auto-generated 5-digit Beneficiary ID (00001, 00002, etc.)
- Complete personal information
- Barangay and Purok selection (dropdown only)
- Family size (1-20)
- Special conditions (PWD, Senior Citizen, Pregnant, Solo Parent)
- Duplicate checking (name + barangay + purok)

### Distribution Management
- Search beneficiaries by name or ID
- Select multiple inventory items
- Automatic inventory deduction
- Distribution notes
- Receipt generation

### Inventory Management
- Add/edit inventory items
- Low stock alerts
- Category organization
- Quantity tracking

## Troubleshooting

### Database Issues
If you encounter database errors:
1. Delete the `aidsync.db` file
2. Restart the application (database will be recreated)

### Login Issues
- Ensure you're using the correct default credentials
- Admin users can create new accounts from the login screen

### Performance Issues
- For large datasets, consider optimizing the database
- Ensure adequate system memory

## Development Notes

- Built with Java Swing for the UI
- SQLite for database (file-based, no server required)
- Maven for dependency management
- Follows MVC-like architecture (Model-DAO-Service-UI)

## License

This project is developed for Philippine LGUs and barangays for disaster relief management.

## Support

For issues or questions, please refer to the system documentation (pdfcrowd.pdf) or contact your system administrator.

=======
# OOP_LabExercises_SUPPANIGZ
>>>>>>> 4c54b77f1cc1737aa9d010c9f8378b54558c123b
