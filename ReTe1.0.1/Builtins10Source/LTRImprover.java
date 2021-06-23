/*
* Copyright (©) 2000-2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0.2
*
* @author  Göran Sperber
* @version 17/6 -09
* Beautified17/6 -09
*/
package builtins;

import retrotector.*;
import retrotectorcore.*;
import java.util.*;
import java.io.*;

/**
* ChainProcessor which tries to improve LTRs.
*<PRE>
*     Parameters:
*
*   ProcessorPriority
* Priority relative to other ChainProcessors.
* Default: -1.
*
*   Threshold
* The lowest acceptable value for the internal evaluation of an LTR pair.
* Range is 0>14000. Practical upper limit ca 6000.
* Default: 0.
*
*   ChainScoreThreshold
* Chains with lower score than this are not treated.
* Default: 0.
*
*   LastLeadSubGene
* If there is no SubGene hit with score at least 20 before or at this SubGene, the Chain is not treated.
* Default: NC.
*
*   FirstTrailSubGene
* If there is no SubGene hit with score at least 20 after or at this SubGene, the Chain is not treated.
* Default: TM.
*
*   MaxOutputChains
* Dummy, not used.
* Default: 0.
*
*   MerLength
* The shortest identity accepted in preliminary search.
* Default: 11
*
*   CheckTime
* The time in seconds at which total execution time is estimated.
* Default: 300
*
*   Debugging
* If this is Yes, alignment is output to standard output.
* Default: No
*</PRE>
*/
public class LTRImprover extends AbstractChainProcessor {

	
/**
* Key for the lowest acceptable value for the internal evaluation of an LTR pair = "Threshold".
*/
	public static final String THRESHOLDKEY = "Threshold";

/**
* Chains with lower score than indicated under this key are not treated.
*/
	public static final String CHAINSCORETHRESHOLDKEY = "ChainScoreThreshold";

/**
* If there is no SubGene hit with score at least 20 before or at SubGene indicated under this key, the Chain is not treated.
*/
	public static final String LASTLEADSUBGENEKEY = "LastLeadSubGene";
	
/**
* If there is no SubGene hit with score at least 20 after or at SubGene indicated under this key, the Chain is not treated.
*/
	public static final String FIRSTTRAILSUBGENEKEY = "FirstTrailSubGene";

/**
* Key for the shortest identity accepted in preliminary search.
*/
	public static final String MERLENGTHKEY = "MerLength";

/**
* Key for the time in seconds at which total execution time is estimated.
*/
	public static final String CHECKTIMEKEY = "CheckTime";
	
	
/**
* Index of start position in information array.
*/
	static final int FIRSTINDEX = 0;
	
/**
* Index of hotspot position in information array.
*/
	static final int HOTSPOTINDEX = 1;
	
/**
* Index of end position in information array.
*/
	static final int LASTINDEX = 2;
	
/**
* Index of direct repeat length in information array.
*/
	static final int REPLENGTHINDEX = 3;
	
/**
* Index of integration repeat score in information array.
*/
	static final int REPSCOREINDEX = 4;
	
/**
* Index of similarity score in information array.
*/
	static final int IDENTITYINDEX = 5;
	
/**
* Index of LTR candidate score * 1000 in information array.
*/
	static final int FACTORINDEX = 6;
	
/**
* Index of final score in information array.
*/
	static final int TOTALSCOREINDEX = 7;

/**
* Constructor.
*/
	public LTRImprover() {
		parameters = new Hashtable();
		getDefaults();
		processorPriority = getFloat(PROCESSORPRIORITYKEY, -1.0f);
		chainScoreThreshold = getFloat(CHAINSCORETHRESHOLDKEY, 0.0f);
		lastLeadName = getString(LASTLEADSUBGENEKEY, "NC");
		firstTrailName = getString(FIRSTTRAILSUBGENEKEY, "TM");
		threshold = getInt(THRESHOLDKEY, 0);
		nrToOutput = getInt(MAXOUTPUTKEY, 0);
		merLength = getInt(MERLENGTHKEY, 11);
		checkTime = getInt(CHECKTIMEKEY, 300);
		debugging = getString(Executor.DEBUGGINGKEY, Executor.NO).equals(Executor.YES);
	} // end of constructor()
	
/**
* Performs final operations on all in processedChains.
* Dummy.
*/
	public void postProcess() throws RetroTectorException {
	} // end of postProcess
	
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
*	@return	True if ch is selected and its score >= chainScoreThreshold, thus should be processed.
*/
	public boolean isEligible(Chain ch, RetroVID retroVID) {
		return ch.isSelected() & ch.CHAINSCORE >= chainScoreThreshold;
	} // end of isEligible

