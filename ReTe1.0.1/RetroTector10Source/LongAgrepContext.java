/*
 * Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
 */

/**
 *   For RetroTector 1.0
 *
 * @author  G. Sperber
 * @version 21/9 -06
 * Beautified 21/9 -06
*/

package retrotector;

/**
* Keeps the data used in an agrep search in DNA.
* Used by DNA.longAgrepFind.
*/
public class LongAgrepContext {

/**
* 1, 2, 4...
*/
	public static final long[ ] bitmasks = new long[64];

/**
* The pattern to search for.
*/
	final byte[ ] PATTERN;
	
/**
* The last position in DNA to search at.
*/
	public final int LAST;
	
/**
* The highest number of errors accepted.
*/
	public final int MUTLIMIT;
	
/**
* The currently first position in DNA to search at.
*/
	int beginAt;
	
/**
* Agrep working bit arrays.
*/
	long[ ] r;
	
/**
* Bit arrays showing where bases are in pattern.
*/
	long[ ] s;
	
/**
* Bit mask for this pattern length.
*/
	final long bitmask;
	
/**
* Set if an ambiguous base code was found in pattern.
*/
	public boolean ambiguous = false;
	

	static {
		bitmasks[0] = 1;
		for (int b=1; b<64; b++) {
			bitmasks[b] = bitmasks[b - 1] << 1;
		}
	} // end of static initializer
	
	
/**
* Constructor.
* @param	pattern	Base codes of pattern to search for.
* @param	first	The first position in DNA to search at.
* @param	last	The last position in DNA to search at.
* @param	mutlimit	The highest number of errors accepted.
*/
public LongAgrepContext(byte[ ] pattern, int first, int last, int mutlimit) {
		PATTERN = pattern;
		beginAt = first;
		LAST = last;
		MUTLIMIT = mutlimit;
		bitmask = bitmasks[pattern.length - 1];
		r = new long[mutlimit + 1];
		for (int rr=1; rr<r.length; rr++) {
			r[rr] = (1 << rr) - 1;
		}
		s = new long[5];
		for (int q=0; q<pattern.length; q++) {
			if (pattern[q] > 3) {
				ambiguous = true;
			}
			s[pattern[q]] |= bitmasks[q];
		}
	} // end of constructor(byte[ ], int, int, int)

}