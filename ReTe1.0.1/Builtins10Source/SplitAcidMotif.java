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
* Motif subclass for Motifs consisting of several acid combinations. In the defining
* line in Motifs.txt, the MOTIFSTRING field must contain a '!' to mark the hotspot,
* preceded and/or followed by alternate parenthesized distance ranges (in acids) and
* acid symbols. A couple of examples:<BR>
* K(1-5)K(10-25)!PPPY<BR>
* !(4)D(51-58)D(35)E<BR>
* Acids are scored by the same rules as in AcidMotiff, and the final raw score at a
* target position as the highest sum of such scores obtainable within the distance constraints.
* The distances refer to the gaps between the fragments, not their starts.
* This Motif has a complex function and may contain bugs.
* Also, if there are frameshifts within a Motif hit, its amino acid sequence may not
* be shown correctly.
*/
public class SplitAcidMotif extends OrdinaryMotif {

/**
* @return	String identifying this class = "SPA".
*/
  public static final String classType() {
    return "SPA";
  } // end of classType()

  private SplitAcidComponent[ ] PRECOMPONENTS; // SplitAcidComponents preceding hotspot, the one closest to hotspot last
  private SplitAcidComponent[ ] POSTCOMPONENTS; // SplitAcidComponents following hotspot, the one closest to hotspot last

  private float cfactor; // bonus factor for conserved acids
  private AcidMatrix MOTIFMATRIX = Matrix.acidMATRIX;
  private final int MASK = 63;
  private final Compactor MOTIFCOMPACTOR = Compactor.ACIDCOMPACTOR;


/**
* Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
*/
  public SplitAcidMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
    if (!MOTIFTYPE.equals(classType())) {
      RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
    }

    int exind = MOTIFSTRING.indexOf("!");
    if (exind < 0) {
      RetroTectorException.sendError(this, "Syntax error in Motif " + MOTIFID, MOTIFSTRING);
    }
    String pre = MOTIFSTRING.substring(0, exind);
    PRECOMPONENTS = new SplitAcidComponent[Utilities.charCount(pre, ")")]; // one for each range spec
    int ind;
    int p = 0;
    while ((ind = pre.indexOf(")")) > 0) {
      PRECOMPONENTS[p++] = new SplitAcidComponent(pre.substring(0, ind + 1), MOTIFID);
      pre = pre.substring(ind + 1);
    }
    if (pre.length() < 0) {
      RetroTectorException.sendError(this, "Syntax error in Motif " + MOTIFID, MOTIFSTRING);
    }
    String post = MOTIFSTRING.substring(exind + 1);
    if (!post.startsWith("(")) {
      post = "(0<0)" + post;
    }
    POSTCOMPONENTS = new SplitAcidComponent[Utilities.charCount(post, ")")];
    int x = 0;
    p = POSTCOMPONENTS.length - 1;
    while ((ind = post.indexOf("(", 1)) >= 0) {
      POSTCOMPONENTS[p] = new SplitAcidComponent(post.substring(0, ind), x, MOTIFID);
      x = POSTCOMPONENTS[p].PATTERN.length * 3;
      p--;
      post = post.substring(ind);
    }
    POSTCOMPONENTS[0] = new SplitAcidComponent(post, x, MOTIFID);
                  
    frameDefined = true;
		exactlyAligned = true;

  } // end of constructor()

/**
* This does not work satisfactorily if there are frame shifts.
* @param	A MotifHit, presumably of this Motif.
* @return	A String representation of the DNA corresponding to theHit.
*/
  public final String correspondingDNA(MotifHit theHit) {
    return theHit.SOURCEDNA.subString(theHit.MOTIFHITFIRST, theHit.MOTIFHITHOTSPOT - 1, false) + theHit.SOURCEDNA.subString(theHit.MOTIFHITHOTSPOT, theHit.MOTIFHITLAST, false);
  } // end of correspondingDNA(MotifHit)

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    SplitAcidMotif result = new SplitAcidMotif();
    motifCopyHelp(result);
    result.cfactor = cfactor;
    return result;
  } // end of motifCopy()

/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 0) | (pos >= currentDNA.LENGTH)) {
      return Float.NaN;
    }
    float result = 0;
    SplitAcidComponent co;
    if (PRECOMPONENTS.length > 0) {
      co = PRECOMPONENTS[PRECOMPONENTS.length - 1]; // SplitAcidComponent just before hotspot
      if (pos >= co.OFFSET.HIGHESTDISTANCE) { // is pos within meaningful range?
        result += co.outscores[pos - co.OFFSET.HIGHESTDISTANCE];
      }
    }
    if (POSTCOMPONENTS.length > 0) {
      co = POSTCOMPONENTS[POSTCOMPONENTS.length - 1]; // SplitAcidComponent just after hotspot
      if ((pos - co.OFFSET.HIGHESTDISTANCE) < co.outscores.length) { // is pos within meaningful range?
        result += co.outscores[pos - co.OFFSET.HIGHESTDISTANCE];
      }
    }
    return result;
  } // end of getRawScoreAt(int