	private float chainScoreThreshold = 0; // Chains with lower score than this are not treated
	private String lastLeadName = "";
	private int lastLeadIndex = -1;
	private String firstTrailName = "";
	private int firstTrailIndex = -1;
	
	private int start1; // first position to seek 5'LTR
	private int hotfirst1;
	private int hotlast1;
	private int end1; // last position to seek 5'LTR
	private int start2; // first position to seek 3'LTR
	private int hotfirst2;
	private int hotlast2;
	private int end2; // last position to seek 3'LTR
	private int merLength; // shortest acceptable identity
	private int checkTime; // time in s to check for long time
	
	private int newstart1; // revised first position to seek 5'LTR
	private int newend1; // revised last position to seek 5'LTR
	private int newstart2; // revised first position to seek 3'LTR
	private int newend2; // revised last position to seek 3'LTR

	private int bestMerPos1 = -1; // first position in 5'LTR region of longest identity
	private int bestMerPos2 = -1; // first position in 3'LTR region of longest identity
	private int bestLength = -1; // length of longest identity

	private DNA dna;
	private int[ ] basecodes; 
	private long[ ] mers;

	private String[ ] aline = new String[3]; // for aligned strings
	private int alineLength;

	LTRID ltrid; // to make LTRCandidates
	
	private int threshold;
	
	private boolean debugging = true;

