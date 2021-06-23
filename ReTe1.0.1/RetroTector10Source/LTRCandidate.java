/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/11 -06
* Beautified 22/11 -06
*/

package retrotector;

import java.util.*;

/**
* Class for potential LTRs, and for communication of them from LTRID to RetroVID
*/
public class LTRCandidate implements Scorable {

/**
* For direct repeats inside LTR.
*/
  class Repeat {
  
/**
* Internal start position of first component.
*/
    public final int STARTPOS1;

/**
* Internal start position of second component.
*/
    public final int STARTPOS2;

/**
* Length of either component.
*/
    public final int RLENGTH;
    
/**
* Constructor.
*	@param	s1	See STARTPOS1.
*	@param	s2	See STARTPOS2.
*	@param	le	See RLENGTH.
*/
    Repeat(int s1, int s2, int le) {
      STARTPOS1 = s1;
      STARTPOS2 = s2;
      RLENGTH = le;
    } // end of Repeat.constructor(int, int, int)
    
/**
*	@param	r	Another Repeat.
*	@return	True if this identical to r.
*/
    public final boolean doesEqual(Repeat r) {
      return ((STARTPOS1 == r.STARTPOS1) & (STARTPOS2 == r.STARTPOS2) & (RLENGTH == r.RLENGTH));
    } // end of Repeat.doesEqual(Repeat)
    
/**
*	@param	dna	The relevant DNA.
*	@return	A String description of this.
*/
    public String toString(DNA dna) {
      return "" + dna.externalize(STARTPOS1) + " " + dna.externalize(STARTPOS2) + " " + RLENGTH;
    } // end of Repeat.toString(DNA)
    
  } // end of Repeat

/**
* = "SimilarityStart".
*/
  public static final String CANDIDATESIMILARITYSTARTKEY = "SimilarityStart";

/**
* = "First".
*/
  public static final String CANDIDATEFIRSTKEY = "First";

/**
* = "Last".
*/
  public static final String CANDIDATELASTKEY = "Last";

/**
* = "SimilarityEnd".
*/
  public static final String CANDIDATESIMILARITYENDKEY = "SimilarityEnd";

/**
* = "ScoreFactor".
*/
  public static final String FACTORKEY = "ScoreFactor";

/**
* = "VirusGenus".
*/
  public static final String VIRUSGENUSKEY = "VirusGenus";

/**
* = "Hotspot".
*/
  public static final String HOTSPOTKEY = "Hotspot";

/**
* = "U5NN".
*/
  public static final String U5NNKEY = "U5NN";

/**
* = "GTModifier".
*/
  public static final String GTKEY = "GTModifier";

/**
* = "U3NN".
*/
  public static final String U3NNKEY = "U3NN";

/**
* = "TATAA".
*/
  public static final String TATAAKEY = "TATAA";

/**
* = "MEME50".
*/
  public static final String MEME50KEY = "MEME50";

/**
* = "Motifs1".
*/
  public static final String MOT1KEY = "Motifs1";

/**
* = "Motifs2".
*/
  public static final String MOT2KEY = "Motifs2";

/**
* = "Transsites".
*/
  public static final String TRANSKEY = "Transsites";

/**
* = "CpGModifier".
*/
  public static final String CPGKEY = "CpGModifier";

/**
* = "Spl8Modifier".
*/
  public static final String SPL8KEY = "Spl8Modifier";

/**
* = "ShortDescription".
*/
  public static final String SHORTDESCIPTIONKEY = "ShortDescription";
  
/**
* = "Limiters".
*/
  public static final String LIMITERSKEY = "Limiters";
  
/**
* = "DirectRepeat".
*/
  public static final String DIRECTREPEATKEY = "DirectRepeat";

  
/**
* = "RepBaseFinds".
*/
  public static final String REPBASEFINDSKEY = "RepBaseFinds";

/**
* No more than this direct repeats collected = 20.
*/
	public static final int MAXREPEATS = 20;
    

/**
* Length of LINE fragments to search for in LTRCandidate.
*/
	public static final int LINEMERSIZE = 11;

/**
*	Step between points to pick LINE fragments to search for in LTRCandidate.
*/
	public static final int LINEMERSTEP = 7;

/**
* Model LINE sequence. Not in use at present.
*/
	public static final String LINE = "gggggaggagccaagatggccgaataggaacagctccggtctacagctcccagcgtgagcgacgcagaagacgggtgatttctgcatttccaactgaggtaccaggttcatctcactggggagtgccagacagtgggcgcaggacagtgggtgcagcgcaccgtgcgtgagccgaagcagggcgaggcatcgcctcacccgggaagcgcaaggggtcagggaattccctttcctagtcaaagaaaggggtgacagacggcacctggaaaatcgggtcactcccgccctaatactgcgcttttccgacgggcttaaaaaacggcgcaccaggagattatatcccgcacctggctcggagggtcctacgcccacggagtctcgctgattgctagcacagcagtccgagatcaaactgcaaggcggcagcgaggctgggggaggggcgcccgccattgcccaggcttgattaggtaaacaaagcggccgggaagctcgaactgggtggagcccaccacagctcaaggaggcctgcctgcctctgtaggctccacctctgggggcagggcacagacaaacaaaaagacagcagtaacctctgcagacttaaatgtccctgtctgacagctttgaagagagcagtggttctcccagcacgcagcttcagatctgagaacgggcagactgcctcctcaagtgggtccctgacccccgagtagcctaactgggaggcaccccccagtaggggcggactgacacctcacacggccgggtactcctctgagacaaaacttccagaggaacgatcaggcagcagcatctgcggttcaccaatatccactgttctgcagccaccgctgctgatacccaggcaaacagggtctggagtggacctccagcaaactccaacagacctgcagctgagggtcctgtctgttagaaggaaaactaacaaacagaaaggacatccacaccaaaaacccatctgtacgtcaccatcatcaaagaccaaaggtagataaaaccacaaagatggggaaaaaacagagcagaaaaactggaaactctaaaaatcagagcgcctctccttctccaaaggaacgcagctcctcaccagcaacggaacaaagctggacggagaatgactttgacgagttgagagaagaaggcttcagacgatcaaactactccgagctacgggaggaaattcgaaccaacggcaaagaagttaaaaactttgaaaaaaaattagatgaatggataactagaataaccaatgcagagaagtccttaaaggacctgatggagctgaaaaccaaggcacgagaactacgtgacgaatgcagaagcctcagtagccgatgcgatcaactggaagaaagggtatcagtgacggaagatgaaatgaatgaaatgaagcgagaagagaagtttagagaaaaaagaataaaaagaaacgaacaaagcctccaagaaatatgggactatgtgaaaagaccaaatctgcgtctgattggtgtacctgaaagtgacggggagaatggaaccaagttggaaaacactctgcaggatattatccaggagaacttccccaatctagcaaggcaggccaacgttcagattcaggaaatacagagaacgccacaaagatactcctcgagaagagcaactccaagacacataattgtcagattcaccaaagttgaaatgaaggaaaaaatgttaagggcagccagagagaaaggtcgggttacccacaaagggaagcccatcagactaacggctgatctctcggcagaaactctacaagccagaagagagtgggggccaatattcaacattcttaaagaaaagaattttcgacccagaatttcatatccagccaaactaagcttcataagcgaaggagaaataaaatactttacagacaagcaaatgctgagagattttgtcaccaccaggcctgccctaaaagagctcctgaaggaagcgctaaacatggaaaggaacaaccagtaccagccgctgcaaaaacatgccaaattgtaaagaccatcaaggctaggaagaaactgcatcaactaacgagcaaaataaccagctaacgtcataatgacaggatcaaattcacacataacaatattaactttaaatgtaaatgggctaaatgctccaattaaaagacacagactggcaaattggataaagagtcaagacccatcagtgtgccgtattcaggaaacccatctcacgtgcagagacacacataggctcgaaataaaaggatggaggaagatctaccaagcaaatggaaaacaaaaaaaggcaggggttgcaatcctagtctctgataaaacagattttaaaccaacaaagatcaaaagagacaaagaaggccattacataatggtaaagggatcaattcaacaagaagagctaactatcctaaatatatatgcacccaatacaggagcacccagattcataaagcaagtcctgagtgacctacaaagagacttagactcccacacaataataatgggagactttaacaccccactgtcaacattagacagatcaacgagacagaaagttaacaaggatacccaggaattgaactcagctctgcaccaagcggacctaatagacatctacagaactctccaccccaaatcaacagaatatacattcttttcagcaccacaccacacctattccaaaattgaccacatagttggaagtaaagctctcctcagcaaatgtaaaagaacagaaattataacaaactgtctctcagaccacagtgcaatcaaactagaactcaggattaagaaactcactcaaaaccgctcaactacatggaaactgaacaacctgctcctgaatgactactgggtacataacgaaatgaaggcagaaataaagatgttctttgaaaccaacgagaacaaagacacaacataccagaatctctgggacacattcaaagcagtgtgtagagggaaatttatagcactaaatgcccacaagagaaagcaggaaagatctaaaattgacaccctaacatcacaattaaaagaactagaaaagcaagagcaaacacattcaaaagctagcagaaggcaagaaataactaaaatcagagcagaactgaaggaaatagagacacaaaaaacccttcaaaaaattaatgaatccaggagctggttttttgaaaagatcaacaaaattgatagaccgctagcaagactaataaagaagaaaagagagaagaatcaaatagacgcaataaaaaatgatacaggggatatcaccaccgatcccacagaaatacaaactaccgtcagagaatactataaacacctctacgcaaataaactagaaaatctagaagaaatggataaattcctcgacacgtacactctcccaagactaaaccaggaagaagttgaatctctgaatagaccaataacaggctctgaaattgaggcaataatcaatagcttaccaaccaaaaaaagtccgggaccagatggattcacagccgaattctaccagaggtacaaggaggagctggtaccattccttctgaaactattccaatcaatagaaaaagagggaatcctccctaactcattttatgaggccagcatcatcctgataccaaagcctggcagagacacaacaaaaaaagagaattttagaccaatatccttgatgaacatcgatgcaaaaatcctcaataaaatactggcaaaccgaatccagcagcacatcaaaaagcttatccaccatgatcaagtgggcttcatccctgggatgcaaggctggttcaacatacgcaaatcaataaacgtaatccagcatataaacagaaccaaagacaaaaaccacatgattatctcaatagatgcagaaaaggcctttgacaaaattcaacaacgcttcatgctaaaaactctcaataaattaggtattgatgggacgtatctcaaaataataagagctatctatgacaaacccacagccaatatcatactgaatgggcaaaaactggaagcattccctttgaaaactggcacaagacagggatgccctctctcaccactcctattcaacatagtgttggaagttctggccagggcaatcaggcaggagaaggaaataaagggtattcaattaggaaaagaggaagtcaaattgtccctgtttgcagatgacatgattgtatatctagaaaaccccatcgtctcagcccaaaatctccttaagctgataagcaacttcagcaaagtctcaggatacaaaatcaatgtgcaaaaatcacaagcattcttatacaccaataacagacaaacagagagccaaatcatgagtgaactcccattcacaattgcttcaaagagaataaaatacctaggaatccaacttacaagggatgtgaaggacctcttcaaggagaactacaaaccactgctcaatgaaataaaagaggatacaaacaaatggaagaacattccatgctcatgggtaggaagaatcaatatcgtgaaaatggccatactgcccaaggtaatttatagattcaatgccatccccatcaagctaccaatgactttcttcacagaattggaaaaaactactttaaagttcatatggaaccaaaaaagagcccacatcgccaagtcaatcctaagccaaaagaacaaagctggaggcatcacgctacctgacttcaaactatactacaaggctacggtaaccaaaacagcatggtactggtaccaaaacagagatatagaccaatggaacagaacagagccctcagaaataatgccgcatatctacaactatccgatctttgacaaacctgagaaaaacaagcaatggggaaaggattccctatttaataaatggtgctgggaaaactggctagccatatgtagaaagctgaaactggatcccttccttacaccttatacaaaaattaattcaagatggattaaagacttaaacgttagacctaaaaccataaaaaccctagaagaaaacctaggcaataccattcaggacataggcatgggcaaggacttcatgtctaaaacaccaaaagcaatggcaacaaaagccaaaattgacaaacgggatctaattaaactaaagagcttctgcacagcaaaagaaactaccatcagagtgaacaggcaacctacaaaatgggagaaaatttttgcaacctactcatctgacaaagggctaatatccagaatctacaatgaactcaaacaaatttacaagaaaaaaacaaacaaccccatcaaaaagtgggcaaaggatatgaacagacacttctcaaaagaagacatttatgcagccaaaaaacacatgaaaaaatgctcatcatca";

