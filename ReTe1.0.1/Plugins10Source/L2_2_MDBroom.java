/*
* Copyright (©) 2000-2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Göran Sperber
* @version 26/10 -06
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect L2_2_MD fragments.
*<PRE>
*     Parameters:
*
*   BroomPriority
* Tolerance relative to other Brooms
* Default: -1.
*
*   MerSize
* Length of sequence for initial search.
* Default: 17.
*
*   MerStep
* Step by which sequence for initial search is moved.
* Default: 23.
*
*   Tolerance
* Error tolerance in dynamic programming.
* Default: 10.
*
*   TailMinLength
* Shortest acceptable a-tail length.
* Default: -1.
*</PRE>
*/
public class L2_2_MDBroom extends StandardBroom {

// prototype sequence
	private String l2_2_MD = "taatttttatagtttatgaaatctcgaacctcctggcactttccagtactggctgcaccttcactcattctcctcagctcactggttgaggtgggagagttggaatactccttgctccccattgccacttccaggttcttctccttccatcactcagtaacctctcttcctttgaggttcatgctattcatatctaccacccaatcaaaatcctggtagctgttgtctacagacccccaggtcactcccttccttcctcaatgagtttaatacctggctcacaatttttctctcctccccaactcctgccctcatactaggggacttcaacatacatattgattctccctcaaataccctaaccactcagttcctcaacctactcacttcccatgagctactcctccaccccacctcagccacacacaaagatggtcatacccttgatcttgccatcacccacaaatgtaccacctctatgttcaagaattctgaaatccccttatctgaccataatctattggcttttcacctctccctctgccttcccttacaaaaccctactcttcatccacactatgacctccaatcccttgacccctcaattctctcccaggccatctcccctgcactagccactcttttctccccatcttgactccttggtgaaccaattcaactctacactgtcctcctctcttgaatccctagcccccttatcatatcaccaattatgcccagccaagcctcagccttggatcactcccaccatttatcaccttctcctacacatatgctgctgaatgaaggtggagaaaatcatacaactattctgactgggtccactacaaatttatgttacacaacctcaactgggccctcactgctgctaggcaatcctactatacctcccttatcaactcactatcccactctccacagtggctcttccaaaccttttcatccctcctcaaacctcccatggctctccctccccccactctctcagctgagaaccttgcctcatattttacagaaaaaattgaggccatttaccatgaactccctcttctcccctcttcctcatctcctatcactcagatgccttctgccactatctccttcacccctgtctcacatgatgaagtggccttactccttaccaaggctaacccctctacttgttcaagtgatcccattccatcctatctcctccaacagattgccccctctgtcatccccactctttcacttattttcaatctctccctgtctactggctcattccctactgcctacaaacatgcccatgtctcccccatcctgaaaaaaccctcacttgatccttccatccctgctatcatcctatatctcttctgccctttgtagctaaactccttgaaaaggccatctacaataggtgcctccactttctctcctctcactctcttcttaaccccttacaatctggcttctgaccttatcattccactgaaactgctctctccaaagttactaatgatctcttagttgccaaatccaatggccttttctcaatcctcattctccttgacctctctgcagcctttgacactgttgatcactctctttcttctctctaggtttttaggacaccactctctcctggttctcctcctacctatctgaccactccttctctgtctcctttgctggatcctcttccagatcacgccctctaaccataggtgtccctcagagttctgtcctgggccctcttctcttctccctctatactacttcacttggtgatctcatcagctcccatggatttaattaccatctctatgctgatgattctcaaatctacctatcttgccccaactctctgctgacctccagtctcacatctccaactgcctttcagacatcttaaactcaatatgtccaaaacagaactcattatctttccccctaaaccctcccctctcctaccttccctattactgtagagggcaacacatcctcccagtccctcaggctcacaacctagagtcatcctggattcctcactatctctcatcctccatatccaatctgttgccaaggcctgttgatttcacctttgcaacatctcttgaatatacccccttctctcctctgacactgccaccactctagtgcaggccctcatcacctcaccctggattattgcaatagcctctggtgggtctgcctgcctcaagtctctccccactccaatccatcctccattcagccactaaagtgattttcctaaagcacagtctgaccatgtcacctcataactccagtggcttcctatctctaaaaatcctctgtttggcattcaaagcccttcataacctagccccctcctacctttccagtcttcttaacttactccatactctttatccagtgacactggcctcctggctgttccatgaacaagaactccatctctgctcggattttctctggcttcccatgcctgaattctccctcctcactctactctctttaagtcccaataaaatatttaagaaccttcaacctttaattcatgctttcttattcatttttatattatt";

/**
* Constructor.
*/
	public L2_2_MDBroom() {
		parameters = new Hashtable();
		getDefaults();
		broomPriority = getFloat(BROOMPRIORITYKEY, -1.0f);
		merSize = getInt(MERSIZEKEY, 17);
		merStep = getInt(MERSTEPKEY, 23);
		tolerance = getFloat(TOLERANCEKEY, 10.0f);
		tailMin = getInt(TAILMINKEY, -1);
	} // end of constructor
	
/**
* @return	Name base for finds in primary strand.
*/
	public String getDirtName() {
		return "L2_2_MD";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CL2_2_MD";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return l2_2_MD;
	} // end of  dirtString() 
	
}
