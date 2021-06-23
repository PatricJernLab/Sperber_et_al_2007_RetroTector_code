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

/**
* Class defining a Motif using a neural network of DNA bases, defined in a file whose
* name is given in the MOTIFSTRING part of the defining line in Motifs.txt.
* Hot spot is at first base.
*/
public class BaseNNMotif extends OrdinaryMotif {

/**
* @return	String identifying this class = "BNN".
*/
	public static final String classType() {
		return "BNN";
	} // end of classType()

  private BaseNet net;
  
/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
*/
  public BaseNNMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}
    net = new BaseNet(motifDataBase.getFile(MOTIFSTRING));
		showAsBases = true;
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
		BaseNNMotif result = new BaseNNMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    return net.scoreNetAt(pos, currentDNA);
  } // end of getRawScoreAt(int)
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
    return net.scoreNetAt(pos, currentDNA);
  } // end of refreshScoreAt()

/**
* @return	The highest raw score obtainable = 1.
*/
  public final float getBestRawScore() {
    return 1.0f;
  } // end of getBestRawScore()
  
/**
 * @return	The number of bases in the net.
 */
 	public final int getBasesLength() {
  	return net.NROFBASES;
  } // end of getBasesLength()

} // end od BaseNNMotif
