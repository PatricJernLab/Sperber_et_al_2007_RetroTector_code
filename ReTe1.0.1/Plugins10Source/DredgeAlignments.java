/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 16/9 -06
* Beautified 16/9 -06
*/
package plugins;

import retrotector.*;

import java.io.*;
import java.util.*;

/**
* Executor to check consistency of alignment files and construct putative
* MotifGroups.txt, MakeSubGenesScript.txt and Genes.txt files from them.
*	It also generates Dredge.txt with detailed findings.
* The Database subdirectory where this is done must first be selected
* as working directory.
* It should be run from a script looking something like this (second column in SubGenes is hotspot MotifGroup):
*<PRE>
* Executor: DredgeAlignments
* SmallLimitFactor: 0.75
* BigLimitFactor: 1.25
* SubGenes::
* 5LTR 5LT
* PBS  PBS
* MA   MA1
* CA   CA1
* NC   NC1
* DU   DU1
* Prot PR2
* RT   RT3
* DL   DL1
* IN   IN4
* SU   SU3
* TM   TM2
* PPT  PPT
* 3LTR 3LT
* ::
* Genes::
* Gag  MA CA NC
* Pro  DU Prot
* Pol  RT DL IN
* Env  SU TM
* ::
*
*     Parameters:
*
*   SmallLimitFactor
* Lower security margin factor.
* Default: 0.75.
*
*   BigLimitFactor
* Upper security margin factor.
* Default: 1.25.
*</PRE>
*/
public class DredgeAlignments extends Executor {

/**
* Class defining an interval associated with a string.
*/
	private class IntString {
	
/**
* The String.
*/
		String STRING;

/**
* Lowest value in interval.
*/
		int MIN;

/**
* Highest value in interval.
*/
		int MAX;
		
/**
* Constructor.
* @param	s		->STRING.
* @param	min	->MIN.
* @param	max	->MAX.
*/
		IntString(String s, int min, int max) {
			STRING = s;
			MIN = min;
			MAX = max;
		} // end of IntString.constructor(String, int, int)
		
/**
* Constructor. MAX is set to Integer.MAX_VALUE.
* @param	s		->STRING.
* @param	min	->MIN.
*/
		IntString(String s, int min) {
			STRING = s;
			MIN = min;
			MAX = Integer.MAX_VALUE;
		} // end of IntString.constructor(String, int)
		
/**
* Constructor using two IntStrings with the same STRING. New instance covers both.
* @param	is1	An IntString.
* @param	is2	An IntString.
*/
		IntString(IntString is1, IntString is2) throws RetroTectorException {
			if (!is1.STRING.equals(is2.STRING)) {
				RetroTectorException.sendError(this, is1.STRING + " differs from " + is2.STRING);
			}
			STRING = is1.STRING;
			MIN = Math.min(is1.MIN, is2.MIN);
			MAX = Math.max(is1.MAX, is2.MAX);
		} // end of IntString.constructor(IntString, IntString)
		
// Like Math.round, but symmetrical between pos. and neg. arguments
		private final int round(float f) {
			if (f == 0) {
				return 0;
			} else if (f > 0) {
				return Math.round(f);
			} else {
				return -Math.round(-f);
			}
		} // end of IntString.round(float)
		
/**
* Generates blank-separated String of STRING and MIN, and also MAX and extended MIN and MAX, if fin = true.
*/
		String asString() {
			StringBuffer sb = new StringBuffer(STRING);
			while (sb.length() < 20) {
				sb.append(" ");
			}
			sb.append(MIN);
			if (fin) {
				sb.append("<");
				sb.append(MAX);
				while (sb.length() < 40) {
					sb.append(" ");
				}
				if (MAX * MIN < 0) {
					sb.append(round(MIN * bigFactor));
					sb.append("<");
					sb.append(round(MAX * bigFactor));
				} else if (MAX * MIN == 0) {
					sb.append(round(MIN * bigFactor));
					sb.append("<");
					sb.append(round(MAX * bigFactor));
				} else if (MAX >= 0) {
					sb.append(round(MIN * smallFactor));
					sb.append("<");
					sb.append(round(MAX * bigFactor));
				} else {
					sb.append(round(MIN * bigFactor));
					sb.append("<");
					sb.append(round(MAX * smallFactor));
				}
			}
			return sb.toString();
		} // end of IntString.asString()

/**
* Generates a string with extended MIN and MAX.
*/
		String asShortString() {
			StringBuffer sb = new StringBuffer();
			if (MAX * MIN < 0) {
				sb.append(round(MIN * bigFactor));
				sb.append("<");
				sb.append(round(MAX * bigFactor));
			} else if (MAX * MIN == 0) {
				sb.append(round(MIN * bigFactor));
				sb.append("<");
				sb.append(round(MAX * bigFactor));
			} else if (MAX >= 0) {
				sb.append(round(MIN * smallFactor));
				sb.append("<");
				sb.append(round(MAX * bigFactor));
			} else {
				sb.append(round(MIN * bigFactor));
				sb.append("<");
				sb.append(round(MAX * smallFactor));
			}
			return sb.toString();
		} // end of IntString.asShortString()

// Not in use at the moment
		String asInverseString() {
			StringBuffer sb = new StringBuffer();
			if (MAX * MIN < 0) {
				sb.append(-Math.round(MAX * bigFactor));
				sb.append("<");
				sb.append(-Math.round(MIN * bigFactor));
			} else if (MAX * MIN == 0) {
				sb.append(-Math.round(MAX * bigFactor));
				sb.append("<");
				sb.append(-Math.round(MIN * bigFactor));
			} else if (MAX >= 0) {
				sb.append(-Math.round(MAX * bigFactor));
				sb.append("<");
				sb.append(-Math.round(MIN * smallFactor));
			} else {
				sb.append(-Math.round(MAX * smallFactor));
				sb.append("<");
				sb.append(-Math.round(MIN * bigFactor));
			}
			return sb.toString();
		} // end of IntString.asInverseString()
		
	} // end of IntString
			

/**
* Key for lower security margin factor = "SmallLimitFactor".
*/
	public static final String SMALLFACTORKEY = "SmallLimitFactor";

/**
* Key for upper security margin factor = "BigLimitFactor".
*/
	public static final String BIGFACTORKEY = "BigLimitFactor";

/**
* = "OrderedNames".
*/
	static final String ORDEREDNAMES = "OrderedNames";

