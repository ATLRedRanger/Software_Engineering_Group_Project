package com.example.playfaircipher;

import java.util.*;

public class PlayfairDecryptor {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Get Key
        String rawKey = getKeyInput(scanner);
        List<Character> cleanKey = cleanKey(rawKey);

        // Get Omitted Letter
        String oLetter = getOmittedLetter(scanner);

        // Get Encrypted Message
        String encryptedMessage = getEncryptedMessage(scanner);

        // Replace Letters If Necessary
        String fixedMessage = replaceLettersInMessage(encryptedMessage, oLetter);

        // Turn Encrypted Message Into Digrams
        List<String> digrams = digramMessage(fixedMessage);

        // Arrange 5x5 Grid
        String newAlpha = rearrangeAlphabet(cleanKey, oLetter);
        char[][] grid = populateGrid(newAlpha);

        // Print Grid
        System.out.println("\nCipher Grid:");
        for (char[] row : grid) {
            System.out.println(Arrays.toString(row));
        }

        // Decrypt Message
        System.out.print("\nDecrypted Message: ");
        System.out.println(decryptMessage(digrams, grid));

        scanner.close();
    }

    public static List<Character> cleanKey(String key) {
        List<Character> cleanList = new ArrayList<>();
        for (char letter : key.toCharArray()) {
            if (!cleanList.contains(letter)) {
                cleanList.add(letter);
            }
        }
        return cleanList;
    }

    public static String rearrangeAlphabet(List<Character> key, String cutLetter) {
        String usableAlphabet = ALPHABET.replace(cutLetter, "");
        StringBuilder sbKey = new StringBuilder();

        // Add key letters (excluding the cutLetter if it was in the key)
        for (char c : key) {
            if (c != cutLetter.charAt(0)) {
                sbKey.append(c);
            }
        }

        // Add remaining alphabet
        for (char c : usableAlphabet.toCharArray()) {
            if (sbKey.indexOf(String.valueOf(c)) == -1) {
                sbKey.append(c);
            }
        }
        return sbKey.toString();
    }

    public static char[][] populateGrid(String cleanedAlpha) {
        char[][] grid = new char[5][5];
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                grid[row][col] = cleanedAlpha.charAt(row * 5 + col);
            }
        }
        return grid;
    }

    public static int[] charLookUp(char c, char[][] grid) {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                if (grid[row][col] == c) {
                    return new int[]{row, col};
                }
            }
        }
        System.out.println("Char not found: " + c);
        return null;
    }

    public static String decryptMessage(List<String> digrams, char[][] grid) {
        StringBuilder decrypted = new StringBuilder();
        int shiftValue = -1; // Negative for decryption

        for (String pair : digrams) {
            int[] pos1 = charLookUp(pair.charAt(0), grid);
            int[] pos2 = charLookUp(pair.charAt(1), grid);

            if (pos1[0] == pos2[0]) { // Same Row
                decrypted.append(shiftRight(grid, pos1, shiftValue));
                decrypted.append(shiftRight(grid, pos2, shiftValue));
            } else if (pos1[1] == pos2[1]) { // Same Column
                decrypted.append(shiftDown(grid, pos1, shiftValue));
                decrypted.append(shiftDown(grid, pos2, shiftValue));
            } else { // Diagonal/Rectangle
                decrypted.append(grid[pos1[0]][pos2[1]]);
                decrypted.append(grid[pos2[0]][pos1[1]]);
            }
        }
        return decrypted.toString();
    }

    private static char shiftRight(char[][] grid, int[] pos, int distance) {
        // Adding 5 before modulo ensures positive result for negative shifts
        int newCol = (pos[1] + distance + 5) % 5;
        return grid[pos[0]][newCol];
    }

    private static char shiftDown(char[][] grid, int[] pos, int distance) {
        int newRow = (pos[0] + distance + 5) % 5;
        return grid[newRow][pos[1]];
    }

    public static List<String> digramMessage(String message) {
        StringBuilder modified = new StringBuilder();
        char filler = 'X';
        int i = 0;

        while (i < message.length()) {
            modified.append(message.charAt(i));
            if (i + 1 < message.length()) {
                if (message.charAt(i) == message.charAt(i + 1)) {
                    modified.append(filler);
                    i++;
                } else {
                    modified.append(message.charAt(i + 1));
                    i += 2;
                }
            } else {
                modified.append(filler);
                i++;
            }
        }

        List<String> result = new ArrayList<>();
        for (int j = 0; j < modified.length(); j += 2) {
            result.add(modified.substring(j, j + 2));
        }
        return result;
    }

    // --- Input Handling ---

    public static String getKeyInput(Scanner sc) {
        while (true) {
            System.out.print("Please enter the Cipher Key: ");
            String input = sc.nextLine().toUpperCase().replace(" ", "");
            if (input.matches("[A-Z]+")) return input;
            System.out.println("Invalid Key: Letters Only");
        }
    }

    public static String getOmittedLetter(Scanner sc) {
        while (true) {
            System.out.print("What letter would you like to omit? ");
            String input = sc.nextLine().toUpperCase().trim();
            if (input.length() == 1 && input.matches("[A-Z]") && !input.equals("X")) {
                return input;
            }
            System.out.println("Invalid Letter: Enter one letter (not X).");
        }
    }

    public static String getEncryptedMessage(Scanner sc) {
        while (true) {
            System.out.print("What is the encrypted message? ");
            String input = sc.nextLine().toUpperCase().replace(" ", "");
            if (input.matches("[A-Z]+")) return input;
            System.out.println("Message must be all letters.");
        }
    }

    public static String replaceLettersInMessage(String message, String omittedLetter) {
        String replacement = omittedLetter.equals("J") ? "I" : "X";
        if (omittedLetter.equals(replacement)) replacement = "A";
        return message.replace(omittedLetter, replacement);
    }
}