	private static int[ ] line; // LINE as integer codes. Not in use at present
	private static int[ ] cline; // complementary LINE as integer codes. Not in use at present
	
/*
* Constructs line and cline
*/
	static {
		line = new int[LINE.length()];
		cline = new int[LINE.length()];
		int ci;
		Compactor comp = Compactor.BASECOMPACTOR;
		for (int p=0; p<line.length; p++) {
			ci = comp.charToIntId(LINE.charAt(p));
			if (ci < 0) {
				System.out.println("Invalid character in LINE:" + LINE.charAt(p));
			}
			if (ci > 3) {
				line[p] = 4;
				cline[cline.length - 1 - p] = 4;
			} else {
				line[p] = ci;
				cline[cline.length - 1 - p] = (~ci) & 3;
			}
		}
	} // end of static initializer
	

/**
* The DNA in which this was found.
*/
  public final DNA LTRCANDIDATEDNA;
  
/**
* The Database used in making this.
*/
  public final Database LTRCANDIDATEDATABASE;
  
/**
* Internal score for hotspot.
*/
  public float hotSpotScore = 0;
  
/**
* Maximal internal score for hotspot.
*/
  public float hotSpotMax = 0;
  
/**
* Internal position of hotspot.
*/
  public int hotSpotPosition = -1;

/**
* AATAAA, AGTAAA or ATTAAA, or "-".
*/
  public String hotSpotType = "-"; // - if no hexamer present
  
  
/**
* Internal score for u5 NN.
*/
  public float u5Score = 0;
  
/**
* Maximal internal score for u5 NN.
*/
  public float u5Max = 0;

/**
* Internal position of best u5 NN score.
*/
  public int u5Position = -1;

  
/**
* Internal score for GTModifier at u5Position.
*/
  public float gtScore = 0;
  
/**
* Maximal internal score for GTModifier.
*/
  public float gtMax = 0;
  
  
/**
* Internal score for u3 NN.
*/
  public float u3Score = 0;
  
/**
* Maximal internal score for u3 NN.
*/
  public float u3Max = 0;

/**
* Internal position of best u3 NN score.
*/
  public int u3Position = -1;
  
  
/**
* Internal score for TATAA weight matrix.
*/
  public float tataaScore = 0;
  
/**
* Maximal internal score for TATAA weight matrix.
*/
  public float tataaMax = 0;

/**
* Internal position of best TATAA weight matrix score.
*/
  public int tataaPosition = -1;
  
/**
* Internal score for MEME50 weight matrix.
*/
  public float mEME50Score = 0;
  
/**
* Maximal internal score for MEME50 weight matrix.
*/
  public float mEME50Max = 0;

/**
* Internal position of best MEME50 weight matrix score.
*/
  public int mEME50Position = -1;
  
  
/**
* Internal score for group 1 base Motifs.
*/
  public float mot1Score = 0;
  
/**
* Maximal internal score for group 1 base Motifs.
*/
  public float mot1Max = 0;

/**
* Internal position of bestgroup 1 base Motifs score.
*/
  public int mot1Position = -1;
  
  
/**
* Internal score for group 2 base Motifs.
*/
  public float mot2Score = 0;
  
/**
* Maximal internal score for group 2 base Motifs.
*/
  public float mot2Max = 0;

/**
* Internal position of bestgroup 2 base Motifs score.
*/
  public int mot2Position = -1;
  
  
/**
* Internal score for TransSiteModifier.
*/
  public float transScore = 0;
  
/**
* Maximal internal score for TransSiteModifier.
*/
  public float transMax = 0;

  
/**
* Internal score for CpGModifier.
*/
  public float cpgScore = 0;
  
/**
* Maximal internal score for CpGModifier.
*/
  public float cpgMax = 0;

  
/**
* Internal score for SplitOctamerModifier.
*/
  public float spl8Score = 0;
  
/**
* Maximal internal score for SplitOctamerModifier.
*/
  public float spl8Max = 0;

/**
* The position (internal) of the start of leading similarity.
*/
  public int candidateSimilarityStart = -1;
  
/**
* The position (internal) of the start of the candidate.
*/
  public int candidateFirst;
  
/**
* The position (internal) of the end of the candidate.
*/
  public int candidateLast;
  
/**
* The position (internal) of the end of trailing similarity.
*/
  public int candidateSimilarityEnd = -1;
  
/**
* The factor by which the maximal score should be multiplied in order to
* obtain the final score.
*/
  public float candidateFactor;
  
/**
* The String representing the virus genus of the candidate.
*/
  public String candidateVirusGenus;
  
/**
* A comment, summarizing the above.
*/
  public String candidateComment;
	
/**
* The limits set by PBS and PPT hits.
*/
	public String candidateLimiters = "";
  
/**
* The direct repeats found in this.
*/
  public Repeat[ ] directrepeats = null;
	
/**
* String representing similarities to RepBase consensuses.
*/
	public String repBaseFinds = null;
  
/**
* The best score of any Chain using this. Used by CollectLTRs.
*/
	public String chainScore = "";
  
/**
* Constructor, expecting fields to be filled in by LTRID.
* @param	dna	The relevant DNA.
* @param	db	The relevant Database.
*/
  public LTRCandidate(DNA dna, Database db) throws RetroTectorException {
    LTRCANDIDATEDNA = dna;
		LTRCANDIDATEDATABASE = db;
  } // end of constructor(DNA, Database)

/**
* Constructor, using output of toStrings().
* @param	dna	The relevant DNA.
* @param	ss	The toStrings() output.
*/
  public LTRCandidate(DNA dna, String[ ] ss) throws RetroTectorException {
    LTRCANDIDATEDNA = dna;
		LTRCANDIDATEDATABASE = null;
    Hashtable table = new Hashtable();
    ParameterFileReader reader = new ParameterFileReader(ss, table);
    reader.readParameters();
    
    String[ ] sss;
    sss = Utilities.splitString((String) table.get(FACTORKEY));
    candidateFactor = Utilities.decodeFloat(sss[0]);
    
    sss = Utilities.splitString((String) table.get(VIRUSGENUSKEY));
    candidateVirusGenus = sss[0];
    
    if (table.get(CANDIDATESIMILARITYSTARTKEY) != null) {
			candidateSimilarityStart = LTRCANDIDATEDNA.internalize(Utilities.decodeInt((String) table.get(CANDIDATESIMILARITYSTARTKEY)));
		}
    
    sss = Utilities.splitString((String) table.get(CANDIDATEFIRSTKEY));
    candidateFirst = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[0]));
    
    sss = Utilities.splitString((String) table.get(CANDIDATELASTKEY));
    candidateLast = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[0]));
    
    if (table.get(CANDIDATESIMILARITYENDKEY) != null) {
			candidateSimilarityEnd = LTRCANDIDATEDNA.internalize(Utilities.decodeInt((String) table.get(CANDIDATESIMILARITYENDKEY)));
		}
    
    sss = Utilities.splitString((String) table.get(HOTSPOTKEY));
    hotSpotScore = Utilities.decodeFloat(sss[0]);
    hotSpotMax = Utilities.decodeFloat(sss[1]);
    hotSpotPosition = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));
    hotSpotType = sss[3];
    
    sss = Utilities.splitString((String) table.get(U5NNKEY));
    u5Score = Utilities.decodeFloat(sss[0]);
    u5Max = Utilities.decodeFloat(sss[1]);
    u5Position = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));

    sss = Utilities.splitString((String) table.get(GTKEY));
    gtScore = Utilities.decodeFloat(sss[0]);
    gtMax = Utilities.decodeFloat(sss[1]);

    sss = Utilities.splitString((String) table.get(U3NNKEY));
    u3Score = Utilities.decodeFloat(sss[0]);
    u3Max = Utilities.decodeFloat(sss[1]);
    u3Position = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));

    sss = Utilities.splitString((String) table.get(TATAAKEY));
    tataaScore = Utilities.decodeFloat(sss[0]);
    tataaMax = Utilities.decodeFloat(sss[1]);
    tataaPosition = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));

    sss = Utilities.splitString((String) table.get(MEME50KEY));
    mEME50Score = Utilities.decodeFloat(sss[0]);
    mEME50Max = Utilities.decodeFloat(sss[1]);
    mEME50Position = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));

    sss = Utilities.splitString((String) table.get(MOT1KEY));
    mot1Score = Utilities.decodeFloat(sss[0]);
    mot1Max = Utilities.decodeFloat(sss[1]);
    mot1Position = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));

    sss = Utilities.splitString((String) table.get(MOT2KEY));
    mot2Score = Utilities.decodeFloat(sss[0]);
    mot2Max = Utilities.decodeFloat(sss[1]);
    mot2Position = LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[2]));

    sss = Utilities.splitString((String) table.get(TRANSKEY));
    transScore = Utilities.decodeFloat(sss[0]);
    transMax = Utilities.decodeFloat(sss[1]);

    sss = Utilities.splitString((String) table.get(CPGKEY));
    cpgScore = Utilities.decodeFloat(sss[0]);
    cpgMax = Utilities.decodeFloat(sss[1]);

    sss = Utilities.splitString((String) table.get(SPL8KEY));
    spl8Score = Utilities.decodeFloat(sss[0]);
    spl8Max = Utilities.decodeFloat(sss[1]);

    sss = Utilities.splitString((String) table.get(SHORTDESCIPTIONKEY));
    candidateComment = sss[0];
		
		if (table.get(LIMITERSKEY) != null) {
			candidateLimiters = (String) table.get(LIMITERSKEY);
		}
    
    int ri = 1;
    while (table.get(DIRECTREPEATKEY + ri) != null) {
      ri++;
    }
    directrepeats = new Repeat[ri - 1];
    for (ri = 0; ri<directrepeats.length; ri++) {
      sss = Utilities.splitString((String) table.get(DIRECTREPEATKEY + (ri + 1)));
      directrepeats[ri] = new Repeat(LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[0])), LTRCANDIDATEDNA.internalize(Utilities.decodeInt(sss[1])), Utilities.decodeInt(sss[2]));
    }
		
		repBaseFinds = (String) table.get(REPBASEFINDSKEY);
  } // end of constructor(DNA, String[ ])


