import java.util.Scanner;

class Operate {
	private Scanner console;
	private int plaintext[], ciphertext[], inputKey[], IP[], inverseIP[], roundKeys[][];

	public Operate(Scanner sc) {
		this.console = sc;
		plaintext = ciphertext = inputKey = null;
		getKey();
		getTextInput();
	}

	private void getKey() {
		int i;
		inputKey = new int[10];
		System.out.println("Enter the 10-bit key :");
		for(i = 0 ; i < 10 ; i++) {
			inputKey[i] = console.nextInt();
		}

		roundKeys = generateKeys();
		System.out.println("Round Keys :");
		for(i = 0 ; i < 2 ; i++) {
			System.out.println("K" + i + " :");
			displayArr(roundKeys[i]);
		}
	}

	private void getTextInput() {
		String input;
		System.out.println("Enter string :");
		console.nextLine();
		input = console.nextLine();
		char words[] = input.toCharArray();
		int number, i, j, n;

		IP = new int[]{2, 6, 3, 1, 4, 8, 5, 7};
		inverseIP = findInverse(IP);
		plaintext = new int[8];

		for(i = 0, n = words.length ; i < n ; i++) {

			number = words[i];
			//System.out.println(number);
			for(j= 0 ; j < 8 ; j++) {
				plaintext[j] = number & 0x00000001;
				number >>= 1;
			}
			//System.out.println("Word " + i + " ");
			//displayArr(plaintext);
			/*System.out.println("Plaintext :");
			displayArr(plaintext);

			ciphertext = encrypt(roundKeys);
			System.out.println("Ciphertext :");
			displayArr(ciphertext);*/
			ciphertext = encrypt(roundKeys);
			number = 0;
			for(j = 7 ; j > -1 ; j--) {
				number <<= 1;
				number |= ciphertext[j];
			}
			words[i] = (char)number;
		}

		String encryptedText = new String(words);

		System.out.println("Encrypted Text :" + encryptedText);

		words = encryptedText.toCharArray();

		for(i = 0, n = words.length ; i < n ; i++) {
			number = words[i];
			for(j = 0 ; j < 8 ; j++) {
				ciphertext[j] = number & 0x00000001;
				number >>= 1;
			}
			plaintext = decrypt(ciphertext, roundKeys);
			number = 0;
			for(j = 7 ; j > -1 ; j--) {
				number <<= 1;
				number |= plaintext[j];
			}
			words[i] = (char)number;
		}

		String decryptedText = new String(words);

		System.out.println("Decrypted Text:" + decryptedText);
	}

	private void getInput() {
		plaintext = new int[8];
		inputKey = new int[10];
		ciphertext = new int[10];

		IP = new int[]{2, 6, 3, 1, 4, 8, 5, 7};
		inverseIP = findInverse(IP);

		int i;

		System.out.println("Enter the 8-bit plaintext :");
		for(i = 0 ; i < 8 ; i++) {
			plaintext[i] = console.nextInt();
		}

		System.out.println("Plaintext :");
		displayArr(plaintext);

		

		ciphertext = encrypt(roundKeys);

		System.out.println("Ciphertext :");
		displayArr(ciphertext);

		int[] decryptedText = decrypt(ciphertext, roundKeys);
		System.out.println("Decrypted text :");
		displayArr(decryptedText);
	}

	private int[] decrypt(int ciphertext[], int roundKeys[][]) {
		int[] permutedCipher = permutation(ciphertext, IP);
		int i, roundOutput[][];
		int[] left, right;

		left = new int[4];
		right = new int[4];

		for(i = 0 ; i < 4 ; i++) {
			left[i] = permutedCipher[i];
			right[i] = permutedCipher[i + 4];
		}


		for(i = 1 ; i > -1 ; i--) {

			roundOutput = round(i + 1, roundKeys[i], left, right);

			if(i == 1) {
				//SWAP
				left = roundOutput[1];
				right = roundOutput[0];
			}
			else {
				left = roundOutput[0];
				right = roundOutput[1];
			}
		}
		
		int []decryptedText = new int[8];
		for(i = 0 ; i < 4 ; i++) {
			decryptedText[i] = left[i];
			decryptedText[i + 4] = right[i];
		}

		decryptedText = permutation(decryptedText, inverseIP);

		return decryptedText;
	}

	private int[] encrypt(int roundKeys[][]) {
		int i;

		//System.out.println("Initial Permutation :");
		//displayArr(IP);
		//System.out.println("Inverse Initial Permutation :");
		//displayArr(inverseIP);

		int []permutedPlaintext = permutation(plaintext, IP);
		int roundOutput[][];

		int[] left, right;
		left = new int[4];
		right = new int[4];

		for(i = 0 ; i < 4 ; i++) {
			left[i] = permutedPlaintext[i];
			right[i] = permutedPlaintext[i + 4];
		}

		//System.out.println("L0");
		//displayArr(left);
		//System.out.println("R0");
		//displayArr(right);

		for(i = 0 ; i < 2 ; i++) {

			roundOutput = round(i + 1, roundKeys[i], left, right);

			if(i == 1) {
				left = roundOutput[0];
				right = roundOutput[1];
			}
			else {
				//SWAP
				left = roundOutput[1];
				right = roundOutput[0];
			}

			/*System.out.println("L" + (i + 1) + " :");
			displayArr(left);
			System.out.println("R" + (i + 1) + " :");
			displayArr(right); */
		}

		int ciphertext[] = new int[8];
		for(i = 0 ; i < 4 ; i++) {
			ciphertext[i] = left[i];
			ciphertext[i + 4] = right[i];
		}

		ciphertext = permutation(ciphertext, inverseIP);

		return ciphertext;
	}

