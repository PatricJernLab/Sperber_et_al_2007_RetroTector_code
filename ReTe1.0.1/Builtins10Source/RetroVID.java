/*
* Copyright (?) 2000-2006, G?ran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/10 -06
* Beautified 12/10 -06
*/
package builtins;

import retrotector.*;
import retrotectorcore.*;

import java.util.*;
import java.io.*;

/**
* Executor to construct and select Chains.
* It is normally run using a script generated by LTRID.
* Output is a file with selected Chains (usually from both strands) and a number of ORFID scripts.
* Optionally, files with nonselected Chains from each strand may be output.
*<PRE>
*     Parameters:
*
*   InputFile
* File to get parameters from. Only used in interactive mode.
* Default: "".
*
*   DNAFile
* The name of the DNA file in the current directory to search in.
* Default: name of current directory + '.txt'.
*
*   Database
* The Database subdirectory to use.
* Default: Ordinary.
*
*   ConservationFactor
* Bonus factor for highly conserved positions.
* Default: 2.0.
*
*   FrameFactor
* Bonus factor when no disturbing frame shifts are present.
* Default: 1.5.
*
*   LengthBonus
* Bonus factor applied for each SubGeneHit.
* Default: 1.02.
*
*   SDFactor
* To multiply SD with when setting threshold.
* Default: 5.5.
*
*   SubGeneHitsMax
* Maximum number of SubGeneHits tried in one SubGene when making a Chain.
* Default: 5.
*
*   ImproveHitsMax
* Maximum number of SubGeneHits tried in one SubGene when improving a Chain.
* Default: 5.
*
*   SelectionThreshold
* Chains with less score than this before improving cannot be selected.
* Default: 150.0.
*
*   FinalSelectionThreshold
* Chains with less score than this after improving cannot be selected.
* Default: 250.0.
*
*   KeepThreshold
* Chains with less score than this are discarded early.
* Default: 25.0.
*
*   MaxSubGeneSkip
* Largest number of SubGenes skipped in forming Chains.
* Default: 2
*
*   BrokenPenalty
* Penalty factor applied to score of broken Chain.
* Default: 0.9
*
*   BrokenPasses
* Number of passes in making broken Chains.
* Default: 1.
*
*   FitPuteins
* If this is Yes, ordinary ORFID scripts are generated non-sweepable, and a 'biggie'
* ORFID script is generated.
* Default: Yes
*
*   Strand
* May be Both, Primary or Secondary, depending on which DNA strand
* should be analyzed.
* Default: Both
*
*   ORFIDMinScore
* For Chains with less score than this, ORFID scripts are not generated sweepable.
* Default: 200
*
*   MakeChainsFiles
* If this is >0, Chain output files are generated for each strand, with
* up to this many Chains.
* Default: 0
*
*   Debugging
* If this is Yes, a lot of extra information is written to Standard Output.
* Default: No
*</PRE>
*/
public class RetroVID extends Executor {

/**
* Key for bonus factor when no disturbing frame shifts are present = "FrameFactor".
*/
	static final String FRAMEKEY = "FrameFactor";
  
/**
* Key for bonus factor applied for each SubGeneHit = "LengthBonus".
*/
	static final String LENGTHBONUSKEY = "LengthBonus";

/**
* Key for maximum number of SubGeneHits tried in one SubGene
* when making a Chain = "SubGeneHitsMax".
*/
	static final String SUBGENELIMITKEY = "SubGeneHitsMax";

/**
* Key for maximum number of SubGeneHits tried in one SubGene
* when improving a Chain = "ImproveHitsMax".
*/
	static final String IMPROVELIMITKEY = "ImproveHitsMax";

/**
* Key for lowest score for selected Chains before processing = "SelectionThreshold".
*/
	static final String SELECTIONTHRESHOLDKEY = "SelectionThreshold";

/**
* Key for lowest score for selected Chains after processing = "FinalSelectionThreshold".
*/
	static final String FINALSELECTIONTHRESHOLDKEY = "FinalSelectionThreshold";

/**
* Key for score below which Chains are summarily killed = "KeepThreshold".
*/
	static final String KEEPTHRESHOLDKEY = "KeepThreshold";

/**
* Key for largest number of SubGenes skipped in forming Chains.
*/
	static final String SUBGENESKIPKEY = "MaxSubGeneSkip";

/**
* Key for length of largest insertion assumed in forming broken Chains = "MaxInsertion".
*/
	static final String MAXINSERTKEY = "MaxInsertion";

/**
* Key for penalty factor for score of broken Chain = "BrokenPenalty".
*/
	static final String BROKENPENALTYKEY = "BrokenPenalty";

/**
* Key for number of passes in making broken Chains = "BrokenPasses".
*/
	static final String BROKENPASSESKEY = "BrokenPasses";

/**
* Key for indicator that gag, pro and pol should be end-to-end = "FitPuteins".
*/
	static final String FITPUTEINSKEY = "FitPuteins";

/**
* Key for number of Chains included in Chains files = "MakeChainsFiles".
*/
	static final String CHAINSFILESKEY = "MakeChainsFiles";

/**
* Maximum nuber of selected Chains in output file for those = 100.
*/
	public static final int OUTPUTSELECTEDCHAINS = 100;

/**
* RetroVID searches for broken chains if true (it is).
*/
	public static final boolean SEARCHBROKEN = true;
	

