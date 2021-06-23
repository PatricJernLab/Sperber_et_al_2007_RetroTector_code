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

/**
* Motif subclass implementing a pair of Motifs.
* The two component Motifs should be defined in the ComponentMotifs parameter in Motifs.txt.
* The line in Motifs.txt defining the CoincidenceMotif should have the MOTIFIDs of the
* component Motifs and a DistanceRange specifier for the acceptable range of distance between
* the component Motif hotspots in the MOTIFSTRING field (blank separated).
* The component Motif mentioned first in this will be searched first.
* The absolute values for MAXSCORE for the component Motifs are irrelevant,
* but they serve as weights between the Motifs in calculating the raw score.
* A raw score threshold should normally be specified in the PARAM field, or it
* will be set to zero.
*/
public class CoincidenceMotif extends Motif {

/**
* @return	String identifying this class = "COI".
*/
	public final static String classType() {
    return "COI";
  } // end of classType()
  
/**
* The component Motif mentioned first.
*/
  public final Motif MOTIF1;
  
/**
* The component Motif mentioned second.
*/
  public final Motif MOTIF2;

/**
* Range of acceptable distance betweem hotspots.
*/
  public final DistanceRange HOTSPOTDISTANCE;
  
/**
* Raw score threshold.
*/
  public final float SCORETHRESHOLD;
  
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
 */
  public CoincidenceMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}
    String[ ] ss = Utilities.splitString(MOTIFSTRING);
    MOTIF1 = (Motif) motifDataBase.COMPONENTMOTIFS.get(new Integer(Utilities.decodeInt(ss[0])));
    MOTIF2 = (Motif) motifDataBase.COMPONENTMOTIFS.get(new Integer(Utilities.decodeInt(ss[1])));
    HOTSPOTDISTANCE = new DistanceRange(ss[2]);
    if (PARAM.length() > 0) {
      SCORETHRESHOLD = Utilities.decodeFloat(PARAM);
    } else {
      SCORETHRESHOLD = 0;
    }

    frameDefined = MOTIF1.frameDefined & MOTIF2.frameDefined & HOTSPOTDISTANCE.ACIDTYPE;
    showAsBases = MOTIF1.showAsBases | MOTIF2.showAsBases;
  } // end of constructor()

/**
* Reset Motif with new parameters. Is just shoved on to the component Motifs.
* @param	theInfo	RefreshInfo with required information.
*/
  public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    MOTIF1.refresh(theInfo);
    MOTIF2.refresh(theInfo);
  } // end of refresh(RefreshInfo)
  
/**
* Makes further preparations before scoring within a part of currentDNA.
* Is just shoved on to the component Motifs.
* @param	firstpos	The first (internal) positon in current DNA to prepare.
* @param	lastpos		The last (internal) positon in current DNA to prepare.
*/
  public final void localRefresh(int firstpos, int lastpos) throws RetroTectorException {
    MOTIF1.localRefresh(firstpos, lastpos);
    MOTIF2.localRefresh(firstpos, lastpos);
  } // end of localRefresh(int, int)
  
/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    CoincidenceMotif result = new CoincidenceMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()

/**
* @param	position	An (internal) positon in current DNA.
* @return	A MotifHit at position, or null.
*/
  public final MotifHit getMotifHitAt(int position) throws RetroTectorException {
    MotifHit mh1 = MOTIF1.getMotifHitAt(position);
    if ( mh1 == null) {
      return null;
    }
    Range ra = new Range(Range.UNDEFINED, position, HOTSPOTDISTANCE);
    MotifHit mh2 = null;
    MotifHit mh;
    for (int p=ra.RANGEMIN; p<=ra.RANGEMAX; p++) {
      mh = MOTIF2.getMotifHitAt(p);
      if (mh != null) {
        if ((mh2 == null) || (mh.MOTIFHITSCORE > mh2.MOTIFHITSCORE)) {
          mh2 = mh;
        }
      }
    }
    if (mh2 != null) {
      float maxscore = MOTIF1.MAXSCORE + MOTIF2.MAXSCORE;
      float rawscore = MAXSCORE * (mh1.MOTIFHITSCORE + mh2.MOTIFHITSCORE) / maxscore;
      if (rawscore >= SCORETHRESHOLD) {
        return new MotifHit(this,
          (rawscore - SCORETHRESHOLD) / (MAXSCORE - SCORETHRESHOLD),
          mh1.MOTIFHITHOTSPOT,
          Math.min(mh1.MOTIFHITFIRST, mh2.MOTIFHITFIRST),
          Math.max(mh1.MOTIFHITLAST, mh2.MOTIFHITLAST),
          mh1.SOURCEDNA);
      } else {
        return null;
      }        
    } else {
      return null;
    }
  } // end of getMotifHitAt(int)
  
} // end of CoincidenceMotif
