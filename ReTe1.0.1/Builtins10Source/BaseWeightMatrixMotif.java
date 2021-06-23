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
import java.io.*;
import java.util.*;

/**
* Class defining a Motif using a base weight matrix.
* The MOTIFSTRING part of the defining line in Motifs.txt
*	should be divided by a colon, the first part being the name
*	of the file containing the parameter whose name is given
*	by the second part, which follows the syntax required by BaseWeightMatrix.
*
* Hotspot is at first base.
*/
public class BaseWeightMatrixMotif extends OrdinaryMotif {

/**
* @return	String identifying this class = "BWM".
*/
	public static final String classType() {
		return "BWM";
	} // end of classType()

  private BaseWeightMatrix matrix;
  
/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
*/
  public BaseWeightMatrixMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}
    int ind = MOTIFSTRING.indexOf(":");
    File fi = motifDataBase.getFile(MOTIFSTRING.substring(0, ind).trim());
    parameters = new Hashtable();
    ParameterFileReader reader = new ParameterFileReader(fi, parameters);
    reader.readParameters();
    reader.close();
    String[ ] lines = getStringArray(MOTIFSTRING.substring(ind + 1).trim());
    matrix = new BaseWeightMatrix(lines);
		showAsBases = true;
		parameters = null;
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    BaseWeightMatrixMotif result = new BaseWeightMatrixMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    return currentDNA.baseMatrixScoreAt(matrix, pos);
  } // end of getRawScoreAt(int)
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
    return currentDNA.baseMatrixScoreAt(matrix, pos);
  } // end of refreshScoreAt(int)

/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
    return matrix.MAXSCORE;
  } // end of getBestRawScore()
  
/**
* @return	The number of bases in the matrix.
*/
 	public final int getBasesLength() {
  	return matrix.LENGTH;
  } // end of getBasesLength()

} // end of BaseWeightMatrixMotif
