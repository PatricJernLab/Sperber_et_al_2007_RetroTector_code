/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 2/11 -06
* Beautified 2/11 -06
*/
package plugins;

import retrotector.*;
import builtins.*;

import java.io.*;
import java.util.*;


/**
* Executor to align two base sequences on the assumption that they contain an LTR pair.
*<PRE>
*     Parameters:
*
*   DNAFile
* The name of the DNA file in the current directory to search in.
* Default: name of current directory + '.txt'.
*
*   Start1
* The first (external) position in DNA of the first sequence.
* Default: -1 
*
*   End1
* The last (external) position in DNA of the first sequence.
* Default: -1 
*
*   Start2
* The first (external) position in DNA of the second sequence.
* Default: -1 
*
*   End2
* The last (external) position in DNA of the second sequence.
* Default: -1 
*
*   MerLength
* The length of the initial search sequence.
* Default: 11 
*</PRE>
*/
public class LTRAlign extends Executor {

// Indices in bestresult
	static final int FIRSTINDEX = 0;
	static final int HOTSPOTINDEX = 1;
	static final int LASTINDEX = 2;
	static final int REPLENGTHINDEX = 3;
	static final int REPSCOREINDEX = 4;
	static final int IDENTITYINDEX = 5;
	static final int FACTORINDEX = 6;
	static final int TOTALSCOREINDEX = 7;

/**
* Standard Executor constructor.
*/
	public LTRAlign() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put("Start1", "-1");
		explanations.put("Start1", "The first (external) position in DNA of the first sequence");
		orderedkeys.push("Start1");
		parameters.put("End1", "-1");
		explanations.put("End1", "The last (external) position in DNA of the first sequence");
		orderedkeys.push("End1");
		parameters.put("Start2", "-1");
		explanations.put("Start2", "The first (external) position in DNA of the second sequence");
		orderedkeys.push("Start2");
		parameters.put("End2", "-1");
		explanations.put("End2", "The last (external) position in DNA of the second sequence");
		orderedkeys.push("End2");
		parameters.put("MerLength", "11");
		explanations.put("MerLength", "The length of the initial search sequence");
		orderedkeys.push("MerLength");
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 02";
  } //end of version
	
	private int bestFirst1 = -1;
	private int bestFirst2 = -1;
	private int bestLength = -1;
	
	private int start1;
	private int end1;
	private int start2;
	private int end2;
	private int merLength;
	
	private DNA dna;
	
	private int[ ] basecodes;
	private long[ ] mers;
	
	private String[ ] aline = new String[3];
	private int bef;
	
	LTRID ltrid;
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {

		start1 = getInt("Start1", -1);
		end1 = getInt("End1", -1);
		start2 = getInt("Start2", -1);
		end2 = getInt("End2", -1);
		merLength = getInt("MerLength", 11);
		dna = getDNA(getString(DNAFILEKEY, ""), end1 > start1);
		start1 = dna.internalize(start1);
		end1 = dna.internalize(end1);
		start2 = dna.internalize(start2);
		end2 = dna.internalize(end2);
		basecodes = dna.getBaseCodes();
		mers = Utilities.merize(basecodes, 0, basecodes.length - 1, merLength, true);
		
		int first1;
		int first2;
		int len;
// search for identity of merLength or more
		for (int offset=start2-end1+merLength; offset<=end2-merLength-start1; offset++) {
			first1 = start1;
			first2 = first1 + offset;
			len = -1;
			while ((first1 <= end1 - merLength) & (first2 <= end2 - merLength)) {
				if (first2 >= start2) {
					if ((len < 0) && (mers[first1] == mers[first2])) {
						len = merLength; // bew onr found
					} else if ((len >= 0) && (mers[first1] == mers[first2])) {
						len++; // extend
					} else if ((len >= 0) && (mers[first1] != mers[first2])) {
						if (len > bestLength) { // end of identity
							bestLength = len;
							bestFirst1 = first1 - len + merLength - 1;
							bestFirst2 = first2 - len + merLength - 1;
						}
						len = -1;
					}
				}
				first1++;
				first2++;
			}
		}
		
		if ((bestFirst1 < 0) | (bestFirst2 < 0)) {
			System.out.println("No identity of length " + merLength + " was found");
			return "";
		}
		
		System.out.println(dna.externalize(bestFirst1));
		System.out.println(dna.externalize(bestFirst2));
		System.out.println(bestLength);
		System.out.println(dna.subString(bestFirst1, bestFirst1 + bestLength - 1, true));
		System.out.println();
		System.out.println(dna.subString(bestFirst2, bestFirst2 + bestLength - 1, true));
		bef = Math.min(bestFirst1 - start1, bestFirst2 - start2);
		int aft = Math.min(end1 - bestFirst1, end2 - bestFirst2);
// make s1 and s2 equal length before and after identity
		String s1 = " " + dna.subString(bestFirst1 - bef, bestFirst1 + aft, true);
		System.out.println();
		System.out.println();
		System.out.println(s1);
		String s2 = " " + dna.subString(bestFirst2 - bef, bestFirst2 + aft, true);
		System.out.println();
		System.out.println(s2);
		
		
// align them
		Utilities.alignBases(s1.toCharArray(), s2.toCharArray(), 2 * bef + bestLength, 20, 60, 100, 0);
		System.out.println();
		System.out.println(Utilities.aline1);
		System.out.println();
		System.out.println(Utilities.aline2);
		aline[1] = Utilities.aline1;
		aline[2] = Utilities.aline2;
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		StringBuffer sb3 = new StringBuffer();
		int ad1 = bestFirst1 - bef;
		int ad2 = bestFirst2 - bef;
		char c1;
		char c2;
		
		ltrid = new LTRID();
		ltrid.makePreparations();
		ltrid.setDNA(dna, bestFirst1 - bef, bestFirst2 + aft, "");
		
		int[ ] bestresult = null;
		int[ ] br;
// search for tg-ca in at least one sequence
		for (int fi=4; fi<bef+bestLength/2; fi++) { // look for tg in first half
			if (aline[1].substring(fi, fi + 2).equals("tg") | aline[2].substring(fi, fi + 2).equals("tg")) {
				for (int la=aline[2].length()-5; la>bef+bestLength/2; la--) { // look for ca in second half
					if (aline[2].substring(la - 1, la + 1).equals("ca") | aline[2].substring(la - 1, la + 1).equals("ca")) {
// pair found
						br = fits(fi, la); // try to make candidates
						if ((br != null) && ((bestresult == null) || (br[TOTALSCOREINDEX] > bestresult[TOTALSCOREINDEX]))) {
							bestresult = br;
							integLeast = br[TOTALSCOREINDEX] / 1000.0f;
							identLeast = br[TOTALSCOREINDEX];
							reallybestcand1 = bestcand1;
							reallybestcand2 = bestcand2;
						}
					}
				}
			}
		}
		System.out.println(bestresult[0] + " " + bestresult[1] + " " + bestresult[2] + " " + bestresult[3]);  
		
		for (int p=0; p<aline[1].length(); p++) {
			if ((p % 60 == 0) | (p == aline[1].length() - 1)) {
				System.out.println(sb1.toString());
				System.out.println(sb2.toString());
				System.out.println(sb3.toString());
				System.out.println();
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
			} else if (c1 == c2) {
				sb3.append("*");
			} else {
				sb3.append(" ");
			}
		}
		
		
		System.out.println();
		System.out.println("5'LTR at " + dna.externalize(posOf(bestresult[HOTSPOTINDEX], 1)) + " [" + dna.externalize(posOf(bestresult[FIRSTINDEX], 1)) + "-" + dna.externalize(posOf(bestresult[LASTINDEX], 1)) + "]");
		System.out.println("3'LTR at " + dna.externalize(posOf(bestresult[HOTSPOTINDEX], 2)) + " [" + dna.externalize(posOf(bestresult[FIRSTINDEX], 2)) + "-" + dna.externalize(posOf(bestresult[LASTINDEX], 2)) + "]");
		
		String[ ] ss;
		System.out.println();
		if (reallybestcand1 != null) {
			ss = reallybestcand1.toStrings();
			for (int is=0; is<ss.length; is++) {
				System.out.println(ss[is]);
			}
			System.out.println();
		}
		if (reallybestcand2 != null) {
			ss = reallybestcand2.toStrings();
			for (int is=0; is<ss.length; is++) {
				System.out.println(ss[is]);
			}
		}
		
		return "";

	} // end of execute()
	
	private float integLeast = 0;
	private int identLeast = 0;
	LTRCandidate bestcand1 = null;
	LTRCandidate bestcand2 = null;
	LTRCandidate reallybestcand1 = null;
	LTRCandidate reallybestcand2 = null;
		
