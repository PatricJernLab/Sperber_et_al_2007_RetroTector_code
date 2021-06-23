/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 13/12 -06
* Beautified 13/12 -06
*/
package builtins;

import retrotector.*;
import retrotectorcore.*;

import java.util.*;
import java.io.*;
import javax.swing.*;

/**
* Executor which takes all files from the subdirectory NewDNA (or as specified)
* within the current directory and transfers them into separate
* subdirectories with the same name and accompanies them with a
* script file. The script will normally just specify LTRID as executor,
* and the appropriate DNA file. More lines may be added as extra parameters
* of the form "Line" + number (starting at 1).
* The source files may be either bare DNA files (possibly with leading comment
* lines with leading { or >), or Parameter files with a 'Sequence' parameter.
* If source files are longer than a certain maximum,
* they will be split into several overlapping files each in its own subdirectory.
* Source files may have the extension .fa, which will be changed into .txt.
*
* Unless the relevant Tolerance parameters ar <0, ALUs (entire) and LINE/L1 fragments will be
* eliminated. If any Brooms are present in plugins, they will also be applied.
*
* If the DNA file is shorter than 30000, padding with random sewuences and integration repeats
* will be offered.
*<PRE>
*     Parameters:
*
*   ChunkSize
* If the length of a DNA sequence exceeds this, it will be split into several
* files of this length or less.
* Default: 115000.
*
*   ChunkOverlap
* If several files are created, they overlap by this.
* Default: 15000.
*
*   ALUTolerance
* If this is >= 0, ALUs are searched for, with the specified error tolerance.
* Default: 10.
*
*   LINETolerance
* If this is >= 0, LINEs are searched for, with the specified error tolerance.
* Default: 10.
*
*   NewDNADirectory
* The subdirectory within the current directory containing the files
* to process.
* Default: NewDNA.
*
*   ExecutorToUse
* The Executor named in the output file.
* Default: LTRID.
*</PRE>
*/
public class SweepDNA extends Executor implements FilenameFilter {

/**
* Key for maximal length of created files = "ChunkSize".
*/
	public static final String CHUNKSIZEKEY = "ChunkSize";

/**
* Key for overlap between created files = "ChunkOverlap".
*/
	public static final String CHUNKOVERLAPKEY = "ChunkOverlap";

/**
* Key for directory where files are = "NewDNADirectory".
*/
	public static final String NEWDNAKEY = "NewDNADirectory";

/**
* Key for name of executor in created script = "ExecutorToUse".
*/
	public static final String EXECUTORTOUSEKEY = "ExecutorToUse";

/**
* Key for tolerance for errors in ALU = "ALUTolerance".
*/
	public static final String ALUTOLERANCEKEY = "ALUTolerance";
	
/**
* Key for tolerance for errors in LINE = "LINETolerance".
*/
	public static final String LINETOLERANCEKEY = "LINETolerance";
	
/**
* Default name of directory where files are = "NewDNA".
*/
	public static final String NEWDNANAME = "NewDNA";
	
	
/**
* Size of sequences to search for similarity with ALU = 11.
*/
	public static final int ALUMERSIZE = 11;
	
/**
* Step between points to search for similarity with ALU = 7.
*/
	public static final int ALUMERSTEP = 7;
		
/**
* Shortest acceptable 'a' tail of ALU = 10.
*/
	public static final int TAILMIN = 10;

/**
* Indices in result array from PadDialog.
*/
	public static final int BEFOREINDEX = 0;
	public static final int AFTERINDEX = 1;
	public static final int APERCINDEX = 2;
	public static final int CPERCINDEX = 3;
	public static final int GPERCINDEX = 4;
	public static final int REPEATINDEX = 5;
	
/**
* Model ALU sequence.
*/
	public static final String ALU = "ggccgggcgcggtggctcacgcctgtaatcccagcactttgggaggccgaggcgggaggatcacttgagcccaggagttcgagaccagcctgggcaacatagtgaaaccccgtctctacaaaaaatacaaaaattagccgggcgtggtggcgcgcgcctgtagtcccagctactcgggaggctgaggcaggaggatcgcttgagcccgggaggtcgaggctgcagtgagccgtgatcgcgccactgcactccagcctgggcgacagagcgagaccctgtctcaaaaaaaa";

	
	private int[ ] alu; // ALU as integer codes
	private int[ ] calu; // complementary ALU as integer codes
	
