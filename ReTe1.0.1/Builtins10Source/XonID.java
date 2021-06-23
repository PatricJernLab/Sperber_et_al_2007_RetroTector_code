/*
* Copyright ((©)) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 17/10 -06
* Beautified 17/10 -06
*/
package builtins;

import retrotector.*;
import retrotectorcore.*;

import java.util.*;
import java.io.*;

	
/**
* Attempts to fit XXons and Xons.
*<PRE>
*     Parameters:
*
*  InputFile
* File to get parameters from. Only used in interactive mode.
*
*   MinChainScore
* If the parent Chain has lower score than this, nothing is done.
* Default: 1000.
*
*   LTRGagMinGap
* If the gap between 5'LTR and Gag is at least this much, XonID acts.
* Default: 100.
*
*   GagProMinGap
* If the gap between Gag and Pro is at least this much, XonID acts.
* Default: 100.
*
*   ProPolMinGap
* If the gap between Pro and Pol is at least this much, XonID acts.
* Default: 100.
*
*   PolEnvMinGap
* If the gap between Pol and Env is at least this much, XonID acts.
* Default: 100.
*
*   EnvLTRMinGap
* If the gap between Env and 3'LTR is at least this much, XonID acts.
* Default: 100.
*
*  Database
* Database subdirectory to use.
* Default: Ordinary.
*
*  ChainNumber
* Number of parent Chain.
* Default: 0
*
*  FaultsPer51
* Max of stops + 5*shifts per 51 acids.
* Default: 12.0.
*
*  SnakeThreshold
* Tolerance parameter for Xon generation.
* Default: 0.20.
*
*  ChainsFile
* Name of SelectedChains file to use.
* Default: 001SelectedChains.txt
*
*  NonAlignedScore
* The initial value assigned to the score for unaligned fitting.
* Default: 0.4
*
*  StopCodonFactor
* Factor to multiply stop codon load by in unaligned fitting.
* Default: -0.4
*
*  FrameShiftFactor
* Penalty for frame shift in unaligned fitting.
* Default: -2.0
*
*  ORFHexamerFactor
* Factor to multiply ORFHexamerModifier by in unaligned fitting.
* Default: 0.2
*
*  NonORFHexamerFactor
* Factor to multiply NonORFHexamerFactor by in unaligned fitting.
* Default: -0.1
*
*  DNAFile
* The file to read DNA from.
* Default: Directory name + .txt
*
*  Strand
* Primary or Secondary
* Default: Primary
*
*  ChainStart
* Start in DNA
* Default: 1
*
*  ChainEnd
* End in DNA
* Default: 200000
*</PRE>
*/
public class XonID extends ORFID {

/**
* A Xon generated as a part of the snake.
*/
	private class SnakeXon extends Xon {
	
/**
* Constructor.
* @param	firstIndex	Index of first element in snake to use.
* @param	lastIndex		Index of last element in snake to use.
* @param	par	An ORFID.ParameterBlock.
*/
		SnakeXon(int firstIndex, int lastIndex, ORFID.ParameterBlock par) throws RetroTectorException {
			super(par);
			acidPositions = new int[lastIndex - firstIndex + 1];
			for (int i=0; i<acidPositions.length; i++) {
				try {
					acidPositions[i] = snake[i + firstIndex].POSIT;
				} catch (ArrayIndexOutOfBoundsException e) {
					throw e;
				}
			}
			positionsToPathStack();
			finishSequence(-Integer.MAX_VALUE, Integer.MAX_VALUE);
			estimatedFirst = path[0].FROMDNA;
			estimatedLast = path[path.length - 1].TODNA - 1;
		} // end of SnakeXon.constructor(int, int, ORFID.ParameterBlock)
				
	} // end of SnakeXon
	
	
/**
* snake consists of these
*/
	private class SnakeElement {
	
