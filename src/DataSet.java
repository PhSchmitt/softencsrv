
public class DataSet {

	public char[] aprimes;
	public char[] bprimes;
	public char[] cprimes;
	public char[] dprimes;
	public char[] fullencryptedstream;
	public char[] fulldecryptedstream;

    public DataSet(char[] aprimesToSet, char[] bprimesToSet, char[] cprimesToSet, char[] dprimesToSet) {
        aprimes = aprimesToSet;
        bprimes = bprimesToSet;
        cprimes = cprimesToSet;
        dprimes = dprimesToSet;
        fullencryptedstream = new char[aprimesToSet.length + bprimesToSet.length + cprimesToSet.length + dprimesToSet.length];
        fulldecryptedstream = new char[aprimesToSet.length + bprimesToSet.length + cprimesToSet.length + dprimesToSet.length];
    }

}