  private float cfact = Float.NaN; // bonus factor for highly conserved positions
  private float sdFactor = Float.NaN;  // To multiply SD with when setting threshold
	private float frameFactor; // Bonus factor for no frame shift

	private int subGeneHitsLimit; // Maximum number of SubGeneHits tried in one SubGene when making a Chain
	private int processHitsLimit; // Maximum number of SubGeneHits tried in one SubGene when improving a Chain

	private float selectionThreshold; // Lowest score for selected Chains before impropvement
	private float finalSelectionThreshold; // Lowest score for selected Chains after impropvement
	private float keepThreshold; // If score lower than this, skip it

	private float brokenPenalty; // Score penalty for insertion (broken chain)
	private int brokenPasses; // Number of passes in making broken Chains

	private boolean fitPuteins; // If true, end-to-end gag, pro, pol
	private float lengthBonus; // bonus factor for each new SubGene in Chain
  
  private DNA targetDNA = null; // the DNA currently attacked
	private DNA primDNA = null; // primary strand
	private DNA secDNA = null; // secondary strand

	private int maxSubGeneSkip; // Largest number of SubGenes skipped in forming Chains
	private float orfidMinScore; // Chains with less score than this are not swept by ORFID
	private int chainsFilesLength; // Number of Chains to output for separate strands
	
	private Vector tempchainhits = null; // Chains, ordered by position
	private Chain[ ] chains = null; // array of all Chains
	private Chain[ ] chainsPrimary = null; // array of all Chains in primary strand
	private Chain[ ] chainsSecondary = null; // array of all Chains in secondary strand

	private File allFile = null; // for best chains
	private File selectedFile = null; // for selected chains

  private Database database;
	
	private String scriptName;
	private String selectedFileName = null;

 	private boolean debugging = false;
	
	private long starttime;
 	
/**
* Standard type Executor constructor.
*/
	public RetroVID() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(INPUTFILEKEY, "");
		explanations.put(INPUTFILEKEY, "LTRID output file with parameters for RetroVID");
		orderedkeys.push(INPUTFILEKEY);

		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(DATABASEKEY, ORDINARYDATABASE);
		explanations.put(DATABASEKEY, "The relevant database subdirectory");
		orderedkeys.push(DATABASEKEY);
		parameters.put(CFACTKEY, "2");
		explanations.put(CFACTKEY, "Bonus factor for highly conserved positions");
		orderedkeys.push(CFACTKEY);
		parameters.put(FRAMEKEY, "1.5");
		explanations.put(FRAMEKEY, "Bonus factor for no frame shift");
		orderedkeys.push(FRAMEKEY);
		parameters.put(LENGTHBONUSKEY, "1.02");
		explanations.put(LENGTHBONUSKEY, "Bonus for each MotifHit in Chain");
		orderedkeys.push(LENGTHBONUSKEY);
		parameters.put(SDFACTORKEY, "5.5");
		explanations.put(SDFACTORKEY, "To multiply SD with when setting threshold");
		orderedkeys.push(SDFACTORKEY);
		parameters.put(SUBGENELIMITKEY, "5");
		explanations.put(SUBGENELIMITKEY, "Maximum number of SubGeneHits tried in one SubGene when making a Chain");
		orderedkeys.push(SUBGENELIMITKEY);
		parameters.put(IMPROVELIMITKEY, "5");
		explanations.put(IMPROVELIMITKEY, "Maximum number of SubGeneHits tried in one SubGene when improving a Chain");
		orderedkeys.push(IMPROVELIMITKEY);
		parameters.put(SELECTIONTHRESHOLDKEY, "150.0");
		explanations.put(SELECTIONTHRESHOLDKEY, "Lowest score for selected Chains before processing");
		orderedkeys.push(SELECTIONTHRESHOLDKEY);
		orderedkeys.push(FINALSELECTIONTHRESHOLDKEY);
		parameters.put(FINALSELECTIONTHRESHOLDKEY, "250.0");
		explanations.put(FINALSELECTIONTHRESHOLDKEY, "Lowest score for selected Chains after processing");
		parameters.put(KEEPTHRESHOLDKEY, "25.0");
		explanations.put(KEEPTHRESHOLDKEY, "Chains worse than this are thrown out early");
		orderedkeys.push(KEEPTHRESHOLDKEY);
		parameters.put(SUBGENESKIPKEY, "2");
		explanations.put(SUBGENESKIPKEY, "Largest number of SubGenes skipped in forming Chains");
		orderedkeys.push(SUBGENESKIPKEY);
		parameters.put(BROKENPENALTYKEY, "0.9");
		explanations.put(BROKENPENALTYKEY, "Score penalty for broken Chain");
		orderedkeys.push(BROKENPENALTYKEY);
		parameters.put(BROKENPASSESKEY, "1");
		explanations.put(BROKENPASSESKEY, "Number of passes in making broken Chains");
		orderedkeys.push(BROKENPASSESKEY);
		parameters.put(FITPUTEINSKEY, YES);
		explanations.put(FITPUTEINSKEY, "Fit gag, pro & pol end-to-end");
		orderedkeys.push(FITPUTEINSKEY);
		parameters.put(STRANDKEY, BOTH);
		explanations.put(STRANDKEY, BOTH + " or " + PRIMARY + " or " + SECONDARY);
		orderedkeys.push(STRANDKEY);
		parameters.put(ORFIDMINSCOREKEY, "200");
		explanations.put(ORFIDMINSCOREKEY, "Chains with less score than this are not swept by ORFID");
		orderedkeys.push(ORFIDMINSCOREKEY);
		parameters.put(CHAINSFILESKEY, "0");
		explanations.put(CHAINSFILESKEY, "Number of Chains to output for separate strands");
		orderedkeys.push(CHAINSFILESKEY);
		parameters.put(DEBUGGINGKEY, NO);
		explanations.put(DEBUGGINGKEY, NO + " or " + YES);
		orderedkeys.push(DEBUGGINGKEY);
		
	} // end of constructor()
	

