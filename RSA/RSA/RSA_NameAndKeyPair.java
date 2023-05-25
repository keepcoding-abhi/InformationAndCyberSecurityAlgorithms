package RSA;

import java.io.Serializable;

public class RSA_NameAndKeyPair implements Serializable {
	private String name;
	private RSA_Key publicKey;
	private final static long SerialVersionUID = 1111L;

	public RSA_NameAndKeyPair(String name, RSA_Key key) {
		this.name = name;
		this.publicKey = key;
	}

	public String getName() {
		return name;
	}

	public RSA_Key getPublicKey() {
		return publicKey;
	}

	@Override
	public String toString() {
		return "{" + name + ", "+ publicKey + "}";
	}
}
