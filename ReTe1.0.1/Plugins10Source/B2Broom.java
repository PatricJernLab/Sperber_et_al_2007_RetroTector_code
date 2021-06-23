/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 29/9 -06
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect B2 fragments.
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
* Default: 13.
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
public class B2Broom extends StandardBroom {

// prototype sequence
	private String b2 = "gggctggagagatggctcagtggttaagagcacctgactgctcttccagaggtcctgagttcaattcccagcaaccacatggtggctcacaaccatctgtaatgagatctgatgccctcttctggtgtgtctgaagacagctacagtgtacttacatataataaataaataaataaataaatcttaaaaaaaaaaaaaagaaagaaaaa";

/**
* Constructor.
*/
	public B2Broom() {
		parameters = new Hashtable();
		getDefaults();
		broomPriority = getFloat(BROOMPRIORITYKEY, -1.0f);
		merSize = getInt(MERSIZEKEY, 11);
		merStep = getInt(MERSTEPKEY, 13);
		tolerance = getFloat(TOLERANCEKEY, 7.0f);
		tailMin = getInt(TAILMINKEY, -1);
	} // end of constructor
	
/**
* @return	Name base for finds in primary strand.
*/
	public String getDirtName() {
		return "B2";
	} // end of getDirtName()
	
/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "CB2";
	} // end of getCDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return b2;
	} // end of  dirtString() 
	
}
