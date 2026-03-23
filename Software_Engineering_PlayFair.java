import java.util.*;
import java.lang.Math;

//Vincent - I am making a 5x5 grid so that I can use coordinates
// to get and store values. 

public class PlayfairDecrypt {

    static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static String cutLetter = "";

    //@var key = The key for the cipher
    //We want to make sure that it has no duplicate letters
    public static List<Character> CleanKey(String key) {
        List<Character> cleanList = new ArrayList<>();
        for(char letter : key.toCharArray()){
            if(!cleanList.contains(letter)){
                cleanList.add(letter);
            }
        }
        return cleanList;
    }

    //@var key = The list of chars used at the beginning of the new alpha
    //@var cutLetter = Because the grid is 5x5 and there are 26 letters, we need to cut a letter from the alphabet.
    //Usually these are letters like x, y, z, sometimes j, etc.
    //We can populate multiple grids using common cut letters and en/decode the ciphers that way
    //The key goes in the front and then the rest of the alphabet follows after
    public static String RearrangeAlphabet(List<Character> key, String cutLetter){
        String useableAlphabet = ALPHABET.replace(cutLetter, "");
        StringBuilder stringKey = new StringBuilder();
        for(char c : key){
            stringKey.append(c);
        }
        String keyString = stringKey.toString().replace(cutLetter, "");

        StringBuilder remaining = new StringBuilder();
        for(char c : useableAlphabet.toCharArray()){
            if(!key.contains(c)){
                remaining.append(c);
            }
        }

        String newAlpha = keyString + remaining.toString();
        return newAlpha;
    }

    //@var cleanedAlpha = This is the 25 letters of the alphabet we're going to use to set up the 5x5 grid
    //Loops through all the open positions and uses a formula to place the letter in the correct spot
    public static char[][] PopulateGrid(String cleanedAlpha){
        char[][] grid = new char[5][5];

        for(int row = 0; row < 5; row++){
            for(int column = 0; column < 5; column++){
                grid[row][column] = cleanedAlpha.charAt(row * 5 + column);
            }
        }

        return grid;
    }

    //This function loops through the grid to give the coordinates of the letter you're looking for. 
    public static int[] CharLookUp(char character, char[][] grid){
        boolean found = false;
        for(int row = 0; row < grid.length; row++){
            for(int column = 0; column < grid[row].length; column++){
                if(character == grid[row][column]){
                    found = true;
                    return new int[]{row, column};
                }
            }
        }
        if(found == false){
            System.out.println("Char not found");
        }
        return null;
    }

    public static boolean SameRow(int[] p1, int[] p2){
        if(p1[0] == p2[0]){
            return true;
        }
        return false;
    }

    public static boolean SameColumn(int[] p1, int[] p2){
        if(p1[1] == p2[1]){
            return true;
        }
        return false;
    }

    //For Diagonals, I want the distance between columns, because I don't care if they're true diagonals. I only char if they are "diagonal"
    //So I'd need to check if the columns and rows are different, then get the distance between the two.
    public static boolean IsDiagonal(int[] p1, int[] p2){
        if(p1[0] != p2[0] && p1[1] != p2[1]){
            return true;
        }
        return false;
    }

    /*
    public static double CheckRelationship(int[] p1, int[] p2){
        double distance = 0;
        try{
            if(SameColumn(p1, p2)){
                distance = Math.abs(p1[0] - p2[0]);
                System.out.println("Same Column, Distance = " + distance);
            }
            else if(SameRow(p1, p2)){
                distance = Math.abs(p1[1] - p2[1]);
                System.out.println("Same Row, Distance = " + distance);
            }
            else{
                distance = Math.abs(p1[1] - p2[1]);
                System.out.println("Diagonal, Distance = " + distance);
            }
        }
        catch(Exception e){
            System.out.println("One or More Invalid Coordinates");
        }
        return distance;
    }
    */

    public static double CheckDistance(int[] p1, int[] p2){
        double distance = 0;
        try{
            if(SameColumn(p1, p2)){
                distance = Math.abs(p1[0] - p2[0]);
                System.out.println("Same Column, Distance = " + distance);
            }
            else if(SameRow(p1, p2)){
                distance = Math.abs(p1[1] - p2[1]);
                System.out.println("Same Row, Distance = " + distance);
            }
            else{
                distance = Math.abs(p1[1] - p2[1]);
                System.out.println("Diagonal, Distance = " + distance);
            }
        }
        catch(Exception e){
            System.out.println("One or More Invalid Coordinates");
        }
        return distance;
    }

