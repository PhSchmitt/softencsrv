import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */

/**
 * @author philipp
 */
public class Srvapp {

    // first 4 bits of char
    final static char firstsubcharNullMask = (char) 0x0FFF;
    final static char secondsubcharNullMask = (char) 0xF0FF;
    final static char thirdsubcharNullMask = (char) 0xFF0F;
    final static char fourthsubcharNullMask = (char) 0xFFF0;
    final static char firstsubcharOnlyMask = (char) 0xF000;
    final static char secondsubcharOnlyMask = (char) 0x0F00;
    final static char thirdsubcharOnlyMask = (char) 0x00F0;
    final static char fourthsubcharOnlyMask = (char) 0x000F;

    final static int portNumber = 8080;

    public static void main(String[] args) {
        DataSet data = readEncryptedDataFromSocket();
        reorderData(data);
        decryptDataStream(data);
        System.out.println(data.fulldecryptedstream);
    }


    private static DataSet readEncryptedDataFromSocket() {
        List<String> wholeDataStream = getMessage();
        System.out.println(wholeDataStream.size());
        List<String> aprimes = new ArrayList<>();
        List<String> bprimes = new ArrayList<>();
        List<String> cprimes = new ArrayList<>();
        List<String> dprimes = new ArrayList<>();
        final char indicatorAprime = (char) 0b00;
        final char indicatorBprime = (char) 0b01;
        final char indicatorCprime = (char) 0b10;
        final char indicatorDprime = (char) 0b11;

        for (String s : wholeDataStream) {
            char indicator = extractIndicator(s.toCharArray()[0]);
            switch (indicator) {
                case indicatorAprime:
                    aprimes.add(s);
                    break;
                case indicatorBprime:
                    bprimes.add(s);
                    break;
                case indicatorCprime:
                    cprimes.add(s);
                    break;
                case indicatorDprime:
                    dprimes.add(s);
                    break;
            }
        }

        return new DataSet(streamToCharArray(aprimes), streamToCharArray(bprimes),
                streamToCharArray(cprimes), streamToCharArray(dprimes));
    }

    private static char[] streamToCharArray(List<String> stream) {
        int nextSubstream = 0;
        String result = "";
        Boolean lastSentStringAdded = false;
        Boolean lastElementAdded = false;
        while (!lastSentStringAdded || !lastElementAdded) {
            if (!stream.isEmpty()) {
                for (int i = 0; i < stream.size(); i++) {
                    String s = stream.get(i);
                    if ((s.toCharArray()[0] & 0x0FFF) == nextSubstream) {
                        result += s.substring(1);
                        if (0 != maskChar(s.toCharArray()[0], (char) 0x1000, Operation.and)) {
                            lastSentStringAdded = true;
                        }
                        nextSubstream++;
                        stream.remove(s);
                    }
                    if (stream.isEmpty()) {
                        lastElementAdded = true;
                    }
                }
            }
        }
        return result.toCharArray();
    }

    static char extractIndicator(char indicatorAndCntr) {
        final char maskIndicator = (char) 0xC000;
        char indicator = maskChar(indicatorAndCntr, maskIndicator, Operation.and);
        return shiftBits(indicator, 14, Direction.right);
    }

