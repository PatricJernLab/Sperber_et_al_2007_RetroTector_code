/*
* Copyright (©) 2000-2005, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 15/5 -05
* Beautified 15/5 -05
*/
package builtins;

import retrotector.*;
import java.util.*;
import java.io.*;

/**
* Executor to identify a Chain as a possible pseudogene.
* It should be executed throgh a script created by RetroVID.
* It searches for a structure of the type
* TSD-R....R-A-rich tail-TSD.
* It also searches for hits by RMotif inside thr Rs.
*<PRE>
*     Parameters:
*
*   Tolerance
* Tolerance parameter in R aligning, similar to LTRepTolerance in LTRID.
* Default: 3.
*
*</PRE>
*
*/

public class PseuGID extends Executor {

/**
* A structure fulfilling the requirements.
*/
	class PseudoGene {
	
/**
* First position in first TSD.
*/
		public final int TSD1FIRST;
	
/**
* Last position in first TSD.
*/
		public final int TSD1LAST;
	
/**
* First position in first R.
*/
		public final int R1FIRST;
	
/**
* First position in RMotif inside first R.
*/
		public final int R1MOTFIRST;
	
/**
* Last position in RMotif inside first R.
*/
		public final int R1MOTLAST;

/**
* Last position in first R.
*/
		public final int R1LAST;
	
/**
* First position in second R.
*/
		public final int R2FIRST;
	
/**
* First position in RMotif inside second R.
*/
		public final int R2MOTFIRST;
	
/**
* Last position in RMotif inside second R.
*/
		public final int R2MOTLAST;
	
/**
* Last position in second R.
*/
		public final int R2LAST;
	
/**
* First position in A-tail.
*/
		public final int TAILFIRST;
	
/**
* Last position in A-tail.
*/
		public final int TAILLAST;
	
/**
* First position in second TSD.
*/
		public final int TSD2FIRST;
	
/**
* Last position in second TSD.
*/
		public final int TSD2LAST;
		
/**
* Estimate of goodness.
*/
		public final float SCORE;
		
/**
* Constructor.
* @param	tsd1First		->TSD1FIRST
* @param	tsd1Last		->TSD1LAST
* @param	r1First			->R1FIRST
* @param	r1hotspot		Position of aaTaaa in first R
* @param	r1Last			->R1LAST
* @param	r2First			->R2FIRST
* @param	r2hotspot		Position of aaTaaa in second R
* @param	r2Last			->R2LAST
* @param	tsd2First		->TSD2FIRST
* @param	tsd2Last		->TSD2LAST
* @param	tsdscore		Estimate of TSD similarity
*/
		PseudoGene(int tsd1First, int tsd1Last, int r1First, int r1hotspot, int r1Last, int r2First, int r2hotspot, int r2Last, int tsd2First, int tsd2Last, float tsdscore) throws RetroTectorException {
		
			if (tsd1Last < tsd1First + MINTSDLENGTH - 1) {
				throw new RetroTectorException("PseudoGene", "Too short TSD1");
			}
			if (r1First <= tsd1Last) {
				throw new RetroTectorException("PseudoGene", "TSD1-R1 overlap");
			}
			if (r1Last < r1First + RMINTRAILING + RMINLEADING) {
				throw new RetroTectorException("PseudoGene", "Too short R1");
			}
			if (r2First <= r1Last) {
				throw new RetroTectorException("PseudoGene", "R1-R2 overlap");
			}
			if (r2Last < r2First + RMINTRAILING + RMINLEADING) {
				throw new RetroTectorException("PseudoGene", "Too short R2");
			}
			if (tsd2Last < tsd2First + MINTSDLENGTH - 1) {
				throw new RetroTectorException("PseudoGene", "Too short TSD2");
			}					
		
			if ((tsd2First - r2Last) <= MINTAILLENGTH) {
				throw new RetroTectorException("PseudoGene", "No room for tail");
			}
		
			TSD1FIRST = tsd1First;
			TSD1LAST = tsd1Last;
			TSD2FIRST = tsd2First;
			TSD2LAST = tsd2Last;
			
			int tailFirst;
			int tailLast;
// find first a
			for (tailFirst=r2Last+1; (tailFirst<TSD2FIRST) && (targetDNA.getBase(tailFirst) != 'a'); tailFirst++);
// find last a
			for (tailLast=TSD2FIRST-1; (tailLast>tailFirst) && (targetDNA.getBase(tailLast) != 'a'); tailLast--);
			int a = 0;
// count a
			for (int ai=tailFirst; ai<=tailLast; ai++) {
				if (targetDNA.getBase(ai) == 'a') {
					a++;
				}
			}
			float sc = 0;
			rmot.setEnds(r1First, r1Last);
			rmot.refresh(new Motif.RefreshInfo(targetDNA, 0, 0, null));
			MotifHit mh = rmot.getMotifHitAt(r1hotspot);
			if (mh != null) {
				sc = mh.MOTIFHITSCORE;
				R1MOTFIRST = mh.MOTIFHITFIRST;
				R1MOTLAST = mh.MOTIFHITLAST;
			} else {
				R1MOTFIRST = r1First;
				R1MOTLAST = r1Last;
			}
			R1FIRST = r1First;
			R1LAST = r1Last;
			rmot.setEnds(r2First, r2Last);
			mh = rmot.getMotifHitAt(r2hotspot);
			if (mh != null) {
				sc += mh.MOTIFHITSCORE;
				R2MOTFIRST = mh.MOTIFHITFIRST;
				R2MOTLAST = mh.MOTIFHITLAST;
			} else {
				R2MOTFIRST = r2First;
				R2MOTLAST = r2Last;
			}
			R2FIRST = r2First;
			R2LAST = r2Last;
			SCORE = sc * tsdscore * a / (R1FIRST - TSD1LAST + 1 + TSD2FIRST - R2LAST + 1);
			TAILFIRST = tailFirst;
			TAILLAST = tailLast;
			
		} // end of PseudoGene(int, int, int, int, int, int, int, int, int, int, float)
		
/**
* Outputs description of this PseudoGene.
* @param	wr				The ParameterFileWriter to output to.
* @param	bestpos5	Hotspot position of first R.
* @param	bestpos5	Hotspot position of second R.
*/
		public void toWriter(ParameterFileWriter wr, int bestpos5, int bestpos3) throws RetroTectorException {
		
			wr.writeSingleParameter("TSD1first", "" + targetDNA.externalize(TSD1FIRST), false);
			wr.writeSingleParameter("TSD1", targetDNA.subString(TSD1FIRST, TSD1LAST, true), false);
			wr.writeSingleParameter("TSD1last", "" + targetDNA.externalize(TSD1LAST), false);
			wr.writeSingleParameter("R1first", "" + targetDNA.externalize(R1FIRST), false);
			wr.writeSingleParameter("R1hotspot", "" + targetDNA.externalize(bestpos5), false);
			wr.writeSingleParameter("R1",
					targetDNA.subString(R1FIRST, R1MOTFIRST - 1, true) + " " +
					targetDNA.subString(R1MOTFIRST, R1MOTLAST, true) + " " +
					targetDNA.subString(R1MOTLAST + 1, R1LAST, true), false);
			wr.writeSingleParameter("R1last", "" + targetDNA.externalize(R1LAST), false);
			wr.writeSingleParameter("R2first", "" + targetDNA.externalize(R2FIRST), false);
			wr.writeSingleParameter("R2hotspot", "" + targetDNA.externalize(bestpos3), false);
			wr.writeSingleParameter("R2",
					targetDNA.subString(R2FIRST, R2MOTFIRST - 1, true) + " " +
					targetDNA.subString(R2MOTFIRST, R2MOTLAST, true) + " " +
					targetDNA.subString(R2MOTLAST + 1, R2LAST, true), false);
			wr.writeSingleParameter("R2last", "" + targetDNA.externalize(R2LAST), false);
			wr.writeSingleParameter("Tailfirst", "" + targetDNA.externalize(TAILFIRST), false);
			wr.writeSingleParameter("Tail", targetDNA.subString(TAILFIRST, TAILLAST, true), false);
			wr.writeSingleParameter("Taillast", "" + targetDNA.externalize(TAILLAST), false);
			wr.writeSingleParameter("TSD2first", "" + targetDNA.externalize(TSD2FIRST), false);
			wr.writeSingleParameter("TSD2", targetDNA.subString(TSD2FIRST, TSD2LAST, true), false);
			wr.writeSingleParameter("TSD2last", "" + targetDNA.externalize(TSD2LAST), false);
			wr.writeSingleParameter("Score", "" + SCORE, false);
		} //  end of toWriter
	
	} // end of PseudoGene
	
/**
* Dynamic matrix to align two TSD or R candidates, one possibly preceded by an a-rich tail.
*/
	class TSDMatrix {
	
/**
* First internal position in first TSD.
*/
		public final int FIRST1;
	
/**
* Last internal position in first TSD.
*/
		public final int LAST1;
	
/**
* First internal position in second TSD.
*/
		public final int FIRST2;

/**
* First internal position in second TSD.
*/
		public final int LAST2;

/**
* Highest cell score in matrix.
*/
		public final float SCORE;

/**
* Bonus for aligned step, or a in tail,
*/
		public final float GOOD = 3f;

/**
* Penalty for nonaligned step, or non-a in tail,
*/
		public final float BAD = -5.5f;

