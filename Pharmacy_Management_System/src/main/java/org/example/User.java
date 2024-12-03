package org.example;

public class User {
    private String username;
    private String hashedPassword;
    private String salt;
    private String role; // e.g., Manager, Pharmacist, Technician, Patient

    // Constructor
    public User(String username, String hashedPassword, String salt, String role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.role = role;
    }

    // Getters
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getSalt() { return salt; }
    public String getRole() { return role; }

    // Setters (if needed)
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setRole(String role) { this.role = role; }
}

