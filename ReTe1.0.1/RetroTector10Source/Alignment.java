/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/10 -06
* Beautified 12/10 -06
*/

package retrotector;

import java.io.*;
import java.util.*;

/**
* Representation of a multi-row alignment. Rows are numbered starting at 0.
*/
public class Alignment {

/**
* Class representing one row in an alignment.
*/
	public class AlignmentRow {
	
	/**
	* The number (1, 2...) of this row in the aligment.
	*/
		public final int IDNUMBER;
		
	/**
	* Int with a bit set corresponding to the number (0, 1...) of this row.
	*/
		public final int ROWCODE;
		
	/**
	* Position of first non-dash.
	*/
		public final int FIRSTVALID;
		
	/**
	* Position of last non-dash.
	*/
		public final int LASTVALID;
		
	/**
	* A description of where this row comes from;
	*/
		public final String ROWORIGIN;
		
	/**
	* Concatenation of all non-dashes, in lower case.
	*/
		public final String STRINGFORM;
	
	/**
	* Value of Short.MIN_VALUE means that the corresponding element in FOLLOWING represents
	* an amino acid according to the acid Compactor. 
	* Other value means position is dash, and is index of nearest preceding non-dash (or -1).
	*/
		public final short[ ] PRECEDING;
	
	/**
	* If corresponding element in PRECEDING is Short.MIN_VALUE this is
	* an amino acid according to the acid Compactor. 
	* Otherwise position is dash, and this is index of nearest following non-dash (or Short.MAX_VALUE).
	*/
		public final short[ ] FOLLOWING;
		
	/**
	* Rectangles whose x and width define parts of this of dubious value.
	*/
		public final Utilities.Rectangle[ ] SUSPECTPARTS;
		
	/**
	* Where scoreAt leaves the score.
	*/
		public float latestScore;
	
	/**
	* Used by ORFID to determine most popular row in Alignment.
	*/
		public int hitcount = 0;
		
