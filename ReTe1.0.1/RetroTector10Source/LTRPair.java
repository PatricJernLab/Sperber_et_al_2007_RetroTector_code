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

package retrotector;

import retrotectorcore.*;
import java.util.*;

/**
* Interface to CoreLTRPair.
*/
public class LTRPair implements Scorable {

/**
* The largest acceptable distance from start of LTR to hotspot = 1950.
*/
  public static final int MAXLEADING = 1950;

/**
* The smallest acceptable distance from start of LTR to hotspot = 50.
*/
  public static final int MINLEADING = 50;

/**
* The largest acceptable distance from hotspot to end of LTR = 1000.
*/
  public static final int MAXTRAILING = 1000;

/**
* The smallest acceptable distance from hotspot to end of LTR = 40.
*/
  public static final int MINTRAILING = 40;

	private CoreLTRPair coreLTRPair;
	
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
  public LTRPair(LTRCandidate ltr5candidate, LTRCandidate ltr3candidate, float tolerance, DNA dna, int pbsBarrier, int pptBarrier, int extendIntoPads) throws RetroTectorException {
  
		try {
			coreLTRPair = new CoreLTRPair(ltr5candidate, ltr3candidate, tolerance, dna, pbsBarrier, pptBarrier, extendIntoPads);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreLTRPair, "LTRPair", rte);
		}
		
		FACTOR = coreLTRPair.FACTOR;
		LTR5HOTSPOT = coreLTRPair.LTR5HOTSPOT;
		LTR5FIRST = coreLTRPair.LTR5FIRST;
		LTR5LAST = coreLTRPair.LTR5LAST;
		LTR3HOTSPOT = coreLTRPair.LTR3HOTSPOT;
		LTR3FIRST = coreLTRPair.LTR3FIRST;
		LTR3LAST = coreLTRPair.LTR3LAST;
		REPLENGTH = coreLTRPair.REPLENGTH;
		SOURCEDNA = coreLTRPair.SOURCEDNA;
		INTEGRATIONSITES = coreLTRPair.INTEGRATIONSITES;
		
  } // end of constructor(LTRCandidate, LTRCandidate, float, DNA, int, int)

/**
* Constructor, assuming that LTR candidates are already vetted.
* @param	ltr5candidate		5'LTR candidate.
* @param	ltr3candidate		3'LTR candidate.
* @param	dna							The relevant DNA.
* @param	repl						See REPLENGTH.
* @param	comm						See INTEGRATIONSITES.
*/
  public LTRPair(LTRCandidate ltr5candidate, LTRCandidate ltr3candidate, DNA dna, int repl, String comm) throws RetroTectorException {

		try {
			coreLTRPair = new CoreLTRPair(ltr5candidate, ltr3candidate, dna, repl, comm);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreLTRPair, "LTRPair", rte);
		}
		
		FACTOR = coreLTRPair.FACTOR;
		LTR5HOTSPOT = coreLTRPair.LTR5HOTSPOT;
		LTR5FIRST = coreLTRPair.LTR5FIRST;
		LTR5LAST = coreLTRPair.LTR5LAST;
		LTR3HOTSPOT = coreLTRPair.LTR3HOTSPOT;
		LTR3FIRST = coreLTRPair.LTR3FIRST;
		LTR3LAST = coreLTRPair.LTR3LAST;
		REPLENGTH = coreLTRPair.REPLENGTH;
		SOURCEDNA = coreLTRPair.SOURCEDNA;
		INTEGRATIONSITES = coreLTRPair.INTEGRATIONSITES;

  } // end of constructor(LTRCandidate, LTRCandidate, DNA, int, String)

/**
* @return	String describing this, for debugging.
*/
  public String toString() {
    return coreLTRPair.toString();
	} // end of toString()
  
/**
* Outputs a description to a file.
* @param	writer	The (open) ParameterFileWriter to output to.
* @param	s				A prefix for the parameter keys.
*/
  public final void toWriter(ParameterFileWriter writer, String s) throws RetroTectorException {
		try {
			coreLTRPair.toWriter(writer, s);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreLTRPair, "LTRPair", rte);
		}
  } // end of toWriter(ParameterFileWriter, String)

/**
* As required by Scorable.
* @return	FACTOR.
*/
	public float fetchScore() {
		return FACTOR;
	} // end of fetchScore()
	
/**
* @return	True if all final parameters are equal.
*/
	public boolean doesEqual(Object o) {
		if (!(o instanceof LTRPair)) {
			return false;
		}
		LTRPair lp = (LTRPair) o;
		if ((FACTOR != lp.FACTOR) | 
				(LTR5HOTSPOT != lp.LTR5HOTSPOT) | (LTR5FIRST != lp.LTR5FIRST) | (LTR5LAST != lp.LTR5LAST) |
				(LTR3HOTSPOT != lp.LTR3HOTSPOT) | (LTR3FIRST != lp.LTR3FIRST) | (LTR3LAST != lp.LTR3LAST) | (REPLENGTH != lp.REPLENGTH)) {
			return false;
		}
		return (INTEGRATIONSITES.equals(lp.INTEGRATIONSITES));
	} // end of doesEqual(Object)
		
/**
* @param	table			Hashtable from RetroVID script.
* @param	s					Prefix String, consisting of P or S, and a number.
* @param	theDNA		The relevant DNA.
* @param	parent		The Database to use.
* @param	maxscore	From Motifs.txt.
* @return	Array of the two LTRMotifHits defined by the output from toWriter(), or null.
*/
  public final static LTRMotifHit[ ] getHits(Hashtable table, String s, DNA theDNA, Database database, float maxscore) throws RetroTectorException {

    return CoreLTRPair.getHits(table, s, theDNA, database, maxscore);
		
  } // end of getHits(Hashtable, String, DNA, Database, float)
  
} // end of LTRPair