		private final int ACODE = Compactor.BASECOMPACTOR.charToIntId('a');

		
/**
* Constructor.
* @param	seq1	First base sequence to align.
* @param	seq2	Second base sequence to align.
* @param	tail	If true, seq2 should start with an a-rich tail.
*/
		TSDMatrix(int[ ] seq1, int[ ] seq2, boolean tail) {
		
			float[ ][ ] matrix = new float[seq1.length][seq2.length];
			
			float topscore = -Float.MAX_VALUE;
			int best1 = -1;
			int best2 = -1;
			float x;
			
// find highest cell score going forwards
			for (int i2=1; i2<seq2.length; i2++) {
				if (tail) {
					if (seq2[i2] == ACODE) {
						matrix[0][i2] = matrix[0][i2 - 1] + GOOD;
					} else {
						matrix[0][i2] = matrix[0][i2 - 1] + BAD;
					}
				} else {
					matrix[0][i2] = matrix[0][i2 - 1] + BAD;
				}
				for (int i1=1; i1<seq1.length; i1++) {
					x = matrix[i1 - 1][i2 - 1];
					if (seq1[i1] == seq2[i2]) {
						x += GOOD;
					} else {
						x += BAD;
					}
					x = Math.max(x, matrix[i1][i2 - 1] + BAD);
					x = Math.max(x, matrix[i1 - 1][i2] + BAD);
					matrix[i1][i2] = x;
					if (x > topscore) {
						topscore = x;
						best1 = i1;
						best2 = i2;
					}
				}
			}
			LAST1 = best1;
			LAST2 = best2;
			
// find highest cell score going backwards again
			matrix = new float[LAST1 + 1][LAST2 + 1];
			topscore = -Float.MAX_VALUE;
			best1 = -1;
			best2 = -1;
			float[ ] sequ1 = new float[LAST1 + 1];
			for (int iu1=0; iu1<=LAST1; iu1++) {
				sequ1[iu1] = seq1[LAST1 - iu1];
			}
			float[ ] sequ2 = new float[LAST2 + 1];
			for (int iu2=0; iu2<=LAST2; iu2++) {
				sequ2[iu2] = seq2[LAST2 - iu2];
			}
			for (int i2=1; i2<sequ2.length; i2++) {
				matrix[0][i2] = i2 * BAD;
			}
			for (int i1=1; i1<sequ1.length; i1++) {
				matrix[i1][0] = i1 * BAD;
				for (int i2=1; i2<sequ2.length; i2++) {
					x = matrix[i1 - 1][i2 - 1];
					if (sequ1[i1] == sequ2[i2]) {
						x += GOOD;
					} else {
						x += BAD;
					}
					x = Math.max(x, matrix[i1][i2 - 1] + BAD);
					x = Math.max(x, matrix[i1 - 1][i2] + BAD);
					matrix[i1][i2] = x;
					if (x > topscore) {
						topscore = x;
						best1 = i1;
						best2 = i2;
					}
				}
			}
			FIRST1 = LAST1 - best1;
			FIRST2 = LAST2 - best2;
			SCORE = topscore;
		}
	} // end of TSDMatrix
	
	
	
/**
* Key for tolerance parameter (roof) in LTRMatrix used to align Rs = "Tolerance".
*/
	public final static String ROOFKEY = "Tolerance";

/**
* Least acceptable length of a-rich tail = 10.
*/
	public final static int MINTAILLENGTH = 10;

/**
* Largest acceptable length of a-rich tail = 40.
*/
	public final static int MAXTAILLENGTH = 40;

/**
* Least acceptable length of TSD = 5.
*/
	public final static int MINTSDLENGTH = 5;

/**
* Largest acceptable length of TSD = 16.
*/
	public final static int MAXTSDLENGTH = 16;

/**
* Least acceptable length of R after hotspot.
*/
	public final int RMINTRAILING;

/**
* Largest acceptable length of R after hotspot.
*/
	public final int RMAXTRAILING;

/**
* Least acceptable length of R before hotspot.
*/
	public final int RMINLEADING;

/**
* Largest acceptable length of R before hotspot.
*/
	public final int RMAXLEADING;
	
	
	private float roof = 3;
	private DNA targetDNA;
	private ChainGraphInfo chainInfo;
	private String[ ] keyList;
	private String chainKey;
	private PseudoGene bestpg = null;
	private Database database;
	private Range ltr5Range;
	private Range ltr3Range;
	private RMotif rmot;

/**
* Standard Executor constructor.
*/
	public PseuGID() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(ROOFKEY, "3");
		explanations.put(ROOFKEY, "");
		orderedkeys.push(ROOFKEY);
		try {
			rmot = (RMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif("RMotif");
		} catch (RetroTectorException e) {
		}
		RMINTRAILING = rmot.MINTRAILERLENGTH;
		RMAXTRAILING = rmot.MAXTRAILERLENGTH;
		RMINLEADING = rmot.MINLEADERLENGTH;
		RMAXLEADING = rmot.MAXLEADERLENGTH;
	} // end of constructor
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "05 05 15";
  } //end of version
	
