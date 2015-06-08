import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;

/**
 * 
 */

/**
 * @author philipp
 *
 */
public class Srvapp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataSet data = readEncryptedDataFromSocket();
		reorderData(data);
		decryptData(data);
		System.out.println(data.fulldecryptedstream);		
	}
	private static DataSet readEncryptedDataFromSocket ()
	{
		char[] firstDataStream = getMessage().toCharArray();
		char[] secondDataStream = getMessage().toCharArray();
		char[] thirdDataStream = getMessage().toCharArray();
		char[] fourthDataStream = getMessage().toCharArray();
		
		System.out.println(firstDataStream.length + "  " + secondDataStream.length + "  "
				+ thirdDataStream.length + "   " + fourthDataStream.length);
		System.out.println(firstDataStream.toString());
		System.out.println(secondDataStream.toString());
		System.out.println(thirdDataStream.toString());
		System.out.println(fourthDataStream.toString());
		System.out.println("begin select");
		
		DataSet data = new DataSet(Math.max(firstDataStream.length,
				Math.max(secondDataStream.length,
						Math.max(thirdDataStream.length, fourthDataStream.length))));
		
		char indicatorFirst = (char) (firstDataStream[0]);
		indicatorFirst &= 0xC000;
		indicatorFirst >>>=14;
		switch (indicatorFirst)
		{
		case 0b00:
			data.aprimes = firstDataStream;
			break;
		case 0b01:
			data.bprimes = firstDataStream;
			break;
		case 0b10:
			data.cprimes = firstDataStream;
			break;
		case 0b11:
			data.dprimes = firstDataStream;
			break;
		}
		
		char indicatorSecond = (char) (secondDataStream[0]);
		indicatorSecond &= 0xC000;
		indicatorSecond >>>=14;
		switch (indicatorSecond)
		{
		case 0b00:
			data.aprimes = secondDataStream;
			break;
		case 0b01:
			data.bprimes = secondDataStream;
			break;
		case 0b10:
			data.cprimes = secondDataStream;
			break;
		case 0b11:
			data.dprimes = secondDataStream;
			break;
		}
		
		char indicatorThird = (char) (thirdDataStream[0]);
		indicatorThird &= 0xC000;
		indicatorThird >>>=14;
		switch (indicatorThird)
		{
		case 0b00:
			data.aprimes = thirdDataStream;
			break;
		case 0b01:
			data.bprimes = thirdDataStream;
			break;
		case 0b10:
			data.cprimes = thirdDataStream;
			break;
		case 0b11:
			data.dprimes = thirdDataStream;
			break;
		}
		
		char indicatorFourth = (char) (fourthDataStream[0]);
		indicatorFourth &= 0xC000;
		indicatorFourth >>>=14;
		switch (indicatorFourth)
		{
		case 0b00:
			data.aprimes = fourthDataStream;
			break;
		case 0b01:
			data.bprimes = fourthDataStream;
			break;
		case 0b10:
			data.cprimes = fourthDataStream;
			break;
		case 0b11:
			data.dprimes = fourthDataStream;
			break;
		}
		System.out.println(data.aprimes.toString());
		System.out.println(data.bprimes.toString());
		System.out.println(data.cprimes.toString());
		System.out.println(data.dprimes.toString());
		return data;
	}
	
	private static void reorderData (DataSet dataSet)
	{
		//i=1 because aprimes[0] is indicator+pkt_nr and should not be considered here
		//i=index in aprimes
		int i=1;
		//TODO might run in parallel
		for (int j = 0; (i < dataSet.aprimes.length 
				&& i < dataSet.bprimes.length 
				&& i < dataSet.cprimes.length 
				&& i < dataSet.dprimes.length 
				&& j < dataSet.fullencryptedstream.length); j++)
		{
	//		System.out.println("i: "+i + "j: " + j);
			switch (j%4)
			{
			case 0:
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.aprimes[i] & 0xF000));
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.bprimes[i] & 0xF000)>>>4);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.cprimes[i] & 0xF000)>>>8);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.dprimes[i] & 0xF000)>>>12);
				break;
			case 1:
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.aprimes[i] & 0x0F00)<<4);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.bprimes[i] & 0x0F00));
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.cprimes[i] & 0x0F00)>>>4);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.dprimes[i] & 0x0F00)>>>8);
				break;
			case 2:
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.aprimes[i] & 0x00F0)<<8);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.bprimes[i] & 0x00F0)<<4);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.cprimes[i] & 0x00F0));
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.dprimes[i] & 0x00F0)>>>4);
				break;
			case 3:
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.aprimes[i] & 0x000F)<<12);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.bprimes[i] & 0x000F)<<8);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.cprimes[i] & 0x000F)<<4);
				dataSet.fullencryptedstream[j]|=(char) ((dataSet.dprimes[i] & 0x000F));
				i++;
				break;
			}
		}
	//	System.out.println(dataSet.fullencryptedstream.toString());
	}
	
	private static void decryptData (DataSet dataSet)
	{
		//TODO run in parallel if possible
		for (int i=0; i< dataSet.fullencryptedstream.length;i++)
		{
			char aprime = (char) ((dataSet.fullencryptedstream[i] & 0xF000)>>>12);
			char bprime = (char) ((dataSet.fullencryptedstream[i] & 0x0F00)>>>8);
			char cprime = (char) ((dataSet.fullencryptedstream[i] & 0x00F0)>>>4);
			char dprime = (char) ((dataSet.fullencryptedstream[i] & 0x000F));
			char a = (char) (bprime ^ dprime);
			char b = (char) (aprime ^ bprime ^ cprime ^ dprime);
			char c = (char) (aprime ^ bprime ^ dprime);
			char d = (char) (aprime ^ cprime ^ dprime);
			dataSet.fulldecryptedstream[i] = (char) 0;
			dataSet.fulldecryptedstream[i] |= (char) (a <<12);
			dataSet.fulldecryptedstream[i] |= (char) (b <<8);
			dataSet.fulldecryptedstream[i] |= (char) (c <<4);
			dataSet.fulldecryptedstream[i] |= (char) (d);
		//	System.out.println("Cleartext-Data: "+ dataSet.fulldecryptedstream[i]);	
		}
	}

	private final static int portNumber = 8080;
	
	private static String getMessage ()
	{
		try ( 
			    ServerSocket serverSocket = new ServerSocket(portNumber);
			    Socket clientSocket = serverSocket.accept();
			    BufferedReader in = new BufferedReader(
			        new InputStreamReader(clientSocket.getInputStream()));
			)
			{
//			String inputLine = new String();
			CharBuffer cb;
			//Bytereader oder sowas
            while (in.read(tmpinput) != -1) {
		       
		    }
            System.out.println("Data from port: "+tmpinput);	
            return tmpinput.toString();

			}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error in creating socket");
			return new char[0].toString();
		}
		
		
	}

}