	private int bestTotalScore = 0; // threshold for integration repeats * LTR identity scores
	LTRCandidate bestcand1 = null; // best 5'LTR candidate so far
	LTRCandidate bestcand2 = null; // best 3'LTR candidate so far
	LTRCandidate reallybestcand1 = null; // best 5'LTR candidate
	LTRCandidate reallybestcand2 = null; // best 3'LTR candidate
	DistanceRange hotspotToStart;
	DistanceRange hotspotToEnd;
	
/**
* Remakes inChain, adding or improving LTRs.
* @param	inChain						Chain to process.
* @param	info							ProcessorInfo with parameters.
* @param	retroVID					Caller.
* @return	The (possibly) processed Chain, which is also pushed onto processedChains.
*/
	public final Chain processChain(Chain inChain,  ProcessorInfo info, RetroVID retroVID) throws RetroTectorException {
	
		RetroTectorEngine.setInfoField("LTRImprover working");
		ChainCollector collector = inChain.COLLECTOR;
		getDefaults();
		dna = inChain.SOURCEDNA;
		basecodes = dna.getBaseCodes();
		mers = Utilities.merize(basecodes, 0, basecodes.length - 1, merLength, true);
		bestMerPos1 = -1;
		bestMerPos2 = -1;
		bestLength = -1;
		bestTotalScore = 0;
		bestcand1 = null; // best 5'LTR candidate so far
		bestcand2 = null; // best 3'LTR candidate so far
		reallybestcand1 = null; // best 5'LTR candidate
		reallybestcand2 = null; // best 3'LTR candidate
		
		int gg=0;
		try {
			while (!collector.COLLECTORDATABASE.subGeneNames[gg].equals(firstTrailName)) {
				if (collector.COLLECTORDATABASE.subGeneNames[gg].equals(lastLeadName)) {
					lastLeadIndex = gg;
				}
				gg++;
			}
		} catch (NullPointerException npe) {
			throw new RetroTectorException("LTRImprover", firstTrailName + " does not seem to be a valid SubGene name");
		}
		firstTrailIndex = gg;
		if (lastLeadIndex < 0) {
			throw new RetroTectorException("LTRImprover", lastLeadName + " does not seem to be a valid SubGene name");
		}
		SubGeneHit g1 = null;
		int gi = 1;
		while ((gi <= lastLeadIndex) && (((g1 = inChain.getSubGeneHit(gi)) == null) || (g1.SCORE < 20))) {
			gi++;
		}
		if ((g1 == null) || (g1.SCORE < 20)) {
			return inChain;
		}
		SubGeneHit g2 = null;
		gi = collector.COLLECTORDATABASE.subGeneNames.length - 2;
		while ((gi >= firstTrailIndex) && (((g2 = inChain.getSubGeneHit(gi)) == null) || (g2.SCORE < 20))) {
			gi--;
		}
		if ((g2 == null) || (g2.SCORE < 20)) {
			RetroTectorEngine.setInfoField("LTRImprover finished");
			return inChain;
		}
		
		ltrid = new LTRID();
		ltrid.makePreparations();
		hotspotToStart = new DistanceRange(-LTRPair.MAXLEADING, -LTRPair.MINLEADING);
		hotspotToEnd = new DistanceRange(LTRPair.MINTRAILING, LTRPair.MAXTRAILING);

		SubGene sg = collector.COLLECTORDATABASE.getSubGene(Executor.LTR5KEY);
		Range ra = new Range(Range.UNDEFINED, g1.SGHITHOTSPOT, g1.PARENTSUBGENE.distanceTo(sg));
		hotfirst1 = ra.RANGEMIN;
		hotlast1 = ra.RANGEMAX;
		start1 = Math.max(dna.firstUnPadded(), hotfirst1 + hotspotToStart.LOWESTDISTANCE - 10);
//		end1 = Math.min(g1.firstMotifHit().MOTIFHITFIRST, hotlast1 + hotspotToEnd.HIGHESTDISTANCE + 10);
// altered 090617
		end1 = hotlast1 + hotspotToEnd.HIGHESTDISTANCE + 10;
		sg = collector.COLLECTORDATABASE.getSubGene(Executor.LTR3KEY);
		ra = new Range(Range.UNDEFINED, g2.SGHITHOTSPOT, g2.PARENTSUBGENE.distanceTo(sg));
		hotfirst2 = ra.RANGEMIN;
		hotlast2 = ra.RANGEMAX;
//
// altered 090617
		start2 = hotfirst2 + hotspotToStart.LOWESTDISTANCE - 10;
		end2 = Math.min(dna.lastUnPadded(), hotlast2 + hotspotToEnd.HIGHESTDISTANCE + 10);

// search for longest identity at least merLength long
		int first1; // mer start in 5'LTR region
		int first2; // mer start in 3'LTR region
		int len;
		RetroTectorEngine.setInfoField("LTRImprover searching for identity in Chain " + inChain.STRANDCHAR + inChain.chainNumber);
		for (int offset=start2-end1+merLength; offset<=end2-merLength-start1; offset++) {
			first1 = start1;
			first2 = first1 + offset;
			len = -1;
			while ((first1 <= end1 - merLength) & (first2 <= end2 - merLength)) {
				if (first2 >= start2) {
					if ((len < 0) && (mers[first1] == mers[first2])) {
						len = merLength;
					} else if ((len >= 0) && (mers[first1] == mers[first2])) {
						len++;
					} else if ((len >= 0) && (mers[first1] != mers[first2])) {
						if (len > bestLength) {
							bestLength = len;
							bestMerPos1 = first1 - len + merLength - 1;
							bestMerPos2 = first2 - len + merLength - 1;
						}
						len = -1;
					}
				}
				first1++;
				first2++;
			}
		}
		if ((bestMerPos1 < 0) | (bestMerPos2 < 0)) {
			System.out.println("No identity of length " + merLength + " was found in Chain " + inChain.STRANDCHAR + inChain.chainNumber);
			RetroTectorEngine.setInfoField("LTRImprover finished");
			return inChain;
		}
		
// revise search regions
		int bef = Math.min(bestMerPos1 - start1, bestMerPos2 - start2);
		int aft = Math.min(end1 - bestMerPos1, end2 - bestMerPos2);
		newstart1 = bestMerPos1 - bef;
		newend1 = bestMerPos1 + aft;
		newstart2 = bestMerPos2 - bef;
		newend2 = bestMerPos2 + aft;
		
// align
		RetroTectorEngine.showProgress();
		RetroTectorEngine.setInfoField("LTRImprover aligning in Chain " + inChain.STRANDCHAR + inChain.chainNumber);
		String s1 = " " + dna.subString(newstart1, newend1, true);
		String s2 = " " + dna.subString(newstart2, newend2, true);
		Utilities.alignBases(s1.toCharArray(), s2.toCharArray(), 2 * bef + bestLength, 20, 60, 100, 0);
		aline[1] = Utilities.aline1;
		aline[2] = Utilities.aline2;
		alineLength = aline[1].length();

		RetroTectorEngine.showProgress();
		ltrid.setDNA(dna, newstart1, newend2, "");
		
		RetroTectorEngine.showProgress();
		int trycount = 0; // to estimate possible combinations
		for (int fi=4; fi<alineLength-50; fi++) { // look for tg in first half
			if (aline[1].substring(fi, fi + 2).equals("tg") | aline[2].substring(fi, fi + 2).equals("tg")) {
				for (int la=Math.min(alineLength-5, fi-hotspotToStart.LOWESTDISTANCE+hotspotToEnd.HIGHESTDISTANCE); la>=fi-hotspotToStart.HIGHESTDISTANCE+hotspotToEnd.LOWESTDISTANCE; la--) { // look for ca in second half
					if (aline[2].substring(la - 1, la + 1).equals("ca") | aline[2].substring(la - 1, la + 1).equals("ca")) {
						trycount++;
					}
				}
			}
		}
		int[ ] bestresult = null;
		int[ ] br;
		long starttime = System.currentTimeMillis();
		int triesSoFar = 0;
		RetroTectorEngine.setInfoField("LTRImprover searching for best LTR candidates in Chain " + inChain.STRANDCHAR + inChain.chainNumber);
		for (int fi=4; fi<alineLength-50; fi++) { // look for tg in first half
			if ((triesSoFar >= 0) && (System.currentTimeMillis() - starttime > 1000 * checkTime)) {
				if (triesSoFar * 3 > trycount) {
					triesSoFar = Integer.MIN_VALUE;
				} else {
					RetroTectorEngine.displayError(new RetroTectorException("LTRImprover", "Timed out in " + inChain.STRANDCHAR + inChain.chainNumber), RetroTectorEngine.WARNINGLEVEL);
					RetroTectorEngine.setInfoField("LTRImprover finished");
					return inChain;
				}
			}
			if (aline[1].substring(fi, fi + 2).equals("tg") | aline[2].substring(fi, fi + 2).equals("tg")) {
				for (int la=Math.min(alineLength-5, fi-hotspotToStart.LOWESTDISTANCE+hotspotToEnd.HIGHESTDISTANCE); la>=fi-hotspotToStart.HIGHESTDISTANCE+hotspotToEnd.LOWESTDISTANCE; la--) { // look for ca in second half
					if (aline[2].substring(la - 1, la + 1).equals("ca") | aline[2].substring(la - 1, la + 1).equals("ca")) {
						triesSoFar++;
						br = fits(fi, la); // try to make candidates
						if ((br != null) && ((bestresult == null) || (br[TOTALSCOREINDEX] > bestresult[TOTALSCOREINDEX]))) {
							if (br[TOTALSCOREINDEX] > threshold) {
								bestresult = br;
								bestTotalScore = br[TOTALSCOREINDEX];
								reallybestcand1 = bestcand1;
								reallybestcand2 = bestcand2;
							}
						}
					}
				}
			}
		}
		if ((reallybestcand1 == null) | (reallybestcand2 == null)) {
			RetroTectorEngine.setInfoField("LTRImprover finished");
			return inChain;
		}
		if (reallybestcand1.likeLINE() | reallybestcand2.likeLINE()) {
			RetroTectorEngine.setInfoField("LTRImprover finished");
			return inChain;
		}

		RetroTectorEngine.setInfoField("LTRImprover revising Chain " + inChain.STRANDCHAR + inChain.chainNumber);
		LTRMotifHit ltr5 = new LTRMotifHit(collector.COLLECTORDATABASE.ltr5Motif, (reallybestcand1.candidateFactor + reallybestcand2.candidateFactor) * collector.COLLECTORDATABASE.ltr5Motif.MAXSCORE, reallybestcand1.hotSpotPosition, reallybestcand1.candidateFirst, reallybestcand1.candidateLast, new RVector(reallybestcand1.candidateVirusGenus), dna);
		LTRMotifHit ltr3 = new LTRMotifHit(collector.COLLECTORDATABASE.ltr3Motif, (reallybestcand1.candidateFactor + reallybestcand2.candidateFactor) * collector.COLLECTORDATABASE.ltr3Motif.MAXSCORE, reallybestcand2.hotSpotPosition, reallybestcand2.candidateFirst, reallybestcand2.candidateLast, new RVector(reallybestcand2.candidateVirusGenus), dna);
		ltr5.companion = ltr3;
		ltr3.companion = ltr5;
		StringBuffer sb = new StringBuffer();
// make integration description
		if (posOf(bestresult[FIRSTINDEX], 1) - bestresult[REPLENGTHINDEX] < dna.FIRSTUNPADDED) {
			sb.append("* ");
		}
		for (int i=0; i<bestresult[REPLENGTHINDEX]; i++) {
			try {
				sb.append(dna.getBase(posOf(bestresult[FIRSTINDEX], 1) - bestresult[REPLENGTHINDEX] + i));
			} catch (ArrayIndexOutOfBoundsException e) {
				throw e;
			}
		}
		sb.append("/");
		sb.append(dna.getBase(posOf(bestresult[FIRSTINDEX], 1)));
		sb.append(dna.getBase(posOf(bestresult[FIRSTINDEX], 1) + 1));
		sb.append("<>");
		sb.append(dna.getBase(posOf(bestresult[LASTINDEX], 2) - 1));
		sb.append(dna.getBase(posOf(bestresult[LASTINDEX], 2)));
		sb.append("/");
		for (int i=1; i<=bestresult[REPLENGTHINDEX]; i++) {
			sb.append(dna.getBase(posOf(bestresult[LASTINDEX], 2) + i));
		}
		ltr5.integSites = sb.toString();
		ltr3.integSites = ltr5.integSites;
		ltr5.hitkey = inChain.STRANDCHAR + inChain.chainNumber + "X" + CoreLTRPair.KEY5LTR;
		ltr3.hitkey = inChain.STRANDCHAR + inChain.chainNumber + "X" + CoreLTRPair.KEY3LTR;
		
		int simStart = -1;
		int si;
		int simInd = bestresult[FIRSTINDEX] - IDWINDOW;
		while ((si = firstIdentityAt(simInd)) >= 0) {
			simStart = si;
			simInd--;
		}
		if (simStart >= 0) {
			reallybestcand1.candidateSimilarityStart = posOf(simStart, 1);
			reallybestcand2.candidateSimilarityStart = posOf(simStart, 2);
		}
		int simEnd = -1;
		simInd = bestresult[LASTINDEX] + IDWINDOW;
		while ((si = lastIdentityAt(simInd)) >= 0) {
			simEnd = si;
			simInd++;
		}
		if (simEnd >= 0) {
			reallybestcand1.candidateSimilarityEnd = posOf(simEnd, 1);
			reallybestcand2.candidateSimilarityEnd = posOf(simEnd, 2);
		}
		
		Chain theChain = Chain.setLTRs(ltr5, ltr3, inChain, info.BROKENPENALTY, info.LENGTHBONUS);
		if (theChain == null) {
			RetroTectorEngine.setInfoField("LTRImprover finished");
			return inChain;
		}

		if (debugging) {
			Utilities.outputString("");
			Utilities.outputString("Alignment for Chain " + inChain.STRANDCHAR + inChain.chainNumber);
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			StringBuffer sb3 = new StringBuffer();
			int ad1 = newstart1;
			int ad2 = newstart2;
			char c1;
			char c2;
			for (int p=0; p<aline[1].length(); p++) {
				if ((p % 60 == 0) | (p == aline[1].length() - 1)) {
					Utilities.outputString(sb1.toString());
					Utilities.outputString(sb2.toString());
					Utilities.outputString(sb3.toString());
					Utilities.outputString("");
					sb1 = new StringBuffer();
					sb2 = new StringBuffer();
					sb3 = new StringBuffer();
					sb1.append(dna.externalize(ad1));
					while(sb1.length() < 18) {
						sb1.append(" ");
					}
					sb2.append(dna.externalize(ad2));
					while(sb2.length() < 18) {
						sb2.append(" ");
					}
					while(sb3.length() < 18) {
						sb3.append(" ");
					}
				}
				c1 = aline[1].charAt(p);
				c2 = aline[2].charAt(p);
				if (c1 != '-') {
					ad1++;
				}
				if (c2 != '-') {
					ad2++;
				}
				sb1.append(c1);
				sb2.append(c2);
				if ((p >= bestresult[FIRSTINDEX] - bestresult[REPLENGTHINDEX]) & (p < bestresult[FIRSTINDEX])) {
					sb3.append("#");
				} else if ((p == bestresult[FIRSTINDEX]) | (p == bestresult[FIRSTINDEX] + 1)) {
					sb3.append("!");
				} else if ((p == bestresult[LASTINDEX]) | (p == bestresult[LASTINDEX] - 1)) {
					sb3.append("!");
				} else if ((p > bestresult[LASTINDEX]) & (p <= bestresult[LASTINDEX] +  bestresult[REPLENGTHINDEX])) {
					sb3.append("#");
				} else if (p == bestresult[HOTSPOTINDEX]) {
					sb3.append("%");
				} else if (p == simStart) {
					sb3.append(")");
				} else if (p == simEnd) {
					sb3.append("(");
				} else if (c1 == c2) {
					sb3.append("*");
				} else {
					sb3.append(" ");
				}
			}
			Utilities.outputString("");
			Utilities.outputString("Score for integrations: " + bestresult[REPSCOREINDEX]);
			Utilities.outputString("Score for similarity: " + bestresult[IDENTITYINDEX]);
			Utilities.outputString("Score for LTRs: " + bestresult[FACTORINDEX]);
			Utilities.outputString("Total score: " + bestresult[TOTALSCOREINDEX]);
		}
		theChain.chainNumber = inChain.chainNumber;
		theChain.select(inChain.isSelected());
		theChain.appendToHistory("Processed by LTRImprover " + (new Date()).toString());
		Hashtable htab = info.CALLER.getParameterTable();
		htab.put(ltr5.hitkey, reallybestcand1.toStrings());
		htab.put(ltr3.hitkey, reallybestcand2.toStrings());
		processedChains.push(theChain);
		RetroTectorEngine.setInfoField("LTRImprover finished");
		return theChain;
	} // end of processChain(Chain, ProcessorInfo, RetroVID)

/*
* Searches for best candidate pair between indices first and last	in aline.
* Updates bestcand1 and bestcand2.
* Returns array with summary of result.
*/
	private final int[ ] fits(int first, int last) throws RetroTectorException {
		RetroTectorEngine.showProgress();
		int[ ] result = new int[8];
		result[FIRSTINDEX] = first;
		result[LASTINDEX] = last;

		result[REPSCOREINDEX] = -Integer.MAX_VALUE;
// count tg and ca
		int wrong = 0;
		if (aline[1].charAt(first) != 't') {
			wrong++;
		}
		if (aline[1].charAt(first + 1) != 'g') {
			wrong++;
		}
		if (aline[1].charAt(last) != 'a') {
			wrong++;
		}
		if (aline[1].charAt(last - 1) != 'c') {
			wrong++;
		}
		if (aline[2].charAt(first) != 't') {
			wrong++;
		}
		if (aline[2].charAt(first + 1) != 'g') {
			wrong++;
		}
		if (aline[2].charAt(last) != 'a') {
			wrong++;
		}
		if (aline[2].charAt(last - 1) != 'c') {
			wrong++;
		}
		
// get 6 characters before first
		StringBuffer sb = new StringBuffer();
		for (int le=first-1; (le>=0) & (sb.length()<6); le--) {
			if (aline[1].charAt(le) != '-') {
				sb.insert(0, aline[1].charAt(le));
			}
		}
		String leader = sb.toString();
		if (leader.length() != 6) {
			return null;
		}
		
// get 6 characters after last
		sb = new StringBuffer();
		for (int tr=last+1; (tr<aline[2].length()) & (sb.length()<6); tr++) {
			if (aline[2].charAt(tr) != '-') {
				sb.append(aline[2].charAt(tr));
			}
		}
		String trailer = sb.toString();
		if (trailer.length() != 6) {
			return null;
		}
		
		int w;
		int r;
// try direct repeat lengths 4-6
		for (int l=4; l<=6; l++) {
			w = 0;
			r = 0;
			for (int ll=0; ll<l; ll++) {
				if ((leader.length() - l + ll >= 0) & (ll < trailer.length())) {
					if (leader.charAt(leader.length() - l + ll) == trailer.charAt(ll)) {
						r++;
					} else {
						w++;
					}
				}
			}
			if (8 + r - 2 * (wrong + w) >= result[REPSCOREINDEX]) {
				result[REPSCOREINDEX] = 8 + r - 2 * (wrong + w);
				result[REPLENGTHINDEX] = l;
			}
		}
		if (result[REPSCOREINDEX] <= 0) {
			return null;
		}
		
// count identical bases
		wrong = 0;
		int right = 0;
		char c1;
		char c2;
		for (int wr=first; wr<=last; wr++) {
			c1 = aline[1].charAt(wr);
			c2 = aline[2].charAt(wr);
			if ((c1 != '-') & (c1 != '-')) {
				if (c1 == c2) {
					right++;
				} else {
					wrong++;
				}
			}
		}
		result[IDENTITYINDEX] = Math.max(0, Math.round(2000.0f * right / (right + wrong) - 1000.0f));
		if (result[IDENTITYINDEX] <= 0) {
			return null;
		}
		if (result[IDENTITYINDEX] * result[REPSCOREINDEX] < bestTotalScore) {
			return null; // no use trying
		}
		if (result[IDENTITYINDEX] * result[REPSCOREINDEX] < threshold) {
			return null; // no use trying
		}
		
// try different hotspots for LTR candidates
		LTRCandidate cand1;
		LTRCandidate cand2;
		bestcand1 = null;
		bestcand2 = null;
		float bestscore = -Float.MAX_VALUE;
		
		int pos1start = posOf(first, 1);
		int pos1end = posOf(last, 1);
		int pos2start = posOf(first, 2);
		int pos2end = posOf(last, 2);
		
		int hotf1 = Math.max(hotfirst1, pos1start - hotspotToStart.HIGHESTDISTANCE);
		int hotl1 = Math.min(hotlast1, pos1start - hotspotToStart.LOWESTDISTANCE);
		hotf1 = Math.max(hotf1, pos1end - hotspotToEnd.HIGHESTDISTANCE);
		hotl1 = Math.min(hotl1, pos1end - hotspotToEnd.LOWESTDISTANCE);
		int hotf2 = Math.max(hotfirst2, pos2start - hotspotToStart.HIGHESTDISTANCE);
		int hotl2 = Math.min(hotlast2, pos2start - hotspotToStart.LOWESTDISTANCE);
		hotf2 = Math.max(hotf2, pos2end - hotspotToEnd.HIGHESTDISTANCE);
		hotl2 = Math.min(hotl2, pos2end - hotspotToEnd.LOWESTDISTANCE);
		
		int pos1;
		int pos2;
		
		for (int tryp=first; tryp<=last; tryp++) {
			pos1 = posOf(tryp, 1);
			pos2 = posOf(tryp, 2);
			if ((pos1 >= hotf1) && (pos1 <= hotl1) && (pos2 >= hotf2) && (pos2 <= hotl2) && (aline[1].charAt(tryp) != '-') & (aline[2].charAt(tryp) != '-')) {
				cand1 = ltrid.makeCandidate(pos1, pos1start, pos1end);
				cand2 = ltrid.makeCandidate(pos2, pos2start, pos2end);
				if ((cand1 != null) && (cand2 != null) && (cand1.candidateFactor + cand2.candidateFactor >= bestscore)) {
					bestscore = cand1.candidateFactor + cand2.candidateFactor;
					bestcand1 = cand1;
					bestcand2 = cand2;
					result[HOTSPOTINDEX] = tryp;
				}
			}
		}
		result[FACTORINDEX] = Math.round(1000.0f * bestscore);
		result[TOTALSCOREINDEX] = Math.round(result[REPSCOREINDEX] * result[IDENTITYINDEX] * bestscore);

		if ((bestcand1 != null) && (bestcand2 != null)) {
			return result;
		}
		return null;
	} // end of fits(int, int)
	
/*
* Returns the internal position in dna corresponding to the position in aline[aindex]
* corresponding to index.
*/
	private final int posOf(int index, int aindex) {
		int ad;
		if (aindex == 1) {
			ad = newstart1;
		} else {
			ad = newstart2;
		}
		for (int i=0; i<index; i++) {
			if (aline[aindex].charAt(i) != '-') {
				ad++;
			}
		}
		return ad;
	} // end of posOf(int, int)
	
	private final static int IDWINDOW = 20;
	private final static int ERRMAX = 4;
	
	private int firstIdentityAt(int index) {
		if (index + IDWINDOW >= alineLength) {
			return -1;
		}
		if (index < 0) {
			return -1;
		}
		int errcount = 0;
		int hitindex = -1;
		for (int i=index; i<index+IDWINDOW; i++) {
			if (aline[1].charAt(i) != aline[2].charAt(i)) {
				errcount++;
				if (errcount > ERRMAX) {
					return -1;
				}
			} else {
				if (hitindex == -1) {
					hitindex = i;
				}
			}
		}
		return hitindex;
	} // end of firstIdentityAt(int)
	
	private int lastIdentityAt(int index) {
		if (index >= alineLength) {
			return -1;
		}
		if (index < IDWINDOW) {
			return -1;
		}
		int errcount = 0;
		int hitindex = -1;
		for (int i=index; i>index-IDWINDOW; i--) {
			if (aline[1].charAt(i) != aline[2].charAt(i)) {
				errcount++;
				if (errcount > ERRMAX) {
					return -1;
				}
			} else {
				if (hitindex == -1) {
					hitindex = i;
				}
			}
		}
		return hitindex;
	} // end of lastIdentityAt(int)
	
}
