package com.playfair.backend;

import java.util.*;

//Vincent - I am making a 5x5 grid so that I can use coordinates
// to get and store values.
public class PlayfairDecrypt {

    static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // CleanKey ensures no duplicate letters are in the list
    public static List<Character> CleanKey(String key) {
        List<Character> cleanList = new ArrayList<>();
        for (char letter : key.toCharArray()) {
            if (!cleanList.contains(letter)) {
                cleanList.add(letter);
            }
        }
        return cleanList;
    }

    // Fixed logic to ensure the cutLetter is never added to the grid
    public static String RearrangeAlphabet(List<Character> key, String cutLetter) {
        char charToSkip = cutLetter.charAt(0);
        StringBuilder sbKey = new StringBuilder();

        // 1. Add key letters ONLY if they aren't the omitted letter
        for (char c : key) {
            if (c != charToSkip && sbKey.indexOf(String.valueOf(c)) == -1) {
                sbKey.append(c);
            }
        }

        // 2. Add remaining alphabet letters (excluding omitted)
        String usableAlphabet = ALPHABET.replace(cutLetter, "");
        for (char c : usableAlphabet.toCharArray()) {
            if (sbKey.indexOf(String.valueOf(c)) == -1) {
                sbKey.append(c);
            }
        }
        return sbKey.toString();
    }

    public static char[][] PopulateGrid(String cleanedAlpha) {
        char[][] grid = new char[5][5];
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                grid[row][column] = cleanedAlpha.charAt(row * 5 + column);
            }
        }
        return grid;
    }

    public static int[] CharLookUp(char character, char[][] grid) {
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                if (character == grid[row][column]) {
                    return new int[]{row, column};
                }
            }
        }
        return null;
    }

    public static boolean SameRow(int[] p1, int[] p2) {
        return p1[0] == p2[0];
    }

    public static boolean SameColumn(int[] p1, int[] p2) {
        return p1[1] == p2[1];
    }

    // Input Handling - Fixed Scanner to not close prematurely in loops
    public static String GetKeyInput(Scanner scanner) {
        while (true) {
            System.out.print("Please enter the Cipher Key: ");
            String userKey = scanner.nextLine().toUpperCase().replace(" ", "");
            if (userKey.matches("[A-Z]+")) return userKey;
            System.out.println("Invalid Key: Letters Only");
        }
    }

    public static String GetOmittedLetter(Scanner scanner) {
        while (true) {
            System.out.print("What letter would you like to omit? ");
            String input = scanner.nextLine().toUpperCase().trim();
            if (input.length() == 1 && input.matches("[A-Z]") && !input.equals("X")) {
                return input;
            }
            System.out.println("Invalid Letter: Enter one letter (not X).");
        }
    }

    public static String GetEncryptedMessage(Scanner scanner) {
        while (true) {
            System.out.print("What is the encrypted message? ");
            String message = scanner.nextLine().toUpperCase().replace(" ", "");
            if (message.matches("[A-Z]+")) return message;
            System.out.println("Message must be all letters.");
        }
    }

    // Ensures any instance of the omitted letter in the message is replaced
    public static String ReplaceLettersInMessage(String message, String omittedLetter) {
        // Standard Playfair: If J is missing, use I. Otherwise, use X.
        String replacement = omittedLetter.equals("J") ? "I" : "X";
        if (omittedLetter.equals(replacement)) replacement = "A";

        return message.replace(omittedLetter, replacement);
    }

    public static List<String> DigramMessage(String message) {
        StringBuilder modified = new StringBuilder();
        char filler = 'X';

        int i = 0;
        while (i < message.length()) {
            char first = message.charAt(i);
            modified.append(first);

            if (i + 1 < message.length()) {
                char second = message.charAt(i + 1);
                if (first == second) {
                    // If letters are the same, add filler and process 'second' in the next pair
                    modified.append(filler);
                    i++;
                } else {
                    // If letters are different, add the second and move past both
                    modified.append(second);
                    i += 2;
                }
            } else {
                // Only one letter left, add filler to complete the pair
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

    // Change the name to processMessage and add the 'distance' parameter
    public static String ProcessMessage(List<String> digrams, char[][] grid, int distance) {
        StringBuilder result = new StringBuilder();

        for (String pair : digrams) {
            int[] pos1 = CharLookUp(pair.charAt(0), grid);
            int[] pos2 = CharLookUp(pair.charAt(1), grid);

            if (pos1[0] == pos2[0]) { // Same Row
                result.append(ShiftRight(grid, pos1, distance));
                result.append(ShiftRight(grid, pos2, distance));
            } else if (pos1[1] == pos2[1]) { // Same Column
                result.append(ShiftDown(grid, pos1, distance));
                result.append(ShiftDown(grid, pos2, distance));
            } else { // Rectangle
                result.append(grid[pos1[0]][pos2[1]]);
                result.append(grid[pos2[0]][pos1[1]]);
            }
        }
        return result.toString();
    }

    public static char ShiftRight(char[][] grid, int[] pos, int distance) {
        int newCol = (pos[1] + distance + 5) % 5;
        return grid[pos[0]][newCol];
    }

    public static char ShiftDown(char[][] grid, int[] pos, int distance) {
        int newRow = (pos[0] + distance + 5) % 5;
        return grid[newRow][pos[1]];
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String rawKey = GetKeyInput(scanner);
        List<Character> cleanKey = CleanKey(rawKey);
        String oLetter = GetOmittedLetter(scanner);
        String encryptedMessage = GetEncryptedMessage(scanner);

        // Pre-process message to remove omitted letters
        String fixedMessage = ReplaceLettersInMessage(encryptedMessage, oLetter);
        List<String> digrams = DigramMessage(fixedMessage);

        String newAlpha = RearrangeAlphabet(cleanKey, oLetter);
        char[][] grid = PopulateGrid(newAlpha);

        System.out.println("\nCipher Grid:");
        for (char[] row : grid) {
            System.out.println(Arrays.toString(row));
        }

        System.out.print("\nDecrypted Message: ");
        //System.out.println(DecryptMessage(digrams, grid));

        scanner.close();
    }
}