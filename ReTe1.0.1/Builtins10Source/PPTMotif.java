/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 1/10 -06
* Beautified 1/10 -06
*/
package builtins;

import retrotector.*;

import java.util.*;

/**
* Class representing polypurine tract Motifs.
* Hot spot is at first base.
*<PRE>
*     Parameters:
*
*   LowLimit
* Raw score threshold.
* Default: 20.
*
*   HighLimit
* Presumed maximum raw score.
* Default: 90.
*
*   TgBonus
* Raw score bonus for subsequent 'tg'.
* Default: 5
*</PRE>
*/
public class PPTMotif extends OrdinaryMotif {

/**
* Key for parameter defining raw score threshold = "LowLimit".
* Default value is 20.
*/
	public static final String LOWLIMITKEY = "LowLimit";

/**
* Key for parameter defining presumed maximum raw score = "HighLimit".
* Default value is 90.
*/
	public static final String HIGHLIMITKEY = "HighLimit";

/**
* Key for parameter defining raw score for subsequent 'tg' = "TgBonus".
* Default value is 5.
*/
	public static final String TGBONUSKEY = "TgBonus";


/**
* @return String identifying this class = "PPT".
*/
	public static final String classType() {
		return SubGene.PPT;
	} // end of classType()

	private float tgbonus; // raw score for subsequent 'tg'

  private Compactor MOTIFCOMPACTOR = Compactor.BASECOMPACTOR;
	private float bestScore = Float.NaN; // theoretically best raw score for this
	private int endpos = Integer.MIN_VALUE; // end position of latest hit
	private final int ACODE = MOTIFCOMPACTOR.charToIntId('a');
  private final int GCODE = MOTIFCOMPACTOR.charToIntId('g');
	private final int TCODE = MOTIFCOMPACTOR.charToIntId('t');
	
/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt.
* Parameters in PPTMotifdefaults in Configuration.txt may also be used.
*/
  public PPTMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch", MOTIFTYPE, classType());
		}
    frameDefined = false;
		showAsBases = true;
    parameters = new Hashtable();
    parameters.put(LOWLIMITKEY, "20");
    parameters.put(HIGHLIMITKEY, "90");
    parameters.put(TGBONUSKEY, "5");
    getDefaults();
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    PPTMotif result = new PPTMotif();
    motifCopyHelp(result);
    result.bestScore = bestScore;
    result.tgbonus = tgbonus;
    return result;
  } // end of motifCopy
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - 15))) {
      return Float.NaN;
    }
    int sum = 0;
  	boolean b;
  	int twobits;
  	if (pos > 0) {
  		twobits = currentDNA.get2bit(pos - 1); // is there 'a' in position before?
  		if (twobits == ACODE) {
        return -1; // yes, hit does not begin here
      }
  	}
   	twobits = currentDNA.get2bit(pos + 1);
  	b = (twobits == ACODE) || (twobits == GCODE);
 		if ((currentDNA.get2bit(pos) != ACODE) || !b) { // is this 'a' and next 'a' or 'g'?
 			return -1; // no, not a hit here
 		}
  	int errcount = 0; // counter for non-ag, may not exceed 1
  	endpos = pos;
  	twobits = currentDNA.get2bit(endpos);
  	b = (twobits == ACODE) || (twobits == GCODE);
  	while ((endpos < (currentDNA.LENGTH - 1)) && (b || (errcount == 0)) ) {
  		if (!b) {
  			errcount++;
  		}
  		endpos++;
  		twobits = currentDNA.get2bit(endpos);
  		b = (twobits == ACODE) || (twobits == GCODE);
  	}
  	sum = 0;
  	for (int a=pos; a<(endpos-1); a++) {
  		if (currentDNA.get2bit(a) == ACODE) { // for each 'a'
  			for (int g=a+1; g<endpos; g++) { // add the number of 'g' after it
  				if (currentDNA.get2bit(g) == GCODE) {
  					sum++;
  				}
  			}
  		}
  	}
  	boolean found = false;
// search for following 'tg'
  	for (int ppo=endpos; ppo<=Math.min(endpos+5, currentDNA.LENGTH-1); ppo++) {
  		if ((currentDNA.get2bit(ppo - 1) == TCODE) && (currentDNA.get2bit(ppo) == GCODE) && !found) {
  			found = true;
  			endpos = ppo;
  			sum += tgbonus;
  		}
  	}
  	return sum;
  } // end of getRawScoreAt(int)

/**
* @param	pos	position to match at.
*	@return getRawScoreAt(pos)
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
  	return getRawScoreAt(pos);
  } // end of refreshScoreAt()

/**
* @return	The highest raw score accepted.
*/
  public final float getBestRawScore() {
    return bestScore;
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
    rawThreshold = getFloat(LOWLIMITKEY, 20);
    bestScore = getFloat(HIGHLIMITKEY, 90);
    tgbonus = getFloat(TGBONUSKEY, 5);
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
  protected MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    float sc = (getRawScoreAt(position) - rawThreshold) * rawFactor;
    if (sc <= 0) {
      return null;
    } else {
      return new MotifHit(this, Math.min(sc, MAXSCORE), position, position, endpos, currentDNA);
    }
  } // end of makeMotifHitAt(int)
  
} // end of PPTMotif
