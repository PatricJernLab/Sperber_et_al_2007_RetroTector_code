/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 17/10 -06
* Beautified 17/10 -06
*/

package retrotector;

import java.util.*;

/**
* Information about a single MotifHit needed by ORFID.
*/
public class MotifHitInfo {

/**
* The character marking hotspot = '#'.
*/
	public final static char HOTSPOTMARKER = '#';
  
/**
* Name of MotifGroup of hitting Motif.
*/
	public final String MOTIFGROUP;

/**
* DNA position (internal) of hit hotspot.
*/
	public final int HOTSPOT;
	
/**
* See Motif.MOTIFTYPE.
*/
	public final String HITMOTIFTYPE;
	
/**
* Rounded score of this hit.
*/
	public final int SCORE;

/**
* String describing virus genus.
*/
	public final String RVGENUS;

/**
* String of length equal to acids length of hit + 1 (for the HOTSPOTMARKER).
*/
	public final String CONTENTS;
	
/**
* The number of acids CONTENTS describes (=CONTENTS.length - 1).
*/
	public final int ACIDSLENGTH;

/**
* Short description of Motif.
*/
	public final String COMMENT;
	
/**
* DNA where hit is.
*/
	public final DNA TARGETDNA;

/**
* Alignment where Motif sought.
*/
	public final Alignment MASTER;

/**
* Unique position where Motif found in Master, or -1 (not found) or -2 (multiply found).
*/
	public final int POSINMASTER;

/**
* The ordinal number of this, in String form.
*/
	public final String INDEXSTRING;
	
/**
* True if not in correct order of hotspot position within containing MotifHitsInfo.
*/
	public boolean outOfOrder;
	
	private String str; // String to mark hit with in Putein file
  private int strpos; // position in DNA to start str

