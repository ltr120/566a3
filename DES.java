import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class DES {

	static String input_file;
	static String output_file;
	static String key_string;
	Key key;

	/**
	 * Constructor:
	 * 
	 * @param key
	 */
	public DES(Key key) {
		this.key = key;
	}

	public static void main(String args[]) {
		// parse extra options if needed
		try {
			switch (args[0]) {
			case "-e":
			case "-d":
				get_extra_options(args);
				break;
			case "-k":
				break;
			default:
				print_usage(args);
			}
		} catch (Exception e) {
			print_usage(args);
			System.exit(1);
		}

		// start job
		switch (args[0]) {
		case "-k":
			Key newKey = new Key(64);
			// generate a none weak random key
			do {
				newKey.random();
			} while (newKey.isWeak());
			System.out.println(newKey.toHex());
			break;

		case "-e":
			
			Key enKey = new Key(key_string, 16);
			DES enDes = new DES(enKey);
			enDes.encrypt(input_file, output_file);
			break;

		case "-d":
			Key deKey = new Key(key_string, 16);
			DES deDes = new DES(deKey);
			deDes.decrypt(input_file, output_file);
			break;
		}
	}

	/**
	 * print_usage:
	 * @param args
	 */
	private static void print_usage(String[] args) {
		String head = "Usage: DES ";
		String preb = "";
		for (int i = 0; i < head.length(); i++) {
			preb += " ";
		}
		System.out.println(head + "-h |");
		System.out.println(preb + "-k |");
		System.out.println(preb
				+ "-e <64_bit_key_in_hex> -i <input_file> -o <output_file> |");
		System.out.println(preb
				+ "-d <64_bit_key_in_hex> -i <input_file> -o <output_file>");
	}

	/**
	 * get_extra_options:
	 *
	 * parse extra options for encryption and decryption
	 *
	 * @param args
	 */
	private static void get_extra_options(String[] args) {
		try {
			boolean hasInput = false;
			boolean hasOutput = false;
			int i = 2;
			key_string = args[1];
			// get both input and outfile filenames
			while (!hasInput || !hasOutput) {
				switch (args[i]) {
				case "-i":
					if (!hasInput) {
						input_file = args[++i];
						hasInput = true;
					} else {
						throw new IllegalArgumentException(
								"multiple input files!");
					}
					break;
				case "-o":
					if (!hasOutput) {
						output_file = args[++i];
						hasOutput = true;
					} else {
						throw new IllegalArgumentException(
								"multiple output files!");
					}
					break;
				default:
					throw new IllegalArgumentException(
							"Unrecorgnizable argument:" + args[i]);
				}
				// increament i
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			print_usage(args);
			System.exit(1);
		}
	}

	/*
	Encrypt a string and return a hex string
	*/
	public String encrypt(String plaintext) {
		InputStream inputStream = new ByteArrayInputStream(plaintext.getBytes());
		String result = "";
		ArrayList<Block> cipherBlocks = encrypt(inputStream);
		for (Block cipher: cipherBlocks) {
			result += cipher.toHex();
		}
		return result;
	}

	/**
	 * encrypt:
	 *
	 * encrypt output file and write the hex encoded cipher into output file
	 *
	 * @param input_file
	 * @param output_file
	 */
	private void encrypt(String input_file, String output_file) {
		InputStream inputStream = null;
		FileWriter outputStream = null;
		try {
			inputStream = new FileInputStream(input_file);
			outputStream = new FileWriter(output_file);
			ArrayList<Block> cipherBlocks = encrypt(inputStream);
			for (Block cipher: cipherBlocks) {
				outputStream.write(cipher.toHex() + "\n");
			}
			inputStream.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(3);
		}
	}

	/*
	encrypt content of input stream, 
	*/
	private ArrayList<Block> encrypt (InputStream inputStream) {
		Key[] keys = key_schedule();
		ArrayList<Block> cipherBlocks = new ArrayList<Block>();
		
		
		// read from input
		boolean eof = false;
		
		// number of bytes to get rid off in decryption
		// will be appended to the end of the last block
		int count = 0;
		while (!eof) {
			BigInteger blockValue = new BigInteger("0");
			for (int i = 0; i < 8; i++) {
				int b = -1;
				if (!eof) {
					try {
						// read a byte
						b = inputStream.read();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(4);
					}
					if (b == -1) {
						// if eof encountered
						// start padding
						blockValue = blockValue.shiftLeft(8);
						eof = true;
						count++;
					} else {
						// if the char is not eof
						// shift space for new char
						blockValue = blockValue.shiftLeft(8);
						// add this byte to the block
						blockValue = blockValue.add(BigInteger.valueOf(b));
					}
				} else {
					// padding
					if (i != 7) {
						// if this is the last byte in the block
						// append the count
						count++;
						blockValue = blockValue.shiftLeft(8);
					} else {
						// add 0's to this byte
						count++;
						blockValue = blockValue.shiftLeft(8);
						blockValue = blockValue.add(BigInteger.valueOf(count));
					}
				}
			}
			Block block = new DataBlock(blockValue, 64);
			// encrypt this block with keys and get the cipher block
			cipherBlocks.add(encrypt(block, keys));
			if (count == 0 && eof == true) {
				// if there is no padding when reaching eof
				// add a padding block
				// 0000000000000008
				Block paddingBlock = new DataBlock(BigInteger.valueOf(8), 64);
				cipherBlocks.add(encrypt(paddingBlock, keys));
			}
		}
		return cipherBlocks;
	}

	private Block encrypt(Block block, Key[] keys) {
		// Initial Permutation
		Scanner ip = scan_file("IP.tab");
		block = block.permutation(ip);
		return cycle(block, keys);
	}

	private Block cycle(Block block, Key[] keys) {
		// split
		Scanner e = scan_file("E.tab");
		Block r = block.subBlock(0, 32);
		Block l = block.subBlock(32, 64);
		for (int i = 0; i < 16; i++) {
			l.xor(feistel(r, keys[i]));

			// swap l and r
			Block temp = l;
			l = r;
			r = temp;
		}
		// switch l and r
		Block cipher = new DataBlock(r, l);
		return cipher;
	}


	public String decrypt(String ciphertext) {
		Scanner input = new Scanner(ciphertext);
		return decrypt(input);
	}

	private void decrypt(String input_file, String output_file) {
		Scanner input = scan_file(input_file);
		FileWriter outputStream = null;
		try {
			outputStream = new FileWriter(output_file);
			outputStream.write(decrypt(input));
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	private String decrypt(Scanner input) {
		String result = "";
		Key[] keys = key_schedule();	
		while (input.hasNextLine()) {
			String line = input.nextLine();
			Block block = new DataBlock(new BigInteger(line, 16), 64);
			Block plaintextBlock = decrypt(block, keys);
			String plaintext = plaintextBlock.toString();
			if (!input.hasNextLine()) {
				// if this is the last block
				// get rid of the padding
				int count = Integer.valueOf(plaintext.charAt(7));
				plaintext = plaintext.substring(0, plaintext.length()
						- count);
			}
			result += plaintext;
		}
		return result;
	}

	/*
	decrypt the block with a set of keys (16 keys)
	this function only do the fiestel cycle.
	*/
	private Block decrypt(Block block, Key[] keys) {
		Key[] reverse_keys = new Key[16];
		for (int i = 15; i >= 0; i--) {
			reverse_keys[15 - i] = keys[i];
		}
		Block decryptedBlock = cycle(block, reverse_keys);

		// final permutation
		Scanner ip_1 = scan_file("IP-1.tab");
		Block result = new DataBlock(64);
		result = decryptedBlock.permutation(ip_1);
		return result;
	}

	/*
	returns 16 keys using key attributes
	*/
	private Key[] key_schedule() {
		Key[] result = new Key[16];

		// PC-1 and split
		Scanner pc1 = scan_file("PC-1.tab");
		Key c = new Key(28);
		Key d = new Key(28);
		for (int i = 1; i <= 28; i++) {
			int index = 64 - pc1.nextInt();
			c.setBit(28 - i, this.key.at(index));
		}
		for (int i = 1; i <= 28; i++) {
			int index = 64 - pc1.nextInt();
			d.setBit(28 - i, this.key.at(index));
		}

		// shift table
		int[] shift_table = { 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1 };
		for (int i = 0; i < 16; i++) {
			c.shiftLeft(shift_table[i]);
			d.shiftLeft(shift_table[i]);
			Key cd = new Key(c, d);
			Key k2 = new Key(48);

			// Key compress using PC-2
			Scanner pc2 = scan_file("PC-2.tab");
			for (int j = 1; j <= k2.size; j++) {
				int index = pc2.nextInt();
				k2.setBit(48 - j, cd.at(56 - index));
			}
			result[i] = k2;
		}

		return result;
	}

	/*
	feistel function, the function accept a 32 bits block and a single key block
	*/
	private Block feistel(Block r, Key key) {
		Scanner e = scan_file("E.tab");
		// expand permutation

		r = r.permutation(e, 48);

		r.xor(key);

		// S-Box
		BigInteger expanded = r.value;
		BigInteger temp = new BigInteger("0");
		for (int i = 0; i < 48 / 6; i++) {
			int[][][] sBox = get_sBox(8 - i);
			int a, b, c = 0;
			c = expanded.mod(BigInteger.valueOf(2)).intValue();
			expanded = expanded.shiftRight(1);
			b = expanded.mod(BigInteger.valueOf(16)).intValue();
			expanded = expanded.shiftRight(4);
			a = expanded.mod(BigInteger.valueOf(2)).intValue();
			expanded = expanded.shiftRight(1);
			BigInteger value = BigInteger.valueOf(sBox[a][b][c]);
			temp = temp.add(value.shiftLeft(i * 4));
		}

		// P-Box
		Scanner p = scan_file("P.tab");
		Block result = new DataBlock(temp, 32);
		result = result.permutation(p, 32);
		return result;
	}

	// Get S-box number n and return it as a 3D array
	private int[][][] get_sBox(int n) {
		Scanner s = scan_file("S" + (n) + ".tab");
		int[][][] sBox = new int[2][16][2];
		for (int i = 0; i < 16; i++) {
			sBox[0][i][0] = s.nextInt();
		}
		for (int i = 0; i < 16; i++) {
			sBox[0][i][1] = s.nextInt();
		}
		for (int i = 0; i < 16; i++) {
			sBox[1][i][0] = s.nextInt();
		}
		for (int i = 0; i < 16; i++) {
			sBox[1][i][1] = s.nextInt();
		}
		return sBox;
	}
	
	/**
	 * scan_file:
	 *
	 * scan the file and return the Scanner object
	 *
	 * @param filename
	 * @return
	 */
	private Scanner scan_file(String filename) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Fail to load file: " + filename + ", ABORT!");
			System.exit(2);
		}
		return scanner;
	}
}
