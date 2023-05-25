import java.util.Scanner;

class Operate {
	private Scanner console;
	private int[][] SubNibbles, InvSubNibbles, ConstantMatrix, InvConstantMatrix;
	private int[][][] roundKeys;
	private final int IrreduciblePolynomial;
	private int[] RoundConstant, PlainText, CipherText;

	public Operate(Scanner sc) {
		this.console = sc;
		SubNibbles = new int[][] {{0x9, 0x4, 0xA, 0xB}, {0xD, 0x1, 0x8, 0x5}, {0x6, 0x2, 0x0, 0x3}, {0xC, 0xE, 0xF, 0x7}};
		InvSubNibbles = getInverseTransformation(SubNibbles);
		/*(System.out.println("SubNibble Transformation :");
		displayHexadecimalMatrix(SubNibbles);
		System.out.println("InvSubNibble Transformation :");
		displayHexadecimalMatrix(InvSubNibbles);*/
		IrreduciblePolynomial = 0b10011;
		ConstantMatrix = new int[][] {{1, 4}, {4, 1}};
		InvConstantMatrix = new int[][] {{9, 2}, {2, 9}};
		RoundConstant = new int[]{0x80, 0x30};
		getCipherKey();
		getPlainText();

		//PlainText = new int[]{0xD7, 0x28};
		encrypt();
		decrypt();
		//System.out.println(finiteFieldMultiplication(0x0, 0x0));
	}

	private void encrypt() {
		int i, j, currentBlock, k;
		int currentState[][] = new int[2][2];
		int n = PlainText.length;
		CipherText = new int[n];
		for(i = 0 ; i < n ; i += 2) {
			currentBlock = PlainText[i];
			currentBlock <<= 8;
			currentBlock |= PlainText[i + 1];

			currentState[0][0] = (currentBlock & 0xF000) >> 12;
			currentState[1][0] = (currentBlock & 0x0F00) >> 8;
			currentState[0][1] = (currentBlock & 0x00F0) >> 4;
			currentState[1][1] = currentBlock & 0x000F;
			//System.out.println("Before pre-round :");
			//displayHexadecimalMatrix(currentState);

			addRoundKey(currentState, roundKeys[0]);
			//System.out.println("After pre-round :");
			//displayHexadecimalMatrix(currentState);

			for(j = 0 ; j < 2 ; j++) {
				substituteState(currentState, SubNibbles);
				//System.out.println("After substitution :");
				//displayHexadecimalMatrix(currentState);
				for(k = 1 ; k < 2 ; k++) {
					rotate(currentState[k], k, true);
				}
				//System.out.println("After rotation :");
				//displayHexadecimalMatrix(currentState);
				if(j < 1) {
					mixColumn(currentState, true);
				}
				//System.out.println("After Mix Column :");
				//displayHexadecimalMatrix(currentState);
				addRoundKey(currentState, roundKeys[j + 1]);
				//System.out.println("After AddRoundKey :");
				//displayHexadecimalMatrix(currentState);
			}
			CipherText[i] = (currentState[0][0] << 4);
			CipherText[i] |= currentState[1][0];
			CipherText[i + 1] = (currentState[0][1] << 4);
			CipherText[i + 1] |= currentState[1][1];
		}

		System.out.println("Cipher-text :");
		for(i = 0 ; i < n ; i++) {
			System.out.print((char)CipherText[i]);
		}
		System.out.println();
	}

	private void decrypt() {
		System.out.println("Decrypting :");
		int n = CipherText.length;
		int i, j, k, currentBlock = 0;
		int currentState[][] = new int[2][2];
		int decryptedText[] = new int[n];
		for(i = 0 ; i < n ; i += 2) {
			currentBlock = (CipherText[i] << 8);
			currentBlock |= CipherText[i + 1];

			currentState[0][0] = (currentBlock & 0xF000) >> 12;
			currentState[1][0] = (currentBlock & 0x0F00) >> 8;
			currentState[0][1] = (currentBlock & 0x00F0) >> 4;
			currentState[1][1] = currentBlock & 0x000F;
			/*System.out.println("Before pre-round :");
			displayHexadecimalMatrix(currentState);*/

			addRoundKey(currentState, roundKeys[2]);
			/*System.out.println("After pre-round :");
			displayHexadecimalMatrix(currentState);*/

			for(j = 2 ; j > 0 ; j--) {
				for(k = 1 ; k < 2 ; k++) {
					rotate(currentState[k], k, false);
				}
				substituteState(currentState, InvSubNibbles);
				addRoundKey(currentState, roundKeys[j - 1]);
				if(j > 1) {
					mixColumn(currentState, false);
				}
			}
			decryptedText[i] = (currentState[0][0] << 4);
			decryptedText[i] |= currentState[1][0];
			decryptedText[i + 1] = (currentState[0][1] << 4);
			decryptedText[i + 1] |= currentState[1][1];
		}

		System.out.println("Deciphered-text :");
		for(i = 0 ; i < n ; i++) {
			System.out.print((char)decryptedText[i]);
		}
		System.out.println();
	}

