/*
* Copyright (©) 2000-2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Göran Sperber
* @version 14/11 -06
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect DNA2_2_DR fragments.
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
public class DNA2_2_DRBroom extends StandardBroom {

// prototype sequence
	private String dNA2_2_DR = "gcccctttcacacagtgataccggtaaatatccggaaaatttccggaacgactttaccggtatattcaaaaaagcgctgttcacacaggcgaggacgttacggaatttttccggaaaagagcattcacacatccattccaaaataccggtaaattctgacatcattcaccacaaatgagctttaaacggctgcgcttgtgtttgtaaacatttgactaaattacaaactctgtggatggatcaatattgtgaacaacttcaatgaaaacatatcgcaggatcactttcgcatgtcgagatgttcataatatgtgtgtgtgctggcgctcacaggctgtttcacaggcacacgcaaagcttgaaggtaaacaaacaacggcttatcataagcatctcatcgatgattatttacacagttggcattaagaagaacatataaacgtgatctgactaacttctagcagctaaatgtgtctggaaaaatattcaaaggcttttattctcataaaccgcgcggacgtaaatgcgtctgactgttgtgattggctaaagcagacgtctcacgtcagcacgttctagacgtgcacgcgcttattacggcaatcttccttctgcattcacacagcgcagcattccggcaaattgccggtaatgttacaacttctctttccggaaaatagccagaacgaatttaccggtattttcaaaaaggacctgttcacacatacaacctttccggaaaattgccggtaattttccggaaaggtctgtatgtgtgaaaggggc";

/**
* Constructor.
*/
	public DNA2_2_DRBroom() {
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
		return "DNA2_2_DR";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CDNA2_2_DR";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return dNA2_2_DR;
	} // end of  dirtString() 
	
}