/**
* Performs normal initialization.
* @param	script	The script to fetch from, or null.
* @return	True if successful.
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		runFlag = false;
		getDefaults();
		interactive = false;
		script.setTable(parameters);
		script.readParameters();
		keyList = script.close();
		parameters.put(SCRIPTPATHKEY, script.FILEPATH);
		runFlag = true;
	} // end of initialize
	
/**
* @param	pos5	Internal position of hotspot of first R.
* @param	pos3	Internal position of hotspot of second R.
* @return	a PseudoGene as specified, or null.
*/
	PseudoGene getPseudoGene(int pos5, int pos3) throws RetroTectorException {
//		System.out.println(targetDNA.externalize(pos5) + "  " + targetDNA.externalize(pos3));
// earliest start position of 5'LTR
    int delimiter5 = targetDNA.forceInside(pos5 - RMAXLEADING);
// earliest start position of 3'LTR
    int delimiter3 = targetDNA.forceInside(pos3 - RMAXLEADING);
// make arrays of bases backwards from hotspot
    int[ ] lead5 = new int[pos5 - delimiter5];
    int[ ] lead3 = new int[pos3 - delimiter3];
    for (int i=0; i<lead5.length; i++) {
      lead5[i] = targetDNA.get2bit(pos5 - i);
    }
    for (int ii=0; ii<lead3.length; ii++) {
      lead3[ii] = targetDNA.get2bit(pos3 - ii);
    }

		LTRMatrix leadmat = new LTRMatrix(lead5, lead3, 1.0f, 1.0f, 0.95f, roof);
// is similarity good enough?
    if ((leadmat.SEQ1END < RMINLEADING) | (leadmat.SEQ2END < RMINLEADING)) {
      return null;
    }

// latest end position of 5'LTR
    delimiter5 = targetDNA.forceInside(pos5 + RMAXTRAILING);
// latest end position of 3'LTR
    delimiter3 = targetDNA.forceInside(pos3 + RMAXTRAILING);
// make arrays of bases forwards from hotspot
    int[ ] trail5 = new int[delimiter5 - pos5];
    int[ ] trail3 = new int[delimiter3 - pos3];
    for (int i=0; i<trail5.length; i++) {
      trail5[i] = targetDNA.get2bit(pos5 + i);
    }
    for (int ii=0; ii<trail3.length; ii++) {
      trail3[ii] = targetDNA.get2bit(pos3 + ii);
    }
    
// check similarity
    LTRMatrix trailmat = new LTRMatrix(trail5, trail3, 1.0f, 1.0f, 0.95f, roof);
// is similarity good enough?
    if ((trailmat.SEQ1END < RMINTRAILING) | (trailmat.SEQ2END < RMINTRAILING)) {
      return null;
    }
		
// Rs are similar enough, seek TSDs
		int offset1 = targetDNA.forceInside(pos5 - leadmat.SEQ1END - MAXTSDLENGTH - 5);
		int[ ] seq1 = new int[MAXTSDLENGTH + 10];
		for (int i=0; i<seq1.length; i++) {
			seq1[i] = targetDNA.get2bit(offset1 + i);
		}
		int offset2 = pos3 + trailmat.SEQ2END - 5;
		int[ ] seq2 = new int[Math.min(MAXTAILLENGTH + MAXTSDLENGTH + 10, targetDNA.LENGTH - offset2 - 1)];
		for (int i=0; i<seq2.length; i++) {
			seq2[i] = targetDNA.get2bit(offset2 + i);
		}
		TSDMatrix mat = new TSDMatrix(seq1, seq2, true);
// is score reasonably good?
		if (mat.SCORE < mat.GOOD * (MINTSDLENGTH - 1)) {
			return null;
		}
		
// TSDs OK
		int tsd1Last = offset1 + mat.LAST1;
		int tsd2Last = offset2 + mat.LAST2;
		int tsd1First = offset1 + mat.FIRST1;
		int tsd2First = offset2 + mat.FIRST2;
		
// align Rs
		seq1 = new int[leadmat.SEQ1END + trailmat.SEQ1END + 1];
		offset1 = pos5 - leadmat.SEQ1END;
		for (int i=0; i<seq1.length; i++) {
			seq1[i] = targetDNA.get2bit(offset1 + i);
		}
		seq2 = new int[leadmat.SEQ2END + trailmat.SEQ2END + 1];
		offset2 = pos3 - leadmat.SEQ2END;
		for (int i=0; i<seq2.length; i++) {
			seq2[i] = targetDNA.get2bit(offset2 + i);
		}
		TSDMatrix mat2 = new TSDMatrix(seq1, seq2, false);

		int r1Last = offset1 + mat2.LAST1;
		int r2Last = offset2 + mat2.LAST2;
		int r1First = offset1 + mat2.FIRST1;
		int r2First = offset2 + mat2.FIRST2;

// try to make PseudoGene according to specifications
		PseudoGene bestpg = null;
		try {
			bestpg = new PseudoGene(tsd1First, tsd1Last, r1First, pos5, r1Last, r2First, pos3, r2Last, tsd2First, tsd2Last, mat.SCORE * mat2.SCORE);
		} catch (RetroTectorException e) {
		}
//		System.out.println("!" + bestpg);
		return bestpg;
	} // end of getPseudoGene(int, int)
			