	private void mixColumn(int state[][], boolean encrypt) {
		int rows = state.length;
		int j, cols = state[0].length;
		int currentColumn[] = new int[rows];
		for(int i = 0 ; i < cols ; i++) {
			for(j = 0 ; j < rows ; j++) {
				currentColumn[j] = state[j][i];
			}
			currentColumn = matrixVectorMultiplication(currentColumn, encrypt);
			for(j = 0 ; j < rows ; j++) {
				state[j][i] = currentColumn[j];
			}
		}
	}

	private int[] matrixVectorMultiplication(int vector[], boolean encrypt) {
		int i, j, val, n = vector.length;
		//System.out.println("State matrix column :");
		//displayArr(vector);
		int result[] = new int[n];
		if(encrypt) {
			//System.out.println("Constant matrix :");
			//displayHexadecimalMatrix(ConstantMatrix);
			for(i = 0 ; i < n ; i++) {
				val = 0;
				for(j = 0 ; j < n ; j++) {
					val ^= finiteFieldMultiplication(ConstantMatrix[i][j], vector[j]);
				}
				result[i] = val;
			}
			//System.out.println("After multiplication :");
			//displayArr(vector);
		}
		else {
			//System.out.println("Constant Matrix:");
			//displayHexadecimalMatrix(InvConstantMatrix);
			for(i = 0 ; i < n ; i++) {
				val = 0;
				for(j = 0 ; j < n ; j++) {
					val ^= finiteFieldMultiplication(InvConstantMatrix[i][j], vector[j]);
				}
				result[i] = val;
			}
		}
		return result;
	}

	private void displayArr(int arr[]) {
		int n = arr.length;
		for(int i = 0 ; i < n - 1 ; i++) {
			System.out.print(arr[i] + " ");
		}
		System.out.println(arr[n - 1]);
	}

	private int finiteFieldMultiplication(int a, int b) {
		int dividend = multiply(a, b);
		int divisor = IrreduciblePolynomial;
		int numDividend = numberOfBits(dividend);
		int numPolynomial = numberOfBits(divisor);
		int difference = numDividend - numPolynomial;
		int factorOfDivisor;
		while(difference >= 0) {
			factorOfDivisor = divisor << difference;
			dividend ^= factorOfDivisor;
			numDividend = numberOfBits(dividend);
			difference = numDividend - numPolynomial;
		}
		return dividend;
	}

	private int numberOfBits(int number) {
		int count = 0;
		while(number != 0) {
			count++;
			number >>= 1;
		}
		return count;
	}

	private int multiply(int a, int b) {
		int product = 0;
		while(b != 0) {
			if((b & 0x1) != 0) {
				product ^= a;
			}
			a <<= 1;
			b >>= 1;
		}
		return product;
	}

