import java.math.BigInteger;


public class DataBlock extends Block {

	/**
	 * Constructors:
	 * 
	 * All constructors are inherited from Super Class "Block"
	 */
	public DataBlock(BigInteger value, int size) {
		super(value, size);
	}

	public DataBlock(int i) {
		super(i);
	}

	public DataBlock(Block l, Block r) {
		super(l,r);
	}
	
	public DataBlock(String s, int radix) {
		super(s,radix);
	}

}
