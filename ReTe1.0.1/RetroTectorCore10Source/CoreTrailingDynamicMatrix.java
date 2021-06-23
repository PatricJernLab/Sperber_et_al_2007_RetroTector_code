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
* Class to generate putein after last MotifHit.
*/
public class CoreTrailingDynamicMatrix extends DynamicMatrix {

/**
* A sequence of PathElements.
*/
	public class TrailingPath extends PathClass {
	
/**
* The actual PathElements.
*/
		final PathElement[ ] THEPATH;
		
/**
*	The internal position in PBLOCK.TARGETDNA relative to BOUNDS.x where (backward) Path building starts.
*/
		public final int POSINDNA;

/**
*	The position in PBLOCK.MASTER relative to BOUNDS.y where (backward) Path building starts.
*/
		final int POSINMASTER;
						
/**
* The last element constituting a frame shift.
*/
		final PathElement LASTSHIFT;

/**
* The total number of shifts.
*/
		final int SHIFTCOUNT;
		
/**
* The total number of stop codons.
*/
		final int STOPCOUNT;
		
/**
* Part of acidString ending at LASTSHIFT.
*/
		public final String BEFORESHIFTACIDS;
		
/*
* If the distance between LASTSHIFT and Path end is <= lateShiftMaxLength and the
* distance between preceding shift and LASTSHIFT >= preFrameMinLength, then
* lateShift = true.
*/
		private int lateShiftMaxLength = 18;
		private int preFrameMinLength = 50;
		
		
/**
* @return	Multiline String specifying how COMPARESCORE was calculated and corrected.
*/
		public String toString() {
			return "\nTrailing path in " + PBLOCK.GENENAME + "\nEnds at=" + PBLOCK.TARGETDNA.externalize(getLastInDNA()) + super.toString();
		} // end of TrailingPath.toString
		
		
/**
* Constructor.
* @param	posInDNA			The internal position in PBLOCK.TARGETDNA relative to BOUNDS.x where (backward) Path building starts.
* @param	posInMaster		The position in MASTER relative to PBLOCK.BOUNDS.y where (backward) Path building starts.
* @param	insideBonus		Bias on fetchScore().
*/
		TrailingPath(int posInDNA, int posInMaster, float insideBonus) throws RetroTectorException, FinishedException {
			POSINDNA = posInDNA;
			POSINMASTER = posInMaster;
			Database database = PBLOCK.DATABASE;
			pathGenusChar = Character.toLowerCase(PBLOCK.GENUSCHAR);
			insideLimitsBonus = insideBonus;
			
			parameters = new Hashtable();
			getDefaults();
			alignmentFactor = getFloat(PUTEINENDFACTORKEY, 3.0f);
			lateShiftPenalty = -Math.abs(getFloat(LATESHIFTPENALTYKEY, 0.0f));
			lateShiftMaxLength = getInt(LATESHIFTMAXLENGTHKEY, 18);
			preFrameMinLength = getInt(PREFRAMEMINLENGTHKEY, 50);
			
			int ds = posInDNA; // pointer in DNA
			int ms = posInMaster; // pointer in Alignment
			Stack stack = new Stack(); // to store PathElements
			
			acidStringScore = scoresums[POSINDNA][POSINMASTER];
// build Path step by step backwards
			while (links[ds][ms] != 0) {
				RetroTectorEngine.showProgress();
				if (links[ds][ms] == 19) { // add a regular element to path
					stack.push(new PathElement(ds + BOUNDS.x - 3, ds + BOUNDS.x, ms + BOUNDS.y - 1, ms + BOUNDS.y, SCORES[ds - 3][ms - 1], ROWCODES[ds - 3][ms - 1]));
					ds -= 3;
					ms--;
				} else if (links[ds][ms] == 16) {
					stack.push(new PathElement(ds + BOUNDS.x, ds + BOUNDS.x, ms + BOUNDS.y - 1, ms + BOUNDS.y, 0, 0));
					ms--;
				} else if ((links[ds][ms] == 3) |
						((links[ds][ms] == 1) && (links[ds - 1][ms] == 2)) |
						((links[ds][ms] == 2) && (links[ds - 2][ms] == 1)) |
						((links[ds][ms] == 1) && (links[ds - 1][ms] == 1) && (links[ds - 2][ms] == 1))) {
					stack.push(new PathElement(ds + BOUNDS.x - 3, ds + BOUNDS.x, ms + BOUNDS.y, ms + BOUNDS.y, nonAlignedScores[ds - 3], 0));
					ds -= 3;
				} else if (links[ds][ms] == 2) {
					stack.push(new PathElement(ds + BOUNDS.x - 2, ds + BOUNDS.x, ms + BOUNDS.y, ms + BOUNDS.y, 0, 0));
					ds -= 2;
				} else if (links[ds][ms] == 1) {
					stack.push(new PathElement(ds + BOUNDS.x - 1, ds + BOUNDS.x, ms + BOUNDS.y, ms + BOUNDS.y, 0, 0));
					ds--;
				}
			}
			
// put PathElements into PATH looking out for LASTSHIFT
			PathElement lastShift = null;
			int shiftCount = 0;
			boolean leading = true; // to bypass trailing shifts
// put PathElements into PATH, reversing order
			THEPATH = new PathElement[stack.size()];
			PathElement pe;
			for (int s=THEPATH.length-1; s>=0; s--) {
				pe = (PathElement) stack.elementAt(stack.size() - 1 - s);
				THEPATH[s] = pe;
				if (((pe.TODNA - pe.FROMDNA) == 0) | ((pe.TODNA - pe.FROMDNA) == 3)) {
					leading = false;
				} else if (!leading) {
					shiftCount++;
					if (lastShift == null) {
						lastShift = pe;
					}
				}
			}
			LASTSHIFT = lastShift;
			SHIFTCOUNT = shiftCount;
			
// build acidString
			int stopCount = 0;
			StringBuffer buf = new StringBuffer();
			StringBuffer abuf = new StringBuffer();
			char c;
			boolean early = true;
			for (int p=0; p<THEPATH.length; p++) {
				if (THEPATH[p] == LASTSHIFT) {
					early = false;
				}
				if ((THEPATH[p].TODNA - THEPATH[p].FROMDNA) == 3) {
					c = PBLOCK.TARGETDNA.getAcid(THEPATH[p].FROMDNA);
					if (c == 'z') {
						stopCount++;
					}
					if ((THEPATH[p].TOMASTER - THEPATH[p].FROMMASTER) == 1) {
						c = Character.toUpperCase(c);
					}
					buf.append(c);
					if (early) {
						abuf.append(c);
					}
				}
			}
			STOPCOUNT = stopCount;
			acidString = buf.toString();
			if (LASTSHIFT == null) {
				BEFORESHIFTACIDS = "";
			} else {
				BEFORESHIFTACIDS = abuf.toString();
			}
			int lap = THEPATH[THEPATH.length-1].TODNA;
			for (int sh=THEPATH.length-1; (sh>=0) && (THEPATH[sh].TODNA == lap); sh--) {
				if (((THEPATH[sh].TODNA - THEPATH[sh].FROMDNA) == 1) | ((THEPATH[sh].TODNA - THEPATH[sh].FROMDNA) == 2)) {
					shiftEnd = true;
				}
			}
			
// check for lateShift
			lateShift = false;
			for (int il=THEPATH.length-1; (il>=0) && il>=THEPATH.length-lateShiftMaxLength; il--) {
				if (((THEPATH[il].TODNA - THEPATH[il].FROMDNA) == 1) | ((THEPATH[il].TODNA - THEPATH[il].FROMDNA) == 2)) {
					if (il >= preFrameMinLength) {
						lateShift = true;
						for (int ik=il-1; (ik>=0) && (ik>=il-preFrameMinLength); ik--) {
							if (((THEPATH[ik].TODNA - THEPATH[ik].FROMDNA) == 1) | ((THEPATH[ik].TODNA - THEPATH[ik].FROMDNA) == 2)) {
								lateShift = false;
							}
						}
					}
				}
			}
		} // end of TrailingPath.constructor(int, int, float)
		

/**
* @param	pa	Another TrailingPath.
* @return	True if pa is longer, identical before LASTSHIFT and has an ORF after it.
*/
	public final boolean majoredByPath(TrailingPath pa) {
		if (pa.acidString.length() < acidString.length()) {
			return false;
		}
		if (pa.BEFORESHIFTACIDS.length() >= BEFORESHIFTACIDS.length()) {
			return false;
		}
		int ind = BEFORESHIFTACIDS.length();
		String s = pa.acidString.substring(0, ind);
		if (!s.equalsIgnoreCase(BEFORESHIFTACIDS)) {
			return false;
		}
		int indx = pa.acidString.indexOf(Compactor.STOPCHAR, ind);
		if (indx < 0) {
			return true;
		}
		return false;
	} // end of TrailingPath.majoredByPath(TrailingPath)
	
		
/**
* @return THEPATH.
*/
		public final PathElement[ ] getPath() {
			return THEPATH;
		} // end of TrailingPath.getPath()
		
/**
* @return	The number of stop codons in the Path.
*/
		public final float getStopCount() {
			return STOPCOUNT;
		} // end of TrailingPath.getStopCount()
	
/**
* @return	The number of shifts in the Path.
*/
		public final float getShiftCount() {
			return SHIFTCOUNT;
		} // end of TrailingPath.getShiftCount()
	
/**
* @return	The gene of the Putein where this Path belongs.
*/
		public final String getGeneName() {
			return PBLOCK.GENENAME;
		} // end of TrailingPath.getGeneName()
	
/**
* @return	The ORFID.ParameterBlock used.
*/
		public final ORFID.ParameterBlock getParameterBlock() {
			return PBLOCK;
		} // end of TrailingPath.getParameterBlock()
		
	} // end of TrailingPath
	

	
/**
* Non-aligned score may not exceed this.
*/
	public static final float NONALIGNEDSCOREBOUND = 0.85f;
	
/**
* Array of all considered Paths.
*/
	public final TrailingPath[ ] ALLPATHS;
	
/**
* A position relative to BOUNDS.x. Paths starting no earlier are prioritized.
*/
	public final int INSIDELIMIT;
		
