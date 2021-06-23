/*
* Copyright ((©)) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/11 -06
* Beautified 12/11 -06
*/
package builtins;

import retrotector.*;

import retrotectorcore.*;
import java.util.*;
import java.io.*;

/**
* Executor which searches for possible LTR candidates.
*
* Each candidate is assigned a score factor (max 0.5), depending on a number of
* factors, whose weights may be adjusted. Possible candidates
* may also be created with similarity to another candidate as criterion.
* If the score factor exceeds a threshold, and reasonable integration sites are
* found, a single LTR candidate is registered.
* If two possible candidates a reasonable distance apart are sufficiently
* similar, an LTR pair candidate is registered.
* Output is in the form of a RetroVID script describing LTR pair and single
* LTR candidates, and optionally of all possible candidates.
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
* Database subdirectory to use.
* Default: Ordinary.
*
*   ExponentStrength
* The higher this value, the more sensitive is the score factor to
* deviations from perfection.
* Default: 2.
*
*   LTRepTolerance
* A measure of the tolerance for dissimilarities between LTRs in a pair.
* See 'roof' parameter in LTRPair.
* Default: 7.
*
*   LINELTRTolerance
* Effectively not in use at present.
* A measure of the tolerance for dissimilarities between LTR and LINE.
* See 'roof' parameter in LTRPair.
* If negative, no checking against LINE is done.
* Default: 10.
* 
*   AATAAAWeight
* Relative weight of AATAAA (AGTAAA, ATTAAA) in scoring.
* Default: 1.
*
*   U5NetWeight
* Relative weight of U5NN.txt neural network in scoring.
* Default: 2.
*
*   GTWeight
* Relative weight of GTModifier in scoring.
* Default: 1.
*
*   U3NetWeight
* Relative weight of U3NN.txt neural network in scoring.
* Default: 1.
*
*   TATAAWeight
* Relative weight of TATAA box in scoring.
* Default: 1.
*
*   MEME50Weight
* Relative weight fpr MEME50 weight matrix in scoring.
* Default: 1
*
*   Motifs1Weight
* Relative weight of LTR1Motifs in Motifs.txt in scoring.
* Default: 4.
*
*   Motifs2Weight
* Relative weight of LTR2Motifs in Motifs.txt in scoring.
* Default: 4.
*
*   TransSitesWeight
* Relative weight of TransSitesModifier in scoring.
* Default: 1.
*
*   CpGWeight
* Relative weight of CpGModifier in scoring.
* Default: 1.
*
*   SplitOctamerWeight
* Relative weight of SplitOctamerModifier in scoring.
* Default: 1.
*
*   SingleLTRThreshold
* Lowest acceptable score factor in single LTR candidates.
* Default: 0.18.
*
*   DoSingleLTRs
* If Yes, single LTR candidates are to be output.
* Default: Yes.
*
*   MaxPairsOutput
* No more LTR pairs than this are output per strand.
* Default: 50.
*
*   Debugging
* If Yes, extra information, in particular all preliminary candidates, is output.
* Default: No.
*</PRE>
*/
public class LTRID extends Executor {

/**
* Set of acceptable distances from a particular feature.
*/
  private class DistanceSet {
  
    private final DistanceRange DISTTOSTART;
    private final DistanceRange DISTTOHOTSPOT;
    private final DistanceRange DISTTOEND;
    
/**
* @param	line	A line consisting of three distance specifiers.
*/
    private DistanceSet(String line) throws RetroTectorException {
      String[ ] ll = Utilities.splitString(line);
      DISTTOSTART = new DistanceRange(ll[0]);
      DISTTOHOTSPOT = new DistanceRange(ll[1]);
      DISTTOEND = new DistanceRange(ll[2]);
    } // end of DistanceSet.constructor(String)
    
  } // end of DistanceSet

  
/**
* Class describing a feature of an LTR possible.
*/
  private class InternalHit implements Scorable {
  
    private final float IHSCORE;
    private final Range IHSTARTRANGE;
    private final Range IHENDRANGE;
    
/**
* Constructor.
* @param	score	Its internal score.
* @param	stR		Its acceptable distance to LTR start.
* @param	enR		Its acceptable distance to LTR end.
*/
    private InternalHit(float score, Range stR, Range enR) {
      IHSCORE = score;
      IHSTARTRANGE = stR;
      IHENDRANGE = enR;
    } // end of InternalHit.constructor(float, Range, Range)
    
/**
* As required by Scorable.
*/
    public float fetchScore() {
      return IHSCORE;
    } // end of InternalHit.fetchScore()

  } // end of InternalHit
	
  
/**
* Name of file describing LTR constraints = "LTRIDData.txt".
*/
	public static final String LTRIDDATAFILENAME = "LTRIDData.txt";

/**
* Key for value of exponent in score calculation = "ExponentStrength".
*/
	public static final String EXPSTRENGTHKEY = "ExponentStrength";

/**
* Key for tolerance in LINE exclusion = "LINELTRTolerance".
*/
 	public static final String LINELTRTOLERANCEKEY = "LINELTRTolerance";
	
/**
* Key for relative weight of AATAAA (AGTAAA, ATTAAA) in scoring = "AATAAAWeight".
*/
  public static final String AATAAAWEIGHTKEY = "AATAAAWeight";

/**
* Key for relative weight of U5NN.txt neural network in scoring = "U5NetWeight".
*/
  public static final String U5NETWEIGHTKEY = "U5NetWeight";

/**
* Key for relative weight of GTModifier in scoring = "GTWeight".
*/
  public static final String GTWEIGHTKEY = "GTWeight";

/**
* Key for relative weight of U3NN.txt neural network in scoring = "U3NetWeight".
*/
  public static final String U3NETWEIGHTKEY = "U3NetWeight";

/**
* Key for relative weight of TATAA box in scoring = "TATAAWeight".
*/
  public static final String TATAAWEIGHTKEY = "TATAAWeight";

/**
* Key for relative weight of MEME50 matrix in scoring = "MEME50Weight".
*/
  public static final String MEME50WEIGHTKEY = "MEME50Weight";

/**
* Key for relative weight of LTR1Motifs in Motifs.txt in scoring = "Motifs1Weight".
*/
  public static final String MOTIFS1WEIGHTKEY = "Motifs1Weight";

/**
* Key for relative weight of LTR2Motifs in Motifs.txt in scoring = "Motifs2Weight".
*/
  public static final String MOTIFS2WEIGHTKEY = "Motifs2Weight";

/**
* Key for relative weight of TransSiteModifier in scoring = "TransSitesWeight".
*/
  public static final String TRANSWEIGHTKEY = "TransSitesWeight";

/**
* Key for relative weight of CpGModifier in scoring = "CpGWeight".
*/
  public static final String CPGWEIGHTKEY = "CpGWeight";

/**
* Key for relative weight of SplitOctamerModifier in scoring = "SplitOctamerWeight".
*/
  public static final String SPLIT8WEIGHTKEY = "SplitOctamerWeight";

/**
* Key for lowest acceptable score factor in single LTR candidates = "SingleLTRThreshold".
*/
  public static final String SINGLETHRESHOLDKEY = "SingleLTRThreshold";
  
/**
* Key for specification that single LTR candidates are to be output.
*/
	public static final String DOSINGLESKEY = "DoSingleLTRs";
  
/**
* Key for maximum number of LTR pairs output.
*/
	public static final String MAXPAIRSOUTPUTSKEY = "MaxPairsOutput";
	
/**
* Key for parameter defining TATAA box weight matrix = "TATAA".
*/
  public static final String TATAAMATRIXKEY = "TATAA";
  
/**
* Key for parameter defining MEME50 weight matrix = "MEME50".
*/
  public static final String MEME50MATRIXKEY = "MEME50";
	
/**
* Key for secret parameter deciding extension into pads = "ExtendIntoPads".
*/
	public static final String EXTENDINTOPADSKEY = "ExtendIntoPads";
  
/**
* Assumed smallest distance between hotspots of paired LTRs = 2900.
*/
  public static final int MINLTRPAIRSPAN = 2900;
  
/**
* Assumed largest distance between hotspots of paired LTRs = 14000.
*/
  public static final int MAXLTRPAIRSPAN = 14000;

/**
* Counter for single LTRs during one RetroTector session.
*/
	public static int singlesCount = 0;
	
/**
* Histogram of candidateFactor of singe LTRs. Not in use at present.
*/
	public static int[ ] singlesHistogram = new int[101];
	
/**
* Histogram of candidateFactor of LTR pairs made. Not in use at present.
*/
	public static int[ ] pairsMadeHistogram = new int[101];
	
/**
* Histogram of candidateFactor of LTR pairs output. Not in use at present.
*/
	public static int[ ] pairsOutputHistogram = new int[101];
	

