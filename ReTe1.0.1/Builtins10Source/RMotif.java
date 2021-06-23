/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 5/10 -06
*/
package builtins;

import retrotector.*;

import java.util.*;

/**
* Class representing R part of LTR.
* Hot spot is at first base.
*/
public class RMotif extends OrdinaryMotif {

	public static final String MINRLENGTHKEY = "MinRLength";
	public static final String MAXRLENGTHKEY = "MaxRLength";
	public static final String TATAAMINOFFSETKEY = "TataaMinOffset";
	public static final String TATAAMAXOFFSETKEY = "TataaMaxOffset";
	public static final String INRMINOFFSETKEY = "InrMinOffset";
	public static final String INRMAXOFFSETKEY = "InrMaxOffset";
	public static final String MINLEADERLENGTHKEY = "MinLeaderLength";
	public static final String MAXLEADERLENGTHKEY = "MaxLeaderLength";
	public static final String MINTRAILERLENGTHKEY = "MinTrailerLength";
	public static final String MAXTRAILERLENGTHKEY = "MaxTrailerLength";


/**
* @return String identifying this class = "R".
*/
	public static final String classType() {
		return "R";
	} // end of classType

	public int FIRSTSTART;
	public int LASTEND;
	
	public final int MINRLENGTH;
	public final int MAXRLENGTH;
	public final int TATAAMINOFFSET;
	public final int TATAAMAXOFFSET;
	public final int INRMINOFFSET;
	public final int INRMAXOFFSET;
	public final int MINLEADERLENGTH;
	public final int MAXLEADERLENGTH;
	public final int MINTRAILERLENGTH;
	public final int MAXTRAILERLENGTH;
	
  private Compactor MOTIFCOMPACTOR = Compactor.BASECOMPACTOR;
	private float bestScore = Float.NaN; // theoretically best raw score for this
	private int startpos;
	private int endpos;
	private final int ACODE = MOTIFCOMPACTOR.charToIntId('a');
  private final int GCODE = MOTIFCOMPACTOR.charToIntId('g');
	private final int TCODE = MOTIFCOMPACTOR.charToIntId('t');
	private final int CCODE = MOTIFCOMPACTOR.charToIntId('c');
	private final byte[ ] ATAASTRAND = {3, 0, 3, 3};

	
/**
 * Constructs an instance, independently of Motifs.txt.
 * Parameters in RMotifdefaults in Configuration.txt may also be used.
 * @param	firststart	Internal position in DNA before after R is not allowed to start.
 * @param	lastend			Internal position in DNA after after R is not allowed to end.
 */
  public RMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
    frameDefined = false;
		showAsBases = true;
    parameters = new Hashtable();
    parameters.put(MINRLENGTHKEY, "30");
    parameters.put(MAXRLENGTHKEY, "300");
    parameters.put(TATAAMINOFFSETKEY, "-40");
    parameters.put(TATAAMAXOFFSETKEY, "-1");
    parameters.put(INRMINOFFSETKEY, "1");
    parameters.put(INRMAXOFFSETKEY, "100");
    parameters.put(MINTRAILERLENGTHKEY, "10");
    parameters.put(MAXTRAILERLENGTHKEY, "30");
    parameters.put(MINLEADERLENGTHKEY, "20");
    parameters.put(MAXLEADERLENGTHKEY, "300");
    getDefaults();
    MINRLENGTH = getInt(MINRLENGTHKEY, 30);
    MAXRLENGTH = getInt(MAXRLENGTHKEY, 300);
    TATAAMINOFFSET = getInt(TATAAMINOFFSETKEY, -40);
    TATAAMAXOFFSET = getInt(TATAAMAXOFFSETKEY, -1);
    INRMINOFFSET = getInt(INRMINOFFSETKEY, 1);
    INRMAXOFFSET = getInt(INRMAXOFFSETKEY, 100);
    MINTRAILERLENGTH = getInt(MINTRAILERLENGTHKEY, 10);
    MAXTRAILERLENGTH = getInt(MAXTRAILERLENGTHKEY, 30);
    MINLEADERLENGTH = getInt(MINLEADERLENGTHKEY, 20);
    MAXLEADERLENGTH = getInt(MAXLEADERLENGTHKEY, 300);
  } // end of constructor
	
	public void setEnds(int firststart, int lastend) {
		FIRSTSTART = firststart;
		LASTEND = lastend;
	} // end of setEnds


