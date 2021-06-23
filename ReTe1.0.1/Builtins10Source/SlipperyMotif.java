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

/**
* Class representing slippery sequence.
* Sequence model is (aaa|ggg|ttt)(aaa|ttt)(a|c|t).
* Only exact fits to this model are accepted.
* Hot spot is at first base.
*/
public class SlipperyMotif extends OrdinaryMotif {

/**
* @return String identifying this class = "SLI".
*/
	public static final String classType() {
		return "SLI";
	} // end of classType()

  private Compactor MOTIFCOMPACTOR = Compactor.BASECOMPACTOR;
	private float bestScore = 1.0f; // theoretically best raw score for this
	
	private int acode = MOTIFCOMPACTOR.charToIntId('a');
	private int ccode = MOTIFCOMPACTOR.charToIntId('c');
	private int gcode = MOTIFCOMPACTOR.charToIntId('g');
	private int tcode = MOTIFCOMPACTOR.charToIntId('t');
	
	private int aaacode = acode * 16 + acode * 4 + acode;
	private int gggcode = gcode * 16 + gcode * 4 + gcode;
	private int tttcode = tcode * 16 + tcode * 4 + tcode;
	
	
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt.
 */
  public SlipperyMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch", MOTIFTYPE, classType());
		}
    frameDefined = false;
		showAsBases = true;
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    SlipperyMotif result = new SlipperyMotif();
    motifCopyHelp(result);
    result.bestScore = bestScore;
    return result;
  } // end of motifCopy()
  
/**
* Returns the raw score at a particular (internal) position in current DNA,
* or NaN if the position is unacceptable.
* endpos is set.
* @param	pos			position (internal in DNA) to match at.
*/
  public float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - 8))) {
      return Float.NaN;
    }
		int code = currentDNA.get6bit(pos);
		if ((code != aaacode) & (code != gggcode) & (code != tttcode)) {
			return 0.0f;
		}
		code = currentDNA.get6bit(pos + 3);
		if ((code != aaacode) & (code != tttcode)) {
			return 0.0f;
		}
		code = currentDNA.get2bit(pos + 6);
		if (code == gcode) {
			return 0.0f;
		}
  	return bestScore;
  } // end of getRawScoreAt(int)

/**
* @param	pos	Position (internal in DNA) to match at
* @return	getRawScoreAt(pos).
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
  	return getRawScoreAt(pos);
  } // end of refreshScoreAt(int)

/**
* @return	The highest raw score obtainable = 1.
*/
  public final float getBestRawScore() {
    return bestScore;
  } // end of getBestRawScore()
  
/**
 * @return 7.
 */
 	public final int getBasesLength() {
  	return 7;
  } // end of getBasesLength()
  
/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold and rawFactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
    rawThreshold = 0.5f;
    rawFactor = MAXSCORE / (getBestRawScore() - rawThreshold);
  	if (rawFactor <= 0) { // cannot happen, of course
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
      return new MotifHit(this, Math.min(sc, MAXSCORE), position, position, position + 6, currentDNA);
    }
  } // end of makeMotifHitAt(int)
  
} // end of SlipperyMotif