 /**
* Constructor. Specifies obligatory parameters.
*/
	public LTRID() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(INPUTFILEKEY, "");
		explanations.put(INPUTFILEKEY, "SweepDNA output file with parameters for LTRID");
		orderedkeys.push(INPUTFILEKEY);

		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(DATABASEKEY, ORDINARYDATABASE);
		explanations.put(DATABASEKEY, "The relevant database");
		orderedkeys.push(DATABASEKEY);
		parameters.put(EXPSTRENGTHKEY, "2.0");
		explanations.put(EXPSTRENGTHKEY, "The factor to multiply exponent with in final calculation");
		orderedkeys.push(EXPSTRENGTHKEY);
		parameters.put(LTREPTOLERANCEKEY, "7.0");
		explanations.put(LTREPTOLERANCEKEY, "Indicates tolerance for differences between LTRs");
		orderedkeys.push(LTREPTOLERANCEKEY);
		parameters.put(LINELTRTOLERANCEKEY, "10.0");
		explanations.put(LINELTRTOLERANCEKEY, "Indicates tolerance for LINE similarity");
		orderedkeys.push(LINELTRTOLERANCEKEY);
		parameters.put(AATAAAWEIGHTKEY, "1.0");
		explanations.put(AATAAAWEIGHTKEY, "The weight for AATAAA, ATTAAA or AGTAAA");
		orderedkeys.push(AATAAAWEIGHTKEY);
		parameters.put(U5NETWEIGHTKEY, "2.0");
		explanations.put(U5NETWEIGHTKEY, "The weight for the U5 neural net");
		orderedkeys.push(U5NETWEIGHTKEY);
		parameters.put(GTWEIGHTKEY, "1.0");
		explanations.put(GTWEIGHTKEY, "The weight for GT accumulation");
		orderedkeys.push(GTWEIGHTKEY);
		parameters.put(U3NETWEIGHTKEY, "1.0");
		explanations.put(U3NETWEIGHTKEY, "The weight for the U3 neural net");
		orderedkeys.push(U3NETWEIGHTKEY);
		parameters.put(TATAAWEIGHTKEY, "1.0");
		explanations.put(TATAAWEIGHTKEY, "The weight for the TATA box");
		orderedkeys.push(TATAAWEIGHTKEY);
		parameters.put(MEME50WEIGHTKEY, "1.0");
		explanations.put(MEME50WEIGHTKEY, "The weight for MEME50");
		orderedkeys.push(MEME50WEIGHTKEY);
		parameters.put(MOTIFS1WEIGHTKEY, "4.0");
		explanations.put(MOTIFS1WEIGHTKEY, "The weight for group 1 base motifs");
		orderedkeys.push(MOTIFS1WEIGHTKEY);
		parameters.put(MOTIFS2WEIGHTKEY, "4.0");
		explanations.put(MOTIFS2WEIGHTKEY, "The weight for group 2 base motifs");
		orderedkeys.push(MOTIFS2WEIGHTKEY);
		parameters.put(TRANSWEIGHTKEY, "1.0");
		explanations.put(TRANSWEIGHTKEY, "The weight for transcription factor binding sites");
		orderedkeys.push(TRANSWEIGHTKEY);
		parameters.put(CPGWEIGHTKEY, "1.0");
		explanations.put(CPGWEIGHTKEY, "The weight for CpG");
		orderedkeys.push(CPGWEIGHTKEY);
		parameters.put(SPLIT8WEIGHTKEY, "1.0");
		explanations.put(SPLIT8WEIGHTKEY, "The weight for split octamers");
		orderedkeys.push(SPLIT8WEIGHTKEY);
		parameters.put(SINGLETHRESHOLDKEY, "0.18");
		explanations.put(SINGLETHRESHOLDKEY, "Minimum score factor for single LTR");
		orderedkeys.push(SINGLETHRESHOLDKEY);
    parameters.put(DOSINGLESKEY, YES);
		explanations.put(DOSINGLESKEY, NO + " or " + YES);
		orderedkeys.push(DOSINGLESKEY);
    parameters.put(MAXPAIRSOUTPUTSKEY, "50");
		explanations.put(MAXPAIRSOUTPUTSKEY, "No more LTR pairs than this are output per strand");
		orderedkeys.push(MAXPAIRSOUTPUTSKEY);
		parameters.put(DEBUGGINGKEY, NO);
		explanations.put(DEBUGGINGKEY, NO + " or " + YES);
		orderedkeys.push(DEBUGGINGKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 12";
  } //end of version()
  
/**
* Execute as specified above.
*/
	public final String execute() throws RetroTectorException {
		
		long starttime = System.currentTimeMillis(); // Set up for time counting
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
		
		makePreparations();
		
		if (!setDNA(getDNA(getString(DNAFILEKEY, ""), true), "P")) {
			return "setDNA failed";
		}
    if (debugging) {
      System.out.println("Starting primary strand");
    }
    doStrand("P");
    if (debugging) {
      System.out.println("Primary strand done");
    }
		
    if (debugging) { // output all candidates
      getWriter().startMultiParameter(LTRHITSPRIMARYKEY, false);
      for (int i=0; i<ltrcands.length; i++) {
        if (ltrcands[i] != null) {
          getWriter().appendToMultiParameter(ltrcands[i].toString(), false);
        }
      }
      getWriter().finishMultiParameter(false);
    }
    
    
    setDNA(getDNA(getString(DNAFILEKEY, ""), false), "S"); // complement
    if (debugging) {
      System.out.println("Starting secondary strand");
    }
    doStrand("S");
    if (debugging) {
      System.out.println("Secondary strand done");
    }

    if (debugging) {
      getWriter().startMultiParameter(LTRHITSSECONDARYKEY, false);
      for (int i=0; i<ltrcands.length; i++) {
        if (ltrcands[i] != null) {
          getWriter().appendToMultiParameter(ltrcands[i].toString(), false);
        }
      }
      getWriter().finishMultiParameter(false);
    }
 
		writeInfo(getWriter());
		getWriter().writeComment("Execution time was " + (System.currentTimeMillis() - starttime) + " milliseconds");
		getWriter().writeComment("Latecoming LTRCandidates: " + rogueCounter);
		getWriter().close();
		return "";
	} // end of execute()
	
/**
* Does DNA-independent initialization.
*/
	public final void makePreparations() throws RetroTectorException {
    RetroTectorEngine.setCurrentDatabase(getString(DATABASEKEY, Executor.ORDINARYDATABASE));
    database = RetroTectorEngine.getCurrentDatabase();
		ParameterFileReader reader = new ParameterFileReader(database.getFile(LTRIDDATAFILENAME), table);
		reader.readParameters();
		reader.close();
		
		cfactor = Utilities.decodeFloat(retroVID.defaultValueOf(CFACTKEY));
		sdfactor = Utilities.decodeFloat(retroVID.defaultValueOf(SDFACTORKEY));
		
		expStrength = getFloat(EXPSTRENGTHKEY, 2.0f);

		repTolerance = getFloat(LTREPTOLERANCEKEY, 7.0f);
		lineLTRTolerance = getFloat(LINELTRTOLERANCEKEY, 10.0f);

		aATAAAWeight = getFloat(AATAAAWEIGHTKEY, 1.0f);
    aATAAADistances = new DistanceSet((String) table.get("AATAAADistance"));
 
    u3NetWeight = getFloat(U3NETWEIGHTKEY, 1.0f);
    u3Net = new BaseNet(database.getFile("U3NN.txt"));
    u3NetDistances = new DistanceSet((String) table.get("U3NNDistance"));

    u5NetWeight = getFloat(U5NETWEIGHTKEY, 2.0f);
    u5Net = new BaseNet(database.getFile("U5NN.txt"));
    u5NetDistances = new DistanceSet((String) table.get("U5NNDistance"));

		tATAAWeight = getFloat(TATAAWEIGHTKEY, 1.0f);
		tATAAMatrix = new BaseWeightMatrix((String[ ]) table.get(TATAAMATRIXKEY));
    tATAADistances = new DistanceSet((String) table.get("TATAADistance"));
		
		mEME50Weight = getFloat(MEME50WEIGHTKEY, 1.0f);
		mEME50Matrix = new BaseWeightMatrix((String[ ]) table.get(MEME50MATRIXKEY));
    mEME50Distances = new DistanceSet((String) table.get("MEME50Distance"));
		

		motifs1Weight = getFloat(MOTIFS1WEIGHTKEY, 4.0f);
    motifs1Distances = new DistanceSet((String) table.get("Motifs1Distance"));
    motifs1 = database.LTR1MOTIFS;
		
		motifs2Weight = getFloat(MOTIFS2WEIGHTKEY, 4.0f);
    motifs2Distances = new DistanceSet((String) table.get("Motifs2Distance"));
    motifs2 = database.LTR2MOTIFS;
		
    gtWeight = getFloat(GTWEIGHTKEY, 1.0f);

    transWeight = getFloat(TRANSWEIGHTKEY, 1.0f);
    transDistances = new DistanceSet((String) table.get("TranssitesDistance"));
   
    cpgWeight = getFloat(CPGWEIGHTKEY, 1.0f);
    cpgDistances = new DistanceSet((String) table.get("CpGDistance"));
   
    split8Weight = getFloat(SPLIT8WEIGHTKEY, 1.0f);
    split8Distances = new DistanceSet((String) table.get("SplitOctamerDistance"));
    
    singleThreshold = getFloat(SINGLETHRESHOLDKEY, 0.18f);

		debugging = getString(DEBUGGINGKEY, NO).equals(YES);
    outputsingles = getString(DOSINGLESKEY, NO).equals(YES);
		maxPairsOutput = getInt(MAXPAIRSOUTPUTSKEY, 50);
		
		pptMotif = database.getMotifs("Motifs", SubGene.PPT)[0];
		pbsMotifs = database.getMotifs("Motifs", SubGene.PBS);
		
		extendIntoPads = getInt(EXTENDINTOPADSKEY, 10);

	} // end of makePreparations()
	
/**
* Does DNA-dependent initialization.
* @param	dna		The DNA to use.
* @param	strs	'P' or 'S'.
* @return	True if successful, false if dna too short.
*/
	public final boolean setDNA(DNA dna, String strs) throws RetroTectorException {
		return setDNA(dna, 0, dna.LENGTH - 1, strs);
	} // end of setDNA(DNA, String)
	
/**
* Does DNA-dependent initialization.
* @param	dna	The DNA to use.
* @param	first	The first position in dna that will be used.
* @param	last	The last position in dna that will be used.
* @param	strs	'P' or 'S'.
* @return	True if successful, false if dna too short.
*/
	public final boolean setDNA(DNA dna, int first, int last, String strs) throws RetroTectorException {
		theDNA = dna;
		if (theDNA.NCOUNT >= theDNA.LENGTH - 1000) {
			RetroTectorEngine.displayError(new RetroTectorException("LTRID", "The DNA file " + theDNA.NAME, "has less than 1000 valid bases"), RetroTectorEngine.WARNINGLEVEL);
			return false;
		}
    Motif.RefreshInfo rf = new Motif.RefreshInfo(theDNA, 2.0f, 5.0f, null);
    for (int m=0; m<motifs1.length; m++) {
			motifs1[m].refresh(rf);
		}
    for (int m=0; m<motifs2.length; m++) {
			motifs2[m].refresh(rf);
		}

    u3scores = new float[theDNA.LENGTH]; // for results of U3NN
    u5scores = new float[theDNA.LENGTH]; // for resukts of U5NN
    showInfo("Running NNs in " + strs + " strand");
    showProgress();
		u5Net.scoreNetOver(first, last, theDNA, u5scores);
    u3Net.scoreNetOver(first, last, theDNA, u3scores);
		showProgress();

    int ba;
    int uuu;
    for (int uu=0; uu<theDNA.LENGTH; uu++) {
      ba = theDNA.get2bit(uu);
      if ((ba < 0) || (ba > 3)) { // search for ambiguous bases
        for (uuu=theDNA.forceInside(uu-u5Net.NROFBASES); uuu<=theDNA.forceInside(uu+u5Net.NROFBASES); uuu++) { // eliminate all scores involving them
          u5scores[uuu] = 0;
        }
      }
    }
    u5topper = new TopFinder(u5scores, 2, 10); // prepare to search for local max
    u3topper = new TopFinder(u3scores, 2, 10); // ditto


    showInfo("Continuing preparations in " + strs + " strand");
		showProgress();
		
		Motif.RefreshInfo info = new Motif.RefreshInfo(theDNA, cfactor, sdfactor, null);
		MotifHit mh;
		int ind;
		pptHits = new boolean[theDNA.LENGTH];
		pptMotif.refresh(info);
		for (int poi=first; poi<=last; poi++) {
			if ((mh = pptMotif.getMotifHitAt(poi)) != null) {
				pptHits[mh.upperOverlapLimit(0.1f)] = true;
			}
		}
		pbsHits = new boolean[theDNA.LENGTH];
		for (int pb=0; pb<pbsMotifs.length; pb++) {
			pbsMotifs[pb].refresh(info);
			for (int poi=first; poi<=last; poi++) {
				if ((mh = pbsMotifs[pb].getMotifHitAt(poi)) != null) {
					try {
						ind = mh.lowerOverlapLimit(0.1f);
						pbsHits[ind] = true;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw e;
					}
				}
			}
		}
    
// do GTModifier
    gtm = theDNA.getGTModifier();
		showProgress();
    
// do TATAA box weight matrix
    float[ ] tataaval = new float[theDNA.LENGTH];
    for (int pos=first; (pos<theDNA.LENGTH-tATAAMatrix.LENGTH) & (pos<=last); pos++) {
      tataaval[pos] = theDNA.baseMatrixScoreAt(tATAAMatrix, pos);
    }
    tataatopper = new TopFinder(tataaval, 2, 10);
		showProgress();
    
// do MEME 50 base weight matrxix
    float[ ] mEME50val = new float[theDNA.LENGTH];
    for (int pos = first; (pos < theDNA.LENGTH - mEME50Matrix.LENGTH) & (pos <= last); pos++) {
      mEME50val[pos] = theDNA.baseMatrixScoreAt(mEME50Matrix, pos);
    }
    mEME50topper = new TopFinder(mEME50val, 2, 10);
		showProgress();
    
// do first group of base motifs
		motifs1val = new float[theDNA.LENGTH]; // for scores
    for (int po=0; po<motifs1val.length; po++) {
      motifs1val[po] = -Float.MAX_VALUE;
    }
    motifs1mot = new Motif[theDNA.LENGTH]; // for best Motifs
    float tempf;
  	for (int mot=0; mot<motifs1.length; mot++) {
      for (int pos = first; pos <= last; pos++) {
        tempf = motifs1[mot].getRawScoreAt(pos)/motifs1[mot].getBestRawScore();
        if (tempf >= motifs1val[pos]) {
          motifs1val[pos] = tempf;
          motifs1mot[pos] = motifs1[mot];
        }
      }
    }
    motifs1topper = new TopFinder(motifs1val, 2, 10);
		showProgress();

// do second group of base motifs
		motifs2val = new float[theDNA.LENGTH]; // for scores
    for (int po=0; po<motifs2val.length; po++) {
      motifs2val[po] = -Float.MAX_VALUE;
    }
    motifs2mot = new Motif[theDNA.LENGTH]; // for best Motifs
  	for (int mot=0; mot<motifs2.length; mot++) {
      for (int pos = first; pos <= last; pos++) {
        tempf = motifs2[mot].getRawScoreAt(pos)/motifs2[mot].getBestRawScore();
        if (tempf >= motifs2val[pos]) {
          motifs2val[pos] = tempf;
          motifs2mot[pos] = motifs2[mot];
        }
      }
    }
    motifs2topper = new TopFinder(motifs2val, 2, 10);
		showProgress();

// do Modifiers
    trpgm = theDNA.getTransSiteModifier();
    transsummer = new SumFinder(trpgm.getArray(), 2, 10);
		showProgress();
    
    cpgm = theDNA.getCpGModifier();
    cpgsummer = new SumFinder(cpgm.getArray(), 2, 10);
		showProgress();

    s8m = theDNA.getSplitOctamerModifier();
    s8summer = new SumFinder(s8m.getArray(), 2, 10);
		showProgress();
    return true;
	} // end of setDNA(DNA, int, int, String)
	
	
	private Hashtable table = new Hashtable(); // for contents of LTRIDData.txt
	private DNA theDNA = null;

