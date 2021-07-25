package com.github.astronoodles.peerpal.extras;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CryptographyHelper {
    /*
    Creating all of the secure items for the cryptography algorithm to store the password
    - RAND -> better version of using psuedorandom algorithms for cryptography
    - ITERATIONS --> # of times to repeat the hashing algorithm
    - KEY_LENGTH --> the length of the encoded password in bytes
    - ALGORITHM --> the hashing algorithm
     */
    private static final SecureRandom RAND = new SecureRandom();
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * This is the class that will be used to deal with the cryptography and security in Peerpal.
     * All cryptographic algorithms will be placed in this helper class.
     */
    private CryptographyHelper() {
    }

    /**
     * Generates a secure salt to protect the cryptography algorithm from being cracked with brute force attacks.
     *
     * @param length The length of the secure salt (a good value is 512 bytes)
     * @return The salt as a {@link Base64} encoded {@link Optional<String>} that may contain the password.
     * It is empty if length is a non-negative integer
     */
    public static Optional<String> generateSecureSalt(final int length) {
        if (length < 1) {
            System.err.println("The length of a salt should be a non-negative number to prevent brute force attacks. " +
                    "Choose a positive length for the salt");
            return Optional.empty();
        }
        byte[] saltBytes = new byte[length];
        RAND.nextBytes(saltBytes);
        return Optional.of(Base64.getEncoder().encodeToString(saltBytes));
    }

    /**
     * Generates the {@link Base64} encoded password using the PBKDF2WithHmacSHA512 algorithm,
     * which is repeated <i>ITERATIONS</i> times using the given <i>salt</i>
     *
     * @param password The user password to be stored in plain text.
     * @param salt     The given salt for the algorithm. This makes the cryptography less easy to crack.
     * @return The {@link Base64} encoded password created using the PBKDF2WithHmacSHA512 ({@link PBEKeySpec}) algorithm
     * on the given plain text password.
     */
    public static Optional<String> encodeUserPassword(String password, String salt) {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = salt.getBytes();

        // going to encode the password in the PBE specification but first we need to intialize the hashing algorithm
        PBEKeySpec pbeSpec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);

        // remove all traces of the original password
        Arrays.fill(passwordChars, Character.MIN_VALUE);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] secretPwdBytes = keyFactory.generateSecret(pbeSpec).getEncoded();
            return Optional.of(Base64.getEncoder().encodeToString(secretPwdBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // uh oh an error happened during hashing
            e.printStackTrace();
            return Optional.empty();
        } finally {
            pbeSpec.clearPassword();
        }
    }

    /**
     * Verifies that the given password entered into the method matches the encoded password
     * of a user in the database of users in PeerPal.
     *
     * @param password   The given password to verify in plain text.
     * @param dbPassword The password in {@link Base64} encoding and encrypted in the
     *                   PBKDF2WithHmacSHA512 ({@link PBEKeySpec}) algorithm found in the PeerPal database of users.
     * @param salt       The salt to encode the password in. <b>MAKE SURE THIS IS CONSISTANT OR DIFFERENT RESULTS MAY ARISE!</b>
     * @return The method returns <b>true</b> if the encrypted and plain text passwords match and <b>false</b> otherwise.
     */
    public static boolean verifyPassword(String password, String dbPassword, String salt) {
        Optional<String> hashedPassword = encodeUserPassword(password, salt);
        return hashedPassword.map(s -> s.equals(dbPassword)).orElse(false);
    }
}
