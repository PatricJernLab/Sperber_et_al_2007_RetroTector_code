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
* Broom to detect P7SL_MD fragments.
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
* Default: 7.
*</PRE>
*/
public class P7SL_MDBroom extends StandardBroom {

// prototype sequence
	private String p7SL_MD = "ggcatggtggcacatacctgtaatccctgctactggggaggctgaggctggtggatcgcttgagttcaggagttctgagctgcagtagggctaaagaccaattgggtgtctacactaagtctggcaccaatatggtgagcccctaggagtggggggccaccaggctgcctaaggaggggtaaactggcccaggtcagaaatggagcaggtcaaagcttctgtgctgatcagtagtgggatcaggcccatgagtggccactgtacttccagcctgggcaagatagggagacccagtctaaaaa";

/**
* Constructor.
*/
	public P7SL_MDBroom() {
		parameters = new Hashtable();
		getDefaults();
		broomPriority = getFloat(BROOMPRIORITYKEY, -1.0f);
		merSize = getInt(MERSIZEKEY, 11);
		merStep = getInt(MERSTEPKEY, 7);
		tolerance = getFloat(TOLERANCEKEY, 7.0f);
		tailMin = getInt(TAILMINKEY, 7);
	} // end of constructor
	
/**
* @return	Name base for finds in primary strand.
*/
	public String getDirtName() {
		return "P7SL_MD";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CP7SL_MD";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return p7SL_MD;
	} // end of  dirtString() 
	
}