		final int POSIT; // position in DNA
		final float SCORE;
		final boolean ISSHIFT;
		final boolean ISSTOP;
		float meanErrScore; // average nr of faults over +- HALFMINLENGTH
		float scoreDiff; // score difference over +- HALFMINLENGTH
		
/**
* Constructor.
* @param	po		Position in DNA
* @param	sc		Score
* @param	shift	Shift here
*/
		SnakeElement(int po, float sc, boolean shift) {
			POSIT = po;
			SCORE = sc;
			ISSHIFT = shift;
			ISSTOP = (dna.getAcid(po) == 'z');
			meanErrScore = 0;
		} // end of SnakeElement.constructor(int, float, boolean)
		
	} // end of SnakeElement


/**
* Key for min Chain score = "MinChainScore".
*/
	public static final String MINCHAINSCOREKEY = "MinChainScore";
	
/**
* Key for min 5'LTR-Gag gap = "LTRGagMinGap".
*/

	public static final String LTRGAGGAPKEY = "LTRGagMinGap";
	
/**
* Key for min Gag-Pro gap = "GagProMinGap".
*/

	public static final String GAGPROGAPKEY = "GagProMinGap";
	
/**
* Key for min Pro-Pol gap = "ProPolMinGap".
*/

	public static final String PROPOLGAPKEY = "ProPolMinGap";
	
/**
* Key for min Pol-Env gap = "PolEnvMinGap".
*/

	public static final String POLENVGAPKEY = "PolEnvMinGap";
	
/**
* Key for min Env-3'LTR gap = "EnvLTRMinGap".
*/

	public static final String ENVLTRGAPKEY = "EnvLTRMinGap";

/**
* Key for Max of stops + 5*shifts per 51 acids = "FaultsPer51".
*/
	public static final String FAULTSPER51KEY = "FaultsPer51";

/**
* Key for Tolerance parameter for Xon generation = "SnakeThreshold".
*/
	public static final String SNAKETHRESHOLDKEY = "SnakeThreshold";
	
/**
* Key for name of SelectedChains file = "ChainsFile".
*/
	public static final String CHAINSFILEKEY = "ChainsFile";
	
/**
* Penalty for using same DNA position as a Putein = -10.0f.
*/
	public static final float PUTEINCRASHPENALTY = -10.0f;
	
/**
* Penalty for using same DNA position as a XXon = -10.0f.
*/
	public static final float XXONCRASHPENALTY = -10.0f;

/**
* 25.
*/
	public static final int HALFMINLENGTH = 25;
	
/**
* Xon through entire Chain.
*/
	private SnakeElement[ ] snake = null;
	private float snakeThreshold = 0.20f;
	private float faultThreshold = 51.0f / 12.0f;

	private int firstInDNA;
	private int lastInDNA;
	
	private float[ ] nonAlignedScores;
	
	private int minChainScore = 1000;
	private int minLTRGagGap = 100;
	private int minGagProGap = 100;
	private int minProPolGap = 100;
	private int minPolEnvGap = 100;
	private int minEnvLTRGap = 100;
	
	private MotifHitGraphInfo ltr5 = null;
	private Putein gag = null;
	private Putein pro = null;
	private Putein pol = null;
	private Putein env = null;
	private MotifHitGraphInfo ltr3 = null;
	
	private int generation = -1;
	
	private SpliceDonorMotif spliceDonorMotif;
	private Object[ ] donorhits;
	private Object notdone = new Object();
	
/**
* Standard Executor constructor.
*/
	public XonID() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();