	private static boolean fin = false; // if true, IntString.asString() gives long format
	
	private Hashtable intStringsTable = new Hashtable(); // for summed IntStrings
	
	private float smallFactor; // lower security margin factor
	private float bigFactor; // upper security margin factor
	
	private PrintWriter outFile;
	
	private Hashtable subGeneTable = new Hashtable(); // contains one Hashtable for each SubGene, each containing the MotifGroup names, and those names in numerical order under key ORDEREDNAMES
	private String[ ] subGenesArray; // array of lines from SubGenes in script, consisting of SubGene name and hotspot MotifGroup name
	private String[ ] genesArray;
	private ParameterFileWriter groupsWriter;
	private ParameterFileWriter genesWriter;
	private ExecutorScriptWriter subgenesWriter;
	
/**
* Standard Executor constructor.
*/
	public DredgeAlignments() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(SMALLFACTORKEY, "0.75");
		explanations.put(SMALLFACTORKEY, "Factor for lower limit");
		orderedkeys.push(SMALLFACTORKEY);
		parameters.put(BIGFACTORKEY, "1.25");
		explanations.put(BIGFACTORKEY, "Factor for upper limit");
		orderedkeys.push(BIGFACTORKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 16";
  } // end of version()
  
/**
* Execute as specified above.
*/
	public String execute() throws RetroTectorException {

		smallFactor = getFloat(SMALLFACTORKEY, 0.75f);
		bigFactor = getFloat(BIGFACTORKEY, 1.25f);
		subGenesArray = getStringArray("SubGenes");
		genesArray = getStringArray("Genes");
		for (int isss=0; isss<subGenesArray.length; isss++) { // make one Hashtable for each SubGene
			String[ ] ss = Utilities.splitString(subGenesArray[isss]); // to get at SubGebe name
			Hashtable h = new Hashtable();
			subGeneTable.put(ss[0], h);
		}
		File oFile = new File(RetroTectorEngine.currentDirectory(), "Dredge.txt");
		try {
			outFile = new PrintWriter(new FileWriter(oFile));
		} catch (IOException ioe) {
			haltError("Trouble creating Dredge.txt");
		}
		ParameterFileReader reader = new ParameterFileReader(new File(RetroTectorEngine. currentDirectory(), Database.MOTIFFILENAME), parameters);
		reader.readParameters();
		reader.close();
		String[ ] mos = getStringArray(Database.MOTIFSKEY); // all ines in Motifs
		Hashtable htemp;
		for (int imos=0; imos<mos.length; imos++) { // collect MotifGroup names for SubGenes
			if (subGeneTable.get(Motif.subGenePart(mos[imos])) != null) { // Hashtable for this SubGene?
				try {
					htemp = (Hashtable) subGeneTable.get(Motif.subGenePart(mos[imos]));
					htemp.put(Motif.motifGroupPart(mos[imos]), Motif.motifGroupPart(mos[imos]));
				} catch (RuntimeException e) { // left over for debugging
					System.err.println(mos[imos]);
					System.err.println(Motif.subGenePart(mos[imos]));
					System.err.println(subGeneTable.get(Motif.subGenePart(mos[imos])));
					throw e;
				}
			}
		}
		
		String[ ] ss = RetroTectorEngine. currentDirectory().list();
		String fileName;
		for (int s=0; s<ss.length; s++) { // dredge each Alignment file
			fileName = ss[s];
			if (fileName.endsWith("Alignment.txt") | fileName.endsWith("alignment.txt")) {
				doOneAlignment(fileName);
      }
    }
		
		fin = true; // making summing-up part at end of Dredge.txt
// move contents of intStringsTable to array
		Enumeration el = intStringsTable.keys();
		String s;
		IntString js;
		Stack sta = new Stack();
		while (el.hasMoreElements()) {
      s = (String) el.nextElement();
			js = (IntString) intStringsTable.get(s);
			sta.push(js.asString());
		}
		String[ ] isa = new String[sta.size()];
		sta.copyInto(isa);
		Arrays.sort(isa); // sort alphabetically
		for (int ui=0; ui<isa.length; ui++) {
			outFile.println(isa[ui]);
		}
		outFile.println(" " + ali + " Alignments dredged");
		outFile.close();
		
// make MotifGroups
		groupsWriter = new ParameterFileWriter(new File(RetroTectorEngine. currentDirectory(), "MotifGroupsCandidate.txt"));
		groupsWriter.writeComment(" " + Database.LASTCHANGED + " " + Utilities.swedishDate(new Date()));
		for (int igr=0; igr<subGenesArray.length; igr++) {
			doGroupsOf(subGenesArray[igr]);
		}
		groupsWriter.close();
		
// make Genes
		genesWriter = new ParameterFileWriter(new File(RetroTectorEngine. currentDirectory(), "GenesCandidate.txt"));
		genesWriter.writeComment(" " + Database.LASTCHANGED + " " + Utilities.swedishDate(new Date()));
		for (int ifg=0; ifg<genesArray.length; ifg++) {
			doOneGene(genesArray[ifg]);
		}
		genesWriter.close();
		
// make script for MakeSubGenes
		subgenesWriter = new ExecutorScriptWriter(new File(RetroTectorEngine. currentDirectory(), "MakeSubGenesScript.txt"), "MakeSubGenes");
		subgenesWriter.writeComment(" " + Database.LASTCHANGED + " " + Utilities.swedishDate(new Date()));
		subgenesWriter.startMultiParameter("SubGenes", false);
		for (int ifg=0; ifg<subGenesArray.length; ifg++) {
			doOneSubGene(ifg); // output one line for each SubGene
		}
		subgenesWriter.finishMultiParameter(false);
		
// make Help parameter with distances between MotifGroups and Gene end points
		subgenesWriter.startMultiParameter("Help", false);
		String q = null;
		for (int hi=0; hi<genesArray.length; hi++) {
			try {
				String[ ] split = Utilities.splitString(genesArray[hi]);
				q = "<" + split[0].toLowerCase() + "START><" + hotspotName(split[1]) + ">";
				Object o = intStringsTable.get(q);
				IntString ist = (IntString) o;
				if (ist != null) {
					subgenesWriter.appendToMultiParameter(ist.asString(), false);
				}
				q = "<" + hotspotName(split[split.length - 1]) + "><" + split[0].toLowerCase() + "END>";
				o = intStringsTable.get(q);
				ist = (IntString) o;
				if (ist != null) {
					subgenesWriter.appendToMultiParameter(ist.asString(), false);
				}
			} catch (RuntimeException e) { // debugging aid
				System.err.println(q);
				throw e;
			}
		}
		subgenesWriter.finishMultiParameter(false);
		subgenesWriter.close();
				
		RetroTectorEngine.displayError(new RetroTectorException("DredgeAlignments", "Results are in MakeSubGenesScript.txt", "Edit and run it"), RetroTectorEngine.NOTICELEVEL);
	  return "";
	} // end of execute()

