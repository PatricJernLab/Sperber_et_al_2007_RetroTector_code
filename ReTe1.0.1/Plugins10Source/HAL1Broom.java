/*
* Copyright (©) 2000-2007, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 1/1 -07
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect HAL1 fragments.
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
public class HAL1Broom extends StandardBroom {

// prototype sequence
	private String hAL1 = "gacttccggttaaagatggcggattgaacacacgcatctanttttgctccctcccgaaaccccactaaaatnacagtaaaggaattttttaanaaaggcataaacccacaaggacaaagagaacaggagaggagacaacagcaacaaaattttggaagctggaaagcagatggacaagtggtaactgacttagcagacccgagaaagctgaatcctaagccagcagtggggaaagccgagaancaacccaatttacaccgcagaatccccaaaaggctcaggaattggcggcaccaggtacctctggaagtgggggtgaaggtgaggctaaaaacagggaggattggttgaaagtctgtttaagaagcagttagacccccagattccctcccccactctatngcagccaggcgactgctcctcccccaccctagcagaagactggaggtttattctctggagagggtaaaacagagggtctctggactgggggacaccaggcacagttgagggcagaggggtaccgtactgaaaacagggggattaagtgaaagtttacatactgaatgncgagagacccccagccctcttcccccactcggctcccagaacgctggcagccaggcctataccctccaggcaggagattggaagagtcttctctggggaatctgaccagcccaagaggaaagacctaaagatactgacattaggggttccccaagcaaaacagcccagccagatcaccctacagtgaagcccacagtcgacaagccccacccacgcgcncagagcttccaatcagctttttagtgcctcactcttaaatatgagcagacagccaaggatcaccagacatttgaggaaagcctctaacatgaaagacagagaccaaaacaaacagaaaaangcaacttggaggaaacagaagantatgcagagaggagaagaaaacttcaaaaaaactatcattaatatcctcagagagataagagaagatattgcatccatgaaacaagaacaggatgctataaaaaaggaacattcagagaacaaaaagaagctcttggaaattaaaaatatgatagcagaaatgaaaaactcaatagaagggttggaagataaagttgaggaaatntcccagaaagtagaacaaaaagacaaagagatggaaaataggagagaaaagataaaaaaattagaggaccagtccaggaggtccaatatccgantaataggagttccagaaagagagaacagagaaaangatggaagagaaattatcaaagaaataatncaagaaaatttcccagaactgaaggacatgagtttccagattgaaagggcccaccgagtgcccagcacaatgaatgaaaaaaatagacccacaccaaggcacatcattgtgaaatttcagaacactggggataaagagaagatcctaaaagcttccagagagaaaaaacaggtcacatacaaaggatcaggaatcagaatggcatcggacttctcaacagcaacactggaagctagaagacaatggagcaatgccttcaaaattctgagggaaaatgatttccaacctagaattctatacccagccaaactatcaatcaagtgtgagggtagaataaagacattttcagacatgcaaggtctcaaaaaatttacctcccatgcaccctttctcaggaagctactggaggatgtgctccaccaaaacgagggagtaaaccaagaaagaggaagacatgggatccaggaaacaggggatccaacacaggagagaggtgaagggaattcccaggatgatggtgaagggaagtcccaggatgacagctgtgcancaggcctagagagcaaccagtccagattggagcaggaggacagaaggctccaggagagatntctccaagaaaatgaaactgatagantacctgatgtgtttgaacatattgagaggagatttatacaattggcggagagtttggggatgaattagtgataagtacatagaaaactaagcaaatgaaaagcgagacaattattaactccagggaaaacaaaaagttgtacaagaaaggaaangtaatcatagtacactacatggctcagctgtgaataatatttacatagtcataataatgtaaacactgaatattgatttaaccaaaattatgatataactatattgggaggatggggggatgggaagtgtgtgtgtgntgagggggaggtgtgaaagaagagctaaatcctcatcttccatagtaggaagtcaacagataatgcctaaaattgaaaaatcaagaaatagcaatataagcatgttatttagaaatatggaggtaaataccagaagaaacagctaaaagagttgaaagtggttgcctctggggagggagtgaagatggaagagggnagggatggggactgcttttcgtnataagccttgtagnactatttgactttttaaactatgtgcatgtatnactttgataaaaataaaa";

/**
* Constructor.
*/
	public HAL1Broom() {
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
		return "HAL1";
	} // end of getDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return hAL1;
	} // end of  dirtString() 
	
}
