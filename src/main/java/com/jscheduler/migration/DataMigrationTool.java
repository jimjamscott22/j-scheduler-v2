package com.jscheduler.migration;

import com.jscheduler.model.Course;
import com.jscheduler.repository.DataRepository;
import com.jscheduler.repository.MySQLCourseRepository;

import java.util.List;
import java.util.Scanner;

/**
 * Standalone utility to migrate data from JSON to MySQL.
 * Run this once before switching to MySQL repository.
 *
 * Usage: mvn compile exec:java -Dexec.mainClass="com.jscheduler.migration.DataMigrationTool"
 */
public class DataMigrationTool {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  J-Scheduler Data Migration Tool");
        System.out.println("  JSON → MySQL Database Migration");
        System.out.println("===========================================");
        System.out.println();

        try (Scanner scanner = new Scanner(System.in)) {
            // Confirm migration
            System.out.print("Continue with migration? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes") && !response.equals("y")) {
                System.out.println("Migration cancelled.");
                return;
            }

            migrateData(scanner);

        } catch (Exception e) {
            System.err.println("\n✗ Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateData(Scanner scanner) {
        try {
            // Load from JSON
            System.out.println("\n[1/4] Loading data from JSON file...");
            DataRepository jsonRepo = new DataRepository();
            List<Course> courses = jsonRepo.getAllCourses();
            System.out.println("      ✓ Found " + courses.size() + " courses");

            if (courses.isEmpty()) {
                System.out.println("\n⚠ No data to migrate. Exiting.");
                return;
            }

            // Count assignments
            int totalAssignments = courses.stream()
                .mapToInt(c -> c.getAssignments() != null ?
                    c.getAssignments().size() : 0)
                .sum();
            System.out.println("      ✓ Found " + totalAssignments + " assignments");

            // Initialize MySQL
            System.out.println("\n[2/4] Connecting to MySQL database...");
            MySQLCourseRepository mysqlRepo = new MySQLCourseRepository();
            System.out.println("      ✓ Connected successfully");

            // Check for existing data
            List<Course> existingCourses = mysqlRepo.getAllCourses();
            if (!existingCourses.isEmpty()) {
                System.out.println("\n⚠ WARNING: Database already contains " +
                    existingCourses.size() + " courses");
                System.out.print("      Overwrite existing data? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes") && !response.equals("y")) {
                    System.out.println("\nMigration cancelled.");
                    return;
                }

                System.out.println("\n[3/4] Deleting existing data...");
                for (Course course : existingCourses) {
                    mysqlRepo.deleteCourse(course.getId());
                }
                System.out.println("      ✓ Existing data deleted");
            } else {
                System.out.println("\n[3/4] Database is empty, ready for migration");
            }

            // Migrate data
            System.out.println("\n[4/4] Migrating courses to MySQL...");
            int migratedCourses = 0;
            int migratedAssignments = 0;

            for (Course course : courses) {
                try {
                    mysqlRepo.addCourse(course);
                    migratedCourses++;

                    int assignmentCount = course.getAssignments() != null ?
                        course.getAssignments().size() : 0;
                    migratedAssignments += assignmentCount;

                    System.out.println("      ✓ " + course.getCode() +
                        " - " + course.getName() + " (" + assignmentCount +
                        " assignments)");

                } catch (Exception e) {
                    System.err.println("      ✗ ERROR migrating course " +
                        course.getCode() + ": " + e.getMessage());
                }
            }

            // Summary
            System.out.println("\n===========================================");
            System.out.println("  Migration Complete");
            System.out.println("===========================================");
            System.out.println("Courses migrated:     " + migratedCourses + "/" +
                courses.size());
            System.out.println("Assignments migrated: " + migratedAssignments + "/" +
                totalAssignments);

            if (migratedCourses == courses.size() &&
                migratedAssignments == totalAssignments) {
                System.out.println("\n✓ SUCCESS: All data migrated successfully!");
                System.out.println("\nNext steps:");
                System.out.println("1. Verify data in MySQL database");
                System.out.println("2. Test the application with MySQL");
                System.out.println("3. Backup JSON file: cp data/scheduler-data.json data/scheduler-data.json.backup");
            } else {
                System.out.println("\n⚠ WARNING: Some data may not have migrated.");
                System.out.println("   Please review errors above.");
            }

        } catch (Exception e) {
            System.err.println("\n✗ Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
