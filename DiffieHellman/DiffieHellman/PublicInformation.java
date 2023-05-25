package DiffieHellman;

import java.io.Serializable;

public class PublicInformation implements Serializable {
	private int publicKey;
	private static final long serialversionUID = 1111L;

	public PublicInformation(int publicKey) {
		this.publicKey = publicKey;
	}

	public int getKey() {
		return publicKey;
	}
}