	private int ali = 0; // counts Alignment files dredged
	
/**
* Returns name of hotspot MotifGroup.
*/
	private final String hotspotName(String subGeneName) {
		for (int i=0; i<subGenesArray.length; i++) {
			if (subGenesArray[i].startsWith(subGeneName)) {
				return Utilities.splitString(subGenesArray[i])[1].trim();
			}
		}
		return null;
	} // end of hotspotName(String)
	
	private StringBuffer sbu;
	private int cw = 13; // minimum width of columns
	
	private final void sbuAppend(String s) {
	  sbu.append(s);
		for (int i = s.length(); i<cw - 1; i++) {
			sbu.append(" ");
		}
		sbu.append(" ");
	} // end of sbAppend(String)
	  
	private final void doOneSubGene(int index) throws RetroTectorException {
		String subGene1Name = Utilities.splitString(subGenesArray[index])[0].trim();
		sbu = new StringBuffer();
		sbuAppend(subGene1Name);
		if (index == subGenesArray.length - 1) {
			for (int u=0; u<subGenesArray.length; u++) {
				if (u == index) {
					sbuAppend("0<0");
				} else {
					sbuAppend("?");
				}
			}
			subgenesWriter.appendToMultiParameter(sbu.toString(), false);
			return;
		}
	
		String hotspot1Name = Utilities.splitString(subGenesArray[index])[1].trim();
		String hotspot2Name = Utilities.splitString(subGenesArray[index + 1])[1].trim();
		String s = "<" + hotspot1Name + "><" + hotspot2Name + ">";
		IntString ist = (IntString) intStringsTable.get(s); // get distance between hotspot of this and next

		for (int u=0; u<subGenesArray.length; u++) {
			if (u == index) {
				sbuAppend("0<0");
			} else if ((u == index + 1) && (ist != null)) {
				sbuAppend(ist.asShortString());
			} else {
				sbuAppend("?");
			}
		}
		subgenesWriter.appendToMultiParameter(sbu.toString(), false);
	} // end of doOneSubGene(int)
	