// returns base code array from LINE. Not in use at present
	private int[ ] makeLINESequence(int[ ] line, int posInLine, int posInDNA) {
		int linefirst = posInLine - (posInDNA - candidateFirst);
		if (linefirst < 0) {
			return null;
		}
		int resultlength = candidateLast - candidateFirst + 1;
		if ((linefirst + resultlength) >= line.length) {
			return null;
		}
		int[ ] result = new int[resultlength];
		System.arraycopy(line, linefirst, result, 0, result.length);
		return result;
	} // end of makeLINESequence

/**
* Not in use at present.
* @return	likeLine(10).
*/
	public boolean likeLINE() {
		return likeLINE(10.0f);
	} // end of likeLINE()
	
/**
* Not in use at present.
* Searches for similarity to LINEs, direct and complementary.
* @param	lineTolerance	See roof parameter in ALUMatrix.
* @return	True if similarity between this an LINE was found.
*/
	public boolean likeLINE(float lineTolerance) {
/*
		if (lineTolerance < 0) {
			return false;
		}
 		long tim = System.currentTimeMillis();
		byte[ ] pattern = new byte[LINEMERSIZE];
		int result;
		ALUMatrix aluM1;
		ALUMatrix aluM2;
		int[ ] res;
		
		int[ ] candBases = new int[candidateLast - candidateFirst + 1];
		for (int i=0; i<candBases.length; i++) {
			candBases[i] = LTRCANDIDATEDNA.get2bit(i + candidateFirst);
		}
		
		for (int merpos = LINEMERSIZE; merpos<(LINE.length()-2*LINEMERSIZE); merpos += LINEMERSTEP) {
			for (int p=0; p<pattern.length; p++) { // make pattern of part of LINE
				pattern[p] = (byte) line[merpos + p];
			}
			AgrepContext lcont = new AgrepContext(pattern, candidateFirst + LINEMERSIZE, candidateLast - LINEMERSIZE  * 2, 0);
			while ((result = LTRCANDIDATEDNA.agrepFind(lcont)) != Integer.MIN_VALUE) { // got one
				result = Math.abs(result);
				res = makeLINESequence(line, merpos, result);
				if (res != null) {
					try {
						aluM1 = new ALUMatrix(res, candBases, 1.0f, 1.0f, 0.95f, lineTolerance);
						return true;
					} catch (RetroTectorException re) {
					}
				}
			}
		}
		for (int merpos = LINEMERSIZE; merpos<(LINE.length()-2*LINEMERSIZE); merpos += LINEMERSTEP) {
			for (int p=0; p<pattern.length; p++) { // make pattern of part of complementary LINE
				pattern[p] = (byte) cline[merpos + p];
			}
			AgrepContext lcont = new AgrepContext(pattern, candidateFirst + LINEMERSIZE, candidateLast - LINEMERSIZE  * 2, 0);
			while ((result = LTRCANDIDATEDNA.agrepFind(lcont)) != Integer.MIN_VALUE) { // got one
				result = Math.abs(result);
				res = makeLINESequence(cline, merpos, result);
				if (res != null) {
					try {
						aluM1 = new ALUMatrix(res, candBases, 1.0f, 1.0f, 0.95f, lineTolerance);
						return true;
					} catch (RetroTectorException re) {
					}
				}
			}
		}
*/
		return false;
	} // end of likeLINE(float)
  