	/**
	* Makes an instance from the collected text lines descibing a row in an alignment.
	* @param	theBuffer	StringBuffer with the characters defining the contents.
	* @param	id				Unique number (1..) of this row.
	* @param	orig			Name or other text identifying source of data.
	* @param	suspect		Contents of "Suspect" parameter in Alignment file.
	*/
		public AlignmentRow(StringBuffer theBuffer, int id, String orig, String[ ] suspect) throws RetroTectorException {
		
			IDNUMBER = id;
			ROWCODE = 1 << (id - 1);
			ROWORIGIN = orig;
			
			StringBuffer buf = new StringBuffer();
			char cha;
			for (int chai=0; chai<theBuffer.length(); chai++) {
				cha = theBuffer.charAt(chai);
				if (!isDash(cha)) {
					buf.append(cha);
				}
			}
			STRINGFORM = buf.toString().toLowerCase();
			
			Compactor theCompactor = Compactor.ACIDCOMPACTOR;
			PRECEDING = new short[theBuffer.length()];
			FOLLOWING = new short[theBuffer.length()];
			
	// look for terminal dashes
			short bp = (short) (FOLLOWING.length - 1);
			short target = 0; // first non-dash position after current
			while ((bp >= 0) && (isDash(theBuffer.charAt(bp)))) {
				FOLLOWING[bp] = Short.MAX_VALUE;
				bp--;
			}
			LASTVALID = bp;
			while (bp >= 0) {
				while ((bp >= 0) && (!isDash(theBuffer.charAt(bp)))) {
					target = bp;
					FOLLOWING[bp] = (short) theCompactor.charToIntId(theBuffer.charAt(bp));
					bp--;
				}
				while ((bp >= 0) && (isDash(theBuffer.charAt(bp)))) {
					FOLLOWING[bp] = target;
					bp--;
				}
			}
			bp = 0;
	// look for leading dashes
			while ((bp < PRECEDING.length) && (isDash(theBuffer.charAt(bp)))) {
				PRECEDING[bp] = -1;
				bp++;
			}
			FIRSTVALID = bp;
			while (bp < PRECEDING.length) {
				while ((bp < PRECEDING.length) && (!isDash(theBuffer.charAt(bp)))) {
					target = bp;
					PRECEDING[bp] = Short.MIN_VALUE;
					bp++;
				}
				while ((bp < PRECEDING.length) && (isDash(theBuffer.charAt(bp)))) {
					PRECEDING[bp] = target;
					bp++;
				}
			}
			
			if (suspect != null) { // suspect parts are specified
				Stack sustack = new Stack();
				for (int su=0; su<suspect.length; su++) { // read them one by one
					if ((suspect[su].length() > ROWORIGIN.length()) && suspect[su].substring(0, ROWORIGIN.length()).equalsIgnoreCase(ROWORIGIN)) { // does it concern this?
						String sus = suspect[su].substring(ROWORIGIN.length()).trim(); // remove label
						String susp = sus.substring(0, sus.indexOf(">") + 1); // read off first specifier
						int p1; // position pointed to by first specifier
						if (susp.equalsIgnoreCase("<START>")) {
							p1 = 0;
						} else {
							p1 = MOTIFLINE.indexOf(susp);
						}
						if (p1 < 0) {
							p1 = MOTIFLINE2.indexOf(susp);
						}
						susp = sus.substring(sus.lastIndexOf("<")).trim(); // read off second specifier
						int p2; // position pointed to by second specifier
						if (susp.equalsIgnoreCase("<END>")) {
							p2 = PRECEDING.length + 1;
						} else {
							p2 = MOTIFLINE.indexOf(susp);
						}
						if (p2 < 0) {
							p2 = MOTIFLINE2.indexOf(susp);
						}
						if ((p1 >= 0) & (p2 >= p1)) {
							sustack.push(new Utilities.Rectangle(p1, 0, p2 - p1 + 1, 1));
						}
					}
				}
				if (sustack.size() > 0) {
					SUSPECTPARTS = new Utilities.Rectangle[sustack.size()];
					sustack.copyInto(SUSPECTPARTS);
				} else {
					SUSPECTPARTS = null;
				}
			} else {
				SUSPECTPARTS = null;
			}
		} // end of AlignmentRow.constructor(StringBuffer, int, String, String[ ])
	
	
	/**
	* @param	startingAt	A position to start searching at.
	* @return	The position of the first valid amino acid at or after startingAt, or Integer.MIN_VALUE.
	*/
		public final int nextAcidPos(int startingAt) {
			if ((startingAt>=FOLLOWING.length) || (startingAt < 0)) {
				return Integer.MIN_VALUE;
			}
			if (PRECEDING[startingAt] == Short.MIN_VALUE) {
				return startingAt;
			} else {
				if (FOLLOWING[startingAt] == Short.MAX_VALUE) {
					return Integer.MIN_VALUE;
				} else {
					return FOLLOWING[startingAt];
				}
			}
		} // end of AlignmentRow.nextAcidPos(int)
	
	/**
	* @param	startingAt	A position to start searching at.
	* @return	The position of the first valid amino acid at or before startingAt, or Integer.MIN_VALUE.
	*/
		public final int previousAcidPos(int startingAt) {
			if ((startingAt>=FOLLOWING.length) || (startingAt < 0)) {
				return Integer.MIN_VALUE;
			}
			if (PRECEDING[startingAt] == Short.MIN_VALUE) {
				return startingAt;
			} else {
				if (PRECEDING[startingAt] == -1) {
					return Integer.MIN_VALUE;
				} else {
					return PRECEDING[startingAt];
				}
			}
		} // end of AlignmentRow.previousAcidPos(int)
		
		
	/**
	* Calculates score between this and DNA at a position. 
	* @param	dna				The DNA to score with.
	* @param	dnapos		The target position	(internal) in DNA.
	* @param	thispos		The target position in this.
	* @param	stopscore	The score to assign stop codons in DNA.acidRawScoreOne().
	* @return	Score, or -Float.MAX_VALUE if position is dash.
	*/
		public final float scoreAtInRow(DNA dna, int dnapos, int thispos, int stopscore) {
			if (PRECEDING[thispos] != Short.MIN_VALUE) {
				return -Float.MAX_VALUE;
			}
			latestScore = dna.acidRawScoreOne(FOLLOWING[thispos], dnapos, stopscore);
			return latestScore;
		} // end of AlignmentRow.scoreAt(DNA, int, int, int)
					