	private float aluTolerance; // tolerance for errors in ALU
	private float lineTolerance; // tolerance for errors in LINE
	
	private File currDir; // directory being treated at the moment
	private File newDNADirectory; // directory to fetch fresh DNA files from
	private int chunkSize; // max chunk length
	private int chunkOverlap; // overlap between chunks
	
	private int alucount = 0; // counts total ALUs

	private int acode = Compactor.BASECOMPACTOR.charToIntId('a');
	private int tcode = Compactor.BASECOMPACTOR.charToIntId('t');

/**
* Constructor. Specifies obligatory parameters.
*/
	public SweepDNA() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(CHUNKSIZEKEY, "115000");
		explanations.put(CHUNKSIZEKEY, "Maximal length of partial sequences");
		orderedkeys.push(CHUNKSIZEKEY);
		parameters.put(CHUNKOVERLAPKEY, "15000");
		explanations.put(CHUNKOVERLAPKEY, "Overlap between partial sequences");
		orderedkeys.push(CHUNKOVERLAPKEY);
		parameters.put(ALUTOLERANCEKEY, "10");
		explanations.put(ALUTOLERANCEKEY, "Tolerance for errors in ALU");
		orderedkeys.push(ALUTOLERANCEKEY);
		parameters.put(LINETOLERANCEKEY, "10");
		explanations.put(LINETOLERANCEKEY, "Tolerance for errors in L1");
		orderedkeys.push(LINETOLERANCEKEY);
		parameters.put(NEWDNAKEY, NEWDNANAME);
		explanations.put(NEWDNAKEY, "The directory with unused DNA files");
		orderedkeys.push(NEWDNAKEY);
		parameters.put(EXECUTORTOUSEKEY, "LTRID");
		explanations.put(EXECUTORTOUSEKEY, "The executor to use the script");
		orderedkeys.push(EXECUTORTOUSEKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 06";
  } // end of version()
	
// searches for two non-a bases with less distance than TAILMIN between them, the last
// being at startat or later
// returns the last position before the second one
	private final int atail(int startat, DNA dna) {
		int previous = Integer.MIN_VALUE;
// find last non-a before startat
		for (int p=dna.forceInside(startat - TAILMIN); p<startat; p++) {
			if (dna.get2bit(p) != acode) {
				previous = p;
			}
		}
		for (int pp=startat; pp<dna.LENGTH; pp++) {
			if (dna.get2bit(pp) != acode) {
				if ((pp - previous) < TAILMIN) {
					return pp - 1;
				} else {
					previous = pp;
				}
			}
		}
		return dna.LENGTH - 1;
	} // end of atail(int, DNA)
	
// searches for two non-t bases with less distance than TAILMIN between them, the first
// being at startat or earlier
// returns the last position before the second one
	private final int ttail(int startat, DNA dna) {
		int previous = Integer.MAX_VALUE;
		for (int p=dna.forceInside(startat + TAILMIN); p>startat; p--) {
			if (dna.get2bit(p) != tcode) {
				previous = p;
			}
		}
		for (int pp=startat; pp>=0; pp--) {
			if (dna.get2bit(pp) != tcode) {
				if ((previous - pp) < TAILMIN) {
					return pp + 1;
				} else {
					previous = pp;
				}
			}
		}
		return 0;
	} // end of ttail(int, DNA)
	
// searches for ALUs, direct and complementary
	private final DNA findALUs(DNA inDNA) throws RetroTectorException {
		DNA currDNA = inDNA;
		boolean oneMoreTime = true; // true if something found on latest sweep
		byte[ ] pattern = new byte[ALUMERSIZE];
		int result;
		ALUMatrix aluM1;
		ALUMatrix aluM2;
		int alun = 1; // number of next ALU
		int calun = 1; // number of next complementary ALU
		
		while (oneMoreTime) { // go through DNA repeatedly until nothing found
			oneMoreTime = false;
			for (int merpos = ALUMERSIZE; merpos<(ALU.length()-2*ALUMERSIZE); merpos += ALUMERSTEP) {
				for (int p=0; p<pattern.length; p++) { // make pattern of part of ALU
					pattern[p] = (byte) alu[merpos + p];
				}
				AgrepContext acont = new AgrepContext(pattern, ALUMERSIZE, currDNA.LENGTH - ALUMERSIZE  * 2, 0);
				while ((result = currDNA.agrepFind(acont)) != Integer.MIN_VALUE) { // got one
					result = Math.abs(result);
					aluM1 = ALUMatrix.getALUMatrix(alu, merpos - 1, currDNA, result - 1, Math.round(1.2f * (merpos - 1)), false,  1.0f, 1.0f, 0.95f, aluTolerance); // align backwards
					if (aluM1 != null) {
						aluM2 = ALUMatrix.getALUMatrix(alu, merpos + ALUMERSIZE, currDNA, result + ALUMERSIZE, Math.round(1.2f * (alu.length - merpos - ALUMERSIZE)), true,  1.0f, 1.0f, 0.95f, aluTolerance); // align forwards
						if (aluM2 != null) { // both worked, get end of tail
							int at = atail(result + ALUMERSIZE + aluM2.intercept() + 1, currDNA);
							if (currDNA.get2bit(at) != acode) {
								at--;
							}
							Utilities.outputString("ALU found at " + currDNA.externalize(result - aluM1.intercept() - 1) + " to " + currDNA.externalize(at));
							try {
								currDNA = new DNA(currDNA, result - aluM1.intercept() - 1, at, "ALU " + alun++);
								alucount++;
								oneMoreTime = true;
							} catch (RetroTectorException e) {
							}
						}
					}
				}
			}
// the same for complementary ALU
			for (int merpos = ALUMERSIZE; merpos<(ALU.length()-2*ALUMERSIZE); merpos += ALUMERSTEP) {
				for (int p=0; p<pattern.length; p++) { // make pattern of part of complementary ALU
					pattern[p] = (byte) calu[merpos + p];
				}
				AgrepContext acont = new AgrepContext(pattern, ALUMERSIZE, currDNA.LENGTH - ALUMERSIZE  * 2, 0);
				while ((result = currDNA.agrepFind(acont)) != Integer.MIN_VALUE) { // got one
					result = Math.abs(result);
					aluM1 = ALUMatrix.getALUMatrix(calu, merpos - 1, currDNA, result - 1, Math.round(1.2f * (merpos - 1)), false,  1.0f, 1.0f, 0.95f, aluTolerance); // align backwards
					if (aluM1 != null) {
						aluM2 = ALUMatrix.getALUMatrix(calu, merpos + ALUMERSIZE, currDNA, result + ALUMERSIZE, Math.round(1.2f * (alu.length - merpos - ALUMERSIZE)), true,  1.0f, 1.0f, 0.95f, aluTolerance); // align forwards
						if (aluM2 != null) { // both worked, get start of tail
							int tt = ttail(result - aluM1.intercept() - 2, currDNA);
							if (currDNA.get2bit(tt) != tcode) {
								tt++;
							}
							Utilities.outputString("Complementary ALU found at " + currDNA.externalize(tt) + " to " + currDNA.externalize(result + ALUMERSIZE + aluM2.intercept()));
							try {
								currDNA = new DNA(currDNA, tt, result + ALUMERSIZE + aluM2.intercept(), "CALU " + calun++);
								alucount++;
								oneMoreTime = true;
							} catch (RetroTectorException e) {
							}
						}
					}
				}
			}
		}
		return currDNA;
	} // end of findALUs(DNA)
	  
