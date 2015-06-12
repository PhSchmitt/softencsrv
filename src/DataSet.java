
public class DataSet {

	public char[] aprimes;
	public char[] bprimes;
	public char[] cprimes;
	public char[] dprimes;
	public char[] fullencryptedstream;
	public char[] fulldecryptedstream;

    /**
     * @param arraylength = incoming data length (without leading indicatorAndPktCntr)
     */
    public DataSet(int arraylength) {
        aprimes = new char[arraylength];
        bprimes = new char[arraylength];
        cprimes = new char[arraylength];
        dprimes = new char[arraylength];
        fullencryptedstream = new char[(arraylength) * 4];
        fulldecryptedstream = new char[(arraylength) * 4];
    }

    public DataSet(char[] aprimesToSet, char[] bprimesToSet, char[] cprimesToSet, char[] dprimesToSet) {
        aprimes = aprimesToSet;
        bprimes = bprimesToSet;
        cprimes = cprimesToSet;
        dprimes = dprimesToSet;
        fullencryptedstream = new char[aprimesToSet.length + bprimesToSet.length + cprimesToSet.length + dprimesToSet.length];
        fulldecryptedstream = new char[aprimesToSet.length + bprimesToSet.length + cprimesToSet.length + dprimesToSet.length];
    }

}
