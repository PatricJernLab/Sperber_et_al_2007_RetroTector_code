/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 1/10 -06
* Beautified 1/10 -06
*/
package builtins;


import retrotector.*;
import java.util.*;
import java.io.*;

/**
* ChainProcessor which tries to add a PBS.
*<PRE>
*     Parameters:
*
*   ProcessorPriority
* Priority relative to other ChainProcessors.
* Default: -2.
*
*   ScoreThreshold
* The lowest raw score % acceptable in a PBS Motif hit.
* Default: .33.
*
*   Debugging
* If this is Yes, debugging info is output to standard output.
* Default: No
*</PRE>
*/
public class PBSImprover extends AbstractChainProcessor {
	
	public static final String SCORETHRESHOLDKEY = "ScoreThreshold";

	private float scoreThreshold;
	private boolean debugging = true;

/**
* Constructor.
*/
	public PBSImprover() {
		parameters = new Hashtable();
		getDefaults();
		processorPriority = getFloat(PROCESSORPRIORITYKEY, -2.0f);
		debugging = getString(Executor.DEBUGGINGKEY, Executor.NO).equals(Executor.YES);
		scoreThreshold = getFloat(SCORETHRESHOLDKEY, 0.33f);
	} // end of constructor()
	
/**
* Performs final operations on all in processedChains.
* Dummy.
*/
	public final void postProcess() throws RetroTectorException {
	} // end of postProcess()
	
/**
* As required by Scorable.
*	@return	processorPriority.
*/
	public final float fetchScore() {
		return processorPriority;
	} // end of fetchScore()
	
/**
*	@param	ch				A Chain.
* @param	retroVID	Caller.
*	@return	True if ch contains LTR5 hit but not PBS hit.
*/
	public final boolean isEligible(Chain ch, RetroVID retroVID) {
		return (ch.get5LTR() != null) & (ch.getPBShit() == null);
	} // end of isEligible(Chain, RetroVID)

	private DNA dna;
	
/**
* Remakes inChain searching for PBS. If one is found with raw score above raw score threshold, it is
* given half the final score it would otherwise have. If it has raw score below raw score threshold but
* above scoreThreshold, it is given final score 0.
* @param	inChain						Chain to process.
* @param	info							ProcessorInfo with parameters.
* @param	retroVID					Caller.
* @return	The (possibly) processed Chain, which is also pushed onto processedChains.
*/
	public Chain processChain(Chain inChain,  ProcessorInfo info, RetroVID retroVID) throws RetroTectorException {

		ChainCollector collector = inChain.COLLECTOR;
		Motif[ ] mmm = collector.getSubGene(1).getAllMotifs();
		BaseMotif[ ] pbsMotifs = new BaseMotif[mmm.length];
		float[ ] max = new float[mmm.length];
		for (int mi=0; mi<mmm.length; mi++) {
			pbsMotifs[mi] = (BaseMotif) mmm[mi];
			max[mi] = pbsMotifs[mi].getBestRawScore();
		}

		dna = inChain.SOURCEDNA;
		int ltrend = inChain.get5LTR().firstMotifHit().MOTIFHITLAST;
		float bestscore = -Float.MAX_VALUE;
		float newscore;
		int bestpos = Integer.MIN_VALUE + 1;
		BaseMotif bestMotif = null;
		for (int i=Math.max(0, ltrend - 40); i<Math.min(dna.LENGTH - 22, ltrend + 40); i++) {
			if (debugging) {
				System.out.println(dna.externalize(i));
			}
			if ((dna.getBase(i) == 't') & (dna.getBase(i + 1) == 'g') & (dna.getBase(i + 2) == 'g')) {
				for (int m=0; m<pbsMotifs.length; m++) {
					newscore = dna.baseRawScoreAt(pbsMotifs[m].MOTIFSTRAND, i, 2.0f) / max[m];
					if ((newscore >= bestscore) && (newscore >= scoreThreshold)) {
						bestscore = newscore;
						bestpos = i;
						bestMotif = pbsMotifs[m];
					}
				}
			}
		}
		if (bestscore < scoreThreshold) {
			return inChain;
		} else {
			float sc = 0.0f;
			bestscore = bestscore * bestMotif.getBestRawScore();
			if (bestscore > bestMotif.getRawThreshold()) {
				sc = bestMotif.MAXSCORE * 0.5f * (bestscore - bestMotif.getRawThreshold()) / (bestMotif.getBestRawScore() - bestMotif.getRawThreshold());
			}
			MotifHit pbsHit = new MotifHit(bestMotif, sc, bestpos, bestpos, bestpos + bestMotif.getBasesLength() - 1, dna);
			Chain theChain = Chain.setPBS(pbsHit, inChain, info.BROKENPENALTY, info.LENGTHBONUS);
			if (theChain == null) {
				return inChain;
			}
			theChain.chainNumber = inChain.chainNumber;
			theChain.select(inChain.isSelected());
			theChain.appendToHistory("Processed by PBSImprover " + (new Date()).toString());
			processedChains.push(theChain);
			return theChain;
		}
	} // end of processChain(Chain,  ProcessorInfo, RetroVID)
	
}
