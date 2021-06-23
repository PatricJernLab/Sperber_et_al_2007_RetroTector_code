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
import java.util.*;

/**
* Motif subclass defining an LTR Motif, using information from RefreshInfo.LTRTABLE.
* Hot spot is at T in AATAAA or equivalent.
*/
public class LTRMotif extends Motif {

/**
* @return	String identifying this class = "LTR".
*/
	public static final String classType() {
		return "LTR";
	} // end of classType()
	
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
 */
  public LTRMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}

    frameDefined = false;
    showAsBases = true;
  } // end of constructor()

/**
* Reset Motif with new parameters. Puts LTR candidates in LTR pairs into motifHitTable.
* @param	theInfo	RefreshInfo with hits from LTRID in LTRTABLE.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
  
    Stack hitStack = new Stack();
    String strandc;
    
    LTRMotifHit[ ] hitpair;
    if (theInfo.TARGETDNA.PRIMARYSTRAND) {
      strandc = "P";
    } else {
      strandc = "S";
    }
    int pi=1;
// hits are put into stack ordered by position so that hits with same hotspot are adjacent
// ordering not really necessary, I think
    while ((hitpair = LTRPair.getHits(theInfo.LTRTABLE, strandc + pi, theInfo.TARGETDNA, MOTIFDATABASE, MAXSCORE)) != null) {
			if (hitpair[0].PARENTMOTIF.MOTIFSUBGENE.SUBGENENAME.equals(this.MOTIFSUBGENE.SUBGENENAME)) {
				int x = hitpair[0].MOTIFHITHOTSPOT;
				int p = 0;
				while ((p < hitStack.size()) && (((LTRMotifHit) hitStack.elementAt(p)).MOTIFHITHOTSPOT < x)) {
					p++; // bypass elements to the left of this
				}
	// put it in
				if (p >= hitStack.size()) {
					hitStack.push(hitpair[0]);
				} else {
					hitStack.insertElementAt(hitpair[0], p);
				}
			}
// the same for second LTR
			if (hitpair[1].PARENTMOTIF.MOTIFSUBGENE.SUBGENENAME.equals(this.MOTIFSUBGENE.SUBGENENAME)) {
				int x = hitpair[1].MOTIFHITHOTSPOT;
				int p = 0;
				while ((p < hitStack.size()) && (((LTRMotifHit) hitStack.elementAt(p)).MOTIFHITHOTSPOT < x)) {
					p++;
				}
				if (p >= hitStack.size()) {
					hitStack.push(hitpair[1]);
				} else {
					hitStack.insertElementAt(hitpair[1], p);
				}
			}
      pi++;
    }
		
// and single LTRs
		if (RetroTectorEngine.USESINGLELTRS) {
			LTRCandidate cand;
			LTRMotifHit hit;
			String[ ] ss;
			pi = 1;
			while ((ss = (String[ ]) theInfo.LTRTABLE.get(Executor.SINGLELTRSUFFIX + strandc + pi)) != null) {
				cand = new LTRCandidate(theInfo.TARGETDNA, ss);
				hit = new LTRMotifHit(this, cand.candidateFactor * MAXSCORE, cand.hotSpotPosition, cand.candidateFirst, cand.candidateLast, new RVector(cand.candidateVirusGenus), theInfo.TARGETDNA);
				hit.hitkey = Executor.SINGLELTRSUFFIX + strandc + pi;
				int x = cand.hotSpotPosition;
				int p = 0;
				while ((p < hitStack.size()) && (((LTRMotifHit) hitStack.elementAt(p)).MOTIFHITHOTSPOT < x)) {
					p++; // bypass elements to the left of this
				}
				if (p >= hitStack.size()) {
					hitStack.push(hit);
				} else {
					hitStack.insertElementAt(hit, p);
				}
				pi++;
			}
		}

    motifHitTable = new Hashtable();
    LTRMotifHit h;
    LTRMotifHit h2;
		LTRMotifHit[ ] hs;
		LTRMotifHit[ ] hs2;
		boolean found;
		Object o;
		Integer inte;
    for (int i=0; i<hitStack.size(); i++) {
      h = (LTRMotifHit) hitStack.elementAt(i);
			inte = new Integer(h.MOTIFHITHOTSPOT);
			o = motifHitTable.get(inte);
      if (o == null) { // no other hit here yet
				motifHitTable.put(inte, h);
			} else if (o instanceof LTRMotifHit) { 
				h2 = (LTRMotifHit) o; // one already, make array of two
				if (!h.similar(h2)) {
					hs = new LTRMotifHit[2];
					hs[0] = h2;
					hs[1] = h;
					motifHitTable.put(inte, hs);
				}
			} else { // more already, extend array
				hs = (LTRMotifHit[ ]) o;
				found = false;
				for (int ii=0; ii<hs.length; ii++) {
					if (h.similar(hs[ii])) {
						found = true;
					}
				}
				if (!found) {
					hs2 = new LTRMotifHit[hs.length + 1];
					System.arraycopy(hs, 0, hs2, 0, hs.length);
					hs2[hs.length - 1] = h;
					motifHitTable.put(inte, hs);
				}
			}
    }
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
    LTRMotif result = new LTRMotif();
    motifCopyHelp(result);
    if (motifHitTable != null) {
      result.motifHitTable = (Hashtable) motifHitTable.clone();
    }
    return result;
  } // end of motifCopy()
  
/**
* Dummy. I do not think it is needed at all.
*/
  protected MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    return null;
  } // end of makeMotifHitAt(int)
  
} // end of LTRMotif
