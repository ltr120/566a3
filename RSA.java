import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class RSA {

	static String input_file;
	static String output_file;
	static int key_size;
	static String key_file;
	static BigInteger d;
	static BigInteger e;
	Key key;

	public RSA(Key key) {
		this.key = key;
	}

	public RSA(Key key, BigInteger i, boolean is_encryption_key) {
		if (is_encryption_key) {
			this.e = i;
		} else {
			this.d = i;
		}
		this.key = key;
	}

	public static void main(String args[]) {
		// parse extra options
		try {
			switch (args[0]) {
			case "-e":
			case "-d":
			case "-k":
				get_extra_options(args);
				break;
			default:
				print_usage(args);
			}
		} catch (Exception e) {
			print_usage(args);
			System.exit(1);
		}
		RSA rsa = new RSA(null);
		switch (args[0]) {
		case "-k":
			generate_keys();
			break;
		case "-e":
			rsa.encrypt(input_file, output_file);
			break;
		case "-d":
			rsa.decrypt(input_file, output_file);
			break;
		}
	}

	private static void print_usage(String[] args) {
		String head = "Usage: RSA ";
		String preb = "";
		for (int i = 0; i < head.length(); i++) {
			preb += " ";
		}
		System.out.println(head + "-h |");
		System.out.println(preb + "-k <key_file> [-b <bit_size>] |");
		System.out.println(preb
				+ "-e <public_key_file> -i <input_file> -o <output_file> |");
		System.out.println(preb
				+ "-d <private_key_file> -i <input_file> -o <output_file>");
	}
	
	/**
	 * get_extra_options:
	 *
	 * parse extra arguments
	 *
	 * @param args
	 */
	private static void get_extra_options(String[] args) {
		try {
			int i = 2;
			key_file = args[1];
			if (args[0].equals("-k")) {
				// if command option is generating new keys
				if (args.length > 2) {
					// if there are more argument
					if (args[2].equals("-b")) {
						key_size = Integer.valueOf(args[3]);
					} else {
						throw new IllegalArgumentException("Unrecorgnizable argument: " + args[2]);
					}
				} else {
					// if the bit size is not specified
					// set key_size to 1024
					key_size = 1024;
				}
			}
			else {
				// command option is either encrypt or decrypt
				boolean hasInput = false;
				boolean hasOutput = false;
				while (!hasInput || !hasOutput) {
					switch (args[i]) {
					case "-i":
						if (!hasInput){
							input_file = args[++i];
							hasInput = true;
						} else {
							throw new IllegalArgumentException("multiple input files!");
						}
						break;
					case "-o":
						if (!hasOutput) {
							output_file = args[++i];
							hasOutput = true;
						} else {
							throw new IllegalArgumentException("multiple output files!");
						}
						break;
					default:
						throw new IllegalArgumentException("Unrecorgnizable argument:" + args[i]);
					}
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			print_usage(args);
			System.exit(1);
		}
	}
	
	private static void generate_keys() {
		// generate two large prime number
		// the bitsize of prime numbers are key_size/2
		// since we want the size of n to be key_size
		BigInteger n = p.multiply(q);

		BigInteger p = BigInteger.probablePrime(key_size/2, new Random());
		BigInteger q = BigInteger.probablePrime(key_size/2, new Random());
		
		// select a small odd integer e relatively prime with Totient of n
		BigInteger totient = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		int e = 2;
		while (e % 2 == 0 || totient.gcd(BigInteger.valueOf(e)).intValue() != 1) {
			Random random = new Random();
			do {
				e = random.nextInt();
			} while (e < 2 || e % 2 == 0);
		}
		
		// compute the modular inverse of e
		BigInteger d = BigInteger.valueOf(e).modInverse(totient);
		
		// write to key files
		try {
			Key key = new Key(n, key_size);
			FileWriter fw = new FileWriter(key_file+".public");
			fw.write(key.toHex()+"\n"+BigInteger.valueOf(e).toString(16));
			fw.close();
			fw = new FileWriter(key_file+".private");
			fw.write(key.toHex()+"\n"+d.toString(16));
			fw.close();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(1);
		}
		
		
	}

	/*
	Encrypt a message, return the cipher in a String
	Block is seperated by newline character
	*/
	public String encrypt(String message) {
		InputStream inputStream = new ByteArrayInputStream(message.getBytes());
		try {
			return encrypt(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
		return "";
	}

	/*
	Encrypt the content of input_file and print the result in output file
	*/
	private void encrypt(String input_file, String output_file) {
		try {
			Scanner pub = scan_file(key_file);
			key = new Key(pub.nextLine(), 16);
			e = new BigInteger(pub.nextLine(), 16);
			InputStream inputStream;
			FileWriter outputWriter;
			inputStream = new FileInputStream(input_file);
			outputWriter = new FileWriter(output_file);
			outputWriter.write(encrypt(inputStream));
			outputWriter.close();
			inputStream.close();

		} catch (Exception exception){
			exception.printStackTrace();
			System.exit(2);
		}
	}

	/*
	Core method of encryption, inputStream will be seperate to individual
	blocks, and padding will be added to the end
	*/
	public String encrypt(InputStream inputStream) throws IOException{
		// assume the first line is n and second line is e
		// use key to store n
		Block m;
		Block c;
		String result = "";
		int blockSize = key.size/2;
		// block size mod 8 should be 0
		while (blockSize % 8 != 0) {
			blockSize++;
		}
		
			
		// block size is half the key size
		byte[] blockInBytes = new byte[blockSize];
		while (inputStream.available() >= key.size) {
			inputStream.read(blockInBytes);
			m = new DataBlock(new BigInteger(blockInBytes), blockSize);
			// c = m^e mod n
			c = encrypt(m,e);
			result += (c.toHex()+"\n");
		}
		// for the last block
		BigInteger blockInInt = BigInteger.ZERO;
		boolean eof = false;
		int dataCount = 0;
		while (!eof) {
			int b = inputStream.read();
			if (b == -1) {
				eof = true;
			} else {
				blockInInt = blockInInt.shiftLeft(8);
				blockInInt = blockInInt.add(BigInteger.valueOf(b));
				dataCount++;
			}
		}
		int remainingBytes = blockSize/8 - dataCount;
		for (int i = 0; i < remainingBytes; i++) {
			blockInInt = blockInInt.shiftLeft(8);
		}

		if (remainingBytes < 4) {
			// if there is not enough bytes to append count (an integer)
			result += (encrypt(new DataBlock(blockInInt,key.size), e).toHex()+"\n");
			// add a padding block, and the count = remaining bytes + key.size
			result += (encrypt(new DataBlock(BigInteger.valueOf(remainingBytes),blockSize), e).toHex()+"\n");
		} else {
			blockInInt = blockInInt.add(BigInteger.valueOf(remainingBytes));
			result += (encrypt(new DataBlock(blockInInt,blockSize), e).toHex()+"\n");
		}
		return result; 
		
	}

	/*
	Some RSA object already know the e part of the key, this is just a backup
	function in case e is not specified.
	*/
	public Block encrypt(Block m) {
		return encrypt(m, e);
	}
	
	/*
	n is stored in key object, this method will encrypt block m (assume m is
	properly padded), and return the cipher in a block.
	*/
	private Block encrypt(Block m, BigInteger e) {
		return new DataBlock(m.value.modPow(e,key.value), key.size); 
	}

	/*
	decrypt a cipher string and return the plaintext as a string
	*/
	public String decrypt(String cipher) {
		Scanner scanner = new Scanner(cipher);
		return decrypt(scanner);
	}

	/*
	decrypt file and write to output file
	*/
	private void decrypt(String input_file, String output_file) {
		Scanner priv = scan_file(key_file);
		// assume the first line is n and second line is e
		// use key to store n
		key = new Key(priv.nextLine(), 16);
		d = new BigInteger(priv.nextLine(), 16);
		Scanner inputScanner;
		FileWriter outputWriter;
		try {
			inputScanner = scan_file(input_file);
			outputWriter = new FileWriter(output_file);
			outputWriter.write(decrypt(inputScanner));
			outputWriter.close();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(2);
		}
	}

	/*
	core method of decryption, it will get rid of the padding after decrypt
	each cipher block. Assume each block is seperated by new line.
	*/
	public String decrypt(Scanner inputScanner) {
		Block prev = null, curr = null, cipher = null;
		String result = "";
		while (inputScanner.hasNextLine()) {
			cipher = new DataBlock(inputScanner.nextLine(), 16);
			curr = decrypt(cipher, d);
			if (!inputScanner.hasNextLine() && curr != null) {
				// if this is the last block
				// get the padding count
				int count = curr.value.intValue();
				if (count >= cipher.size) {
					// if padding count is greater than block size
					// get rid of last block
					count -= cipher.size;
					String plaintext = prev.toString();
					result += (plaintext.substring(0, plaintext.length() - count));				
				} else {
					// if padding is within the last block
					if (prev != null) {
						result += (prev.toString());
					}
					String plaintext = curr.toString();

					result += (plaintext.substring(0, plaintext.length() - count));
				}
			}
			prev = curr;
		}
		return result;
			
		
		
	}
	
	/**
	 * decrypt:
	 *
	 * decrypt provided cipher block, n is stored in this.key attributes
	 *
	 * @param cipher
	 * @param d
	 * @return the block contains message
	 */
	private DataBlock decrypt(Block cipher, BigInteger d) {
		int plaintext_blocksize = key.size / 2;
		while (plaintext_blocksize % 8 != 0) {
			plaintext_blocksize++;
		}
		return new DataBlock(cipher.value.modPow(d, key.value), plaintext_blocksize);
	}

	public DataBlock decrypt(Block cipher) {
		return decrypt(cipher, d);
	}

	/**
	 * scan_file:
	 *
	 * scan the file and return the Scanner object
	 *
	 * @param filename
	 * @return
	 */
	public static Scanner scan_file(String filename) {
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
