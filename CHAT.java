import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/*
 * Main class for CHAT part of this assignment.
 *
 * Author: Yi Huang, Youhao Wei
 */
public class CHAT {
    
    private static boolean aliceFlag = false;
    private static boolean bobFlag = false;

    private static boolean hFlag = false;
    private static boolean eFlag = false;
    private static boolean dFlag = false;
    private static boolean pFlag = false;
    private static boolean aFlag = false;
    private static String pub;
    private static String pri;
    private static int port;
    private static String address;
    private static final String HelpMessage = "Usage: \n"
            + "-h\n\tList out all the command line options supported by the progrm.\n\n"
            + "-alice -e <bob's public key file> -d <alice's private key file> -p <alice's port> -a <alice's address>\n\t"
            + "Run this program as Alice, knowing Bob's public key, Alice's private key, address, and port. "
            + "Alice starts up first and wait for Bob to connect.\n\n"
            + "-bob -e <alice's public key file> -d <bob's private key file> -p <alice's port> -a <alice's address>\n\t"
            + "Run this program as Bob, knowing Alice's public key, address, port, and Bob's private key. "
            + "Bob starts up after Alice.\n\n";

    /* 
     * Main function. This function handles command line options and 
     * print error messages when command line options are not appropriate.
     */
    public static void main(String[] args) {
        try {
            parseArguments(args);
            if (hFlag) {
                // display help
                System.out.println(HelpMessage);
                return;
            } else if (aliceFlag) {
                Alice alice = new Alice(address, port, pub, pri);
            } else if (bobFlag) {
                Bob bob = new Bob(address, port, pub, pri);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /*
     * This function parses arguments and fill in variables for further steps
     * to use.
     */
    private static void parseArguments(String[] args) {
        Iterator<String> it = Arrays.asList(args).iterator();
        String tmp;
        while (it.hasNext()) {
            tmp = it.next();
            if ("-h".equals(tmp)) {
                hFlag = true;
            } else if ("-alice".equals(tmp)) {
                aliceFlag = true;
            } else if ("-bob".equals(tmp)) {
                bobFlag = true;
            } else if ("-e".equals(tmp)) {
                eFlag = true;
                if (!it.hasNext()) {
                    exitWithError();
                }
                String inFileName = it.next();
                pub = inFileName;
            } else if ("-d".equals(tmp)) {
                dFlag = true;
                if (!it.hasNext()) {
                    exitWithError();
                }
                String inFileName = it.next();
                pri = inFileName;
            } else if ("-p".equals(tmp)) {
                pFlag = true;
                if (!it.hasNext()) {
                    exitWithError();
                }
                port = Integer.valueOf(it.next());
            } else if ("-a".equals(tmp)) {
                aFlag = true;
                if (!it.hasNext()) {
                    exitWithError();
                }
                address = it.next();
            } else {
                exitWithError();
            }
        }

        // check if multiple flags are set
        int fCount = 0;
        if (aliceFlag) {
            ++fCount;
        }
        if (bobFlag) {
            ++fCount;
        }
        if (hFlag) {
            ++fCount;
        }
        if (fCount != 1) {
            exitWithError();
        }
        if (((!eFlag) || (!dFlag) || (!pFlag) || (!aFlag)) && (!hFlag)) {
            exitWithError();
        }
    }

    /*
     * This function is called when there is an error with command line options.
     */
    private static void exitWithError() {
        System.err.println("Invalid Command Line Options!\n");
        System.err.println(HelpMessage);
        System.exit(1);
    }
}
