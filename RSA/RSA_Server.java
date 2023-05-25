import java.util.HashMap;
import RSA.RSA_Key;
import RSA.RSA_NameAndKeyPair;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;

class Manager {
	private HashMap<String, RSA_Key> directory;
	private final int SERVER_PORT = 5432;

	public Manager() {
		directory = new HashMap<String, RSA_Key>();
		listenRequests();
	}

	public void listenRequests() {
		try {
			ServerSocket server = new ServerSocket(SERVER_PORT);
			Socket client;
			ObjectInputStream oin;
			ObjectOutputStream out;
			Object received;
			RSA_NameAndKeyPair toAdd;
			Object response;

			while(true) {
				client = server.accept();
				oin = new ObjectInputStream(client.getInputStream());
				out = new ObjectOutputStream(client.getOutputStream());
				received = oin.readObject();

				if(received instanceof RSA_NameAndKeyPair) {
					toAdd = (RSA_NameAndKeyPair)received;
					response = addToDirectory(toAdd);
				}
				else if(received instanceof String) {
					System.out.println("Look-up starting :");
					response = lookup_directory((String)received);
					System.out.println("Look-up completed.");
				}
				else {
					System.out.println("Received unknown object from " + client.getRemoteSocketAddress().toString());
					response = "Invalid object sent.";
				}

				out.writeObject(response);
				out.close();
				oin.close();
				client.close();

				displayDirectory();
			}
		}
		catch(IOException ioe) {
			System.out.println(ioe);
		}
		catch(ClassNotFoundException cfe) {
			System.out.println(cfe);
		}
	}

	public void displayDirectory() {
		System.out.println("\tDirectory\n");
		System.out.println("Name\tPublic Key\n");

		Iterator<Map.Entry<String, RSA_Key>> directoryIterator = directory.entrySet().iterator();
		Map.Entry<String, RSA_Key> currentEntry;
		RSA_Key publicKey;
		while(directoryIterator.hasNext()) {
			currentEntry = directoryIterator.next();
			System.out.print(currentEntry.getKey() + "\t");
			publicKey = currentEntry.getValue();

			System.out.println("{" + publicKey.getExponent() + ", " + publicKey.getBase() + "}");
		}
	}

	private Object lookup_directory(String name) {
		RSA_Key publicKey = directory.get(name);
		if(publicKey == null) {
			return ("The directory does not contain any public key for user " + name + ".");
		}

		return new RSA_NameAndKeyPair(name, publicKey);
	}

	private Object addToDirectory(RSA_NameAndKeyPair toAdd) {
		String name = toAdd.getName();
		RSA_Key publicKey = toAdd.getPublicKey();
		int base = publicKey.getBase();
		int exponent = publicKey.getExponent();

		Iterator<Map.Entry<String, RSA_Key>> directoryIterator = directory.entrySet().iterator();
		Map.Entry<String, RSA_Key> currentEntry;
		RSA_Key currentPublicKey;
		String currentName;
		while(directoryIterator.hasNext()) {
			currentEntry = directoryIterator.next();
			currentName = currentEntry.getKey();
			if(currentName.equals(name)) {
				return ("The name " + name + " is already taken by a different user.");
			}
			currentPublicKey = currentEntry.getValue();
			if((currentPublicKey.getBase() == base) && (currentPublicKey.getExponent() == exponent)) {
				return ("This public key already exists. Select a new key.");
			}
		}

		directory.put(name, publicKey);
		return ("The entry {" + name + ", {" + exponent + ", " + base + "}"+ "} was added successfully to the directory.");
	}
}


public class RSA_Server {
	public static void main(String args[]) {
		new Manager();
	}
}
