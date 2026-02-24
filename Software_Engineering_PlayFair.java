import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

//Vincent - I am making a 5x5 grid so that I can use coordinates
// to get and store values. 

public class CipherGrid {

    private static final String ALPHABET = "ABCDEFGHIJKLMONPQRSTUVWXYZ";
    private static final int GRID_SIZE = 5;
    private static String[][] grid = new String[GRID_SIZE][GRID_SIZE];
    private static String cutLetter = "";

    // @var key = The key for the cipher
    // We want to make sure that it has no duplicate letters
    public static List<Character> CleanKey(String key) {
        List<Character> cleanList = new ArrayList<>();
        for (char letter : key.toCharArray()) {
            if (!cleanList.contains(letter)) {
                cleanList.add(letter);
            }
        }
        return cleanList;
    }

    // @var key = The list of chars used at the beginning of the new alpha
    // @var cutLetter = Because the grid is 5x5 and there are 26 letters, we need to cut a letter from the alphabet.
    // Usually these are letters like x, y, z, sometimes j, etc.
    // We can populate multiple grids using common cut letters and en/decode the ciphers that way
    // The key goes in the front and then the rest of the alphabet follows after
    public static String RearrangeAlphabet(List<Character> key, String cutLetter) {
        String useableAlphabet = ALPHABET.replace(cutLetter, "");
        StringBuilder stringKey = new StringBuilder();
        for (char c : key) {
            if (c != cutLetter.charAt(0)) {
                stringKey.append(c);
            }
        }
        StringBuilder remaining = new StringBuilder();
        for (char c : useableAlphabet.toCharArray()) {
            if (!key.contains(c)) {
                remaining.append(c);
            }
        }
        String newAlpha = stringKey.toString() + remaining.toString();
        return newAlpha;
    }

    // @var cleanedAlpha = This is the 25 letters of the alphabet we're going to use to set up the 5x5 grid
    // Loops through all the open positions and uses a formula to place the letter in the correct spot
    public static void PopulateGrid(String cleanedAlpha) {
        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                grid[row][column] = String.valueOf(cleanedAlpha.charAt(index));
                index++;
            }
        }
    }

    // This function loops through the grid to give the coordinates of the letter you're looking for. 
    public static int[] CharLookUp(String character) {
        boolean found = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                if (grid[row][column].equals(character)) {
                    found = true;
                    return new int[]{row, column};
                }
            }
        }
        if (!found) {
            System.out.println("Char not found");
        }
        return null;
    }

    public static boolean SameRow(int[] p1, int[] p2) {
        if (p1[0] == p2[0]) {
            return true;
        }
        return false;
    }

    public static boolean SameColumn(int[] p1, int[] p2) {
        if (p1[1] == p2[1]) {
            return true;
        }
        return false;
    }

    // For Diagonals, I want the distance between columns, because I don't care if they're true diagonals. 
    // So I'd need to check if the columns and rows are different, then get the distance between the two.
    public static boolean IsDiagonal(int[] p1, int[] p2) {
        if (p1[0] != p2[0] && p1[1] != p2[1]) {
            return true;
        }
        return false;
    }

    public static void CheckRelationship(int[] p1, int[] p2) {
        try {
            if (SameColumn(p1, p2)) {
                double r_distance = Math.abs(p1[0] - p2[0]);
                System.out.println("Same Column, Distance = " + r_distance);
            } else if (SameRow(p1, p2)) {
                double c_distance = Math.abs(p1[1] - p2[1]);
                System.out.println("Same Row, Distance = " + c_distance);
            } else {
                double c_distance = Math.abs(p1[1] - p2[1]);
                System.out.println("Diagonal, Distance = " + c_distance);
            }
        } catch (Exception e) {
            System.out.println("One or More Invalid Coordinates");
        }
    }

    public static void main(String[] args) {
        List<Character> key = CleanKey("APPLE");
        PopulateGrid(RearrangeAlphabet(key, "F"));
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                System.out.print(grid[row][col] + " ");
            }
            System.out.println();
        }

        int[] pos1 = CharLookUp("E");
        int[] pos2 = CharLookUp("B");

        CheckRelationship(pos1, pos2);
    }
}
