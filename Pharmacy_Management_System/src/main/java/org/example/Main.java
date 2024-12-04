package org.example;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Main {
    private static Scanner scanner = new Scanner(System.in); // Scanner for user input
    private static DatabaseHandler dbHandler = new DatabaseHandler(); // Database handler for DB operations
    private static LinkedListDSA<Drug> drugList = new LinkedListDSA<>(); // List to store drugs
    private static LinkedListDSA<Customer> customerList = new LinkedListDSA<>(); // List to store customers
    private static LinkedListDSA<Cart> cartList = new LinkedListDSA<>(); // List to store carts
    private static StackDSA actionStack = new StackDSA(100); // Stack to store user actions
    private static int cartIdCounter = 1; // cartIdCounter is incremented to ensure that each cart has a unique identifier.
    private static final String MANAGER_CODE = "MGR123";
    private static final String PHARMACIST_CODE = "PHARM456";
    private static final String TECHNICIAN_CODE = "TECH789";
    // public static void main(String[] args) {
    //     dbHandler.connect(); // Connect to the database
    //     loadInitialData(); // Load initial data from the database
    //     // WELCOME MESSAGE
    //     System.out.println("\n                                       *---*---*---*---*---*---*---*---*---*---*---*---*---*---*---*---*");
    //     System.out.println("                                       *--------------------------WELCOME TO---------------------------*");
    //     System.out.println("                                       *------------------PHARMACY MANAGEMENT SYSTEM-------------------*");
    //     System.out.println("                                       *---*---*---*---*---*---*---*---*---*---*---*---*---*---*---*---*");

    //     while (true) {
    //         showMainMenu(); // Show the main menu
    //         int choice = getInputInt();
    //         switch (choice) {
    //             case 1:
    //                 drugManagement(); // Manage drugs
    //                 break;
    //             case 2:
    //                 customerManagement(); // Manage customers
    //                 break;
    //             case 3:
    //                 manageCart(); // Manage cart
    //                 break;
    //             case 4:
    //                 actionStack.printStack(); // Print the actions stack
    //                 break;
    //             case 5:
    //                 report(); // Generate sales report
    //                 break;
    //             case 6:
    //                 showAlerts(); // Show alerts for low stock and expired drugs
    //                 break;
    //             case 7:
    //                 help(); // Show help information
    //                 break;
    //             case 8:
    //                 System.out.println("Exiting...");
    //                 dbHandler.closeConnection(); // Close the database connection
    //                 return;
    //             default:
    //                 System.out.println("Invalid choice. Please try again.");
    //         }
    //     }
    // }

        public static void main(String[] args) {
        dbHandler.connect(); // Connect to the database
        loadInitialData(); // Load initial data from the database
        // WELCOME MESSAGE
        System.out.println("\n*------------------------------------*");
        System.out.println("|      WELCOME TO THE PHARMACY       |");
        System.out.println("|        MANAGEMENT SYSTEM           |");
        System.out.println("*------------------------------------*");

        User currentUser = null;
        while (currentUser == null) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            int choice = getInputInt();
            switch (choice) {
                case 1:
                    currentUser = login();
                    break;
                case 2:
                    registerUser();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    dbHandler.closeConnection();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        while (true) {
            showMainMenu(currentUser); // Show the main menu based on user role
            int choice = getInputInt();
            handleMenuChoice(choice, currentUser);
        }
    
    }


    private static User login() {
    System.out.println("Enter email:");
    String email = getInputString();
    System.out.println("Enter password:");
    String password = getInputString();

    String query = "SELECT * FROM users WHERE email = ?";
    try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            String storedHash = rs.getString("password");
            String role = rs.getString("role");
            
            // Hash the input password and compare
            String hashedInput = hashPassword(password);
            
            if (hashedInput.equals(storedHash)) {
                System.out.println("Login successful!");
                return new User(email, storedHash, role);
            }
        }
        System.out.println("Invalid email or password");
    } catch (SQLException e) {
        System.out.println("Login error: " + e.getMessage());
    }
    return null;
}

private static void registerUser() {
    System.out.println("Enter your email:");
    String email = getInputString();
    
    if (!isValidEmail(email)) {
        System.out.println("Invalid email format");
        return;
    }
    
    if (isEmailExists(email)) {
        System.out.println("User already exists with this email.");
        return;
    }
    
    System.out.println("Enter your password:");
    String password = getInputString();
    
    System.out.println("\nSelect role:");
    System.out.println("1. Manager");
    System.out.println("2. Pharmacist");
    System.out.println("3. Technician");
    System.out.println("4. Patient");
    
    int roleChoice = getInputInt();
    String role;
    
    boolean isValidRegistration = false;
    
    switch (roleChoice) {
        case 1:
            role = "MANAGER";
            isValidRegistration = verifyStaffCode(role, MANAGER_CODE);
            break;
        case 2:
            role = "PHARMACIST";
            isValidRegistration = verifyStaffCode(role, PHARMACIST_CODE);
            break;
        case 3:
            role = "TECHNICIAN";
            isValidRegistration = verifyStaffCode(role, TECHNICIAN_CODE);
            break;
        case 4:
            role = "PATIENT";
            isValidRegistration = true;
            break;
        default:
            System.out.println("Invalid role selection");
            return;
    }
    
    if (isValidRegistration) {
        String hashedPassword = hashPassword(password);
        addUserToDatabase(email, hashedPassword, role);
        System.out.println("Registration successful!");
        if (role.equals("PATIENT")) {
            registerCustomerAfterUserRegistration(email);
        }

    } else {
        System.out.println("Registration failed - Invalid verification code");
    }
}


