/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 5/10 -06
* Beautified 5/10 -06
*/
package builtins;

import retrotector.*;

import java.util.*;

/**
* Class representing consensus splice acceptor.
* <BR>
*	14*(T/C) N T/C A G<BR>
* where the 14 may be redefined through the parameter LeadingTC.<BR>
* Raw score is number of T/C in tail.<BR>
* Threshold is 0.65 of max raw score.<BR>
* Hot spot is after last base.<BR>
*/
public class SpliceAcceptorMotif extends OrdinaryMotif {

/**
* Key for parameter defining least number of leading T/C = "LeadingTC".
* Default value is 14.
*/
	public static final String LEADINGTCKEY = "LeadingTC";


/**
* @return String identifying this class = "SPL".
*/
	public static final String classType() {
		return "SPL";
	} // end of classType()

	private int leadingTC;

  private Compactor MOTIFCOMPACTOR = Compactor.BASECOMPACTOR;
	private float bestScore = Float.NaN; // theoretically best raw score for this
	
	private int acode = MOTIFCOMPACTOR.charToIntId('a');
	private int ccode = MOTIFCOMPACTOR.charToIntId('c');
	private int gcode = MOTIFCOMPACTOR.charToIntId('g');
	private int tcode = MOTIFCOMPACTOR.charToIntId('t');
	
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt.
 * Parameters in SpliceAcceptorMotifDefaults in Configuration.txt may also be used.
 */
  public SpliceAcceptorMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch", MOTIFTYPE, classType());
		}
    frameDefined = false;
		showAsBases = true;
    parameters = new Hashtable();
    parameters.put(LEADINGTCKEY, "14");
    getDefaults();
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    SpliceAcceptorMotif result = new SpliceAcceptorMotif();
    motifCopyHelp(result);
    result.bestScore = bestScore;
    result.leadingTC = leadingTC;
    return result;
  } // end of motifCopy()
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or 0 if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < leadingTC + 4) | (pos >= (currentDNA.LENGTH - 1))) {
      return 0;
    }
		if ((currentDNA.get2bit(pos - 3) != ccode) & (currentDNA.get2bit(pos - 3) != tcode)) {
			return 0;
		}
		if (currentDNA.get2bit(pos - 2) != acode) {
			return 0;
		}
		if (currentDNA.get2bit(pos - 1) != gcode) {
			return 0;
		}
		
    int sum = 0;
		int te;
		for (int i=1; i<=leadingTC; i++) {
			te = currentDNA.get2bit(pos - 4 - i);
			if ((te == tcode) | (te == ccode)) {
				sum++;
			}
		}
  	return sum;
  } // end of getRawScoreAt(int)

/**
* @param	pos	Position (internal in DNA) to match at
* @return	getRawScoreAt(pos).
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
  	return getRawScoreAt(pos);
  } // end of refreshScoreAt()

/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
    return bestScore;
  } // end of getBestRawScore()
  
/**
 * @return 0.
 */
 	public final int getBasesLength() {
  	return 5 + leadingTC;
  } // end of getBasesLength()
  
/**
* Reset Motif with new parameters.
* leadingTC, currentDNA, rawThreshold and rawFactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
    leadingTC = getInt(LEADINGTCKEY, 14);
    bestScore = leadingTC;
    rawThreshold = bestScore * 0.65f;
    rawFactor = MAXSCORE / (getBestRawScore() - rawThreshold);
  	if (rawFactor <= 0) {
  		RetroTectorEngine.displayError(new RetroTectorException("OrdinaryMotif", "Threshold=" +
  				rawThreshold + "> best possible score", "Motif nr " + MOTIFID), RetroTectorEngine.WARNINGLEVEL);
  	}
  } // end of refresh(RefreshInfo)

/**
* @param	position	An (internal) position in DNA.
* @return	A hit by this Motif if there is one at position, otherwise null.
*/
  protected final MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    float sc = (getRawScoreAt(position) - rawThreshold) * rawFactor;
    if (sc <= 0) {
      return null;
    } else {
      return new MotifHit(this, Math.min(sc, MAXSCORE), position, position - 4 - leadingTC, position - 1, currentDNA);
    }
  } // end of makeMotifHitAt(int)
  
} // end of SpliceAcceptorMotif