	private BaseWeightMatrix tATAAMatrix; // from LTRIDData.txt
	private BaseWeightMatrix mEME50Matrix; // from LTRIDData.txt

  private float expStrength = 2;
  private float repTolerance = 7;
  private float lineLTRTolerance = 10;
  
	private float aATAAAWeight;
  private DistanceSet aATAAADistances;
  private final byte[ ] AATAAASTRAND = {3, 3, 0, 3, 3, 3};
  private final byte[ ] ATTAAASTRAND = {3, 0, 0, 3, 3, 3};
  private final byte[ ] AGTAAASTRAND = {3, 2, 0, 3, 3, 3};

	private float u3NetWeight;
  private BaseNet u3Net;
  private DistanceSet u3NetDistances;
  private float[ ] u3scores;
  private TopFinder u3topper;

	private float u5NetWeight;
  private BaseNet u5Net;
  private DistanceSet u5NetDistances;
  private float[ ] u5scores;
  private TopFinder u5topper;

	private float gtWeight;
  private GTModifier gtm;
  
	private float tATAAWeight;
  private DistanceSet tATAADistances;
  private TopFinder tataatopper;
	
	private float mEME50Weight;
  private DistanceSet mEME50Distances;
  private TopFinder mEME50topper;
	
	private OrdinaryMotif[ ] motifs1;
	private DistanceSet motifs1Distances;
	private float motifs1Weight;
  private float[ ] motifs1val;
  private Motif[ ] motifs1mot;
  private TopFinder motifs1topper;