	private void substituteState(int matrix[][], int rules[][]) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		int value;
		for(int i = 0 ; i < rows ; i++) {
			for(int j = 0 ; j < cols ; j++) {
				value = matrix[i][j];
				matrix[i][j] = rules[((value & 0xC) >> 2)][value & 0x3];
			}
		}
	}

	private void addRoundKey(int state[][], int roundKey[][]) {
		for(int i = 0 ; i < 2 ; i++) {
			for(int j = 0 ; j < 2 ; j++) {
				state[i][j] ^= roundKey[i][j];
			}
		}
	}

	private void getPlainText() {
		int i, n;
		System.out.println("Enter plain text :");
		String text = console.nextLine();
		n = text.length();
		if(n % 2 == 0) {
			PlainText = new int[n];
		}
		else {
			PlainText = new int[n + 1];
		}

		for(i = 0 ; i < n ; i += 2) {
			PlainText[i] = (int)text.charAt(i);
			if(i + 1 < n) {
				PlainText[i + 1] = (int)text.charAt(i + 1);
			}
			else {
				PlainText[i + 1] = 0;
			}
		}
	}

	private void getCipherKey() {
		String keyString;
		do {
			System.out.println("Enter 16-bit key:");
			keyString = console.nextLine();
		}while(keyString.length() != 16);

		int cipherKey[] = new int[16];
		for(int i = 0 ; i < 16 ; i++) {
			cipherKey[i] = keyString.charAt(i) - '0';
		}

		int[][] words = generateKeyWords(cipherKey);
		generateRoundKeysMatrix(words);
		System.out.println("Round Keys :");
		for(int i = 0 ; i < 3 ; i++) {
			System.out.println("K" + i);
			displayHexadecimalMatrix(roundKeys[i]);
		}
	}

	private void generateRoundKeysMatrix(int words[][]) {
		roundKeys = new int[3][2][2];
		int z = 0;
		for(int i = 0 ; i < 3 ; i++) {
			for(int j = 0 ; j < 2 ; j++) {
				for(int k = 0 ; k < 2 ; k++) {
					roundKeys[i][k][j] = words[z / 2][z % 2];
					z++;
				}
			}
		}
	}

	private int[][] generateKeyWords(int cipherKey[]) {
		int i, j, combinedValue, words[][] = new int[6][2];
		words[0][0] = valueFromBits(cipherKey, 0, 4);
		words[0][1] = valueFromBits(cipherKey, 4, 8);
		words[1][0] = valueFromBits(cipherKey, 8, 12);
		words[1][1] = valueFromBits(cipherKey, 12, 16);
		
		for(i = 2 ; i < 6 ; i++) {
			if(i % 2 == 0) {
				for(j = 0 ; j < 2 ; j++) {
					words[i][j] = words[i - 1][j];
				}
				rotate(words[i], 1, true);
				substituteWord(words[i], SubNibbles);
				int rc = RoundConstant[(i / 2) - 1];
				words[i][0] = words[i][0] ^ (rc >> 4);
				words[i][1] = words[i][1] ^ (rc & 0x0F);

				words[i][0] = words[i][0] ^ words[i - 2][0];
				words[i][1] = words[i][1] ^ words[i - 2][1];
			}
			else {
				words[i][0] = words[i - 1][0] ^ words[i - 2][0];
				words[i][1] = words[i - 1][1] ^ words[i - 2][1];
			}
		}
		return words;
	}

	

	private void substituteWord(int arr[], int rules[][]) {
		for(int i = 0 ; i < arr.length ; i++) {
			arr[i] = rules[((arr[i] & 0xC) >> 2)][arr[i] & 0x3];
		}
	}

	private void rotate(int arr[], int pos, boolean leftShift) {
		int i, j, n = arr.length;
		pos = pos % n;
		int overwritten[] = new int[pos];
		if(leftShift) {
			for(i = 0 ; i < pos ; i++) {
				overwritten[i] = arr[i];
			}
			for(i = pos ; i < n ; i++) {
				arr[i - pos] = arr[i];
			}
			j = 0;
			for(i = n - pos ; i < n ; i++) {
				arr[i] = overwritten[j++];
			}
		}
		else {
			j = 0;
			for(i = n - pos ; i < n ; i++) {
				overwritten[j++] = arr[i];
			}
			for(i = n - pos - 1 ; i > -1 ; i--) {
				arr[i + pos] = arr[i];
			}
			for(i = 0 ; i < pos ; i++) {
				arr[i] = overwritten[i];
			}
		}
	}

	private int valueFromBits(int arr[], int start, int end) {
		int value = 0;
		for(int i = start ; i < end ; i++) {
			value <<= 1;
			value |= arr[i];
		}
		return value;
	}

	public int[][] getInverseTransformation(int transformation[][]) {
		if(transformation == null) {
			return null;
		}
		int rows = transformation.length;
		int cols = transformation[0].length;
		int[][] inverse = new int[rows][cols];
		int current;
		for(int i = 0 ; i < rows ; i++) {
			for(int j = 0 ; j < cols ; j++) {
				current = transformation[i][j];
				//System.out.println("Value = " + current + ", Row = " + (current & 0x3) + ", Column = " + (current & 0xC));
				inverse[((current & 0xC) >> 2)][current & 0x3] = i * 4 + j;
			}
		}
		return inverse;
	}

	public void displayHexadecimalMatrix(int matrix[][]) {
		int cols;
		for(int i = 0, rows = matrix.length ; i < rows ; i++) {
			cols = matrix[i].length;
			for(int j = 0 ; j < cols - 1 ; j++) {
				System.out.print(decToHex(matrix[i][j]) + " ");
			}
			System.out.println(decToHex(matrix[i][cols - 1]));
		}
	}

	private char decToHex(int number) {
		switch(number) {
		case 0 :
			return '0';
		case 1 :
			return '1';
		case 2 :
			return '2';
		case 3 :
			return '3';
		case 4 :
			return '4';
		case 5 :
			return '5';
		case 6 :
			return '6';
		case 7 :
			return '7';
		case 8 :
			return '8';
		case 9 :
			return '9';
		case 10 :
			return 'A';
		case 11 :
			return 'B';
		case 12 :
			return 'C';
		case 13 :
			return 'D';
		case 14 :
			return 'E';
		case 15 :
			return 'F';
		default :
			return '\n';
		}
	}
}

public class SAES {
	public static void main(String args[]) {
		new Operate(new Scanner(System.in));
	}
}