/**
* @return	candidateFirst, or candidateSimilarityStart if >= 0.
*/
  public final int getVeryFirst() {
		if (candidateSimilarityStart >= 0) {
			return candidateSimilarityStart;
		} else {
			return candidateFirst;
		}
  } // end of getVeryFirst()
	
/**
* @return	candidateLast, or candidateSimilarityEnd if >= 0.
*/
  public final int getVeryLast() {
		if (candidateSimilarityEnd >= 0) {
			return candidateSimilarityEnd;
		} else {
			return candidateLast;
		}
  } // end of getVeryLast()
	
/**
* @return	candidateFactor (at present).
*/
  public final float getFactor() {
    return candidateFactor;
  } // end of getFactor()
	
/**
* @param	cand	An LTRCandidate
* @return	true if candidateFactor, hotSpotPosition, candidateFirst and candidateLast are the same.
*/
	public final boolean similar(LTRCandidate cand) {
		return ((candidateFactor == cand.candidateFactor) & (hotSpotPosition == cand.hotSpotPosition) & (candidateFirst == cand.candidateFirst) & (candidateLast == cand.candidateLast));
	} // end of similar(LTRCandidate)
  
/**
* As required by Scorable.
* @return	hotSpotPosition (for ordering by position).
*/
  public float fetchScore() {
    return 1.0f * hotSpotPosition;
  } // end of fetchScore()
  