    //Gets user input for cipher key
    public static String GetKeyInput(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print("Please enter the Cipher Key: ");
            String userKey = scanner.nextLine().toUpperCase();
            try{
                userKey = userKey.replace(" ", "");
                if(userKey.matches("[A-Z]+")){
                    return userKey;
                }
                else{
                    System.out.print("Invalid Key: ");
                    throw new Exception();
                }
            }
            catch(Exception e){
                System.out.println("Letters Only");
            }
        }
    }

    //Gets user input for omitted letter
    public static String GetOmittedLetter(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print("What letter would you like to omit? ");
            String omittedLetter = scanner.nextLine().toUpperCase();
            try{
                if(omittedLetter.matches("[A-Z]") && !omittedLetter.equals("X")){
                    return omittedLetter;
                }
                else{
                    System.out.println("Invalid Letter");
                    throw new Exception();
                }
            }
            catch(Exception e){
                System.out.println("Please Enter One Letter.");
            }
        }
    }

    //Gets the encrypted message
    //Removes spaces from message
    public static String GetEncryptedMessage(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print("What is the encrypted message? ");
            String message = scanner.nextLine();
            try{
                message = message.replace(" ", "");
                if(message.matches("[a-zA-Z]+")){
                    return message.toUpperCase();
                }
                else{
                    throw new Exception();
                }
            }
            catch(Exception e){
                System.out.println("Message must be all letters.");
            }
        }
    }

    //Turns the message into a list of strings that are no longer than 2
    public static List<String> DigramMessage(String message){
        String modifiedText = "";
        String filler = "X";
        int i = 0;

        while(i < message.length()){
            modifiedText += message.charAt(i);

            if(i + 1 < message.length()){
                if(message.charAt(i) == message.charAt(i+1)){
                    modifiedText += filler;
                    i += 1;
                }
                else{
                    modifiedText += message.charAt(i+1);
                    i += 2;
                }
            }
            else{
                modifiedText += filler;
                i += 1;
            }
        }

        if(modifiedText.length() % 2 != 0){
            modifiedText += filler;
        }

        List<String> digrams = new ArrayList<>();
        for(int j = 0; j < modifiedText.length(); j += 2){
            digrams.add(modifiedText.substring(j, j+2));
        }

        return digrams;
    }

    public static String DecryptMessage(List<String> digram, char[][] grid){
        int[] pos1;
        int[] pos2;
        List<String> decryptedMessage = new ArrayList<>();

        int distance = -1;

        for(String tup : digram){
            if(tup.length() < 2){
                continue;
            }

            pos1 = CharLookUp(tup.charAt(0), grid);
            pos2 = CharLookUp(tup.charAt(1), grid);

            String shift = "";
            String shift2 = "";

            if(SameColumn(pos1, pos2)){
                shift = String.valueOf(ShiftDown(grid, pos1, distance));
                shift2 = String.valueOf(ShiftDown(grid, pos2, distance));
            }
            else if(SameRow(pos1, pos2)){
                shift = String.valueOf(ShiftRight(grid, pos1, distance));
                shift2 = String.valueOf(ShiftRight(grid, pos2, distance));
            }
            else{
                shift = String.valueOf(grid[pos1[0]][pos2[1]]);
                shift2 = String.valueOf(grid[pos2[0]][pos1[1]]);
            }

            decryptedMessage.add(shift + shift2);
        }

        return String.join("", decryptedMessage);
    }

    public static char ShiftRight(char[][] grid, int[] pos, int distance){
        int col = pos[1];
        int newCol = (col + distance) % 5;
        if(newCol < 0) newCol += 5;
        return grid[pos[0]][newCol];
    }

    public static char ShiftDown(char[][] grid, int[] pos, int distance){
        int row = pos[0];
        int newRow = (row + distance) % 5;
        if(newRow < 0) newRow += 5;
        return grid[newRow][pos[1]];
    }

    public static void main(String[] args){

        //Get Key
        List<Character> key = CleanKey(GetKeyInput());

        //Get Omitted Letter
        String oLetter = GetOmittedLetter();

        //Get Encrypted Message
        String encryptedMessage = GetEncryptedMessage();

        //Turn Encrypted Message Into a Digram
        List<String> digram = DigramMessage(encryptedMessage);

        //Arrange 5x5 Grid using Key and Omitted Letter
        char[][] grid = PopulateGrid(RearrangeAlphabet(key, oLetter));
        for(char[] row : grid){
            System.out.println(Arrays.toString(row));
        }

        //Decrypt Message
        System.out.println(DecryptMessage(digram, grid));
    }
}