    private static void reorderData(DataSet dataSet) {
        Arrays.fill(dataSet.fullencryptedstream, (char) 0);
        //i=index in aprimes
        int i = 0;
        //TODO might run in parallel
        for (int j = 0; (i < dataSet.aprimes.length
                && i < dataSet.bprimes.length
                && i < dataSet.cprimes.length
                && i < dataSet.dprimes.length
                && j < dataSet.fullencryptedstream.length); j++) {
            /* reorder:
             * old: aprimes[i]=aj0 aj1 aj2 aj3 a(j+1)0 a(j+1)1 a(j+1)2 a(j+1)3 ...
			 * new: fullencstream[j]=aj0 aj1 aj2 aj3 bj0 bj1 bj2 bj3 ...
			 * requires extracting the bits, shifting them to the right position and ORing them to the new stream
			 */
            switch (j % 4) {
                case 0:
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], firstsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.aprimes[i], firstsubcharOnlyMask, Operation.and),
                                    0, Direction.noDirection),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], secondsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.bprimes[i], firstsubcharOnlyMask, Operation.and),
                                    4, Direction.right),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], thirdsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.cprimes[i], firstsubcharOnlyMask, Operation.and),
                                    8, Direction.right),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], fourthsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.dprimes[i], firstsubcharOnlyMask, Operation.and),
                                    12, Direction.right),
                            Operation.or);
                    break;
                case 1:
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], firstsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.aprimes[i], secondsubcharOnlyMask, Operation.and),
                                    4, Direction.left),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], secondsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.bprimes[i], secondsubcharOnlyMask, Operation.and),
                                    0, Direction.noDirection),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], thirdsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.cprimes[i], secondsubcharOnlyMask, Operation.and),
                                    4, Direction.right),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], fourthsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.dprimes[i], secondsubcharOnlyMask, Operation.and),
                                    8, Direction.right),
                            Operation.or);
                    break;
                case 2:
                    dataSet.fullencryptedstream[j] = maskChar(
                        maskChar(dataSet.fullencryptedstream[j], firstsubcharNullMask, Operation.and),
                        shiftBits(
                                maskChar(dataSet.aprimes[i], thirdsubcharOnlyMask, Operation.and),
                                8, Direction.left),
                        Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], secondsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.bprimes[i], thirdsubcharOnlyMask, Operation.and),
                                    4, Direction.left),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], thirdsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.cprimes[i], thirdsubcharOnlyMask, Operation.and),
                                    0, Direction.noDirection),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], fourthsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.dprimes[i], thirdsubcharOnlyMask, Operation.and),
                                    4, Direction.right),
                            Operation.or);
                    break;
                case 3:
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], firstsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.aprimes[i], fourthsubcharOnlyMask, Operation.and),
                                    12, Direction.left),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], secondsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.bprimes[i], fourthsubcharOnlyMask, Operation.and),
                                    8, Direction.left),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], thirdsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.cprimes[i], fourthsubcharOnlyMask, Operation.and),
                                    4, Direction.left),
                            Operation.or);
                    dataSet.fullencryptedstream[j] = maskChar(
                            maskChar(dataSet.fullencryptedstream[j], fourthsubcharNullMask, Operation.and),
                            shiftBits(
                                    maskChar(dataSet.dprimes[i], fourthsubcharOnlyMask, Operation.and),
                                    0, Direction.noDirection),
                            Operation.or);
                    //iterated through all 4 subchars => next char
                    i++;
                    break;
            }
        }
    }

    private static void decryptDataStream(DataSet dataSet) {
        Arrays.fill(dataSet.fulldecryptedstream, (char) 0);
        //TODO run in parallel if possible
        for (int i = 0; i < dataSet.fullencryptedstream.length; i++) {
            dataSet.fulldecryptedstream[i] = decryptSingleChar(dataSet.fullencryptedstream[i]);
        }
    }

    private static char decryptSingleChar(char encryptedChar) {
        char aprime = shiftBits(
                maskChar(encryptedChar, firstsubcharOnlyMask, Operation.and),
                12, Direction.right);
        char bprime = shiftBits(
                maskChar(encryptedChar, secondsubcharOnlyMask, Operation.and),
                8, Direction.right);
        char cprime = shiftBits(
                maskChar(encryptedChar, thirdsubcharOnlyMask, Operation.and),
                4, Direction.right);
        char dprime = shiftBits(
                maskChar(encryptedChar, fourthsubcharOnlyMask, Operation.and),
                0, Direction.noDirection);

        //XOR-decryption according to Haniotakis, Tragoudas and Kalapodas
        char a = (char) (bprime ^ dprime);
        char b = (char) (aprime ^ bprime ^ cprime ^ dprime);
        char c = (char) (aprime ^ bprime ^ dprime);
        char d = (char) (aprime ^ cprime ^ dprime);
        char decryptedChar = 0;
        decryptedChar = maskChar(
                decryptedChar, shiftBits(a, 12, Direction.left), Operation.or);
        decryptedChar = maskChar(
                decryptedChar, shiftBits(b, 8, Direction.left), Operation.or);
        decryptedChar = maskChar(
                decryptedChar, shiftBits(c, 4, Direction.left), Operation.or);
        decryptedChar = maskChar(
                decryptedChar, shiftBits(d, 0, Direction.noDirection), Operation.or);
        return decryptedChar;
    }

    private static List<String> getMessage() {
        try {
            List<String> result = new ArrayList<>();
            Boolean lastAreceived = false;
            Boolean lastBreceived = false;
            Boolean lastCreceived = false;
            Boolean lastDreceived = false;

            while (!(lastAreceived && lastBreceived && lastCreceived && lastDreceived)) {
                String inputString = readSingleMessageFromSocket();
                result.add(inputString);
                switch ((inputString.toCharArray()[0] & 0xF000) >>> 12) {
                    case 0b0010:
//                        System.out.println("a following");
                        break;
                    case 0b0011:
//                        System.out.println("a last");
                        lastAreceived = true;
                        break;
                    case 0b0110:
//                        System.out.println("b following");
                        break;
                    case 0b0111:
//                        System.out.println("b last");
                        lastBreceived = true;
                        break;
                    case 0b1010:
//                        System.out.println("c following");
                        break;
                    case 0b1011:
//                        System.out.println("c last");
                        lastCreceived = true;
                        break;
                    case 0b1110:
//                        System.out.println("d following");
                        break;
                    case 0b1111:
//                        System.out.println("d last");
                        lastDreceived = true;
                        break;
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in creating socket");
            return new ArrayList<>();
        }
    }

    private static String readSingleMessageFromSocket() throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Socket clientSocket = serverSocket.accept();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        String inputLine;
        String inputString = "";

        while ((inputLine = in.readLine()) != null) {
            inputString += inputLine;
        }
        clientSocket.close();
        serverSocket.close();
        return inputString;
    }

    public static char shiftBits(char toShift, int shiftcount, Direction direction) {
        char tmp = toShift;
        switch (direction) {
            case left:
                tmp <<= shiftcount;
                break;
            case right:
                tmp >>>= shiftcount;
                break;
            case noDirection:
                break;
        }
        return tmp;
    }

    public static char maskChar(char toMask, char mask, Operation operation) {
        char tmp = toMask;
        switch (operation) {
            case and:
                tmp &= mask;
                break;
            case or:
                tmp |= mask;
                break;
            case xor:
                tmp ^= mask;
                break;
            case noOperation:
                break;
        }
        return tmp;
    }

    public enum Direction {
        left,
        right,
        noDirection
    }

    public enum Operation {
        and,
        or,
        xor,
        noOperation
    }

}