	private final DNA useBrooms(DNA inDNA) throws RetroTectorException {
		DNA dna = inDNA;
		do {
			Broom.doneCount = 0;
			for (int i=0; i<Broom.brooms.length; i++) {
				dna = Broom.brooms[i].findDirt(dna);
			}
		} while (Broom.doneCount != 0);
		return dna;
	} // end of useBrooms(DNA)
	
/**
* Executes, using parameters above.
*/
	public final String execute() throws RetroTectorException {
    currDir = RetroTectorEngine.currentDirectory();
		if (currDir == null) {
			haltError("The current directory is not valid");
		}
		newDNADirectory = new File(currDir, getString(NEWDNAKEY, NEWDNANAME));
		String[ ] fileList = newDNADirectory.list(this);
		if ((fileList == null) || (fileList.length == 0)) {
			return "";
		}
		chunkSize = getInt(CHUNKSIZEKEY, 115000);
		chunkOverlap = getInt(CHUNKOVERLAPKEY, 15000);
		
		int ci;
		Compactor comp = Compactor.BASECOMPACTOR;

		aluTolerance = getFloat(ALUTOLERANCEKEY, 10);
// build alu and calu
		alu = new int[ALU.length()];
		calu = new int[ALU.length()];
		for (int p=0; p<alu.length; p++) {
			ci = comp.charToIntId(ALU.charAt(p));
			if (ci < 0) {
				haltError("Invalid character in ALU:" + ALU.charAt(p));
			}
			if (ci > 3) {
				alu[p] = 4;
				calu[calu.length - 1 - p] = 4;
			} else {
				alu[p] = ci;
				calu[calu.length - 1 - p] = (~ci) & 3;
			}
		}
		
		lineTolerance = getFloat(LINETOLERANCEKEY, 10);
		L1Broom.setTolerance(lineTolerance);
		for (int i=0; i<Broom.brooms.length; i++) {
			Broom.brooms[i].zeroCount();
		}

		for (int f=0; f<fileList.length; f++) { // go through list of files to transfer
			long fl = (new File(newDNADirectory, fileList[f])).length(); // do small files first
			if (fl < 60000) {
				doOneFile(fileList[f]);
				fileList[f] = null;
				RetroTectorEngine.setCurrentDirectory(currDir, "swps");
			}
		}
		for (int f=0; f<fileList.length; f++) { // go through list of files to transfer
			if (fileList[f] != null) {
				doOneFile(fileList[f]);
				RetroTectorEngine.setCurrentDirectory(currDir, "swps");
			}
		}
		
		Utilities.outputString("Total nr of ALUs: " + alucount);
		for (int i=0; i<Broom.brooms.length; i++) {
			Utilities.outputString("Total nr of " + Broom.brooms[i].getDirtName() + "s: " + Broom.brooms[i].getCount());
		}
		return "";
	} // end of execute()

// handle one file in NewDNA
	private final void doOneFile(String oldFileName) throws RetroTectorException {
		showProgress();
		if (oldFileName.endsWith(".fa")) { // change .fa extension to .txt
			File oFile = new File(newDNADirectory, oldFileName);
			oldFileName = oldFileName.substring(0, oldFileName.length() - 3) + FileNamer.TXTTERMINATOR;
			File nFile = new File(newDNADirectory, oldFileName);
			if (!oFile.renameTo(nFile)) {
				haltError("Renaming of .fa file to " + oldFileName + " did not succeed.",
						"You will have to rename it manually.");
			}
		}
		String newDirName = oldFileName;
		if (newDirName.indexOf(' ') >= 0) {
			haltError("File name " + newDirName, "contains a blank character");
		}
		if (newDirName.endsWith(FileNamer.TXTTERMINATOR) || newDirName.endsWith(".TXT")) {
			newDirName = newDirName.substring(0, newDirName.length() - 4);
		} // newDirName freed of extension
		File newDir = new File(currDir, newDirName); // create directory
		if (newDir.exists()) {
			haltError(newDirName + " already exists");
		} else {
			newDir.mkdir();
		}
	
		String currentline = null;
		File oldFile = new File(newDNADirectory, oldFileName); // old file
		long oldFileLength = oldFile.length();
		File newFile = new File(newDir, oldFileName); // new file
		Hashtable h = new Hashtable();
		ParameterFileReader pfr = new ParameterFileReader(oldFile, h);
		RetroTectorEngine.setInfoField("Transferring " + oldFileName);
		if (pfr.getSingleParameters()) { // is it RetroTector DNA file?
			if (!oldFile.renameTo(newFile)) { //yes
				BufferedReader br = null; // renaming did not work, copy line by line
				try {
					br = new BufferedReader(new FileReader(oldFile));
				} catch (IOException ioe) {
					haltError("Could not open " + oldFile.getPath());
				}
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(new FileWriter(newFile));
				} catch (IOException ioe) {
					haltError("Could not open " + newFile.getPath());
				}
				try {
					while ((currentline = br.readLine()) != null) {
						pw.println(currentline);
					}
				} catch (IOException ioe) {
					haltError("Trouble writing " + newFile.getPath());
				}
				try {
					br.close();
					pw.close();
				} catch (IOException ioe) {
					haltError("Trouble closing files ");
				}
				oldFile.delete();
			}
		} else { // no, make it such
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(oldFile));
			} catch (IOException ioe) {
				haltError("Could not open " + oldFile.getPath());
			}
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new FileWriter(newFile));
			} catch (IOException ioe) {
				haltError("Could not open " + newFile.getPath());
			}
			currentline = null;
			try {
				while (((currentline = br.readLine()).trim().startsWith("{")) || (currentline.trim().startsWith(">"))) {
					if (currentline.startsWith("{")) {
						pw.println(currentline);
					}
				}
			} catch (IOException ioe) {
				haltError("Trouble reading " + oldFile.getPath());
			}
			pw.println(DNA.SEQUENCEKEY + ParameterStream.MULTIPARAMTERMINATOR);
			pw.println(currentline);
			try {
				while ((currentline = br.readLine()) != null) {
					pw.println(currentline);
				}
			} catch (IOException ioe) {
				haltError("Trouble writing " + newFile.getPath());
			}
			pw.println(ParameterStream.MULTIPARAMTERMINATOR);
			try {
				br.close();
				pw.close();
			} catch (IOException ioe) {
				haltError("Trouble closing files ");
			}
			oldFile.delete();
		}
		if (oldFile.exists() & newFile.exists()) { // try again
			oldFile.delete();
		}
      
