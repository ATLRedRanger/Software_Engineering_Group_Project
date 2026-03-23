import unittest
from Software_Engineering_PlayFair import (
    CleanKey, 
    RearrangeAlphabet, 
    PopulateGrid, 
    DigramMessage, 
    DecryptMessage
)

class TestPlayfairCipher(unittest.TestCase):

    def setUp(self):
        # Common setup for multiple tests
        self.key_python = CleanKey("PYTHON")
        self.omitted_j = "J"
        self.new_alpha = RearrangeAlphabet(self.key_python, self.omitted_j)
        self.grid = PopulateGrid(self.new_alpha)

    def test_rearrange_alphabet(self):
        #aThis tests if J is removed and the key is at the front
        expected_start = "PYTHONABCDE"
        self.assertTrue(self.new_alpha.startswith("PYTHON"))
        self.assertNotIn("J", self.new_alpha)
        self.assertEqual(len(self.new_alpha), 25)

    def test_rectangle_rule_decryption(self):
        """
        Test Case 1: Rectangle/Diagonal Rule
        In the grid:
        H is (0, 3), I is (2, 3) -> Same Column
        In the rectangle:
        P (0,0) and A (1,1) should decrypt to Y (0,1) and N (1,0)
        DecryptMessage uses distance -1
        """
        #Manually passing in a digram for 'YN' to see if the result is 'PA'
        digram = ("YN",)
        result = DecryptMessage(digram, self.grid)
        self.assertEqual(result, "PA")

    def test_row_wrap_decryption(self):
        """
        Test Case 2: Row Wrapping
        In the grid, the first row should be [P, Y, T, H, O]
        Decrypting 'PY' should shift left to 'OP'
        """
        digram = ("PY",)
        result = DecryptMessage(digram, self.grid)
        # P -> O (wraps), Y -> P
        self.assertEqual(result, "OP")

    def test_digram_generation(self):
        #Tests to see if duplicate letters get an 'X' filler
        message = "HELLO"
        # H E L L O -> HE, LX, LO
        expected = ("HE", "LX", "LO")
        self.assertEqual(DigramMessage(message), expected)

if __name__ == "__main__":
    unittest.main()