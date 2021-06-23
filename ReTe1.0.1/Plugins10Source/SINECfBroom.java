/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 3/5 -06
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect SINE Cf fragments.
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
* Default: 10.
*</PRE>
*/
public class SINECfBroom extends StandardBroom {

// prototype sequence
	private String sineCf = "GGATCCCTGGGTGGCGCACGGTTTGGCGCCTGCCTTTGGCCCAGGGAGCGATCCTGGAGACCCGGGATCGAATCCCACATCGGGCTCCCGGTGCATGGAGCCTGCTTCTCCCTCTGCCTGTGTCTCTGCG";

/**
* Constructor.
*/
	public SINECfBroom() {
		parameters = new Hashtable();
		getDefaults();
		broomPriority = getFloat(BROOMPRIORITYKEY, -1.0f);
		merSize = getInt(MERSIZEKEY, 11);
		merStep = getInt(MERSTEPKEY, 7);
		tolerance = getFloat(TOLERANCEKEY, 10.0f);
	} // end of constructor
	
/**
* @return	Name base for finds in primary strand.
*/
	public String getDirtName() {
		return "SINECf";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CSINECf";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return sineCf;
	} // end of  dirtString() 
	
}