		parameters.put(INPUTFILEKEY, "");
		explanations.put(INPUTFILEKEY, "RetroVID output file with parameters for XonID");
		orderedkeys.push(INPUTFILEKEY);
		parameters.put(MINCHAINSCOREKEY, "1000");
		explanations.put(MINCHAINSCOREKEY, "If the parent Chain has lower score than this, nothing is done.");
		orderedkeys.push(MINCHAINSCOREKEY);
		parameters.put(LTRGAGGAPKEY, "100");
		explanations.put(LTRGAGGAPKEY, "If the gap between 5'LTR and Gag is at least this much, XonID acts.");
		orderedkeys.push(LTRGAGGAPKEY);
		parameters.put(GAGPROGAPKEY, "100");
		explanations.put(GAGPROGAPKEY, "If the gap between Gag and Pro is at least this much, XonID acts.");
		orderedkeys.push(GAGPROGAPKEY);
		parameters.put(PROPOLGAPKEY, "100");
		explanations.put(PROPOLGAPKEY, "If the gap between Pro and Pol and Gag is at least this much, XonID acts.");
		orderedkeys.push(PROPOLGAPKEY);
		parameters.put(POLENVGAPKEY, "100");
		explanations.put(POLENVGAPKEY, "If the gap between Pol and Env is at least this much, XonID acts.");
		orderedkeys.push(POLENVGAPKEY);
		parameters.put(ENVLTRGAPKEY, "100");
		explanations.put(ENVLTRGAPKEY, "If the gap between Env and 3'LTR is at least this much, XonID acts.");
		orderedkeys.push(ENVLTRGAPKEY);
		parameters.put(DATABASEKEY, ORDINARYDATABASE);
		explanations.put(DATABASEKEY, "The relevant database subdirectory");
		orderedkeys.push(DATABASEKEY);
		parameters.put(CHAINNUMBERKEY, "0");
		explanations.put(CHAINNUMBERKEY, "Chain used");
		orderedkeys.push(CHAINNUMBERKEY);
		parameters.put(FAULTSPER51KEY, "12.0");
		explanations.put(FAULTSPER51KEY, "Max of stops + 5* shifts per 51 acids");
		orderedkeys.push(FAULTSPER51KEY);
		parameters.put(SNAKETHRESHOLDKEY, "0.20");
		explanations.put(SNAKETHRESHOLDKEY, "");
		orderedkeys.push(SNAKETHRESHOLDKEY);
		parameters.put(CHAINSFILEKEY, "001SelectedChains.txt");
		explanations.put(CHAINSFILEKEY, "Name of SelectedChains file to use");
		orderedkeys.push(CHAINSFILEKEY);

		parameters.put(NONALIGNEDSCOREKEY, NONALIGNEDDEFAULT);
		explanations.put(NONALIGNEDSCOREKEY, "Basic score assigned to non-aligned step");
		orderedkeys.push(NONALIGNEDSCOREKEY);
		parameters.put(STOPCODONFACTORKEY, STOPCODONDEFAULT);
		explanations.put(STOPCODONFACTORKEY, "Weight assigned to StopCodonModifier");
		orderedkeys.push(STOPCODONFACTORKEY);
		parameters.put(FRAMESHIFTPENALTYKEY, "-2.0");
		explanations.put(FRAMESHIFTPENALTYKEY, "Weight assigned to frame shift");
		orderedkeys.push(FRAMESHIFTPENALTYKEY);
		parameters.put(ORFHEXAMERFACTORKEY, ORFHEXAMERDEFAULT);
		explanations.put(ORFHEXAMERFACTORKEY, "Weight assigned to ORFHexamerModifier");
		orderedkeys.push(ORFHEXAMERFACTORKEY);
		parameters.put(NONORFHEXAMERFACTORKEY, NONORFHEXAMERDEFAULT);
		explanations.put(NONORFHEXAMERFACTORKEY, "Weight assigned to NonORFHexamerModifier");
		orderedkeys.push(NONORFHEXAMERFACTORKEY);
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(STRANDKEY, PRIMARY);
		explanations.put(STRANDKEY, PRIMARY + " or " + SECONDARY);
		orderedkeys.push(STRANDKEY);
		parameters.put(CHAINSTARTKEY, "1");
		explanations.put(CHAINSTARTKEY, "Start in DNA");
		orderedkeys.push(CHAINSTARTKEY);
		parameters.put(CHAINENDKEY, "200000");
		explanations.put(CHAINENDKEY, "End in DNA");
		orderedkeys.push(CHAINENDKEY);
	} // end of constructor()
	
