package RSA;

import java.io.Serializable;

public class RSA_Key implements Serializable {
	private int exponent, base;
	private static final long SerialVersionUID = 2222L;

	public RSA_Key(int k, int n) {
		this.exponent = k;
		this.base = n;
	}

	public int getExponent() {
		return exponent;
	}

	public int getBase() {
		return base;
	}

	@Override
	public String toString() {
		return "{" + exponent + ", " + base + "}";
	}
}