// selects nonoverlapping highscoring Chains. chainarray is assumed to be sorted by score but not necessarily full
	private final void makeSelection(Chain[ ] chainarray) {
		Chain tempchain;
		int counter = 0;
		int c;
// select suitable chains and select them
		while ((counter < chainarray.length) && ((tempchain = chainarray[counter]) != null)) {
			for (c=0; (c < counter) && (!tempchain.majoredBy(chainarray[c])); c++) {
			} // anybody selected and better and overlapping?
			if ((c == counter) && (chainarray[counter].CHAINSCORE > selectionThreshold)){
				chainarray[counter].select(true); // no, select it if good enough
			}
			counter++;
		}
	} // end of makeSelection(Chain[ ])


// numbers selected and unselected chains for one strand, and optionally outputs them
	private final void chainsToFiles() throws RetroTectorException {

		char strandchar;
		int counter;
    if (chains == null) {
      chains = new Chain[0];
    }
    for (int c=0; c<chains.length; c++) {
    	chains[c].chainNumber = c + 1;
    }
    if (chainsFilesLength < 1) {
    	return;
    }
    
		if (targetDNA.PRIMARYSTRAND) {
			strandchar = 'P';
		} else {
			strandchar = 'S';
		}
		showInfo("Outputting files from " + strandchar + " strand");
    allFile = Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), "" + strandchar + "_", "_Chains.txt");
    ExecutorScriptWriter outc1 = new ExecutorScriptWriter(allFile, "Chainview");
    outc1.writeSingleParameter(DNAFILEKEY, targetDNA.NAME, false);
    if (targetDNA.PRIMARYSTRAND) {
    	outc1.writeSingleParameter(STRANDKEY, PRIMARY, false);
    } else {
    	outc1.writeSingleParameter(STRANDKEY, SECONDARY, false);
    }
    outc1.writeSingleParameter(DATABASEKEY, database.DATABASENAME, false);
    counter = chainsFilesLength;
    for (int c=0; c<chains.length; c++) {
    	if (counter > 0) {
    		outc1.startMultiParameter("Chain" + chains[c].STRANDCHAR + chains[c].chainNumber, false);
    		ChainGraphInfo inf = new ChainGraphInfo(chains[c]);
    		outc1.appendToMultiParameter(inf.toStrings(chains[c].SOURCEDNA), false);
    		outc1.finishMultiParameter(false);
    		counter--;
    	}
    }
    writeInfo(outc1);
    outc1.close();
    showProgress();
  } // end of chainsToFiles()
   