// constructs a Xon between accp and donp, or null
	private final Xon makeXXon(int accp, int donp) throws RetroTectorException {
		Xon result = null;
		try {
			result = new Xon(accp, donp, new ParameterBlock(this), faultThreshold);
		} catch (RetroTectorException e) {
			return null;
		}
		int ppp;
//update nonAlignedScores
		for (int i=0; i<result.acidPositions.length; i++) {
			ppp = result.acidPositions[i] - firstInDNA;
			if ((ppp >= 0) & (ppp < nonAlignedScores.length)) {
				nonAlignedScores[ppp] += XXONCRASHPENALTY;
			}
		}
		return result;
	} // end of makeXXon(int, int)
	
	
	private final boolean makeSnake() {
		float[ ] snakeMatrix = new float[lastInDNA - firstInDNA + 1]; 
		float temps;
		float temps2;
		int templ;
		int wi = snakeMatrix.length;

// build matrix
		snakeMatrix[wi - 1] = 0;
		for (int d=wi-2; d>=0; d--) {
			temps = snakeMatrix[d + 1] + nonAlignedScores[d] + frameShiftPenalty;
			if (d < (wi-2)) { // then try 2 base step
				temps2 = snakeMatrix[d + 2] + nonAlignedScores[d] + frameShiftPenalty;
				if (temps2 > temps) {
					temps = temps2;
				}
			}
			if (d < (wi-3)) { // then try codon step
				temps2 = snakeMatrix[d + 3] + nonAlignedScores[d];
				if (temps2 > temps) {
					temps = temps2;
				}
			}
			snakeMatrix[d] = temps;
		}
		
		boolean shi = false;
		Stack st = new Stack();
		int ds = 0;
		while (ds < wi - 3) {
			if (snakeMatrix[ds] == snakeMatrix[ds + 3] + nonAlignedScores[ds]) {
				st.push(new SnakeElement(ds + firstInDNA, snakeMatrix[ds], shi));
				ds += 3;
				shi = false;
			} else if (snakeMatrix[ds] == snakeMatrix[ds + 2] + nonAlignedScores[ds] + frameShiftPenalty) {
				ds += 2;
				shi = true;
			} else {
				ds++;
				shi = true;
			}
		}
		if (ds == wi - 3) {
			st.push(new SnakeElement(ds + firstInDNA, snakeMatrix[ds], shi));
		}
		snake = new SnakeElement[st.size()];
		st.copyInto(snake);
		
		float errsum;
		for (int b=HALFMINLENGTH; b<snake.length-HALFMINLENGTH; b++) {
			errsum = 0;
			for (int bb=b - HALFMINLENGTH; bb<=b + HALFMINLENGTH; bb++) {
				if (snake[bb].ISSTOP) {
					errsum += 1;
				}
				if (snake[bb].ISSHIFT) {
					errsum += 5;
				}
			}
			snake[b].meanErrScore = errsum / (HALFMINLENGTH * 2 + 1);
			snake[b].scoreDiff = snake[b - HALFMINLENGTH].SCORE - snake[b + HALFMINLENGTH].SCORE;
		}
		
		return true;
	} // end of makeSnake()
	

// make an Xon from snake, or null
	private final Xon oneMoreXon() throws RetroTectorException {
		makeSnake();
// find best scoreDiff
		int maxindex = -1;
		float maxscore = -Float.MAX_VALUE;
		for (int b=HALFMINLENGTH; b<snake.length-HALFMINLENGTH; b++) {
			if (snake[b].scoreDiff > maxscore) {
				maxscore = snake[b].scoreDiff;
				maxindex = b;
			}
		}
// good enough?
		if (snake[maxindex].meanErrScore > snakeThreshold) {
			return null;
		}

// extend downwards
		int b1;
		for (b1=maxindex; (b1>=HALFMINLENGTH) && (snake[b1].meanErrScore <= snakeThreshold); b1--) {
		}
		b1++;
// extend upwards
		int b2;
		for (b2=maxindex; (b2<snake.length-HALFMINLENGTH) && (snake[b2].meanErrScore <= snakeThreshold); b2++) {
		}
		b2--;
		SnakeXon result = new SnakeXon(b1 - HALFMINLENGTH, b2 + HALFMINLENGTH, new ParameterBlock(this));
		int ppp;
// update nonAlignedScores
		for (int i=0; i<result.acidPositions.length; i++) {
			ppp = result.acidPositions[i] - firstInDNA;
			if ((ppp >= 0) & (ppp < nonAlignedScores.length)) {
				nonAlignedScores[ppp] += XXONCRASHPENALTY;
			}
		}
		RetroTectorEngine.setInfoField("Xon made at " + dna.externalize(result.estimatedFirst));
		return result;
	} // end of oneMoreXon()
	
