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
 *
 */
public class Srvapp {

	// first 4 bits of char
	final static char firstsubcharMask = (char) 0xF000;
	final static char secondsubcharMask = (char) 0x0F00;
	final static char thirdsubcharMask = (char) 0x00F0;
	final static char fourthsubcharMask = (char) 0x000F;

	final static int portNumber = 8080;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataSet data = readEncryptedDataFromSocket();
//		reorderData(data);
        decryptDataStream(data);
        System.out.println(data.fulldecryptedstream);
	}


    private static DataSet readEncryptedDataFromSocket ()
	{
		char[] firstDataStream = getMessage().toCharArray();
		char[] secondDataStream = getMessage().toCharArray();
		char[] thirdDataStream = getMessage().toCharArray();
		char[] fourthDataStream = getMessage().toCharArray();

        System.out.println(firstDataStream);
        System.out.println(secondDataStream);
        System.out.println(thirdDataStream);
        System.out.println(fourthDataStream);

		System.out.println(firstDataStream.length + "  " + secondDataStream.length + "  "
				+ thirdDataStream.length + "   " + fourthDataStream.length);

        DataSet data = new DataSet(Math.max(firstDataStream.length,
                Math.max(secondDataStream.length,
						Math.max(thirdDataStream.length, fourthDataStream.length))));

		final char indicatorAprime = (char) 0b00;
		final char indicatorBprime = (char) 0b01;
		final char indicatorCprime = (char) 0b10;
		final char indicatorDprime = (char) 0b11;

        char indicatorFirst = extractIndicator(firstDataStream[0]);
        switch (indicatorFirst)
		{
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
        switch (indicatorSecond)
		{
		case indicatorAprime:
            data.aprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
            break;
		case indicatorBprime:
            data.bprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
            ;
            break;
		case indicatorCprime:
            data.cprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
            ;
            break;
		case indicatorDprime:
            data.dprimes = Arrays.copyOfRange(secondDataStream, 1, secondDataStream.length);
            ;
            break;
		}

        char indicatorThird = extractIndicator(thirdDataStream[0]);
        switch (indicatorThird)
		{
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
        switch (indicatorFourth)
		{
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
        System.out.println("a': " + new String(data.aprimes));
        System.out.println("b': " + new String (data.bprimes));
		System.out.println("c': " + new String (data.cprimes));
		System.out.println("d': " + new String (data.dprimes));

        System.arraycopy(data.aprimes, 0, data.fullencryptedstream, 0, data.aprimes.length);
        System.arraycopy(data.bprimes, 0, data.fullencryptedstream, data.aprimes.length, data.bprimes.length);
        System.arraycopy(data.cprimes, 0, data.fullencryptedstream, data.aprimes.length + data.bprimes.length,
                data.cprimes.length);
        System.arraycopy(data.dprimes, 0, data.fullencryptedstream,
                data.aprimes.length + data.bprimes.length + data.cprimes.length, data.dprimes.length);

        System.out.println(new String(data.fullencryptedstream));
        System.out.println(data.fullencryptedstream.length);
        System.out.println(data.fullencryptedstream[data.fullencryptedstream.length - 1]);

		return data;
	}

	static char extractIndicator(char indicatorAndCntr)
	{
		final char maskIndicator = (char) 0xC000;
		char indicator = maskChar(indicatorAndCntr,maskIndicator,Operation.and);
		char shiftedIndicator = shiftBits(indicator,14,Direction.right);
		return shiftedIndicator;
	}

	private static void reorderData (DataSet dataSet)
	{
		//i=index in aprimes
        int i = 0;
        //TODO might run in parallel
		for (int j = 0; (i < dataSet.aprimes.length 
				&& i < dataSet.bprimes.length 
				&& i < dataSet.cprimes.length 
				&& i < dataSet.dprimes.length 
				&& j < dataSet.fullencryptedstream.length); j++)
		{			
			/* reorder:
			 * old: aprimes[i]=aj0 aj1 aj2 aj3 a(j+1)0 a(j+1)1 a(j+1)2 a(j+1)3 ...
			 * new: fullencstream[j]=aj0 aj1 aj2 aj3 bj0 bj1 bj2 bj3 ...
			 * requires extracting the bits, shifting them to the right position and ORing them to the new stream
			 */

			switch (j%4)
			{
                //	whole functions lead to:	dataSet.fullencryptedstream[j]|=(char) ((char) ((dataSet.dprimes[i] & firstsubcharMask)))>>>12;
                case 0:
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.aprimes[i],firstsubcharMask,Operation.and),
								0,Direction.noDirection),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.bprimes[i],firstsubcharMask,Operation.and),
								4,Direction.right),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.cprimes[i],firstsubcharMask,Operation.and),
								8,Direction.right),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.dprimes[i],firstsubcharMask,Operation.and),
								12,Direction.right),
								Operation.or);
				break;
			case 1:
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.aprimes[i],firstsubcharMask,Operation.and),
								4,Direction.left),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.bprimes[i],firstsubcharMask,Operation.and),
								0,Direction.noDirection),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.cprimes[i],firstsubcharMask,Operation.and),
								4,Direction.right),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.dprimes[i],firstsubcharMask,Operation.and),
								8,Direction.right),
								Operation.or);
				break;
			case 2:
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.aprimes[i],firstsubcharMask,Operation.and),
								8,Direction.left),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.bprimes[i],firstsubcharMask,Operation.and),
								4,Direction.left),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.cprimes[i],firstsubcharMask,Operation.and),
								0,Direction.noDirection),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.dprimes[i],firstsubcharMask,Operation.and),
								4,Direction.right),
								Operation.or);
				break;
			case 3:

				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.aprimes[i],firstsubcharMask,Operation.and),
								12,Direction.left),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.bprimes[i],firstsubcharMask,Operation.and),
								8,Direction.left),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.cprimes[i],firstsubcharMask,Operation.and),
								4,Direction.left),
								Operation.or);
				dataSet.fullencryptedstream[j] = maskChar(
						dataSet.fullencryptedstream[j], 
						shiftBits(
								maskChar(dataSet.dprimes[i],firstsubcharMask,Operation.and),
								0,Direction.noDirection),
								Operation.or);
				//iterated through all 4 subchars => next char
				i++;
				break;
			}
		}
        System.out.println("Full enc Stream :" + new String(dataSet.fullencryptedstream));
    }

    private static void decryptDataStream(DataSet dataSet) {
		//TODO run in parallel if possible
		for (int i=0; i< dataSet.fullencryptedstream.length;i++)
		{
            dataSet.fulldecryptedstream[i] = decryptSingleChar(dataSet.fullencryptedstream[i]);
        }
	}

    private static char decryptSingleChar(char encryptedChar) {
        char aprime = shiftBits(
                maskChar(encryptedChar, firstsubcharMask, Operation.and),
                12, Direction.right);
        char bprime = shiftBits(
                maskChar(encryptedChar, secondsubcharMask, Operation.and),
                8, Direction.right);
        char cprime = shiftBits(
                maskChar(encryptedChar, thirdsubcharMask, Operation.and),
                4, Direction.right);
        char dprime = shiftBits(
                maskChar(encryptedChar, fourthsubcharMask, Operation.and),
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
        } catch (Exception e)
		{
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