// processes and outputs selected files in chains array and optionally PseuGID script
	private final String selectedChainsToFile(String na) throws RetroTectorException {
		SlipperyMotif slipperyMotif = (SlipperyMotif) database.getFirstMotif(Database.SLIPPERYMOTIFKEY);
		PseudoKnotMotif pseudoKnotMotif = (PseudoKnotMotif) database.getFirstMotif(Database.PSEUDOKNOTMOTIFKEY);
		SpliceAcceptorMotif spliceAcceptorMotif = (SpliceAcceptorMotif) database.getFirstMotif(Database.SPLICEACCEPTORMOTIFKEY);
		SpliceDonorMotif spliceDonorMotif = (SpliceDonorMotif) database.getFirstMotif(Database.SPLICEDONORMOTIFKEY);

    showInfo("Refining chains and outputting files");
		AbstractChainProcessor.collectProcessors();
		AbstractChainProcessor.resetProcessors();
		ExecutorScriptWriter pseugwriter;
    selectedFile = FileNamer.createSelectedChainsFile(na);
    ExecutorScriptWriter outc2 = new ExecutorScriptWriter(selectedFile, "Chainview");
    outc2.writeSingleParameter(DNAFILEKEY, targetDNA.NAME, false);
    outc2.writeSingleParameter(SELECTEDKEY, YES, false);
    outc2.writeSingleParameter(DATABASEKEY, database.DATABASENAME, false);

// output short descriptions of singleLTRs, for SequenceView
		LTRCandidate cand;
		String[ ] ss;
		outc2.startMultiParameter("PSingleLTRs", false);
		int c = 1;
		while ((ss = getStringArray("SingleLTR_P" + c)) != null) {
			cand = new LTRCandidate(primDNA, ss);
			outc2.appendToMultiParameter(primDNA.externalize(cand.hotSpotPosition) + " (" + primDNA.externalize(cand.candidateFirst) + "-" + primDNA.externalize(cand.candidateLast) + ") " + cand.candidateComment, false);
			c++;
		}
		outc2.finishMultiParameter(false);
		outc2.startMultiParameter("SSingleLTRs", false);
		c = 1;
		while ((ss = getStringArray("SingleLTR_S" + c)) != null) {
			cand = new LTRCandidate(secDNA, ss);
			outc2.appendToMultiParameter(secDNA.externalize(cand.hotSpotPosition) + " (" + secDNA.externalize(cand.candidateFirst) + "-" + secDNA.externalize(cand.candidateLast) + ") " + cand.candidateComment, false);
			c++;
		}
		outc2.finishMultiParameter(false);

    int counter = OUTPUTSELECTEDCHAINS;
		boolean sel;
		AbstractChainProcessor.ProcessorInfo info = new AbstractChainProcessor.ProcessorInfo(frameFactor, lengthBonus, maxSubGeneSkip, brokenPenalty, subGeneHitsLimit, processHitsLimit, keepThreshold, debugging, brokenPasses, cfact, sdFactor, parameters, this);
		SubGeneHit sgh5;
		SubGeneHit sgh3;
		LTRMotifHit lmh5;
		LTRMotifHit lmh3;
// go through chains
    for (c=0; (c<chains.length) & (counter > 0); c++) {
			sel = chains[c].isSelected();
// apply all ChainProcessors
			for (int im = 0; im<AbstractChainProcessor.processors.length; im++) {
				if (AbstractChainProcessor.processors[im].isEligible(chains[c], this)) {
					chains[c] = AbstractChainProcessor.processors[im].processChain(chains[c], info, this);
				}
			}
			if (sel & (chains[c].CHAINSCORE > finalSelectionThreshold)) {
				chains[c].select(true);
				outc2.startMultiParameter("Chain" + chains[c].STRANDCHAR + chains[c].chainNumber, false);
				ChainGraphInfo inf = new ChainGraphInfo(chains[c]);
				outc2.appendToMultiParameter(inf.toStrings(chains[c].SOURCEDNA), false);
				outc2.finishMultiParameter(false);
// include LTR descriptions, for ChainView
				sgh5 = chains[c].get5LTR();
				if (sgh5 != null) {
					lmh5 = (LTRMotifHit) sgh5.firstMotifHit();
				} else {
					lmh5 = null;
				}
				sgh3 = chains[c].get3LTR();
				if (sgh3 != null) {
					lmh3 = (LTRMotifHit) sgh3.firstMotifHit();
				} else {
					lmh3 = null;
				}
				if ((lmh5 == null) && (lmh3 != null)) {
					lmh5 = lmh3.companion;
				}
				if ((lmh3 == null) && (lmh5 != null)) {
					lmh3 = lmh5.companion;
				}
				if (lmh5 != null) {
					ss = getStringArray(lmh5.hitkey);
					outc2.writeMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + CoreLTRPair.KEY5LTR, ss, false);
				}
				if (lmh3 != null) {
					ss = getStringArray(lmh3.hitkey);
					outc2.writeMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + CoreLTRPair.KEY3LTR, ss, false);
				}
				
				if (RetroTectorEngine.getClusterMode()) {
					String rb = Utilities.repBaseFind(chains[c].SOURCEDNA, chains[c].CHAINSTART, chains[c].CHAINEND, database.getRepBaseTemplates(), false);
					outc2.startMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + "RepBaseFinds", false);
					outc2.appendToMultiParameter(Utilities.expandLines(rb), false);
					outc2.finishMultiParameter(false);
					
					String re = Utilities.findBestRV(chains[c].SOURCEDNA, chains[c].CHAINSTART, chains[c].CHAINEND, database.getRefRVs());
					outc2.writeSingleParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + "BestRefRV", re, false);
				}
				
				MotifHit mh;
				slipperyMotif.refresh(new Motif.RefreshInfo(chains[c].SOURCEDNA, 0, 0, null));
				outc2.startMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + "SlipperyMotifHits", false);
				for (int i=chains[c].CHAINSTART; i<=chains[c].CHAINEND; i++) {
					if ((mh = slipperyMotif.getMotifHitAt(i)) != null) {
						outc2.appendToMultiParameter(chains[c].SOURCEDNA.externalize(mh.MOTIFHITFIRST) + " " + chains[c].SOURCEDNA.externalize(mh.MOTIFHITLAST) + " " + Math.round(mh.MOTIFHITSCORE), false);
					}
				}
				outc2.finishMultiParameter(false);
				
				pseudoKnotMotif.refresh(new Motif.RefreshInfo(chains[c].SOURCEDNA, 0, 0, null));
				outc2.startMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + "PseudoKnotMotifHits", false);
				for (int i=chains[c].CHAINSTART; i<=chains[c].CHAINEND; i++) {
					if ((mh = pseudoKnotMotif.getMotifHitAt(i)) != null) {
						outc2.appendToMultiParameter(chains[c].SOURCEDNA.externalize(mh.MOTIFHITFIRST) + " " + chains[c].SOURCEDNA.externalize(mh.MOTIFHITLAST) + " " + Math.round(mh.MOTIFHITSCORE), false);
					}
				}
				outc2.finishMultiParameter(false);
				
				spliceAcceptorMotif.refresh(new Motif.RefreshInfo(chains[c].SOURCEDNA, 0, 0, null));
				outc2.startMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + "SpliceAcceptorMotifHits", false);
				for (int i=chains[c].CHAINSTART; i<=chains[c].CHAINEND; i++) {
					if ((mh = spliceAcceptorMotif.getMotifHitAt(i)) != null) {
						outc2.appendToMultiParameter(chains[c].SOURCEDNA.externalize(mh.MOTIFHITFIRST) + " " + chains[c].SOURCEDNA.externalize(mh.MOTIFHITLAST) + " " + Math.round(mh.MOTIFHITSCORE), false);
					}
				}
				outc2.finishMultiParameter(false);
				
				spliceDonorMotif.refresh(new Motif.RefreshInfo(chains[c].SOURCEDNA, 0, 0, null));
				outc2.startMultiParameter("" + chains[c].STRANDCHAR + chains[c].chainNumber + "SpliceDonorMotifHits", false);
				for (int i=chains[c].CHAINSTART; i<=chains[c].CHAINEND; i++) {
					if ((mh = spliceDonorMotif.getMotifHitAt(i)) != null) {
						outc2.appendToMultiParameter(chains[c].SOURCEDNA.externalize(mh.MOTIFHITFIRST) + " " + chains[c].SOURCEDNA.externalize(mh.MOTIFHITLAST) + " " + Math.round(mh.MOTIFHITSCORE), false);
					}
				}
				outc2.finishMultiParameter(false);
				
