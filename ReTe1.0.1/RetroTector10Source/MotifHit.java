/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Tore Eriksson & Gšran Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

/**
* A hit by a Motif in SOURCEDNA.
*/
public class MotifHit implements Scorable, RangeOpiner {

/**
* The DNA where hit was found.
*/
  public final DNA SOURCEDNA;

/**
* The score of the hit.
*/
  public final float MOTIFHITSCORE;

/**
* The base position (internal) in SOURCEDNA where hit occurred.
*/
  public final int MOTIFHITHOTSPOT;

/**
* The first base position (internal) in SOURCEDNA touched by this hit.
*/
  public final int MOTIFHITFIRST;

/**
* The last base position (internal) in SOURCEDNA touched by this hit.
*/
  public final int MOTIFHITLAST;

/**
* The hitting Motif.
*/
  public final Motif PARENTMOTIF;

/**
* The RVector of this hit.
*/
  public final RVector RVECTOR;

/**
* The range of positions for the hotspot of the containing SubGeneHit
* acceptable from the point of view of this MotifHit.
*/
	public final Range SUBGENEHITHOTSPOTOPINION;
	
/**
* Creates one instance.
* @param	parent	The hitting Motif.
* @param	score		See MOTIFHITSCORE.
* @param	hotspot	See MOTIFHITHOTSPOT.
* @param	first		See MOTIFHITFIRST.
* @param	last		See MOTIFHITLAST.
* @param	sDNA		See SOURCEDNA.
*/
  public MotifHit(Motif parent, float score, int hotspot, int first, int last, DNA sDNA) throws RetroTectorException { 
    PARENTMOTIF = parent;
    MOTIFHITSCORE = score;
    MOTIFHITHOTSPOT = hotspot;
    MOTIFHITFIRST = first;
    MOTIFHITLAST = last;
 		RVECTOR = PARENTMOTIF.getRVector();
    RVECTOR.multiplyBy(score);
    SOURCEDNA = sDNA;
    SUBGENEHITHOTSPOTOPINION = PARENTMOTIF.subGeneHotspotRange(MOTIFHITHOTSPOT);
 } // end of constructor(Motif, float, int, int, int, DNA)

/*
* Creates one instance with specified RVector. Used by LTRMotifHit.
* @param	parent	The hitting Motif.
* @param	score		See MOTIFHITSCORE.
* @param	hotspot	See MOTIFHITHOTSPOT.
* @param	first		See MOTIFHITFIRST.
* @param	last		See MOTIFHITLAST.
* @param	rvector	See RVECTOR.
* @param	sDNA		See SOURCEDNA.
*/
  public MotifHit(Motif parent, float score, int hotspot, int first, int last, RVector rvector, DNA sDNA) throws RetroTectorException { 
    PARENTMOTIF = parent;
    MOTIFHITSCORE = score;
    MOTIFHITHOTSPOT = hotspot;
    MOTIFHITFIRST = first;
    MOTIFHITLAST = last;
 		RVECTOR = rvector;
    RVECTOR.multiplyBy(score);
    SOURCEDNA = sDNA;
    SUBGENEHITHOTSPOTOPINION = PARENTMOTIF.subGeneHotspotRange(MOTIFHITHOTSPOT);
 } // end of constructor(Motif, float, int, int, int, RVector, DNA)

/**
* @return The rvgenus of the hitting Motif.
*/
  public final String getRvGenus() {
  	return PARENTMOTIF.MOTIFRVGENUS;
  } // end of getRvGenus()

/**
* @return	MOTIFTYPE of the hitting Motif.
*/
  public final String motifType() {
  	return PARENTMOTIF.MOTIFTYPE;
  } // end of motifType()
  
/**
* Returns the score. Required by Scorable.
*/
  public float fetchScore() {
  	return MOTIFHITSCORE;
  } // end of fetchScore
  
/**
* As required by RangeOpiner.
*	@param	specification	An integer acceptable as a specification in a Range.
* @return	SUBGENEHITHOTSPOTOPINION if specification OK.
*/
	public final Range rangeOpinion(int specification) {
		if (specification == SUBGENEHITHOTSPOTOPINION.SPECIFICATION) {
			return SUBGENEHITHOTSPOTOPINION;
		} else {
			return null;
		}
	} // end of rangeOpinion(int)

/**
 * Returns a minimal description, at present motif group and rvgenus.
 */
  public final String signature() {
  	return PARENTMOTIF.MOTIFGROUP + ":" + PARENTMOTIF.MOTIFRVGENUS;
  } // end of signature()
  
/**
* Mainly for debugging.
* @param	mh	A MotifHit.
* @return	true	If there is effectively no difference between mh and this.
*/
  public final boolean sameAs(MotifHit mh) {
    return ((MOTIFHITSCORE == mh.MOTIFHITSCORE) &
        (MOTIFHITHOTSPOT == mh.MOTIFHITHOTSPOT) &
        (MOTIFHITFIRST == mh.MOTIFHITFIRST) &
        (MOTIFHITLAST == mh.MOTIFHITLAST) &
        (PARENTMOTIF == mh.PARENTMOTIF) &
        (SOURCEDNA == mh.SOURCEDNA) &
        RVECTOR.sameAs(mh.RVECTOR));
  } // end of sameAs(MotifHit)
  
/**
* @param	mh	A MotifHit.
* @return	true	If the parent Motif is effectively the only difference between mh and this.
*/
  public boolean similar(MotifHit mh) {
    return ((MOTIFHITSCORE == mh.MOTIFHITSCORE) &
        (MOTIFHITHOTSPOT == mh.MOTIFHITHOTSPOT) &
        (MOTIFHITFIRST == mh.MOTIFHITFIRST) &
        (MOTIFHITLAST == mh.MOTIFHITLAST) &
        (SOURCEDNA == mh.SOURCEDNA) &
        RVECTOR.sameAs(mh.RVECTOR));
  } // end of similar(MotifHit)

/**
* @param	factor	A float.
* @return	Int value of MOTIFHITFIRST + factor * (MOTIFHITLAST - MOTIFHITFIRST).
*/
	public final int lowerOverlapLimit(float factor) {
		return MOTIFHITFIRST + Math.round(factor * (MOTIFHITLAST - MOTIFHITFIRST));
	} // end of lowerOverlapLimit(float)
	
/**
* @param	factor	A float.
* @return	Int value of MOTIFHITLAST - factor * (MOTIFHITLAST - MOTIFHITFIRST).
*/
	public final int upperOverlapLimit(float factor) {
		return MOTIFHITLAST - Math.round(factor * (MOTIFHITLAST - MOTIFHITFIRST));
	} // end of upperOverlapLimit(float)
	
/**
 * Mainly for debugging-
 */
	public String toString() {
		return ("MotifHit with position:" + SOURCEDNA.externalize(MOTIFHITHOTSPOT) + " parent:" + PARENTMOTIF.MOTIFID);
	} // end of toString()
	
} // end of class MotifHit
