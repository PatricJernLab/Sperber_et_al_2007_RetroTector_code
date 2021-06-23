/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 8/12 -06
* Beautified 8/12 -06
*/

package retrotector;

import java.util.*;

/**
* A collection of MotifHits all belonging to the same SubGene.
*/
public class SubGeneHit implements Scorable {

/*
* Stack to assemble MotifHits for a putative SubGeneHit.
*/
	private static MotifHit[ ] hits = new MotifHit[20];
	private static Range[ ] ranges = new Range[20];
	private static int topp = -1;
	
/**
* Empties MotifHit stack.
*/
	public static final void resetStack() {
		topp = -1;
	} // end of resetStack()
	
/**
* @return	True if there is no MotifHit in the MotifHit stack.
*/
	public static final boolean empty() {
		return (topp < 0);
	} // end of empty()
	
/*
* @return	The top MotifHit on the MotifHit stack, or null.
*/
	public static final MotifHit peekHit() {
		if (topp < 0) {
			return null;
		} else {
			return hits[topp];
		}
	} // end of peekHit()

/*
* @return	The range of SubGeneHit	hotspot positions compatible with all MotifHits on the MotifHit stack.
*/
	public static final Range peekRange() {
		if (topp < 0) {
			return null;
		} else {
			return ranges[topp];
		}
	} // end of peekRange()
	
/**
* For debugging.
*/
	public static void printStack() {
		Utilities.debugPrint(null, "SubGeneHitStack:");
		for (int t=0; t<=topp; t++) {
			Utilities.debugPrint(null, hits[t].toString());
		}
	} // end of printStack()
	
/**
* @param	mh	A MotifHit to try to add to top of MotifHit stack.
* @return	True if mh accepted and pushed.
*/
	public final static boolean pushMotifHit(MotifHit mh) throws RetroTectorException {
		Range r = mh.PARENTMOTIF.acceptedRange(mh); // get SubGeneHit hotspot range according to mh
		MotifHit mh2;
		DistanceRange dr;
		int dist;
		if (r == null) { // not acceptable
			return false;
		} else { // check against particular constraints
			for (int t=0; t<=topp; t++) {
				mh2 = hits[t];
				dist = mh2.MOTIFHITHOTSPOT - mh.MOTIFHITHOTSPOT;
				dr = mh.PARENTMOTIF.MOTIFGROUP.distanceToGroup(mh2.PARENTMOTIF.MOTIFGROUP);
				if ((dr != null) && (!dr.containsDistance(dist))) {
					return false;
				}
				dr = mh2.PARENTMOTIF.MOTIFGROUP.distanceToGroup(mh.PARENTMOTIF.MOTIFGROUP);
				if ((dr != null) && (!dr.containsDistance(-dist))) {
					return false;
				}
			}
			topp++;
			hits[topp] = mh;
			ranges[topp] = r;
			return true;
		}
	} // end of pushMotifHit(MotifHit)
		
/**
* Remove top element from MotifHit stack, if any.
* @return	True if an element was actually removed.
*/
	public static final boolean pop() {
		if (topp >= 0) {
			topp--;
			return true;
		} else {
			return false;
		}
	} // end of pop()
	
	
/**
* Position (internal in DNA) of hotspot.
*/
	public final int SGHITHOTSPOT;
	
/**
* Like in MotifHit.
*/
	public final String RVGENUS;
	
/**
* The SubGene to which this SubGeneHit belongs.
*/
	public final SubGene PARENTSUBGENE; // see getSubGene
	
/**
* The DNA where hit was found.
*/
  public final DNA SOURCEDNA;

/**
* The score of this SubGeneHit.
*/
	public final float SCORE;
	
/**
* The RVector of this hit.
*/
  public final RVector RVECTOR;

/**
* The MotifHits making up this.
*/	
	public final MotifHit[ ] MOTIFHITS; // the contents
	
/**
* First position in DNA (internal) of MOTIFHITFIRST of any MotifHit.
*/
	public final int FIRSTHITSTART;
	
/**
* Last position in DNA (internal) of MOTIFHITLAST of any MotifHit.
*/
	public final int LASTHITEND;
	
/**
* True if all the Motif hits are from detection Motifs.
*/
	public final boolean ISDETECTOR;
  
/**
* SUBGENEINDEX of PARENTSUBGENE.
*/
  public final int SUBGINDEX;
	
/**
* SUBGENEMASK of PARENTSUBGENE.
*/
  public final int SUBGMASK;
  
/**
* Constructor for a SubGeneHit consisting of a single MotifHit.
*	@param	mh	The MotifHit in question.
*/
  public SubGeneHit(MotifHit mh)  throws RetroTectorException {
    SGHITHOTSPOT = mh.MOTIFHITHOTSPOT;
    RVECTOR = mh.RVECTOR.copy();
		RVGENUS = RVECTOR.rvGenus();
    PARENTSUBGENE = mh.PARENTMOTIF.MOTIFSUBGENE;
    SOURCEDNA = mh.SOURCEDNA;
    SCORE = mh.MOTIFHITSCORE;
    MOTIFHITS = new MotifHit[1];
    MOTIFHITS[0] = mh;
    FIRSTHITSTART = mh.MOTIFHITFIRST;
    LASTHITEND = mh.MOTIFHITLAST;
    ISDETECTOR = mh.PARENTMOTIF.MOTIFLEVEL.equals("D");
    SUBGINDEX = PARENTSUBGENE.SUBGENEINDEX;
    SUBGMASK = PARENTSUBGENE.SUBGENEMASK;
  } // end of constructor(MotifHit)
	
/**
* Constructs a new SubGeneHit from MotifHit stack. The vector of the SubGeneHit
* is first calculated as the sum of the MotifHit vectors.
* If all the MotifHits (more than 1) are in the same reading frame.
* the vector is multiplied by a bonus factor.
* @param	frameBonus	Bonus factor if all MotifHits in same frame.
*/
 	public SubGeneHit(float frameBonus) throws RetroTectorException {
	
		MOTIFHITS = new MotifHit[topp + 1];
		RVector rvector = new RVector();
		int fi = Integer.MAX_VALUE;
		int la = Integer.MIN_VALUE;
		for (int i=0; i<MOTIFHITS.length; i++) { // transfer MotifHits
			MOTIFHITS[i] = hits[i];
			rvector.plus(MOTIFHITS[i].RVECTOR);
			fi = Math.min(fi, MOTIFHITS[i].MOTIFHITFIRST);
			la = Math.max(la, MOTIFHITS[i].MOTIFHITLAST);
		}
		FIRSTHITSTART = fi;
		LASTHITEND = la;
		SOURCEDNA = MOTIFHITS[0].SOURCEDNA;
		boolean frame = true;
		boolean det = MOTIFHITS[0].PARENTMOTIF.MOTIFLEVEL.equals("D");
		if (MOTIFHITS.length > 1) { // check if all MotifHits in same reading frame
			for (int m=1; m<MOTIFHITS.length; m++) {
				if (MOTIFHITS[m].PARENTMOTIF.frameDefined & (((MOTIFHITS[m].MOTIFHITHOTSPOT - MOTIFHITS[0].MOTIFHITHOTSPOT) % 3) != 0)) {
					frame = false;
				}
				if (MOTIFHITS[m].SOURCEDNA != SOURCEDNA) {
					RetroTectorException.sendError(this, "DNA mismatch");
				}
				if (!MOTIFHITS[m].PARENTMOTIF.MOTIFLEVEL.equals("D")) {
					det = false;
				}
			}
			if (frame) {
				rvector.multiplyBy(frameBonus);
			}
		}

// set up a few fields
		RVECTOR = rvector;
		SGHITHOTSPOT = Range.consensus(MOTIFHITS, Range.SUBGENEHOTSPOT).middle();
		PARENTSUBGENE = MOTIFHITS[0].PARENTMOTIF.MOTIFSUBGENE;
		ISDETECTOR = det;
		
		RVGENUS = RVECTOR.rvGenus();
		SCORE = RVECTOR.modulus();
    SUBGMASK = PARENTSUBGENE.SUBGENEMASK;
    SUBGINDEX = PARENTSUBGENE.SUBGENEINDEX;
} // end of constructor (float)
	
/**
* Does this SubGeneHit contain a particular MotifHit?
* @param	mh	The MotifHit to search for.
* @return	True if mh was found.
*/
 	public final boolean containsMotifHit(MotifHit mh) {
		if ((MOTIFHITS == null) || (MOTIFHITS.length == 0)) {
			return false;
		}
		for (int m=0; m<MOTIFHITS.length; m++) {
			if (MOTIFHITS[m] == mh) {
				return true;
			}
		}
		return false;
	} // end of containsMotifHit(MotifHit)
	
/**
* Returns the first MotifHit.
*/
 	public final MotifHit firstMotifHit() {
		return MOTIFHITS[0];
	} // end of firstMotifHit()
	
/**
* Returns the last MotifHit.
*/
	public final MotifHit lastMotifHit() {
		return MOTIFHITS[MOTIFHITS.length - 1];
	} // end of lastMotifHit()
	
/**
* A particular MotifHit or null.
* @param	index	Array index of MotifHit.
* @return	The MotifHit, or null.
*/
	public final MotifHit getMotifHit(int index) {
		if ((index<0) | (index >= MOTIFHITS.length)) {
			return null;
		} else {
			return MOTIFHITS[index];
		}
	} // end of getMotifHit(int)
	
/**
* @param	g	A MotifGroup.
* @return	A MotfiHit in g, if this SubGeneHit contains one, or null.
*/
	public final MotifHit hitInGroup(MotifGroup g) {
		String id = g.MOTIFGROUPNAME;
		for (int m=0; m<MOTIFHITS.length; m++) {
			if (MOTIFHITS[m].PARENTMOTIF.MOTIFGROUP.MOTIFGROUPNAME.equals(id)) {
				return MOTIFHITS[m];
			}
		}
		return null;
	} // end of hitInGroup(MotifGroup)

/**
* The score of the whole SubGeneHit. Required by Scorable.
*/
	public float fetchScore() {
		return SCORE;
	} // end of fetchScore()
	
/**
* Mainly for debugging.
* @param	sgh	A SubGeneHit.
* @return	-2 if hits of different length, -1 if hits identical, otherwise index of difference.
*/
  public int differentAt(SubGeneHit sgh) {
    if ((sgh == null) || (MOTIFHITS.length != sgh.MOTIFHITS.length)) {
      return -2;
    }
    for (int i=0; i<MOTIFHITS.length; i++) {
      if (!MOTIFHITS[i].sameAs(sgh.MOTIFHITS[i])) {
        return i;
      }
    }
    return -1;
  } // end of differentAt
  
/**
*	@return	The companion of the first MotifHit, if it is an LTRMotifHit, otherwise null.
*/
  public final  LTRMotifHit companion() {
    if (firstMotifHit() instanceof LTRMotifHit) {
      return ((LTRMotifHit) firstMotifHit()).companion;
    } else {
      return null;
    }
  } // end of companion()

/**
 * Short text description of SubGeneHit. Mainly for debugging.
 */
	public String toString() {
		return "Hit with subgene " + PARENTSUBGENE.SUBGENENAME + " at " + SOURCEDNA.externalize(SGHITHOTSPOT) + " score=" + SCORE + "  " + MOTIFHITS.length + " hits. Rvgenus=" + RVGENUS;
	} // end of toString()
	
} // end of SubGeneHit