	private final void doOneGene(String gString) throws RetroTectorException {
		String[ ] split = Utilities.splitString(gString);
		String gstart = "<" + split[0].toLowerCase() + "START>";
		String gend = "<" + split[0].toLowerCase() + "END>";
		genesWriter.startMultiParameter(split[0], false);
		Hashtable h;
		String[ ] gnames;
		IntString ist1;
		IntString ist2;
		String st0;
		for (int spi=1; spi<split.length; spi++) { // one for each subgene mentioned under this Gene
			h = (Hashtable) subGeneTable.get(split[spi]);
			try {
				gnames = (String[ ]) h.get(ORDEREDNAMES);
			} catch (NullPointerException e) { // debugging aid
				System.err.println(split[spi] + " " + spi);
				throw e;
			}
			for (int gni=0; gni<gnames.length; gni++) {
				st0 = "<" + gnames[gni] + ">" + gstart;
				ist1 = (IntString) intStringsTable.get(st0);
				st0 = "<" + gnames[gni] + ">" + gend;
				ist2 = (IntString) intStringsTable.get(st0);
				if ((ist1 != null) & (ist2 != null)) {
					st0 = gnames[gni] + " " + ist1.asShortString() + "  " + ist2.asShortString();
					genesWriter.appendToMultiParameter(st0, false);
				}
			}
		}
		genesWriter.finishMultiParameter(false);
				
	} // end of doOneGene(String)
	
// sgString consists of SubGene name and hotspot MotifGroup name
	private final void doGroupsOf(String sgString) throws RetroTectorException {
	
		String subGeneName = Utilities.splitString(sgString)[0].trim();
		String hotspotName = Utilities.splitString(sgString)[1].trim();
		Hashtable oneSGtab = (Hashtable) subGeneTable.get(subGeneName); // get the table associated with the SubGene
		String[ ] groupNames = new String[oneSGtab.size()];
		Enumeration e = oneSGtab.keys();
		int gi = 0;
		while (e.hasMoreElements()) { // get MotifGroup names into groupNames
			groupNames[gi++] = (String) e.nextElement();
		}
		Arrays.sort(groupNames); // sort it alphabetically, i e by terminating number, for doOneGene
		oneSGtab.put(ORDEREDNAMES, groupNames); // and put it into Hasthable
		
		String value; // to build line in
		Object o;
		String ks;
		IntString ist;
		for (gi=0; gi<groupNames.length; gi++) { // build one line in MotifGroups.txt
			value = subGeneName; // begins with name of containing SubGene
			ks = "<" + groupNames[gi] + "><" + hotspotName + ">";
// followed by distance to hotspot
			if (groupNames[gi].equals(hotspotName)) {
				value = value + " 0<0";
			} else if ((o = intStringsTable.get(ks)) != null) {
				ist = (IntString) o;
				value = value + " " + ist.asShortString();
// followed by distances to other MotifGroups
				for (int gii=0; gii<groupNames.length; gii++) {
					if (!groupNames[gii].equals(groupNames[gi]) & !groupNames[gii].equals(hotspotName)) {
						ks = "<" + groupNames[gi] + "><" + groupNames[gii] + ">";
						if ((o = intStringsTable.get(ks)) != null) {
							ist = (IntString) o;
							value = value + " " + groupNames[gii] + ":" + ist.asShortString();
						}
					}
				}
			} else {
				value = value + " ??????"; // distance to hotspot not found
			}
			groupsWriter.writeSingleParameter(groupNames[gi], value, false);
		}
	} // end of doGroupsOf(String)
	
