import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPlayfairCipher {

    String keyPython;
    String omittedJ;
    String newAlpha;
    String[][] grid;

    public void setUp() {
        // Common setup for multiple tests
        keyPython = CleanKey("PYTHON");
        omittedJ = "J";
        newAlpha = RearrangeAlphabet(keyPython, omittedJ);
        grid = PopulateGrid(newAlpha);
    }

    @Test
    public void testRearrangeAlphabet() {
        //aThis tests if J is removed and the key is at the front
        String expectedStart = "PYTHONABCDE";
        assertTrue(newAlpha.startsWith("PYTHON"));
        assertFalse(newAlpha.contains("J"));
        assertEquals(25, newAlpha.length());
    }

    @Test
    public void testRectangleRuleDecryption() {
        """
        Test Case 1: Rectangle/Diagonal Rule
        In the grid:
        H is (0, 3), I is (2, 3) -> Same Column
        In the rectangle:
        P (0,0) and A (1,1) should decrypt to Y (0,1) and N (1,0)
        DecryptMessage uses distance -1
        """
        //Manually passing in a digram for 'YN' to see if the result is 'PA'
        String[] digram = {"YN"};
        String result = DecryptMessage(digram, grid);
        assertEquals("PA", result);
    }

    @Test
    public void testRowWrapDecryption() {
        """
        Test Case 2: Row Wrapping
        In the grid, the first row should be [P, Y, T, H, O]
        Decrypting 'PY' should shift left to 'OP'
        """
        String[] digram = {"PY"};
        String result = DecryptMessage(digram, grid);
        // P -> O (wraps), Y -> P
        assertEquals("OP", result);
    }

    @Test
    public void testDigramGeneration() {
        //Tests to see if duplicate letters get an 'X' filler
        String message = "HELLO";
        // H E L L O -> HE, LX, LO
        String[] expected = {"HE", "LX", "LO"};
        assertArrayEquals(expected, DigramMessage(message));
    }
}
