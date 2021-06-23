/*
* Copyright (©) 2000-2006, Gšran Sperber & Tore Eriksson. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Tore Eriksson & Gšran Sperber
* @version 30/9 -06
* Beautified 30/9 -06
*/
package builtins;

import retrotector.*;

/**
* Class defining a Motif consisting of DNA bases.
* Hotspot is at first base.
*/
public class BaseMotif extends OrdinaryMotif {

/*
* Contains the sequence data one position/byte. Bit 3 set if position conserved.
* -1 if ambiguous base.
*/ 
  public final byte[ ] MOTIFSTRAND;

  private float cfactor; // bonus for conserved base
  private BaseMatrix MOTIFMATRIX = Matrix.baseMATRIX;
	private final int MASK = 3; // low 2 bits
  private final Compactor MOTIFCOMPACTOR = Compactor.BASECOMPACTOR;

/**
* @return String identifying this class = "N".
*/
	public static final String classType() {
		return "N";
	} // end of classType()

/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
*/
  public BaseMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}

   	MOTIFSTRAND = new byte[MOTIFSTRING.length()];
// code characters into strand
		int xi;
    for (int i=0; i<MOTIFSTRAND.length; i++) {
    	xi = MOTIFCOMPACTOR.charToIntId(MOTIFSTRING.charAt(i));
    	if (xi < 0) {
    		RetroTectorException.sendError(this, "Illegal character in Motif " + MOTIFID, MOTIFSTRING.charAt(i) + " in", MOTIFSTRING);
    	} else if (xi > MASK) {
    		MOTIFSTRAND[i] = -1; // ambiguous base in strand
    	} else {
      	if (Character.isUpperCase(MOTIFSTRING.charAt(i))) {
					xi |= (MASK + 1); // Set bit to mark conserved position
      	}
      	MOTIFSTRAND[i] = (byte) xi;
      }
    }
    frameDefined = true;
		showAsBases = true;
  } // end of constructor()

/**
* @return	The number of bases in the Motif.
*/
 	public final int getBasesLength() {
  	return MOTIFSTRAND.length;
  } // end of getBasesLength()

/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - MOTIFSTRAND.length))) {
      return Float.NaN;
    }
  	return currentDNA.baseRawScoreAt(MOTIFSTRAND, pos, cfactor);
  } // end of getRawScoreAt(int)

/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or NaN if the position is unacceptable or an ambiguous base is encountered.
*/
  protected final float refreshScoreAt(int pos ) throws RetroTectorException {
    if ((pos < 0) | (pos >= (currentDNA.LENGTH - MOTIFSTRAND.length))) {
      return Float.NaN;
    }
  	return currentDNA.baseRefreshScoreAt(MOTIFSTRAND, pos, cfactor);
  } // end of refreshScoreAt(int)
  
/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold rawFactor and cfactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    MOTIFMATRIX = BaseMatrix.refreshBaseMatrix(theInfo.CFACTOR);
    cfactor = theInfo.CFACTOR;
    super.refresh(theInfo);
  } // end of refresh(RefreshInfo)

/**
* Reset Motif with new cfactor.
* currentDNA, rawThreshold and rawFactor are nulled.
* @param	cfac	New value for cfactor.
*/
	public final void refresh(float cfac) throws RetroTectorException {
    cfactor = cfac;
    MOTIFMATRIX = BaseMatrix.refreshBaseMatrix(cfactor);
		currentDNA = null;
		rawThreshold = Float.NaN;
		rawFactor = Float.NaN;
	} // end of refresh(float)

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    BaseMotif result = new BaseMotif();
    motifCopyHelp(result);
    result.cfactor = cfactor;
    return result;
  } // end of motifCopy()
  
/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
  	float bestScore = 0;
    for (int i=0; i<MOTIFSTRAND.length; i++ ) {
    	if (MOTIFSTRAND[i] >= 0) {
      	bestScore += MOTIFMATRIX.floatMatrix[MOTIFSTRAND[i]][MOTIFSTRAND[i] & MASK];
      }
		}
    return bestScore;
  } // end of getBestRawScore()

} // end of BaseMotif
