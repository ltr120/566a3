import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Alice {
    
    ServerSocket alice;
    Socket bob;
    Scanner in;
    DataOutputStream os;
    RSA encrypter;
    RSA decrypter;
    Key desKey;
    DES des;
    String pubFileName;
    String priFileName;
    private static final String OK = "OK";

    public Alice(String addr, int port, String pubFileName, String priFileName) throws UnknownHostException, IOException {
        alice = new ServerSocket(port, 50, InetAddress.getByName(addr));
        bob = alice.accept();
        bob.setTcpNoDelay(true);
        in = new Scanner(bob.getInputStream());
        os = new DataOutputStream(bob.getOutputStream());
        this.pubFileName = pubFileName;
        this.priFileName = priFileName;
        prepareKeys();
        acceptHandShake();
        startChatting();
    }
    
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
    }
    
    private void acceptHandShake() throws IOException {
        String cipher;
        if (in.hasNext()) {
            cipher = in.next();
            System.out.println("cipher" + cipher);
            desKey = new Key(decrypter.decrypt(cipher), 16);
            des = new DES(desKey);
            System.out.println("DES KEY: " + desKey.toHex());
            os.writeBytes(des.encrypt("OK") + " ");
        }
    }
    
    private void startChatting() throws IOException {
        Scanner stdin = new Scanner(System.in);
        String msg;
        String cipher;
        while (in.hasNext()) {
            cipher = in.next();
            msg = des.decrypt(cipher);
            System.out.println(msg);
            if (stdin.hasNextLine()) {
                msg = stdin.nextLine();
            } else {
                break;
            }
            cipher = des.encrypt(msg);
            os.writeBytes(cipher + " ");
            os.flush();
        }
        stdin.close();
        in.close();
        os.close();
    }
}