// suspiciously small LTRs, generate PseuGID script
				if (chains[c].lengthLTRs() < 200) {
					pseugwriter = new ExecutorScriptWriter(FileNamer.createPseuGIDScript(chains[c]), "PseuGID");
					pseugwriter.writeSingleParameter(DNAFILEKEY, targetDNA.NAME, false);
					pseugwriter.writeSingleParameter(DATABASEKEY, database.DATABASENAME, false);
					pseugwriter.startMultiParameter("Chain" + chains[c].STRANDCHAR + chains[c].chainNumber, false);
					pseugwriter.appendToMultiParameter(inf.toStrings(chains[c].SOURCEDNA), false);
					pseugwriter.finishMultiParameter(false);
					pseugwriter.close();
				}
				
				counter--;
    	}
    }
		outc2.writeComment("Execution time was " + (System.currentTimeMillis() - starttime) + " milliseconds");
    writeInfo(outc2);
    outc2.close();
    showProgress();
		return selectedFile.getName();
	} // end of selectedChainsToFile(String)
	

// does the job on targetDNA
	private final void doStrand(char strandchar) throws RetroTectorException {

// Collect Chains
    ChainCollector collector = new ChainCollector(targetDNA, lengthBonus, maxSubGeneSkip, brokenPenalty, brokenPasses, database);
    collector.setDebugging(debugging);
    showInfo("Refreshing in strand " + strandchar);
    collector.refreshCollector(new Motif.RefreshInfo(targetDNA, cfact, sdFactor, parameters));
    showInfo("Scoring in strand " + strandchar);
    collector.scoreSubGenes();
    showInfo("Collecting SubGene hits in strand " + strandchar);
    collector.collectSubGeneHits(frameFactor, 0, targetDNA.LENGTH - 1);
		if (debugging) {
			collector.printTree();
		}
    showInfo("Collecting chains in strand " + strandchar);
		chains = collector.collectChains(strandchar, 0, targetDNA.LENGTH - 1, subGeneHitsLimit, keepThreshold, null);
		
		chainsToFiles();
		
		if (debugging) {
			Utilities.outputString("Gene hits");
		}

	} // end of doStrand(char)