// new file is in place. Is it already treated?
		String sj = (String) h.get(DNA.PROCESSEDKEY);
		if ((sj != null) && sj.equals(YES)) {
			makeLTRIDScript(newDir, newDirName + FileNamer.TXTTERMINATOR);
			return;
		}
		
// Should it be split?
		RetroTectorEngine.setCurrentDirectory(newDir, "swps");
		showInfo("Processing " + newFile.getName() + "(" + oldFileLength + " bytes)");
		BufferedReader source = null;
// read off until Sequence::
		try {
			source = new BufferedReader(new FileReader(newFile));
			while (!(currentline = source.readLine().trim()).startsWith(DNA.SEQUENCEKEY)) {
			}
		} catch (IOException ioe) {
			haltError("Trouble reading " + newFile.getPath());
		}
		
		StringBuffer sb = new StringBuffer();
		try {
			currentline = source.readLine().trim();
		} catch (IOException ioe) {
			haltError("Trouble reading " + newFile.getPath());
		}
// try to pick up leading external position
		int origin = 1;
		int ind;
		if ((ind = currentline.indexOf(" ")) > 0) {
			try {
				origin = Utilities.decodeInt(currentline.substring(0, ind));
				currentline = currentline.substring(ind);
			} catch (RetroTectorException e) {
			}
		}
		
		int p;
		char c;
		Compactor comp = Compactor.BASECOMPACTOR;
		int subNr = 1; // number of next subdirectory
		File subDir;
		String subDirName;
		String cont;
		DNA chunkDNA;
		boolean processed;
