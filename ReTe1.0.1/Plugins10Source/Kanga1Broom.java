/*
* Copyright (?) 2000-2007, G?ran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G?ran Sperber
* @version 1/1 -07
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect Kanga1 fragments.
*<PRE>
*     Parameters:
*
*   BroomPriority
* Tolerance relative to other Brooms
* Default: -1.
*
*   MerSize
* Length of sequence for initial search.
* Default: 11.
*
*   MerStep
* Step by which sequence for initial search is moved.
* Default: 7.
*
*   Tolerance
* Error tolerance in dynamic programming.
* Default: 7.
*
*   TailMinLength
* Shortest acceptable a-tail length.
* Default: -1.
*</PRE>
*/
public class Kanga1Broom extends StandardBroom {

// prototype sequence
	private String kanga1 = "ccatatttcatcgattctaagatgcacattttttcacattttaacatctctgaaatcgggatgcatcttacaatcgatggcatcttacaatcgctgtcggccaggcggcagtcgtgacgtagttgtcattgcctgcacgtgtgcgaacttggtcatagctgttcatattgtcatcacttcaattgagttatgtgcattgttggtactacacgtgttgagtttaattgccatttaaaatgtcttcaaaaagattacactatgattcggcattgaaatgaaaagttattgtgtacacagaaaggcacggaaacagagcagcggggcgtaaatttgatattagtgaagcaaatattcgtcgttggaggaatgaccgcaattccatattttcttgcaaagcaacaaccaagtgctttatgggacctaagaaaggaagatacccacaagtagatgaagctgtgttacgttttgttactgagatacgtgcaaaaggattgcctatcacacgccaagcaatgcaactgaaggcaggagaaattgccaaatccctcggaatagatgaaagaaatttcaaagcaatgagaggctggtgtgaccgattcatgcgtcgtgcaggactatcgttaaggcatcaaacatcaatttgtcagaaacttcctgctgactttgaacagaagctgcttaacttccagcgacatgtgattcaattgaggaaaaaacgaaactatgagtttagtcaaataggaaatgctgatgaaaccncggtgttcttcgacatgcctcaaaattatactgtcaatgctaaaggtgctaaagagatcaagatcatgagcacaggttatgaaaagcagcatatcactgtgatgctatgcataattgccgatggccaaaagttgtcgccatatttaattttaaactacaaaataattcctaagaatgaaatcttccccaaagatgttactgtgcttnccantaaacatggagatatagacgtcagntgagctgatgaaggagtggctaaagtcatctggaattggatgtccaggagccctacgtaacccaccaagtgtgttggttcttgatgcatttcatggacgtgtatctgaacagttaaagaatangttcactgaaaagtaanacaagttggtggttgatccatnaactgcaacccctcaanattccagtcagcaaaccatttaaggaccatttgaggaaggaatatgagtcctggttgttgtctgaaaaccttccgttgacaccttctggtaagatcaagaaagcgccagcatcaaaacttgcagaatgggtgtcagcggcttggaagaaaatcccggagacaatagtggagcactcttttaactcctaggaaccaaatggttgtgggnaagggagggagaaggcagtgaatcagacatgtttagcaacattcccgaagcgggagtcaaaagtaggctagctctacataattgcaaagccggctcaagagcccagcaccaatggctctcagttcttttatggattcttttaagaaatgctgcatcaccaacgctcttgatggcacagaggatgatattgtgtggaaaaacacggacatcgatgactctgagtcgaaaagtgattcagaagagttggactctgaatgtgaagaagttttaggaataccttaaccaatttatttcgcttatattttcctttttatgtatgcacaagagtgatatatgataaaaatctgtgtctaaataagtctaaaagagctctttcaataagtataaaataaaaattctaatgataaggaaagcattgtgtcatagtttaattggcagcgttttttctttcttagtggtacataaaataatggtgcgtcttacaatcgatggcatcttagattcgatgaaatacgg";

/**
* Constructor.
*/
	public Kanga1Broom() {
		parameters = new Hashtable();
		getDefaults();
		broomPriority = getFloat(BROOMPRIORITYKEY, -1.0f);
		merSize = getInt(MERSIZEKEY, 11);
		merStep = getInt(MERSTEPKEY, 7);
		tolerance = getFloat(TOLERANCEKEY, 7.0f);
		tailMin = getInt(TAILMINKEY, -1);
	} // end of constructor
	
/**
* @return	Name base for finds in primary strand.
*/
	public String getDirtName() {
		return "Kanga1";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CKanga1";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return kanga1;
	} // end of  dirtString() 
	
}
