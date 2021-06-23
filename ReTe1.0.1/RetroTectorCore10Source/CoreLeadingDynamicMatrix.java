/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 24/9 -06
* Beautified 24/9 -06
*/

package retrotectorcore;

import retrotector.*;
import builtins.*;
import java.util.*;

/**
* Class to generate putein up to first MotifHit.
*/
public class CoreLeadingDynamicMatrix extends DynamicMatrix {

/**
* A sequence of PathElements.
*/
	private class LeadingPath extends PathClass {
		
/**
* The actual PathElements.
*/
		final PathElement[ ] THEPATH;
		
/**
*	The internal position in PBLOCK.TARGETDNA relative to BOUNDS.x where Path building starts.
*/
		final int POSINDNA;

/**
*	The position in PBLOCK.MASTER relative to BOUNDS.y where Path building starts.
*/
		final int POSINMASTER;

/**
* The first element constituting a frame shift.
*/
		final PathElement FIRSTSHIFT;
		
/**
* The total number of shifts.
*/
		final int SHIFTCOUNT;
		
/**
* The total number of stop codons.
*/
		final int STOPCOUNT;
		
/**
* Part of acidString starting after FIRSTSHIFT.
*/
		public final String AFTERSHIFTACIDS;

/**
* @return	Multiline String specifying how fetchScore was calculated and corrected.
*/
		public String toString() {
			return "\nLeading path in " + PBLOCK.GENENAME + "\nStarts at=" + PBLOCK.TARGETDNA.externalize(getFirstInDNA()) + super.toString();
		} // end of LeadingPath.toString()
		
		
/**
* Constructor.
* @param	posInDNA			The internal position in PBLOCK.TARGETDNA relative to BOUNDS.x where Path building starts.
* @param	posInMaster		The position in PBLOCK.MASTER relative to BOUNDS.y where Path building starts.
* @param	insideBonus		Bias on fetchScore().
*/
		LeadingPath(int posInDNA, int posInMaster, float insideBonus) throws RetroTectorException, FinishedException {
			POSINDNA = posInDNA;
			POSINMASTER = posInMaster;
			Database database = PBLOCK.DATABASE;
			pathGenusChar = Character.toLowerCase(PBLOCK.GENUSCHAR);
			insideLimitsBonus = insideBonus;

			int ds = posInDNA;
			int ms = posInMaster;
			int dmax = links.length - 1;
			int mmax = links[0].length - 1;
			Stack stack = new Stack();
			
			acidStringScore = scoresums[POSINDNA][POSINMASTER];
// build Path step by step
			try {
				while (links[ds][ms] != 0) {
					RetroTectorEngine.showProgress();
					if (links[ds][ms] == 19) { // add a regular element to path
						stack.push(new PathElement(ds + BOUNDS.x, ds + 3 + BOUNDS.x, ms + BOUNDS.y, ms + 1 + BOUNDS.y, SCORES[ds][ms], ROWCODES[ds][ms]));
						ds += 3;
						ms++;
					} else if (links[ds][ms] == 16) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + BOUNDS.x, ms + BOUNDS.y, ms + 1 + BOUNDS.y, 0, 0));
						ms++;
					} else if ((links[ds][ms] == 3) |
							((links[ds][ms] == 1) && (links[ds + 1][ms] == 2)) |
							((links[ds][ms] == 2) && (links[ds + 2][ms] == 1)) |
							((links[ds][ms] == 1) && (links[ds + 1][ms] == 1) && (links[ds + 2][ms] == 1))) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + 3 + BOUNDS.x, ms + BOUNDS.y, ms + BOUNDS.y, nonAlignedScores[ds], 0));
						ds += 3;
					} else if (links[ds][ms] == 2) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + 2 + BOUNDS.x, ms + BOUNDS.y, ms + BOUNDS.y, 0, 0));
						ds += 2;
					} else if (links[ds][ms] == 1) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + 1 + BOUNDS.x, ms + BOUNDS.y, ms + BOUNDS.y, 0, 0));
						ds++;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) { // for debugging
				throw e;
			}
			
// put PathElements into PATH
			THEPATH = new PathElement[stack.size()];
			PathElement pe;
			for (int s=0; s<stack.size(); s++) {
				pe = (PathElement) stack.elementAt(s);
				THEPATH[s] = pe;
			}
			