/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 12";
  } //end of version
	
/**
* @return	finalSelectionThreshold.
*/
	public final float getFinalSelectionThreshold() {
		return finalSelectionThreshold;
	} // end of getFinalSelectionThreshold()
  
// for debugging. Not in use at present
  private void printChains() {
    System.out.println("  **********");
    for (int i=0; (i<10) & (i<chains.length); i++) {
      System.out.println(chains[i].STRANDCHAR + chains[i].chainNumber + " " + chains[i].CHAINSCORE);
    }
    System.out.println("  **********");
  } // end of printChains()

/**
* Execute as specified above.
*/
	public final String execute() throws RetroTectorException {

		starttime = System.currentTimeMillis(); // Set up for time counting
		showInfo("");

		if (interactive) {
// get input file if any
			String s1 = getString(INPUTFILEKEY, "");
			if (s1.length() > 0) {
				File f = new File(RetroTectorEngine.currentDirectory(), s1);
				ParameterFileReader reader = new ParameterFileReader(f, parameters);
				reader.readParameters();
				reader.close();
			}
    }
		
    RetroTectorEngine.setCurrentDatabase(getString(DATABASEKEY, Executor.ORDINARYDATABASE));
    database = RetroTectorEngine.getCurrentDatabase();
		String fName = getString(DNAFILEKEY, "");
    cfact = getFloat(CFACTKEY, 2.0f);
		frameFactor = getFloat(FRAMEKEY, 1.5f);
		lengthBonus = getFloat(LENGTHBONUSKEY, 1.02f);
		sdFactor = getFloat(SDFACTORKEY, 5.5f);
		subGeneHitsLimit = getInt(SUBGENELIMITKEY, 5);
		processHitsLimit = getInt(IMPROVELIMITKEY, 5);
		selectionThreshold = getFloat(SELECTIONTHRESHOLDKEY, 150.0f);
		finalSelectionThreshold = getFloat(FINALSELECTIONTHRESHOLDKEY, 250.0f);
		keepThreshold = getFloat(KEEPTHRESHOLDKEY, 25.0f);
    if (getString("ChainThreshold", null) != null) {
      haltError("Please note that ChainThreshold is now called SelectionThreshold");
    }
		maxSubGeneSkip = getInt(SUBGENESKIPKEY, 2);
    if (getString("InsertionPenalty", null) != null) {
      haltError("Please note that InsertionPenalty is now called BrokenPenalty");
    }
		brokenPenalty = getFloat(BROKENPENALTYKEY, 0.9f);
		brokenPasses = getInt(BROKENPASSESKEY, 1);
		fitPuteins = getString(FITPUTEINSKEY, YES).equals(YES);
		orfidMinScore = getFloat(ORFIDMINSCOREKEY, 200.0f);
		chainsFilesLength = getInt(CHAINSFILESKEY, 0);
		scriptName = new File(getString(SCRIPTPATHKEY, "")).getName();
		if (scriptName.endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
			scriptName = scriptName.substring(0, scriptName.length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length()) + Utilities.SWEPTSCRIPTFILESUFFIX;
		}
		debugging = getString(DEBUGGINGKEY, NO).equals(YES);

		String fileName = fName;
		if (fileName.length() > 0) {
			primDNA = getDNA(fileName, true);
			secDNA = getDNA(fileName, false);
			if (getString(STRANDKEY, PRIMARY).equals(BOTH)) {
				targetDNA = primDNA;
				doStrand('P');
				chainsPrimary = chains;
				targetDNA = secDNA;
				doStrand('S');
				chainsSecondary = chains;

// collect all chains in chains array, deselected
				chains = new Chain[chainsPrimary.length + chainsSecondary.length];
				showProgress();
				Chain tempchain;
				int pp = 0;
				for (int c=0; c<chainsPrimary.length; c++) {
					tempchain = chainsPrimary[c];
					tempchain.select(false);
					chains[pp++] = tempchain;
				}
				for (int d=0; d<chainsSecondary.length; d++) {
					tempchain = chainsSecondary[d];
					tempchain.select(false);
					chains[pp++] = tempchain;
				}
				showProgress();

// select suitable chains
        Utilities.sort(chains);
				makeSelection(chains);
				selectedFileName = selectedChainsToFile("");
				showProgress();
			} else { // not BOTH
				boolean strand = getString(STRANDKEY, PRIMARY).equals(PRIMARY);
				
				if (strand) {
					targetDNA = primDNA;
					doStrand('P');
					selectedFileName = selectedChainsToFile("P_");
				} else {
					targetDNA = secDNA;
					doStrand('S');
					selectedFileName = selectedChainsToFile("S_");
				}
			}
		} else { // no file specified. Get from window
			targetDNA = RetroTectorEngine.doDNAWindow();
			if (targetDNA.PRIMARYSTRAND) {
				doStrand('P');
				selectedFileName = selectedChainsToFile("P_");
			} else {
				doStrand('S');
				selectedFileName = selectedChainsToFile("S_");
			}
		}

		for (int im=0; im<AbstractChainProcessor.processors.length; im++) {
			AbstractChainProcessor.processors[im].postProcess();
		}
		
// output ORFID scripts
		Range[ ] rstart = new Range[database.geneNames.length]; // for startpos limits
		Range[ ] rend = new Range[database.geneNames.length]; // for endpos limits
		Gene[ ] fG = new Gene[database.geneNames.length];
		Stack st;
		MotifHit mf1;
		MotifHit mf2;
		ExecutorScriptWriter fgout;
		String[ ] troubleStrings = new String[database.geneNames.length];
		for (int ch=0; ch<chains.length; ch++) {
			if (chains[ch].STRANDCHAR == 'S') {
				targetDNA = secDNA;
			} else {
				targetDNA = primDNA;
			}

// only finally selected chains
			if (chains[ch].isSelected() & (chains[ch].CHAINSCORE > finalSelectionThreshold)) { 
				for (int fg1=0; fg1<fG.length; fg1++) { //  for each gene
					fG[fg1] = database.getGene(database.geneNames[fg1]);
					rstart[fg1] = chains[ch].findGeneStartRange(fG[fg1]);
					rend[fg1] = chains[ch].findGeneEndRange(fG[fg1]);
					troubleStrings[fg1] = "";
				}

// assemble MotifHits
				MotifHitsInfo[ ] mhi = new MotifHitsInfo[fG.length];
				for (int fgg=0; fgg<fG.length; fgg++) { //  for each gene
					if ((rstart[fgg] != null) & (rend[fgg] != null)) {
						if (debugging) {
							Utilities.outputString(fG[fgg].GENENAME + " " + targetDNA.externalize(rstart[fgg].RANGEMIN) + " " + targetDNA.externalize(rend[fgg].RANGEMAX));
						}
						st = fG[fgg].findHitsInChain(chains[ch]);
						if (st.size() > 0) {
							mhi[fgg] = new MotifHitsInfo(st, targetDNA);
						}
					}
				}
				
				Stack biggieStack;
				String suff;

// make XonID script
				ExecutorScriptWriter xonout;
				File po = FileNamer.createXonIDScript(chains[ch], true);
				xonout = new ExecutorScriptWriter(po, "XonID");
				xonout.writeSingleParameter(DNAFILEKEY, targetDNA.NAME, false);
				if (targetDNA == primDNA) {
					xonout.writeSingleParameter(STRANDKEY, PRIMARY, false);
				} else {
					xonout.writeSingleParameter(STRANDKEY, SECONDARY, false);
				}
				xonout.writeSingleParameter(DATABASEKEY, chains[ch].COLLECTOR.COLLECTORDATABASE.DATABASENAME, false);
				xonout.writeSingleParameter(CHAINNUMBERKEY, String.valueOf(chains[ch].chainNumber), false);
				xonout.writeSingleParameter(CHAINSTARTKEY, String.valueOf(targetDNA.externalize(chains[ch].CHAINSTART)), false);
				xonout.writeSingleParameter(CHAINENDKEY, String.valueOf(targetDNA.externalize(chains[ch].CHAINEND)), false);
				xonout.writeSingleParameter(XonID.CHAINSFILEKEY, selectedFileName, false);
				writeInfo(xonout);
				xonout.close();

// make ORFID scripts
				for (int rv=0; rv<chains[ch].CHAINRVGENUS.length(); rv++) { // for each prominent virus genus
					biggieStack = new Stack();
					for (int fg=0; fg<fG.length; fg++) { //  for each gene
						if ((mhi[fg] != null) && (mhi[fg].usefulToORFID(database))) {
							if (fitPuteins || (chains[ch].CHAINSCORE < orfidMinScore)) { // do not sweep chains with low scores
								suff = Utilities.SWEPTSCRIPTFILESUFFIX;
							} else {
								suff = Utilities.SWEEPABLESCRIPTFILESUFFIX;
							}
							File f = FileNamer.createORFIDScript(chains[ch], chains[ch].CHAINRVGENUS.charAt(rv), fG[fg].GENENAME, suff);
							biggieStack.push(f.getName());
							fgout = new ExecutorScriptWriter(f, "ORFID");
							fgout.writeSingleParameter(DNAFILEKEY, targetDNA.NAME, false);
							if (targetDNA == primDNA) {
								fgout.writeSingleParameter(STRANDKEY, PRIMARY, false);
							} else {
								fgout.writeSingleParameter(STRANDKEY, SECONDARY, false);
							}
							fgout.writeSingleParameter(DATABASEKEY, chains[ch].COLLECTOR.COLLECTORDATABASE.DATABASENAME, false);
							if (troubleStrings[fg].length() > 0) {
								fgout.writeSingleParameter(TROUBLEKEY, troubleStrings[fg], false);
							}
							fgout.writeSingleParameter(VIRUSGENUSKEY, String.valueOf(chains[ch].CHAINRVGENUS.charAt(rv)), false);
							fgout.writeSingleParameter(GENEKEY, fG[fg].GENENAME, false);
							fgout.writeSingleParameter(FIRSTDNASTARTKEY, String.valueOf(targetDNA.externalize(targetDNA.forceInside(rstart[fg].RANGEMIN))), false);
							fgout.writeSingleParameter(LASTDNASTARTKEY, String.valueOf(targetDNA.externalize(targetDNA.forceInside(rstart[fg].RANGEMAX))), false);
							fgout.writeSingleParameter(CHAINNUMBERKEY, String.valueOf(zeroLead(chains[ch].chainNumber, 90000)), false);
							fgout.writeSingleParameter(FIRSTDNAENDKEY, String.valueOf(targetDNA.externalize(targetDNA.forceInside(rend[fg].RANGEMIN))), false);
							fgout.writeSingleParameter(LASTDNAENDKEY, String.valueOf(targetDNA.externalize(targetDNA.forceInside(rend[fg].RANGEMAX))), false);
							fgout.writeMultiParameter(HITINFOKEY, mhi[fg].toStrings(), false);
							String s = chains[ch].breaks();
							if (s.length() > 0) {
								fgout.writeSingleParameter(BREAKINFOKEY, s, false);
							}
								
							writeInfo(fgout);
							fgout.writeComment("Execution time was " + (System.currentTimeMillis() - starttime) + " milliseconds");
							fgout.close();
							showProgress();
						}
					}
					if (fitPuteins & (biggieStack.size() > 0)) {
						File f;
						if (chains[ch].CHAINSCORE < orfidMinScore) {
							f = FileNamer.createORFIDScript(chains[ch], chains[ch].CHAINRVGENUS.charAt(rv), "", "N" + Utilities.SWEPTSCRIPTFILESUFFIX);
						} else {
							f = FileNamer.createORFIDScript(chains[ch], chains[ch].CHAINRVGENUS.charAt(rv), "", "M" + Utilities.SWEEPABLESCRIPTFILESUFFIX);
						}
						fgout = new ExecutorScriptWriter(f, "ORFID");
						fgout.writeSingleParameter(DNAFILEKEY, targetDNA.NAME, false);
						if (targetDNA == primDNA) {
							fgout.writeSingleParameter(STRANDKEY, PRIMARY, false);
						} else {
							fgout.writeSingleParameter(STRANDKEY, SECONDARY, false);
						}
						fgout.writeSingleParameter(DATABASEKEY, chains[ch].COLLECTOR.COLLECTORDATABASE.DATABASENAME, false);
						fgout.writeSingleParameter(VIRUSGENUSKEY, String.valueOf(chains[ch].CHAINRVGENUS.charAt(rv)), false);
						fgout.startMultiParameter(ORFID.GENESCRIPTSKEY, false);
						for (int e=0; e<biggieStack.size(); e++) {
							fgout.appendToMultiParameter((String) biggieStack.elementAt(e), false);
						}
						fgout.finishMultiParameter(false);
						writeInfo(fgout);
						fgout.writeComment("Execution time was " + (System.currentTimeMillis() - starttime) + " milliseconds");
						fgout.close();
						showProgress();
					}
					showProgress();
				}
			}
		}
		
		RetroTectorEngine.beep();
		
		if (interactive) { // go straight for Chainview
			ExecutorScriptReader esr = new ExecutorScriptReader(selectedFile);
			Executor view = getExecutor("Chainview");
			view.initialize(esr);
			if (view.runFlag) {
				view.execute();
			}
		}

		return "";
	} // end of execute()
	
} // end of RetroVID
