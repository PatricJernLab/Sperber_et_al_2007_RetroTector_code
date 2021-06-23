/*
* Copyright (©) 2000-2004, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 27/9 -04
* Beautified 27/9 -04
*/
package plugins;

import retrotector.*;

import java.io.*;
import java.util.*;


/**
* Executor which collects LTRs within the current directory tree.
*<PRE>
*     Parameters:
*
*   SearchIn
* Files with this name are collected from.
* Default: RetroVID_001Script.txt.
*
*   ChainsIn
* Files with this name are searched for Chains.
* Default: 001SelectedChains.txt.
*
*   OutputFile
* Text file for result.
* Default: CollectLTRsOutput.txt.
*
*</PRE>
*/
public class CollectLTRs extends Executor implements Utilities.FileTreater {

	static char tab = (char) 9;
	
	static String header;
	
	static {
		header = "Hotspot" + tab +
				"Type" + tab +
				"First" + tab +
				"Last" + tab +
				"Factor" + tab +
				"CFactor" + tab +
				"U5NNscore" + tab +
				"U5NNpos" + tab +
				"GTatU5NN" + tab +
				"U3NNscore" + tab +
				"U3NNpos" + tab +
				"TATAAscr" + tab +
				"TATAApos" + tab +
				"MEME50scr" + tab +
				"MEME50pos" + tab +
				"Motif1scr" + tab +
				"Motif1pos" + tab +
				"Motif2scr" + tab +
				"Motif2pos" + tab +
				"Transscr" + tab +
				"CpGscr" + tab +
				"Spl8scr" + tab +
				"Chainscr" + tab +
				"PBS,PPT" + tab +
				"Removed";
	} // end of static initializer

/**
* Represents one LTR.
*/
	class LTRLine {
	
/**
* Its hotspot position.
*/
		int position;

/**
* Desctiptive String.
*/
		String contents;

/**
* Score factor of pair companion.
*/
		String companionScore = "";
		
		LTRLine(LTRCandidate cand, LTRCandidate ca2) throws RetroTectorException {
			position = cand.LTRCANDIDATEDNA.externalize(cand.hotSpotPosition);
			if (ca2 != null) {
				companionScore = "" + ca2.candidateFactor;
			}
			contents = "" + position + tab + 
					cand.hotSpotType + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.candidateFirst) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.candidateLast) + tab +
					cand.candidateFactor + tab +
					companionScore + tab +
					(cand.u5Score / cand.u5Max) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.u5Position) + tab +
					(cand.gtScore / cand.gtMax) + tab +
					(cand.u3Score / cand.u3Max) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.u3Position) + tab +
					(cand.tataaScore / cand.tataaMax) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.tataaPosition) + tab +
					(cand.mEME50Score / cand.mEME50Max) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.mEME50Position) + tab +
					(cand.mot1Score / cand.mot1Max) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.mot1Position) + tab +
					(cand.mot2Score / cand.mot2Max) + tab +
					cand.LTRCANDIDATEDNA.externalize(cand.mot2Position) + tab +
					(cand.transScore / cand.transMax)  + tab +
					(cand.cpgScore / cand.cpgMax) + tab +
					(cand.spl8Score / cand.spl8Max) + tab +
					cand.chainScore + tab +
					cand.candidateLimiters + tab +
					cand.LTRCANDIDATEDNA.insertsBetween(cand.candidateFirst, cand.candidateLast);
		}
	} // end of LTRLine

/**
* Key for name of file to be searched for LTRs = "SearchIn".
*/
	public static final String SEARCHINKEY = "SearchIn";

/**
* Key for name of associated RetroVID output file = "ChainsIn".
*/
	public static final String CHAINSINKEY = "ChainsIn";

	private String searchIn;
	private String chainsIn;
	private ParameterFileWriter writer;
	private LinkedList primList = new LinkedList();
	private LinkedList secList = new LinkedList();
	private LTRCandidate[ ] chainedCands = null; // all LTRs found in Chains

		