	private int[][] round(int number, int roundKey[], int left[], int right[]) {
		int roundOutput[][] = new int[2][];

		roundOutput[1] = new int[4];
		for(int i = 0 ; i < 4 ; i++) {
			roundOutput[1][i] = right[i];
		}

		roundOutput[0] = xor(left, f(right, roundKey));

		return roundOutput;
	}

	private int[] f(int right[], int subKey[]) {
		int EP[] = new int[]{4, 1, 2, 3, 2, 3, 4, 1};
		int P4[] = new int[]{2, 4, 3, 1};

		int expanded[] = permutation(right, EP);
		expanded = xor(expanded, subKey);

		int compressed[] = sBox(expanded);

		return permutation(compressed, P4);
	}

	private int[] sBox(int[] expanded) {
		int[][][] sBoxes = new int[][][]{
			{{1, 0, 3, 2}, {3, 2, 1, 0}, {0, 2, 1, 3}, {3, 1, 3, 2}},
			{{0, 1, 2, 3}, {2, 0, 1, 3}, {3, 0, 1, 0}, {2, 1, 0, 3}}
		};

		int[] compressed = new int[4];
		int row, col, num, count, offset;
		int box[][];

		count = 0;
		for(int i = 0 ; i < 2 ; i++) {
			box = sBoxes[i];
			offset = i * 4;
			row = (expanded[0 + offset] * 2) + expanded[3 + offset];
			col = (expanded[1 + offset] * 2) + expanded[2 + offset];
			num = box[row][col];
			offset = (i + 1) * 2 - 1;
			for(int j = 0 ; j < 2 ; j++) {
				compressed[offset--] = num % 2;
				num /= 2;
			}
		}

		return compressed;
	}

	private int[] decToBin(int num) {
		int binary[] = new int[2];
		if(num > 3) {
			System.out.println("Error! Numbner too large.");
			return null;
		}
		for(int i = 0 ; i < 2 ; i++) {
			if(num % 2 == 0) {
				binary[i] = 0;
			}
			else {
				binary[i] = 1;
			}
			num /= 2;
		}
		return binary;
	}

	private int[] xor(int left[], int[] right) {
		int len = left.length;

		if(len != right.length) {
			System.out.println("Error! The operands must be of equal length for XOR.");
			return null;
		}

		int result[] = new int[len];
		for(int i = 0 ; i < len ; i++) {
			result[i] = ((left[i] == right[i]) ? 0 : 1);
		}

		return result;
	}

	private int[] findInverse(int[] rules) {
		int len = rules.length;
		int inverse[] = new int[len];

		for(int i = 0 ; i < len ; i++) {
			inverse[rules[i] - 1] = i + 1;
		}

		return inverse;
	}

	public void displayArr(int arr[]) {
		int n = arr.length;
		for(int i = 0; i < n - 1 ; i++) {
			System.out.print(arr[i] + ", ");
		}
		System.out.println(arr[n - 1]);
	}

	private int[][] generateKeys() {
		int P10[] = new int[]{3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
		int P8[] = new int[]{6, 3, 7, 4, 8, 5, 10, 9};

		int permutedKey[] = permutation(inputKey, P10);
		int left[], right[], i, j, k;
		int subKeys[][] = new int[2][8];
		left = new int[5];
		right = new int[5];

		for(i = 0 ; i < 5 ; i++) {
			left[i] = permutedKey[i];
			right[i] = permutedKey[i + 5];
		}

		for(i = 0 ; i < 2 ; i++) {
			leftShift(left, i + 1);
			leftShift(right, i + 1);

			for(j = 0 ; j < 5 ; j++) {
				permutedKey[j] = left[j];
			}
			k = 0;
			for(j = 5 ; j < 10 ; j++) {
				permutedKey[j] = right[k++];
			}

			subKeys[i] = permutation(permutedKey, P8);
		}

		return subKeys;
	}

	private void leftShift(int arr[], int count) {
		int leftOut[] = new int[count];
		int i, j, n, len = arr.length;

		count = count % len;
		n = len - count;

		for(i = 0 ; i < count ; i++ ) {
			leftOut[i] = arr[i];
		}

		for(i = 0 ; i < n ; i++) {
			arr[i] = arr[i + count];
		}
		j = 0;
		for(i = n ; i < len ; i++) {
			arr[i] = leftOut[j++];
		}
	}

	private int[] permutation(int arr[], int rule[]) {
		int len = rule.length;
		int permutedArr[] = new int[len];
		for(int i = 0 ; i < len ; i++) {
			permutedArr[i] = arr[rule[i] - 1];
		}
		return permutedArr;
	}
}


public class SDES_new {
	public static void main(String args[]) {
		Scanner sc = new Scanner(System.in);
		new Operate(sc);
	}
}
