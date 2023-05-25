import java.util.Scanner;
import RSA.RSA_Key;
import RSA.RSA_NameAndKeyPair;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

class RsaEncryption {
	private Scanner console;
	private RSA_Key my_public, my_private;
	private String name, serverIP;
	private final int SERVER_PORT = 5432;

	public RsaEncryption(Scanner sc) {
		this.console = sc;
		getInput();
		displayMenu();
		//encrypt();
		/*System.out.println("Enter a, b, and n.");
		int a = sc.nextInt();
		int b = sc.nextInt();
		int n = sc.nextInt();
		System.out.println("Result = " + find_exponentiation(a, b, n));*/
	}

	private void displayMenu() {
		int choice;
		do {
			System.out.println("Press 1 to send message.\nPress 2 to receive a message.\nPress 3 to quit.");
			choice = console.nextInt();
			console.nextLine();
			switch(choice) {
			case 1:
				encrypt();
				break;
			case 2:
				decrypt();
				break;
			}
		}while(choice != 3);
	}

	private void decrypt() {
		System.out.println("Enter your port number :");
		int port = console.nextInt();
		console.nextLine();
		String cipher = null;

		try {
			ServerSocket server = new ServerSocket(port);
			Socket client = server.accept();
			DataInputStream din= new DataInputStream(new BufferedInputStream(client.getInputStream()));

			cipher = din.readUTF();
			din.close();
			client.close();
			server.close();
		}
		catch(IOException ioe) {
			System.out.println(ioe);
			return;
		}

		System.out.println("Received ciphertext :\n" + cipher);
		String plaintext = decrypt_string(my_private, cipher);

		System.out.println("The plaintext is :\n" + plaintext);
	}

	private String decrypt_string(RSA_Key private_key, String ciphertext) {
		int currentVal, cipher_pointer = 0, cipher_length, current_plain;
		StringBuilder deciphered = new StringBuilder();
		char cipher[] = ciphertext.toCharArray();
		cipher_length = cipher.length;
		char current_ch;
		int base = private_key.getBase(), exponent = private_key.getExponent();

		while(cipher_pointer < cipher_length) {
			currentVal = 0;
			while((current_ch = cipher[cipher_pointer++]) != ',') {
				currentVal *= 10;
				currentVal += (current_ch - '0');
			}

			current_plain = find_exponentiation(currentVal, exponent, base);
			deciphered.append(current_plain);
		}

		return (new String(deciphered));
	}

	private void encrypt() {
		String plaintext = null;
		String userName, other_ip;
		Socket client;
		ObjectInputStream oin;
		ObjectOutputStream out;
		Object received;
		RSA_Key other_public = null;
		int other_port;

		System.out.println("Enter the user-name of receiver :");
		userName = console.nextLine();
		System.out.println(userName);

		try {
			System.out.println("Connecting to : " + serverIP + " : " + SERVER_PORT);
			client = new Socket(serverIP, SERVER_PORT);
			System.out.println("Connected.");
			//oin = new ObjectInputStream(client.getInputStream());
			out = new ObjectOutputStream(client.getOutputStream());
			oin = new ObjectInputStream(client.getInputStream());
			System.out.println("Connected.");

			out.writeObject(userName);
			out.flush();
			System.out.println("Sent :" + userName);

			received = oin.readObject();

			if(received instanceof RSA_NameAndKeyPair) {
				RSA_NameAndKeyPair received_pair = (RSA_NameAndKeyPair)received;
				System.out.println("Received :\n" + received_pair);
				other_public = received_pair.getPublicKey();
			}
			else if(received instanceof String) {
				System.out.println("Got following message from server :");
				System.out.println((String)received);
			}
			else {
				System.out.println("Received unknown object from server.");
			}

			out.close();
			oin.close();
			client.close();
		}
		catch(UnknownHostException uhe) {
			System.out.println(uhe);
		}
		catch(IOException ioe) {
			System.out.println(ioe);
		}
		catch(ClassNotFoundException cfe) {
			System.out.println(cfe);
		}

		if(other_public == null) {
			return;
		}

		boolean check;
		while(true) {
			System.out.println("Enter the number to encrypt :");
			plaintext = console.nextLine();
			check = isNumber(plaintext);

			if(check == false) {
				System.out.println("The enetered string contais non-numeric characters.");
				continue;
			}
			else {
				break;
			}
		}

		String ciphertext = encrypt_string(other_public, plaintext);
		System.out.println("Encrypted cipher text is :\n" + ciphertext);

		System.out.println("Enter IP address and port of the receiver :");
		other_ip = console.next();
		other_port = console.nextInt();
		console.nextLine();

		try {
			client = new Socket(other_ip, other_port);
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
			dout.writeUTF(ciphertext);
			dout.close();
			client.close();
		}
		catch(UnknownHostException uhe) {
			System.out.println(uhe);
		}
		catch(IOException ioe) {
			System.out.println(ioe);
		}
	}

	public String encrypt_string(RSA_Key public_key, String plaintext_string) {
		int currentVal, newVal, currentCipher;
		char[] plaintext = plaintext_string.toCharArray();
		int plain_length = plaintext.length;
		int plaintext_pointer = 0;
		int base = public_key.getBase(), exponent = public_key.getExponent();

		StringBuilder cipher = new StringBuilder();
		while(true) {
			currentVal = 0;
			while(plaintext_pointer < plain_length) {
				newVal = currentVal;
				newVal *= 10;
				newVal += (plaintext[plaintext_pointer] - '0');

				if(newVal < base) {
					currentVal = newVal;
					plaintext_pointer++;
				}
				else {
					break;
				}
			}

			currentCipher = find_exponentiation(currentVal, exponent, base);
			cipher.append(currentCipher + ",");

			if(plaintext_pointer == plain_length) {
				break;
			}
		}

		return (new String(cipher));
	}