/**
* Standard Executor constructor.
*/
	public CollectLTRs() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(SEARCHINKEY, "RetroVID_001Script.txt");
		explanations.put(SEARCHINKEY, "Files with this name are collected from");
		orderedkeys.push(SEARCHINKEY);
		parameters.put(CHAINSINKEY, "001SelectedChains.txt");
		explanations.put(CHAINSINKEY, "Associated RetroVID output file");
		orderedkeys.push(CHAINSINKEY);
		parameters.put(OUTPUTFILEKEY, "CollectLTRsOutput.txt");
		explanations.put(OUTPUTFILEKEY, "Text file for result");
		orderedkeys.push(OUTPUTFILEKEY);
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2004 09 27";
  } //end of version
	
/*
* Puts l in s ordered by position, unless an identical one is already there.
*/
	private void putInList(LinkedList s, LTRLine l) {
		LTRLine li;
		int ind = 0;
		while ((ind < s.size()) && (((LTRLine) s.get(ind)).position < l.position)) {
			ind++;
		}
		while ((ind < s.size()) && ((li = (LTRLine) s.get(ind)).position == l.position)) {
			if (li.contents.equals(l.contents)) {
				return;
			}
			ind++;
		}
		s.add(ind, l);
		return;
	} // end of putInList
	
/*
* If can occurs in Chain, set its chainScore.
*/
	private void checkCandidate(LTRCandidate can) throws RetroTectorException {
		if (chainedCands != null) {
			for (int i=0; i<chainedCands.length; i++) {
				if (chainedCands[i].similar(can) && (Utilities.decodeFloat(chainedCands[i].chainScore) > Utilities.decodeFloat(can.chainScore))) {
					can.chainScore = chainedCands[i].chainScore;
				}
			}
		}
	} // end of checkCandidate
	  
