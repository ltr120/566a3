import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;


public class Key extends Block {

	BigInteger[] weak_keys = {
			new BigInteger("0000000000000000", 16),
			new BigInteger("001e001e000e000e", 16),
			new BigInteger("00e000e000f000f0", 16),
			new BigInteger("00fe00fe00fe00fe", 16),
			new BigInteger("1e001e000e000e00", 16),
			new BigInteger("1e1e1e1e0e0e0e0e", 16),
			new BigInteger("1ee01ee00ef00ef0", 16),
			new BigInteger("1efe1efe0efe0efe", 16),
			new BigInteger("e000e000f000f000", 16),
			new BigInteger("e01ee010f00ef00e", 16),
			new BigInteger("e0e0e0e0e0e0e0e0", 16),
			new BigInteger("e0fee0fef0fef0fe", 16),
			new BigInteger("fe00fe00fe00fe00", 16),
			new BigInteger("fe1efe1efe0efe0e", 16),
			new BigInteger("fee0fee0fee0fee0", 16),
			new BigInteger("fefefefefefefefe", 16),
	};
	
	/**
	 * Call super constructors
	 */
	public Key(int n) {
		super(n);
	}
		
	public Key(BigInteger value, int size) {
		super(value,size);
	}

	public Key(Key c, Key d) {
		super(c,d);
	}

	public Key(String s, int radix) {
		super(s,radix);
	}

	// return whether the this is a weak key
	public boolean isWeak() {
		BigInteger k = value.and(new BigInteger("0e0e0e0e0e0e0e0e", 16)); // p => 0, P => e
		return Arrays.asList(weak_keys).contains(k);
	}

	public BigInteger xor(BigInteger i) {
		return value.xor(i);
	}
	
	/**
	 * Random:
	 * 
	 * give the key a random value, won't generate weak key value
	 */
	public void random() {
		Random random = new Random();
		while (!this.isWeak()) {
			value = new BigInteger(this.size, random);
		}
	}
}
