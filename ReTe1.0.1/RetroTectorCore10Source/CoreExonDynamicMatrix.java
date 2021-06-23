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
* Class to generate putein without Alignment.
*/
public class CoreExonDynamicMatrix extends DynamicMatrix {

	
/**
* A sequence of PathElements.
*/
	private class ExonPath extends PathClass {
	
/**
* The actual PathElements.
*/
		final PathElement[ ] THEPATH;
		
/**
*	The internal position in TARGETDNA relative to BOUNDS.x where Path building starts.
*/
		final int POSINDNA;

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
*/
		ExonPath(int posInDNA) throws RetroTectorException, FinishedException {
			POSINDNA = posInDNA;
			Database database = PBLOCK.DATABASE;
			pathGenusChar = Character.toLowerCase(PBLOCK.GENUSCHAR);

			
			int ds = posInDNA;
			int dmax = links.length - 1;
			Stack stack = new Stack();
		
			acidStringScore = scoresums[POSINDNA];
// build Path step by step
			try {
				while (links[ds] != 0) {
					RetroTectorEngine.showProgress();
					if ((links[ds] == 3) |
							((links[ds] == 1) && (links[ds + 1] == 2)) |
							((links[ds] == 2) && (links[ds + 2] == 1)) |
							((links[ds] == 1) && (links[ds + 1] == 1) && (links[ds + 2] == 1))) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + 3 + BOUNDS.x, BOUNDS.y, BOUNDS.y, nonAlignedScores[ds], 0));
						ds += 3;
					} else if (links[ds] == 2) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + 2 + BOUNDS.x, BOUNDS.y, BOUNDS.y, 0, 0));
						ds += 2;
					} else if (links[ds] == 1) {
						stack.push(new PathElement(ds + BOUNDS.x, ds + 1 + BOUNDS.x, BOUNDS.y, BOUNDS.y, 0, 0));
						ds++;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw e;
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
		} // end of ExonPath.constructor(int)

/**
* @return THEPATH.
*/
		public final PathElement[ ] getPath() {
			return THEPATH;
		} // end of ExonPath.getPath()
		
/**
* @return	The number of stop codons in the Path.
*/
		public final float getStopCount() {
			return STOPCOUNT;
		} // end of ExonPath.getStopCount
	
/**
* @return	The number of shifts in the Path.
*/
		public final float getShiftCount() {
			return SHIFTCOUNT;
		} // end of ExonPath.getShiftCount()
	
/**
* @return	The gene of the Putein where this Path belongs.
*/
		public final String getGeneName() {
			return PBLOCK.GENENAME;
		} // end of ExonPath.getGeneName()
	
/**
* @return	The ORFID.ParameterBlock used.
*/
		public final ORFID.ParameterBlock getParameterBlock() {
			return PBLOCK;
		} // end of ExonPath.getParameterBlock()
		
	} // end of ExonPath.
	
	  	
	private ExonPath THEPATH;

// showing best steps in matrix
	private byte[ ] links;
// one-dimensional dynamic matrix
	private float[ ] scoresums;


/**
* Constructor.
* @param	rect	Defines segment of DNA to work in. Only x and width are relevant.
* @param	block	Collection of useful parameters.
*/
	public CoreExonDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock par) throws RetroTectorException, FinishedException {
		
		PBLOCK = par;
		BOUNDS = new Utilities.Rectangle(rect);
		isEnv = "env".equalsIgnoreCase(PBLOCK.GENENAME);
		isGag = "gag".equalsIgnoreCase(PBLOCK.GENENAME);
		isPol = "pol".equalsIgnoreCase(PBLOCK.GENENAME);
		isPro = "pro".equalsIgnoreCase(PBLOCK.GENENAME);

    nonAlignedScores = new float[BOUNDS.width];
    StopCodonModifier stopMod = PBLOCK.TARGETDNA.getStopCodonModifier();
    ORFHexamerModifier orfMod = PBLOCK.TARGETDNA.getORFHexamerModifier();
    NonORFHexamerModifier nonOrfMod = PBLOCK.TARGETDNA.getNonORFHexamerModifier();
    GlycSiteModifier glycMod = PBLOCK.TARGETDNA.getGlycSiteModifier();

// build nonAlignedScores
    for (int m=0; m<BOUNDS.width; m++) {
      nonAlignedScores[m] += (PBLOCK.NONALIGNEDSCORE +
          stopMod.stopCodonModifierAt(BOUNDS.x + m, BOUNDS.x, BOUNDS.x + BOUNDS.width) * PBLOCK.STOPCODONFACTOR +
          orfMod.modification(BOUNDS.x + m) * PBLOCK.ORFHEXAMERFACTOR +
          nonOrfMod.modification(BOUNDS.x + m) * PBLOCK.NONORFHEXAMERFACTOR);
      if (isEnv) {
        nonAlignedScores[m] += glycMod.modification(BOUNDS.x + m) * PBLOCK.GLYCSITEFACTOR;
      }
// bound non-aligned score
    }
		
    
		fillscoresums(BOUNDS.width);
		THEPATH = new ExonPath(0);
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock)
	

// builds dynamic matrix of width wi
	private final void fillscoresums(int wi) throws RetroTectorException {
		try {
			scoresums = new float[wi];
			links = new byte[wi];
		} catch (OutOfMemoryError me) {
			throw new RetroTectorException(Utilities.className(this), "Out of memory creating scoresums or links");
		}
    
		float temps;
		float temps2;
		int templ;

// build scoresums
		scoresums[wi - 1] = 0;
		links[wi - 1] = (byte) 0;
		for (int d=wi-2; d>=0; d--) {
// first try 1 base step
			temps = scoresums[d + 1] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
			templ = 1;
			if (d < (wi-2)) { // then try 2 base step
				temps2 = scoresums[d + 2] + nonAlignedScores[d] + PBLOCK.FRAMESHIFTPENALTY;
				if (temps2 > temps) {
					temps = temps2;
					templ = 2;
				}
			}
			if (d < (wi-3)) { // then try codon step
				temps2 = scoresums[d + 3] + nonAlignedScores[d];
				if (temps2 > temps) {
					temps = temps2;
					templ = 3;
				}
			}
			scoresums[d] = temps;
			links[d] = (byte) templ;
		}
	} // end of fillscoresums(int)
	
/**
* @return		The Path cpnstructed by this.
*/
	public final PathClass getPath() {
		return THEPATH;
	} // end of getPath()
	
} // end of CoreExonDynamicMatrix