/**
* Searches for repeats at least 10 long inside this, with no more than 10% errors.
* @return	Number of repeats found.
*/
  public final int makeDirectRepeats() throws RetroTectorException {
    Stack reps = new Stack(); // for found Repeats
    int minlength = 10;
		boolean found;
    byte [ ] pattern = new byte[minlength];
    for (int pos = candidateFirst; pos <= candidateLast - minlength; pos++) {
			RetroTectorEngine.showProgress();
      for (int p=0; p<minlength; p++) {
        pattern[p] = LTRCANDIDATEDNA.get2bit(pos + p);
      }
      AgrepContext context = new AgrepContext(pattern, pos + minlength, candidateLast - minlength, 1);
      int po; // + or - start of second component
			int s1; // start of first component
			int s2; // start of second component
			int le; // current estimate of repeat length
			int err; // number of mismatches
      while ((po = LTRCANDIDATEDNA.agrepFind(context)) != Integer.MIN_VALUE) { // search for second component with max 1 error
        s1 = pos;
        le = minlength;
        if (po >= 0) {
          s2 = po;
          err = 0;
        } else {
          s2 = -po;
          err = 1;
          if (LTRCANDIDATEDNA.get2bit(s2) != LTRCANDIDATEDNA.get2bit(s1)) { // is error at start?
            s2++;
            s1++;
            le--;
            err--;
          }
        }
// extend to the left. Can it happen?
        while ((s1 > candidateFirst) && (LTRCANDIDATEDNA.get2bit(s1 - 1) == LTRCANDIDATEDNA.get2bit(s2 - 1))) {
          s1--;
          s2--;
          le++;
        }
 // extend to the right
        while (((s2 + le) <= candidateLast) && ((LTRCANDIDATEDNA.get2bit(s2 + le) == LTRCANDIDATEDNA.get2bit(s1 + le)) | ((err + 1) <= 0.1f * (le + 1)))) {
          if (LTRCANDIDATEDNA.get2bit(s2 + le) != LTRCANDIDATEDNA.get2bit(s1 + le)) {
            err++;
          }
          le++;
        }

        Repeat re = new Repeat(s1, s2, le);
        found = false;
 // is there an identical repeat?
        for (int i=0; (i<reps.size()) & !found; i++) {
          if (re.doesEqual((Repeat) reps.elementAt(i))) {
            found = true;
          }
        }
        if (!found) {// no, include it
          reps.push(re);
					if (reps.size() >= MAXREPEATS) {
						directrepeats = new Repeat[reps.size()];
						reps.copyInto(directrepeats);
						return(directrepeats.length);
					}
        }
      }
    }
    directrepeats = new Repeat[reps.size()];
    reps.copyInto(directrepeats);
    return(directrepeats.length);
  } // end of makeDirectRepeats
  
  