/**
* @param	pos	Position (internal in DNA) to match at
* @return	getRawScoreAt(pos).
*/
  protected final float refreshScoreAt(int pos ) throws RetroTectorException {
    return getRawScoreAt(pos);
  } // end of refreshScoreAt(int)

/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
    float bestScore = 0;
    for (int i=0; i<PRECOMPONENTS.length; i++) {
      bestScore += acidPatternMaxScore(PRECOMPONENTS[i].PATTERN, MOTIFMATRIX);
    }
    for (int i=0; i<POSTCOMPONENTS.length; i++) {
      bestScore += acidPatternMaxScore(POSTCOMPONENTS[i].PATTERN, MOTIFMATRIX);
    }
    return bestScore;
  } // end of getBestRawScore()

/**
* @return	0.
*/
 	public final int getBasesLength() {
    return 0;
  } // end of getBasesLength()

/**
* Reset Motif with new parameters.
* Hits are idedntified and stored in motifHitTable.
* @param	theInfo	RefreshInfo with required information.
*/
  public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    MOTIFMATRIX = AcidMatrix.refreshAcidMatrix(theInfo.CFACTOR);
    cfactor = theInfo.CFACTOR;
    if (PRECOMPONENTS.length > 0) {
      PRECOMPONENTS[0].makeScores(theInfo.TARGETDNA, null, cfactor, 0);
      for (int j=1; j<PRECOMPONENTS.length; j++) {
        PRECOMPONENTS[j].makeScores(theInfo.TARGETDNA, PRECOMPONENTS[j - 1], cfactor, 0);
      }
    }
    if (POSTCOMPONENTS.length > 0) {
      POSTCOMPONENTS[0].makeScores(theInfo.TARGETDNA, null, cfactor, 0);
      for (int j=1; j<POSTCOMPONENTS.length; j++) {
        POSTCOMPONENTS[j].makeScores(theInfo.TARGETDNA, POSTCOMPONENTS[j - 1], cfactor, 0);
      }
    }
    super.refresh(theInfo);
    MotifHit mh;
    MotifHit mh1;
    Hashtable tab = new Hashtable();
    long le = currentDNA.LENGTH;
    Long l;
// load hits into tab indexed by MOTIFHITFIRST and MOTIFHITLAST
    for (int p=0; p<currentDNA.LENGTH; p++) {
      if ((mh = makeMotifHitAt(p)) != null) {
        l = new Long(mh.MOTIFHITFIRST * le + mh.MOTIFHITLAST); // compress both into longint
        mh1 = (MotifHit) tab.get(l);
        if (mh1 == null) {
          tab.put(l, mh);
        } else if (mh.MOTIFHITSCORE > mh1.MOTIFHITSCORE) {
					tab.remove(mh1);
					tab.put(l, mh);
        }
      }
    }
    motifHitTable = new Hashtable();

// transfer to motifHitTable
    Enumeration e = tab.elements();
    while (e.hasMoreElements()) {
      mh = (MotifHit) e.nextElement();
			mh1 = (MotifHit) motifHitTable.get(new Integer(mh.MOTIFHITHOTSPOT));
			if (mh1 == null) {
				motifHitTable.put(new Integer(mh.MOTIFHITHOTSPOT), mh);
			} else if (mh.MOTIFHITSCORE > mh1.MOTIFHITSCORE) {
				motifHitTable.remove(mh1);
				motifHitTable.put(new Integer(mh.MOTIFHITHOTSPOT), mh);
			}
    }
// all hits collected, SplitAcidComponents no longer needed
    for (int j=0; j<PRECOMPONENTS.length; j++) {
      PRECOMPONENTS[j].clean();
    }
    for (int j=0; j<POSTCOMPONENTS.length; j++) {
      POSTCOMPONENTS[j].clean();
    }
  } // end of refresh(RefreshInfo)

/**
*	@param	position	An internal position in dna.
* @return	A MotifHit at position, or null.
*/
  public final MotifHit getMotifHitAt(int position) throws RetroTectorException {
    Integer in = new Integer(position);
    return (MotifHit) motifHitTable.get(in);
  } // end of getMotifHitAt(int)

/**
* @param	position	An (internal) position in DNA.
* @return	A hit by this Motif if there is one at position, otherwise null.
*/
  protected final MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    float sc = (getRawScoreAt(position) - rawThreshold) * rawFactor;
    if (sc <= 0) {
      return null;
    }
    int pos1 = position; // optimal position of first component
    for (int p1=PRECOMPONENTS.length-1; p1>=0; p1--) {
      pos1 = PRECOMPONENTS[p1].maxRelativeTo(pos1);
			if (pos1 < 0) {
				return null;
			}
    }
    int pos2 = position; // optimal position of last component
    for (int p2=POSTCOMPONENTS.length-1; p2>=0; p2--) {
      pos2 = POSTCOMPONENTS[p2].maxRelativeTo(pos2);
			if (pos2 < 0) {
				return null;
			}
    }
    if (POSTCOMPONENTS.length > 0) { // add length of last component
      pos2 += POSTCOMPONENTS[0].PATTERN.length * 3 - 1;
    }
    return new MotifHit(this, sc, position, pos1, pos2, currentDNA);
  } // end of makeMotifHitAt(int)
    
} // end of SplitAcidMotif