	private final Putein PUTEIN;
	
	private final StopCodonModifier STOPMODIFIER;
	private final ORFHexamerModifier ORFMODIFIER;
	private final NonORFHexamerModifier NONORFMODIFIER;
	private final GlycSiteModifier GLYCMODIFIER;
	
// scoresums are valid up to this DNA index. Not used at present
	private int scoresumsValidTo = -Integer.MAX_VALUE;


/**
* Constructor.
* @param	rect				Defines segment of DNA (x and width) and alignment (y and height) to work in.
* @param	par					Collection of useful parameters.
* @param	put					Putein to receive result.
* @param	insideLimit	A position relative to BOUNDS.x. Paths ending no earlier are prioritized.
*/
	public CoreTrailingDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock par, Putein put, int insideLimit) throws RetroTectorException, FinishedException {
	
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
		ALLPATHS = new TrailingPath[BOUNDS.width - 4];
		TrailingPath pat;
		int pp = 0;
		for (int p=BOUNDS.width-1; p>=4; p--) {
			RetroTectorEngine.showProgress();
			updatescoresums(p);
			if (p >= INSIDELIMIT) {
				pat = new TrailingPath(p, BOUNDS.height - 1, 1.0f);
			} else {
				pat = new TrailingPath(p, BOUNDS.height - 1, 0);
			}

			for (int ip=0; ip<pp; ip++) {
				if (pat.majoredByPath(ALLPATHS[ip])) {
					pat.majored = true;
				}
			}

			ALLPATHS[pp++] = pat;
		}
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock, Putein, int)
	
	
// returns Path starting at posInDNA
	private final TrailingPath getPathWith(int posInDNA) throws RetroTectorException {
		int pInDNA = posInDNA - BOUNDS.x;
		if ((BOUNDS.width - 1 - pInDNA - 1 < 0) || (BOUNDS.width - 1 - pInDNA - 1 >= ALLPATHS.length)) {
			return null;
		}
		TrailingPath pat = ALLPATHS[BOUNDS.width - 1 - pInDNA - 1];
		if (pat.getLastInDNA() != posInDNA) {
			throw new RetroTectorException("CoreTrailingDynamicMatrix", "getPathWith mismatch " + pat.getLastInDNA() + " " + posInDNA);
		}
		return pat;
	} // end of getPathWith(int)
	

