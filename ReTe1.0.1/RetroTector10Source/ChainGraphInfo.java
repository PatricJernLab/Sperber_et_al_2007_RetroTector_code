/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 15/12 -06
* Beautified 15/12 -06
*/

package retrotector;

import java.util.*;

/**
* Textual description of a Chain.
*/
public class ChainGraphInfo implements Scorable {
	
/**
* Summary information about a Chain used by ListChains.
*/
	public static class ChainShortDescription implements Scorable {
	
/**
* Position of Chain start (internal).
*/
		public final int FIRSTBASEPOS;
		
/**
* Position of Chain end (internal).
*/
		public final int LASTBASEPOS;
		
/**
* Virus genus(s) of Chain.
*/
		public final String GENUS;
		
/**
* Score of Chain.
*/
		public final int SCORE;
		
		private float sortscore; // for fetchScore()
				
/**
* Constructor.
* @param	startpos	See FIRSTBASEPOS.
* @param	endpos		See LASTBASEPOS.
* @param	type			See TYPE.
* @param	score			See SCORE.
*/
		public ChainShortDescription(int startpos, int endpos, String type, int score) {
			FIRSTBASEPOS = startpos;
			LASTBASEPOS = endpos;
			GENUS = type;
			SCORE = score;
			if (LASTBASEPOS > FIRSTBASEPOS) {
				sortscore = FIRSTBASEPOS + 0.00001f * (LASTBASEPOS - FIRSTBASEPOS);
			} else {
				sortscore = LASTBASEPOS + 0.00001f * (FIRSTBASEPOS - LASTBASEPOS);
			}
		} // end of constructor(int, int, String, int)
		
/**
* An object, reconstructed from the output of toString()
* @param	sourceString	Output from toString().
*/
		public ChainShortDescription(String sourceString) throws RetroTectorException {
			String[ ] ss = Utilities.splitString(sourceString);
			FIRSTBASEPOS = Utilities.decodeInt(ss[0]);
			LASTBASEPOS = Utilities.decodeInt(ss[1]);
			GENUS = ss[2];
			SCORE = Utilities.decodeInt(ss[3]);			
		} // end of constructor(String)
		
/**
* @param	csd	Another ChainShortDescription.
* @return	True if contents are identical.
*/
		public final boolean doesEqual(ChainShortDescription csd) {
			return (FIRSTBASEPOS == csd.FIRSTBASEPOS) & (LASTBASEPOS == csd.LASTBASEPOS) & GENUS.equals(csd.GENUS) & (SCORE == csd.SCORE);
		} // end of doesEqual(ChainShortDescription)
		
/**
* As required by Scorable.
* @return	A value depending primarily on FIRSTBASEPOS and secondarily on length.
*/
		public float fetchScore() {
			return sortscore;
		} // end of fetchScore()
			
/**
* @return	 Contents in text form, for display or for file storage.
*/
		public String toString() {
			return FIRSTBASEPOS + " " + LASTBASEPOS + " " + GENUS + " " + SCORE;
		} // end of toString()
	
	} // end of ChainShortDescription
	
	
/**
* @param	ss	A String array as produced by toStrings().
* @return	A ChainShortDescription relating to that Chain.
*/
	public static ChainShortDescription shortDescription(String[ ] ss) throws RetroTectorException {
		int index = ss[12].indexOf(PAD2);
		return new ChainShortDescription(Utilities.decodeInt(ss[10]), Utilities.decodeInt(ss[11]), ss[12].substring(PAD1.length(), index),  Utilities.decodeInt(ss[12].substring(index + PAD2.length())));
	} // end of shortDescription(String[ ])
	