// build acidString
			PathElement firstShift = null;
			int shiftCount = 0;
			int stopCount = 0;
			StringBuffer buf = new StringBuffer();
			StringBuffer abuf = new StringBuffer();
			char c;
			boolean leading = true; // looking at leading shifts
			for (int p=0; p<THEPATH.length; p++) {
				if ((THEPATH[p].TODNA - THEPATH[p].FROMDNA) == 3) {
					leading = false;
					c = PBLOCK.TARGETDNA.getAcid(THEPATH[p].FROMDNA);
					if (c == 'z') {
						stopCount++;
					}
					if ((THEPATH[p].TOMASTER - THEPATH[p].FROMMASTER) == 1) {
						c = Character.toUpperCase(c);
					}
					buf.append(c);
					if (firstShift != null) {
						abuf.append(c);
					}
				} else if ((THEPATH[p].TODNA - THEPATH[p].FROMDNA) != 0) {
					if (!leading) {
						shiftCount++;
						if (firstShift == null) {
							firstShift = THEPATH[p];
						}
					}
				}
			}
			acidString = buf.toString();
			AFTERSHIFTACIDS = abuf.toString();
			FIRSTSHIFT = firstShift;
			int lap = THEPATH[0].FROMDNA;
			for (int sh=0; (sh<THEPATH.length) && (THEPATH[sh].FROMDNA == lap); sh++) {
				if (((THEPATH[sh].TODNA - THEPATH[sh].FROMDNA) == 1) | ((THEPATH[sh].TODNA - THEPATH[sh].FROMDNA) == 2)) {
					shiftEnd = true;
				}
			}
			SHIFTCOUNT = shiftCount;
			STOPCOUNT = stopCount;
									
		} // end of LeadingPath.constructor(int, int, float)
		
/**
* @param	pa	Another LeadingPath.
* @return	True if pa is longer, identical after FIRSTSHIFT and has an ORF up to it.
*/
	public final boolean majoredByPath(LeadingPath pa) {
		if (pa.acidString.length() < acidString.length()) { //pa is shorter
			return false;
		}
		if (pa. AFTERSHIFTACIDS.length() >= AFTERSHIFTACIDS.length()) { //pa has shift before FIRSTSHIFT
			return false;
		}
		int ind = pa.acidString.length() - AFTERSHIFTACIDS.length();
		String s = pa.acidString.substring(ind);
		if (!s.equalsIgnoreCase(AFTERSHIFTACIDS)) {
			return false; // not identical after FIRSTSHIFT
		}
		int indx = pa.acidString.indexOf(Compactor.STOPCHAR);
		if (indx < 0) {
			return true;
		}
		if (indx > ind) {
			return true;
		}
		return false; // stop: not ORF
	} // end of LeadingPath.majoredByPath(LeadingPath)
	
/**
* @return THEPATH.
*/
		public final PathElement[ ] getPath() {
			return THEPATH;
		} // end of LeadingPath.getPath()
		
/**
* @return	The number of stop codons in the Path.
*/
		public final float getStopCount() {
			return STOPCOUNT;
		} // end of LeadingPath.getStopCount()
	
/**
* @return	The number of shifts in the Path.
*/
		public final float getShiftCount() {
			return SHIFTCOUNT;
		} // end of LeadingPath.getShiftCount()
	
/**
* @return	The gene of the Putein where this Path belongs.
*/
		public final String getGeneName() {
			return PBLOCK.GENENAME;
		} // end of LeadingPath.getGeneName()
	
/**
* @return	The ORFID.ParameterBlock used.
*/
		public final ORFID.ParameterBlock getParameterBlock() {
			return PBLOCK;
		} // end of LeadingPath.getParameterBlock()
		
	} // end of LeadingPath



/**
* Non-aligned score may not exceed this.
*/
	public static final float NONALIGNEDSCOREBOUND = 0.85f;
	
/**
* Array of all considered Paths.
*/
	public final LeadingPath[ ] ALLPATHS;
	
/**
* A position relative to BOUNDS.x. Paths starting no later are prioritized.
*/
	public final int INSIDELIMIT;
		
	private final Putein PUTEIN;
	  
	private final StopCodonModifier STOPMODIFIER;
	private final ORFHexamerModifier ORFMODIFIER;
	private final NonORFHexamerModifier NONORFMODIFIER;
	private final GlycSiteModifier GLYCMODIFIER;

