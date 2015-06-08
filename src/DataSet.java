
public class DataSet {

	public char[] aprimes;
	public char[] bprimes;
	public char[] cprimes;
	public char[] dprimes;
	public char[] fullencryptedstream;
	public char[] fulldecryptedstream;
	
	public DataSet(int arraylength)
	{	
		aprimes = new char[arraylength];
		bprimes = new char[arraylength];
		cprimes = new char[arraylength];
		dprimes = new char[arraylength];
		fullencryptedstream = new char [(arraylength-1)*4];
		fulldecryptedstream = new char [(arraylength-1)*4];
	}

}
