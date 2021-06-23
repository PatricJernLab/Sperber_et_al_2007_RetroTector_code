/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 30/9 -06
* Beautified 30/9 -06
*/

package retrotector;

import java.util.*;

/**
* A hit by an LTRMotif in targetDNA.
*/
public class LTRMotifHit extends MotifHit implements Scorable, RangeOpiner {

/**
* The LTRMotifHit with which this forms a pair.
*/
  public LTRMotifHit companion = null;

/**
* String describing integration sites.
*/
  public String integSites = "";
	
/**
* Key of parameter defining this.
*/
	public String hitkey = null;
  
/**
 * Creates one instance.
 * @param	parent	The hitting Motif.
 * @param	score		Its score.
 * @param	hotspot	The base position (internal) in targetDNA where hit occurred.
 * @param	first		See MOTIFHITFIRST.
 * @param	last		See MOTIFHITLAST.
 * @param	rvector	The RVector of the hit,
 * @param	sDNA		The DNA where hit occurred.
 */
  public LTRMotifHit(Motif parent, float score, int hotspot, int first, int last, RVector rvector, DNA sDNA) throws RetroTectorException { 
    super(parent, score, hotspot, first, last, rvector, sDNA);
  } // end of constructor(Motif, float, int, int, int, RVector, DNA)
  
/**
* @param	mh	A MotifHit.
* @return	True if the parent Motif is effectively the only difference between mh and this.
*/
  public final boolean similar(MotifHit mh) {
		if (!(mh instanceof LTRMotifHit)) {
			return false;
		}
		LTRMotifHit lh = (LTRMotifHit) mh;
		if (!integSites.equals(lh.integSites)) {
			return false;
		}
		return super.similar(mh);
	} // end of similar((MotifHit)
	
} // end of LTRMotifHit