// scoresums are valid starting at this DNA index and up. Not used at present
	private int scoresumsValidFrom = Integer.MAX_VALUE;

/**
* Constructor.
* @param	rect				Defines segment of DNA (x and width) and alignment (y and height) to work in.
* @param	block				Collection of useful parameters.
* @param	put					Putein to receive result.
* @param	insideLimit	A position relative to BOUNDS.x. Paths starting no later are prioritized.
*/
	public CoreLeadingDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock par, Putein put, int insideLimit) throws RetroTectorException, FinishedException {
		
		INSIDELIMIT = insideLimit;

		PBLOCK = par;
		BOUNDS = rect;
		SCORES = new float[BOUNDS.width][BOUNDS.height];
		ROWCODES = new int[BOUNDS.width][BOUNDS.height];
		
		isEnv = PBLOCK.GENENAME.equalsIgnoreCase("env");
		isGag = PBLOCK.GENENAME.equalsIgnoreCase("gag");
		isPol = PBLOCK.GENENAME.equalsIgnoreCase("pol");
		isPro = PBLOCK.GENENAME.equalsIgnoreCase("pro");
		
		PUTEIN = put;

		int temp;
    float acidNormalizer = 1.0f / Matrix.acidMATRIX.MAXACIDSCORE;
// make SCORES and ROWCODES
		for (int d=0; d<BOUNDS.width; d++) {
			RetroTectorEngine.showProgress();
			for (int m=0; m<BOUNDS.height; m++) {
				temp = PBLOCK.MASTER.scoreAt(PBLOCK.TARGETDNA, BOUNDS.x + d, BOUNDS.y + m, PBLOCK.STOPVALUE);
				ROWCODES[d][m] = temp;
				SCORES[d][m] = PBLOCK.MASTER.latestScore * acidNormalizer;
			}
		}
    STOPMODIFIER = PBLOCK.TARGETDNA.getStopCodonModifier();
    ORFMODIFIER = PBLOCK.TARGETDNA.getORFHexamerModifier();
    NONORFMODIFIER = PBLOCK.TARGETDNA.getNonORFHexamerModifier();
    GLYCMODIFIER = PBLOCK.TARGETDNA.getGlycSiteModifier();

		fillscoresums(BOUNDS.width, BOUNDS.height);
// generate all reasonable Paths
		ALLPATHS = new LeadingPath[BOUNDS.width - 4];
		LeadingPath pat;
		int pp = 0;
		for (int p=0; p<(BOUNDS.width-4); p++) {
			RetroTectorEngine.showProgress();
			updatescoresums(p);
			if (p <= INSIDELIMIT) {
				pat = new LeadingPath(p, 0, 1.0f);
			} else {
				pat = new LeadingPath(p, 0, 0);
			}
			for (int ip=0; ip<pp; ip++) {
				if (pat.majoredByPath(ALLPATHS[ip])) {
					pat.majored = true;
				}
			}
			ALLPATHS[pp++] = pat;
		}
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock, Putein, int)
	
// returns Path starting at posInDNA (absolute internal)
	private final LeadingPath getPathWith(int posInDNA) throws RetroTectorException {
		posInDNA = posInDNA - BOUNDS.x;
		if ((posInDNA < 0) || (posInDNA >= ALLPATHS.length)) {
			return null;
		}
		LeadingPath pat = null;
		try {
			pat = ALLPATHS[posInDNA];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw e; // for debugging
		}
		if (pat.POSINDNA != posInDNA) {
			throw new RetroTectorException("CoreLeadingDynamicMatrix", "getPathWith mismatch " + pat.POSINDNA + " " + posInDNA);
		}
		return pat;
	} // end of getPathWith(int)
	