// read lines internal to Sequence and collect base codes in sb
		while ((currentline != null) && !currentline.startsWith(ParameterStream.MULTIPARAMTERMINATOR)) {
			currentline = currentline.trim();
			for (p=0; p<currentline.length(); p++) {
				c = currentline.charAt(p);
				if ((c == DNA.INSERTLEADCHAR) | (c == DNA.INSERTTRAILCHAR) | (c == ':')) {
					haltError(newFile.getPath() + " seems to be already processed");
				} else if (comp.charToIntId(c) >= 0) {
					sb.append(c);
					if (sb.length() >= chunkSize) { // peel off one chunk
						showProgress();
						subDirName = newDirName + "_" + subNr++;
						subDir = new File(newDir, subDirName);
						subDir.mkdir();
						showInfo("Processing " + subDirName);
						cont = "" + origin + " " + sb.toString(); // make string for DNA constructor
						chunkDNA = new DNA(cont, subDirName, true);
						processed = false;
						if (aluTolerance >= 0) {
							chunkDNA = findALUs(chunkDNA);
							processed = true;
						}
						chunkDNA = useBrooms(chunkDNA);
						processed = true;
						
						chunkDNA.toFile(new File(subDir, subDirName + FileNamer.TXTTERMINATOR), 0, chunkDNA.LENGTH - 1, parameters, processed);

						makeLTRIDScript(subDir, subDirName + FileNamer.TXTTERMINATOR);
						sb.delete(0, chunkSize - chunkOverlap);
						origin += (chunkSize - chunkOverlap);
					}
				}
			}
			try {
				currentline = source.readLine();
			} catch (IOException ioe) {
				haltError("Trouble reading " + newFile.getPath());
			}
		}
		if (subNr == 1) { // no chunks were made, no subdirectory needed
			cont = "" + origin + " " + sb.toString();
			chunkDNA = new DNA(cont, newDirName, true);
			if (chunkDNA.LENGTH < 30000) {
				String [ ] ints = RetroTectorEngine.doPadQuestion(chunkDNA.LENGTH);
				if (ints != null) {
					int beg = chunkDNA.externalize(0);
					double amax = 0.01 * Utilities.decodeInt(ints[APERCINDEX]);
					double cmax = amax + 0.01 * Utilities.decodeInt(ints[CPERCINDEX]);
					double gmax = cmax + 0.01 * Utilities.decodeInt(ints[GPERCINDEX]);
					double d;
					StringBuffer sbbefore = new StringBuffer();
					for (int i=1; i<=Utilities.decodeInt(ints[BEFOREINDEX])-ints[REPEATINDEX].length(); i++) {
						d = Math.random();
						if (d < amax) {
							sbbefore.append('a');
						} else if (d < cmax) {
							sbbefore.append('c');
						} else if (d < gmax) {
							sbbefore.append('g');
						} else {
							sbbefore.append('t');
						}
					}
					sbbefore.append(ints[REPEATINDEX]);
					StringBuffer sbafter = new StringBuffer();
					sbafter.append(ints[REPEATINDEX]);
					for (int i=1; i<=Utilities.decodeInt(ints[AFTERINDEX])-ints[REPEATINDEX].length(); i++) {
						d = Math.random();
						if (d < amax) {
							sbafter.append('a');
						} else if (d < cmax) {
							sbafter.append('c');
						} else if (d < gmax) {
							sbafter.append('g');
						} else {
							sbafter.append('t');
						}
					}
					cont = "" + (beg - Utilities.decodeInt(ints[BEFOREINDEX])) + " " + sbbefore.toString() + sb.toString() + sbafter.toString();
					chunkDNA = new DNA(cont, newDirName, true, Utilities.decodeInt(ints[BEFOREINDEX]), Utilities.decodeInt(ints[BEFOREINDEX]) + chunkDNA.LENGTH - 1);
				}
			}
			processed = false;
			if (aluTolerance >= 0) {
				chunkDNA = findALUs(chunkDNA);
				processed = true;
			}
			chunkDNA = useBrooms(chunkDNA);
			processed = true;
						
			chunkDNA.toFile(new File(newDir, newDirName + FileNamer.TXTTERMINATOR), 0, chunkDNA.LENGTH - 1, parameters, processed);
			makeLTRIDScript(newDir, newDirName + FileNamer.TXTTERMINATOR);
		} else if (sb.length() > chunkOverlap) { // make last chunk
			subDirName = newDirName + "_" + subNr++;
			subDir = new File(newDir, subDirName);
			subDir.mkdir();
			cont = "" + origin + " " + sb.toString();
			chunkDNA = new DNA(cont, subDirName, true);
			processed = false;
			if (aluTolerance >= 0) {
				chunkDNA = findALUs(chunkDNA);
				processed = true;
			}
			chunkDNA = useBrooms(chunkDNA);
			processed = true;
						
			chunkDNA.toFile(new File(subDir, subDirName + FileNamer.TXTTERMINATOR), 0, chunkDNA.LENGTH - 1, parameters, processed);
			makeLTRIDScript(subDir, subDirName + FileNamer.TXTTERMINATOR);
		}
	} // end of doOne(String)
			

// make script, normally for LTRID, in specified directory
  private final void makeLTRIDScript(File dir, String name) throws RetroTectorException {
		String ltrid = getString(EXECUTORTOUSEKEY, "LTRID");
		File scriptFile = null;
		if (ltrid.equals("LTRID")) {
    	scriptFile = FileNamer.createLTRIDScript(dir);
		} else {
			scriptFile = Utilities.uniqueFile(dir, ltrid + "_", Utilities.SWEEPABLESCRIPTFILESUFFIX);
		}
    ExecutorScriptWriter pw = new ExecutorScriptWriter(scriptFile, ltrid);
    pw.writeSingleParameter(DNAFILEKEY, name, false);
    for (int l=1; parameters.containsKey("Line" + l); l++) {
      pw.writeLine((String) parameters.get("Line" + l));
    }
    writeInfo(pw);		
    pw.close();
  } // end of makeLTRIDScript(File, String)
	
/**
* As required by FileNameFilter. To avoid invisible files, whose name starts with '.'.
*/
	public boolean accept(File dir, String name) {
		return !name.startsWith(".");
	} // end of accept(File, String)
  
} // end of SweepDNA