// evaluates one try
	private int[ ] fits(int first, int last) throws RetroTectorException {
		int[ ] result = new int[8];
		result[FIRSTINDEX] = first;
		result[LASTINDEX] = last;
		result[HOTSPOTINDEX] = -1;

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
		if (result[REPSCOREINDEX] < integLeast) {
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
		result[IDENTITYINDEX] = Math.round(1000.0f * right / (right + wrong));
		if (result[IDENTITYINDEX] * result[REPSCOREINDEX] < identLeast) {
			return null;
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
		
		for (int tryp=first + 50; tryp<=last-50; tryp++) {
			if ((aline[1].charAt(tryp) != '-') & (aline[2].charAt(tryp) != '-')) {
				cand1 = ltrid.makeCandidate(posOf(tryp, 1), pos1start, pos1end);
				cand2 = ltrid.makeCandidate(posOf(tryp, 2), pos2start, pos2end);
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

		return result;
	} // end of fits(int, int)
	
/*
* Returns the internal position in dna corresponding to the position in aline[aindex]
* corresponding to index.
*/
	private int posOf(int index, int aindex) {
		int ad;
		if (aindex == 1) {
			ad = bestFirst1 - bef;
		} else {
			ad = bestFirst2 - bef;
		}
		for (int i=0; i<index; i++) {
			if (aline[aindex].charAt(i) != '-') {
				ad++;
			}
		}
		return ad;
	} // end of posOf(int, int)
	
} // end of LTRAlign
