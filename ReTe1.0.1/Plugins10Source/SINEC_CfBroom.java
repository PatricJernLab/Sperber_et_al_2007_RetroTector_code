/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 28/6 -06
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect SINEC_Cf fragments.
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
public class SINEC_CfBroom extends StandardBroom {

// prototype sequence
	private String sineC_Cf = "GGGGTGCCTGGGTGGCTCAGCGGTTTAGCGCCTGCCTTTGGCCCAGGGCGTGATCCTGGAGACCCGGGATCGAGTCCCACATCGGGCTCCCTGCATGGAGCCTGCTTCTCCCTCTGCCTGTGTCTCTGCCTCTCTCTCTCTCTCTGTCTCTCATGAATAAA";

/**
* Constructor.
*/
	public SINEC_CfBroom() {
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
		return "SINEC_Cf";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CSINEC_Cf";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return sineC_Cf;
	} // end of  dirtString() 
	
}
