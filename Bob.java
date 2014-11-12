import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/*
 * Author: Yi Huang, Youhao Wei
 *
 * This is a chat client that follows the protocol which does a handshake
 * with RSA encrypted DES key and use DES to encrypt actual chatting message.
 */
public class Bob {
    
    Socket alice;
    Scanner in;
    DataOutputStream os;
    RSA encrypter;
    RSA decrypter;
    Key desKey;
    DES des;
    String pubFileName;
    String priFileName;

    /*
     * On startup, Bob connect to Alice, then read in RSA key files. After
     * that, Bob initializes handshake and then start chatting if handshake
     * is successful.
     */
    public Bob(String addr, int port, String pubFile, String priFile) throws UnknownHostException, IOException {
        alice = new Socket(addr, port);
        alice.setTcpNoDelay(true);
        in = new Scanner(alice.getInputStream());
        os = new DataOutputStream(alice.getOutputStream());
        this.pubFileName = pubFile;
        this.priFileName = priFile;
        prepareKeys();
        initHandShake();
        startChatting();
    }
    
    /*
     * This is the main chatting function. Bob sends the first message and wait
     * for Alice to type something. Once read Alice's message, Bob can reply to
     * it, and so on.
     */
    private void startChatting() throws IOException {
        Scanner stdin = new Scanner(System.in);
        String msg;
        String cipher;
        while (stdin.hasNextLine()) {
            msg = stdin.nextLine();
            cipher = des.encrypt(msg);
            os.writeBytes(cipher + " ");
            os.flush();
            if (in.hasNext()) {
                cipher = in.next();
            } else {
                break;
            }
            msg = des.decrypt(cipher);
            System.out.println(msg);
        }
        stdin.close();
        in.close();
        os.close();
    }
    
    /*
     * Bob uses Alice's public key to construct a RSA encrypter. And he uses his
     * own private key to construct a RSA decrypter. Also, he generates DES key
     * for later communication.
     */
    private void prepareKeys() {
        Scanner pub = RSA.scan_file(pubFileName);
        Key key = new Key(pub.nextLine(), 16);
        BigInteger e = new BigInteger(pub.nextLine(), 16);
        encrypter = new RSA(key, e, true);
        Scanner pri = RSA.scan_file(priFileName);
        key = new Key(pri.nextLine(), 16);
        BigInteger d = new BigInteger(pri.nextLine(), 16);
        decrypter = new RSA(key, d, false);
        pub.close();
        pri.close();
        desKey = new Key(64);
        desKey.random();
        des = new DES(desKey);
    }
    
    /*
     * Bob initializes the handshake process. He encrypts DES key and send it to
     * Alice. Once Alice see's it without a problem, Alice will use the DES key to
     * encrypt a string "OK" and send it back. When Bob see the OK message, the
     * handshake is successful.
     */
    private void initHandShake() throws IOException {
        String str = encrypter.encrypt(desKey.toHex());
        os.writeBytes(str + " ");
        String s = in.next();
        System.out.println(s);
        String msg = des.decrypt(s);
        if (!"OK".equals(msg)) {
            System.err.println("RSA Authentication Failed! Exiting...");
            System.exit(1);
        }
    }
}