// rebuilds scoresums and links disregarding stop codons after startAt
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

    for (int m=0; (m<BOUNDS.width) && (m<=startAt+1); m++) {
      nonAlignedScores[m] += (PBLOCK.NONALIGNEDSCORE +
          STOPMODIFIER.stopCodonModifierAt(BOUNDS.x + m, -Integer.MAX_VALUE, BOUNDS.x + startAt) * PBLOCK.STOPCODONFACTOR +
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
		int wi = Math.max(1, startAt - StopCodonModifier.STOPCODONRANGE - 1);

		for (int m=0; m<he; m++) {
			RetroTectorEngine.showProgress();
			for (int d=wi; d<=startAt; d++) {
// first try 1 base step
				try {
					temps = scoresums[d - 1][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
				} catch (ArrayIndexOutOfBoundsException e) {
					throw e;
				}
				templ = 1;
				if (d > 1) { // then try 2 base step
					temps2 = scoresums[d - 2][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
					if (temps2 > temps) {
						temps = temps2;
						templ = 2;
					}
				}
				if (d > 2) { // then try codon step
					temps2 = scoresums[d - 3][m] + nonAlignedScores[d];
					if (PBLOCK.INORF[BOUNDS.x + d]) {
						temps2 += PBLOCK.INORFBONUS;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 3;
					}
				}
				if (m > 0) { // try step in master
					temps2 = scoresums[d][m - 1];
					if (PBLOCK.MASTER.NODASH[m - 1]) {
						temps2 += PBLOCK.MASTERPENALTY;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 16;
					}
					if (d > 2) { // then try aligned step
						temps2 = scoresums[d - 3][m - 1] + SCORES[d - 3][m - 1];
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
		scoresumsValidTo = startAt;
	} // end of updatescoresums(int)
	
	
// fills scoresums and links
	private final void fillscoresums(int wi, int he) throws RetroTectorException {
// build nonAlignedScores
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

    for (int m=0; m<BOUNDS.width; m++) {
      nonAlignedScores[m] += (PBLOCK.NONALIGNEDSCORE +
          STOPMODIFIER.modification(BOUNDS.x + m) * PBLOCK.STOPCODONFACTOR +
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
			scoresums[0][i] = 0;
			links[0][i] = (byte) 16;
			links[0][0] = (byte) 0;
		}
		for (int m=0; m<he; m++) {
			RetroTectorEngine.showProgress();
			for (int d=1; d<wi; d++) {
// first try 1 base step
				temps = scoresums[d - 1][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
				templ = 1;
				if (d > 1) { // then try 2 base step
					temps2 = scoresums[d - 2][m] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
					if (temps2 > temps) {
						temps = temps2;
						templ = 2;
					}
				}
				if (d > 2) { // then try codon step
					temps2 = scoresums[d - 3][m] + nonAlignedScores[d];
					if (PBLOCK.INORF[BOUNDS.x + d]) {
						temps2 += PBLOCK.INORFBONUS;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 3;
					}
				}
				if (m > 0) { // try step in master
					temps2 = scoresums[d][m - 1];
					if (PBLOCK.MASTER.NODASH[m - 1]) {
						temps2 += PBLOCK.MASTERPENALTY;
					}
					if (temps2 > temps) {
						temps = temps2;
						templ = 16;
					}
					if (d > 2) { // then try aligned step
						temps2 = scoresums[d - 3][m - 1] + SCORES[d - 3][m - 1];
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
		scoresumsValidTo = -Integer.MAX_VALUE;
	} // end of fillscoresums(int, int)
	
/**
* @return	Array of possible Paths, in topological order.
*/
	public final PathClass[ ] getPaths() {
		return (TrailingPath[ ]) ALLPATHS.clone();
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
* @param	posInDNA		Last base position (internal) in PathClass.
* @return	Such a Path, or null.
*/
	public final PathClass getPathEndingAt(int posInDNA) throws RetroTectorException {
		return getPathWith(posInDNA);
	} // end of getPathEndingAt(int)
	
} // end of CoreTrailingDynamicMatrix