	public boolean isNumber(String text) {
		char ch;
		for(int i = 0 , n = text.length() ; i < n ; i++) {
			ch = text.charAt(i);
			if(ch > '9' || ch < '0') {
				return false;
			}
		}
		return true;
	}

	private void getInput() {

		boolean correct = false;
		int p, q, n, m, e, d;

		System.out.println("Enter your name :");
		name = console.nextLine();

		while(true) {
			System.out.println("Enter p and q:");
			p = console.nextInt();
			q = console.nextInt();
			console.nextLine();
			if(p == q) {
				System.out.println("Error! p == q.");
				continue;
			}
			correct = checkPrime(p);
			if(correct == true) {
				correct = checkPrime(q);
				if(correct == true) {
					break;
				}
				else {
					System.out.println("Error ! q = " + q + " is not a prime.");
				}
			}
			else {
				System.out.println("Error ! p = " + p + " is not a prime.");
			}
		}

		n = p * q;
		m = (p - 1) * (q - 1);

		while(true) {
			System.out.println("Enter public key(e) :");
			e = console.nextInt();
			console.nextLine();
			if(e > 1 && e < m) {
				if(checkCoPrime(e, m)) {
					break;
				}
				else {
					System.out.println("e and m must be co-prime.");
				}
			}
			else {
				System.out.println("Error! e must be > 1 and < m.");
			}
		}

		d = find_multiplicative_inverse(m, e);

		//System.out.println("d = " + d);
		my_public = new RSA_Key(e, n);
		my_private = new RSA_Key(d, n);

		System.out.println("My private key : {" + my_private.getExponent() + ", " + my_private.getBase() + "}");
		System.out.println("My public key : {" + my_public.getExponent() + ", " + my_public.getBase() + "}");

		RSA_NameAndKeyPair my_entry = new RSA_NameAndKeyPair(name, my_public);

		System.out.println("Enter IP address of server :");
		serverIP = console.next();
		console.nextLine();

		try {
			Socket client = new Socket(serverIP, SERVER_PORT);
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream oin = new ObjectInputStream(client.getInputStream());
			out.writeObject(my_entry);
			Object response = oin.readObject();
			if(response instanceof String) {
				System.out.println((String)response);
			}
			oin.close();
			out.close();
			client.close();
		}
		catch(UnknownHostException uhe) {
			System.out.println(uhe);
		}
		catch(IOException ioe) {
			System.out.println(ioe);
		}
		catch(ClassNotFoundException cfe) {
			System.out.println(cfe);
		}
	}

	public int find_exponentiation(int a, int b, int n) {
		//Calculates a^b mod(n)
		int result = 1;
		int bitCount = get_number_of_bits(b);
		int mask = (1 << bitCount);

		while(mask != 0) {
			result = ((result * result) % n);
			if((mask & b) != 0) {
				result = ((result * a) % n);
			}
			mask >>= 1;
		}
		return result;
	}

	public int get_number_of_bits(int num) {
		int count = 0;
		while(num != 0) {
			count++;
			num >>= 1;	
		}
		return count;
	}

	public int find_multiplicative_inverse(int base, int num) {
		int x_minus_2, y_minus_2, x_i, y_i, remainder, r_minus_2, r_i, q_i, old_x_i, old_y_i, old_r_i;

		x_minus_2 = 1;
		y_minus_2 = 0;
		x_i = 0;
		y_i = 1;
		r_minus_2 = base;
		r_i = num;

		old_r_i = r_i;
		q_i = r_minus_2 / r_i;
		r_i = r_minus_2 - (r_i * q_i);

		old_x_i = x_i;
		old_y_i = y_i;
		x_i = x_minus_2 - (q_i * x_i);
		y_i = y_minus_2 - (q_i * y_i);

		y_minus_2 = old_y_i;
		x_minus_2 = old_x_i;
		r_minus_2 = old_r_i;

		while(true) {
			old_r_i = r_i;
			q_i = r_minus_2 / r_i;
			r_i = r_minus_2 - (r_i * q_i);
			//System.out.println("ri-2 = " + r_minus_2 + " q_i = " + q_i);
			//System.out.println("x_i = " + x_i + " y_i = " + y_i);
			if(r_i == 0) {
				break;
			}

			old_x_i = x_i;
			old_y_i = y_i;

			x_i = x_minus_2 - (q_i * x_i);
			y_i = y_minus_2 - (q_i * y_i);

			x_minus_2 = old_x_i;
			y_minus_2 = old_y_i;

			r_minus_2 = old_r_i;
		}

		//System.out.println("x_i = " + x_i + " y_i = " + y_i);
		if(y_i < 0) {
			return (y_i + base);
		}
		return y_i;
	}

	private boolean checkCoPrime(int a, int b) {
		if(a < b) {
			a = a + b;
			b = a - b;
			a = a - b;
		}
		int remainder;
		do {
			remainder = a % b;
			a = b;
			b = remainder;
		}while(remainder != 0);
		if(a == 1) {
			return true;
		}
		return false;
	}

	private boolean checkPrime(int num) {
		if(num < 2) {
			return false;
		}
		if(num == 2) {
			return true;
		}
		if(num % 2 == 0) {
			return false;
		}
		double squareRoot = Math.sqrt(num);
		int last = (int)squareRoot;
		if(last != squareRoot) {
			last++;
		}
		for(int i = 3 ; i <= last ; i += 2) {
			if((num % i) == 0) {
				return false;
			}
		}
		return true;
	}
}

public class RSA {
	public static void main(String args[]) {
		new RsaEncryption(new Scanner(System.in));
	}
}
