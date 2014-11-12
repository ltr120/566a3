import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


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

    public Bob(String addr, int port, String pubFile, String priFile) throws UnknownHostException, IOException {
        alice = new Socket(addr, port);
        in = new Scanner(alice.getInputStream());
        os = new DataOutputStream(alice.getOutputStream());
        this.pubFileName = pubFile;
        this.priFileName = priFile;
        prepareKeys();
        initHandShake();
        startChatting();
    }
    
    private void startChatting() throws IOException {
        Scanner stdin = new Scanner(System.in);
        String msg;
        String cipher;
        while (stdin.hasNextLine()) {
            msg = stdin.nextLine();
            cipher = des.encrypt(msg);
            os.writeBytes(cipher);
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
    
    private void initHandShake() throws IOException {
        System.out.println("DES KEY: " + desKey.toHex());
        String str = encrypter.encrypt(desKey.toHex());
        System.out.println("DES encrypted: " + str);
        os.writeBytes(str + " ");
        String s = in.next();
        System.out.println(s);
        String msg = des.decrypt(s);
        if (!"OK".equals(msg)) {
            System.err.println("RSA Authentication Failed! Exiting...\n");
            System.exit(1);
        }
    }
}