/**
* @return	Summary on one line.
*/
  public String toString() {
    return "" + LTRCANDIDATEDNA.externalize(hotSpotPosition) + " " + 
        LTRCANDIDATEDNA.externalize(candidateFirst) + " " + 
        LTRCANDIDATEDNA.externalize(candidateLast) + " " + 
        Utilities.formattedNumber(candidateFactor, 1, 3) + " " + 
        candidateVirusGenus + " " + candidateComment;
  } // end of toString()
  
/**
* @return	A full, multiline description.
*/
  public final String[ ] toStrings() throws RetroTectorException {
    Stack lines = new Stack();

    lines.push("  " + FACTORKEY + ": " + Utilities.formattedNumber(candidateFactor, 1, 3) );
    lines.push("  " + VIRUSGENUSKEY + ": " + candidateVirusGenus );
		if (candidateSimilarityStart >= 0) {
			lines.push("  " + CANDIDATESIMILARITYSTARTKEY + ": " + LTRCANDIDATEDNA.externalize(candidateSimilarityStart) );
		}
    lines.push("  " + CANDIDATEFIRSTKEY + ": " + LTRCANDIDATEDNA.externalize(candidateFirst) );
    lines.push("  " + CANDIDATELASTKEY + ": " + LTRCANDIDATEDNA.externalize(candidateLast) );
		if (candidateSimilarityEnd >= 0) {
			lines.push("  " + CANDIDATESIMILARITYENDKEY + ": " + LTRCANDIDATEDNA.externalize(candidateSimilarityEnd) );
		}

    lines.push("  " + HOTSPOTKEY + ": " + Utilities.twoDecimals(hotSpotScore) + " " + Utilities.twoDecimals(hotSpotMax) + " " + LTRCANDIDATEDNA.externalize(hotSpotPosition) + " " + hotSpotType);
    lines.push("  " + U5NNKEY + ": " + Utilities.twoDecimals(u5Score) + " " + Utilities.twoDecimals(u5Max) + " " + LTRCANDIDATEDNA.externalize(u5Position));
    lines.push("  " + GTKEY + ": " + Utilities.twoDecimals(gtScore) + " " + Utilities.twoDecimals(gtMax));
    lines.push("  " + U3NNKEY + ": " + Utilities.twoDecimals(u3Score) + " " + Utilities.twoDecimals(u3Max) + " " + LTRCANDIDATEDNA.externalize(u3Position));
    lines.push("  " + TATAAKEY + ": " + Utilities.twoDecimals(tataaScore) + " " + Utilities.twoDecimals(tataaMax) + " " + LTRCANDIDATEDNA.externalize(tataaPosition));
    lines.push("  " + MEME50KEY + ": " + Utilities.twoDecimals(mEME50Score) + " " + Utilities.twoDecimals(mEME50Max) + " " + LTRCANDIDATEDNA.externalize(mEME50Position));
    lines.push("  " + MOT1KEY + ": " + Utilities.twoDecimals(mot1Score) + " " + Utilities.twoDecimals(mot1Max) +  " " + LTRCANDIDATEDNA.externalize(mot1Position));
    lines.push("  " + MOT2KEY + ": " + Utilities.twoDecimals(mot2Score) + " " + Utilities.twoDecimals(mot2Max) +  " " + LTRCANDIDATEDNA.externalize(mot2Position));
    lines.push("  " + TRANSKEY + ": " + Utilities.twoDecimals(transScore) + " " + Utilities.twoDecimals(transMax) );
    lines.push("  " + CPGKEY + ": " + Utilities.twoDecimals(cpgScore) + " " + Utilities.twoDecimals(cpgMax) );
    lines.push("  " + SPL8KEY + ": " + Utilities.twoDecimals(spl8Score) + " " + Utilities.twoDecimals(spl8Max) );
    lines.push("  " + SHORTDESCIPTIONKEY + ": " + candidateComment );
    if (candidateLimiters.length() > 0) {
			lines.push("  " + LIMITERSKEY + ": " + candidateLimiters );
		}
    
    if (directrepeats != null) {
			for (int j=0; j<directrepeats.length; j++) {
				lines.push("  " + DIRECTREPEATKEY + (j + 1) + ": " + directrepeats[j].toString(LTRCANDIDATEDNA));
			}
		}
		
		if (RetroTectorEngine.getClusterMode()) {
			repBaseFinds = Utilities.repBaseFind(LTRCANDIDATEDNA, getVeryFirst(), getVeryLast(), LTRCANDIDATEDATABASE.getRepBaseTemplates(), true);
			repBaseFinds = repBaseFinds.replace('\n', '\t');
			lines.push("  " + REPBASEFINDSKEY + ": " + repBaseFinds);
		}
    
    String[ ] result = new String[lines.size()];
    lines.copyInto(result);
    return result;
    
  } // end of toStrings()
	
/**
* @return		True if candidateFirst, candidateLast, hotSpotPosition and candidateFactor are equal.
*/
	public boolean equals(Object o) {
		LTRCandidate c = (LTRCandidate) o;
		return (c.candidateFirst == candidateFirst) & (c.candidateLast == candidateLast) & (c.hotSpotPosition == hotSpotPosition) & (c.candidateFactor == candidateFactor);
	} // end of equals(Object)

/**
* Compatible with equals.
*/
	public int hashCode() {
		return candidateFirst - candidateLast + hotSpotPosition;
	} // end of hashCode()
  
} // end of LTRCandidate

