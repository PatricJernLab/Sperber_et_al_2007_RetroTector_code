/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/11 -06
* Beautified 12/11 -06
*/

package retrotectorcore;

import retrotector.*;
import java.util.*;

/**
* Class defining a pair of reasonably similar LTR candidates.
*/
public class CoreLTRPair {

/**
* The largest acceptable distance from start of LTR to hotspot = 1950.
*/
  public static final int MAXLEADING = LTRPair.MAXLEADING;

/**
* The smallest acceptable distance from start of LTR to hotspot = 50.
*/
  public static final int MINLEADING = LTRPair.MINLEADING;

/**
* The largest acceptable distance from hotspot to end of LTR = 1000.
*/
  public static final int MAXTRAILING = LTRPair.MAXTRAILING;

/**
* The smallest acceptable distance from hotspot to end of LTR = 40.
*/
  public static final int MINTRAILING = LTRPair.MINTRAILING;

/**
* = "_3LTR".
*/
  public static final String KEY3LTR = "_3LTR";
  
/**
* = "_5LTR".
*/
  public static final String KEY5LTR = "_5LTR";
  
/**
* = "_LTRpair".
*/
  public static final String KEYPAIR = "_LTRpair";
  
/**
* = "PairFactor".
*/
  public static final String PAIRFACTORKEY = "PairFactor";
  
/**
* = "RepeatLength".
*/
  public static final String REPLENGTHKEY = "RepeatLength";
  
/**
* = "IntegrationSites".
*/
  public static final String INTSITESKEY = "IntegrationSites";

/**
* @param	table			Hashtable from RetroVID script.
* @param	s					Prefix String, consisting of P or S, and a number.
* @param	theDNA		The relevant DNA.
* @param	parent		The Database to use.
* @param	maxscore	From Motifs.txt.
* @return	Array of the two LTRMotifHits defined by the output from toWriter(), or null.
*/
  public final static LTRMotifHit[ ] getHits(Hashtable table, String s, DNA theDNA, Database database, float maxscore) throws RetroTectorException {

// read _LTRpair part
    Object o = table.get(s + KEYPAIR);
    if (o == null) {
      return null;
    }
    String[ ] ss = (String[ ]) o;
    Hashtable tab = new Hashtable();
    ParameterFileReader re = new ParameterFileReader(ss, tab);
    re.readParameters();
    float factor = Utilities.decodeFloat((String) tab.get(PAIRFACTORKEY));
    String is = (String) tab.get(INTSITESKEY);

    LTRMotifHit[ ] result = new LTRMotifHit[2];
// read _5LTR part
    o = table.get(s + KEY5LTR);
    if (o == null) {
      return null;
    }
    ss = (String[ ]) o;
    LTRCandidate cand = new LTRCandidate(theDNA, ss);
    result[0] = new LTRMotifHit(database.ltr5Motif, factor * maxscore, cand.hotSpotPosition, cand.candidateFirst, cand.candidateLast, new RVector(cand.candidateVirusGenus), theDNA);
		result[0].hitkey = s + KEY5LTR;

// read _3LTR part
    o = table.get(s + KEY3LTR);
    if (o == null) {
      return null;
    }
    ss = (String[ ]) o;
    cand = new LTRCandidate(theDNA, ss);
    result[1] = new LTRMotifHit(database.ltr3Motif, factor * maxscore, cand.hotSpotPosition, cand.candidateFirst, cand.candidateLast, new RVector(cand.candidateVirusGenus), theDNA);
		result[1].hitkey = s + KEY3LTR;

    result[0].companion = result[1];
    result[1].companion = result[0];
    result[0].integSites = is;
    result[1].integSites = is;
    return result;
  } // end of getHits(Hashtable, String, DNA, Database, float)
	
  
/**
* Factor between 0 and 1, according to goodness. At present sum of factors of components.
*/
  public final float FACTOR;
  
/**
* Position of hotspot (eg T in AATAAA) of 5'LTR.
*/
  public final int LTR5HOTSPOT;

/**
* Position of start (eg TG) of 5'LTR.
*/
  public final int LTR5FIRST;

/**
* Position of end (eg A in CA) of 5'LTR.
*/
  public final int LTR5LAST;
  
/**
* Position of hotspot (eg T in AATAAA) of 3'LTR.
*/
  public final int LTR3HOTSPOT;

/**
* Position of start (eg TG) of 3'LTR.
*/
  public final int LTR3FIRST;

/**
* Position of end (eg A in CA) of 3'LTR.
*/
  public final int LTR3LAST;

/**
* Length of direct repeat.
*/
  public final int REPLENGTH;

/**
* Relevant DNA.
*/
  public final DNA SOURCEDNA;

/**
* Describes integration sites.
*/
  public final String INTEGRATIONSITES;