  private final void doOneAlignment(String fileName) throws RetroTectorException {
    outFile.println("Dredging alignment " + fileName);
    Utilities.outputString("Dredging alignment " + fileName);
		Alignment aln = new Alignment(new File(RetroTectorEngine. currentDirectory(), fileName));
		Stack stack = new Stack(); // for MotifGroup markers, with their distances from start
		String s = "<" + fileName.substring(1, 4) + "START>";
		stack.push(new IntString(s, 0));
		s = "<" + fileName.substring(1, 4) + "END>";
		stack.push(new IntString(s, aln.MASTERLENGTH));
		
		int index = 0;
		int in;
		while ((index = aln.MOTIFLINE.indexOf("<", index)) >= 0) {
			in = aln.MOTIFLINE.indexOf(">", index);
			s = aln.MOTIFLINE.substring(index, in + 1);
			stack.push(new IntString(s, index));
			index++;
		}
		index = 0;
		while ((index = aln.MOTIFLINE2.indexOf("<", index)) >= 0) {
			in = aln.MOTIFLINE2.indexOf(">", index);
			s = aln.MOTIFLINE2.substring(index, in + 1);
			stack.push(new IntString(s, index));
			index++;
		}
		IntString[ ] iss = new IntString[stack.size()]; // for MotifGroup markers, with their distances from start
		stack.copyInto(iss);
		
		Hashtable markerPairTable = new Hashtable(); // for IntStrings for marker pairs
		IntString is;
		String sqs;
		IntString oi;
		for (int row=0; row<aln.NROFROWS; row++) {
			outFile.println(" " + fileName + " Row " + (row + 1) + " " + aln.getRow(row).ROWORIGIN);
			for (int p1=0; p1<iss.length; p1++) {
				for (int p2=0; p2<iss.length; p2++) {
					if (p2 != p1) {	
						int dist = aln.getRow(row).nonDashesBetween(iss[p1].MIN, iss[p2].MIN); // distance between markers in this row
						if (dist != Integer.MIN_VALUE) {
							sqs = iss[p1].STRING + iss[p2].STRING;
							oi = new IntString(sqs, dist * 3, dist * 3); // IntString for dist
							outFile.println("    " + oi.asString());
							is = (IntString) markerPairTable.get(sqs);
							if (is == null) {  // is there one already for this marker pair?
								markerPairTable.put(sqs, oi); // no, put this one in
							} else {
								markerPairTable.put(sqs, new IntString(oi, is)); // yes, combine them
							}
						}
					}
				}
			}
		}
		
// put summative IntStrings into intStringsTable
		Enumeration eu = markerPairTable.keys();
		while (eu.hasMoreElements()) {
			sqs = (String) eu.nextElement();
			oi = (IntString) markerPairTable.get(sqs);
			is = (IntString) intStringsTable.get(sqs);
			if (is == null) { // is there one already for this marker pair?
				intStringsTable.put(sqs, oi); // no, put this one in
			} else {
				intStringsTable.put(sqs, new IntString(oi, is)); // yes, combine them
			}
		}
		ali++; // increment Alignment counter
  } // end of doOneAlignment(String)
	
} // end of DredgeAlignments
