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
* Motif for consensus splice donor sequence
* C/A A G!G T A/G A G T.<BR>
* The maximum nubmer of non-conforming bases is 2, but mey be redefined using the parameter MaxErrors.
*/
public class SpliceDonorMotif extends OrdinaryMotif {

/**
* Key for parameter defining maximal number of errors accepted = "MaxErrors".
* Default value is 2.
*/
	public static final String MAXERRORSKEY = "MaxErrors";
	
/**
* @return String identifying this class = "SPD".
*/
	public static final String classType() {
		return "SPD";
	} // end of classType
		

  static private Compactor MOTIFCOMPACTOR = Compactor.BASECOMPACTOR;
	
	static private int acode = MOTIFCOMPACTOR.charToIntId('a');
	static private int ccode = MOTIFCOMPACTOR.charToIntId('c');
	static private int gcode = MOTIFCOMPACTOR.charToIntId('g');
	static private int tcode = MOTIFCOMPACTOR.charToIntId('t');
	static private int[ ] backbone = new int[9];
	
	static {
		backbone[0] = ccode;
		backbone[1] = acode;
		backbone[2] = gcode;
		backbone[3] = gcode;
		backbone[4] = tcode;
		backbone[5] = acode;
		backbone[6] = acode;
		backbone[7] = gcode;
		backbone[8] = tcode;
	}
	
	private int maxErrors;
	private float bestScore = Float.NaN; // theoretically best raw score for this
	
/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt.
* Parameters in SpliceDonorMotifdefaults in Configuration.txt may also be used.
*/
  public SpliceDonorMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch", MOTIFTYPE, classType());
		}
    frameDefined = false;
		showAsBases = true;
    parameters = new Hashtable();
    parameters.put(MAXERRORSKEY, "2");
    getDefaults();
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    SpliceDonorMotif result = new SpliceDonorMotif();
    motifCopyHelp(result);
    result.bestScore = bestScore;
    result.maxErrors = maxErrors;
    return result;
  } // end of motifCopy()
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or 0 if the position is unacceptable.
*/
  public float getRawScoreAt(int pos) throws RetroTectorException {
		int errcount = 0;
		if ((pos < 3) | (pos >= (currentDNA.LENGTH - 7))) {
      return 0;
    }
		int c = currentDNA.get2bit(pos - 3);
		if (c == acode) {
			errcount--;
		}
		c = currentDNA.get2bit(pos + 2);
		if (c == gcode) {
			errcount--;
		}
		for (int i=0; i<backbone.length; i++) {
			if (currentDNA.get2bit(pos - 3 + i) != backbone[i]) {
				errcount++;
				if (errcount > maxErrors) {
					return 0;
				}
			}
		}
		return maxErrors + 1 - errcount;
	} // end of getRawScoreAt

/**
* @param	pos	Position (internal in DNA) to match at
* @return	getRawScoreAt(pos).
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
  	return getRawScoreAt(pos);
  } // end of refreshScoreAt(int)

/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
    return bestScore;
  } // end of getBestRawScore()
  
/**
 * @return 9.
 */
 	public final int getBasesLength() {
  	return 9;
  } // end of getBasesLength()
  
/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold and rawFactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
    maxErrors = getInt(MAXERRORSKEY, 2);
    bestScore = maxErrors + 1;
    rawThreshold = 0f;
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
      return new MotifHit(this, Math.min(sc, MAXSCORE), position, position - 3, position + 5, currentDNA);
    }
  } // end of makeMotifHitAt(int)

} // end of SpliceDonorMotif
