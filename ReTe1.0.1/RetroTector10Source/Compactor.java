/*
*
* @(#)Key.java	1.0 97/07/22
* 
* Copyright (©) 1997-2006, Tore Eriksson, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author Tore Eriksson & Gšran Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

/**
* Handles all conversions between characters and
* the internal numerical formats for DNA bases and amino acids.
*
*/ 
public final class Compactor {

/**
* Symbol for stop codons = "z".
*/
  public static final String STOPSTRING = "z";

/**
* Symbol for stop codons = 'z'.
*/
  public static final char STOPCHAR = 'z';
	
/**
* Symbol for unknown codons = 'x'.
*/
  public static final char UNKNOWNCHAR = 'x';
	
/**
* Acceptable base codes, lowercase, in order = "tcgawyrmknsvdhb".
*/
	public static final String BASECODES = "tcgawyrmknsvdhb";
  
/**
* For conversion of amino acids.
*/
  public static final Compactor ACIDCOMPACTOR = new Compactor('a');
  
/**
* For conversion of DNA bases.
*/
  public static final Compactor BASECOMPACTOR = new Compactor('b');

    
/**
* Integer code for STOPCHAR.
*/
	public final int STOPINT;
  
/**
* Integer code for UNKNOWNCHAR.
*/
	public final int UNKNOWNINT;
  
/**
* 'a' (acid) or 'b' (base).
*/
  public final char TYPE;
	
/**
* Smallest char converted.
*/
  public final char MINCHAR;
	
/**
* Largest char converted.
*/
  public final char MAXCHAR;

  private final char[ ] CHARCODEARRAY; // array of possible characters
  private final int[ ] INTCODEARRAY; // positions in CHARCODEARRAY indexed by character-MINCHAR

/**
 * Creates a new Compactor instance of a type given by ch.
 *
 * @param  ch can take the values:<br>
 * 	a: Converts the 21 amino acids into the 64 corresponding numerical values
 * for all codons, and x into 64.<br>
 * 	b: Converts 4 (+11) basevalues into 4 numerical values and back.
 */
  Compactor(char ch) {
    
    TYPE = ch;
    
  	String charCodes = null; // string of possible characters
    switch (TYPE) {
    case 'a':
      charCodes = new String( "ffllssssccw" + STOPSTRING + "yy" + STOPSTRING + STOPSTRING + 
			   "llllpppprrrrhhqqvvvvaaaaggggddeeiimittttssrrnnkk" + UNKNOWNCHAR);
      break;
    case 'b':
      charCodes = BASECODES;
      break;
    }
    CHARCODEARRAY = charCodes.toCharArray();
// find min and max char code used
    char maxchar = CHARCODEARRAY[0];
    char minchar = CHARCODEARRAY[0];
    for (int i=1; i<CHARCODEARRAY.length; i++) {
    	maxchar = (char) Math.max(maxchar, CHARCODEARRAY[i]); 
    	minchar = (char) Math.min(minchar, CHARCODEARRAY[i]);
    }
		MINCHAR = minchar;
		MAXCHAR = maxchar;
    INTCODEARRAY = new int[MAXCHAR - MINCHAR + 1];
// fill with -1 for invalid characters
    for (int j=0; j<INTCODEARRAY.length; j++) {
    	INTCODEARRAY[j] = -1;
    }
// fill with indices for valid characters
    for (int k=CHARCODEARRAY.length - 1; k>=0; k--) {
    	INTCODEARRAY[CHARCODEARRAY[k] - MINCHAR] = k;
    }
		STOPINT = charToIntId(STOPCHAR);
		UNKNOWNINT = charToIntId(UNKNOWNCHAR);
  } // end of constructor(char)

/**
 * @param	ch	A character (upper or lower case) defining an acid or base.
 * @return	Lowest integer code corresponding to ch, or -1.
 */
  public final int charToIntId(char ch) {
  	if ((ch < MINCHAR) | (ch > MAXCHAR)) {
  		ch = Character.toLowerCase(ch);
  		if ((ch < MINCHAR) | (ch > MAXCHAR)) {
  			return -1;
  		}
  	}
  	return INTCODEARRAY[ch - MINCHAR];
  } // end of charToIntId(char)

/**
 * @param	number	The integer code corresponding to an acid or base.
 * @return	The lower case character corresponding to number.
 */
  public final char intToCharId(int number) {
  	return CHARCODEARRAY[number];
  } // end of intToCharId(int)
	
/**
* @param	s	A String, presumably a sequence of acids or bytes.
* @return	An array of the corresponding integer codes, or null if there were any unacceptable characters.
*/
 	public final byte[ ] stringToBytes(String s) {
		byte[ ] res = new byte[s.length()];
		for (int i=0; i<res.length; i++) {
			if ((res[i] = (byte) charToIntId(s.charAt(i))) == -1) {
				return null;
			}
		}
		return res;
	} // end of stringToBytes(String)
	
/**
* @param	s	An array of integer codes of acids or bytes.
* @return	A String of the corresponding lower case characters.
*/
	public final String bytesToString(byte[ ] b) {
		StringBuffer s = new StringBuffer();
		for (int i=0; i<b.length; i++) {
			s.append(intToCharId(b[i]));
		}
		return s.toString();
	} // end of stringToBytes(byte[ ])
	

/**
 * Mainly for debugging
 */
   public String toString() {
    return new String( "Compactor of type: " + TYPE);
  } // end of toString()

}
