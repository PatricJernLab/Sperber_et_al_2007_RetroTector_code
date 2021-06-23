/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 30/9 -06
* Beautified 30/9 -06
*/
package builtins;

import retrotector.*;
import java.util.*;

/**
* Motif for hydrophobic region in Gag. SDfactor is normally 3, but may be changed through
* HyPhobMotifdefaults with parameter SDFactor. Hotspot is at beginning.
*/
public class HyPhobMotif extends OrdinaryMotif {
  
/**
* Minimum number of amino acids in motif hit = 12.
*/
  static final int MINLENGTH = 12;
  
/**
* Maximum number of amino acids in motif hit = 27.
*/
  static final int MAXLENGTH = 27;
  
/**
* @return	String identifying this class = "HYF".
*/
	public static final String classType() {
		return "HYF";
	} // end of classType
	
  private int bestLength; // length used for most recent hit
  
/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
*/
  public HyPhobMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}

    frameDefined = true;
    parameters = new Hashtable();
    parameters.put(Executor.SDFACTORKEY, "3");
    getDefaults();
    
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    HyPhobMotif result = new HyPhobMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()

/**
* Raw score.
*	bestLength is set to the hit length giving the best score.
* @param	pos	Position (internal in DNA) to match at.
* @return The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - 3 * MAXLENGTH))) {
      return Float.NaN;
    }
    float maxsc = 0;
    bestLength = 0;
    int errcount;
    int acid;
    float sc;
    for (int len=MINLENGTH; len<=MAXLENGTH; len++) {
      errcount = 0;
      acid = currentDNA.getAcid(pos);
// first acid should be f or w
      if ((acid != 'f') && (acid != 'w')) {
        errcount++;
      }
// following acids should be l, v, i, a or g
      for (int f=1; f<len-6; f++) {
        acid = currentDNA.getAcid(pos + 3 * f);
        if ((acid != 'l') && (acid != 'v') && (acid != 'i') && (acid != 'a') && (acid != 'g')) {
          errcount++;
        }
      }
// last 6 acids should be 'l', 'v', 'i', 'a', 'g', 'c', 'w', 'y'or 'r'
      for (int ff=len-6; ff<len; ff++) {
        acid = currentDNA.getAcid(pos + 3 * ff);
        if ((acid != 'l') && (acid != 'v') && (acid != 'i') && (acid != 'a') && (acid != 'g') && (acid != 'c') && (acid != 'w') && (acid != 'y') && (acid != 'r')) {
          errcount++;
        }
      }
      sc = 1 - 1.0f * errcount / MINLENGTH;
      if (sc > maxsc) {
        maxsc = sc;
        bestLength = len;
      }
    }
    return maxsc;
  } // end of getRawScoreAt(int)

/**
* @param	pos	Position (internal in DNA) to match at.
* @return	getRawScoreAt(pos).
*/
  protected final float refreshScoreAt(int pos ) throws RetroTectorException {
    return getRawScoreAt(pos);
  } // end of refreshScoreAt(int)

/**
* @return	The highest raw score obtainable = 1.
*/
  public final float getBestRawScore() {
    return 1.0f;
  } // end of getBestRawScore()
  

/**
 * @return 0.
 */
 	public final int getBasesLength() {
  	return 0;
  } // end of getBasesLength()

/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold and rawFactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
    float sdf = getFloat(Executor.SDFACTORKEY, 3.0f);
    if (!Float.isNaN(fixedThreshold)) {
      rawThreshold = fixedThreshold;
    } else {
      rawThreshold = calculateRawThreshold(sdf, theInfo.CFACTOR);
    }
    rawFactor = MAXSCORE / (getBestRawScore() - Math.min(rawThreshold, 0.99f * getBestRawScore()));
  	if (rawFactor <= 0) { // should not be possible, but what the hell
  		RetroTectorEngine.displayError(new RetroTectorException("OrdinaryMotif", "Threshold=" +
  				rawThreshold + "> best possible score", "Motif nr " + MOTIFID), RetroTectorEngine.WARNINGLEVEL);
  	}
  } // end of refresh(RefreshInfo)
  
/**
* @param	position	An (internal) position in DNA.
* @return	A hit by this Motif if there is one at position, otherwise null.
*/
  protected MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    float sc = (getRawScoreAt(position) - rawThreshold) * rawFactor;
    if (sc <= 0) {
      return null;
    } else {
      return new MotifHit(this, sc, position, position, position + 3 * bestLength - 1, currentDNA);
    }
  } // end of makeMotifHitAt(int)

} // end of HyPhobMotif
