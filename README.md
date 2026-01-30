# J-Scheduler

A modern desktop application for tracking college assignments, courses, and academic schedules built with JavaFX.

## Overview

J-Scheduler is a comprehensive academic planning tool designed to help college students manage their coursework effectively. It provides an intuitive interface for tracking assignments, organizing courses, and staying on top of deadlines with a built-in calendar view.

## Features

- **Assignment Management**: Create, track, and manage assignments with due dates and status tracking
- **Course Organization**: Organize assignments by course/subject
- **Calendar View**: Visual calendar interface to see upcoming deadlines
- **Search Functionality**: Quickly find assignments and courses
- **Notification System**: Stay informed about upcoming deadlines
- **Data Persistence**: All data is saved locally using JSON storage
- **Semester Support**: Organize coursework by academic semester

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
  - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
  - On Raspberry Pi/Debian: `sudo apt install openjdk-17-jdk`
  - Verify installation: `java -version`

- **Apache Maven 3.6+**
  - Download from [Maven's website](https://maven.apache.org/download.cgi)
  - On Raspberry Pi/Debian: `sudo apt install maven`
  - Verify installation: `mvn -version`

- **JavaFX SDK 17.0.6** (automatically managed by Maven)
  - Note: Using JavaFX 17 for Raspberry Pi ARM64 compatibility

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/j-scheduler-v2.git
cd j-scheduler-v2
```

### 2. Install Dependencies

Maven will automatically download all required dependencies:

```bash
mvn clean install
```

This will:
- Download JavaFX 21.0.2
- Download Jackson 2.17.0 for JSON processing
- Download JUnit 5.10.2 for testing
- Compile the project
- Run tests (if any)

## Running the Application

### Using Maven

The simplest way to run the application:

```bash
mvn javafx:run
```

### Using Java (after building)

```bash
mvn clean package
java --module-path target/classes --module com.jscheduler/com.jscheduler.App
```

## Building the Application

### Create JAR

```bash
mvn clean package
```

The JAR file will be created in the `target/` directory.

### Run the JAR

```bash
java -jar target/j-scheduler-2.0.0.jar
```

## Project Structure

```
j-scheduler-v2/
├── src/
│   └── main/
│       ├── java/
│       │   ├── com/jscheduler/
│       │   │   ├── App.java                    # Main application entry point
│       │   │   ├── controller/                 # JavaFX controllers
│       │   │   │   ├── MainController.java
│       │   │   │   ├── AssignmentController.java
│       │   │   │   ├── CourseController.java
│       │   │   │   └── CalendarController.java
│       │   │   ├── model/                      # Data models
│       │   │   │   ├── Assignment.java
│       │   │   │   ├── Course.java
│       │   │   │   ├── Semester.java
│       │   │   │   └── AssignmentStatus.java
│       │   │   ├── service/                    # Business logic
│       │   │   │   ├── AssignmentService.java
│       │   │   │   ├── CourseService.java
│       │   │   │   ├── NotificationService.java
│       │   │   │   └── SearchService.java
│       │   │   ├── repository/                 # Data persistence
│       │   │   │   └── DataRepository.java
│       │   │   └── util/                       # Utility classes
│       │   │       └── DateUtil.java
│       │   └── module-info.java                # Java module descriptor
│       └── resources/
│           ├── fxml/                           # UI layouts
│           │   ├── main-view.fxml
│           │   ├── assignment-dialog.fxml
│           │   ├── course-dialog.fxml
│           │   └── calendar-view.fxml
│           └── css/
│               └── styles.css                  # Application styles
├── pom.xml                                     # Maven configuration
├── LICENSE                                     # MIT License
└── README.md                                   # This file
```

## Technology Stack

- **Java 17+**: Core programming language
- **JavaFX 17.0.6**: GUI framework (ARM64/Raspberry Pi compatible)
- **Jackson 2.17.0**: JSON serialization/deserialization
- **Maven**: Build and dependency management
- **JUnit 5**: Testing framework

### Platform Support

- **Desktop**: Windows, macOS, Linux
- **Raspberry Pi**: ARM64 architecture (tested on Raspberry Pi OS)

## Development

### IDE Setup

#### IntelliJ IDEA
1. Open the project folder
2. IntelliJ will automatically detect the Maven project
3. Wait for dependencies to download
4. Right-click on `App.java` and select "Run"

#### Eclipse
1. Import as Maven project: `File > Import > Maven > Existing Maven Projects`
2. Select the project root directory
3. Wait for dependencies to download
4. Right-click on the project and select `Run As > Maven build...`
5. Set goal to `javafx:run`

#### VS Code
1. Install the "Extension Pack for Java" and "JavaFX Support"
2. Open the project folder
3. Open a terminal and run `mvn javafx:run`

### Testing

Run tests with:

```bash
mvn test
```

## Data Storage

J-Scheduler stores all data locally in JSON format. The data files are typically stored in:

- **Linux/macOS**: `~/.jscheduler/data/`
- **Windows**: `%USERPROFILE%\.jscheduler\data\`

Data includes:
- Courses and assignments
- User preferences
- Calendar settings

## Troubleshooting

### JavaFX Runtime Error

If you encounter: `Error: JavaFX runtime components are missing`

**Solution**: Use Maven to run the application:
```bash
mvn javafx:run
```

### Java Version Mismatch

If you get compilation errors about Java version:

**Solution**: Ensure Java 17 or higher is installed and set as default:
```bash
java -version  # Should show version 17 or higher
export JAVA_HOME=/path/to/jdk17  # Linux/macOS
```

### Maven Dependencies Not Downloading

**Solution**: Clear Maven cache and reinstall:
```bash
mvn dependency:purge-local-repository
mvn clean install
```

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Jamie Scott

## Version History

- **2.0.0** - Current version with JavaFX UI and enhanced features
- **1.0.0** - Initial release

## Support

For questions or issues, please open an issue on the GitHub repository.

---

Made with JavaFX and Maven
