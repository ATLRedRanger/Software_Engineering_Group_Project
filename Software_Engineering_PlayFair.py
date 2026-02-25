import math
#Vincent - I am making a 5x5 grid so that I can use coordinates
# to get and store values. 
    
ALPHABET = "ABCDEFGHIJKLMONPQRSTUVWXYZ"
grid = []
cutLetter = ""

#@var key = The key for the cipher
#We want to make sure that it has no duplicate letters
def CleanKey(key: str):
    cleanList = []
    for letter in key:
        if letter not in cleanList:
            cleanList.append(letter)
    return cleanList


#@var key = The list of chars used at the beginning of the new alpha
#@var cutLetter = Because the grid is 5x5 and there are 26 letters, we need to cut a letter from the alphabet.
#Usually these are letters like x, y, z, sometimes j, etc.
#We can populate multiple grids using common cut letters and en/decode the ciphers that way
#The key goes in the front and then the rest of the alphabet follows after
def RearrangeAlphabet(key: list, cutLetter: str):
    useableAlphabet = ALPHABET.replace(cutLetter, "") 
    stringKey = "".join(key)
    stringKey = stringKey.replace(cutLetter, "")
    remaining = [char for char in useableAlphabet if char not in key]
    remaining = "".join(remaining)
    newAlpha = stringKey + remaining
    return newAlpha

#@var cleanedAlpha = This is the 25 letters of the alphabet we're going to use to set up the 5x5 grid
#Loops through all the open positions and uses a formula to place the letter in the correct spot
def PopulateGrid(cleanedAlpha: str):
    for row in range(0,5):
        rowList = []
        for column in range(0, 5):
            rowList.append(cleanedAlpha[row * 5 + column])
        grid.append(rowList)


#This function loops through the grid to give the coordinates of the letter you're looking for. 
def CharLookUp(char: str):
    found = False
    for row in range(len(grid)):
        for column in range(len(grid[row])):
            if(char == grid[row][column]):
                found = True
                return row, column
    if(found == False):
        print("Char not found")
    
def SameRow(p1, p2):
    if(p1[0] == p2[0]):
        return True
    return False

def SameColumn(p1, p2):
    if(p1[1] == p2[1]):
        return True
    return False

#For Diagonals, I want the distance between columns, because I don't care if they're true diagonals. I only char if they are "diagonal"
#So I'd need to check if the columns and rows are different, then get the distance between the two.
def IsDiagonal(p1, p2):
    if(p1[0] != p2[0] and p1[1] != p2[1]):
        return True
    return False

def CheckRelationship(p1, p2):
    try:
        if(SameColumn(p1, p2)):
            r_distance = math.fabs(p1[0] - p2[0])
            print(f"Same Column, Distance = {r_distance}")
        elif(SameRow(p1, p2)):
            c_distance = math.fabs(p1[1] - p2[1])
            print(f"Same Row, Distance = {c_distance}")
        else:
            c_distance = math.fabs(p1[1] - p2[1])
            print(f"Diagonal, Distance = {c_distance}")
    except:
        print("One or More Invalid Coordinates")

#Gets user input for cipher key
def GetKeyInput():
    
    while True:
        userKey = input("Please enter the Cipher Key: ").upper()
        try:
            userKey = userKey.replace(" ", "")
            if(userKey.isalpha()):
                break
            else:
                print("Invalid Key: ", end = "")
                raise TypeError
        except TypeError:
            print("Letters Only")

    return userKey

#Gets user input for omitted letter
def GetOmittedLetter():
    while True:
        omittedLetter = input("What letter would you like to omit? ").upper()
        try:
            if(omittedLetter.isalpha() and len(omittedLetter) == 1):
                return omittedLetter
            else:
                print("Invalid Letter")
                raise IndexError
        except IndexError:
            print("Please Enter One Letter.")

#Gets the encrypted message
#Removes spaces from message
def GetEncryptedMessage():
    while True:
        message = input("What is the encrypted message? ")
        try:
            #print(f"Before: {message}")
            message = message.replace(" ", "")
            #print(f"After: {message}")
            if(message.isalpha()):
                return message.upper()
            else:
                raise TypeError
        except TypeError:
            print("Message must be all letters.")

#Turns the message into a list of strings that are no longer than 2
def DigramMessage(message: str):
    modifiedText = ""
    filler = "X"

    for i in range(len(message) - 1):
        modifiedText += message[i]
        if message[i] == message[i+1]:
            modifiedText += filler

    modifiedText += message[-1]

    return " ".join([modifiedText[j:j+2] for j in range(0, len(message), 2)])
    



def main():
    #Get Key
    key = CleanKey(GetKeyInput())

    #Get Omitted Letter
    oLetter = GetOmittedLetter()

    #Get Encrypted Message
    encryptedMessage = GetEncryptedMessage()
    print(DigramMessage(encryptedMessage))

    #Arrange 5x5 Grid using Key and Omitted Letter
    PopulateGrid(RearrangeAlphabet(key, oLetter))
    for row in grid:
        print(row)

    #Decrypt Message

    pos1 = CharLookUp("E")
    pos2 = CharLookUp("B")

    CheckRelationship(pos1, pos2)

if __name__ == "__main__":
    main()