private static void registerCustomerAfterUserRegistration(String email) {
    System.out.println("\nComplete your customer profile:");
    System.out.println("Enter your name:");
    String name = getInputString();
    System.out.println("Enter your phone number:");
    String phoneNumber = getInputString();
    System.out.println("Enter your address:");
    String address = getInputString();

    Customer customer = new Customer(email, name, address, phoneNumber);
        customerList.add(customer);
        dbHandler.executeQuery("INSERT INTO Customers VALUES ('" + email + "', '" + name + "', '" + address + "', '" + phoneNumber + "')");
        System.out.println("Customer registered successfully!");
        actionStack.push("Registered customer with email: " + email);
}

private static boolean verifyStaffCode(String role, String correctCode) {
    System.out.println("Enter verification code for " + role + ":");
    String inputCode = getInputString();
    return inputCode.equals(correctCode);
}


private static void addUserToDatabase(String email, String hashedPassword, String role) {
    String query = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
        stmt.setString(1, email);
        stmt.setString(2, hashedPassword);
        stmt.setString(3, role);
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error creating user: " + e.getMessage());
    }
}

private static boolean isValidEmail(String email) {
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    return email.matches(emailRegex);
}

private static boolean isEmailExists(String email) {
    String query = "SELECT COUNT(*) FROM users WHERE email = ?";
    try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
        stmt.setString(1, email);
        
        // Debug prints
        System.out.println("Executing query: " + query);
        System.out.println("With email: " + email);
        
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);
            System.out.println("Found " + count + " matching emails"); // Debug print
            return count > 0;
        }
    } catch (SQLException e) {
        System.out.println("Error checking email: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
}

private static void addUserToDatabase(String email, String hashedPassword) {
    String query = "INSERT INTO users (email, password, role) VALUES (?, ?, 'USER')";
    try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
        stmt.setString(1, email);
        stmt.setString(2, hashedPassword);
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error adding user: " + e.getMessage());
        e.printStackTrace();
    }
}

private static String hashPassword(String password) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
    }
}

public static void showMainMenu(User currentUser) {
    System.out.println("\nMain Menu:");

    switch (currentUser.getRole().toLowerCase()) {
        case "manager":
            showManagerMenu();
            break;
        case "patient":
            showPatientMenu();
            break;
        default:
            System.out.println("Unknown role. Access denied.");
            System.exit(0);
    }
}


public static void showManagerMenu() {
    System.out.println("1. Drug Management");
    System.out.println("2. Customer Management");
    System.out.println("3. Manage Cart");
    System.out.println("4. View Action Stack");
    System.out.println("5. Generate Sales Report");
    System.out.println("6. Show Alerts");
    System.out.println("7. Help");
    System.out.println("8. Logout");
}


public static void showPatientMenu() {
    System.out.println("1. Drug Management");
    System.out.println("2. Manage Cart");
    System.out.println("3. Help");
    System.out.println("4. Logout");
}

public static void handleMenuChoice(int choice, User currentUser) {
    String role = currentUser.getRole().toLowerCase();
    switch (role) {
        case "manager":
            handleManagerChoice(choice, currentUser);
            break;
        case "patient":
            handlePatientChoice(choice, currentUser);
            break;
        default:
            System.out.println("Unknown role. Exiting.");
            System.exit(0);
    }
}


public static void handleManagerChoice(int choice, User currentUser) {
    switch (choice) {
        case 1:
            drugManagement(currentUser); // Your existing method
            break;
        case 2:
            customerManagement(); // Your existing method
            break;
        case 3:
            manageCart(); // Your existing method
            break;
        case 4:
            actionStack.printStack(); // Assuming actionStack is defined
            break;
        case 5:
            report(); // Your existing method
            break;
        case 6:
            showAlerts(); // Your existing method
            break;
        case 7:
            help(); // Your existing method
            break;
        case 8:
            logout();
            break;
        default:
            System.out.println("Invalid choice. Please try again.");
    }
}


public static void handlePatientChoice(int choice, User currentUser) {
    switch (choice) {
        case 1:
            drugManagement(currentUser); // You'll need to implement this method
            break;
        case 2:
            manageCart(); // You'll need to implement this method
            break;
        case 3:
            help(); // Assuming this method exists
            break;
        case 4:
            logout();
            break;
        default:
            System.out.println("Invalid choice. Please try again.");
    }
}




