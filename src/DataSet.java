
public class DataSet {

	public char[] aprimes;
	public char[] bprimes;
	public char[] cprimes;
	public char[] dprimes;
	public char[] fullencryptedstream;
	public char[] fulldecryptedstream;

    /**
     * @param arraylength = incoming data length (with leading indicatorAndPktCntr)
     *                    all single arrays within DataSet are of length arraylength-1
     */
    public DataSet(int arraylength) {
        aprimes = new char[arraylength - 1];
        bprimes = new char[arraylength - 1];
        cprimes = new char[arraylength - 1];
        dprimes = new char[arraylength-1];
        fullencryptedstream = new char [(arraylength-1)*4];
		fulldecryptedstream = new char [(arraylength-1)*4];
	}

}
