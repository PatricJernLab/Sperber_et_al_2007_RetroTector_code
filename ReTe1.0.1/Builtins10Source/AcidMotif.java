/*
* Copyright (©) 2000-2006, Gšran Sperber & Tore Eriksson. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Tore Eriksson & Gšran Sperber
* @version 24/9 -06
* Beautified 24/9 -06
*/
package builtins;

import retrotector.*;

/**
* Class defining a Motif consisting of amino acids.
* Hot spot is at first base of first acid. MOTIFSTRAND consists of amino acid symbols,
* uppercase for highly conserved acids.
*/
public class AcidMotif extends OrdinaryMotif {

/**
* @return	String identifying this class = "P".
*/
	public static final String classType() {
		return "P";
	} // end of classType()
	
// ambiguous acids = -1, ardinary acids 0-63, conserved acids = 64-127
  private final byte[ ] MOTIFSTRAND;
	
  private float cfactor; // bonus for conserved acids
  private AcidMatrix MOTIFMATRIX = Matrix.acidMATRIX;
	private final int MASK = 63; // low 6 bits
  private final Compactor MOTIFCOMPACTOR = Compactor.ACIDCOMPACTOR;

/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
*/
  public AcidMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}

   	MOTIFSTRAND = acidPattern(MOTIFSTRING, MOTIFCOMPACTOR, MOTIFID);
    frameDefined = true;
		exactlyAligned = true;
    
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    AcidMotif result = new AcidMotif();
    motifCopyHelp(result);
    result.cfactor = cfactor;
    return result;
  } // end of motifCopy()

/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - MOTIFSTRAND.length * 3))) {
      return Float.NaN;
    }
  	return currentDNA.acidRawScoreAt(MOTIFSTRAND, pos, cfactor, 0);
  } // end of getRawScoreAt(int)
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or NaN if the position is unacceptable or an ambiguous codon is encountered.
*/
  protected final float refreshScoreAt(int pos ) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - MOTIFSTRAND.length * 3))) {
      return Float.NaN;
    }
  	return currentDNA.acidRefreshScoreAt(MOTIFSTRAND, pos, cfactor, 0);
  } // end of refreshScoreAt(int)

/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
		float bestScore = 0;
    for (int i=0; i<MOTIFSTRAND.length; i++) {
    	if (MOTIFSTRAND[i] >= 0) {
      	bestScore += MOTIFMATRIX.floatMatrix[ MOTIFSTRAND[i]][ MOTIFSTRAND[i] & MASK];
      }
		}
    return bestScore;
  } // end of getBestRawScore()
  
/**
* @return	The number of acids * 3 in the Motif.
*/
 	public final int getBasesLength() {
  	return MOTIFSTRAND.length * 3;
  } // end of getBasesLength()

/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold, rawFactor and cfactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    MOTIFMATRIX = AcidMatrix.refreshAcidMatrix(theInfo.CFACTOR);
    cfactor = theInfo.CFACTOR;
    super.refresh(theInfo);
  } // end of refresh(RefreshInfo)
  
} // end of AcidMotif