	/**
	* @param	pos	An acid position.
	* @return	Character representing acid at pos, or dash.
	*/
		public final char acidAt(int pos) {
			if (PRECEDING[pos] == Short.MIN_VALUE) {
				return Compactor.ACIDCOMPACTOR.intToCharId(FOLLOWING[pos]);
			} else {
				return DASHCHAR;
			}
		} // end of acidAt(int)
		
	/**
	* @param	pos1	An acid position.
	* @param	pos2	Another acid position.
	* @return	The number (or negative, if pos1>pos2) of non-dashes between pos1 and pos2, including the lower but not the higher of them, or Integer.MIN_VALUE if either is outside the DNA, or if there is a suspect part between them.
	*/
		public final int nonDashesBetween(int pos1, int pos2) {
			if ((pos1 < 0) | (pos1 > PRECEDING.length)) {
				return Integer.MIN_VALUE;
			}
			if ((pos2 < 0) | (pos2 > PRECEDING.length)) {
				return Integer.MIN_VALUE;
			}
			if (pos1 == pos2) {
				return 0;
			}
			if (SUSPECTPARTS != null) {
				Utilities.Rectangle r;
				if (pos1 > pos2) {
					r = new Utilities.Rectangle(pos2, 0, pos1 - pos2 + 1, 1);
				} else {	
					r = new Utilities.Rectangle(pos1, 0, pos2 - pos1 + 1, 1);
				}
				for (int s=0; s<SUSPECTPARTS.length; s++) {
					if (SUSPECTPARTS[s].intersects(r)) {
						return Integer.MIN_VALUE;
					}
				}
			}
					
			int count = 0;
			int sign = 1;
			if (pos1 > pos2) {
				sign = -1;
				for (int i=pos1-1; i>=pos2; i--) {
					if (acidAt(i) != DASHCHAR) {
						count++;
					}
				}
			} else {
				for (int i=pos1; i!=pos2; i++) {
					if (acidAt(i) != DASHCHAR) {
						count++;
					}
				}
			}
// I wonder why, but it probably does not matter
			if ((count == 0) && (pos1 != pos2)) {
				return Integer.MIN_VALUE;
			}
			return count * sign;
		} // end of AlignmentRow.nonDashesBetween(int, int)
		
/**
* @param	len	Length of String to pick.
* @return	The length first characters in STRINGFORM, or null if start is suspect.
*/
		public final String getLeadingString(int len) {
			if ((SUSPECTPARTS != null) && (SUSPECTPARTS.length > 0) && (SUSPECTPARTS[0].x == 0)) {
				return null;
			}
			return STRINGFORM.substring(0, len);
		} // end of AlignmentRow.getLeadingString(int)
	
/**
* @param	len	Length of String to pick.
* @return	The length last characters in STRINGFORM, or null if end is suspect.
*/
		public final String getTrailingString(int len) {
			if ((SUSPECTPARTS != null) && (SUSPECTPARTS.length > 0)) {
				Utilities.Rectangle ur = SUSPECTPARTS[SUSPECTPARTS.length - 1];
				if (ur.x + ur.width > PRECEDING.length) {
					return null;
				}
			}
			return STRINGFORM.substring(STRINGFORM.length() - len);
		} // end of AlignmentRow.getTrailingString(int)
	
	} // end of AlignmentRow


/**
*	Character representing gaps in alignment = '-';
*/
	public static final char DASHCHAR = '-';

/**
*	 No more rows than this in alignment.
*/
	public static final int MAXMASTERS = 30;
  
/**
* Key of alignment data = "Alignment".
*/
  public static final String ALIGNMENTKEY = "Alignment";
	
/**
* Key of info about inferior data = "Suspect".
*/
  public static final String SUSPECTKEY = "Suspect";
	
/**
* @param	c	A character
* @return	True if c is '-'.
*/
	public static final boolean isDash(char c) {
		if (c == DASHCHAR) {
			return true;
		}
		return false;
	} // end of isDash(char)

/**
* The number of rows.
*/
	public final int NROFROWS;

/**
* The length of each row.
*/
	public final int MASTERLENGTH;

/**
* The first of the lines indicating where MotifGroups hit.
*/
	public final String MOTIFLINE;

/**
* The second of the lines indicating where MotifGroups hit.
*/
	public final String MOTIFLINE2;

/**
* Name of file defining this Alignment.
*/
	public final String FILENAME;
	
/**
* Blank String of length 7 + 2*NROFROWS.
*/
	public final String EMPTYLINE;
	
/**
* True for columns containing no dashes.
*/
	public final boolean[ ] NODASH;
	
/**
* The result of the most recent scoreAt() call.
*/
	public float latestScore;

