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
* Motif subclass for Motifs hitting a single amino acid.
* Its main use is as component of CoincidenceMotifs.
* The MOTIFSTRING field of the defining line should contain a '!' defining the hotspot
* and acid symbols before or/and after it, specifying the valid acid at each
* distance from the hotspot. Only exactly matching acids are accepted.
*/
public class SingleAcidMotif extends Motif {

/**
* @return	String identifying this class = "1A".
*/
	public final static String classType() {
    return "1A";
  } // end of classType()
	
/**
* Distance (in acids) between start of MOTIFSTRING field and '!'.
*/
	public final int HOTOFFSET;

/**
* The acid symbols.
*/
	public final char[ ] ACIDS;
  
  private DNA currentDNA;
  
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
 */
  public SingleAcidMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
    frameDefined = true;
		HOTOFFSET = MOTIFSTRING.indexOf("!");
		if (HOTOFFSET < 0) {
			RetroTectorException.sendError(this, "No ! in definition");
		}
		ACIDS = new char[MOTIFSTRING.length() - 1];
		int p = 0;
		for (int i=0; i<MOTIFSTRING.length(); i++) {
			if (MOTIFSTRING.charAt(i) != '!') {
				ACIDS[p++] = MOTIFSTRING.charAt(i);
			}
		}
  } // end of constructor()
  
/**
* Reset Motif with new parameters.
* @param	theInfo	RefreshInfo with required information.
*/
  public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
  } // end of refresh(RefreshInfo)
  
/**
* Dummy.
*/
  public final void localRefresh(int firstpos, int lastpos) throws RetroTectorException {
  } // end of localRefresh(int, int)
  
/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    SingleAcidMotif result = new SingleAcidMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()
  
/**
* @param	position	An (internal) positon in current DNA.
* @return	A MotifHit at position, or null.
*/
  public final MotifHit getMotifHitAt(int position) throws RetroTectorException {
		int po = position - 3 * HOTOFFSET;
    for (int p=0; p<ACIDS.length; p++) {
      if (currentDNA.insideDNA(po) && (currentDNA.getAcid(po) == ACIDS[p])) {
        return new MotifHit(this, MAXSCORE, position, position, position + 2, currentDNA);
      }
			po += 3;
    }
    return null;
  } // end of getMotifHitAt(int)
  
} // end of SingleAcidMotif