// push r on s if it is not already there
	private void stackRange(Stack s, Range r) {
		if (s.size() == 0) {
			s.push(r);
			return;
		}
		for (int i=0; i<s.size(); i++) {
			if (r.doesEqual((Range) s.elementAt(i))) {
				return;
			}
		}
		s.push(r);
	} // end of stackRange

/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
	
		database = Database.getDatabase(getString(DATABASEKEY, ORDINARYDATABASE));
		roof = getFloat(ROOFKEY, 3.0f);
		
		for (int i=0; i<keyList.length; i++) {
			chainKey = keyList[i];
			if (chainKey.startsWith(ParameterFileReader.CHAINP)) {
				targetDNA = getDNA(getString(DNAFILEKEY, ""), true);
				chainInfo = new ChainGraphInfo(getStringArray(chainKey), targetDNA);
			} else if (chainKey.startsWith(ParameterFileReader.CHAINS)) {
				targetDNA = getDNA(getString(DNAFILEKEY, ""), false);
				chainInfo = new ChainGraphInfo(getStringArray(chainKey), targetDNA);
			}
		}
		
// find Ranges for LTR hotspots compatible with Chain
		SubGene ltr5 = database.getSubGene(Executor.LTR5KEY);
		SubGene ltr3 = database.getSubGene(Executor.LTR3KEY);
		SubGene sg;
		Range r;
		Range rr;
		sg = database.getSubGene(chainInfo.subgeneinfo[0].SUBGENENAME);
		r = new Range(Range.UNDEFINED, chainInfo.subgeneinfo[0].HITHOTSPOT, sg.distanceTo(ltr5));
		for (int i=1; i<chainInfo.subgeneinfo.length; i++) {
			sg = database.getSubGene(chainInfo.subgeneinfo[i].SUBGENENAME);
			rr = new Range(Range.UNDEFINED, chainInfo.subgeneinfo[i].HITHOTSPOT, sg.distanceTo(ltr5));
			rr = Range.consensus(rr, r);
			if (rr != null) {
				r = rr;
			}
		}
		ltr5Range = r;
		int l = chainInfo.subgeneinfo.length - 1;
		sg = database.getSubGene(chainInfo.subgeneinfo[l].SUBGENENAME);
		r = new Range(Range.UNDEFINED, chainInfo.subgeneinfo[l].HITHOTSPOT, sg.distanceTo(ltr3));
		for (int i=l-1; i>=0; i--) {
			sg = database.getSubGene(chainInfo.subgeneinfo[i].SUBGENENAME);
			rr = new Range(Range.UNDEFINED, chainInfo.subgeneinfo[i].HITHOTSPOT, sg.distanceTo(ltr3));
			rr = Range.consensus(rr, r);
			if (rr != null) {
				r = rr;
			}
		}
		ltr3Range = r;
		
