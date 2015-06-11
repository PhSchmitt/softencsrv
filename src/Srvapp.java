import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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

    /**
     * @param args
     */
    public static void main(String[] args) {
        DataSet data = readEncryptedDataFromSocket();
        reorderData(data);
        decryptDataStream(data);
        System.out.println(data.fulldecryptedstream);
    }


    private static DataSet readEncryptedDataFromSocket() {
        char[] firstDataStream = getMessage().toCharArray();
        char[] secondDataStream = getMessage().toCharArray();
        char[] thirdDataStream = getMessage().toCharArray();
        char[] fourthDataStream = getMessage().toCharArray();

        DataSet data = new DataSet(Math.max(firstDataStream.length,
                Math.max(secondDataStream.length,
                        Math.max(thirdDataStream.length, fourthDataStream.length))));

        final char indicatorAprime = (char) 0b00;
        final char indicatorBprime = (char) 0b01;
        final char indicatorCprime = (char) 0b10;
        final char indicatorDprime = (char) 0b11;

        char indicatorFirst = extractIndicator(firstDataStream[0]);
        switch (indicatorFirst) {
            case indicatorAprime:
                data.aprimes = Arrays.copyOfRange(firstDataStream, 1, firstDataStream.length);
                break;
            case indicatorBprime:
                data.bprimes = Arrays.copyOfRange(firstDataStream, 1, firstDataStream.length);
                break;
            case indicatorCprime:
                data.cprimes = Arrays.copyOfRange(firstDataStream, 1, firstDataStream.length);
                break;
            case indicatorDprime:
                data.dprimes = Arrays.copyOfRange(firstDataStream, 1, firstDataStream.length);
                break;
        }

        char indicatorSecond = extractIndicator(secondDataStream[0]);
        switch (indicatorSecond) {
            case indicatorAprime:
                data.aprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
                break;
            case indicatorBprime:
                data.bprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
                break;
            case indicatorCprime:
                data.cprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
                break;
            case indicatorDprime:
                data.dprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
                break;
        }

        char indicatorThird = extractIndicator(thirdDataStream[0]);
        switch (indicatorThird) {
            case indicatorAprime:
                data.aprimes = Arrays.copyOfRange(thirdDataStream, 1, thirdDataStream.length);
                break;
            case indicatorBprime:
                data.bprimes = Arrays.copyOfRange(thirdDataStream, 1, thirdDataStream.length);
                break;
            case indicatorCprime:
                data.cprimes = Arrays.copyOfRange(thirdDataStream, 1, thirdDataStream.length);
                break;
            case indicatorDprime:
                data.dprimes = Arrays.copyOfRange(thirdDataStream, 1, thirdDataStream.length);
                break;
        }

        char indicatorFourth = extractIndicator(fourthDataStream[0]);
        switch (indicatorFourth) {
            case indicatorAprime:
                data.aprimes = Arrays.copyOfRange(fourthDataStream, 1, fourthDataStream.length);
                break;
            case indicatorBprime:
                data.bprimes = Arrays.copyOfRange(fourthDataStream, 1, fourthDataStream.length);
                break;
            case indicatorCprime:
                data.cprimes = Arrays.copyOfRange(fourthDataStream, 1, fourthDataStream.length);
                break;
            case indicatorDprime:
                data.dprimes = Arrays.copyOfRange(fourthDataStream, 1, fourthDataStream.length);
                break;
        }
        return data;
    }

    static char extractIndicator(char indicatorAndCntr) {
        final char maskIndicator = (char) 0xC000;
        char indicator = maskChar(indicatorAndCntr, maskIndicator, Operation.and);
        char shiftedIndicator = shiftBits(indicator, 14, Direction.right);
        return shiftedIndicator;
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
            System.out.println("dprimes: " + Integer.toBinaryString(dataSet.dprimes[i]));
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
                                    8, Direction.right),
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

                    System.out.println("fullenc 3: " + Integer.toBinaryString(dataSet.fullencryptedstream[j]));
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
        System.out.println("Cleartext-Data: " + decryptedChar);

        return decryptedChar;
    }

    private static String getMessage() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine = new String();
            String inputString = new String();
            while ((inputLine = in.readLine()) != null) {
                inputString += inputLine;
            }
            clientSocket.close();
            serverSocket.close();
            System.out.println(inputString);
            return inputString;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in creating socket");
            return new String();
        }
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