// rebuilds scoresums and links disregarding stop codons before startAt relative to BOUNDS.x
	private final void updatescoresums(int startAt) throws RetroTectorException {
    nonAlignedScores = new float[BOUNDS.width];
// make bonus for paths passing through MotifHit hotspot
		MotifHitInfo mhi;
		int hitlength;
		int startpos;
		int apos;
		for (int mi=0; (mhi = PBLOCK.HITSINFO.getHitInfo(mi)) != null; mi++) {
			if ((mhi.HOTSPOT > BOUNDS.x) & (mhi.HOTSPOT < (BOUNDS.x + BOUNDS.width - 1))) {
				if (PBLOCK.DATABASE.getMotifGroup(mhi.MOTIFGROUP).groupFrameDefined & mhi.HITMOTIFTYPE.equals(AcidMotif.classType())) {
					hitlength = mhi.ACIDSLENGTH;
					startpos = mhi.startsAt(); // absolute internal hotspot position
					for (int u=0; u<hitlength; u++) {
						if ((apos = startpos + u * 3 - BOUNDS.x) >= 0) {
							if (apos < nonAlignedScores.length) {
								nonAlignedScores[apos] = PBLOCK.MOTIFHITFACTOR * mhi.SCORE / hitlength;
							}
						}
					}
				}
			}
		}

		for (int m=startAt; m<BOUNDS.width; m++) {
      nonAlignedScores[m] += (PBLOCK.NONALIGNEDSCORE +
          STOPMODIFIER.stopCodonModifierAt(BOUNDS.x + m, BOUNDS.x + startAt, Integer.MAX_VALUE) * PBLOCK.STOPCODONFACTOR +
          ORFMODIFIER.modification(BOUNDS.x + m) * PBLOCK.ORFHEXAMERFACTOR +
          NONORFMODIFIER.modification(BOUNDS.x + m) * PBLOCK.NONORFHEXAMERFACTOR);
      if (isEnv) {
        nonAlignedScores[m] += GLYCMODIFIER.modification(BOUNDS.x + m) * PBLOCK.GLYCSITEFACTOR;
      }
// bound non-aligned score
			nonAlignedScores[m] = Math.min(nonAlignedScores[m], NONALIGNEDSCOREBOUND);
		}
		
		float temps;
		float temps2;
		int templ;
		int he = BOUNDS.height;
		int wi = Math.min(BOUNDS.width, startAt + StopCodonModifier.STOPCODONRANGE + 1);
		
		for (int m=he-1; m>=0; m--) {
			RetroTectorEngine.showProgress();
			for (int d=wi-2; d>=startAt; d--) {
// first try 1 base step
				temps = scoresums[d + 1][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
				templ = 1;
				if (d < (BOUNDS.width-2)) { // then try 2 base step
					temps2 = scoresums[d + 2][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
					if (temps2 > temps) {
						temps = temps2;
						templ = 2;
					}
				}
				if (d < (BOUNDS.width - 3)) { // then try codon step
					temps2 = scoresums[d + 3][m] + nonAlignedScores[d];
					if (PBLOCK.INORF[BOUNDS.x + d]) {
						temps2 += PBLOCK.INORFBONUS;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 3;
					}
				}
				if (m < (he - 1)) { // try step in master
					temps2 = scoresums[d][m + 1];
					if (PBLOCK.MASTER.NODASH[m]) {
						temps2 += PBLOCK.MASTERPENALTY; // for skipping full column in master
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 16;
					}
					if (d < (BOUNDS.width - 3)) { // then try aligned step
						temps2 = scoresums[d + 3][m + 1] + SCORES[d][m];
						if (PBLOCK.INORF[BOUNDS.x + d]) {
							temps2 += PBLOCK.INORFBONUS;
						}
						if (temps2 > temps) {
							temps = temps2;
							templ = 16 + 3;
						}
					}
				}
				scoresums[d][m] = temps;
				links[d][m] = (byte) templ;
			}
		}
		scoresumsValidFrom = startAt;
	} // end of updatescoresums(int)
	
// fills scoresums and links
	private void fillscoresums(int wi, int he) throws RetroTectorException {
    nonAlignedScores = new float[BOUNDS.width];
// make bonus for paths passing through MotifHit hotspot
		MotifHitInfo mhi;
		int hitlength;
		int startpos;
		int apos;
		for (int mi=0; (mhi = PBLOCK.HITSINFO.getHitInfo(mi)) != null; mi++) {
			if ((mhi.HOTSPOT > BOUNDS.x) & (mhi.HOTSPOT < (BOUNDS.x + BOUNDS.width - 1))) {
				if (PBLOCK.DATABASE.getMotifGroup(mhi.MOTIFGROUP).groupFrameDefined & mhi.HITMOTIFTYPE.equals(AcidMotif.classType())) {
					hitlength = mhi.ACIDSLENGTH;
					startpos = mhi.startsAt();
					for (int u=0; u<hitlength; u++) {
						if ((apos = startpos + u * 3 - BOUNDS.x) >= 0) {
							if (apos < nonAlignedScores.length) {
								nonAlignedScores[apos] = PBLOCK.MOTIFHITFACTOR * mhi.SCORE / hitlength;
							}
						}
					}
				}
			}
		}

// build nonAlignedScores
    for (int m=0; m<BOUNDS.width; m++) {
      nonAlignedScores[m] += (PBLOCK.NONALIGNEDSCORE +
          STOPMODIFIER.stopCodonModifierAt(BOUNDS.x + m, BOUNDS.x, Integer.MAX_VALUE) * PBLOCK.STOPCODONFACTOR +
          ORFMODIFIER.modification(BOUNDS.x + m) * PBLOCK.ORFHEXAMERFACTOR +
          NONORFMODIFIER.modification(BOUNDS.x + m) * PBLOCK.NONORFHEXAMERFACTOR);
      if (isEnv) {
        nonAlignedScores[m] += GLYCMODIFIER.modification(BOUNDS.x + m) * PBLOCK.GLYCSITEFACTOR;
      }
// bound non-aligned score
			nonAlignedScores[m] = Math.min(nonAlignedScores[m], NONALIGNEDSCOREBOUND);
    }
		
		try {
			scoresums = new float[wi][he];
			links = new byte[wi][he];
		} catch (OutOfMemoryError me) {
			throw new RetroTectorException(Utilities.className(this), "Out of memory creating scoresums or links");
		}
    
		float temps;
		float temps2;
		int templ;

// build scoresums
		for (int i=0; i<he; i++) {
			scoresums[wi - 1][i] = 0;
			links[wi - 1][i] = (byte) 16;
		}
		links[wi - 1][he - 1] = (byte) 0;
		for (int m=he-1; m>=0; m--) {
			RetroTectorEngine.showProgress();
			for (int d=wi-2; d>=0; d--) {
// first try 1 base step
				temps = scoresums[d + 1][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
				templ = 1;
				if (d < (wi-2)) { // then try 2 base step
					temps2 = scoresums[d + 2][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
					if (temps2 > temps) {
						temps = temps2;
						templ = 2;
					}
				}
				if (d < (wi-3)) { // then try codon step
					temps2 = scoresums[d + 3][m] + nonAlignedScores[d];
					if (PBLOCK.INORF[BOUNDS.x + d]) {
						temps2 += PBLOCK.INORFBONUS;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 3;
					}
				}
				if (m < (he - 1)) { // try step in master
					temps2 = scoresums[d][m + 1];
					if (PBLOCK.MASTER.NODASH[m]) {
						temps2 += PBLOCK.MASTERPENALTY;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 16;
					}
					if (d < (wi-3)) { // then try aligned step
						temps2 = scoresums[d + 3][m + 1] + SCORES[d][m];
						if (PBLOCK.INORF[BOUNDS.x + d]) {
							temps2 += PBLOCK.INORFBONUS;
						}
						if (temps2 > temps) {
							temps = temps2;
							templ = 16 + 3;
						}
					}
				}
				scoresums[d][m] = temps;
				links[d][m] = (byte) templ;
			}
		}
		scoresumsValidFrom = Integer.MAX_VALUE;
	} // end of fillscoresums(int, int)
	
/**
* @return	Array of possible Paths, in topological order.
*/
	public final PathClass[ ] getPaths() {
		return (LeadingPath[ ]) ALLPATHS.clone();
	} // end of getPaths()
	
/**
* @return	The highest scoring Path.
*/
	public final PathClass getBestPath() throws RetroTectorException{
		PathClass[ ] paths = getPaths();
		Utilities.sort(paths);
		return paths[0];				
	} // end of getBestPath()
	
/**
* @param	posInDNA		First base position (internal) in PathClass.
* @return	Such a PathClass, or null.
*/
	public final PathClass getPathStartingAt(int posInDNA) throws RetroTectorException {
		return getPathWith(posInDNA);
	} // end of getPathStartingAt(int)
	
} // end of CoreLeadingDynamicMatrix