// get start of first non-LTR motif hit
		int r7 = chainInfo.FIRSTBASEPOS - 1;
		if (chainInfo.subgeneinfo[0].SUBGENENAME.equals(LTR5KEY)) {
			if ((chainInfo.subgeneinfo.length < 2) || (chainInfo.subgeneinfo[1].SUBGENENAME.equals(LTR3KEY))) {
				RetroTectorEngine.displayError(new RetroTectorException("PseuGID", "Only LTRs in Chain in " + getString(SCRIPTPATHKEY, "")), RetroTectorEngine.WARNINGLEVEL);
				return "Only LTRs";
			} else {
				r7 = chainInfo.subgeneinfo[1].motifhitinfo[0].FIRSTPOS - 1;
			}
		}
// acceptable range for first R hotspot
		Range sign1Range = new Range(Range.UNDEFINED, Math.max(5, ltr5Range.RANGEMIN-Chain.MAXINSERTION), r7);
		
// get end of last non-LTR motif hit
		r7 = chainInfo.LASTBASEPOS + 1;
		int r8 = chainInfo.subgeneinfo.length - 1;
		if (chainInfo.subgeneinfo[r8].SUBGENENAME.equals(LTR3KEY)) {
			if ((chainInfo.subgeneinfo.length < 2) || (chainInfo.subgeneinfo[r8 - 1].SUBGENENAME.equals(LTR5KEY))) {
				RetroTectorEngine.displayError(new RetroTectorException("PseuGID", "Only LTRs in Chain in " + getString(SCRIPTPATHKEY, "")), RetroTectorEngine.WARNINGLEVEL);
				return "Only LTRs";
			} else {
				r7 = chainInfo.subgeneinfo[r8 - 1].motifhitinfo[0].LASTPOS + 1;
			}
		}
