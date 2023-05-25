import java.net.ServerSocket;
import DiffieHellman.GlobalInformation;
import java.util.Scanner;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;

class Server {
	private GlobalInformation global;
	private Scanner console;
	private final int port = 5000;

	public Server(Scanner console) {
		this.console = console;
		getInput();
		listenClients();
	}

	private void getInput() {
		int prime, primitiveRoot;
		boolean status;
		while(true) {
			System.out.println("Enter n:");
			prime = console.nextInt();
			status = checkPrime(prime);
			if(status == false) {
				System.out.println(prime + " is not a prime number.");
			}
			else {
				break;
			}
		}

		while(true) {
			System.out.println("Enter g:");
			primitiveRoot = console.nextInt();
			if(primitiveRoot > 1 && primitiveRoot < prime) {
				status = checkCoPrime(prime, primitiveRoot);
				if(status == false) {
					System.out.println("The numbers " + prime + " and " + primitiveRoot + " are not co-prime.");
				}
				else {
					break;
				}
			}
			else {
				System.out.println("The primitive root must be > 1 and < " + prime + ".");
			}
		}

		global = new GlobalInformation(prime, primitiveRoot);
		System.out.println("Prime = " + global.getPrime() + ". Primitive Root = " + global.getPrimitiveRoot());
	}

	private void listenClients() {
		try {
			ServerSocket server = new ServerSocket(port);
			Socket client;
			ObjectOutputStream out;

			while(true) {
				client = server.accept();
				out = new ObjectOutputStream(client.getOutputStream());
				out.writeObject(global);
				out.close();
				client.close();
			}
		}
		catch(IOException ioe) {
			System.out.println(ioe);
		}
	}

	private static boolean checkPrime(int number) {
		for(int i = 2, n = (int)Math.sqrt(number) ; i <= n ; i++) {
			if((number % i) == 0) {
				return false;
			}  
		}
		return true;
	}

	private static boolean checkCoPrime(int a, int b) {
		if(a < b) {
			a = a + b;
			b = a - b;
			a = a - b;
		}
		int remainder;
		while(true) {
			remainder = a % b;
			if(remainder == 0) {
				break;
			}
			a = b;
			b = remainder;
		}
		if(b == 1) {
			return true;
		}
		return false;
	}
}


public class DiffieHellman_Server {
	public static void main(String args[]) {
		new Server(new Scanner(System.in));
	}
}
