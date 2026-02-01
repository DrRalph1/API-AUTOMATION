// helpers/GenerateRandomPassword.js

/**
 * Generates a secure random password.
 * Includes uppercase, lowercase, numbers, and special characters by default.
 * Ensures at least one of each character type is included.
 *
 * @param {number} length - Desired password length (minimum recommended: 12)
 * @returns {string} Secure random password
 */
export const generateRandomPassword = (length = 12) => {
    if (length < 8) {
        console.warn("⚠️ Password length is too short. Using minimum 8 characters.");
        length = 8;
    }

    const upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const lowerCase = "abcdefghijklmnopqrstuvwxyz";
    const numbers = "0123456789";
    const special = "!@#$%^&*()_+[]{}|;:,.<>?";

    const allChars = upperCase + lowerCase + numbers + special;

    // Ensure at least one of each type
    let password = "";
    password += upperCase.charAt(Math.floor(Math.random() * upperCase.length));
    password += lowerCase.charAt(Math.floor(Math.random() * lowerCase.length));
    password += numbers.charAt(Math.floor(Math.random() * numbers.length));
    password += special.charAt(Math.floor(Math.random() * special.length));

    // Fill the rest with random characters
    for (let i = password.length; i < length; i++) {
        password += allChars.charAt(Math.floor(Math.random() * allChars.length));
    }

    // Shuffle password to avoid predictable order
    password = password
        .split("")
        .sort(() => Math.random() - 0.5)
        .join("");

    return password;
};