// update nonAlignedScores with respect to a Putein
	private final int[ ] extractPositions(File f) throws RetroTectorException {
		Putein put = new Putein(f, new ORFID.ParameterBlock(this));
		if (put.GENE.equalsIgnoreCase("Gag")) {
			gag = put;
		}
		if (put.GENE.equalsIgnoreCase("Pro")) {
			pro = put;
		}
		if (put.GENE.equalsIgnoreCase("Pol")) {
			pol = put;
		}
		if (put.GENE.equalsIgnoreCase("Env")) {
			env = put;
		}
		int[ ] result = put.getAcidPositions();
		int ppp;
		for (int i=0; i<result.length; i++) {
			ppp = result[i] - firstInDNA;
			if ((ppp >= 0) & (ppp < nonAlignedScores.length)) {
				nonAlignedScores[ppp] += PUTEINCRASHPENALTY;
			}
		}
		return result;
	} // end of extractPositions(File)
	
	private final MotifHit getDonorHit(int pos) throws RetroTectorException {
		if ((pos < firstInDNA) | (pos > lastInDNA)) {
			return null;
		}
		if (donorhits[pos - firstInDNA] == notdone) {
			donorhits[pos - firstInDNA] = spliceDonorMotif.getMotifHitAt(pos);
		}
		return (MotifHit) donorhits[pos - firstInDNA];
	} // end of getDonorHit(int)
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 17";
  } // end of version()
	
/**
* Execute as specified above.
*/
	public String execute() throws RetroTectorException {
	
		minChainScore = getInt(MINCHAINSCOREKEY, 1000);
		minLTRGagGap = getInt(LTRGAGGAPKEY, 100);
		minGagProGap = getInt(GAGPROGAPKEY, 100);
		minProPolGap = getInt(PROPOLGAPKEY, 100);
		minPolEnvGap = getInt(POLENVGAPKEY, 100);
		minEnvLTRGap = getInt(ENVLTRGAPKEY, 100);
		
		File ful = new File(getString(SCRIPTPATHKEY, ""));
		generation = FileNamer.getXonIDScriptGeneration(ful.getName());

// Check for unswept ORFID scripts
		String[ ] ss = RetroTectorEngine.currentDirectory().list();
		for (int i=0; i<ss.length; i++) {
			if (FileNamer.isORFIDScript(ss[i]) && (FileNamer.getORFIDScriptGeneration(ss[i]) == generation)	&& ss[i].endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX)) {
				haltError(ORFIDSCRIPTSLEFTMESSAGE, ss[i]);
			}
		}

		String dnaName = getString(DNAFILEKEY, "");
		dna = getDNA(dnaName, !getString(STRANDKEY, PRIMARY).equals(SECONDARY));
    database = RetroTectorEngine.getCurrentDatabase();
		char strandC = getString(STRANDKEY, PRIMARY).charAt(0);
		int chainNr = getInt(CHAINNUMBERKEY, 0);
		File chainsFile = FileNamer.getSelectedChainsFile("", generation);
		String fns = getString(CHAINSFILEKEY, null);
		if (fns != null) {
			chainsFile = new File(RetroTectorEngine.currentDirectory(), fns);
		}
		Hashtable ht = new Hashtable();
		ParameterFileReader reader = new ParameterFileReader(chainsFile, ht);
		reader.readParameters();
		reader.close();
		String[ ] sss = (String[ ]) ht.get("Chain" + strandC + chainNr);
		ChainGraphInfo info = new ChainGraphInfo(sss, dna);
		if (info.SCORE < minChainScore) {
			return "";
		}
		if (info.subgeneinfo[0].SUBGENENAME.equals(Executor.LTR5KEY)) {
			ltr5 = info.subgeneinfo[0].motifhitinfo[0];
		}
		if (info.subgeneinfo[info.subgeneinfo.length - 1].SUBGENENAME.equals(Executor.LTR3KEY)) {
			ltr3 = info.subgeneinfo[info.subgeneinfo.length - 1].motifhitinfo[0];
		}
		
		firstInDNA = dna.internalize(getInt(CHAINSTARTKEY, 0));
		lastInDNA = dna.internalize(getInt(CHAINENDKEY, 200000));
		if (lastInDNA <= firstInDNA) {
			RetroTectorEngine.displayError(new RetroTectorException("XonID", "lastInDNA <= firstInDNA"), RetroTectorEngine.WARNINGLEVEL);
		}
		nonAlignedScores = new float[lastInDNA - firstInDNA + 1];

		snakeThreshold = getFloat(SNAKETHRESHOLDKEY, 0.20f);
		faultThreshold = (2 * HALFMINLENGTH + 1) / getFloat(FAULTSPER51KEY, 12.0f);
		
    nonAlignedScore = getFloat(ORFID.NONALIGNEDSCOREKEY, 0.4f);
    stopCodonFactor = getFloat(ORFID.STOPCODONFACTORKEY, -0.4f);
    frameShiftPenalty = getFloat(ORFID.FRAMESHIFTPENALTYKEY, -2.0f);
    orfHexamerFactor = getFloat(ORFID.ORFHEXAMERFACTORKEY, 0.2f);
    nonOrfHexamerFactor = getFloat(ORFID.NONORFHEXAMERFACTORKEY, -0.1f);
		
