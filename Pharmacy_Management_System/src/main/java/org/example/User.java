package org.example;

public class User {
    private String email;

    private String password;

    private String salt;

    private String role;

    // Constructor
    public User(String email, String password, String role) {
    this.email = email;
    this.password = password;
    this.role = role;
}

    // Getters
    public String getEmail() { return email; }
    public String getHashedPassword() { return password; }
    public String getRole() { return role; }

    // Setters (if needed)
    public void setEmail(String email) { this.email = email; }
    public void setHashedPassword(String hashedPassword) { this.password = hashedPassword; }
    public void setRole(String role) { this.role = role; }
    public String getSalt() {

        return salt;

    }
}