	private OrdinaryMotif[ ] motifs2;
	private DistanceSet motifs2Distances;
	private float motifs2Weight;
  private float[ ] motifs2val;
  private Motif[ ] motifs2mot;
  private TopFinder motifs2topper;

  private float transWeight;
  private DistanceSet transDistances;
  private TransSiteModifier trpgm;
  private SumFinder transsummer;

  private float cpgWeight;
  private DistanceSet cpgDistances;
  private CpGModifier cpgm;
  private SumFinder cpgsummer;

  private float split8Weight;
  private DistanceSet split8Distances;
  private SplitOctamerModifier s8m;
  private SumFinder s8summer;

  private float singleThreshold;
  
	private ExecutorScriptWriter theWriter = null; // for output
		
  private int gcode = Compactor.BASECOMPACTOR.charToIntId('g');
  private int acode = Compactor.BASECOMPACTOR.charToIntId('a');
  private int tcode = Compactor.BASECOMPACTOR.charToIntId('t');
  private int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
  
  private LTRCandidate[ ] ltrcands;
  
  private Stack singleLTRs;
	private Stack pairStack;
  private boolean debugging = false;
  
  private boolean outputsingles = true;
	
	private int maxPairsOutput;
  
  private Database database;
	
	private RetroVID retroVID = new RetroVID();
	private float cfactor;
	private float sdfactor;
	
	private Motif pptMotif;
	private Motif[ ] pbsMotifs;
	
	private boolean[ ] pptHits;
	private boolean[ ] pbsHits;
	
	private int extendIntoPads;
	
	private int rogueCounter = 0; // counter for LTRCandidates on similarity only
	
// sets up theWriter if it is not done
	private final ExecutorScriptWriter getWriter() throws RetroTectorException {
		if (theWriter == null) {
			File scriptFile = FileNamer.createRetroVIDScript();
			theWriter = new ExecutorScriptWriter(scriptFile, "RetroVID");
			theWriter.writeSingleParameter(DNAFILEKEY, getString(DNAFILEKEY, ""), false);
			theWriter.writeSingleParameter(DATABASEKEY, database.DATABASENAME, false);
		}
		return theWriter;
	} // end of getWriter()
	
  private final int firstAllowed() {
		return Math.max(0, theDNA.firstUnPadded() - extendIntoPads);
	} // end of firstAllowed()
	