/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public Motif motifCopy() throws RetroTectorException {
    RMotif result = new RMotif();
    motifCopyHelp(result);
		result.setEnds(FIRSTSTART, LASTEND);
    return result;
  } // end of motifCopy
	
	private boolean inrHitAt(int inrpos) {
		if (inrpos < 0) {
			return false;
		}
		if (inrpos + 7 >= currentDNA.LENGTH) {
			return false;
		}
		int c;
		c = currentDNA.get2bit(inrpos);
		if ((c != TCODE) & (c != CCODE)) {
			return false;
		}
		c = currentDNA.get2bit(inrpos + 1);
		if ((c != TCODE) & (c != CCODE)) {
			return false;
		}
		c = currentDNA.get2bit(inrpos + 2);
		if (c != ACODE) {
			return false;
		}
		c = currentDNA.get2bit(inrpos + 4);
		if ((c != TCODE) & (c != ACODE)) {
			return false;
		}
		c = currentDNA.get2bit(inrpos + 5);
		if ((c != TCODE) & (c != CCODE)) {
			return false;
		}
		c = currentDNA.get2bit(inrpos + 6);
		if ((c != TCODE) & (c != CCODE)) {
			return false;
		}
		return true;
	} // end of inrHitAt
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public float getRawScoreAt(int pos) throws RetroTectorException {
		REndModifier mod = null;
		try {
			mod = currentDNA.getREndModifier();
		} catch (NullPointerException e) {
			throw e;
		}
		endpos = -1;
		float maxmod = -10000;
		int ci = Math.min(pos + MAXTRAILERLENGTH, LASTEND) -1;
		for (int i=pos+MINTRAILERLENGTH; i<ci; i++) {
			if (mod.modification(i) >= maxmod) {
				endpos = i;
				maxmod = mod.modification(i);
			}
			if (mod.modification(ci) >= maxmod) {
				endpos = ci;
				maxmod = mod.modification(ci);
			}
			ci--;
		}
		
		int soffset = Math.max(FIRSTSTART, endpos - MAXRLENGTH);
		int slength = endpos - MINRLENGTH - soffset + 1;
		if (slength < 2) {
			return Float.NaN;
		}
		int ind;
		float[ ] s = new float[slength];
		for (int j=currentDNA.forceInside(soffset + TATAAMINOFFSET); j<=currentDNA.forceInside(soffset+slength+TATAAMAXOFFSET); j++) {
			if (currentDNA.patternAt(j + 1, ATAASTRAND)) {
				if ((currentDNA.get2bit(j) == TCODE) | (currentDNA.get2bit(j) == CCODE)) {
					for (int k=TATAAMINOFFSET; k<=TATAAMAXOFFSET; k++) {
						ind = j - k - soffset;
						if ((ind >= 0) & (ind < s.length)) {
							s[ind] = 2;
						}
					}
				}
			}
		}
				
		for (int j=currentDNA.forceInside(soffset + INRMINOFFSET); j<=currentDNA.forceInside(soffset+slength+INRMAXOFFSET); j++) {
			if (inrHitAt(j)) {
				for (int k=INRMINOFFSET; k<=INRMAXOFFSET; k++) {
					ind = j - k - soffset;
					if ((ind >= 0) & (ind < s.length)) {
						s[ind] +=1;
					}
				}
			}
		}
		startpos = -1;
		float maxmod2 = -10000;
		for (int l=0; l<s.length; l++) {
			if ((l + soffset < currentDNA.LENGTH - 1) && (currentDNA.get2bit(l + soffset) == GCODE) && (currentDNA.get2bit(l + soffset + 1) == CCODE)) {
				s[l] *= 2;
			}
			if (s[l] > maxmod2) {
				startpos = l + soffset;
				maxmod2 = s[l];
			}
		}
				
  	return maxmod * maxmod2;
  } // end of getRawScoreAt

/**
* @param	pos	position to match at.
*	@return getRawScoreAt(pos)
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
  	return getRawScoreAt(pos);
  } // end of refreshScoreAt

/**
* @return	The highest raw score accepted.
*/
  public final float getBestRawScore() {
    return bestScore;
  } // end of getBestRawScore
  
/**
* @return 0.
*/
 	public final int getBasesLength() {
  	return 0;
  } // end of getBasesLength
  
/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold and rawFactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
  } // end of refresh(RefreshInfo)

/**
* @param	position	An (internal) position in DNA.
* @return	A hit by this Motif if there is one at position, otherwise null.
*/
  protected MotifHit makeMotifHitAt(int position) throws RetroTectorException {
		float x = getRawScoreAt(position);
		if (endpos - startpos < MINRLENGTH - 1) {
			return null;
		}
		return new MotifHit(this, x, position, startpos, endpos, currentDNA);
  } // end of makeMotifHitAt
  
} // end of RMotif