// acceptable range for second R hotspot
		Range sign2Range = new Range(Range.UNDEFINED, r7, Math.min(ltr3Range.RANGEMAX+Chain.MAXINSERTION, targetDNA.LENGTH - 6));
		
		Stack rangeStack = new Stack();
		byte[ ] pattern;
		AgrepContext context;
		int po;
// search for 11 base repeats with at least one polyadenylation signal
		for (int pos5 = sign1Range.RANGEMIN; pos5<=sign1Range.RANGEMAX; pos5++) {
			if (targetDNA.polyASignalAt(pos5)) {
				pattern = new byte[11];
				for (int i=0; i<11; i++) {
					pattern[i] = (byte) targetDNA.get2bit(pos5 - 5 + i);
				}
				context = new AgrepContext(pattern, sign2Range.RANGEMIN - 5, sign2Range.RANGEMAX - 5, 1);
				while ((po = targetDNA.agrepFind(context)) != Integer.MIN_VALUE) {
					stackRange(rangeStack, new Range(Range.UNDEFINED, pos5, Math.abs(po) + 5));
				}
			}
		}
		for (int pos3 = sign2Range.RANGEMIN; pos3<=sign2Range.RANGEMAX; pos3++) {
			if (targetDNA.polyASignalAt(pos3)) {
				pattern = new byte[11];
				for (int i=0; i<11; i++) {
					pattern[i] = (byte) targetDNA.get2bit(pos3 - 5 + i);
				}
				context = new AgrepContext(pattern, sign1Range.RANGEMIN - 5, sign1Range.RANGEMAX - 5, 1);
				while ((po = targetDNA.agrepFind(context)) != Integer.MIN_VALUE) {
					stackRange(rangeStack, new Range(Range.UNDEFINED, Math.abs(po) + 5, pos3));
				}
			}
		}
		
		PseudoGene temppg;
		PseudoGene pg = null;
		int bestpos5 = -1;
		int bestpos3 = -1;
		Range ra;
		for (int ran=0; ran<rangeStack.size(); ran++) {
			ra = (Range) rangeStack.elementAt(ran);
			temppg = getPseudoGene(ra.RANGEMIN, ra.RANGEMAX);
			if (temppg != null) {
				if ((pg == null) || (temppg.SCORE > pg.SCORE)) {
					pg = temppg;
					bestpos5 = ra.RANGEMIN;
					bestpos3 = ra.RANGEMAX;
				}
			}
		}
			
		if ((pg != null) && (pg.SCORE > 0)) {
			File fi = Utilities.uniqueFile(RetroTectorEngine.currentDirectory(), "Pseudogene_" + chainKey.substring(5) + "_", FileNamer.TXTTERMINATOR);
			ParameterFileWriter wr = new ParameterFileWriter(fi);
			wr.writeSingleParameter(DNAFILEKEY, getString(DNAFILEKEY, ""), false);
			wr.writeMultiParameter(chainKey, getStringArray(chainKey), false);
			pg.toWriter(wr, bestpos5, bestpos3);
			wr.close();
		}

		return "";
	} // end of execute
		    
} // end of PseuGID