public static void logout() {
    System.out.println("Logging out...");
    dbHandler.closeConnection();
    System.exit(0);
}




    // Show the main menu
    // private static void showMainMenu() {
    //     System.out.println("\n--- Pharmacy Store Management System ---");
    //     System.out.println("1. Drug Management");
    //     System.out.println("2. Customer Management");
    //     System.out.println("3. Manage Cart");
    //     System.out.println("4. Print Actions Stack");
    //     System.out.println("5. Generate Sales Report");
    //     System.out.println("6. Alerts");
    //     System.out.println("7. Help");
    //     System.out.println("8. Exit");
    //     System.out.print("Enter your choice: ");
    // }

    // User class definition removed as it's now using org.example.User
    
   


    // Manage drugs (add, update, delete, view, list expired, help)
    private static void drugManagement(User currentUser) {
        while (true) {
            if (currentUser.getRole().equals("MANAGER")) {
                System.out.println("\n=== Drug Management (Manager) ===");
                System.out.println("1. Add Drug");
                System.out.println("2. Update Drug");
                System.out.println("3. Delete Drug");
                System.out.println("4. View Drug Inventory");
                System.out.println("5. List Expired Drugs");
                System.out.println("6. Help");
                System.out.println("7. Return to Main Menu");
                
                int choice = getInputInt();
                switch (choice) {
                    case 1:
                        addDrug();
                        break;
                    case 2:
                        updateDrug();
                        break;
                    case 3:
                        deleteDrug();
                        break;
                    case 4:
                        viewDrugInventory();
                        break;
                    case 5:
                        listExpiredDrugs();
                        break;
                    case 6:
                        help();
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else if (currentUser.getRole().equals("PATIENT")) {
                System.out.println("\n=== Drug Management (Patient) ===");
                System.out.println("1. View Drug Inventory");
                System.out.println("2. Search Drug");
                System.out.println("3. Help");
                System.out.println("4. Return to Main Menu");
                
                int choice = getInputInt();
                switch (choice) {
                    case 1:
                        viewDrugInventory();
                        break;
                    case 2:
                        listExpiredDrugs();
                        break;
                    case 3:
                        help();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else {
                System.out.println("Unauthorized access");
                return;
            }
        }
    }

    // Show drug management menu
    private static void showDrugMenu() {
        System.out.println("\n--- Drug Management ---");
        System.out.println("1. Add Drug");
        System.out.println("2. Update Drug");
        System.out.println("3. Delete Drug");
        System.out.println("4. View Drug Inventory");
        System.out.println("5. List Expired Drugs");
        System.out.println("6. Help");
        System.out.println("7. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // Manage customers (register, delete, update, manage cart)
    private static void customerManagement() {
        while (true) {
            showCustomerMenu(); // Show customer management menu
            int choice = getInputInt();
            switch (choice) {
                case 1:
                    registerCustomer(); // Register a new customer
                    break;
                case 2:
                    updateCustomer(); // Update customer information
                    break;
                case 3:
                    deleteCustomer(); // Delete a customer
                    break;
                case 4:
                    manageCart(); // Manage customer's cart
                    break;
                case 5:
                    return; // Return to main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Show customer management menu
    private static void showCustomerMenu() {
        System.out.println("\n--- Customer Management ---");
        System.out.println("1. Register Customer");
        System.out.println("2. Update Customer");
        System.out.println("3. Delete Customer");
        System.out.println("4. Manage Cart");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    // Register a new customer
    private static void
    registerCustomer() {
        String email;
        String name;
        String address;
        String phoneNumber;

        // email
        while (true) {
            System.out.print("Enter Customer Email: ");
            scanner.nextLine();
            email = scanner.nextLine().trim(); // Use trim to remove any leading or trailing whitespace
            if (!email.isEmpty()) {
                if (isEmailExists(email)) {
                    System.out.println("Email already exists in the database. Aborting registration.");
                    return; // Abort registration
                }
                break;
            } else {
                System.out.println("Email cannot be null");
            }
        }

        // Validate name
        while (true) {
            System.out.print("Enter Customer Name: ");
            name = scanner.nextLine().trim();
            if (name != null && isValidName(name) && !name.isEmpty()) {
                break;
            } else {
                System.out.println("Invalid name. Name cannot be null, contain numbers, or special characters.");
            }
        }

        // Validate address
        while (true) {
            System.out.print("Enter Customer Address: ");
            address = scanner.nextLine().trim();
            if (address != null && !address.isEmpty()) {
                break;
            } else {
                System.out.println("Address cannot be null or empty.");
            }
        }

        // Validate phone number
        while (true) {
            System.out.print("Enter Customer Phone Number: ");
            phoneNumber = scanner.nextLine().trim();
            if (isValidPhoneNumber(phoneNumber)) {
                break;
            } else {
                System.out.println("Invalid phone number. Phone number must contain only digits and be between 10 to 15 digits long.");
            }
        }

        Customer customer = new Customer(email, name, address, phoneNumber);
        customerList.add(customer);
        dbHandler.executeQuery("INSERT INTO Customers VALUES ('" + email + "', '" + name + "', '" + address + "', '" + phoneNumber + "')");
        System.out.println("Customer registered successfully!");
        actionStack.push("Registered customer with email: " + email);
    }


    // Update customer information
    private static void updateCustomer() {
        System.out.print("Enter Customer Email to update: ");
        scanner.nextLine();
        String email = scanner.nextLine();

        Customer customer = findCustomerByEmail(email);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        String name = "";
        String address = "";
        String phoneNumber = "";

        // Validate name
        while (true) {
            System.out.print("Enter new Customer Name: ");
            name = scanner.nextLine();
            if (isValidName(name)) {
                break;
            } else {
                System.out.println("Invalid name. Name cannot contain numbers or special characters.");
            }
        }

        // Address input
        System.out.print("Enter new Customer Address: ");
        address = scanner.nextLine();

        // Validate phone number
        while (true) {
            System.out.print("Enter new Customer Phone Number: ");
            phoneNumber = scanner.nextLine();
            if (isValidPhoneNumber(phoneNumber)) {
                break;
            } else {
                System.out.println("Invalid phone number. Phone number must contain only digits and be between 10 to 15 digits long.");
            }
        }

        customer.setName(name);
        customer.setAddress(address);
        customer.setPhoneNumber(phoneNumber);

        dbHandler.executeQuery("UPDATE Customers SET name='" + name + "', address='" + address + "', phone_number='" + phoneNumber + "' WHERE email='" + email + "'");
        System.out.println("Customer updated successfully!");
        actionStack.push("Updated customer with email: " + email);
    }

    // Delete a customer
    private static void deleteCustomer() {
        System.out.print("Enter Customer Email to delete: ");
        scanner.nextLine();
        String email = scanner.nextLine();

        Customer customer = findCustomerByEmail(email);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        dbHandler.deleteCustomerByEmail(email);
        customerList.remove(customer);
        System.out.println("Customer deleted successfully!");
        actionStack.push("Deleted customer with email: " + email);
    }

    // Add a new drug
    private static void addDrug() {
        // Fetch the maximum drug_id from the database and increment it
        int drugId = getNextDrugId();
        System.out.println("New Generated Drug ID: "+drugId);
        scanner.nextLine(); // Consume newLine

        System.out.print("Enter Drug Name: ");
        String drugName = scanner.nextLine();
        while (drugName.isEmpty()) {
            System.out.print("Drug Name cannot be empty. Enter Drug Name: ");
            drugName = scanner.nextLine();
        }

        System.out.print("Enter Manufacturer: ");
        String manufacturer = scanner.nextLine();
        while (!manufacturer.matches("[a-zA-Z\\s]+")) {
            System.out.print("Manufacturer cannot be null or number . Enter Manufacturer: ");
            manufacturer = scanner.nextLine();
        }

        System.out.print("Enter Expiry Date (yyyy-MM-dd): ");
        String expiryDateStr = scanner.nextLine();
        Date expiryDate = null;
        while (true) {
            try {
                expiryDate = new SimpleDateFormat("yyyy-MM-dd").parse(expiryDateStr);
                if (expiryDate.before(new Date())) {
                    System.out.print("Expiry Date cannot be in the past. Enter Expiry Date (yyyy-MM-dd): ");
                    expiryDateStr = scanner.nextLine();
                } else {
                    break;
                }
            } catch (ParseException e) {
                System.out.print("Invalid date format. Enter Expiry Date (yyyy-MM-dd): ");
                expiryDateStr = scanner.nextLine();
            }
        }

        System.out.print("Enter Quantity: ");
        int quantity = getInputInt();
        while (quantity < 0) {
            System.out.print("Quantity cannot be negative. Enter Quantity: ");
            quantity = getInputInt();
        }

        System.out.print("Enter Price: ");
        double price = getInputDouble();
        while (price < 0) {
            System.out.print("Price cannot be negative. Enter Price: ");
            price = getInputDouble();
        }
        scanner.nextLine(); // Consume newline

        System.out.print("Enter Description: ");
        String description = scanner.nextLine();
        while (description.matches("[0-9]+")) {
            System.out.print("Description cannot be full of numbers. Enter Description: ");
            description = scanner.nextLine();
        }

        System.out.print("Enter Tags: ");
        String tags = scanner.nextLine();
        while (tags.matches("[0-9]+")) {
            System.out.print("Tags cannot be full of numbers. Enter Tags: ");
            tags = scanner.nextLine();
        }

        Drug drug = new Drug(drugId, drugName, manufacturer, expiryDate, quantity, price, description, tags);
        drugList.add(drug);
        dbHandler.executeQuery("INSERT INTO Drugs (drug_id, drug_name, manufacturer, expiry_date, quantity, price, description, tags) VALUES (" + drugId + ", '" + drugName + "', '" + manufacturer + "', '" + expiryDateStr + "', " + quantity + ", " + price + ", '" + description + "', '" + tags + "')");
        System.out.println("Drug added successfully!");
        actionStack.push("Added drug with ID: " + drugId);
    }


    // Update an existing drug
    private static void updateDrug() {
        System.out.print("Enter Drug ID to update: ");
        int drugId = getInputInt();
        scanner.nextLine(); // Consume newline

        Drug drug = findDrugById(drugId);
        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }

        System.out.println("Enter 'none' if you do not want to update a field.");

        System.out.print("Enter new Drug Name [" + drug.getDrugName() + "]: ");
        String drugName = scanner.nextLine();
        if (!drugName.equalsIgnoreCase("none")) {
            if (drugName.isEmpty()) {
                System.out.println("Drug Name cannot be empty. Keeping the old name.");
            } else {
                drug.setDrugName(drugName);
            }
        }

        System.out.print("Enter new Manufacturer [" + drug.getManufacturer() + "]: ");
        String manufacturer = scanner.nextLine();
        if (!manufacturer.equalsIgnoreCase("none")) {
            if (manufacturer.isEmpty()) {
                System.out.println("Manufacturer cannot be empty. Keeping the old manufacturer.");
            } else {
                drug.setManufacturer(manufacturer);
            }
        }

        System.out.print("Enter new Expiry Date (yyyy-MM-dd) [" + drug.getExpiryDate() + "]: ");
        String expiryDateStr = scanner.nextLine();
        if (!expiryDateStr.equalsIgnoreCase("none")) {
            try {
                Date expiryDate = new SimpleDateFormat("yyyy-MM-dd").parse(expiryDateStr);
                if (expiryDate.before(new Date())) {
                    System.out.println("Expiry Date cannot be in the past. Keeping the old expiry date.");
                } else {
                    drug.setExpiryDate(expiryDate);
                }
            } catch (ParseException e) {
                System.out.println("Invalid date format. Keeping the old date.");
            }
        }

        System.out.print("Enter new Quantity [" + drug.getQuantity() + "]: ");
        String quantityStr = scanner.nextLine();
        if (!quantityStr.equalsIgnoreCase("none")) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    System.out.println("Quantity cannot be negative. Keeping the old quantity.");
                } else {
                    drug.setQuantity(quantity);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity format. Keeping the old quantity.");
            }
        }

        System.out.print("Enter new Price [" + drug.getPrice() + "]: ");
        String priceStr = scanner.nextLine();
        if (!priceStr.equalsIgnoreCase("none")) {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    System.out.println("Price cannot be negative. Keeping the old price.");
                } else {
                    drug.setPrice(price);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Keeping the old price.");
            }
        }

        System.out.print("Enter new Description [" + drug.getDescription() + "]: ");
        String description = scanner.nextLine();
        if (!description.equalsIgnoreCase("none")) {
            if (description.isEmpty()) {
                System.out.println("Description cannot be empty. Keeping the old description.");
            } else {
                drug.setDescription(description);
            }
        }

        System.out.print("Enter new Tags [" + drug.getTags() + "]: ");
        String tags = scanner.nextLine();
        if (!tags.equalsIgnoreCase("none")) {
            if (tags.isEmpty()) {
                System.out.println("Tags cannot be empty. Keeping the old tags.");
            } else {
                drug.setTags(tags);
            }
        }

        // Update the database with the new values
        String expiryDateStrForDB = new SimpleDateFormat("yyyy-MM-dd").format(drug.getExpiryDate());
        dbHandler.executeQuery("UPDATE Drugs SET drug_name='" + drug.getDrugName() + "', manufacturer='" + drug.getManufacturer() + "', expiry_date='" + expiryDateStrForDB + "', quantity=" + drug.getQuantity() + ", price=" + drug.getPrice() + ", description='" + drug.getDescription() + "', tags='" + drug.getTags() + "' WHERE drug_id=" + drugId);
        System.out.println("Drug updated successfully!");
        actionStack.push("Updated drug with ID: " + drugId);
    }


    // Delete a drug
    private static void deleteDrug() {
        System.out.print("Enter Drug ID to delete: ");
        int drugId = getInputInt();
        scanner.nextLine(); // Consume newline

        Drug drug = findDrugById(drugId);
        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }

        // Perform cascading deletes
        dbHandler.deleteDrugById(drugId);

        // Remove the drug from the local list
        drugList.remove(drug);
        System.out.println("Drug deleted successfully along with all related data!");
        actionStack.push("Deleted drug with ID: " + drugId);
    }

    // View all drugs in inventory
    private static void viewDrugInventory() {
        System.out.println("\n--- Drug Inventory ---");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s\n", "Drug ID", "Drug Name", "Expiry Date", "Quantity", "Price");

        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            System.out.printf("%-10d %-20s %-15s %-10d %-10.2f\n", drug.getDrugId(), drug.getDrugName(), new SimpleDateFormat("dd-MM-yyyy").format(drug.getExpiryDate()), drug.getQuantity(), drug.getPrice());
        }
        actionStack.push("Viewed drug inventory");
    }

    // List expired drugs
    private static void listExpiredDrugs() {
        System.out.println("\n--- Expired Drugs ---");
        System.out.printf("%-10s %-20s %-20s %-15s %-10s %-10s\n", "Drug ID", "Drug Name", "Manufacturer", "Expiry Date", "Quantity", "Price");

        Date currentDate = new Date();
        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getExpiryDate().before(currentDate)) {
                System.out.printf("%-10d %-20s %-20s %-15s %-10d %-10.2f\n", drug.getDrugId(), drug.getDrugName(), drug.getManufacturer(), new SimpleDateFormat("dd-MM-yyyy").format(drug.getExpiryDate()), drug.getQuantity(), drug.getPrice());
            }
        }
        actionStack.push("Listed expired drugs");
    }

    // Manage customer's cart (add drug, view cart, checkout, help)
    private static void manageCart() {
        System.out.print("Enter Customer Email to manage cart: ");
        scanner.nextLine();
        String email = scanner.nextLine();

        Customer customer = findCustomerByEmail(email);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        Cart cart = findCartByEmail(email);
        if (cart == null) {
            cart = new Cart(cartIdCounter++, email);
            cartList.add(cart);
        }

        while (true) {
            showCartMenu();
            int choice = getInputInt();
            switch (choice) {
                case 1:
                    addDrugToCart(cart);
                    break;
                case 2:
                    viewCart(cart);
                    break;
                case 3:
                    checkout(cart);
                    return;
                case 4:
                    help();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Show cart management menu
    private static void showCartMenu() {
        System.out.println("\n--- Cart Management ---");
        System.out.println("1. Add Drug to Cart");
        System.out.println("2. View Cart");
        System.out.println("3. Checkout");
        System.out.println("4. Help");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    // Add a drug to the cart
    private static void addDrugToCart(Cart cart) {
        System.out.println("\n--- Available Drugs ---");
        System.out.printf("%-10s %-20s", "Drug ID", "Drug Name");

        int count = 0;

        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (!drug.getExpiryDate().before(new Date())) {
                if (count % 5 == 0) {
                    System.out.println(); // Move to the next line after every 5 drugs
                }
                System.out.printf("%-10d %-20s", drug.getDrugId(), drug.getDrugName());
                count++;
            }
        }
        System.out.println(); // Ensure we move to the next line after the loop

        System.out.print("\nEnter Drug ID or Drug Name to add to cart: ");
        scanner.nextLine();
        String input = scanner.nextLine();
        Drug drug = null;

        try {
            int drugId = Integer.parseInt(input);
            drug = findDrugById(drugId);
        } catch (NumberFormatException e) {
            drug = findDrugByName(input);
        }

        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }

        if (drug.getExpiryDate().before(new Date())) {
            System.out.println("Cannot add expired drug to cart.");
            return;
        }

        System.out.print("Enter Quantity: ");
        int quantity = getInputInt();

        // Check if the quantity is available
        if (drug.getQuantity() < quantity) {
            System.out.println("Insufficient stock. Available quantity: " + drug.getQuantity());
            return;
        }

        CartItem cartItem = new CartItem(drug, quantity);
        cart.addToCart(cartItem);

        try {
            String email = cart.getEmail();
            dbHandler.executeQuery("INSERT INTO Cart (email, drug_id, quantity) VALUES ('" + email + "', " + drug.getDrugId() + ", " + quantity + ") " +
                    "ON CONFLICT (email, drug_id) DO UPDATE SET quantity = Cart.quantity + EXCLUDED.quantity");

            // Update the drug quantity in the database
            int newQuantity = drug.getQuantity() - quantity;
            dbHandler.executeQuery("UPDATE Drugs SET quantity = " + newQuantity + " WHERE drug_id = " + drug.getDrugId());
            // Update the drug quantity in the local list
            drug.setQuantity(newQuantity);
            // Update the drug quantity in the LinkedList
            updateDrugQuantityInList(drug.getDrugId(), -quantity, false);

            System.out.println("Drug added to cart successfully!");
            actionStack.push("Added drug to cart: " + drug.getDrugId() + " with quantity: " + quantity);
        } catch (Exception e) {
            System.out.println("Error updating cart: " + e.getMessage());
        }

    }

    // Find drug by ID
    private static Drug findDrugById(int drugId) {
        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getDrugId() == drugId) {
                return drug;
            }
        }
        return null;
    }

    // Find drug by name
    private static Drug findDrugByName(String drugName) {
        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getDrugName().equalsIgnoreCase(drugName)) {
                return drug;
            }
        }
        return null;
    }

    // View items in the cart
    private static void viewCart(Cart cart) {
        System.out.println("\n--- Cart Items ---");

        // Query to fetch cart items from the database
        String query = "SELECT c.drug_id, d.drug_name, d.price, c.quantity " + "FROM Cart c " + "JOIN Drugs d ON c.drug_id = d.drug_id " + "WHERE c.email = '" + cart.getEmail() + "'";

        ResultSet resultSet = dbHandler.executeSelectQuery(query);

        try {
            if (!resultSet.isBeforeFirst()) {
                System.out.println("Cart is empty.");
                return;
            }

            System.out.printf("%-10s %-20s %-10s %-10s\n", "Drug ID", "Drug Name", "Quantity", "Price");

            // To keep track of drugs in the cart
            HashMap<Integer, Integer> cartDrugIds = new HashMap<>();

            while (resultSet.next()) {
                int drugId = resultSet.getInt("drug_id");
                String drugName = resultSet.getString("drug_name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");

                cartDrugIds.put(drugId, quantity);  // Track drugs in the cart

                System.out.printf("%-10d %-20s %-10d %-10.2f\n", drugId, drugName, quantity, price);
            }

            // Choice for removing items from the cart
            System.out.print("\nDo you want to remove any drug from the cart? (yes/no): ");
            scanner.nextLine();
            String response = scanner.nextLine();

            if (response.equalsIgnoreCase("yes")) {
                System.out.print("Enter Drug ID to remove: ");
                int drugIdToRemove = getInputInt();

                // Check if the drug ID is in the user's cart
                if (cartDrugIds.containsKey(drugIdToRemove)) {
                    int quantityToRemove = cartDrugIds.get(drugIdToRemove);

                    // Remove the drug from the cart in the database
                    String deleteQuery = "DELETE FROM Cart WHERE email='" + cart.getEmail() + "' AND drug_id=" + drugIdToRemove;
                    dbHandler.executeQuery(deleteQuery);

                    // Update the drug quantity back in the drug table
                    String updateDrugQuantityQuery = "UPDATE Drugs SET quantity = quantity + " + quantityToRemove + " WHERE drug_id = " + drugIdToRemove;
                    dbHandler.executeQuery(updateDrugQuantityQuery);

                    // Update the drug quantity in the LinkedList
                    updateDrugQuantityInList(drugIdToRemove, quantityToRemove, true);

                    System.out.println("Drug removed from cart successfully and quantity updated in inventory!");
                    actionStack.push("Removed drug from cart: " + drugIdToRemove);

                } else {
                    System.out.println("Error: Drug ID " + drugIdToRemove + " is not in your cart.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching cart data: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }


    // Help method to provide drug information
    private static void help() {
        System.out.print("\nEnter drug description, tag, name, or ID to get information: \n");
        scanner.nextLine();
        String input = scanner.nextLine();

        Drug drug = null;

        try {
            int drugId = Integer.parseInt(input);
            drug = findDrugById(drugId);
        } catch (NumberFormatException e) {
            drug = findDrugByName(input);
            if (drug == null) {
                drug = findDrugByDescriptionOrTag(input);
            }
        }

        if (drug != null) {
            System.out.println("----Drug Information----");
            System.out.println("ID: " + drug.getDrugId());
            System.out.println("Name: " + drug.getDrugName());
            System.out.println("Manufacturer: " + drug.getManufacturer());
            System.out.println("Expiry Date: " + new SimpleDateFormat("dd-MM-yyyy").format(drug.getExpiryDate()));
            System.out.println("Quantity: " + drug.getQuantity());
            System.out.println("Price: " + drug.getPrice());
            System.out.println("Description: " + drug.getDescription());
            System.out.println("Tags: " + drug.getTags());
            actionStack.push("Viewed information for drug: " + drug.getDrugId());
        } else {
            System.out.println("Drug not found.");
        }
    }

    // Method to generate sales report
    private static void report() {
        System.out.print("Enter the start date (yyyy-MM-dd): ");
        scanner.nextLine(); // Consume newline
        String startDateStr = scanner.nextLine();

        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr);
            Date currentDate = new Date();
            dbHandler.generateSalesReport(startDate, currentDate);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please try again.");
        }
    }

    // Show alerts for low stock and expired drugs
    private static void showAlerts() {
        System.out.println("\n--- Low Stock Alerts (Quantity < 20) ---");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s\n", "Drug ID", "Drug Name", "Manufacturer", "Quantity", "Price");

        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getQuantity() < 20) {
                System.out.printf("%-10d %-20s %-15s %-10d %-10.2f\n",
                        drug.getDrugId(),
                        drug.getDrugName(),
                        drug.getManufacturer(),
                        drug.getQuantity(),
                        drug.getPrice());
            }
        }

        System.out.println("\n--- Expired Drugs ---");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s\n", "Drug ID", "Drug Name", "Expiry Date", "Quantity", "Price");

        Date currentDate = new Date();
        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getExpiryDate().before(currentDate)) {
                System.out.printf("%-10d %-20s %-15s %-10d %-10.2f\n",
                        drug.getDrugId(),
                        drug.getDrugName(),
                        new SimpleDateFormat("dd-MM-yyyy").format(drug.getExpiryDate()),
                        drug.getQuantity(),
                        drug.getPrice());
            }
        }
        actionStack.push("Viewed drug Alerts Information: ");
    }

    /*
      Handles the checkout process for a customer's cart.

      This method finalizes the purchase for the items in the customer's cart. It performs the following steps:
      1. Validates that the cart is not empty.
      2. Calculates the total amount for the items in the cart.
      3. Creates an Order object and saves the order details in the database.
      4. Generates an invoice for the order.
      5. Clears the cart and updates the drug quantities in the database.
      6. Logs the action to the action stack.

     */
    private static void checkout(Cart cart) {
        // Check if the cart is empty in the database
        if (isCartEmptyInDatabase(cart.getEmail())) {
            System.out.println("Cart is empty. Cannot checkout.");
            return;
        }

        double totalAmount = calculateTotalAmount(cart);

        if (totalAmount == 0) {
            System.out.println("Total amount is zero. Cannot checkout.");
            return;
        }

        Order order = new Order(cart.getCartId(), cart.getEmail(), new Date(), totalAmount, cart.getItems());

        try {
            order.saveOrder();
            order.generateInvoice();

            cart.getItems().clear();
            dbHandler.deleteCartByEmail(cart.getEmail());
            System.out.println("Checkout successful! Invoice generated.");
            actionStack.push("Checked out cart with ID: " + cart.getCartId());
        } catch (Exception e) {
            System.out.println("Error during checkout: " + e.getMessage());
        }
    }

    private static boolean isCartEmptyInDatabase(String email) {
        String query = "SELECT COUNT(*) AS item_count FROM Cart WHERE email = '" + email + "'";
        ResultSet resultSet = dbHandler.executeSelectQuery(query);

        try {
            if (resultSet != null && resultSet.next()) {
                int itemCount = resultSet.getInt("item_count");
                return itemCount == 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking cart items: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }

        return true; // Default to empty if there's an error
    }

    // Calculate total amount in the cart
    private static double calculateTotalAmount(Cart cart) {
        double totalAmount = 0;
        LinkedListDSA<CartItem> items = cart.getItems();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getDrug() != null && item.getQuantity() > 0) {
                totalAmount += item.getQuantity() * item.getDrug().getPrice();
            } else {
                System.out.println("Invalid item in cart: " + item);
            }
        }
        return totalAmount;
    }


    // Find customer by email
    private static Customer findCustomerByEmail(String email) {
        for (int i = 0; i < customerList.size(); i++) {
            Customer customer = customerList.get(i);
            if (customer.getEmail().equals(email)) {
                return customer;
            }
        }
        return null;
    }

    // Find cart by email
    private static Cart findCartByEmail(String email) {
        for (int i = 0; i < cartList.size(); i++) {
            Cart cart = cartList.get(i);
            if (cart.getEmail().equals(email)) {
                return cart;
            }
        }
        return null;
    }

    // Find drug by description or tag
    private static Drug findDrugByDescriptionOrTag(String input) {
        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getDescription().toLowerCase().contains(input.toLowerCase()) || drug.getTags().toLowerCase().contains(input.toLowerCase())) {
                return drug;
            }
        }
        return null;
    }

    private static void updateDrugQuantityInList(int drugId, int quantityChange, boolean isAddingBack) {
        for (int i = 0; i < drugList.size(); i++) {
            Drug drug = drugList.get(i);
            if (drug.getDrugId() == drugId) {
                if (isAddingBack) {
                    drug.setQuantity(drug.getQuantity() + quantityChange); // Add back the quantity
                } else {
                    drug.setQuantity(drug.getQuantity() - quantityChange); // Subtract the quantity
                }
                return;
            }
        }
    }


    // Load initial data from the database
    private static void loadInitialData() {
        loadDrugData();
        loadCustomerData();
        loadCartData();
    }

    // Load drug data from the database
    private static void loadDrugData() {
        String query = "SELECT * FROM Drugs ORDER BY drug_id ASC";
        ResultSet resultSet = dbHandler.executeSelectQuery(query);

        try {
            while (resultSet != null && resultSet.next()) {
                int drugId = resultSet.getInt("drug_id");
                String drugName = resultSet.getString("drug_name");
                String manufacturer = resultSet.getString("manufacturer");
                Date expiryDate = resultSet.getDate("expiry_date");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                String description = resultSet.getString("description");
                String tags = resultSet.getString("tags");

                Drug drug = new Drug(drugId, drugName, manufacturer, expiryDate, quantity, price, description, tags);
                drugList.add(drug);
            }
            System.out.println("Initial drug data loaded successfully!");
        } catch (SQLException e) {
            System.out.println("Error loading drug data: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }

    private static void loadCustomerData() {
        String query = "SELECT * FROM Customers";
        ResultSet resultSet = dbHandler.executeSelectQuery(query);

        try {
            while (resultSet != null && resultSet.next()) {
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String phoneNumber = resultSet.getString("phone_number");

                Customer customer = new Customer(email, name, address, phoneNumber);
                customerList.add(customer);
            }
            System.out.println("Initial customer data loaded successfully!");
        } catch (SQLException e) {
            System.out.println("Error loading customer data: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }

    private static void loadCartData() {
        String query = "SELECT * FROM Cart";
        ResultSet resultSet = dbHandler.executeSelectQuery(query);

        try {
            while (resultSet != null && resultSet.next()) {
                int cartId = resultSet.getInt("cart_id");
                String email = resultSet.getString("email");
                int drugId = resultSet.getInt("drug_id");
                int quantity = resultSet.getInt("quantity");

                Drug drug = findDrugById(drugId);
                if (drug != null) {
                    CartItem cartItem = new CartItem(drug, quantity);
                    Cart cart = findCartByEmail(email);
                    if (cart == null) {
                        cart = new Cart(cartId, email);
                        cartList.add(cart);
                    }
                    cart.addToCart(cartItem);
                }
            }
            System.out.println("Initial cart data loaded successfully!");
        } catch (SQLException e) {
            System.out.println("Error loading cart data: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }

    // Utility method to handle integer input
    private static int getInputInt() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    // Utility method to handle double input
    private static double getInputDouble() {
        while (true) {
            try {
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    private static boolean isValidName(String name) {
        if (name == null) {
            System.out.println("ERROR: Name cannot be null");
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 10 && phoneNumber.length() <= 15) {
            for (char c : phoneNumber.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Utility method to handle string input
    private static String getInputString() {
    Scanner scanner = new Scanner(System.in);
    String input = scanner.nextLine().trim();
    
    // Validate input is not empty
    while (input.isEmpty()) {
        System.out.println("Input cannot be empty. Please try again:");
        input = scanner.nextLine().trim();
    }
    
    return input;
}

    

    // Method to get the next drug ID
    private static int getNextDrugId() {
        String query = "SELECT COALESCE(MAX(drug_id), 999) + 1 FROM Drugs"; // drug_id starts from 1000
        ResultSet rs = dbHandler.executeSelectQuery(query);
        try {
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching next drug ID: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }
        return 1000; // Return 1000 if something goes wrong, assuming it’s the starting ID
    }
}
