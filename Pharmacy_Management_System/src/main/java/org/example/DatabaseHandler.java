package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DatabaseHandler {
    private Connection connection;
    public Connection getConnection() {

        return connection;

    }
    public void connect() {
        try {
            String url = "jdbc:postgresql://localhost:5432/pharmacy";
            String user = "postgres";  // Replace with your database username
            String password = "Andybian5";  // Replace with your database password
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection to the database established successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
    }

    // Return Nothing [Only Use for Insert, Update, Delete]
    public void executeQuery(String query) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    // Return ResultSet [Only Use with Select Query]
    public ResultSet executeSelectQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Error executing select query: " + e.getMessage());
            return null;
        }
    }

    public void deleteCustomerByEmail(String email) {
        String deleteOrderItemsQuery = "DELETE FROM OrderItems WHERE order_id IN (SELECT order_id FROM Orders WHERE email = ?)";
        String deleteOrdersQuery = "DELETE FROM Orders WHERE email = ?";
        String deleteCartQuery = "DELETE FROM Cart WHERE email = ?";
        String deleteCustomerQuery = "DELETE FROM Customers WHERE email = ?";

        try (PreparedStatement pstOrderItems = connection.prepareStatement(deleteOrderItemsQuery);
             PreparedStatement pstOrders = connection.prepareStatement(deleteOrdersQuery);
             PreparedStatement pstCart = connection.prepareStatement(deleteCartQuery);
             PreparedStatement pstCustomer = connection.prepareStatement(deleteCustomerQuery)) {

            pstOrderItems.setString(1, email);
            pstOrderItems.executeUpdate();

            pstOrders.setString(1, email);
            pstOrders.executeUpdate();

            pstCart.setString(1, email);
            pstCart.executeUpdate();

            pstCustomer.setString(1, email);
            pstCustomer.executeUpdate();

            System.out.println("Customer and associated data deleted successfully for email: " + email);
        } catch (SQLException e) {
            System.out.println("Error deleting customer data: " + e.getMessage());
        }
    }

    public void deleteCartByEmail(String email) {
        String deleteCartQuery = "DELETE FROM Cart WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(deleteCartQuery)) {
            pst.setString(1, email);
            pst.executeUpdate();

            System.out.println("Cart data deleted successfully for email: " + email);
        } catch (SQLException e) {
            System.out.println("Error deleting cart data: " + e.getMessage());
        }
    }

    public void deleteDrugById(int drugId) {
        String deleteOrderItemsQuery = "DELETE FROM OrderItems WHERE drug_id = ?";
        String deleteCartQuery = "DELETE FROM Cart WHERE drug_id = ?";
        String deleteDrugQuery = "DELETE FROM Drugs WHERE drug_id = ?";

        try (PreparedStatement pstOrderItems = connection.prepareStatement(deleteOrderItemsQuery);
             PreparedStatement pstCart = connection.prepareStatement(deleteCartQuery);
             PreparedStatement pstDrug = connection.prepareStatement(deleteDrugQuery)) {

            pstOrderItems.setInt(1, drugId);
            pstOrderItems.executeUpdate();

            pstCart.setInt(1, drugId);
            pstCart.executeUpdate();

            pstDrug.setInt(1, drugId);
            pstDrug.executeUpdate();

            System.out.println("Drug and associated data deleted successfully for drug ID: " + drugId);
        } catch (SQLException e) {
            System.out.println("Error deleting drug data: " + e.getMessage());
        }
    }

    public void generateSalesReport(Date startDate, Date endDate) {
        String query = "SELECT o.order_id, o.order_date, c.email, c.name, " +
                       "oi.drug_id, d.drug_name, oi.quantity, oi.price, (oi.quantity * oi.price) AS earnings " +
                       "FROM OrderItems oi " +
                       "JOIN Orders o ON oi.order_id = o.order_id " +
                       "JOIN Drugs d ON oi.drug_id = d.drug_id " +
                       "JOIN Customers c ON o.email = c.email " + // Corrected join condition
                       "WHERE o.order_date >= ? AND o.order_date <= ? " +
                       "ORDER BY o.order_date, o.order_id";
    
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, new java.sql.Date(startDate.getTime()));
            statement.setDate(2, new java.sql.Date(endDate.getTime()));
            ResultSet resultSet = statement.executeQuery();
    
            String filePath = "sales_report_" + new SimpleDateFormat("yyyyMMdd").format(endDate) + ".txt";
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write("Sales Report\n");
                writer.write("Start Date: " + new SimpleDateFormat("yyyy-MM-dd").format(startDate) + "\n");
                writer.write("End Date: " + new SimpleDateFormat("yyyy-MM-dd").format(endDate) + "\n\n");
    
                double grandTotal = 0.0;
                
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id");
                    Date orderDate = new Date(resultSet.getDate("order_date").getTime());
                    String customerEmail = resultSet.getString("email");
                    String customerName = resultSet.getString("name");
                    String drugName = resultSet.getString("drug_name");
                    int quantity = resultSet.getInt("quantity");
                    double price = resultSet.getDouble("price");
                    double earnings = resultSet.getDouble("earnings");
                    
                    writer.write("Order ID: " + orderId + "\n");
                    writer.write("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(orderDate) + "\n");
                    writer.write("Customer: " + customerName + " (" + customerEmail + ")\n");
                    writer.write(String.format("Drug: %s, Quantity: %d, Price: $%.2f, Total: $%.2f\n", 
                        drugName, quantity, price, earnings));
                    writer.write("----------------------------------------\n");
                    
                    grandTotal += earnings;
                }
                
                writer.write(String.format("\nGrand Total: $%.2f\n", grandTotal));
                System.out.println("Sales report generated: " + filePath);
                
            } catch (IOException e) {
                System.out.println("Error writing sales report: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error generating sales report: " + e.getMessage());
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed!");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }
    

    public boolean saveUser(User user) {
        String sql = "INSERT INTO users (username, hashed_password, salt, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getEmail()); 
            pstmt.setString(2, user.getHashedPassword());
            pstmt.setString(3, user.getSalt());
            pstmt.setString(4, user.getRole());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT username, hashed_password, salt, role FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
    
            if (rs.next()) {
                String hashedPassword = rs.getString("hashed_password");
                String salt = rs.getString("salt");
                String role = rs.getString("role");
                return new User(username, hashedPassword,role);
            } else {
                return null; // User not found
            }
        } catch (SQLException e) {
            System.out.println("Error getting user: " + e.getMessage());
            return null;
        }
    }
    
    
}
