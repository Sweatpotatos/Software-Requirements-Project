package org.example;

public class Customer {
    private String email;
    private String name;
    private String address;
    private String phoneNumber;
    private String allergies;

    public Customer(String email, String name, String address, String phoneNumber, String allergies) {
        this.email = email;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.allergies = allergies;
    }

    // Getters and Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getAllergies() { return allergies; }

    public String getName() {
        return name;
    }

    public void setAllergies(String allergies) { this.allergies = allergies; }
    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", allergies='" + allergies + '\'' +
                '}';
    }
}
