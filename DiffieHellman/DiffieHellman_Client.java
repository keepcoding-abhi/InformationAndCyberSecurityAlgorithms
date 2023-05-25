import java.util.Scanner;
import DiffieHellman.GlobalInformation;
import DiffieHellman.PublicInformation;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;

class Client {
	private Scanner console;
	private int publicKey, privateKey, sharedSecretKey;
	private int prime, primitiveRoot;
	private final int port = 5000;

	public Client(Scanner sc) {
		this.console = sc;
		getGlobalInfo();
		if(prime != 0) {
			publicKey = calculatePublicKey();
		}
	}

	private int calculatePublicKey() {
		System.out.println("Prime :" + prime);
		System.out.println("Primitive Root :" + primitiveRoot);
	}

	private void getGlobalInfo() {
		String ip;
		GlobalInformation global = null;

		System.out.println("Enter IP address of server :");
		ip = console.nextLine();
		Socket server = new Socket(ip, address);
		ObjectInputStream oin = new ObjectInputStream(server.getInputStream());

		Object received = oin.readObject();
		if(received instanceof GlobalInformation) {
			global = (GlobalInformation)received;
			prime = global.getPrime();
			primitiveRoot = global.getPrimitiveRoot();
		}
		else {
			prime = 0;
			primitiveRoot = 0;
			System.out.println("Error! wrong object received from server.");
		}
		oin.close();
		server.close();
	}
}

public class DiffieHellman_Client {
	public static void main(String args[]) {
		new Client(new Scanner(System.in));
	}
}