// build nonAlignedScores
    StopCodonModifier stopMod = dna.getStopCodonModifier();
    ORFHexamerModifier orfMod = dna.getORFHexamerModifier();
    NonORFHexamerModifier nonOrfMod = dna.getNonORFHexamerModifier();
    for (int m=0; m<nonAlignedScores.length; m++) {
      nonAlignedScores[m] += (nonAlignedScore +
          stopMod.stopCodonModifierAt(firstInDNA + m, firstInDNA, lastInDNA) * stopCodonFactor +
          orfMod.modification(firstInDNA + m) * orfHexamerFactor +
          nonOrfMod.modification(firstInDNA + m) * nonOrfHexamerFactor);
    }
		
		String scriptFileName = (new File(getString(SCRIPTPATHKEY, ""))).getName();
		
// At present, whole chain is searched for stop codons
		int firstStopPos = 0;
// search puteins for forbidden positions
		Stack stack = new Stack();
		String[ ] fnames = RetroTectorEngine.currentDirectory().list();
		File f;
		for (int fn=0; fn<fnames.length; fn++) {
			if (FileNamer.isPuteinFileFromChain(fnames[fn], FileNamer.getLongChainnameFromXonIDScript(scriptFileName))) {
				f = new File(RetroTectorEngine.currentDirectory(), fnames[fn]);
				if (f.exists()) {
					stack.push(extractPositions(f));
				}
			}
		}
		boolean doit = false;
		if ((ltr5 != null) && (gag != null) && ((gag.estimatedFirst - ltr5.LASTPOS) >= minLTRGagGap)) {
			doit = true;
		}
		if ((gag != null) && (pro != null) && ((pro.estimatedFirst - gag.estimatedLast) >= minGagProGap)) {
			doit = true;
		}
		if ((pro != null) && (pol != null) && ((pol.estimatedFirst - pro.estimatedLast) >= minProPolGap)) {
			doit = true;
		}
		if ((pol != null) && (env != null) && ((env.estimatedFirst - pol.estimatedLast) >= minPolEnvGap)) {
			doit = true;
		}
		if ((env != null) && (ltr3 != null) && ((ltr3.FIRSTPOS - env.estimatedLast) >= minEnvLTRGap)) {
			doit = true;
		}
		if (!doit) {
			return "";
		}
		
		
		avoidPositions = new int[stack.size()][ ];
		stack.copyInto(avoidPositions);

		SpliceAcceptorMotif spliceAcceptorMotif = (SpliceAcceptorMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SPLICEACCEPTORMOTIFKEY);
		spliceAcceptorMotif.refresh(new Motif.RefreshInfo(dna, 0, 0, null));
		spliceDonorMotif = (SpliceDonorMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SPLICEDONORMOTIFKEY);
		spliceDonorMotif.refresh(new Motif.RefreshInfo(dna, 0, 0, null));
		donorhits = new Object[lastInDNA + 1 - firstInDNA];
		Arrays.fill(donorhits, notdone);
		
		Xon xon;
		ExecutorScriptWriter sWriter;
		
