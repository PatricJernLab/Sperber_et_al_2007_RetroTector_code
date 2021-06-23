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

/**
* Class representing protease cleavage site.
* Scores a number of acid strings like AcidMotif, and chooses the best.
* The acid strings are specified blank separated in MOTIFSTRING.
* Hot spot is at second acid.
*/
public class ProteaseCleavageMotif extends OrdinaryMotif {

/**
* @return String identifying this class = "PCS".
*/
	public static final String classType() {
		return "PCS";
	} // end of classType()

	private final byte[ ][ ] MOTIFSTRANDS;
	private final float[ ] TOPSCORES; // max score for each of MOTIFSTRANDS
	
	private float cfactor; // bonus factor for conserved acids
  private AcidMatrix MOTIFMATRIX = Matrix.acidMATRIX;
	private final int MASK = 63;
  private Compactor MOTIFCOMPACTOR = Compactor.ACIDCOMPACTOR;
	
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt.
 */
  public ProteaseCleavageMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch", MOTIFTYPE, classType());
		}
		
		String[ ] ss = Utilities.splitString(MOTIFSTRING);
		MOTIFSTRANDS = new byte[ss.length][];
   	for (int i=0; i<MOTIFSTRANDS.length; i++) {
			MOTIFSTRANDS[i] = acidPattern(ss[i], MOTIFCOMPACTOR, MOTIFID);
		}
		TOPSCORES = new float[MOTIFSTRANDS.length];
		float bS;
		for (int h=0; h<TOPSCORES.length; h++) {
			bS = 0;
			for (int i=0; i<MOTIFSTRANDS[h].length; i++) {
				if (MOTIFSTRANDS[h][i] >= 0) {
					bS += MOTIFMATRIX.floatMatrix[ MOTIFSTRANDS[h][i]][ MOTIFSTRANDS[h][i] & MASK];
				}
			}
			TOPSCORES[h] = bS;
		}
		
    frameDefined = true;
		showAsBases = false;
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    ProteaseCleavageMotif result = new ProteaseCleavageMotif();
    motifCopyHelp(result);
    result.cfactor = cfactor;
    return result;
  } // end of motifCopy()
  
/**
* Not in use at present.
* @param	targetString	String to evaluate this at position
* @param	posInTarget		Index in targetStringto evaluate at.
* @return	Resulting normalized score
*/
	public final float scoreInAcidString(String targetString, int posInTarget) {
		float f = -Float.MAX_VALUE;
		for (int i=0; i<MOTIFSTRANDS.length; i++) {
			f = Math.max(f, AcidMatrix.scoreInAcidString(MOTIFSTRANDS[i], targetString, posInTarget, cfactor, 0)) / TOPSCORES[i];
		}
		return f;
	} // end of scoreInAcidString(String, int)
	
// ambiguous acids = -1, ardinary acids 0-63, conserved acids = 64-127
 
 /**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
		float f = -Float.MAX_VALUE;
		for (int i=0; i<MOTIFSTRANDS.length; i++) {
			f = -Float.MAX_VALUE;
			if ((pos >= 3) & (pos < (currentDNA.LENGTH - (MOTIFSTRANDS[i].length - 1) * 3))) {
				f = Math.max(f, currentDNA.acidRawScoreAt(MOTIFSTRANDS[i], pos - 3, cfactor, 0)) / TOPSCORES[i];
			}
		}
		if (f == -Float.MAX_VALUE) {
			f = Float.NaN;
		}
		return f;
  } // end of getRawScoreAt(int)
	
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or NaN if the position is unacceptable or an ambiguous codon is encountered.
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
		float f = -Float.MAX_VALUE;
		float g;
		for (int i=0; i<MOTIFSTRANDS.length; i++) {
			f = -Float.MAX_VALUE;
			if ((pos >= 3) & (pos < (currentDNA.LENGTH - (MOTIFSTRANDS[i].length - 1) * 3))) {
				g = currentDNA.acidRefreshScoreAt(MOTIFSTRANDS[i], pos - 3, cfactor, 0);
				if (Float.isNaN(g)) {
					return Float.NaN;
				}
				f = Math.max(f, g / TOPSCORES[i]);
			}
		}
		if (f == -Float.MAX_VALUE) {
			f = Float.NaN;
		}
		return f;
  } // end of refreshScoreAt(int)


/**
* @return	The highest raw score obtainable = 1.
*/
  public final float getBestRawScore() {
    return 1.0f;
  } // end of getBestRawScore()
    
/**
 * @return	The number of acids * 3 in the Motif.
 */
 	public final int getBasesLength() {
		int l = 0;
		for (int i= 0; i<MOTIFSTRANDS.length; i++) {
			l = Math.max(l, MOTIFSTRANDS[i].length * 3);
		}
		return l;
  } // end of getBasesLength()

/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold rawFactor and cfactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    MOTIFMATRIX = AcidMatrix.refreshAcidMatrix(theInfo.CFACTOR);
    cfactor = theInfo.CFACTOR;
    super.refresh(theInfo);
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
      return new MotifHit(this, sc, position, position - 3, position + getBasesLength() - 4, currentDNA);
    }
  } // end of makeMotifHitAt(int)

} // end of ProteaseCleavageMotif
