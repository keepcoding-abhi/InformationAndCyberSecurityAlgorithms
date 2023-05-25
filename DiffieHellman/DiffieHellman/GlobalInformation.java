package DiffieHellman;

public class GlobalInformation {
	private static final long serialversionUID = 2222L;

	private int prime, primitiveRoot;

	public GlobalInformation(int prime, int primitiveRoot) {
		this.prime = prime;
		this.primitiveRoot = primitiveRoot;
	}

	public int getPrime() {
		return prime;
	}

	public int getPrimitiveRoot() {
		return primitiveRoot;
	}
}
