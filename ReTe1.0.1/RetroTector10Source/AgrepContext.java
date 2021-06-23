/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 18/9 -04
* Beautified 18/9 -04
*/

package retrotector;

/**
* Keeps the data used in an agrep search in DNA.
* Used by DNA.agrepFind.
*/
public class AgrepContext {

/**
* 1, 2, 4...
*/
	private static final int[ ] bitmasks = new int[32];

	static {
		bitmasks[0] = 1;
		for (int b=1; b<32; b++) {
			bitmasks[b] = bitmasks[b - 1] << 1;
		}
	} // end of static initializer
	
	
/**
* The last position in DNA to search at.
*/
	public final int LAST;
	
/**
* The highest number of errors accepted.
*/
	public final int MUTLIMIT;
	
/**
* Set if an ambiguous base code was found in pattern.
*/
	public boolean ambiguous = false;
	
/**
* The pattern to search for.
*/
	final byte[ ] PATTERN;
	
/**
* Bit mask for this pattern length.
*/
	final int BITMASK;
	
/**
* The currently first position in DNA to search at.
*/
	int beginAt;
	
/**
* Agrep working bit arrays.
*/
	int[ ] r;
	
/**
* Bit arrays showing where bases are in pattern.
*/
	int[ ] s;
	

/**
* Constructor.
* @param	pattern		Base codes of pattern to search for.
* @param	first			The first position in DNA to search at.
* @param	last			The last position in DNA to search at.
* @param	mutlimit	The highest number of errors accepted.
*/
	public AgrepContext(byte[ ] pattern, int first, int last, int mutlimit) {
		PATTERN = pattern;
		beginAt = first;
		LAST = last;
		MUTLIMIT = mutlimit;
		BITMASK = bitmasks[pattern.length - 1];
		r = new int[mutlimit + 1];
		for (int rr=1; rr<r.length; rr++) {
			r[rr] = (1 << rr) - 1;
		}
		s = new int[5];
		for (int q=0; q<pattern.length; q++) {
			if (pattern[q] > 3) {
				ambiguous = true;
			}
			s[pattern[q]] |= bitmasks[q];
		}
	} // end of constructor(byte[ ], int, int, int)

	
}