  private final int lastAllowed() {
		return Math.min(theDNA.LENGTH - 1, theDNA.lastUnPadded() + extendIntoPads);
	} // end of lastAllowed()
	
	
// goes through one of the strands
// parameter strs is 'P' or 'S'
  private final void doStrand(String strs) throws RetroTectorException {
    ltrcands = new LTRCandidate[theDNA.LENGTH]; // one slot per position in DNA
    
// collect candidates
    int counter = 10000; // progress counter
    showInfo("Trying positions " + theDNA.externalize(2) + "-" + theDNA.externalize(Math.min(10001, theDNA.LENGTH - 1)) + " in " + strs + " strand");
    for (int position=firstAllowed()+2; position<(lastAllowed() - 31); position++) { // main loop
      doPosition(position); // hits are stored in ltrcands
      counter--;
      if (counter == 0) {
        counter = 10000;
        showProgress();
        showInfo("Trying positions " + theDNA.externalize(Math.min(position, theDNA.LENGTH - 1)) + "-" + theDNA.externalize(Math.min(position + 9999, theDNA.LENGTH - 1)) + " in " + strs + " strand");
      }
    }
    
    showInfo("Building LTR pairs in " + strs + " strand");
    pairStack = new Stack();
    int po; // hotspot of potential first LTR in pair
    int popo; // position with simililar surroundings as po
    LTRCandidate hit1; // potential first LTR in pair
    LTRCandidate hit1b; // potential first LTR in pair
    LTRCandidate hit2; // potential second LTR in pair
    LTRPair pair;
    byte[ ] pattern = new byte[21]; // for bases hotspot - 10 to hotspot + 10
		
    for (int pos1=ltrcands.length-1; pos1>=0; pos1--) { // go through hits found so far, from top
      if (ltrcands[pos1] != null) {
				showProgress();
        hit1 = ltrcands[pos1];
        showInfo("Pairing hit at " + theDNA.externalize(hit1.hotSpotPosition) + " in " + strs + " strand");
        po = hit1.hotSpotPosition;
				if ((po >= 10) && (po < (theDNA.LENGTH - 10))) {
					pattern[10] = theDNA.get2bit(po); // make search pattern around hotspot
					for (int w=1; w<=10; w++) {
						pattern[10 - w] = theDNA.get2bit(po - w);
						pattern[10 + w] = theDNA.get2bit(po + w);
					}
					if (rogueCounter < (theDNA.LENGTH - theDNA.NCOUNT) / 1000) { // look for similar ones
						if ((po >= 10) && (po < (theDNA.LENGTH - 50))) {
		// search for reasonably similar pattern within acceptable distance
							AgrepContext context = new AgrepContext(pattern, Math.min(theDNA.LENGTH - 21, po + MINLTRPAIRSPAN), Math.min(theDNA.LENGTH - 21, po + MAXLTRPAIRSPAN), 3);
							while ((popo = theDNA.agrepFind(context)) != Integer.MIN_VALUE) {
								popo = Math.abs(popo);
								showProgress();
								if (popo != (po - 10)) { // avoid itself
									if (ltrcands[popo + 10] == null) {
										ltrcands[popo + 10] = makeCandidate(popo + 10, -1, Integer.MAX_VALUE);
										rogueCounter++; // make possible candidate
									}
								}
							}
						}
					}
	// search for hits with rough similarity around hotspots and try to pair with them
					for (int pos2=theDNA.forceInside(pos1+MINLTRPAIRSPAN); pos2<=theDNA.forceInside(pos1+MAXLTRPAIRSPAN); pos2++) {
						try {
							if (((hit2 = ltrcands[pos2]) != null) && (theDNA.discrepancies(pattern, pos2 - 10) <= 6) && !(hit1.hotSpotType.equals("-") & hit2.hotSpotType.equals("-"))) {
								pair = makePair(hit1, hit2, theDNA.LENGTH, -1);
								if (pair != null) {
									for (int hitend=pair.LTR5LAST-1; hitend>=pair.LTR5HOTSPOT+LTRPair.MINTRAILING; hitend--) {
										if (pbsHits[hitend]) {
											makePair(hit1, hit2, hitend, -1);
											for (int hitbeg=pair.LTR3FIRST+1; hitbeg<=pair.LTR3HOTSPOT-LTRPair.MINLEADING; hitbeg++) {
												if (pptHits[hitbeg]) {
													makePair(hit1, hit2, hitend, hitbeg);
												}
											}
										}
									}
									for (int hitbeg=pair.LTR3FIRST+1; hitbeg<=pair.LTR3HOTSPOT-LTRPair.MINLEADING; hitbeg++) {
										if (pptHits[hitbeg]) {
											makePair(hit1, hit2, theDNA.LENGTH, hitbeg);
										}
									}
								}
							}
						} catch (NullPointerException npe) {
							throw npe;
						}
					}
				}
      }
    }
    
// output LTR pairs
		LTRPair[ ] paira = new LTRPair[pairStack.size()];
		pairStack.copyInto(paira);
		if (paira.length > maxPairsOutput) {
			Utilities.sort(paira);
			for (int pp=1; pp<=maxPairsOutput; pp++) {
				pairsOutputHistogram[(int) Math.floor(100 * paira[pp - 1].FACTOR)]++;
				paira[pp - 1].toWriter(getWriter(), strs + pp);
			}
		} else {
			for (int pp=1; pp<=paira.length; pp++) {
				pairsOutputHistogram[(int) Math.floor(100 * paira[paira.length - pp].FACTOR)]++;
				paira[paira.length - pp].toWriter(getWriter(), strs + pp);
			}
		}

    if (outputsingles) {
      showInfo("Making single LTR candidates in "  + strs + " strand");
      singleLTRs = new Stack();
      for (int si=ltrcands.length-1; si>=0; si--) {
        if (ltrcands[si] != null) {
          makeSingle(ltrcands[si]);
        }
      }
      int c = 1;
      while ((singleLTRs.size() > 0) && (singleLTRs.peek() != null)) {
        getWriter().startMultiParameter(SINGLELTRSUFFIX + strs + c, false);
        getWriter().appendToMultiParameter(((LTRCandidate) singleLTRs.pop()).toStrings(), false);
        getWriter().finishMultiParameter(false);
        c++;
				singlesCount++;
      }
    }
  } // end of doStrand(String)
	
