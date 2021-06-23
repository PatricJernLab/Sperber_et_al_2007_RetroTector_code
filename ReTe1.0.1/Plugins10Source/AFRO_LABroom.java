/*
* Copyright (©) 2000-2008, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0.1
*
* @author  Göran Sperber
* @version 4/12 -08
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Broom to detect AFRO_LA fragments.
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
public class AFRO_LABroom extends StandardBroom {

// prototype sequence
	private String aFRO_LA = "tggaaaccctggtggcatagtggttaagagctacggctgctaaccaaaaggttggcagttcaaatccaccaggtgctccttggaaactctatggggcattctactctgtcctatagggtcgctatgagtcggaatcaactcgacggcaatgggtttggttttggtttttt";

/**
* Constructor.
*/
	public AFRO_LABroom() {
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
		return "AFRO_LA";
	} // end of getDirtName()
	
/**
* @return	Sequence of prototype.
*/
	public String dirtString() {
		return aFRO_LA;
	} // end of  dirtString() 
	
}
