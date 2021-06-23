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
* Class to generate putein between two MotifHits.
*/
public class CoreMiddleDynamicMatrix extends DynamicMatrix {

	
/**
* A sequence of PathElements.
*/
	private class MiddlePath extends PathClass {
	
/**
* The actual PathElements.
*/
		final PathElement[ ] THEPATH;
		
/**
*	The internal position in TARGETDNA relative to BOUNDS.x where Path building starts. In practice = 0.
*/
		final int POSINDNA;

/**
*	The position in MASTER relative to BOUNDS.y where Path building starts. In practice = 0.
*/
		final int POSINMASTER;

/**
* The total number of shifts.
*/
		final int SHIFTCOUNT;
		
/**
* The total number of stop codons.
*/
		final int STOPCOUNT;
		
		
/**
* Constructor.
* @param	posInDNA			The internal position in TARGETDNA relative to BOUNDS.x where Path building starts.
* @param	posInMaster		The position in MASTER relative to BOUNDS.y where Path building starts.
*/
		MiddlePath(int posInDNA, int posInMaster) throws RetroTectorException, FinishedException {
			POSINDNA = posInDNA;
			POSINMASTER = posInMaster;
			Database database = PBLOCK.DATABASE;
			pathGenusChar = Character.toLowerCase(PBLOCK.GENUSCHAR);
			
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
			} catch (ArrayIndexOutOfBoundsException e) {
				throw e; // for debugging
			}
			
// put PathElements into PATH
			THEPATH = new PathElement[stack.size()];
			stack.copyInto(THEPATH);
			int shiftCount = 0;
			int stopCount = 0;
			int di;
			for (int i=0; i<THEPATH.length; i++) {
				di = THEPATH[i].TODNA - THEPATH[i].FROMDNA;
				if (di == 0) {
				} else if (di == 3) {
					if(PBLOCK.TARGETDNA.getAcid(THEPATH[i].FROMDNA) == 'z') {
						stopCount++;
					}
				} else {
					shiftCount++;
				}
			}
			STOPCOUNT = stopCount;
			SHIFTCOUNT = shiftCount;
		} // end of MiddlePath.constructor(int, int)
						
/**
* @return THEPATH.
*/
		public final PathElement[ ] getPath() {
			return THEPATH;
		} // end of MiddlePath.getPath()
		
/**
* @return	The number of stop codons in the Path.
*/
		public final float getStopCount() {
			return STOPCOUNT;
		} // end ofMiddlePath.getStopCount()
	
/**
* @return	The number of shifts in the Path.
*/
		public final float getShiftCount() {
			return SHIFTCOUNT;
		} // end of MiddlePath.getShiftCount()
	
/**
* @return	The gene of the Putein where this Path belongs.
*/
		public final String getGeneName() {
			return PBLOCK.GENENAME;
		} // end of MiddlePath.getGeneName()
	
/**
* @return	The ORFID.ParameterBlock used.
*/
		public final ORFID.ParameterBlock getParameterBlock() {
			return PBLOCK;
		} // end of MiddlePath.getParameterBlock()
		
	} // end of MiddlePath
	

/**
* Non-aligned score may not exceed this.
*/
	public static final float NONALIGNEDSCOREBOUND = 0.85f;
	  	
	private final Putein PUTEIN;
	
	private final MiddlePath THEPATH;

/**
* Constructor.
* @param	rect	DNA (x-direction) and master (y-direction) ranges to include.
* @param	par		Collection of useful parameters.
* @param	put		Putein to receive result.
*/
	public CoreMiddleDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock par, Putein put) throws RetroTectorException, FinishedException {
		
		PBLOCK = par;
		BOUNDS = rect;
		SCORES = new float[BOUNDS.width][BOUNDS.height];
		ROWCODES = new int[BOUNDS.width][BOUNDS.height];
		
		isEnv = "env".equalsIgnoreCase(PBLOCK.GENENAME);
		isGag = "gag".equalsIgnoreCase(PBLOCK.GENENAME);
		isPol = "pol".equalsIgnoreCase(PBLOCK.GENENAME);
		isPro = "pro".equalsIgnoreCase(PBLOCK.GENENAME);
		
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

    nonAlignedScores = new float[BOUNDS.width];
    StopCodonModifier stopMod = PBLOCK.TARGETDNA.getStopCodonModifier();
    ORFHexamerModifier orfMod = PBLOCK.TARGETDNA.getORFHexamerModifier();
    NonORFHexamerModifier nonOrfMod = PBLOCK.TARGETDNA.getNonORFHexamerModifier();
    GlycSiteModifier glycMod = PBLOCK.TARGETDNA.getGlycSiteModifier();

// make bonus for paths passing through MotifHit
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
          stopMod.modification(BOUNDS.x + m) * PBLOCK.STOPCODONFACTOR +
          orfMod.modification(BOUNDS.x + m) * PBLOCK.ORFHEXAMERFACTOR +
          nonOrfMod.modification(BOUNDS.x + m) * PBLOCK.NONORFHEXAMERFACTOR);
      if (isEnv) {
        nonAlignedScores[m] += glycMod.modification(BOUNDS.x + m) * PBLOCK.GLYCSITEFACTOR;
      }
// bound non-aligned score
			nonAlignedScores[m] = Math.min(nonAlignedScores[m], NONALIGNEDSCOREBOUND);
    }
		
    
		fillscoresums(BOUNDS.width, BOUNDS.height);
		THEPATH = new MiddlePath(0, 0);
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock, Putein)
	

// fills scoresums and links
	private final void fillscoresums(int wi, int he) throws RetroTectorException {
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
			try {
				scoresums[wi - 1][i] = 0;
				links[wi - 1][i] = (byte) 16;
			} catch (ArrayIndexOutOfBoundsException ae) {
				throw ae;
			}
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
	} // end of fillscoresums(int, int)
	
/**
* @return		The Path cpnstructed by this.
*/
	public final MiddlePath getPath() {
		return THEPATH;
	} // end of getPath()
	
} // end of CoreMiddleDynamicMatrix