	private final LTRPair makePair(LTRCandidate hit1, LTRCandidate hit2, int pbsBarrier, int pptBarrier) throws RetroTectorException {
		showProgress();
		String lim = "";
		if (pbsBarrier < theDNA.LENGTH) {
			lim = "PBS:" + theDNA.externalize(pbsBarrier);
			if (pptBarrier >= 0) {
				lim = lim + ",PPT:" + theDNA.externalize(pptBarrier);
			}
		} else {
			if (pptBarrier >= 0) {
				lim = "PPT:" + theDNA.externalize(pptBarrier);
			}
		}
		try { // try to pair them
			LTRPair pair = new LTRPair(hit1, hit2, repTolerance, theDNA, pbsBarrier, pptBarrier, extendIntoPads); // make a first try at pairing
			LTRCandidate hit1b = makeCandidate(pair.LTR5HOTSPOT, pair.LTR5FIRST, pair.LTR5LAST); // recalculate within redefined limits
			if ((hit1b != null) && !hit1b.likeLINE(lineLTRTolerance)) {
				hit1b.candidateLimiters = lim;
				LTRCandidate hit2b = makeCandidate(pair.LTR3HOTSPOT, pair.LTR3FIRST, pair.LTR3LAST); // ditto
				if ((hit2b != null) && !hit2b.likeLINE(lineLTRTolerance)) {
					hit2b.candidateLimiters = lim;
					pair = new LTRPair(hit1b, hit2b, pair.SOURCEDNA, pair.REPLENGTH, pair.INTEGRATIONSITES); // make final pair
					for (int pi=0; pi<pairStack.size(); pi++) {
						if (pair.doesEqual(pairStack.elementAt(pi))) {
							return null;
						}
					}
					pairsMadeHistogram[(int) Math.floor(100 * pair.FACTOR)]++;
					pairStack.push(pair);
					return pair;
				}
			} else {
				return null;
			}
		} catch (RetroTectorException e) {
		}
		return null;
	} // end of makePair(LTRCandidate, LTRCandidate, int, int)
  
// tries to make a single LTR candidate and push it on singleLTRs
  private final void makeSingle(LTRCandidate inHit) throws RetroTectorException {
    if (inHit.candidateFactor < (singleThreshold * 0.9f)) { // not worth the time
      return;
    }
    
// score factor good enough. Are there integration sites?
// optimum[0] = direct repeat length
// optimum[1] = LTR start
// optimum[2] = LTR end
    int[ ] optimum = new int[3];
    int precode;
    int postcode;
    int mask;
// LTR start is assumed to be 50-1950 bases before hotspot
    for (int pre=theDNA.forceInside(inHit.hotSpotPosition-LTRPair.MINLEADING); pre>=theDNA.forceInside(inHit.hotSpotPosition-LTRPair.MAXLEADING); pre--) {
// short inverted repeat is required
      if ((theDNA.get2bit(pre) == tcode) && (theDNA.get2bit(pre + 1) == gcode)) {
// LTR end is assumed to be 40-1000 bases after hotspot
        for (int post=theDNA.forceInside(inHit.hotSpotPosition+LTRPair.MINTRAILING); post<theDNA.forceInside(inHit.hotSpotPosition+LTRPair.MAXTRAILING); post++) {
// short inverted repeat is required
          if ((theDNA.get2bit(post) == acode) && (theDNA.get2bit(post - 1) == ccode)) {
// encode 15 bases as integer
            precode = theDNA.get30bit(pre - 15);
            postcode = theDNA.get30bit(post + 1);
// ambiguous bases not accepted
            if ((precode >= 0) && (postcode >= 0)) {
              mask = 0x0fffffff;
// try direct repeat lengths between 15 and 1
              for (int sim=15; sim>0; sim--) {
                if (precode == postcode) {
                  if (sim > optimum[0]) {
                    optimum[0] = sim;
                    optimum[1] = pre;
                    optimum[2] = post;
                  }
                }
                precode = precode & mask;
                mask = mask >> 2;
                postcode = postcode >> 2;
              }
            }
          }
        }
      }
    }
// direct repeat must be at least 3
    if (optimum[0] > 3) {
// make description of integration sites
      StringBuffer sx = new StringBuffer();
      sx.append('_');
			if (optimum[1]-optimum[0] < theDNA.FIRSTUNPADDED) {
				sx.append("* ");
			}
      for (int px=optimum[1]-optimum[0]; px<=optimum[1]-1; px++) {
        sx.append(theDNA.getBase(px));
      }
      sx.append('/');
      for (int px=optimum[1]; px<=optimum[1]+1; px++) {
        sx.append(theDNA.getBase(px));
      }
      sx.append("<>");
      for (int pxx=optimum[2]-1; pxx<=optimum[2]; pxx++) {
        sx.append(theDNA.getBase(pxx));
      }
      sx.append('/');
      for (int pxx=optimum[2]+1; pxx<=optimum[2]+optimum[0]; pxx++) {
        sx.append(theDNA.getBase(pxx));
      }
      
      LTRCandidate cand = makeCandidate(inHit.hotSpotPosition, optimum[1], optimum[2]);
      if ((cand != null) && (cand.candidateFactor >= singleThreshold) && !cand.likeLINE(lineLTRTolerance)) {
        cand.candidateComment = cand.candidateComment + sx.toString();
				singlesHistogram[(int) Math.floor(100 * cand.candidateFactor)]++;
        singleLTRs.push(cand);
      }
    }
  } // end of makeSingle(LTRCandidate)
  
// try to make possible candidate
  private final void doPosition(int position) throws RetroTectorException {
    
// find hotspot
		if (theDNA.polyASignalAt(position)
          || (u5scores[position + 4] > u5Net.NNTHRESHOLD)
        ) {
			if (debugging) {
				System.out.println("at " + theDNA.externalize(position) + " u5score=" + u5scores[position + 4] + " " + u5Net.NNTHRESHOLD);
			}
      LTRCandidate ltrh = makeCandidate(position, -1, Integer.MAX_VALUE);
      ltrcands[position] = ltrh;
    }
  } // end of doPosition(int)

// returns extranalized pos within parentheses
  private final String posString(int pos) {
    return "(" + theDNA.externalize(pos) + ");";
  } // end of posString(int)
  
/**
* Makes possible LTR candidate at known hotspot position.
* If finalstart < 0 and finalend == Integer.MAX_VALUE start and end are calculated.
* @param	position		The hotspot (polyadenylation signal) position to use.
* @param	finalstart	The first position in the candidate if >= 0.
* @param	finalend		The first position in the candidate if <Integer.MAX_VALUE.
* @return	An LTRCandidate as specified.
*/
  public final LTRCandidate makeCandidate(int position, int finalstart, int finalend) throws RetroTectorException {
  	float maxsum = aATAAAWeight; // to accumulate perfect component values
  	float sum = 0; // to accumulate actual component values
  	RVector rVector = new RVector(); // to accumulate virus genus evidence
    StringBuffer description = new StringBuffer(); // to accumulate description string
    InternalHit[ ] intHits; // for locatable components
    Stack inHitStack = new Stack(); // to accumulate components

    LTRCandidate result = new LTRCandidate(theDNA, database);
    
    result.hotSpotPosition = position;
// aataaahit?
    if (theDNA.patternAt(position - 2, AATAAASTRAND)) {
      description.append("AATAAA");
      description.append(posString(position));
      sum += aATAAAWeight;
      result.hotSpotScore = aATAAAWeight;
      result.hotSpotType = "AATAAA";
   } else if (theDNA.patternAt(position - 2, ATTAAASTRAND)) {
      description.append("ATTAAA");
      description.append(posString(position));
      sum += aATAAAWeight;
      result.hotSpotScore = aATAAAWeight;
      result.hotSpotType = "ATTAAA";
    } else if (theDNA.patternAt(position - 2, AGTAAASTRAND)) {
      description.append("AGTAAA");
      description.append(posString(position));
      sum += aATAAAWeight;
      result.hotSpotScore = aATAAAWeight;
      result.hotSpotType = "AGTAAA";
		} else { // rougher similarity?
			int erri = 0;
			String att = theDNA.subString(position - 2, position + 3, true);
			if (att.charAt(0) != 'a') {
				erri++;
			}
			if (att.charAt(2) != 't') {
				erri++;
			}
			if (att.charAt(3) != 'a') {
				erri++;
			}
			if (att.charAt(4) != 'a') {
				erri++;
			}
			if (att.charAt(5) != 'a') {
				erri++;
			}
			if (erri <= 1) {
				description.append(att);
				description.append(posString(position));
				sum += aATAAAWeight * 0.5f;
				result.hotSpotScore = aATAAAWeight * 0.5f;
				result.hotSpotType = att;
			} else {
				description.append("AATAAA");
				description.append("(-);");
			}
		}
    result.hotSpotMax = aATAAAWeight;
// best u5nn value
    maxsum += u5NetWeight;
		int y1;
		y1 = position - u5NetDistances.DISTTOHOTSPOT.HIGHESTDISTANCE;
		y1 = Math.min(y1, theDNA.LENGTH - 1 - u5Net.NROFBASES);
		y1 = Math.max(y1, firstAllowed() - u5NetDistances.DISTTOSTART.HIGHESTDISTANCE + 1);
		y1 = Math.max(finalstart, y1);
		y1 = theDNA.forceInside(y1);
		
		int y2;
		y2 = position - u5NetDistances.DISTTOHOTSPOT.LOWESTDISTANCE;
		y2 = Math.min(y2, theDNA.LENGTH - 1 - u5Net.NROFBASES);
		y2 = Math.min(y2, lastAllowed() - u5NetDistances.DISTTOEND.LOWESTDISTANCE - 1);
		y2 = Math.min(finalend, y2);
		y2 = theDNA.forceInside(y2);
		
    float usc = u5topper.maxvalue(y1, y2);
    int u5pos = u5topper.maxpos;
    if (u5pos < 0) {
      return null;
    }
    sum += u5NetWeight * usc;
    
    description.append("U5NN:");
    description.append(Utilities.twoDecimals(usc));
    description.append('(');
    description.append(theDNA.externalize(u5pos));
    description.append(");");
    
    result.u5Score = u5NetWeight * usc;
    result.u5Max = u5NetWeight;
    result.u5Position = u5pos;
    
    inHitStack.push(new InternalHit(u5NetWeight * usc,
          new Range(Range.LTRSTART, u5pos, u5NetDistances.DISTTOSTART),
          new Range(Range.LTREND, u5pos, u5NetDistances.DISTTOEND)));
  
// GTModifier at u5nn max
    maxsum += gtWeight;
    float gtx = gtm.modification(u5pos) / gtm.getUpperLimit();
    sum += gtWeight * gtx;
    description.append("GT:");
    description.append(Utilities.twoDecimals(gtx));
    description.append(';');
    
    result.gtScore = gtWeight * gtx;
    result.gtMax = gtWeight;

// hotspot defined and u5net applied. continue
// acceptable range of LTR start
    Range startrange = new Range(Range.LTRSTART, position, aATAAADistances.DISTTOSTART);
// acceptable range of LTR end
    Range endrange = new Range(Range.LTREND, position, aATAAADistances.DISTTOEND);
 
// apply u3 NN
    maxsum += u3NetWeight;
		y1 = position - u3NetDistances.DISTTOHOTSPOT.HIGHESTDISTANCE;
		y1 = Math.min(y1, theDNA.LENGTH - 1 - u3Net.NROFBASES);
		y1 = Math.max(y1, firstAllowed() - u3NetDistances.DISTTOSTART.HIGHESTDISTANCE + 1);
		y1 = Math.max(finalstart, y1);
		y1 = theDNA.forceInside(y1);
		y2 = position - u3NetDistances.DISTTOHOTSPOT.LOWESTDISTANCE;
		y2 = Math.min(y2, theDNA.LENGTH - 1 - u3Net.NROFBASES);
		y2 = Math.min(y2, lastAllowed() - u3NetDistances.DISTTOEND.LOWESTDISTANCE - 1);
		y2 = Math.min(finalend, y2);
		y2 = theDNA.forceInside(y2);
    float u3max = u3topper.maxvalue(y1, y2);
    int u3pos = u3topper.maxpos;
    if (u3pos < 0) {
      return null;
    }
    description.append("U3NN:");
    if (Float.isNaN(u3max)) { // Float.NaN
      description.append("0.0");
      result.u3Score = 0;
    } else {
      sum += u3NetWeight * u3max;
      description.append(Utilities.twoDecimals(u3max));
      result.u3Score = u3NetWeight * u3max;
    }
    description.append('(');
    description.append(theDNA.externalize(u3pos));
    description.append(");");

    result.u3Max = u3NetWeight;
    result.u3Position = u3pos;
    
    inHitStack.push(new InternalHit(u3NetWeight * u3max,
          new Range(Range.LTRSTART, u3pos, u3NetDistances.DISTTOSTART),
          new Range(Range.LTREND, u3pos, u3NetDistances.DISTTOEND)));

// apply TATAA
    maxsum += tATAAWeight;
		y1 = position - tATAADistances.DISTTOHOTSPOT.HIGHESTDISTANCE;
		y1 = Math.max(y1, firstAllowed() - tATAADistances.DISTTOSTART.HIGHESTDISTANCE + 1);
		y1 = Math.max(finalstart, y1);
		y1 = theDNA.forceInside(y1);
		y2 = position - tATAADistances.DISTTOHOTSPOT.LOWESTDISTANCE;
		y2 = Math.min(y2, lastAllowed() - tATAADistances.DISTTOEND.LOWESTDISTANCE - 1);
		y2 = Math.min(finalend, y2);
		y2 = theDNA.forceInside(y2);
    float bestscore = tataatopper.maxvalue(y1, y2);
  	int tataapos = tataatopper.maxpos;
    if (tataapos < 0) {
      return null;
    }
    bestscore /= tATAAMatrix.MAXSCORE;
    sum += tATAAWeight * bestscore;
    if (tataapos > position) { // tataa after hotspot, suggests delta
      rVector.plus(new RVector("D"), bestscore);
    }
    description.append("TATAA:");
    description.append(Utilities.twoDecimals(bestscore));
    description.append('(');
    description.append(theDNA.externalize(tataapos));
    description.append(");");
    
    result.tataaScore =  tATAAWeight * bestscore;
    result.tataaMax =  tATAAWeight;
    result.tataaPosition = tataapos;
    
    inHitStack.push(new InternalHit(tATAAWeight * bestscore,
          new Range(Range.LTRSTART, tataapos, tATAADistances.DISTTOSTART),
          new Range(Range.LTREND, tataapos, tATAADistances.DISTTOEND)));
 
// apply MEME50
    maxsum += mEME50Weight;
		y1 = position - mEME50Distances.DISTTOHOTSPOT.HIGHESTDISTANCE;
		y1 = Math.max(y1, firstAllowed() - mEME50Distances.DISTTOSTART.HIGHESTDISTANCE + 1);
		y1 = Math.max(finalstart, y1);
		y1 = theDNA.forceInside(y1);
		y2 = position - mEME50Distances.DISTTOHOTSPOT.LOWESTDISTANCE;
		y2 = Math.min(y2, lastAllowed() - mEME50Distances.DISTTOEND.LOWESTDISTANCE - 1);
		y2 = Math.min(finalend, y2);
		y2 = theDNA.forceInside(y2);
    bestscore = mEME50topper.maxvalue(y1, y2);
  	int mEME50pos = mEME50topper.maxpos;
    if (mEME50pos < 0) {
      return null;
    }
    bestscore /= mEME50Matrix.MAXSCORE;
    sum += mEME50Weight * bestscore;
    description.append("MEME50:");
    description.append(Utilities.twoDecimals(bestscore));
    description.append('(');
    description.append(theDNA.externalize(mEME50pos));
    description.append(");");
    
    result.mEME50Score =  mEME50Weight * bestscore;
    result.mEME50Max =  mEME50Weight;
    result.mEME50Position = mEME50pos;
    
    inHitStack.push(new InternalHit(mEME50Weight * bestscore,
          new Range(Range.LTRSTART, mEME50pos, mEME50Distances.DISTTOSTART),
          new Range(Range.LTREND, mEME50pos, mEME50Distances.DISTTOEND)));
 
// apply first motif group
    maxsum += motifs1Weight;
		y1 = position - motifs1Distances.DISTTOHOTSPOT.HIGHESTDISTANCE;
		y1 = Math.max(y1, firstAllowed() - motifs1Distances.DISTTOSTART.HIGHESTDISTANCE + 1);
		y1 = Math.max(finalstart, y1);
		y1 = theDNA.forceInside(y1);
		y2 = position - motifs1Distances.DISTTOHOTSPOT.LOWESTDISTANCE;
		y2 = Math.min(y2, lastAllowed() - motifs1Distances.DISTTOEND.LOWESTDISTANCE - 1);
		y2 = Math.min(finalend, y2);
		y2 = theDNA.forceInside(y2);
    bestscore = motifs1topper.maxvalue(y1, y2);
  	int motif1pos = motifs1topper.maxpos;
    if (motif1pos < 0) {
      return null;
    }
    sum += motifs1Weight * bestscore;
    rVector.plus(motifs1mot[motif1pos].MOTIFRVECTOR, bestscore);
    description.append("Mot1:");
    description.append(Utilities.twoDecimals(bestscore));
    description.append('(');
    description.append(theDNA.externalize(motif1pos));
    description.append(");");
    
    result.mot1Score = motifs1Weight * bestscore;
    result.mot1Max = motifs1Weight;
    result.mot1Position = motif1pos;
    
    inHitStack.push(new InternalHit(motifs1Weight * bestscore,
        new Range(Range.LTRSTART, motif1pos, motifs1Distances.DISTTOSTART),
        new Range(Range.LTREND, motif1pos, motifs1Distances.DISTTOEND)));

// apply second motif group
    maxsum += motifs2Weight;
		y1 = position - motifs2Distances.DISTTOHOTSPOT.HIGHESTDISTANCE;
		y1 = Math.max(y1, firstAllowed() - motifs2Distances.DISTTOSTART.HIGHESTDISTANCE + 1);
		y1 = Math.max(finalstart, y1);
		y1 = theDNA.forceInside(y1);
		y2 = position - motifs2Distances.DISTTOHOTSPOT.LOWESTDISTANCE;
		y2 = Math.min(y2, lastAllowed() - motifs2Distances.DISTTOEND.LOWESTDISTANCE - 1);
		y2 = Math.min(finalend, y2);
		y2 = theDNA.forceInside(y2);
    bestscore = motifs2topper.maxvalue(y1, y2);
  	int motif2pos = motifs2topper.maxpos;
    if (motif2pos < 0) {
      return null;
    }
    sum += motifs2Weight * bestscore;
    rVector.plus(motifs2mot[motif2pos].MOTIFRVECTOR, bestscore);
    description.append("Mot2:");
    description.append(Utilities.twoDecimals(bestscore));
    description.append('(');
    description.append(theDNA.externalize(motif2pos));
    description.append(");");
    
    result.mot2Score = motifs2Weight * bestscore;
    result.mot2Max = motifs2Weight;
    result.mot2Position = motif2pos;
    
    inHitStack.push(new InternalHit(motifs2Weight * bestscore,
        new Range(Range.LTRSTART, motif2pos, motifs2Distances.DISTTOSTART),
        new Range(Range.LTREND, motif2pos, motifs2Distances.DISTTOEND)));


// estimate start and end
    intHits = new InternalHit[inHitStack.size()];
    inHitStack.copyInto(intHits);
    Utilities.sort(intHits);
    Range tRange;
    for (int i=0; i<intHits.length; i++) { // go through components from best to worst
      tRange = Range.consensus(intHits[i].IHSTARTRANGE, startrange);
      if (tRange != null) { // if it does not go too far
        startrange = tRange; // restrict allowed range
      }
      tRange = Range.consensus(intHits[i].IHENDRANGE, endrange);
      if (tRange != null) { // if it does not go too far
        endrange = tRange; // restrict allowed range
      }
    }
		startrange = Range.restrictRangeMin(startrange, firstAllowed());
		if (startrange == null) {
			return null;
		}
		endrange = Range.restrictRangeMax(endrange, lastAllowed());
		if (endrange == null) {
			return null;
		}

    boolean redo = false;
    if (finalstart < 0) { // recalculate start
      redo = true;
      int fs = dimerPosition(startrange, 't', 'g'); // try to find tg at start
      if (fs >= 0) {
        finalstart = fs;
      } else { // use range midpoint
        finalstart = Math.max(0, startrange.middle());
      }
    }
    if (finalend == Integer.MAX_VALUE) { // recalculate end
      redo = true;
      int fe = dimerPosition(endrange, 'c', 'a'); // try to find ca at end
      if (fe >= 0) {
        finalend = Math.min(fe + 1, theDNA.LENGTH - 1);
      } else { // use range midpoint
        finalend = Math.min(endrange.middle(), theDNA.LENGTH - 1);
      }
    }
    if (redo) {
      return makeCandidate(position, finalstart, finalend);
    }

// apply transsites
    maxsum += transWeight;
    int fi1 = Math.max(finalstart, position-transDistances.DISTTOHOTSPOT.HIGHESTDISTANCE);
    int la1 = Math.min(position-transDistances.DISTTOHOTSPOT.LOWESTDISTANCE, finalend);
    float trpg = transsummer.sum(fi1, la1) / trpgm.getUpperLimit() / (la1 - fi1 + 1);
    if (Float.isNaN(trpg)) {
      return null;
    }
    sum += transWeight * trpg;
    description.append("Trans:");
    description.append(Utilities.twoDecimals(trpg));
    description.append(';');
    
    result.transScore = transWeight * trpg;
    result.transMax = transWeight;
    
// apply CpG
    maxsum += cpgWeight;
    int fi2 = Math.max(finalstart, position-cpgDistances.DISTTOHOTSPOT.HIGHESTDISTANCE);
    int la2 = Math.min(position-cpgDistances.DISTTOHOTSPOT.LOWESTDISTANCE, finalend);
    float cpg = cpgsummer.sum(fi2, la2) / cpgm.getUpperLimit() / (la2 - fi2 + 1);
    if (Float.isNaN(cpg)) {
      return null;
    }
    sum += cpgWeight * cpg;
    description.append("CpG:");
    description.append(Utilities.twoDecimals(cpg));
    description.append(';');
    
    result.cpgScore = cpgWeight * cpg;
    result.cpgMax = cpgWeight;
    
// apply split octamers
    maxsum += split8Weight;
    int fi3 = Math.max(finalstart, position-split8Distances.DISTTOHOTSPOT.HIGHESTDISTANCE);
    int la3 = Math.min(position-split8Distances.DISTTOHOTSPOT.LOWESTDISTANCE, finalend);
    float s8 = s8summer.sum(fi3, la3) / s8m.getUpperLimit() / (la3 - fi3 + 1);
    if (Float.isNaN(s8)) {
      return null;
    }
    sum += split8Weight * s8;
    description.append("Spl8:");
    description.append(Utilities.twoDecimals(s8));
    description.append(';');
    
    result.spl8Score = split8Weight * s8;
    result.spl8Max = split8Weight;
    
    
// create hit
    result.candidateFirst = finalstart;
		if (result.candidateFirst < firstAllowed()) {
			return null;
		}
    result.candidateLast = finalend;
		if (result.candidateLast > lastAllowed()) {
			return null;
		}
    result.candidateFactor = (float) (0.5 * Math.exp(expStrength * (sum - maxsum) / maxsum));
    String vstr = RVector.RVSTRING;
    if (rVector.modulus() != 0) {
      vstr = rVector.rvGenus();
    }
    result.candidateVirusGenus = vstr;
    result.candidateComment = description.toString();
    return result;
  } // end of makeCandidate(int, int, int)

/**
* @return	The DistanceRange (from polyadenylation signal) within which LTR start should be.
*/
	public final DistanceRange ltrHotspotToStart() {
		return aATAAADistances.DISTTOSTART;
	} // end of ltrHotspotToStart()
  
/**
* @return	The DistanceRange (from polyadenylation signal) within which LTR end should be.
*/
	public final DistanceRange ltrHotspotToEnd() {
		return aATAAADistances.DISTTOEND;
	} // end of ltrHotspotToEnd()
  
// finds a base pair within range, as close to its middle as possible, or -1 if not found
  private final int dimerPosition(Range range, char ch1, char ch2) {
    int i1 = theDNA.forceInside(range.middle());
    int i2 = i1;
    int c1 = Compactor.BASECOMPACTOR.charToIntId(ch1);
    int c2 = Compactor.BASECOMPACTOR.charToIntId(ch2);
    int lolim = Math.max(range.RANGEMIN, 0);
    int hilim = Math.min(range.RANGEMAX, theDNA.LENGTH - 2);
		for (int i=0; i<=(range.RANGEMAX - range.RANGEMIN) / 2; i++) {
			if ((i1 >= lolim) && (i1 <= hilim) && (theDNA.get2bit(i1) == c1) && (theDNA.get2bit(i1 + 1) == c2)) {
				return i1;
			}
			if ((i2 >= lolim) && (i2 <= hilim) && (theDNA.get2bit(i2) == c1) && (theDNA.get2bit(i2 + 1) == c2)) {
				return i2;
			}
			i1--;
			i2++;
		}
    return -1;
  } // end of dimerPosition(Range, char, char)
  
} // end of LTRID
