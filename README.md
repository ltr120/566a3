CSC 566 hw2
Students: Youhao Wei & Yi Huang

Core Files:

DES.java:
    
    contains a main method, and the core methods for DES encryption.

RSA.java:

    contains a main method, and the core methods for RSA encryption.

Block.java:
    
    Abstract class for Key.java and DataBlock.java. These classes are used
    for simulating blocks. a block is either key block or data block depends
    on the content of the block. Compared with key block has some extra methods, such as random a key value, check if the key is a weak key. These
    files are shared by RSA and DES. The data of each block is stored as a BigInteger. There is also a attribute indicates the size of the block.

CHAT.java:

    contains the main method, Alice.java and Bob.java contains methods for
    Alice and Bob.

----------------------------

Note:

RSA key file that generated and used for encryption and decryption are of this format:

n'\n'e or n'\n'd

first line is n in hex, and second line is e/d in hex. Other format might not work with the encryption and decryption.