// make XXons
		Stack xxonStack = new Stack();
		for (int accPos=firstInDNA; accPos<=lastInDNA; accPos++) {
			if (spliceAcceptorMotif.getMotifHitAt(accPos) != null) {
				RetroTectorEngine.setInfoField("Splice acceptor at " + dna.externalize(accPos));
				boolean hit = false;
// search for XXons starting at acceptor
				for (int donPos=Math.min(dna.forceInside(accPos + 3000), lastInDNA); !hit && (donPos>=dna.forceInside(accPos + 150)); donPos--) {
					if (getDonorHit(donPos) != null) {
						hit = (xon = makeXXon(accPos, donPos)) != null;
						if (hit) {
							xxonStack.push(xon);
						}
					} else if ((donPos > firstStopPos) && (dna.getAcid(donPos) == 'z')) {
						hit = (xon = makeXXon(accPos, donPos)) != null;
						if (hit) {
							xxonStack.push(xon);
						}
					}
				}
// search for XXons starting at methionine soon after acceptor
				if (!hit) {
					for (int mpos=accPos+1; (mpos<=accPos+200) & (mpos<dna.LENGTH-2); mpos++) {
						if (dna.getAcid(mpos) == 'm') {
							for (int donPos=Math.min(dna.forceInside(mpos + 3000), lastInDNA); !hit && (donPos>=dna.forceInside(mpos + 150)); donPos--) {
								if (getDonorHit(donPos) != null) {
									hit = (xon = makeXXon(mpos, donPos)) != null;
									if (hit) {
										xxonStack.push(xon);
									}
								} else if ((donPos > firstStopPos) && (dna.getAcid(donPos) == 'z')) {
									hit = (xon = makeXXon(mpos, donPos)) != null;
									if (hit) {
										xxonStack.push(xon);
									}
								}
							}
						}
					}
				}
			}
		}
		Xon[ ] xxona = new Xon[xxonStack.size()];
		xxonStack.copyInto(xxona);
		Utilities.sort(xxona);
		boolean found = false;
		do {
			found = false;
			for (int x=0; x<xxona.length; x++) {
				if (xxona[x].eligible) {
					found = true;
					xxona[x].eligible = false;
					sWriter = new ExecutorScriptWriter(FileNamer.createXXonFile(scriptFileName, xxona[x]), "Puteinview");
					sWriter.writeSingleParameter(NROFHITSKEY, "2", false);
					xxona[x].toFile(sWriter);
					writeInfo(sWriter);
					sWriter.close();
					for (int x2=x+1; x2<xxona.length; x2++) {
						if (xxona[x2].eligible && (Utilities.puteinPositionsCompare(xxona[x2].getAcidPositions(), xxona[x].getAcidPositions()) > 0)) {
							xxona[x2].eligible = false;
						}
					}
				}
			}
		} while (found);
		
// make Xons
		Xon lastxon = null;
		for (int xoncount=0; xoncount<=100; xoncount++) {
			xon = oneMoreXon();
			if (xon == null) {
				return "";
			}
// avoid rare infinite loop
			if ((lastxon != null) && lastxon.mainLines[AcidSequence.PUTEINLINEINDEX].equals(xon.mainLines[AcidSequence.PUTEINLINEINDEX])) {
				return "";
			}
			lastxon = xon;
			sWriter = new ExecutorScriptWriter(FileNamer.createXonFile(scriptFileName, xon), "Puteinview");
			sWriter.writeSingleParameter(NROFHITSKEY, "2", false);
			xon.toFile(sWriter);
			writeInfo(sWriter);
			sWriter.close();
		}
		
		return "";
	} // end of execute()
	
} // end of XonID