	private static final String PAD1 = " Type ";
	private static final String PAD2 = " Score= ";
	private static final String BROKENKEY = "  Broken ";

/**
* String describing breaks.
*/
	public final String BREAKS;

/**
* Position of Chain start (internal).
*/
	public final int FIRSTBASEPOS;
	
/**
* Position of Chain end (internal).
*/
	public final int LASTBASEPOS;
	
/**
* Virus genus(s) of Chain.
*/
	public final String GENUS;
	
/**
* Score of Chain.
*/
	public final int SCORE;
	
/**
* Comment describing LTR properties.
*/
  public final String LTRCOMMENT;
  
/**
* Identification String (P1, S2 or whatever)
*/
	public String number = "";

/**
* Info for all the SubGenes in the Chain.
*/
	public SubGeneHitGraphInfo[ ] subgeneinfo;
	
/**
* Strand character + 5-digit chain number.
*/
	public String tag = null;

/**
*  Cosines of Chain RVector with base vectors.
*/
 	private float[ ] cosines = null;
	private int[ ][ ] breaks;
	
/**
* Constructor.
* @param	theChain	Chain to describe.
*/
	public ChainGraphInfo(Chain theChain) throws RetroTectorException {
		FIRSTBASEPOS = theChain.CHAINSTART;
		LASTBASEPOS = theChain.CHAINEND;
		cosines = new float[RVector.RVSIZE];
		for (int c=0; c<RVector.RVSIZE; c++) {
			cosines[c] = theChain.CHAINRVECTOR.directionCosine(c);
		}
		GENUS = theChain.CHAINRVGENUS;
		SCORE = Math.round(theChain.CHAINSCORE);
		BREAKS = theChain.breaks();
    LTRCOMMENT = theChain.ltrPairComment;
    int gg = 0;
    subgeneinfo = new SubGeneHitGraphInfo[theChain.HITCOUNT];
		for (int g=0; g<SubGene.MAXSUBGENENUMBER; g++) {
			if (theChain.getSubGeneHit(g) != null) {
				subgeneinfo[gg++] = new SubGeneHitGraphInfo(theChain.getSubGeneHit(g));
			}
		}
		tag = theChain.STRANDCHAR + Executor.zeroLead(theChain.chainNumber, 90000);
		number = String.valueOf(theChain.STRANDCHAR) + theChain.chainNumber;
	} // end of constructor(Chain)
	
/**
* An object, reconstructed from the output of toStrings()
* @param	sourceStrings	Output from toStrings().
* @param	theDNA				The DNA whose Translator should be used.
*/
	public ChainGraphInfo(String[ ] sourceStrings, DNA theDNA) throws RetroTectorException {
		this(sourceStrings, theDNA.TRANSLATOR);
	} // end of constructor(String[ ], DNA)

/**
* An object, reconstructed from the output of toStrings()
* @param	sourceStrings	Output from toStrings().
* @param	theTranslator	The DNA.Translator in use.
*/
	public ChainGraphInfo(String[ ] sourceStrings, DNA.Translator theTranslator) throws RetroTectorException {
		int p = 0;
		cosines = new float[RVector.RVSIZE];
		for (int f=0; f<RVector.RVSIZE; f++) {
			try {
				cosines[f] = Float.valueOf(sourceStrings[p++].substring(6)).floatValue();
			} catch (NumberFormatException nfe) {
				RetroTectorException.sendError(this, "syntax error in file");
			}
		}
		if (sourceStrings[p].startsWith(BROKENKEY)) {
			BREAKS = sourceStrings[p].substring(BROKENKEY.length());
		} else {
			BREAKS = "";
		}
		String breakS = BREAKS.trim();
		int ind;
		int h1;
		int h2;
		Stack st = new Stack();
		int[ ] ii;
		while (breakS.length() > 0) {
			ii = new int[2];
			ind = breakS.indexOf(">");
			ii[0] =  theTranslator.internalize(Utilities.decodeInt(breakS.substring(0, ind).trim()));
			breakS = breakS.substring(ind + 1).trim();
			ind = breakS.indexOf(" ");
			if (ind < 0) {
				ii[1] =  theTranslator.internalize(Utilities.decodeInt(breakS.trim()));
				breakS = "";
			} else {
				ii[1] =  theTranslator.internalize(Utilities.decodeInt(breakS.substring(0, ind).trim()));
				breakS = breakS.substring(ind + 1).trim();
			}
			st.push(ii);
		}
		breaks = new int[st.size()][];
		st.copyInto(breaks);
		p++;

		FIRSTBASEPOS = theTranslator.internalize(Utilities.decodeInt(sourceStrings[p++]));
		LASTBASEPOS = theTranslator.internalize(Utilities.decodeInt(sourceStrings[p++]));
		int index = sourceStrings[p].indexOf(PAD2);
		GENUS = sourceStrings[p].substring(PAD1.length(), index);
		SCORE = Utilities.decodeInt(sourceStrings[p++].substring(index + PAD2.length()));
		
		Stack infostack = new Stack();
		Stack linestack;
		String[ ] sss;
		while (sourceStrings[p].length() > 0) {
			linestack = new Stack();
			linestack.push(sourceStrings[p++]);
			while ((sourceStrings[p].length() > 0) && (sourceStrings[p].charAt(0) == ' ')) {
				linestack.push(sourceStrings[p++]);
			}
			sss = new String[linestack.size()];
			for (int q=sss.length - 1; q>=0; q--) {
				sss[q] = (String) linestack.pop();
			}
			infostack.push(new SubGeneHitGraphInfo(sss, theTranslator));
		}
		
    if (p < (sourceStrings.length - 1)) {
      LTRCOMMENT = sourceStrings[p + 1];
    } else {
      LTRCOMMENT = "";
    }
		subgeneinfo = new SubGeneHitGraphInfo[infostack.size()];
		for (int qq=subgeneinfo.length - 1; qq>=0; qq--) {
			subgeneinfo[qq] = (SubGeneHitGraphInfo) infostack.pop();
		}
			
	} // end of constructor(String[ ], DNA.Translator)
  
/**
* As required by Scorable.
* @return	SCORE.
*/
  public float fetchScore() {
    return (float) SCORE;
  } // end of fetchScore()
	
/**
* @param sg				A SubGene.
* @param database	A Database.
* @return	The Range of acceptable hotspot positions in this chain for a hit with SubGene,
*		according to SubGenes.txt in database.
*/
  public final Range allowedRange(SubGene sg, Database database) throws RetroTectorException {
    Range[] rr = new Range[subgeneinfo.length];
    for (int i=0; i<rr.length; i++) {
      SubGene sgg = database.getSubGene(subgeneinfo[i].SUBGENENAME);
      rr[i] = new Range(Range.UNDEFINED, subgeneinfo[i].HITHOTSPOT, sgg.distanceTo(sg));
    }
    return Range.consensus(rr);
  } // end of allowedRange(SubGene, Database)
  
/**
*	@param	theDNA	The DNA in use.
* @return	Contents in text form, for display or for file storage.
*/
	public final String[ ] toStrings(DNA theDNA) {
	
		int size = 0;
		String[ ][ ] subGeneStrings = new String[subgeneinfo.length][ ];
		for (int g=0; g<subGeneStrings.length; g++) {
			subGeneStrings[g] = subgeneinfo[g].toStrings();
			size += subGeneStrings[g].length;
		}
		size += 7;
		size += RVector.RVSIZE;
		String[ ] theStrings = new String[size];
		
		for (int i=0; i<RVector.RVSIZE; i++) {
			theStrings[i] = "    " + RVector.RVCHARS[i] + ":" + Utilities.twoDecimals(cosines[i]);
		}
		int p = RVector.RVSIZE;
		if (BREAKS.length() > 0) {
			theStrings[p++] = BROKENKEY + BREAKS;
		} else {
			theStrings[p++] = "";
		}
		theStrings[p++] = String.valueOf(theDNA.externalize(FIRSTBASEPOS));
		theStrings[p++] = String.valueOf(theDNA.externalize(LASTBASEPOS));
		theStrings[p++] = PAD1 + GENUS + PAD2 + SCORE;
		for (int gg=0; gg<subGeneStrings.length; gg++) {
			for (int ggg=0; ggg<subGeneStrings[gg].length; ggg++) {
				theStrings[p++] = subGeneStrings[gg][ggg];
			}
		}
		theStrings[p++] = "";
		theStrings[p++] = LTRCOMMENT;
		theStrings[p] = "";
		return theStrings;
	} // end of toStrings(DNA)
	
/**
* @return	The first (internal) position in a non-5'LTR.non-PBS Motif hit.
*/
	public final int firstInCore() {
		try {
			int ind = 0;
			if (subgeneinfo[ind].SUBGENENAME.equals(Executor.LTR5KEY)) {
				ind++;
			}
			if (subgeneinfo[ind].SUBGENENAME.equals(SubGene.PBS)) {
				ind++;
			}
			return subgeneinfo[ind].motifhitinfo[0].FIRSTPOS;
		} catch (ArrayIndexOutOfBoundsException e) {
			return FIRSTBASEPOS;
		}
	} // end of firstInCore()

/**
* @return	The last (internal) position in a non-PPT, non-3'LTR Motif hit.
*/
	public final int lastInCore() {
		try {
			int ind = subgeneinfo.length - 1;
			if (subgeneinfo[ind].SUBGENENAME.equals(Executor.LTR3KEY)) {
				ind--;
			}
			if (subgeneinfo[ind].SUBGENENAME.equals(SubGene.PPT)) {
				ind--;
			}
			return subgeneinfo[ind].motifhitinfo[subgeneinfo[ind].motifhitinfo.length - 1].LASTPOS;
		} catch (ArrayIndexOutOfBoundsException e) {
			return LASTBASEPOS;
		}
	} // end of lastInCore()

/**
* @param	genus	The character of a genus.
* @return	The cosine with that genus.
*/
	public final float getCosine(char genus) {
		return cosines[RVector.rvindex(genus)];
	} // end of getCosine(char)

/**
* @return	The lowercase genus char of the best cosine.
*/
	public final char getBestGenus() {
		int besti = 0;
		for (int i=1; i<cosines.length; i++) {
			if (cosines[i] > cosines[besti]) {
				besti = i;
			}
		}
		return Character.toLowerCase(RVector.RVCHARS[besti]);
	} // end of getBestGenus()
	
/**
* @return	Array of (internal) starts and ends of breaks.
*/
	public final int[ ][ ] breaks() {
		return breaks;
	} // end of breaks()
	
/**
* For debugging.
*/
  public void printStrings(DNA theDNA) {
    String[ ] ss = toStrings(theDNA);
    Utilities.outputString("");
    for (int i=0; i<ss.length; i++) {
      Utilities.outputString(ss[i]);
    }
    Utilities.outputString("");
  } // end of printStrings
  
}
