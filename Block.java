import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

/**
 * Class: Block
 * 
 * Attributes: value (BigInteger) -- the value of the block
 * 			   size  (int)        -- the size of the block
 * 
 */
public abstract class Block {
	
	protected BigInteger value;
	protected int size;
	
	/**
	 * Constructor:
	 * 
	 * Create a n sized block with value 0.
	 * 
	 * @param n (int) -- the size of the block
	 */
    public Block(int n) {
		this.value = new BigInteger("0");
		this.size = n;
	}

    /**
     * Constructor:
     * 
     * @param n
     * @param block
     */
	public Block(int n, Block block) {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor:
	 * 
	 * Concatenate two blocks into one.
	 * @param a (Block)
	 * @param b (Block)
	 */
	public Block(Block a, Block b) {
		size = a.size + b.size;
		value = a.value.shiftLeft(b.size).add(b.value);
	}
	
	/**
	 * Constructor:
	 *
	 * Convert a string to a block (string contains the representation of the value)
	 *
	 * @param key_string (String) -- number represented in string.
	 * @param radix      (int)    -- the base in which the string will be converted.
	 */
	public Block(String s, int radix) {
		value = new BigInteger(s, radix);
		if (radix == 16) {
			size = s.length() * 4;
		} else {
			size = value.bitLength();
		}
	}
	
	/**
	 * Constructor:
	 * @param value
	 * @param size
	 */
	public Block(BigInteger value, int size) {
		this.value = value;
		this.size = size;
	}
	
	/**
	 * size:
	 * @return the size of the block.
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * at:
	 * @param n (int)
	 * @return true: if bit at n is 1
	 *         false: if bit at n is 0
	 */
	public boolean at(int n) {
		return this.value.testBit(n);
	}

	/**
	 * setBit:
	 *
	 * set bit i to 1 if b is true
	 * else set bit i to 0
	 *
	 * @param i (int)     -- index
	 * @param b (boolean) -- value to set
	 */
	public void setBit(int i, boolean b) {
		if (b) {
			value = value.setBit(i);
		} else {
			value = value.clearBit(i);
		}		
	}

	/**
	 * toHex:
	 * @return the hex decimal string to represent the block
	 */
	public String toHex() {
		return String.format("%"+(size+3)/4+"s", value.toString(16)).replace(' ', '0');
	}

	/**
	 * toBin:
	 * @return the binary string to represent the block
	 */
	public String toBin() {
		return String.format("%"+size+"s", value.toString(2)).replace(' ', '0');
	}

	/**
	 * shiftLeft:
	 *
	 * shift left n bits
	 * the leftmost n bits become the rightmost n bits
	 *
	 * @param n (int)
	 */
	public void shiftLeft(int n) {
		BigInteger left = this.value.shiftLeft(n);
		BigInteger right = this.value.shiftRight(size - n);
		BigInteger result = left.or(right);
		this.value = result.mod(BigInteger.valueOf(2).pow(size));
	}
	
	/**
	 * permutation:
	 * 
	 * the index in the table is in reverse order.
	 * So, the permutation start from the leftmost (highest index) bit.
	 * 
	 * @param table (Scanner) -- permutation table
	 */
	public Block permutation(Scanner table) {
		return permutation(table, size);
	}

	/**
	 * permutation:
	 *
	 * the output a permutation of the this block with specified size.
	 *
	 * @param table
	 * @param size
	 */
	public Block permutation(Scanner table, int size) {
		Block temp = new DataBlock(size);
		for (int i = 1; i <= size; i++) {
			int index = this.size - table.nextInt();
				temp.setBit(size - i, this.at(index));
				//System.out.println(index + "=>" + (size - i));
		}
		return temp;
	}
	/**
	 * subBlock:
	 * 
	 * @param i
	 * @param j
	 * @return a block has bit [i,j)
	 */
	public Block subBlock(int i, int j) {
		// get rid of bits after i bits.
		BigInteger newValue = value.shiftRight(i);
		// get rid of bits before j by mod 2^(j-i)
		newValue = newValue.mod(new BigInteger("1").shiftLeft(j-i));
		return new DataBlock(newValue, j-i);
	}

	/**
	 * xor:
	 * @param anotherBlock
	 */
	public void xor(Block anotherBlock) {
		value = this.value.xor(anotherBlock.value);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		BigInteger temp = value;
		for (int i = 0; i < size; i+=8) {
			char c = (char) temp.mod(new BigInteger("128")).intValue();
			temp = temp.shiftRight(8);
			sb.insert(0, c);
		}
		return sb.toString();
	}
}