  private final LTRCandidate l5cand;
  private final LTRCandidate l3cand;
  
/**
* Constructor.
* A RetroTectorException is thrown if candidates are not sufficiently similar.
* @param	ltr5candidate		5'LTR candidate.
* @param	ltr3candidate		3'LTR candidate.
* @param	tolerance				See roof parameter of CoreLTRMatrix constructor.
* @param	dna							The relevant DNA.
* @param	pbsBarrier			5'LTR must not extend beyond this address
* @param	pptBarrier			3'LTR must not begin before this address
* @param	extendIntoPads	Extend no longer than this into pads
*/
  public CoreLTRPair(LTRCandidate ltr5candidate, LTRCandidate ltr3candidate, float tolerance, DNA dna, int pbsBarrier, int pptBarrier, int extendIntoPads) throws RetroTectorException {
  
    if ((ltr3candidate == null) | (ltr5candidate == null)) {
      RetroTectorException.sendError(this, "Null parameter");
    }
    
    l5cand = ltr5candidate;
    l3cand = ltr3candidate;
    SOURCEDNA = dna;
    LTR5HOTSPOT = ltr5candidate.hotSpotPosition;
    LTR3HOTSPOT = ltr3candidate.hotSpotPosition;
    int gcode = Compactor.BASECOMPACTOR.charToIntId('g');
    int acode = Compactor.BASECOMPACTOR.charToIntId('a');
    int tcode = Compactor.BASECOMPACTOR.charToIntId('t');
    int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
		
		int firstAllowed = Math.max(0, SOURCEDNA.firstUnPadded() - extendIntoPads);
		int lastAllowed = Math.min(SOURCEDNA.LENGTH - 1, SOURCEDNA.lastUnPadded() + extendIntoPads);
    
// earliest start position of 5'LTR
    int delimiter5 = SOURCEDNA.forceInside(Math.max(LTR5HOTSPOT - MAXLEADING, firstAllowed));
// earliest start position of 3'LTR
    int delimiter3 = SOURCEDNA.forceInside(Math.max(LTR3HOTSPOT - MAXLEADING, pptBarrier - 1));
		if (delimiter3 > LTR3HOTSPOT - MINLEADING) {
      RetroTectorException.sendError(this, "Too large pptBarrier");
    }
// make arrays of bases backwards from hotspot
    int[ ] lead5 = new int[LTR5HOTSPOT - delimiter5];
    int[ ] lead3 = new int[LTR3HOTSPOT - delimiter3];
    for (int i=0; i<lead5.length; i++) {
      lead5[i] = SOURCEDNA.get2bit(LTR5HOTSPOT - i);
    }
    for (int ii=0; ii<lead3.length; ii++) {
      lead3[ii] = SOURCEDNA.get2bit(LTR3HOTSPOT - ii);
    }

    CoreLTRMatrix leadmat = new CoreLTRMatrix(lead5, lead3, 1.0f, 1.0f, 0.95f, tolerance);
// is similarity good enough?
    if ((leadmat.SEQ1END < MINLEADING) | (leadmat.SEQ2END < MINLEADING)) {
      RetroTectorException.sendError(this, "Too short leading");
    }
    
// latest end position of 5'LTR
    delimiter5 = SOURCEDNA.forceInside(Math.min(LTR5HOTSPOT + MAXTRAILING, pbsBarrier + 1));
// latest end position of 3'LTR
    delimiter3 = SOURCEDNA.forceInside(Math.min(LTR3HOTSPOT + MAXTRAILING, lastAllowed));
		if (delimiter5 < LTR5HOTSPOT + MINTRAILING) {
      RetroTectorException.sendError(this, "Too small pbsBarrier");
    }
// make arrays of bases forwards from hotspot
    int[ ] trail5 = new int[delimiter5 - LTR5HOTSPOT];
    int[ ] trail3 = new int[delimiter3 - LTR3HOTSPOT];
    for (int i=0; i<trail5.length; i++) {
      trail5[i] = SOURCEDNA.get2bit(LTR5HOTSPOT + i);
    }
    for (int ii=0; ii<trail3.length; ii++) {
      trail3[ii] = SOURCEDNA.get2bit(LTR3HOTSPOT + ii);
    }
    
// check similarity
    CoreLTRMatrix trailmat = new CoreLTRMatrix(trail5, trail3, 1.0f, 1.0f, 0.95f, tolerance);
// is similarity good enough?
    if ((trailmat.SEQ1END < MINTRAILING) | (trailmat.SEQ2END < MINTRAILING)) {
      RetroTectorException.sendError(this, "Too short trailing");
    }
    
// similarity OK. Look for integration sites
// optimum[0] = measure of similarity between sites
// optimum[1] = position of 5'LTR start (Tg)
// optimum[2] = position of 3'LTR end (cA)
// optimum[3] = length of direct repeat
    int[ ] optimum = new int[4];
    optimum[0] = -1;
// 3' site patterns with 6, 5 or 4 bases direct repeat
    byte[ ][ ] fpattern = new byte[3][ ];

// try within 30 bases of where CoreLTRMatrix stopped
    for (int firstpos=Math.min(LTR5HOTSPOT-MINLEADING, LTR5HOTSPOT-leadmat.SEQ1END + 30); firstpos>=Math.max(firstAllowed, Math.max(6, LTR5HOTSPOT-leadmat.SEQ1END)); firstpos--) {
      int bottomscore = 0; // bonus for TG
      if (SOURCEDNA.get2bit(firstpos) == tcode) {
        bottomscore++;
      }
      if (SOURCEDNA.get2bit(firstpos + 1) == gcode) {
        bottomscore++;
      }
      fpattern[0] = new byte[8];
      fpattern[1] = new byte[7];
      fpattern[2] = new byte[6];
      for (int x=0; x<=2; x++) {
        fpattern[x][0] = (byte) ccode;
        fpattern[x][1] = (byte) acode;
      }
      for (int r=0; r<6; r++) {
        fpattern[0][r + 2] = SOURCEDNA.get2bit(firstpos - 6 + r);
      }
      System.arraycopy(fpattern[0], 3, fpattern[1], 2, 5);
      System.arraycopy(fpattern[1], 3, fpattern[2], 2, 4);
// try within 30 bases of where CoreLTRMatrix stopped
      for (int lastpos=Math.max(LTR3HOTSPOT+MINTRAILING, LTR3HOTSPOT+trailmat.SEQ2END - 30); lastpos<=Math.min(lastAllowed, Math.min(SOURCEDNA.LENGTH - 7, LTR3HOTSPOT+trailmat.SEQ2END)); lastpos++) {
        int count;
// try direct repeats of 6, 5 and 4
        for (int repl=6; repl>=4; repl--) {
// count discrepancies between pattern and actual DNA at 3' site
          count = SOURCEDNA.discrepancies(fpattern[6 - repl], lastpos - 1);
// calculate count as number of correct bases
          if (count >= 0) {
            count = repl + 2 - count + bottomscore;
          }
          if ((count >= 0) && (count >= optimum[0])) { // improvement
            optimum[0] = count;
            optimum[1] = firstpos;
            optimum[2] = lastpos;
            optimum[3] = repl;
          }
        }
      }
    }
// use best parameters found
    REPLENGTH = optimum[3];
    LTR5FIRST = optimum[1];
    LTR3LAST = optimum[2];
    FACTOR = ltr5candidate.candidateFactor + ltr3candidate.candidateFactor;
// get start of 3'LTR by pairing with start of 5'LTR
    int f = leadmat.seq2intercept(LTR5HOTSPOT - LTR5FIRST);
    if (f < 0) {
      RetroTectorException.sendError(this, "Hook error leading");
    }
    LTR3FIRST = LTR3HOTSPOT - f;
// get end of 5'LTR by pairing with end of 3'LTR
    f = trailmat.seq1intercept(LTR3LAST - LTR3HOTSPOT);
    if (f < 0) {
      RetroTectorException.sendError(this, "Hook error trailing");
    }
    LTR5LAST = LTR5HOTSPOT + f;
// construct description of integration sites
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
		if (LTR5FIRST - REPLENGTH < SOURCEDNA.FIRSTUNPADDED) {
			sb1.append("* ");
		}
    int i1;
    for (i1=0; i1<(REPLENGTH+2); i1++) {
      if (i1 == REPLENGTH) {
        sb1.append("/");
      }
      sb1.append(SOURCEDNA.getBase(LTR5FIRST - REPLENGTH + i1));
      if (i1 == 2) {
        sb2.append("/");
      }
      sb2.append(SOURCEDNA.getBase(LTR3LAST - 1 + i1));
    }
    INTEGRATIONSITES = sb1.toString() + "<>" + sb2.toString();
  } // end of constructor(LTRCandidate, LTRCandidate, float, DNA)

/**
* Constructor, assuming that LTR candidates are already vetted.
* @param	ltr5candidate		5'LTR candidate.
* @param	ltr3candidate		3'LTR candidate.
* @param	dna							The relevant DNA.
* @param	repl						See REPLENGTH.
* @param	comm						See INTEGRATIONSITES.
*/
  public CoreLTRPair(LTRCandidate ltr5candidate, LTRCandidate ltr3candidate, DNA dna, int repl, String comm) throws RetroTectorException {

    if ((ltr3candidate == null) | (ltr5candidate == null)) {
      RetroTectorException.sendError(this, "Null parameter");
    }
    
    l5cand = ltr5candidate;
    l3cand = ltr3candidate;
    SOURCEDNA = dna;
    LTR5HOTSPOT = ltr5candidate.hotSpotPosition;
    LTR5FIRST = ltr5candidate.candidateFirst;
    LTR5LAST = ltr5candidate.candidateLast;
    LTR3HOTSPOT = ltr3candidate.hotSpotPosition;
    LTR3FIRST = ltr3candidate.candidateFirst;
    LTR3LAST = ltr3candidate.candidateLast;
    REPLENGTH = repl;
    INTEGRATIONSITES = comm;
    FACTOR = ltr5candidate.candidateFactor + ltr3candidate.candidateFactor;
  } // end of constructor(LTRCandidate, LTRCandidate, DNA, int, String)

/**
* @return	String describing this, for debugging.
*/
  public String toString() {
    return "" + SOURCEDNA.externalize(LTR5HOTSPOT) + 
        " " + SOURCEDNA.externalize(LTR5FIRST) + 
        " " + SOURCEDNA.externalize(LTR5LAST) + 
        " " + l5cand.candidateVirusGenus + 
        " " + SOURCEDNA.externalize(LTR3HOTSPOT) + 
        " " + SOURCEDNA.externalize(LTR3FIRST) + 
        " " + SOURCEDNA.externalize(LTR3LAST) + 
        " " + l3cand.candidateVirusGenus + 
        " " + REPLENGTH + 
        " " + INTEGRATIONSITES + 
        " " + Utilities.formattedNumber(FACTOR, 1, 3) +
        " " + l5cand.candidateComment +
        " " + l3cand.candidateComment;
  } // end of toString
  
/**
* Outputs a description to a file.
* @param	writer	The (open) ParameterFileWriter to output to.
* @param	s				A prefix for the parameter keys.
*/
  public final void toWriter(ParameterFileWriter writer, String s) throws RetroTectorException {
    writer.startMultiParameter(s + KEYPAIR, false);
    writer.appendToMultiParameter("    " + PAIRFACTORKEY + ": " + Utilities.formattedNumber(FACTOR, 1, 3), false);
    writer.appendToMultiParameter("    " + REPLENGTHKEY + ": " + REPLENGTH, false);
    writer.appendToMultiParameter("    " + INTSITESKEY + ": " + INTEGRATIONSITES, false);
    writer.finishMultiParameter(false);
    writer.writeMultiParameter(s + KEY5LTR, l5cand.toStrings(), false);
    writer.writeMultiParameter(s + KEY3LTR, l3cand.toStrings(), false);
  } // end of toWriter
  
} // end of LTRPair
