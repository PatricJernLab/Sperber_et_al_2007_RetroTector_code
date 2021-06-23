/*
* Copyright (©) 2000-2065, Gšran Sperber. All Rights Reserved.
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
* Class representing putein starts.
* Scores a number of acid strings like AcidMotif, and chooses the best.
* The acid strings are fetched from an alignment file.
*/
public class PuteinStartMotif extends OrdinaryMotif {

/**
* @return String identifying this class = "PUS".
*/
	public static final String classType() {
		return "PUS";
	} // end of classType()
	
/**
* Length of acid strings to fetch = 10.
*/
	public static final int PUTEINSTARTLENGTH = 10;

/**
* ROWORIGIN of Alignment row that produced the latest scoreInAcidString();
*/
	public String latestRowOrigin = null;
	
	private final byte[ ][ ] MOTIFSTRANDS;
	private final float[ ] TOPSCORES; // max score for each of MOTIFSTRANDS
	private final String[ ] ROWNAMES;
	
	private float cfactor; // bonus factor for conserved acids
  private AcidMatrix MOTIFMATRIX = Matrix.acidMATRIX;
	private final int MASK = 63;
  private Compactor MOTIFCOMPACTOR = Compactor.ACIDCOMPACTOR;
	
/**
* Constructs an instance.
* @param	database	Database to use.
* @param	motifID		Id number for error messages.
* @param	rvGenus		Character of virus genus.
* @param	alignmentName	Name of Alignment file to use
*/
  public PuteinStartMotif(Database database, int motifID, char rvGenus, String alignmentName) throws RetroTectorException {
   	super(database, motifID, classType(), "D", String.valueOf(rvGenus), 5.5f, null, null, 100.0f, "", alignmentName, "");
		
		Alignment ali = new Alignment(database.getFile(alignmentName));
		MOTIFSTRANDS = new byte[ali.nrOfRows()][PUTEINSTARTLENGTH];
		ROWNAMES = new String[ali.nrOfRows()];
		String s;
   	for (int i=0; i<MOTIFSTRANDS.length; i++) {
			s = ali.getRow(i).getLeadingString(PUTEINSTARTLENGTH);
			if (s != null) {
				MOTIFSTRANDS[i] = acidPattern(s, MOTIFCOMPACTOR, MOTIFID);
				ROWNAMES[i] = ali.getRow(i).ROWORIGIN;
			}
		}
		TOPSCORES = new float[MOTIFSTRANDS.length];
		float bS;
		for (int h=0; h<TOPSCORES.length; h++) {
			if (MOTIFSTRANDS[h] != null) {
				bS = 0;
				for (int i=0; i<MOTIFSTRANDS[h].length; i++) {
					if (MOTIFSTRANDS[h][i] >= 0) {
						bS += MOTIFMATRIX.floatMatrix[ MOTIFSTRANDS[h][i]][ MOTIFSTRANDS[h][i] & MASK];
					}
				}
				TOPSCORES[h] = bS;
			}
		}
		
    frameDefined = true;
		showAsBases = false;
  } // end of constructor(Database, int, char, String)

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
		throw new RetroTectorException("PuteinStartMotif cannot be copied");
	} // end of motifCopy()
  
/**
* @param	targetString	String to evaluate this at position
* @param	posInTarget
* @return	Resulting normalized score (ie <= 1).
*/
	public final float scoreInAcidString(String targetString, int posInTarget) {
		while (targetString.length() < PUTEINSTARTLENGTH) {
			targetString = targetString + Compactor.UNKNOWNCHAR;
		}
		float f = -Float.MAX_VALUE;
		float ff;
		for (int i=0; i<MOTIFSTRANDS.length; i++) {
			if (MOTIFSTRANDS[i] != null) {
				ff = AcidMatrix.scoreInAcidString(MOTIFSTRANDS[i], targetString, posInTarget, cfactor, 0) / TOPSCORES[i];
				if (ff > f) {
					f = ff;
					latestRowOrigin = ROWNAMES[i];
				}
			}
		}
		return f;
	} // end of scoreInAcidString(String, int)
	
// ambiguous acids = -1, ardinary acids 0-63, conserved acids = 64-127
 
/**
* Not in use at present.
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
		float f = -Float.MAX_VALUE;
		for (int i=0; i<MOTIFSTRANDS.length; i++) {
			if ((MOTIFSTRANDS[i] != null) && (pos < (currentDNA.LENGTH - MOTIFSTRANDS[i].length * 3))) {
				f = Math.max(f, currentDNA.acidRawScoreAt(MOTIFSTRANDS[i], pos, cfactor, 0)) / TOPSCORES[i];
			}
		}
		if (f == -Float.MAX_VALUE) {
			f = Float.NaN;
		}
		return f;
  } // end of getRawScoreAt
	
/**
* Not in use at present.
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or NaN if the position is unacceptable or an ambiguous codon is encountered.
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
		float f = -Float.MAX_VALUE;
		float g;
		for (int i=0; i<MOTIFSTRANDS.length; i++) {
			if ((MOTIFSTRANDS[i] != null) && (pos < (currentDNA.LENGTH - MOTIFSTRANDS[i].length * 3))) {
				g = currentDNA.acidRefreshScoreAt(MOTIFSTRANDS[i], pos, cfactor, 0);
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
  } // end of refreshScoreAt


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
		return PUTEINSTARTLENGTH * 3;
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
      return new MotifHit(this, sc, position, position, position + getBasesLength() - 1, currentDNA);
    }
  } // end of makeMotifHitAt(int)

} // end of PuteinStartMotif