	private final void helper() { // makes str and strpos
    int ind = CONTENTS.indexOf(HOTSPOTMARKER);
    strpos = HOTSPOT - 3 * ind;
		StringBuffer sb = new StringBuffer();
    for (int i=0; i<ind; i++) {
      sb.append('_');
    }
    sb.append(INDEXSTRING);
		while (sb.length() < ACIDSLENGTH) {
			sb.append('_');
		}
		str = sb.toString();
	} // end of helper()
	

/**
* Constructor using a String, as output by toString().
* @param	inString	The String containing the information.
* @param	index			Ordinal number.
* @param	targetDNA	->TARGETDNA.
* @param	master		->MASTER.
*/
	public MotifHitInfo(String inString, int index, DNA targetDNA, Alignment master) throws RetroTectorException {
		TARGETDNA = targetDNA;
		MASTER = master;
		INDEXSTRING = String.valueOf(index);
		int	ind = inString.indexOf(' ');
		int ind2 = -1;
		try {
			ind2 = targetDNA.internalize(Utilities.decodeInt(inString.substring(0, ind)));
		} catch (NumberFormatException nfe) {
			RetroTectorException.sendError(this, "Syntax error", inString);
		}
		HOTSPOT = ind2;
		inString = inString.substring(ind).trim();
		ind = inString.indexOf(' ');
		try {
			ind2 = Utilities.decodeInt(inString.substring(0, ind));
		} catch (NumberFormatException nfe) {
			RetroTectorException.sendError(this, "Syntax error", inString);
		}
		SCORE = ind2;
		inString = inString.substring(ind).trim();
		ind = inString.indexOf(' ');
		MOTIFGROUP = inString.substring(0, ind).trim();
		inString = inString.substring(ind).trim();
		ind = inString.indexOf(' ');
		HITMOTIFTYPE = inString.substring(0, ind).trim();
		inString = inString.substring(ind).trim();
		ind = inString.indexOf(' ');
		RVGENUS = inString.substring(0, ind).trim();
		inString = inString.substring(ind).trim();
		ind = inString.indexOf(' ');
		CONTENTS = inString.substring(0, ind).trim();
    ACIDSLENGTH = CONTENTS.length() - 1;
		COMMENT = inString.substring(ind + 1).trim();
		helper();
		POSINMASTER = posInAlignment(MASTER);
	} // end of constructor(String, int, DNA, Alignment)

/**
* Constructor using a MotifHit.
* @param	theHit		The MotifHit to describe.
* @param	index			Ordinal number.
* @param	targetDNA	->TARGETDNA.
*/
	public MotifHitInfo(MotifHit theHit, int index, DNA targetDNA) throws RetroTectorException {
		MOTIFGROUP = theHit.PARENTMOTIF.MOTIFGROUP.MOTIFGROUPNAME;
		TARGETDNA = targetDNA;
		MASTER = null;
		INDEXSTRING = String.valueOf(index);
		HOTSPOT = theHit.MOTIFHITHOTSPOT;
		Motif m = theHit.PARENTMOTIF;
		HITMOTIFTYPE = m.MOTIFTYPE;
		SCORE = Math.round(theHit.fetchScore());
		RVGENUS = m.MOTIFRVGENUS;
    ACIDSLENGTH = (theHit.MOTIFHITLAST - theHit.MOTIFHITFIRST + 1) / 3;
    String s;
    if (ACIDSLENGTH <= theHit.PARENTMOTIF.MOTIFSTRING.length()) { // eg SplitAcidMotif
      s = theHit.PARENTMOTIF.MOTIFSTRING.substring(0, ACIDSLENGTH);
    } else {
      StringBuffer sb = new StringBuffer(theHit.PARENTMOTIF.MOTIFSTRING);
      while (sb.length() < ACIDSLENGTH) {
        sb.append("_");
      }
      s = sb.toString();
    }
    int n = (theHit.MOTIFHITHOTSPOT - theHit.MOTIFHITFIRST) / 3;
    CONTENTS = s.substring(0, n) + HOTSPOTMARKER + s.substring(n);
		COMMENT = m.MOTIFORIGIN;
		helper();
		POSINMASTER = -1;
	} // end of constructor(MotifHit, int, DNA)
	
/**
* String representation, acceptable as constructor input.
*/
	public String toString() {
		return(TARGETDNA.externalize(HOTSPOT) + " " + SCORE + " " + MOTIFGROUP + " " + HITMOTIFTYPE + " " + RVGENUS + " " + CONTENTS + " " + COMMENT);
	} // end of toString()
	
/**
* String representation, used by MotifHitsInfo.
*/
	public final String toStringIndexed() {
		return(INDEXSTRING + " " + toString());
	} // end of toStringIndexed()
	
/* Not in use at present.
* searches for match of contents with AlignmentRow
* returns position in AlignmentRow if found once
* -1 if not found, or not AcidMotif
* -2 if multiply found
*/
 	private final int posInRow(Alignment.AlignmentRow theRow) {
		if (!HITMOTIFTYPE.equals("P")) {
			return -1;
		}

		Compactor theCompactor = Compactor.ACIDCOMPACTOR;
		int pos = 0;
		char[ ] pattern = new char[ACIDSLENGTH];
		int ppo = 0;
		for (int pi=0; pi<pattern.length; pi++) {
			if (CONTENTS.charAt(ppo) == HOTSPOTMARKER) {
				ppo++;
			}
			pattern[pi] = Character.toLowerCase(CONTENTS.charAt(ppo));
			ppo++;
		}
		char[ ] charstore = new char[ACIDSLENGTH]; // successive non-dash characters from row
		int[ ] charindex = new int[ACIDSLENGTH]; // positions of characters in row
		for (int cc=0; cc<(ACIDSLENGTH-1); cc++) { // fill charstore & charindex except last element
			pos = theRow.nextAcidPos(pos);
			charindex[cc] = pos;
			charstore[cc] = theCompactor.intToCharId(theRow.FOLLOWING[pos]);
			pos++;
		}
		pos = theRow.nextAcidPos(pos);
		int foundpos = -1;
		do {
			charindex[ACIDSLENGTH - 1] = pos; // fill in last element
			charstore[ACIDSLENGTH - 1] = theCompactor.intToCharId(theRow.FOLLOWING[pos]);
			int p;
			for (p=0; (p<ACIDSLENGTH) && (charstore[p] == pattern[p]); p++) {
			} // check for identity with contents
			if (p >= ACIDSLENGTH) { // identity
				if (foundpos == -1) { // not yet found
					foundpos = charindex[0];
				} else { // duplicate hit
					return -2;
				}
			}
			System.arraycopy(charindex, 1, charindex, 0, ACIDSLENGTH - 1); // move down
			System.arraycopy(charstore, 1, charstore, 0, ACIDSLENGTH - 1);
			pos++;
		} while ((pos = theRow.nextAcidPos(pos)) != Integer.MIN_VALUE);
		return foundpos;
	} // end of posInRow
	
/**
* Searches for match in Alignment
* @param	theAlignment	Alignment to search in.
* @return position in AlignmentRow if found, -1 if not found.
*/
	public final int posInAlignment(Alignment theAlignment) {
		int lfoundpos = theAlignment.MOTIFLINE.indexOf("<" + MOTIFGROUP + ">");
		if (lfoundpos < 0) {
			lfoundpos = theAlignment.MOTIFLINE2.indexOf("<" + MOTIFGROUP + ">");
		}
		return lfoundpos;
	} // end of posInAlignment(Alignment)
	
/**
* @param	pos	A DNA position.
* @return	The character corresponding to pos when marking in Putein file.
*/
	public final char charAt(int pos) {
		if (pos < strpos) {
			return ' ';
		}
		int p = (pos - strpos) / 3;
		if (p < ACIDSLENGTH) {
			return str.charAt(p);
		}
		return ' ';
	} // end of charAt(int)
	
/**
*	@param	database	The relevant Database.
*	@return	True if belonging to an exactlyAligned Motif, not within 10 posiions of DNA ends and in correct order within MotifHitsInfo.
*/
	public final boolean usefulToORFID(Database database) {
		if (outOfOrder) {
			return false;
		}
		if (HOTSPOT < 10) {
			return false;
		}
		if (HOTSPOT > TARGETDNA.LENGTH - 10) {
			return false;
		}
		boolean b = database.getMotifGroup(MOTIFGROUP).groupExactlyAligned;
		return b;
	} // end of usefulToORFID(Database)
	
/**
* @return	The (internal) position in DNA where hit begins.
*/
	public final int startsAt() {
		return strpos;
	} // end os startsAt()
	
} // end of MotifHitInfo