	private final Compactor THECOMPACTOR = Compactor.ACIDCOMPACTOR; // acid Compactor
	
	private AlignmentRow[ ] rows; // one byte per acid, coded through THECOMPACTOR
	private AlignmentRow currentRow = null;
	private float[ ] tempscores; // scratch area for scoreAt()
	
	private int acidMark = 0; // pos of current amino acid.
		
/**
* Reads a standard alignment file.
* @param	theFile	The relevant file.
*/
	public Alignment(File theFile) throws RetroTectorException {
	
		FILENAME = theFile.getName();
    Hashtable table = new Hashtable();
		ParameterFileReader reader = new ParameterFileReader(theFile, table);
		reader.readParameters();
		reader.close();
		String[ ] lines = (String[ ]) table.get(ALIGNMENTKEY);
		int line = 0;
		
		String[ ] names = new String[MAXMASTERS];
		for (int s = 0; s<MAXMASTERS; s++) {
			names[s] = "";
		}
		
		StringBuffer[ ] tempal = new StringBuffer[MAXMASTERS]; // to read data into
		StringBuffer tempmot = null; // to read motif group data into
		StringBuffer tempmot2 = null; // to read motif group data into
		int tempalp; // pointer to current sequence
		int nroflines;
		String mott = ""; // motif group data from current line batch
		String mott2 = ""; // motif group data from current line batch
		int namePartLength = Integer.MIN_VALUE; // length of name part of line
		
		String currentline = lines[line++]; // read off title line
		while (!isValid(currentline)) {
			currentline = lines[line++]; // peel off blank lines
		}
		while (currentline.trim().startsWith("<")) { // motif line
			mott2 = mott;
			mott = currentline;
			currentline = lines[line++];
		}
		tempalp = 0;
		while (isValid(currentline)) { // read first bunch of data lines
			tempal[tempalp] = new StringBuffer();
			currentline = currentline.trim();
			if (Character.isDigit(currentline.charAt(currentline.length() - 1))) { // terminal number?
				currentline = currentline.substring(0, currentline.lastIndexOf(" ")).trim();
			}
			if (namePartLength == Integer.MIN_VALUE) {
				namePartLength = currentline.lastIndexOf(" ") + 1;
			}
			names[tempalp] = currentline.substring(0, namePartLength).trim();
			tempal[tempalp++].append(currentline.substring(namePartLength));
			currentline = lines[line++];
		}
		nroflines = tempalp;
		if (namePartLength >= mott.length()) {
			tempmot = new StringBuffer(); // Motif line was empty
		} else {
			tempmot = new StringBuffer(mott.substring(namePartLength));
		}
		if (namePartLength >= mott2.length()) {
			tempmot2 = new StringBuffer(); // Motif line was empty
		} else {
			tempmot2 = new StringBuffer(mott2.substring(namePartLength));
		}
		while (tempmot.length() < tempal[0].length()) { // extend Motif line
			tempmot.append(' ');
		}
		if (tempmot.length() > tempal[0].length()) {
			RetroTectorException.sendError(this, "Too long motif line in alignment file", mott);
		}
		while (tempmot2.length() < tempal[0].length()) {
			tempmot2.append(' ');
		}
		if (tempmot2.length() > tempal[0].length()) {
			RetroTectorException.sendError(this, "Too long motif line in alignment file", mott2);
		} // initial block read
		
		mott = "";
		mott2 = "";
		tempalp = -2;
		while  (line < lines.length) {
			currentline = lines[line++];
			if (isValid(currentline)) { // look for EOF
				if ((currentline.length() > namePartLength) && (currentline.substring(0, namePartLength).trim().length() == 0)) {
					if (tempalp >= 0) {
						RetroTectorException.sendError(this, "Motif line in wrong place in alignment file", currentline);
					}
					mott2 = mott;
					mott = currentline;
					tempalp++;
				} else {
					if (tempalp < 0) {
						tempalp = 0;
					}
					currentline = currentline.trim();
					if (Character.isDigit(currentline.charAt(currentline.length() - 1))) { // terminal number?
						currentline = currentline.substring(0, currentline.lastIndexOf(" ")).trim();
					}
					boolean bo = true;
					try {
						bo = names[tempalp].equals(currentline.substring(0, namePartLength).trim());
					} catch (StringIndexOutOfBoundsException se) {
						RetroTectorException.sendError(this, "Syntax error in line", currentline);
					}
					if (!bo) {
						RetroTectorException.sendError(this, "Name disparity in alignment file", names[tempalp], currentline.substring(0, namePartLength).trim());
					}
					tempal[tempalp++].append(currentline.substring(namePartLength));
				}
				if (tempalp >= nroflines) {
					if (namePartLength < mott.length()) {
						tempmot.append(mott.substring(namePartLength));
					}
					if (namePartLength < mott2.length()) {
						tempmot2.append(mott2.substring(namePartLength));
					}
					while (tempmot.length() < tempal[0].length()) {
						tempmot.append(' '); // extend Motif line
					}
					if (tempmot.length() > tempal[0].length()) {
						RetroTectorException.sendError(this, "Too long motif line in alignment file", mott);
					}
					while (tempmot2.length() < tempal[0].length()) {
						tempmot2.append(' '); // extend Motif line
					}
					if (tempmot2.length() > tempal[0].length()) {
						RetroTectorException.sendError(this, "Too long motif line in alignment file", mott2);
					}
					mott = "";
					mott2 = "";
					tempalp = -2;
				}
			}
		}
		
		MOTIFLINE = tempmot.toString();
		MOTIFLINE2 = tempmot2.toString();
// check for dashfilled columns
		char ch;
		boolean dashline;
		for (int cc=0; cc<tempal[0].length(); cc++) {
			dashline = true;
			while (dashline && (cc<tempal[0].length())) {
				for (int rr=0; dashline && (rr<tempal.length) && (tempal[rr] != null); rr++) {
					ch = tempal[rr].charAt(cc);
					if (ch != DASHCHAR) {
						dashline = false;
					}
				}
				if (dashline) {
					RetroTectorException.sendError(this, "Dash column in alignment file " + theFile.getName() + " at " + cc);
				}
			}
		}
		
// check read data for consistency and transfer them to arrays
		MASTERLENGTH = tempal[0].length();
		tempalp = 0;
		while ((tempalp < tempal.length) && (tempal[tempalp] != null)) {
			if (tempal[tempalp].length() != MASTERLENGTH) {
				RetroTectorException.sendError(this, "Unequal lengths in master alignment:", "" + tempalp, "" + tempal[tempalp].length());
			}
			tempalp++;
		}
		
		NROFROWS = tempalp;
		rows = new AlignmentRow[NROFROWS];
		tempscores = new float[NROFROWS];
		for (tempalp=0; tempalp<NROFROWS; tempalp++) {
			try {
				rows[tempalp] = new AlignmentRow(tempal[tempalp], tempalp + 1, names[tempalp], (String[ ]) table.get(SUSPECTKEY));
			} catch (NullPointerException e) {
				throw e;
			}
		}
		currentRow = rows[0];
		
// mark dashfree columns
		NODASH = new boolean[MASTERLENGTH];
		boolean fil;
		for (int d=0; d<MASTERLENGTH; d++) {
			fil = true;
			for (int dd=0; dd<NROFROWS; dd++) {
				if (rows[dd].acidAt(d) == DASHCHAR) {
					fil = false;
				}
			}
			NODASH[d] = fil;
		}
		
// construct empty line of suitable length
		StringBuffer sbb = new StringBuffer();
		for (int sb=0; sb<(7 + 2*NROFROWS); sb++) {
			sbb.append(' ');
		}
		EMPTYLINE = sbb.toString();

	} // end of constructor(File)


/**
* @return	Number of amino acid pointed at.
*/
	public final int getAcidMark() {
		return acidMark;
	} // end of getMark()
	
/**
* @return	The row in vogue at the moment.
*/
	public final AlignmentRow getCurrentRow() {
		return currentRow;
	} // end of getCurrentRow()
	
/**
* Calculates best score between this and DNA at one position.
* Best score is put into latestScore.
* @param	dna				The DNA to score with.
* @param	dnapos		The target position	(internal) in DNA.
* @param	thispos		The target position in this.
* @param	stopscore	The score to assign stop codons in DNA.acidRawScoreOne().
* @return	Int with bits set for rows where maximal score obtained.
*/
	public final int scoreAt(DNA dna, int dnapos, int thispos, int stopscore) {
		
		float bestScore = -Float.MAX_VALUE;
		for (int r=0; r<NROFROWS; r++) {
			tempscores[r] = rows[r].scoreAtInRow(dna, dnapos, thispos, stopscore);
			if (tempscores[r] > bestScore) {
				bestScore = tempscores[r];
			}
		}
		int result = 0;
		for (int rr=NROFROWS-1; rr>=0; rr--) {
			result = result << 1;
			if (tempscores[rr] >= bestScore) {
				result = result | 1;
			}
		}
		latestScore = bestScore;
		return result;
	} // end of scoreAt(DNA, int, int, int)
		
/**
* Gives a choice of rows. If it includes the current row, it is retained.
* Otherwise the first of the suggested rows is made current.
* @param	suggestedRowCode	Int with bits set for choice rows.
*/
	public final void suggestCurrentRow(int suggestedRowCode) {
		if ((currentRow.ROWCODE & suggestedRowCode) != 0) {
			return;
		}
		for (int i=0; i<NROFROWS; i++) {
			currentRow = rows[i];
			if ((currentRow.ROWCODE & suggestedRowCode) != 0) {
				return;
			}
		}
	} // end of suggestCurrentRow(int)
	
/**
* @param	index	Number (from 0) of a row.
* @return	The row with that number, or null.
*/
	public final AlignmentRow getRow(int index) {
		if ((index < 0) | (index >= rows.length)) {
			return null;
		}
		return rows[index];
	} // end of getRow(int)

/**
* @return		The number of rows
*/
	public final int nrOfRows() {
		return rows.length;
	} // end of nrOfRows()
	
/**
* @param	id	Identification number (from 1) of a row.
* @return	The row with that number, or null.
*/
	public final AlignmentRow getIDRow(int id) {
		return getRow(id - 1);
	} // end of getIDRow(int)
	

/**
* @return	The acid being pointed to in the current master row, or a dash.
*/
	public final char currentAcid() {
		if ((acidMark >= MASTERLENGTH) | (acidMark < 0)) {
			return '_';
		}
		return currentRow.acidAt(acidMark);
	} // end of currentAcid()

/**
* @return Text describing alignment at current position, which is incremented, and two empty lines.
*/
	public final String[ ] advanceMasterAcid() throws FinishedException {
		String[ ] temp = new String[3];
		temp[1] = EMPTYLINE;
		temp[2] = EMPTYLINE;
		StringBuffer sb = new StringBuffer(EMPTYLINE);
		if ((acidMark) % 10 == 0) { // time for position marker?
			String s = String.valueOf(acidMark);
			for (int i=0; i<s.length(); i++) {
				sb.setCharAt(i + 7 - s.length(), s.charAt(i));
			}
		}
		for (int m = 0; m < NROFROWS; m++) { // one character for each sequence
			if (rows[m].PRECEDING[acidMark] != Short.MIN_VALUE) {
				sb.setCharAt(8 + 2*m, DASHCHAR);
			} else {
				sb.setCharAt(8 + 2*m, THECOMPACTOR.intToCharId((int) rows[m].FOLLOWING[acidMark]));
			}
		}
		temp[0] = sb.toString();
		acidMark++;
		if (acidMark >= MASTERLENGTH) {
			throw new FinishedException();
		}
		return temp;
	} // end of advanceMasterAcid()
	
/**
* Zeroes hit count in all rows, acidMark and currentRow.
*/
	public final void reset() {
		for (int i=0; i<rows.length; i++) {
			rows[i].hitcount = 0;
		}
		acidMark = 0;
		currentRow = rows[0];
	} // end of reset()
	
/**
* @return The row most used by ORFID.
*/
	public final AlignmentRow mostHitRow() {
		int tophits = -1;
		AlignmentRow topRow = null;
		for (int i=0; i<rows.length; i++) {
			if (rows[i].hitcount > tophits) {
				topRow = rows[i];
				tophits = topRow.hitcount;
			}
		}
		return topRow;
	} // end of mostHitRow()
  
/**
* @param	groupName	Name of a MotifGroup.
* @return	The position of groupName in this, or -1.
*/
  public final int positionOf(String groupName) {
    String s = "<" + groupName + ">";
    int pos = MOTIFLINE.indexOf(s);
    if (pos < 0) {
      pos = MOTIFLINE2.indexOf(s);
    }
    return pos;
  } // end of positionOf(String)
	
/**
* Not in use at present.
*	@param	q	A String.
*	@return	An int with bits set for rows beginning with q, or 0 if none found.
*/
	public final int leadingIn(String q) {
		int rowcode = 0;
		q = q.toLowerCase();
		for (int r=0; r<NROFROWS; r++) {
			if (rows[r].STRINGFORM.startsWith(q)) {
				rowcode |= rows[r].ROWCODE;
			}
		}
		return rowcode;
	} // end of leadingIn(String)

/**
* Not in use at present.
*	@param	q	A String.
*	@return	An int with bits set for rows ending with q, or 0 if none found.
*/
	public final int trailingIn(String q) {
		int rowcode = 0;
		q = q.toLowerCase();
		for (int r=0; r<NROFROWS; r++) {
			if (rows[r].STRINGFORM.endsWith(q)) {
				rowcode |= rows[r].ROWCODE;
			}
		}
		return rowcode;
	} // end of trailingIn(String)

	
// is this a line with sequence data?
	private final boolean isValid(String line) {
		if (line == null) {
			return false;
		}
		String s = line.trim();
		if (s.length() == 0) {
			return false;
		}
		if (s.charAt(0) == '*') {
			return false;
		}
		if (s.charAt(0) == '.') {
			return false;
		}
		if (s.charAt(0) == ':') {
			return false;
		}
		return true;
	} // end of isValid(String)

// returns next line in file containing data.
	private final String getNextValid(BufferedReader b) throws RetroTectorException {
		try {
			String line = b.readLine();
			while (!isValid(line)) {
				if (line == null) {
					return null;
				}
				line = b.readLine();
			}
			if (line.indexOf('\t') >= 0) {
				RetroTectorException.sendError(this, "Tab in alignment file", line);
			}
			return line;
		} catch (IOException ioe) {
			return null;
		}
	} // end of getNextValid(BufferedReader)

}

