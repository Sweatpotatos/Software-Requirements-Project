package org.example;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class PasswordUtils {

    // Constants for hashing
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    // Generate a secure random salt
    public static String getSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16]; // 128-bit salt
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hash the password with the given salt
    public static String hashPassword(String password, String salt) {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashedPassword = keyFactory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing a password: " + e.getMessage());
        }
    }

    // Verify the password
    public static boolean verifyPassword(String providedPassword, String securedPassword, String salt) {
        // Hash the provided password with the same salt
        String newSecurePassword = hashPassword(providedPassword, salt);
        // Check if the hashed passwords are equal
        return newSecurePassword.equals(securedPassword);
    }
}