/**
* As required by FileTreater.
*/
	public File treatFile(File f) throws RetroTectorException {
		if (!f.getName().equals(searchIn)) {
			return f;
		}
		showProgress();
		parameters = new Hashtable();
		ParameterFileReader reader = new ParameterFileReader(f, parameters);
		reader.readParameters();
		reader.close();
		
		DNA primDNA = new DNA(Utilities.getReadFile(f.getParentFile(), getString(DNAFILEKEY, "")), true);
		DNA secDNA = new DNA(Utilities.getReadFile(f.getParentFile(), getString(DNAFILEKEY, "")), false);

		File cFile = null;
		Hashtable cH = null;
		int maxn = -1;
		LTRCandidate ca;
		if (chainsIn.length() > 0) {
			cFile = Utilities.getReadFile(f.getParentFile(), chainsIn);
			if (cFile.exists()) {
				cH = new Hashtable();
				ParameterFileReader creader = new ParameterFileReader(cFile, cH);
				creader.readParameters();
				creader.close();
				maxn = creader.getMaxchainnumber();
				Stack cs = new Stack();
				String s;
				String[ ] ss;
				Object o;
				ChainGraphInfo cgi;
				for (int cnr=1; cnr<=maxn; cnr++) {
					s = "ChainP" + cnr;
					if ((o = cH.get(s)) != null) {
						ss = (String[ ]) o;
						cgi = new ChainGraphInfo(ss, primDNA);
						s = "P" + cnr + "_5LTR";
						if ((o = cH.get(s)) != null) {
							ss = (String[ ]) o;
							ca = new LTRCandidate(primDNA, ss);
							ca.chainScore = "" + cgi.SCORE;
							cs.push(ca);
						}
						s = "P" + cnr + "_3LTR";
						if ((o = cH.get(s)) != null) {
							ss = (String[ ]) o;
							ca = new LTRCandidate(primDNA, ss);
							ca.chainScore = "" + cgi.SCORE;
							cs.push(ca);
						}
					}
 					s = "ChainS" + cnr;
					if ((o = cH.get(s)) != null) {
						ss = (String[ ]) o;
						cgi = new ChainGraphInfo(ss, secDNA);
						s = "S" + cnr + "_5LTR";
						if ((o = cH.get(s)) != null) {
							ss = (String[ ]) o;
							ca = new LTRCandidate(secDNA, ss);
							ca.chainScore = "" + cgi.SCORE;
							cs.push(ca);
						}
						s = "S" + cnr + "_3LTR";
						if ((o = cH.get(s)) != null) {
							ss = (String[ ]) o;
							ca = new LTRCandidate(secDNA, ss);
							ca.chainScore = "" + cgi.SCORE;
							cs.push(ca);
						}
					}
				}
				chainedCands = new LTRCandidate[cs.size()];
				cs.copyInto(chainedCands);
			}
		}
				
				
				
		String[ ] ss;
		LTRCandidate ca1;
		LTRCandidate ca2;
		for (int ppi=1; (ss = getStringArray("P" + ppi + "_LTRpair")) != null; ppi++) {
			ss = getStringArray("P" + ppi + "_5LTR");
			ca1 = new LTRCandidate(primDNA, ss);
			checkCandidate(ca1);
			ss = getStringArray("P" + ppi + "_3LTR");
			ca2 = new LTRCandidate(primDNA, ss);
			checkCandidate(ca2);
			putInList(primList, new LTRLine(ca1, ca2));
			putInList(primList, new LTRLine(ca2, ca1));
		}
		for (int psi=1; (ss = getStringArray("S" + psi + "_LTRpair")) != null; psi++) {
			ss = getStringArray("S" + psi + "_5LTR");
			ca1 = new LTRCandidate(secDNA, ss);
			checkCandidate(ca1);
			ss = getStringArray("S" + psi + "_3LTR");
			ca2 = new LTRCandidate(secDNA, ss);
			checkCandidate(ca2);
			putInList(secList, new LTRLine(ca1, ca2));
			putInList(secList, new LTRLine(ca2, ca1));
		}
		for (int pi=1; (ss = getStringArray("SingleLTR_P" + pi)) != null; pi++) {
			ca1 = new LTRCandidate(primDNA, ss);
			checkCandidate(ca1);
			putInList(primList, new LTRLine(ca1, null));
		}
		for (int si=1; (ss = getStringArray("SingleLTR_S" + si)) != null; si++) {
			ca1 = new LTRCandidate(secDNA, ss);
			checkCandidate(ca1);
			putInList(secList, new LTRLine(ca1, null));
		}
		return f;
	} // end of treatFile
				
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		searchIn = getString(SEARCHINKEY, "");
		chainsIn = getString(CHAINSINKEY, "");
		writer = new ParameterFileWriter(new File(RetroTectorEngine.currentDirectory(), getString(OUTPUTFILEKEY, "")));
		writeInfo(writer);
		Utilities.treatFilesIn(RetroTectorEngine.currentDirectory(), this);
		LTRLine[ ] primLTRs = new LTRLine[primList.size()];
		primList.toArray(primLTRs);
		primList = null;
		LTRLine[ ] secLTRs = new LTRLine[secList.size()];
		secList.toArray(secLTRs);
		secList = null;
		writer.writeComment(header);
		writer.startMultiParameter("PrimLTRs", false);
		for (int ip=0; ip<primLTRs.length; ip++) {
			writer.appendToMultiParameter(primLTRs[ip].contents, false);
		}
		writer.finishMultiParameter(false);
		writer.startMultiParameter("SecLTRs", false);
		for (int is=secLTRs.length-1; is>=0; is--) {
			writer.appendToMultiParameter(secLTRs[is].contents, false);
		}
		writer.finishMultiParameter(false);
		writer.close();
		return "";
	} // end of execute

} // end of CollectLTRs
