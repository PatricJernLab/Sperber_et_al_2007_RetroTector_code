/*
* Copyright ((©)) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 13/11 -06
* Beautified 13/11 -06
*/
package builtins;

import retrotector.*;

import java.util.*;
import java.io.*;

	
/**
* Executor which attempts to construct a putein.
* May also subervise the simultaneous construction of several puteins, if the GeneScripts parameter is present.
* Generates one or more Putein_ files showing the putein, the reading frames and the motifs,
* and on request a more detailed ORFIDout_ file.
*<PRE>
*     Parameters, normally not set by script:
*
*  InputFile
* File to get parameters from. Only used in interactive mode.
* Default: ""
*
*   GeneScripts
* Only if the script is a "superscript". A list of ordinary scripts.
* Default: null.
*
*   Database
* Database subdirectory to use.
* Default: Ordinary.
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
* Default: -1.5
*
*  ORFHexamerFactor
* Factor to multiply ORFHexamerModifier by in unaligned fitting.
* Default: 0.2
*
*  NonORFHexamerFactor
* Factor to multiply NonORFHexamerFactor by in unaligned fitting.
* Default: -0.1
*
*  GlycosylationFactor
* Factor to multiply GlycSiteModifier by in unaligned fitting of env.
* Default: 0.2
*
*  InORFBonus
* Bonus for position in long ORF
* Default: 0.1
*
*  MotifHitFactor
* Factor to multiply score of Motif hits by in unaligned fitting.
* Default: 0.2
*
*  MasterSkipPenalty
* Penalty for skipping a filled column in alignment.
* Default: -0.2
*
*  StopCodonValue
* Value of stop codon in aligned fitting. Will become stopscoreInDNA argument in DNA.acidRawScoreOne().
* Default: -15
*
*  ScoreFactor
* Factor for discarding lowscoring MotifHits.
* Default: 0.1
*
*  MinSSScore
* Lowest acceptable sum of squared MotifHit scores.
* Default: 600
*
*  MinHitScore
* Lowest acceptable individual MotifHit score.
* Default: 15
*
* Parameters, normally set by script:
*  Gene
* gag, pro, pol or env
*
*  VirusGenus
* Any virus genus symbol defined in RVector.
*
*  DNAFile
* The file to read DNA from.
* Default: Directory name + .txt
*
*  Strand
* Primary or Secondary.
* Default: Primary.
*
*  FirstDNAStart
* Calculated earliest possible start of putein in DNA (external).
* Default: 1
*
*  LastDNAStart
* Calculated latest possible start of putein in DNA (external).
* Default: 1
*
*  FirstDNAEnd
* Calculated earliest possible end of putein in DNA (external).
* Default: 200000
*
*  LastDNAEnd
* Calculated latest possible end of putein in DNA (external).
* Default: 200000
*
*   HitInfo
* A list of Motif hits, according to MotifHitsInfo.
* Default: null.
*
*  OutputFile
* Name of file for detailed output (ORFIDview script).
* Constructed from name of script or input file.
*
*  PuteinFile
* Name of file for putein output (Puteinview script).
* Constructed from name of script or input file.
*
*  ChainNumber
* Number of parent Chain.
* Default: null.
*
*  Trouble
* If RetroVID had problems making this.
* Default: null.
*
*
*  Debugging
* If this is Yes, a lot of extra information is written to ORFIDOutput and Putein files.
* Default: No
*</PRE>
*/
public class ORFID extends Executor {

/**
* A collection of parameters relevant for Putein and DynamicMatrices.
*/
	public static class ParameterBlock {
	
/**
* The parent ORFID.
*/
		public final ORFID THEORFID;

/**
* The relevant DNA.
*/
 		public final DNA TARGETDNA;
	
/**
* The relevant Database.
*/
		public final Database DATABASE;
		
/**
* The relevant master.
*/
 		public final Alignment MASTER;
	
/**
* The relevant genename.
*/
 		public final String GENENAME;
	
/**
* Copy of genuschar.
*/
 		public final char GENUSCHAR;
	
/**
* The relevant hitsInfo.
*/
 		public final MotifHitsInfo HITSINFO;
	
/**
* Copy of nonAlignedScore.
*/
 		public final float NONALIGNEDSCORE;
	
/**
* Copy of stopCodonFactor.
*/
		public final float STOPCODONFACTOR;
	
/**
* Copy of frameShiftPenalty.
*/
		public final float FRAMESHIFTPENALTY;
	
/**
* Copy of orfHexamerFactor.
*/
		public final float ORFHEXAMERFACTOR;
	
/**
* Copy of nonOrfHexamerFactor.
*/
		public final float NONORFHEXAMERFACTOR;
	
/**
* Copy of glycSiteFactor.
*/
		public final float GLYCSITEFACTOR;
	
/**
* Copy of inORFbonus.
*/
		public final float INORFBONUS;
	
/**
* Copy of motifHitFactor.
*/
		public final float MOTIFHITFACTOR;
	
/**
* Copy of masterPenalty.
*/
		public final float MASTERPENALTY;
	
/**
* Copy of stopValue.
*/
		public final int STOPVALUE;

/**
* Positions in TARGETDNA to avoid.
*/
		public final int[ ][ ] AVOIDPOSITIONS;

/**
* Positions in ORFs at least 50 acids long.
*/
		public boolean[ ] INORF;
		
/**
* Constructor.
* @param	o	An ORFID to pick parameters from.
*/
		public ParameterBlock(ORFID o) {
			THEORFID = o;
			TARGETDNA = o.dna;
			DATABASE = o.database;
			MASTER = o.master;
			GENENAME = o.genename;
			GENUSCHAR = o.genusChar;
			HITSINFO = o.hitsInfo;
			NONALIGNEDSCORE = o.nonAlignedScore;
			STOPCODONFACTOR = o.stopCodonFactor;
			FRAMESHIFTPENALTY = o.frameShiftPenalty;
			ORFHEXAMERFACTOR = o.orfHexamerFactor;
			NONORFHEXAMERFACTOR = o.nonOrfHexamerFactor;
			GLYCSITEFACTOR = o.glycSiteFactor;
			INORFBONUS = o.inORFbonus;
			MOTIFHITFACTOR = o.motifHitFactor;
			MASTERPENALTY = o.masterPenalty;
			STOPVALUE = o.stopValue;
			AVOIDPOSITIONS = o.avoidPositions;
			INORF = TARGETDNA.getInORF(50);
		} // end of ParameterBlock.contructor(ORFID)
		
/**
* Constructor setting only TARGETDNA. Other parameters are nulls.
* @param	dna	To be TARGETDNA.
*/
		public ParameterBlock(DNA dna) {
			THEORFID = null;
			TARGETDNA = dna;
			DATABASE = null;
			MASTER = null;
			GENENAME = null;
			GENUSCHAR = ' ';
			HITSINFO = null;
			NONALIGNEDSCORE = 0;
			STOPCODONFACTOR = 0;
			FRAMESHIFTPENALTY = 0;
			ORFHEXAMERFACTOR = 0;
			NONORFHEXAMERFACTOR = 0;
			GLYCSITEFACTOR = 0;
			INORFBONUS = 0;
			MOTIFHITFACTOR = 0;
			MASTERPENALTY = 0;
			STOPVALUE = 0;
			AVOIDPOSITIONS = null;
			INORF = null;
		} // end of ParameterBlock.contructor(DNA)
		
	} // end of ParameterBlock


/**
* Parent class for classes managing choice of start and end sections of puteins.
*/
	abstract class Fitter {
	
/**
* To mark a Path as useless.
*/
		final float REJECTED = -Float.MAX_VALUE;

/**
* Max offset between putein start and slippery sequence in Pro beta.
*/
		final int PROBSHIFTEROFFSET = -30;

/**
* Max offset between putein start and slippery sequence in Pro lenti.
*/
		final int PROLSHIFTEROFFSET = -30;

/**
* Max offset between putein start and slippery sequence in Pol beta.
*/
		final int POLBSHIFTEROFFSET = -50;

/**
* Max offset between putein start and slippery sequence in Pol delta.
*/
		final int POLDSHIFTEROFFSET = -30;

/**
* Max offset between putein start and slippery sequence in Pol lenti.
*/
		final int POLLSHIFTEROFFSET = -30;

/**
* Max offset between putein start and acceptor site sequence in Env.
*/
		final int ENVACCEPTOROFFSET = -20;
		
/**
* For storage of normalized raw scores by ProteaseCleavageMotif.
*/
		Utilities.OffsetFloatArray cleavageScores;

/**
* For storage of scores for stop codons.
*/
		Utilities.OffsetFloatArray stopScores;

/**
* For storage of combined scores for ProteaseCleavageMotif and stop codons.
*/
		Utilities.OffsetFloatArray cleaveorstopScores;

/**
* For storage of normalized raw scores by kozakMotif.
*/
		Utilities.OffsetFloatArray kozakScores;

/**
* For storage of normalized raw scores by SlipperyMotif.
*/
		Utilities.OffsetFloatArray slipperyScores;

/**
* For storage of normalized raw scores by PseudoKnotMotif.
*/
		Utilities.OffsetFloatArray pseudoKnotScores;

/**
* For storage of combined scores by SlipperyMotif and PseudoKnotMotif.
*/
		Utilities.OffsetFloatArray shifterScores;

/**
* For storage of normalized raw scores by SpliceAcceptorMotif.
*/
		Utilities.OffsetFloatArray acceptorScores;

/**
* @param	trailPath	A PathClass to finalize.
* @return	True if the job was already done, false otherwise.
*/
		boolean scoreTrailer(PathClass trailPath) throws RetroTectorException {
			return false;
		} // end of Fitter.scoreTrailer(PathClass)
		
/**
* @param	leadPath	A PathClass to finalize.
* @return	True if the job was already done, false otherwise.
*/
		boolean scoreLeader(PathClass leadPath) throws RetroTectorException {
			return false;
		}// end of Fitter.scoreLeader((PathClass)
		
/**
* Resets offsetFloatArrays.
* @param	firstpos	The first internal position in DNA to try fitting on.
* @param	lastpos		The last internal position in DNA to try fitting on.
*/
		final void prepareScorers(int firstpos, int lastpos) {
			int slf = Math.min(0, PROBSHIFTEROFFSET);
			slf = Math.min(slf, PROLSHIFTEROFFSET);
			slf = Math.min(slf, POLBSHIFTEROFFSET);
			slf = Math.min(slf, POLDSHIFTEROFFSET);
			slf = Math.min(slf, POLLSHIFTEROFFSET);
			slipperyScores = new Utilities.OffsetFloatArray(firstpos + slf, lastpos);
			slipperyScores.fillWith(Float.NaN);
			pseudoKnotScores = new Utilities.OffsetFloatArray(firstpos + slf, Math.min(lastpos + MAXPSEUDOKNOTOFFSET, dna.LENGTH - 1));
			pseudoKnotScores.fillWith(Float.NaN);
			shifterScores = new Utilities.OffsetFloatArray(firstpos + slf, lastpos);
			shifterScores.fillWith(Float.NaN);
			kozakScores = new Utilities.OffsetFloatArray(firstpos, lastpos);
			kozakScores.fillWith(Float.NaN);
			cleavageScores = new Utilities.OffsetFloatArray(firstpos - 3, lastpos);
			cleavageScores.fillWith(Float.NaN);
			stopScores = new Utilities.OffsetFloatArray(firstpos - 3, lastpos);
			stopScores.fillWith(Float.NaN);
			acceptorScores = new Utilities.OffsetFloatArray(firstpos + ENVACCEPTOROFFSET, lastpos);
			acceptorScores.fillWith(Float.NaN);
			cleaveorstopScores = new Utilities.OffsetFloatArray(firstpos - 3, lastpos);
			cleaveorstopScores.fillWith(Float.NaN);
		} // end of Fitter.prepareScorers(int, int)
	
/**
* Resets offsetFloatArrays.
* @param	first	The first ORFID to try fitting on.
* @param	last	The second ORFID to try fitting on.
*/
		final void prepareScorers(ORFID first, ORFID last) {
			if ((first != null) & (last != null)) {
				prepareScorers(Math.min(first.hitinfos[first.hitinfos.length - 1].HOTSPOT + 4, last.dnaStartFirst), Math.max(last.hitinfos[0].HOTSPOT - 4, first.dnaEndLast));
			} else if (first != null) {
				prepareScorers(first.hitinfos[first.hitinfos.length - 1].HOTSPOT + 4, first.dnaEndLast);
			} else if (last != null) {
				prepareScorers(last.dnaStartFirst, last.hitinfos[0].HOTSPOT - 4);
			}
		} // end of Fitter.prepareScorers(ORFID, ORFID)
	
/**
* @param	posInDNA	Internal position in DNA.
* @return	Score for ProteaseCleavageMotif normalized to 1.
*/
		final float cleavagescore(int posInDNA) throws RetroTectorException {
			float xx = cleavageScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			xx = proteaseCleavageMotif.getRawScoreAt(posInDNA);
			if (!Float.isNaN(xx)) {
				cleavageScores.setValueAt(posInDNA, xx / proteaseCleavageMotif.getBestRawScore());
			} else {
				cleavageScores.setValueAt(posInDNA, 0);
			}
			return cleavageScores.getValueAt(posInDNA);
		} // end of Fitter.cleavagescore(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	Score for SpliceAcceptorMotif normalized to 1.
*/
		final float acceptorscore(int posInDNA) throws RetroTectorException {
			float xx = acceptorScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			xx = acceptorMotif.getRawScoreAt(posInDNA);
			if (!Float.isNaN(xx)) {
				acceptorScores.setValueAt(posInDNA, xx / acceptorMotif.getBestRawScore());
			} else {
				acceptorScores.setValueAt(posInDNA, 0);
			}
			return acceptorScores.getValueAt(posInDNA);
		} // end of Fitter.acceptorscore(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	1 if posInDNA at TAG, 0.5 if at other stop codon.
*/
		final float stopBonus(int posInDNA) {
			float xx = stopScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			int co = dna.get6bit(posInDNA);
			if (co == TAGCODON) {
				stopScores.setValueAt(posInDNA, 1.0f);
			} else if ((co == TAACODON) | (co == TGACODON)) {
				stopScores.setValueAt(posInDNA, 0.5f);
			} else {
				stopScores.setValueAt(posInDNA, 0);
			}
			return stopScores.getValueAt(posInDNA);
		} // end of Fitter.stopBonus(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	0.95 if posInDNA at stop codon, otherwise cleavagescore.
*/
		final float cleaveorstopscore(int posInDNA) throws RetroTectorException {
			float xx = cleaveorstopScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			xx = stopBonus(posInDNA);
			if (xx > 0) {
				xx = 0.95f;
			}
			float cl = cleavagescore(posInDNA);
			if (Float.isNaN(cl)) {
				cleaveorstopScores.setValueAt(posInDNA, xx);
			} else {
				cleaveorstopScores.setValueAt(posInDNA, Math.max(xx, cl));
			}
			return cleaveorstopScores.getValueAt(posInDNA);
		} // end of Fitter.cleaveorstopscore(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	Score for SlipperyMotif normalized to 1.
*/
		final float slipperyscore(int posInDNA) throws RetroTectorException {
			float xx = slipperyScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			xx = slipperyMotif.getRawScoreAt(posInDNA);
			if (!Float.isNaN(xx)) {
				slipperyScores.setValueAt(posInDNA, xx / slipperyMotif.getBestRawScore());
			} else {
				slipperyScores.setValueAt(posInDNA, 0);
			}
			return slipperyScores.getValueAt(posInDNA);
		} // end of Fitter.slipperyscore(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	Score for kozakMotif normalized to 1.
*/
		final float kozakscore(int posInDNA) throws RetroTectorException {
			if (posInDNA < 6) {
				return 0;
			}
			float xx = kozakScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			xx = kozakMotif.getRawScoreAt(posInDNA - 6);
			if (!Float.isNaN(xx)) {
				kozakScores.setValueAt(posInDNA, xx / kozakMotif.getBestRawScore());
			} else {
				kozakScores.setValueAt(posInDNA, 0);
			}
			return kozakScores.getValueAt(posInDNA);
		} // end of Fitter.kozakscore(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	1 if hit by PseudoKnotMotif at posInDNA.
*/
		final float pseudoknotscore(int posInDNA) throws RetroTectorException {
			float xx = pseudoKnotScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			MotifHit mh = pseudoKnotMotif.getMotifHitAt(posInDNA);
			if (mh != null) {
				pseudoKnotScores.setValueAt(posInDNA, 1.0f);
			} else {
				pseudoKnotScores.setValueAt(posInDNA, 0);
			}
			return pseudoKnotScores.getValueAt(posInDNA);
		} // end of Fitter.pseudoknotscore(int)
		
/**
* @param	posInDNA	Internal position in DNA.
* @return	Score for combined SlipperyMotif and PseudoKnotMotif normalized to 2.
*/
		final float shifterscore(int posInDNA) throws RetroTectorException {
			float xx = shifterScores.getValueAt(posInDNA);
			if (!Float.isNaN(xx)) {
				return xx;
			}
			xx = 0;
			for (int i=MINPSEUDOKNOTOFFSET; i<=MAXPSEUDOKNOTOFFSET; i++) {
				if (posInDNA + i <= pseudoKnotScores.LASTINDEX) {
					xx = Math.max(xx, pseudoknotscore(posInDNA + i));
				}
			}
			xx += slipperyscore(posInDNA);
			if (!Float.isNaN(xx)) {
				shifterScores.setValueAt(posInDNA, xx);
			} else {
				shifterScores.setValueAt(posInDNA, 0);
			}
			return shifterScores.getValueAt(posInDNA);
		} // end of Fitter.shifterscore(int)
		
	} // end of Fitter
	

/**
* Fitter to optimize leading part of Gag putein.
*/
	class GagStartFitter extends Fitter {
	
/**
* Best rated leading Path.
*/
		final PathClass BESTGAGLEADER;
		
/**
* Constructor, using kozakMotif and gagStartMotif.
*/
		GagStartFitter() throws RetroTectorException{
		
			int firstpos = Math.max(6, dnaStartFirst);
			int lastpos = hitinfos[0].HOTSPOT - 4;
			kozakScores = new Utilities.OffsetFloatArray(firstpos, lastpos);
			kozakScores.fillWith(Float.NaN);

			PathClass gagStartPath;
			PathClass bestPath = null;
			float bestScore = -Float.MAX_VALUE;
			for (int i=firstpos; i<=lastpos; i++) {
				gagStartPath = ldynmatrix.getPathStartingAt(i);
				if ((gagStartPath != null) && !gagStartPath.shiftEnd) {
					gagStartPath.kozakScore = kozakscore(i);
					gagStartPath.alignmentScore = gagStartMotif.scoreInAcidString(gagStartPath.acidString, 0);
					gagStartPath.alignmentScoreRow = gagStartMotif.latestRowOrigin;
					gagStartPath.finalizePath();
					if (gagStartPath.fetchScore() > bestScore) {
						bestScore = gagStartPath.fetchScore();
						bestPath = gagStartPath;
					}
				}
			}
			BESTGAGLEADER = bestPath;
		} // end of GagStartFitter.constructor()
		
	} // end of GagStartFitter
	

/**
* Fitter to optimize trailing part of Gag putein and leading part of Pro putein.
*/
	class GagProFitter extends Fitter {
	
/**
* Best rated trailing Gag Path.
*/
		final PathClass BESTGAGTRAILER;

/**
* Best rated leading Pro Path.
*/
		final PathClass BESTPROLEADER;
		
/**
* Completes Gag Path with gagEndMotif and stop codon information.
* @param	gagEndPath	The path in question.
* @return	True if the job was already done, false otherwise.
*/
		final boolean scoreGagEndPath(PathClass gagEndPath) throws RetroTectorException {
		
			if (!gagEndPath.getGeneName().equalsIgnoreCase("Gag")) {
				haltError("Path is not Gag");
			}
			
			if (gagEndPath.isFinalized()) {
				return true;
			}
			
			gagEndPath.alignmentScore = gagEndMotif.scoreInAcidString(gagEndPath.acidString, gagEndPath.acidString.length() - PuteinEndMotif.PUTEINENDLENGTH);
			gagEndPath.alignmentScoreRow = gagEndMotif.latestRowOrigin;
			char genuschar = gagEndPath.pathGenusChar;
			if (genuschar == 'a') { // Alpharetrovirus
				if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
					gagEndPath.stopScore = 1.0f;
				}
			} else if (genuschar == 'b') { // Betaretrovirus
				if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
					gagEndPath.stopScore = 1.0f;
				}
			} else if ((genuschar == 'c') | (genuschar == 'e')) { // Gammaretrovirus or Epsilonretrovirus
				gagEndPath.stopScore = stopBonus(gagEndPath.getLastInDNA() + 1);
			} else if (genuschar == 'd') { // Deltaretrovirus
				if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
					gagEndPath.stopScore = 1.0f;
				}
			} else if (genuschar == 's') { // Spumaretrovirus
				if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
					gagEndPath.stopScore = 1.0f;
				}
			} else if ((genuschar == 'l') | (genuschar == 'g') | (genuschar == 'o')) { // Lenti, Gypsy or Copia
				if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
					gagEndPath.stopScore = 1.0f;
				}
			} else {
				haltError("Unrecognized virus genus: " + genuschar);
			}
			
			gagEndPath.finalizePath();
			return false;
		} // end of GagProFitter.scoreGagEndPath(PathClass)
		
/**
* Emulates scoreGagEndPath.
*/
		final boolean scoreTrailer(PathClass trailPath) throws RetroTectorException {
			return scoreGagEndPath(trailPath);
		} // end of GagProFitter.scoreTrailer()
	
/**
* Completes Pro Path with proStartMotif, shifter, cleavage and Kozak motif information.
* @param	proStartPath	The path in question.
* @return	True if the job was already done, false otherwise.
*/
		final boolean scoreProStartPath(PathClass proStartPath) throws RetroTectorException {
			if (!proStartPath.getGeneName().equalsIgnoreCase("Pro")) {
				haltError("Path is not Pro");
			}
			
			if (proStartPath.isFinalized()) {
				return true;
			}
			
			proStartPath.alignmentScore = proStartMotif.scoreInAcidString(proStartPath.acidString, 0);
			proStartPath.alignmentScoreRow = proStartMotif.latestRowOrigin;
			char genuschar = proStartPath.pathGenusChar;
			if (genuschar == 'a') { // Alpharetrovirus
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else if (genuschar == 'b') { // Betaretrovirus
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
				float x = 0;
				int xi = -1;
				for (int i=proStartPath.getFirstInDNA()+PROBSHIFTEROFFSET; i<=proStartPath.getFirstInDNA(); i++) {
					if ((i > 0) && (shifterscore(i) > x)) {
						xi = i;
						x = shifterscore(i);
					}
				}
				proStartPath.shifterScore = x;
				proStartPath.shifterPos = xi;
			} else if (genuschar == 'c') { // Gammaretrovirus
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else if (genuschar == 'd') { // Deltaretrovirus
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else if (genuschar == 'e') { // Epsilonretrovirus
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else if (genuschar == 's') { // Spumaretrovirus
				proStartPath.kozakScore = kozakscore(proStartPath.getFirstInDNA());
			} else if (genuschar == 'l') { // Lenti
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else if (genuschar == 'g') { // Gypsy
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else if (genuschar == 'o') { // Copia
				proStartPath.cleavageScore = cleavagescore(proStartPath.getFirstInDNA());
			} else {
				haltError("Unrecognized virus genus: " + genuschar);
			}

			proStartPath.finalizePath();
			return false;
		} // end of GagProFitter.scoreProStartPath(PathClass)
		
/**
* Emulates scoreProStartPath.
*/
		final boolean scoreLeader(PathClass leadPath) throws RetroTectorException {
			return scoreProStartPath(leadPath);
		} // end of GagProFitter.scoreLeader(PathClass)
	
		
/**
* Constructor.
* @param	gagORFID	The Gag ORFID to provide with trailing Path.
* @param	proORFID	The Pro ORFID to provide with leading Path.
*/
		GagProFitter(ORFID gagORFID, ORFID proORFID) throws RetroTectorException {
		
			float bestscore = REJECTED;
			PathClass bestGagEndPath = null;
			PathClass bestProStartPath = null;
			prepareScorers(gagORFID, proORFID);
			if ((gagORFID != null) && (proORFID != null) && (gagORFID.dnaEndLast >= proORFID.dnaStartFirst)) {
// double fit
				PathClass gagEndPath;
				PathClass proStartPath;
				int gagfirst = gagORFID.hitinfos[gagORFID.hitinfos.length - 1].HOTSPOT + 4;
				int gaglast = gagORFID.dnaEndLast;
				int profirst = proORFID.dnaStartFirst;
				int prolast = proORFID.hitinfos[0].HOTSPOT - 4;
				float result = 0;
				int overlap;
				int xi;
				float x;
				for (int bef=gagfirst; bef<=gaglast; bef++) {
					gagEndPath = gagORFID.tdynmatrix.getPathEndingAt(bef);
					if ((gagEndPath != null) && !gagEndPath.shiftEnd) {
						scoreGagEndPath(gagEndPath);
						for (int aft=profirst; aft<=prolast; aft++) {
							proStartPath = proORFID.ldynmatrix.getPathStartingAt(aft);
							if ((proStartPath != null) && !proStartPath.shiftEnd) { // got two valid paths
								scoreProStartPath(proStartPath);
								result = gagEndPath.acidStringScore / gagEndPath.acidString.length() - 0.5f * gagEndPath.getShiftCount() + 1.2f * gagEndPath.alignmentScore + proStartPath.acidStringScore / proStartPath.acidString.length() - 0.5f * proStartPath.getShiftCount() + 1.2f * proStartPath.alignmentScore;
								if (gagEndPath.lateShift) {
									result -= 0.6f;
								}
								overlap = gagEndPath.getLastInDNA() - proStartPath.getFirstInDNA() + 1;
								if (genusChar == 'a') { // Alpharetrovirus
		
									result += 0.1f * overlap;
									result += cleavagescore(proStartPath.getFirstInDNA());
									if (overlap > 0) {
										result = REJECTED;
									}

								} else if (genusChar == 'b') { // Betaretrovirus

									if ((overlap > 400) | (overlap < -5)) {
										result = result - 1.0f;
									}
									if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += cleavagescore(proStartPath.getFirstInDNA());
									x = 0;
									for (int i=proStartPath.getFirstInDNA()+PROBSHIFTEROFFSET; i<=proStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(gagEndPath.getPath(), proStartPath.getPath())) {
										result = REJECTED;
									}
									
								} else if ((genusChar == 'c') | (genusChar == 'e')) { // Gammaretrovirus or Epsilonretrovirus
								
									if ((overlap > 0) | (overlap < -60)) {
										result = result - 1.0f;
									}
									result += stopBonus(gagEndPath.getLastInDNA() + 1);
									result += cleaveorstopscore(proStartPath.getFirstInDNA() - 3);
									if (pathsClash(gagEndPath.getPath(), proStartPath.getPath())) {
										result = REJECTED;
									}
									
								} else if (genusChar == 'd') { // Deltaretrovirus

									if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += 0.1f * overlap;
									result += cleavagescore(proStartPath.getFirstInDNA());
									if (overlap > 0) {
										result = REJECTED;
									}
			
								} else if (genusChar == 's') { // Spumaretrovirus

									if ((overlap > 50) | (overlap < 0)) {
										result = result - 1.0f;
									}
									if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += kozakscore(proStartPath.getFirstInDNA());
									if (pathsClash(gagEndPath.getPath(), proStartPath.getPath())) {
										result = REJECTED;
									}

								} else if ((genusChar == 'l') | (genusChar == 'o')) { // Lenti or Copia

									if ((overlap > 250) | (overlap < -5)) {
										result = result - 1.0f;
									}
									if (stopBonus(gagEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += cleavagescore(proStartPath.getFirstInDNA());
									x = 0;
									for (int i=proStartPath.getFirstInDNA()+PROLSHIFTEROFFSET; i<=proStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(gagEndPath.getPath(), proStartPath.getPath())) {
										result = REJECTED;
									}

								} else if (genusChar == 'g') { // Gypsy

									if ((overlap > 250) | (overlap < -5)) {
										result = result - 1.0f;
									}
									result += cleavagescore(proStartPath.getFirstInDNA());
									x = 0;
									for (int i=proStartPath.getFirstInDNA()+PROLSHIFTEROFFSET; i<=proStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(gagEndPath.getPath(), proStartPath.getPath())) {
										result = REJECTED;
									}

								} else {
									haltError("Unrecognized virus genus: " + genusChar);
								}
								if (result > bestscore) {
									bestscore = result;
									bestGagEndPath = gagEndPath;
									bestProStartPath = proStartPath;
								}
							}
						}
					}
				}
			}
			if (bestscore > REJECTED) {
				BESTPROLEADER = bestProStartPath;
				BESTGAGTRAILER = bestGagEndPath;
			} else {
				if (proORFID != null) { // fit only BESTPROLEADER
					int firstpos = proORFID.dnaStartFirst;
					int lastpos = proORFID.hitinfos[0].HOTSPOT - 4;
					PathClass pc;
					PathClass bestPath = null;
					float bestScore = -Float.MAX_VALUE;
					for (int i=firstpos; i<=lastpos; i++) {
						pc = proORFID.ldynmatrix.getPathStartingAt(i);
						if ((pc != null) && !pc.shiftEnd) {
							scoreProStartPath(pc);
							if (pc.fetchScore() > bestScore) {
								bestScore = pc.fetchScore();
								bestPath = pc;
							}
						}
					}
					BESTPROLEADER = bestPath;
				} else {
					BESTPROLEADER = null;
				}
				if (gagORFID != null) { // fit only BESTGAGTRAILER
					int firstpos = gagORFID.hitinfos[gagORFID.hitinfos.length - 1].HOTSPOT + 4;
					int lastpos = gagORFID.dnaEndLast;
					PathClass pc;
					PathClass bestPath = null;
					float bestScore = -Float.MAX_VALUE;
					for (int i=firstpos; i<=lastpos; i++) {
						pc = gagORFID.tdynmatrix.getPathEndingAt(i);
						if ((pc != null) && !pc.shiftEnd) {
							scoreGagEndPath(pc);
							if (pc.fetchScore() > bestScore) {
								bestScore = pc.fetchScore();
								bestPath = pc;
							}
						}
					}
					BESTGAGTRAILER = bestPath;
				} else {
					BESTGAGTRAILER = null;
				}
			}
		} // end of GagProFitter.constructor(ORFID, ORFID)
		
	} // end of GagProFitter
	
	
/**
* Fitter to optimize trailing part of Pro putein and leading part of Pol putein.
*/
	class ProPolFitter extends Fitter {
	
/**
* Best rated trailing Pro Path.
*/
		final PathClass BESTPROTRAILER;

/**
* Best rated leading Pol Path.
*/
		final PathClass BESTPOLLEADER;
		
/**
* Completes Pro Path with proEndMotif and stop codon and/or cleavage information.
* @param	proEndPath	The path in question.
* @return	True if the job was already done, false otherwise.
*/
		final boolean scoreProEndPath(PathClass proEndPath) throws RetroTectorException {
		
			if (!proEndPath.getGeneName().equalsIgnoreCase("Pro")) {
				haltError("Path is not Pro");
			}
			
			if (proEndPath.isFinalized()) {
				return true;
			}
			
			proEndPath.alignmentScore = proEndMotif.scoreInAcidString(proEndPath.acidString, proEndPath.acidString.length() - PuteinEndMotif.PUTEINENDLENGTH);
			proEndPath.alignmentScoreRow = proEndMotif.latestRowOrigin;

			char genuschar = proEndPath.pathGenusChar;
			if (genuschar == 'a') { // Alpharetrovirus
				proEndPath.cleavageScore = cleavagescore(proEndPath.getLastInDNA() + 1);
			} else if (genuschar == 'b') { // Betaretrovirus
				if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
					proEndPath.stopScore = 1.0f;
				}
			} else if ((genuschar == 'c') | (genuschar == 'e')) { // Gammaretrovirus or Epsilonretrovirus
				proEndPath.cleavageScore = cleavagescore(proEndPath.getLastInDNA() + 1);
			} else if (genuschar == 'd') { // Deltaretrovirus
				if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
					proEndPath.stopScore = 1.0f;
				}
			} else if (genuschar == 's') { // Spumaretrovirus
				proEndPath.cleavageScore = cleavagescore(proEndPath.getLastInDNA() + 1);
			} else if ((genuschar == 'l') | (genuschar == 'g') | (genuschar == 'o')) { // Lenti, Gypsy or Copia
				proEndPath.cleavageScore = cleavagescore(proEndPath.getLastInDNA() + 1);
			} else {
				haltError("Unrecognized virus genus: " + genuschar);
			}
			
			proEndPath.finalizePath();
			return false;
		} // end of ProPolFitter.scoreProEndPath(PathClass)
		
/**
* Emulates scoreProEndPath.
*/
		final boolean scoreTrailer(PathClass trailPath) throws RetroTectorException {
			return scoreProEndPath(trailPath);
		} // end of ProPolFitter.scoreTrailer(PathClass)
	
/**
* Completes Pol Path with polStartMotif, cleavage or Kozak motif information.
* @param	polStartPath	The path in question.
* @return	True if the job was already done, false otherwise.
*/
		final boolean scorePolStartPath(PathClass polStartPath) throws RetroTectorException {
			if (!polStartPath.getGeneName().equalsIgnoreCase("Pol")) {
				haltError("Path is not Pol");
			}
			
			if (polStartPath.isFinalized()) {
				return true;
			}
			
			polStartPath.alignmentScore = polStartMotif.scoreInAcidString(polStartPath.acidString, 0);
			polStartPath.alignmentScoreRow = polStartMotif.latestRowOrigin;
			char genuschar = polStartPath.pathGenusChar;
			if (genuschar == 'a') { // Alpharetrovirus
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'b') { // Betaretrovirus
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'c') { // Gammaretrovirus
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'd') { // Deltaretrovirus
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'e') { // Epsilonretrovirus
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 's') { // Spumaretrovirus
				polStartPath.kozakScore = kozakscore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'l') { // Lenti
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'g') { // Gypsy
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else if (genuschar == 'o') { // Copia
				polStartPath.cleavageScore = cleavagescore(polStartPath.getFirstInDNA());
			} else {
				haltError("Unrecognized virus genus: " + genuschar);
			}

			polStartPath.finalizePath();
			return false;
		} // end of ProPolFitter.scorePolStartPath(PathClass)
		
/**
* Emulates scorePolStartPath.
*/
		final boolean scoreLeader(PathClass leadPath) throws RetroTectorException {
			return scorePolStartPath(leadPath);
		} // end of ProPolFitter.scoreLeader
	
		
/**
* Constructor.
* @param	proORFID	The Pro ORFID to provide with trailing Path.
* @param	polORFID	The Pol ORFID to provide with leading Path.
*/
		ProPolFitter(ORFID proORFID, ORFID polORFID) throws RetroTectorException {
		
			float bestscore = REJECTED;
			PathClass bestProEndPath = null;
			PathClass bestPolStartPath = null;
			prepareScorers(proORFID, polORFID);
			if ((proORFID != null) && (polORFID != null) && (proORFID.dnaEndLast >= polORFID.dnaStartFirst)) {
// double fit
				PathClass proEndPath;
				PathClass polStartPath;
				int profirst = proORFID.hitinfos[proORFID.hitinfos.length - 1].HOTSPOT + 4;
				int prolast = proORFID.dnaEndLast;
				int polfirst = polORFID.dnaStartFirst;
				int pollast = polORFID.hitinfos[0].HOTSPOT - 4;
				float result = 0;
				int overlap;
				int xi;
				float x;
				for (int bef=profirst; bef<=prolast; bef++) {
					proEndPath = proORFID.tdynmatrix.getPathEndingAt(bef);
					if ((proEndPath != null) && !proEndPath.shiftEnd) {
						scoreProEndPath(proEndPath);
						for (int aft=polfirst; aft<=pollast; aft++) {
							polStartPath = polORFID.ldynmatrix.getPathStartingAt(aft);
							if ((polStartPath != null) && !polStartPath.shiftEnd) { // got two valid paths
								scorePolStartPath(polStartPath);
								result = proEndPath.acidStringScore / proEndPath.acidString.length() - 0.5f * proEndPath.getShiftCount() + 1.2f * proEndPath.alignmentScore + polStartPath.acidStringScore / polStartPath.acidString.length() - 0.5f * polStartPath.getShiftCount() + 1.2f * polStartPath.alignmentScore;
								if (proEndPath.lateShift) {
									result -= 0.6f;
								}
								overlap = proEndPath.getLastInDNA() - polStartPath.getFirstInDNA() + 1;
								if (genusChar == 'a') { // Alpharetrovirus
		
									if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += 0.1f * overlap;
									result += cleavagescore(polStartPath.getFirstInDNA());
									if (overlap > 0) {
										result = REJECTED;
									}

								} else if (genusChar == 'b') { // Betaretrovirus

									if ((overlap > 40) | (overlap < -5)) {
										result = result - 1.0f;
									}
									if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += cleavagescore(polStartPath.getFirstInDNA());
									x = 0;
									for (int i=polStartPath.getFirstInDNA()+POLBSHIFTEROFFSET; i<=polStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(proEndPath.getPath(), polStartPath.getPath())) {
										result = REJECTED;
									}
									
								} else if ((genusChar == 'c') | (genusChar == 'e')) { // Gammaretrovirus or Epsilonretrovirus
								
									if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += 0.1f * overlap;
									result += cleavagescore(polStartPath.getFirstInDNA());
									if (overlap > 0) {
										result = REJECTED;
									}
									
								} else if (genusChar == 'd') { // Deltaretrovirus

									if ((overlap > 250) | (overlap < -5)) {
										result = result - 1.0f;
									}
									if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += cleavagescore(polStartPath.getFirstInDNA());
									x = 0;
									for (int i=polStartPath.getFirstInDNA()+POLDSHIFTEROFFSET; i<=polStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(proEndPath.getPath(), polStartPath.getPath())) {
										result = REJECTED;
									}
			
								} else if (genusChar == 's') { // Spumaretrovirus

									if ((overlap > 50) | (overlap < 0)) {
										result = result - 1.0f;
									}
									if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += kozakscore(polStartPath.getFirstInDNA());
									if (pathsClash(proEndPath.getPath(), polStartPath.getPath())) {
										result = REJECTED;
									}

								} else if ((genusChar == 'l') | (genusChar == 'o')) { // Lenti or Copia

									if ((overlap > 250) | (overlap < -5)) {
										result = result - 1.0f;
									}
									if (stopBonus(proEndPath.getLastInDNA() + 1) > 0) {
										result += 1.0f;
									}
									result += cleavagescore(polStartPath.getFirstInDNA());
									x = 0;
									for (int i=polStartPath.getFirstInDNA()+POLLSHIFTEROFFSET; i<=polStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(proEndPath.getPath(), polStartPath.getPath())) {
										result = REJECTED;
									}

								} else if (genusChar == 'g') { // Gypsy

									if ((overlap > 250) | (overlap < -5)) {
										result = result - 1.0f;
									}
									result += cleavagescore(polStartPath.getFirstInDNA());
									x = 0;
									for (int i=polStartPath.getFirstInDNA()+POLLSHIFTEROFFSET; i<=polStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(proEndPath.getPath(), polStartPath.getPath())) {
										result = REJECTED;
									}

								} else {
									haltError("Unrecognized virus genus: " + genusChar);
								}
								if (result > bestscore) {
									bestscore = result;
									bestProEndPath = proEndPath;
									bestPolStartPath = polStartPath;
								}
							}
						}
					}
				}
			}
			if (bestscore > REJECTED) {
				BESTPOLLEADER = bestPolStartPath;
				BESTPROTRAILER = bestProEndPath;
			} else {
				if (polORFID != null) { // fit only BESTPOLLEADER
					int firstpos = polORFID.dnaStartFirst;
					int lastpos = polORFID.hitinfos[0].HOTSPOT - 4;
					PathClass pc;
					PathClass bestPath = null;
					float bestScore = -Float.MAX_VALUE;
					for (int i=firstpos; i<=lastpos; i++) {
						pc = polORFID.ldynmatrix.getPathStartingAt(i);
						if ((pc != null) && !pc.shiftEnd) {
							scorePolStartPath(pc);
							if (pc.fetchScore() > bestScore) {
								bestScore = pc.fetchScore();
								bestPath = pc;
							}
						}
					}
					BESTPOLLEADER = bestPath;
				} else {
					BESTPOLLEADER = null;
				}
				if (proORFID != null) {
					int firstpos = proORFID.hitinfos[proORFID.hitinfos.length - 1].HOTSPOT + 4;
					int lastpos = proORFID.dnaEndLast;
					PathClass pc;
					PathClass bestPath = null;
					float bestScore = -Float.MAX_VALUE;
					for (int i=firstpos; i<=lastpos; i++) {
						pc = proORFID.tdynmatrix.getPathEndingAt(i);
						if ((pc != null) && !pc.shiftEnd) {
							scoreProEndPath(pc);
							if (pc.fetchScore() > bestScore) {
								bestScore = pc.fetchScore();
								bestPath = pc;
							}
						}
					}
					BESTPROTRAILER = bestPath;
				} else {
					BESTPROTRAILER = null;
				}
			}
		} // end of ProPolFitter.constructor(ORFID, ORFID)
		
	} // end of ProPolFitter
	

/**
* Fitter to optimize trailing part of Pol putein and leading part of Env putein.
*/
	class PolEnvFitter extends Fitter {
	
/**
* Best rated trailing Pol Path.
*/
		final PathClass BESTPOLTRAILER;

/**
* Best rated leading Env Path.
*/
		final PathClass BESTENVLEADER;
		
/**
* Completes Pro Path with polEndMotif and stop codon information.
* @param	polEndPath	The path in question.
* @return	True if the job was already done, false otherwise.
*/
		final boolean scorePolEndPath(PathClass polEndPath) throws RetroTectorException {
		
			if (!polEndPath.getGeneName().equalsIgnoreCase("Pol")) {
				haltError("Path is not Pol");
			}
			
			if (polEndPath.isFinalized()) {
				return true;
			}
			
			polEndPath.alignmentScore = polEndMotif.scoreInAcidString(polEndPath.acidString, polEndPath.acidString.length() - PuteinEndMotif.PUTEINENDLENGTH);
			polEndPath.alignmentScoreRow = polEndMotif.latestRowOrigin;
			char genuschar = polEndPath.pathGenusChar;
			if (genuschar == 'a') { // Alpharetrovirus
				if (stopBonus(polEndPath.getLastInDNA() + 1) > 0) {
					polEndPath.stopScore = 1.0f;
				}
			} else if (genuschar == 'b') { // Betaretrovirus
				if (stopBonus(polEndPath.getLastInDNA() + 1) > 0) {
					polEndPath.stopScore = 1.0f;
				}
			} else if ((genuschar == 'c') | (genuschar == 'e')) { // Gammaretrovirus or Epsilonretrovirus
				if (stopBonus(polEndPath.getLastInDNA() + 1) > 0) {
					polEndPath.stopScore = 1.0f;
				}
			} else if (genuschar == 'd') { // Deltaretrovirus
				if (stopBonus(polEndPath.getLastInDNA() + 1) > 0) {
					polEndPath.stopScore = 1.0f;
				}
			} else if (genuschar == 's') { // Spumaretrovirus
				if (stopBonus(polEndPath.getLastInDNA() + 1) > 0) {
					polEndPath.stopScore = 1.0f;
				}
			} else if ((genuschar == 'l') | (genuschar == 'g') | (genuschar == 'o')) { // Lenti, Gypsy or Copia
				if (stopBonus(polEndPath.getLastInDNA() + 1) > 0) {
					polEndPath.stopScore = 1.0f;
				}
			} else {
				haltError("Unrecognized virus genus: " + genuschar);
			}
			
			polEndPath.finalizePath();
			return false;
		} // end of PolEnvFitter.scorePolEndPath(PathClass)
		
/**
* Emulates scorePolEndPath.
*/
		final boolean scoreTrailer(PathClass trailPath) throws RetroTectorException {
			return scorePolEndPath(trailPath);
		} // end of PolEnvFitter.scoreTrailer
	
/**
* Completes Env Path with envStartMotif, acceptor motif, Kozak motif and SiSeqMotif information.
* @param	envStartPath	The path in question.
* @return	True if the job was already done, false otherwise.
*/
		final boolean scoreEnvStartPath(PathClass envStartPath) throws RetroTectorException {
			if (!envStartPath.getGeneName().equalsIgnoreCase("Env")) {
				haltError("Path is not Env");
			}
			
			if (envStartPath.isFinalized()) {
				return true;
			}
			
			char genuschar = envStartPath.pathGenusChar;
			envStartPath.alignmentScore = envStartMotif.scoreInAcidString(envStartPath.acidString, 0);
			envStartPath.alignmentScoreRow = envStartMotif.latestRowOrigin;
			if (genuschar != 'g') {
				float x = 0;
				int xi = -1;
				for (int i=envStartPath.getFirstInDNA()+ENVACCEPTOROFFSET; i<=envStartPath.getFirstInDNA(); i++) {
					if ((i > 0) && (acceptorscore(i) > x)) {
						xi = i;
						x = acceptorscore(i);
					}
				}
				envStartPath.acceptorScore = x;
				envStartPath.acceptorPos = xi;
				
				envStartPath.kozakScore = kozakscore(envStartPath.getFirstInDNA());
			}
			if (envStartPath.acidString.length() < 35) {
				envStartPath.siseqScore = 0;
				envStartPath.siseqString = "";
			} else {
				float sis = siseqMotif.scoreAcidString(envStartPath.acidString.substring(20, 35)) / SiSeqMotif.maxscore;
				int sip = 20;
				float sist;
				for (int i=21; (i<=70) & (i<envStartPath.acidString.length() - 15); i++) {
					sist = siseqMotif.scoreAcidString(envStartPath.acidString.substring(i, i + 15)) / siseqMotif.maxscore;
					if (sist > sis) {
						sis = sist;
						sip = i;
					}
				}
				envStartPath.siseqScore = sis;
				envStartPath.siseqString = envStartPath.acidString.substring(sip, sip + 15);
			}
			
			if (genuschar == 'a') { // Alpharetrovirus
			} else if (genuschar == 'b') { // Betaretrovirus
			} else if (genuschar == 'c') { // Gammaretrovirus
			} else if (genuschar == 'd') { // Deltaretrovirus
			} else if (genuschar == 'e') { // Epsilonretrovirus
			} else if (genuschar == 's') { // Spumaretrovirus
			} else if (genuschar == 'l') { // Lenti
			} else if (genuschar == 'g') { // Gypsy
			} else if (genuschar == 'o') { // Copia
			} else {
				haltError("Unrecognized virus genus: " + genuschar);
			}

			envStartPath.finalizePath();
			return false;
		} // end of PolEnvFitter.scoreEnvStartPath(PathClass)
		
/**
* Emulates scoreEnvStartPath.
*/
		final boolean scoreLeader(PathClass leadPath) throws RetroTectorException {
			return scoreEnvStartPath(leadPath);
		} // end of PolEnvFitter.scoreLeader(PathClass)
	
/**
* Constructor.
* @param	polORFID	The Pol ORFID to provide with trailing Path.
* @param	envORFID	The Env ORFID to provide with leading Path.
*/
		PolEnvFitter(ORFID polORFID, ORFID envORFID) throws RetroTectorException {
		
			float bestscore = REJECTED;
			PathClass bestPolEndPath = null;
			PathClass bestEnvStartPath = null;
			prepareScorers(polORFID, envORFID);
			if ((polORFID != null) && (envORFID != null) && (polORFID.dnaEndLast >= envORFID.dnaStartFirst)) {
				PathClass polEndPath;
				PathClass envStartPath;
				int polfirst = polORFID.hitinfos[polORFID.hitinfos.length - 1].HOTSPOT + 4;
				int pollast = polORFID.dnaEndLast;
				int envfirst = envORFID.dnaStartFirst;
				int envlast = envORFID.hitinfos[0].HOTSPOT - 4;
				float result = 0;
				float polv;
				for (int bef=polfirst; bef<=pollast; bef++) {
					polEndPath = polORFID.tdynmatrix.getPathEndingAt(bef);
					if ((polEndPath != null) && !polEndPath.shiftEnd) {
						scorePolEndPath(polEndPath);
						polv = polEndPath.fetchScore();
						for (int aft=Math.max(envfirst, bef-250); aft<=envlast; aft++) {
							envStartPath = envORFID.ldynmatrix.getPathStartingAt(aft);
							if ((envStartPath != null) && !envStartPath.shiftEnd) {
// double fit
								scoreEnvStartPath(envStartPath);
								if (genusChar == 'g') { // Gypsy

									result = polEndPath.acidStringScore / polEndPath.acidString.length() - 0.5f * polEndPath.getShiftCount() + 1.2f * polEndPath.alignmentScore + envStartPath.acidStringScore / envStartPath.acidString.length() - 0.5f * envStartPath.getShiftCount() + 1.2f * envStartPath.alignmentScore;
									if (polEndPath.lateShift) {
										result -= 0.6f;
									}
									int overlap = polEndPath.getLastInDNA() - envStartPath.getFirstInDNA() + 1;
									if ((overlap > 250) | (overlap < -5)) {
										result = result - 1.0f;
									}
									result += cleavagescore(envStartPath.getFirstInDNA());
									float x = 0;
									for (int i=envStartPath.getFirstInDNA()+POLLSHIFTEROFFSET; i<=envStartPath.getFirstInDNA(); i++) {
										if (i > 0) {
											x = Math.max(x, shifterscore(i));
										}
									}
									result += x;
									if (pathsClash(polEndPath.getPath(), envStartPath.getPath())) {
										result = REJECTED;
									}

								} else {
									result = polv + envStartPath.fetchScore();
									if (pathsClash(polEndPath.getPath(), envStartPath.getPath())) {
										result = REJECTED;
									}
								}
								if (result > bestscore) {
									bestscore = result;
									bestPolEndPath = polEndPath;
									bestEnvStartPath = envStartPath;
								}
							}
						}
					}
				}
			}
			if (bestscore > REJECTED) {
				BESTENVLEADER = bestEnvStartPath;
				BESTPOLTRAILER = bestPolEndPath;
			} else {
				if (envORFID != null) { // fit only BESTENVLEADER
					int firstpos = envORFID.dnaStartFirst;
					int lastpos = envORFID.hitinfos[0].HOTSPOT - 4;
					PathClass pc;
					PathClass bestPath = null;
					float bestScore = -Float.MAX_VALUE;
					for (int i=firstpos; i<=lastpos; i++) {
						pc = envORFID.ldynmatrix.getPathStartingAt(i);
						if ((pc != null) && !pc.shiftEnd) {
							scoreEnvStartPath(pc);
							if (pc.fetchScore() > bestScore) {
								bestScore = pc.fetchScore();
								bestPath = pc;
							}
						}
					}
					BESTENVLEADER = bestPath;
				} else {
					BESTENVLEADER = null;
				}
				if (polORFID != null) {
					int firstpos = polORFID.hitinfos[polORFID.hitinfos.length - 1].HOTSPOT + 4;
					int lastpos = polORFID.dnaEndLast;
					PathClass pc;
					PathClass bestPath = null;
					float bestScore = -Float.MAX_VALUE;
					for (int i=firstpos; i<=lastpos; i++) {
						pc = polORFID.tdynmatrix.getPathEndingAt(i);
						if ((pc != null) && !pc.shiftEnd) {
							scorePolEndPath(pc);
							if (pc.fetchScore() > bestScore) {
								bestScore = pc.fetchScore();
								bestPath = pc;
							}
						}
					}
					BESTPOLTRAILER = bestPath;
				} else {
					BESTPOLTRAILER = null;
				}
			}
		} // end of PolEnvFitter.constructor(ORFID, ORFID)
		
	} // end of PolEnvFitter
	

/**
* Fitter to optimize trailing part of Env putein.
*/
	class EnvEndFitter extends Fitter {
	
/**
* Best rated trailing Path.
*/
		final PathClass BESTENVTRAILER;
		
/**
* Constructor.
*/
		EnvEndFitter() throws RetroTectorException{
		
			int firstpos = hitinfos[hitinfos.length - 1].HOTSPOT + 4;;
			int lastpos = dnaEndLast;
			stopScores = new Utilities.OffsetFloatArray(firstpos, lastpos);
			stopScores.fillWith(Float.NaN);

			PathClass envEndPath;
			PathClass bestPath = null;
			float bestScore = -Float.MAX_VALUE;
			for (int i=firstpos; i<=lastpos; i++) {
				envEndPath = tdynmatrix.getPathEndingAt(i);
				if ((envEndPath != null) && !envEndPath.shiftEnd) {
					if (stopBonus(envEndPath.getLastInDNA() + 1) > 0) {
						envEndPath.stopScore = 1.0f;
					}
					envEndPath.alignmentScore = envEndMotif.scoreInAcidString(envEndPath.acidString, envEndPath.acidString.length() - PuteinEndMotif.PUTEINENDLENGTH);
					envEndPath.alignmentScoreRow = envEndMotif.latestRowOrigin;
					envEndPath.finalizePath();
					if (envEndPath.fetchScore() > bestScore) {
						bestScore = envEndPath.fetchScore();
						bestPath = envEndPath;
					}
				}
			}
			BESTENVTRAILER = bestPath;
		} // end of EnvEndFitter.constructor()
		
	} // end of EnvEndFitter
	

/**
* Fitter to optimize trailing part of a putein and leading part a non-adjacent putein.
*/
	class DistantFitter extends Fitter {
	
/**
* Best rated trailing Path.
*/
		final PathClass BESTTRAILER;

/**
* Best rated leading Path.
*/
		final PathClass BESTLEADER;
		
		private Fitter trailFitter;
		private Fitter leadFitter;
		
		
/**
* Constructor.
* @param	trailORFID	The ORFID to provide with trailing Path.
* @param	leadORFID		The ORFID to provide with leading Path.
*/
		DistantFitter(ORFID trailORFID, ORFID leadORFID) throws RetroTectorException {
			
			if (trailORFID.genename.equalsIgnoreCase("Gag")) {
				trailFitter = new GagProFitter(trailORFID, null);
			} else if (trailORFID.genename.equalsIgnoreCase("Pro")) {
				trailFitter = new ProPolFitter(trailORFID, null);
			} else {
				haltError("Unacceptable Gene: " + genename);
			}
			if (leadORFID.genename.equalsIgnoreCase("Pol")) {
				leadFitter = new ProPolFitter(null, leadORFID);
			} else if (leadORFID.genename.equalsIgnoreCase("Env")) {
				leadFitter = new PolEnvFitter(null, leadORFID);
			} else {
				haltError("Unacceptable Gene: " + genename);
			}

			PathClass bestTrailer = null;
			PathClass bestLeader = null;

			float bestscore = REJECTED;
			if (trailORFID.dnaEndLast >= leadORFID.dnaStartFirst) {
				PathClass trailPath;
				PathClass leadPath;
				int trailfirst = trailORFID.hitinfos[trailORFID.hitinfos.length - 1].HOTSPOT + 4;
				int traillast = trailORFID.dnaEndLast;
				int leadfirst = leadORFID.dnaStartFirst;
				int leadlast = leadORFID.hitinfos[0].HOTSPOT - 4;
				float trailv;
				for (int bef=trailfirst; bef<=traillast; bef++) {
					trailPath = trailORFID.tdynmatrix.getPathEndingAt(bef);
					if ((trailPath != null) && !trailPath.shiftEnd) {
						trailFitter.scoreTrailer(trailPath);
						trailv = trailPath.fetchScore();
						for (int aft=Math.max(leadfirst, bef+1); aft<=leadlast; aft++) {
							leadPath = leadORFID.ldynmatrix.getPathStartingAt(aft);
							if ((leadPath != null) && !leadPath.shiftEnd) {
								leadFitter.scoreLeader(leadPath);
								if (trailv + leadPath.fetchScore() > bestscore) {
									bestscore = trailv + leadPath.fetchScore();
									bestTrailer = trailPath;
									bestLeader = leadPath;
								}
							}
						}
					}
				}
			}
			if (bestscore > REJECTED) {
				BESTLEADER = bestLeader;
				BESTTRAILER = bestTrailer;
			} else {
				int firstpos = leadORFID.dnaStartFirst;
				int lastpos = leadORFID.hitinfos[0].HOTSPOT - 4;
				PathClass pc;
				PathClass bestPath = null;
				float bestScore = -Float.MAX_VALUE;
				for (int i=firstpos; i<=lastpos; i++) {
					pc = leadORFID.ldynmatrix.getPathStartingAt(i);
					if ((pc != null) && !pc.shiftEnd) {
						leadFitter.scoreLeader(pc);
						if (pc.fetchScore() > bestScore) {
							bestScore = pc.fetchScore();
							bestPath = pc;
						}
					}
				}
				BESTLEADER = bestPath;
				firstpos = trailORFID.hitinfos[trailORFID.hitinfos.length - 1].HOTSPOT + 4;
				lastpos = trailORFID.dnaEndLast;
				bestPath = null;
				bestScore = -Float.MAX_VALUE;
				for (int i=firstpos; i<=lastpos; i++) {
					pc = trailORFID.tdynmatrix.getPathEndingAt(i);
					if ((pc != null) && !pc.shiftEnd) {
						trailFitter.scoreTrailer(pc);
						if (pc.fetchScore() > bestScore) {
							bestScore = pc.fetchScore();
							bestPath = pc;
						}
					}
				}
				BESTTRAILER = bestPath;
			}
		} // end of DistantFitter.constructor(ORFID, ORFID)
		
	} // end of DistantFitter
	

/**
* Key for value of stop codon in aligned fitting = "StopCodonValue".
*/
	public final static String STOPCODONKEY = "StopCodonValue";

/**
* Key for factor for discarding lowscoring MotifHits	= "ScoreFactor".
*/
	public final static String SCOREFACTORKEY = "ScoreFactor";

/**
* Key  = "NumberOfHits".
*/
 	public final static String NROFHITSKEY = "NumberOfHits";

/**
* Key for lowest acceptable sum of squared MotifHit scores = "MinSSScore".
*/
	public final static String MINSSSCORE = "MinSSScore";
	
/**
* Key for lowest acceptable individual MotifHit score = "MinHitScore".
*/
	public final static String MINHITSCOREKEY = "MinHitScore";
	
/**
* Key for parameter with individual script names = "GeneScripts".
*/
  public static final String GENESCRIPTSKEY = "GeneScripts";
	
/**
* Basic score for non-aligned interpretation.
*/
  public float nonAlignedScore = 0.4f;
	
/**
* Default value for nonAlignedScore = "0.4".
*/
  public static final String NONALIGNEDDEFAULT = "0.4";
	
/**
* Minimal distance between slippery sequence and pseudoknot.
*/
	public static final int MINPSEUDOKNOTOFFSET = 12;

/**
* Maximal distance between slippery sequence and pseudoknot.
*/
	public static final int MAXPSEUDOKNOTOFFSET = 16;

/**
* Factor to multiply stop codon load by.
*/
   public float stopCodonFactor = -0.4f;
	
/**
* Default value for stopCodonFactor = "-0.4".
*/
  public static final String STOPCODONDEFAULT = "-0.4";

/**
* Penalty for frame shift.
*/
  public float frameShiftPenalty = -1.5f;

/**
* Default value for frameShiftPenalty = "-1.5".
*/
  public static final String FRAMESHIFTDEFAULT = "-1.5";

/**
* To multiply ORFHexamerModifier by.
*/
  public float orfHexamerFactor = 0.2f;

/**
* Default value for orfHexamerFactor = "0.2".
*/
  public static final String ORFHEXAMERDEFAULT = "0.2";

/**
* To multiply NonORFHexamerModifier by.
*/
  public float nonOrfHexamerFactor = -0.1f;

/**
* Default value for nonOrfHexamerFactor = "-0.1".
*/
  public static final String NONORFHEXAMERDEFAULT = "-0.1";

/**
* Instance of SlipperyMotif.
*/
	SlipperyMotif slipperyMotif = null;

/**
* Instance of Kozak Motif.
*/
  OrdinaryMotif kozakMotif;

/**
* Instance of ProteaseCleavageMotif.
*/
	ProteaseCleavageMotif proteaseCleavageMotif = null;

/**
* Instance of PseudoKnotMotif.
*/
	PseudoKnotMotif pseudoKnotMotif = null;

/**
* Instance of SpliceAcceptorMotif.
*/
	SpliceAcceptorMotif acceptorMotif = null;

/**
* Instance of SiSeqMotif.
*/
	SiSeqMotif siseqMotif = null;

/**
* Instance of PuteinStartMotif built on Gag alignment.
*/
	PuteinStartMotif gagStartMotif = null;

/**
* Instance of PuteinStartMotif built on Pro alignment.
*/
	PuteinStartMotif proStartMotif = null;

/**
* Instance of PuteinStartMotif built on Pol alignment.
*/
	PuteinStartMotif polStartMotif = null;

/**
* Instance of PuteinStartMotif built on Env alignment.
*/
	PuteinStartMotif envStartMotif = null;


/**
* Instance of PuteinEndMotif built on Gag alignment.
*/
	PuteinEndMotif gagEndMotif = null;

/**
* Instance of PuteinEndMotif built on Pro alignment.
*/
	PuteinEndMotif proEndMotif = null;

/**
* Instance of PuteinEndMotif built on Pol alignment.
*/
	PuteinEndMotif polEndMotif = null;

/**
* Instance of PuteinEndMotif built on Env alignment.
*/
	PuteinEndMotif envEndMotif = null;

/**
* To multiply glycsite load factor by.
*/
  public float glycSiteFactor = 0.2f; // 

/**
* Default value for glycSiteFactor = "0.2".

*/
  public static final String GLYCSITEDEFAULT = "0.2";
  
/**
* To to add for position in long ORF.
*/
  public float inORFbonus = 0.1f; // 

/**
* Default value for inORFbonus = "0.1".
*/
  public static final String INORFBONUSDEFAULT = "0.1";
	
/**
*	Integer code for TGA codon.
*/
	public static final int TGACODON = 11;
	
/**
*	Integer code for TAG codon.
*/
	public static final int TAGCODON = 14;
	
/**
*	Integer code for TAA codon.
*/
	public static final int TAACODON = 15;
  
/**
* To multiply MotifHit score by.
*/
  public float motifHitFactor = 0.2f;

/**
* Penalty for skipping a filled column in master.
*/
  public float masterPenalty = -0.2f;
  
/**
*	Value of stop codon in aligned fitting.
*/
	public int stopValue = -15;

/**
* The relevant DNA.
*/
	public DNA dna = null;

/**
* Alignment to fit to.
*/
	public Alignment master;

/**
* gag, pro, pol or env.
*/
	public String genename;

/**
* Virus genus, lower case.
*/
	public char genusChar;

/**
* Info about Motif hits.
*/
	public MotifHitsInfo hitsInfo = null; 

/**
* The Putein to build.
*/
	protected Putein putein;

/**
* Putein file to create.
*/
	protected ExecutorScriptWriter puteinWriter = null;

/**
* ORFIDout file to create in debugging mode.
*/
	protected ExecutorScriptWriter outputWriter = null;

/**
* Collection of positions occupied by acids. Mainly used by XonID.
*/
	protected int[ ][ ] avoidPositions = null;

  protected Database database;

	private float scoreFactor = 0.1f; // hit rejection threshold related
	private float minSSScore = 600f;
	private float minHitScore = 15f;
	private int dnaStartFirst = 1; // earliest possible hook position in DNA (internal)
	private int dnaStartLast = 1; // last possible hook position in DNA (internal)
	private int dnaEndFirst = 200000; // first position in DNA (internal) to consider
	private int dnaEndLast = 200000; // last position in DNA (internal) to consider
	private boolean debugging = false;
				
// useful content of hitsInfo
	private MotifHitInfo[ ] hitinfos;
	
	private int startAt; // to be estimatedFirst
	private int endAt; // to be estimatedLast
		
	private long starttime;
	
// names of ORFID scripts from MScript
	private String[ ] geneScripts = null;
	
	private TrailingDynamicMatrix tdynmatrix = null;
	private LeadingDynamicMatrix ldynmatrix = null;
	
// delimiters for prioritized ranges for estimatedFirst and estimatedLast
	private int leadingInsideLimit;
	private int trailingInsideLimit;
	
	private boolean puteinFinished = false;
	
	private int orfidNr = 0;
	private int hitsfound = 0;
	
	private static ORFID[ ] orfids;
	
/**
* @param	path1	Array of PathElement.
* @param	path2	Array of PathElement.
* @return	True if FROMDNA is identical in any pair of an element from path1 and an element from path2.
*/
	public final static boolean pathsClash(PathElement[ ] path1, PathElement[ ] path2) {
		if (path1[path1.length - 1].TODNA < path2[0].FROMDNA) {
			return false;
		}
		if (path2[path2.length - 1].TODNA < path1[0].FROMDNA) {
			return false;
		}
		int p1 = 0;
		int p2 = 0;
		int val1 = path1[p1].FROMDNA;
		int val2 = path2[p2].FROMDNA;
		try {
			for (;;) {
				while (val1 < val2) {
					p1++;
					val1 = path1[p1].FROMDNA;
				}
				if (val1 == val2) {
					return true;
				}
				while (val2 < val1) {
					p2++;
					val2 = path2[p2].FROMDNA;
				}
				if (val1 == val2) {
					return true;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return false;
	} // end of pathsClash(PathElement[ ], PathElement[ ])
	
/**
* Standard Executor constructor.
*/
	public ORFID() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();

		parameters.put(INPUTFILEKEY, "");
		explanations.put(INPUTFILEKEY, "RetroVID output file with parameters for ORFID");
		orderedkeys.push(INPUTFILEKEY);
		parameters.put(DATABASEKEY, ORDINARYDATABASE);
		explanations.put(DATABASEKEY, "The relevant database subdirectory");
		orderedkeys.push(DATABASEKEY);

		parameters.put(NONALIGNEDSCOREKEY, NONALIGNEDDEFAULT);
		explanations.put(NONALIGNEDSCOREKEY, "Basic score assigned to non-aligned step");
		orderedkeys.push(NONALIGNEDSCOREKEY);
		parameters.put(STOPCODONFACTORKEY, STOPCODONDEFAULT);
		explanations.put(STOPCODONFACTORKEY, "Weight assigned to StopCodonModifier");
		orderedkeys.push(STOPCODONFACTORKEY);
		parameters.put(FRAMESHIFTPENALTYKEY, FRAMESHIFTDEFAULT);
		explanations.put(FRAMESHIFTPENALTYKEY, "Weight assigned to frame shift");
		orderedkeys.push(FRAMESHIFTPENALTYKEY);
		parameters.put(ORFHEXAMERFACTORKEY, ORFHEXAMERDEFAULT);
		explanations.put(ORFHEXAMERFACTORKEY, "Weight assigned to ORFHexamerModifier");
		orderedkeys.push(ORFHEXAMERFACTORKEY);
		parameters.put(NONORFHEXAMERFACTORKEY, NONORFHEXAMERDEFAULT);
		explanations.put(NONORFHEXAMERFACTORKEY, "Weight assigned to NonORFHexamerModifier");
		orderedkeys.push(NONORFHEXAMERFACTORKEY);
		parameters.put(GLYCSITEFACTORKEY, GLYCSITEDEFAULT);
		explanations.put(GLYCSITEFACTORKEY, "Weight assigned to GlycSiteModifier");
		orderedkeys.push(GLYCSITEFACTORKEY);
		parameters.put(INORFBONUSKEY, INORFBONUSDEFAULT);
		explanations.put(INORFBONUSKEY, "Bonus for position in long ORF");
		orderedkeys.push(INORFBONUSKEY);
		parameters.put(MOTIFHITFACTORKEY, "0.2");
		explanations.put(MOTIFHITFACTORKEY, "Factor to multiply Motif hit score by");
		orderedkeys.push(MOTIFHITFACTORKEY);
		parameters.put(MASTERSKIPPENALTYKEY, "-0.2");
		explanations.put(MASTERSKIPPENALTYKEY, "Penalty for skipping a filled column in alignment");
		orderedkeys.push(MASTERSKIPPENALTYKEY);

		parameters.put(STOPCODONKEY, "-15");
		explanations.put(STOPCODONKEY, "Value of stop codon in aligned fitting");
		orderedkeys.push(STOPCODONKEY);
		parameters.put(SCOREFACTORKEY, "0.1");
		explanations.put(SCOREFACTORKEY, "Factor for discarding lowscoring MotifHits");
		orderedkeys.push(SCOREFACTORKEY);
		parameters.put(MINSSSCORE, "600");
		explanations.put(MINSSSCORE, "Lowest acceptable sum of squared MotifHit scores");
		orderedkeys.push(MINSSSCORE);
		parameters.put(MINHITSCOREKEY, "15");
		explanations.put(MINHITSCOREKEY, "Lowest acceptable individual MotifHit score");
		orderedkeys.push(MINHITSCOREKEY);
		
    
		parameters.put(GENEKEY, "");
		explanations.put(GENEKEY, "gag, pro, pol or env");
		orderedkeys.push(GENEKEY);
		parameters.put(VIRUSGENUSKEY, "");
		explanations.put(VIRUSGENUSKEY, "a, b, c, d, e, l, s, g or o");
		orderedkeys.push(VIRUSGENUSKEY);
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(STRANDKEY, PRIMARY);
		explanations.put(STRANDKEY, PRIMARY + " or " + SECONDARY);
		orderedkeys.push(STRANDKEY);
		parameters.put(FIRSTDNASTARTKEY, "1");
		explanations.put(FIRSTDNASTARTKEY, "Earliest start in DNA");
		orderedkeys.push(FIRSTDNASTARTKEY);
		parameters.put(LASTDNASTARTKEY, "1");
		explanations.put(LASTDNASTARTKEY, "Last start in DNA");
		orderedkeys.push(LASTDNASTARTKEY);
		parameters.put(FIRSTDNAENDKEY, "200000");
		explanations.put(FIRSTDNAENDKEY, "First reasonable end position in DNA");
		orderedkeys.push(FIRSTDNAENDKEY);
		parameters.put(LASTDNAENDKEY, "200000");
		explanations.put(LASTDNAENDKEY, "Last position in DNA to consider");
		orderedkeys.push(LASTDNAENDKEY);
		parameters.put(OUTPUTFILEKEY, "");
		explanations.put(OUTPUTFILEKEY, "File for display by ORFIDview");
		orderedkeys.push(OUTPUTFILEKEY);
		parameters.put(PUTEINFILEKEY, "");
		explanations.put(PUTEINFILEKEY, "File for amino acid sequence");
		orderedkeys.push(PUTEINFILEKEY);
		parameters.put(CHAINNUMBERKEY, "");
		explanations.put(CHAINNUMBERKEY, "Chain used");
		orderedkeys.push(CHAINNUMBERKEY);

		parameters.put(DEBUGGINGKEY, NO);
		explanations.put(DEBUGGINGKEY, NO + " or " + YES);
		orderedkeys.push(DEBUGGINGKEY);
	} // end of constructor()
	
	
/**
* Performs normal initialization and constructs suitable output file names
* if not defined.
* @param	script	The script to fetch from, or null.
* @return	True if successful.
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		super.initialize(script);
		if (runFlag) {
			if ((getString(OUTPUTFILEKEY, "").length() == 0) & (getString(PUTEINFILEKEY, "").length() == 0)) {
				File[ ] newFiles = FileNamer.createPuteinAndOutputFiles(getString(STRANDKEY, PRIMARY), getString(CHAINNUMBERKEY, ""), getString(VIRUSGENUSKEY, ""), getString(GENEKEY, ""), false);
				parameters.put(OUTPUTFILEKEY, newFiles[0].getName());
				parameters.put(PUTEINFILEKEY, newFiles[1].getName());
			}
		}
	} // end of initialize(ParameterFileReader)
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 13";
  } //end of version()
	
/**
* Execute as specified above.
*/
	public String execute() throws RetroTectorException {

		orfids = new ORFID[6]; // first and last are dummies
		geneScripts = getStringArray(GENESCRIPTSKEY);
		if (geneScripts != null) { // biggie?
			ExecutorScriptReader script;
			ORFID or;
			for (int orf=0; orf<geneScripts.length; orf++) { // try first part of all components
				or = new ORFID();
				script = new ExecutorScriptReader(new File(RetroTectorEngine.currentDirectory(), geneScripts[orf]));
				or.initialize(script);
				String gname = or.getString(GENEKEY, "");
				if (gname.equalsIgnoreCase("Gag")) {
					orfids[1] = or;
					or.orfidNr = 1;
				} else if (gname.equalsIgnoreCase("Pro")) {
					orfids[2] = or;
					or.orfidNr = 2;
				} else if (gname.equalsIgnoreCase("Pol")) {
					orfids[3] = or;
					or.orfidNr = 3;
				} else if (gname.equalsIgnoreCase("Env")) {
					orfids[4] = or;
					or.orfidNr = 4;
				} else {
					haltError("Unrecognized Gene: " + gname);
				}
			}
		} else { // not biggie
			genename = getString(GENEKEY, "");
			if (genename.length() == 0) { // old type script
				genename = getString("FrameGene", "");
			}
			if (genename.equalsIgnoreCase("Gag")) {
				orfids[1] = this;
				orfidNr = 1;
			} else if (genename.equalsIgnoreCase("Pro")) {
				orfids[2] = this;
				orfidNr = 2;
			} else if (genename.equalsIgnoreCase("Pol")) {
				orfids[3] = this;
				orfidNr = 3;
			} else if (genename.equalsIgnoreCase("Env")) {
				orfids[4] = this;
				orfidNr = 4;
			} else {
				haltError("Unrecognized Gene: " + genename);
			}
		}
		try {
			for (int orfi=1; orfi<=4; orfi++) {
				if (orfids[orfi] != null) {
					if (orfids[orfi].makePutein().length() > 0) { // error message
						orfids[orfi] = null;
					}
				}
			}
			for (int orfi=1; orfi<=4; orfi++) {
				if (orfids[orfi] != null) {
					orfids[orfi].forceFinishPutein();
				}
			}
		} catch (FinishedException fe) {
			haltError("End of DNA or master reached");
		}
		return "";
		
	} // end of execute()
	

// setup and make LeadingDynamicMatrix for first section
	private final String makePutein() throws RetroTectorException, FinishedException {
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
			
// construct out files
			File[ ] newFiles = FileNamer.createPuteinAndOutputFiles(getString(STRANDKEY, PRIMARY), getString(CHAINNUMBERKEY, ""), getString(VIRUSGENUSKEY, ""), getString(GENEKEY, ""), false);
			parameters.put(OUTPUTFILEKEY, newFiles[0].getName());
			parameters.put(PUTEINFILEKEY, newFiles[1].getName());
		}
		
    nonAlignedScore = getFloat(NONALIGNEDSCOREKEY, 0.4f);
    stopCodonFactor = getFloat(STOPCODONFACTORKEY, -0.4f);
    frameShiftPenalty = getFloat(FRAMESHIFTPENALTYKEY, -1.5f);
    orfHexamerFactor = getFloat(ORFHEXAMERFACTORKEY, 0.2f);
    nonOrfHexamerFactor = getFloat(NONORFHEXAMERFACTORKEY, -0.1f);
    glycSiteFactor = getFloat(GLYCSITEFACTORKEY, 0.2f);
    inORFbonus = getFloat(INORFBONUSKEY, 0.1f);
    motifHitFactor = getFloat(MOTIFHITFACTORKEY, 0.2f);
		masterPenalty = getFloat(MASTERSKIPPENALTYKEY, -0.2f);
  
		stopValue = getInt(STOPCODONKEY, -15);
		scoreFactor = getFloat(SCOREFACTORKEY, 0.1f);
    minSSScore = getFloat(MINSSSCORE, 600f);
    minHitScore = getFloat(MINHITSCOREKEY, 15f);

    RetroTectorEngine.setCurrentDatabase(getString(DATABASEKEY, Executor.ORDINARYDATABASE));
    database = RetroTectorEngine.getCurrentDatabase();
// read master
		genename = getString(GENEKEY, "");
		if (genename.length() == 0) { // old type script
			genename = getString("FrameGene", "");
		}
		String masterName = getString(VIRUSGENUSKEY, "") + genename + "Alignment.txt";
		genusChar = Character.toLowerCase(getString(VIRUSGENUSKEY, "").charAt(0));
		master = new Alignment(database.getFile(masterName));

// read DNA
		String dnaName = getString(DNAFILEKEY, "");
		dna = getDNA(dnaName, !getString(STRANDKEY, PRIMARY).equals(SECONDARY));
		dnaStartFirst = getInt(FIRSTDNASTARTKEY, 1);
		dnaStartFirst = Math.max(dna.internalize(dnaStartFirst), 0);
		dnaStartLast = getInt(LASTDNASTARTKEY, 1);
		dnaStartLast = Math.max(dna.internalize(dnaStartLast), 0); 
		dnaEndFirst = getInt(FIRSTDNAENDKEY, 200000);
		dnaEndFirst = Math.min(dna.internalize(dnaEndFirst), dna.LENGTH - 1);
		dnaEndLast = getInt(LASTDNAENDKEY, 200000);
		dnaEndLast = Math.min(dna.internalize(dnaEndLast), dna.LENGTH - 1);

		slipperyMotif = (SlipperyMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SLIPPERYMOTIFKEY);
		slipperyMotif.refresh(new Motif.RefreshInfo(dna, 0, 0, null));
		kozakMotif = RetroTectorEngine.getCurrentDatabase().KOZAKMOTIF;
		kozakMotif.refresh(new Motif.RefreshInfo(dna, 3.0f, 3f, null));
		proteaseCleavageMotif = (ProteaseCleavageMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.PROTEASECLEAVAGEMOTIFKEY);
		proteaseCleavageMotif.refresh(new Motif.RefreshInfo(dna, 2.0f, 5.5f, null));
		pseudoKnotMotif = (PseudoKnotMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.PSEUDOKNOTMOTIFKEY);
		pseudoKnotMotif.refresh(new Motif.RefreshInfo(dna, 0, 0, null));
    acceptorMotif = (SpliceAcceptorMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SPLICEACCEPTORMOTIFKEY);
		acceptorMotif.refresh(new Motif.RefreshInfo(dna, 0, 0, null));
		gagStartMotif = new PuteinStartMotif(RetroTectorEngine.getCurrentDatabase(), 10000, genusChar, "" + Character.toUpperCase(genusChar) + "GagAlignment.txt");
		gagStartMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		proStartMotif = new PuteinStartMotif(RetroTectorEngine.getCurrentDatabase(), 10001, genusChar, "" + Character.toUpperCase(genusChar) + "ProAlignment.txt");
		proStartMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		polStartMotif = new PuteinStartMotif(RetroTectorEngine.getCurrentDatabase(), 10002, genusChar, "" + Character.toUpperCase(genusChar) + "PolAlignment.txt");
		polStartMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		envStartMotif = new PuteinStartMotif(RetroTectorEngine.getCurrentDatabase(), 10003, genusChar, "" + Character.toUpperCase(genusChar) + "EnvAlignment.txt");
		envStartMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		gagEndMotif = new PuteinEndMotif(RetroTectorEngine.getCurrentDatabase(), 10004, genusChar, "" + Character.toUpperCase(genusChar) + "GagAlignment.txt");
		gagEndMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		proEndMotif = new PuteinEndMotif(RetroTectorEngine.getCurrentDatabase(), 10005, genusChar, "" + Character.toUpperCase(genusChar) + "ProAlignment.txt");
		proEndMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		polEndMotif = new PuteinEndMotif(RetroTectorEngine.getCurrentDatabase(), 10006, genusChar, "" + Character.toUpperCase(genusChar) + "PolAlignment.txt");
		polEndMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		envEndMotif = new PuteinEndMotif(RetroTectorEngine.getCurrentDatabase(), 10007, genusChar, "" + Character.toUpperCase(genusChar) + "EnvAlignment.txt");
		envEndMotif.refresh(new Motif.RefreshInfo(dna, 1.0f, 5.5f, null));
		
		debugging = getString(DEBUGGINGKEY, NO).equals(YES);
		
// get hit information
		hitsInfo = new MotifHitsInfo(getStringArray(HITINFOKEY), dna, master, getString(SCRIPTPATHKEY, ""), database);
		if (hitsInfo.SSSCORES < minSSScore) {
			return "Too low sum of squared hit scores";
		}
		Stack hist = new Stack();
		boolean b;
		for (int m=0; m<hitsInfo.nrOfHitInfos(); m++) {
			b = true;
			if (!(hitsInfo.getHitInfo(m).POSINMASTER >= 0)) {
				b = false;
			}
			if (!hitsInfo.getHitInfo(m).usefulToORFID(database)) {
				b = false;
			}
			if (!(hitsInfo.getHitInfo(m).SCORE >= (hitsInfo.AVERSCORE * scoreFactor))) {
				b = false;
			}
			if (!(hitsInfo.getHitInfo(m).SCORE >= minHitScore)) {
				b = false;
			}
      if (b) {
				hist.push(hitsInfo.getHitInfo(m));
			}
		}
		if (hist.size() == 0) {
			return "No useful Motif hit";
		}
		hitinfos = new MotifHitInfo[hist.size()];
		hist.copyInto(hitinfos);
		hist = null;

		leadingInsideLimit = dnaStartLast - dnaStartFirst;
		trailingInsideLimit = dnaEndFirst - hitinfos[hitinfos.length - 1].HOTSPOT;
		
		if (debugging) {
			Utilities.outputString("Average hit score=" + hitsInfo.AVERSCORE);
		}
		if (!hitsInfo.inOrder(database)) {
			RetroTectorEngine.displayError(new RetroTectorException("ORFID", "Refused " + getString(SCRIPTPATHKEY, ""), "Hits severely out of order"), RetroTectorEngine.WARNINGLEVEL);
			return "Hits out of order in " + getString(SCRIPTPATHKEY, "");
		}

// create result files
		putein = new Putein(new ParameterBlock(this));
		puteinWriter = new ExecutorScriptWriter(new File(RetroTectorEngine.currentDirectory(), getString(PUTEINFILEKEY, Putein.PUTEINKEY)), "Puteinview");
		if (debugging) {
			outputWriter = new ExecutorScriptWriter(new File(RetroTectorEngine.currentDirectory(), getString(OUTPUTFILEKEY, "ORFIDoutput")), "ORFIDview");
		}

		for (int m=0; m<hitsInfo.nrOfHitInfos(); m++) {
			if (hitsInfo.getHitInfo(m).POSINMASTER >= 0) {
				if (debugging) {
					outputWriter.writeComment(hitsInfo.getHitInfo(m).toString() + ": Found at " + hitsInfo.getHitInfo(m).POSINMASTER);
				}
				hitsfound++;
			} else {
				if (debugging) {
					outputWriter.writeComment(hitsInfo.getHitInfo(m).toString() + ": Not found");
				}
			}
		}
		if (debugging) {
			outputWriter.writeSingleParameter(NROFHITSKEY, String.valueOf(hitsfound), false);
		}
    puteinWriter.writeSingleParameter(NROFHITSKEY, String.valueOf(hitsfound), false);
    if (hitsfound <= 0) { // should not be possible?
			if (debugging) {
				outputWriter.close();
			}
			puteinWriter.close();
      return "No hits";
		}
		if (debugging) {
			outputWriter.startMultiParameter(MASTERNAMESKEY, false);
			Alignment.AlignmentRow row;
			for (int s=0; (row=master.getRow(s)) != null; s++) {
				outputWriter.appendToMultiParameter(row.IDNUMBER + " " + row.ROWORIGIN, false);
				Utilities.outputString(row.IDNUMBER + " " + row.ROWORIGIN);
			}
			outputWriter.finishMultiParameter(false);
			outputWriter.startMultiParameter("ORFIDoutput", false);
		}
				
		dna.setMark(dnaStartFirst);
		Utilities.Rectangle r;

		startAt = dnaStartFirst;
		endAt = dnaEndLast;
		
		r = new Utilities.Rectangle(dnaStartFirst, 0, hitinfos[0].HOTSPOT - dnaStartFirst + 1, hitinfos[0].POSINMASTER + 1);
		showInfo("Building dynamic matrix for " + genename);
		ldynmatrix = new LeadingDynamicMatrix(r, new ParameterBlock(this), putein, leadingInsideLimit);
		showInfo("Building paths: " + genename);
		PathClass leadingPath = null;
		if (orfidNr == 1) {
			GagStartFitter gagf = new GagStartFitter();
			leadingPath = gagf.BESTGAGLEADER;
		} else if (orfidNr == 2) {
			GagProFitter gagprof = new GagProFitter(orfids[1], this);
			leadingPath = gagprof.BESTPROLEADER;
			if (orfids[1] != null) {
				orfids[1].finishPutein(gagprof.BESTGAGTRAILER);
			}
		} else if (orfidNr == 3) {
			if (orfids[2] != null) {
				ProPolFitter propolf = new ProPolFitter(orfids[2], this);
				leadingPath = propolf.BESTPOLLEADER;
				orfids[2].finishPutein(propolf.BESTPROTRAILER);
			} else if (orfids[1] != null) {
				DistantFitter distf = new DistantFitter(orfids[1], this);
				leadingPath = distf.BESTLEADER;
				orfids[1].finishPutein(distf.BESTTRAILER);
			} else {
				ProPolFitter propolf = new ProPolFitter(null, this);
				leadingPath = propolf.BESTPOLLEADER;
			}
		} else if (orfidNr == 4) {
			if (orfids[3] != null) {
				PolEnvFitter polenvf = new PolEnvFitter(orfids[3], this);
				leadingPath = polenvf.BESTENVLEADER;
				orfids[3].finishPutein(polenvf.BESTPOLTRAILER);
			} else if (orfids[2] != null) {
				DistantFitter distf = new DistantFitter(orfids[2], this);
				leadingPath = distf.BESTLEADER;
				orfids[2].finishPutein(distf.BESTTRAILER);
			} else if (orfids[1] != null) {
				DistantFitter distf = new DistantFitter(orfids[1], this);
				leadingPath = distf.BESTLEADER;
				orfids[1].finishPutein(distf.BESTTRAILER);
			} else {
				PolEnvFitter polenvf = new PolEnvFitter(null, this);
				leadingPath = polenvf.BESTENVLEADER;
			}
		} else {
			haltError("Unrecognized ORFID nr: " + orfidNr);
		}

		startAt = leadingPath.getFirstInDNA();
		if (startAt > dnaStartFirst + 3) {
			r = new Utilities.Rectangle(dnaStartFirst, 0, startAt - dnaStartFirst + 1,  1);
			ExonDynamicMatrix edm = new ExonDynamicMatrix(r, new ORFID.ParameterBlock(dna));
			edm.getPath().pushTo(putein);
		}
		leadingPath.pushTo(putein);
		puteinWriter.writeMultiParameter("LeadingInfo", leadingPath.toString());
		if (debugging) { // output all tried Paths
			PathClass[ ] paths = ldynmatrix.getPaths();
			Utilities.sort(paths);
			for (int le=1; le<paths.length; le++) {
				puteinWriter.writeMultiParameter("LeadingInfo" + (le + 1), paths[le].toString());
			}
		}

		
		for (int m=1; m<hitinfos.length; m++) { // go through all segments except last
			r = new Utilities.Rectangle(hitinfos[m - 1].HOTSPOT, hitinfos[m - 1].POSINMASTER, hitinfos[m].HOTSPOT - hitinfos[m - 1].HOTSPOT + 1,  hitinfos[m].POSINMASTER - hitinfos[m - 1].POSINMASTER + 1);
			showInfo("Building dynamic matrix for " + genename);
			MiddleDynamicMatrix mdynmatrix = new MiddleDynamicMatrix(r, new ParameterBlock(this), putein);
			
			mdynmatrix.getPath().pushTo(putein);
    }
// prepare for trailing segment
		r = new Utilities.Rectangle(hitinfos[hitinfos.length - 1].HOTSPOT, hitinfos[hitinfos.length - 1].POSINMASTER, dnaEndLast - hitinfos[hitinfos.length - 1].HOTSPOT + 1, master.MASTERLENGTH - hitinfos[hitinfos.length - 1].POSINMASTER);
		showInfo("Building dynamic matrix for " + genename);
		tdynmatrix = new TrailingDynamicMatrix(r, new ParameterBlock(this), putein, trailingInsideLimit);
		
		return "";
	} // end of makePutein()
	

	private final void finishPutein(PathClass path) throws RetroTectorException, FinishedException {
		if (path == null) {
			path = tdynmatrix.getBestPath();
		}
		endAt = path.getLastInDNA();
		path.pushTo(putein);
		puteinWriter.writeMultiParameter("TrailingInfo", path.toString());
		if (debugging) { // output all tried Paths
			PathClass[ ] paths = tdynmatrix.getPaths();
			Utilities.sort(paths);
			for (int le=1; le<paths.length; le++) {
				puteinWriter.writeMultiParameter("TrailingInfo" + (le + 1), paths[le].toString());
			}
		}
		if (endAt < dnaEndLast - 3) {
// enforce one codon - may be stop
			putein.pushElement(new PathElement(endAt + 1, endAt + 4, path.getLastElement().TOMASTER, path.getLastElement().TOMASTER, 0, 0));
		}
// let ExonDynamicMatrix do the work
		if (endAt < dnaEndLast - 6) {
			Utilities.Rectangle r = new Utilities.Rectangle(endAt + 4, path.getLastElement().TOMASTER, dnaEndLast - endAt - 4, 1);
			ExonDynamicMatrix edm = new ExonDynamicMatrix(r, new ORFID.ParameterBlock(dna));
			edm.getPath().pushTo(putein);
		}
		
		
		putein.finishSequence(startAt, endAt);
		
		if (debugging) {
			putein.toOutputFile(outputWriter);
			outputWriter.finishMultiParameter(false);
			writeInfo(outputWriter);
			outputWriter.close();
		}

		if (getString(TROUBLEKEY, null) != null) {
			puteinWriter.writeSingleParameter(TROUBLEKEY, getString(TROUBLEKEY, null), false);
		}
		if (RetroTectorEngine.getClusterMode() & putein.GENE.equalsIgnoreCase("Pol")) {
			String puteinLine = putein.mainLines[Putein.PUTEINLINEINDEX];
			String fourthLine = putein.mainLines[Putein.FOURTHLINEINDEX];
			int ind1 = fourthLine.indexOf(">") + 1;
			int ind2 = fourthLine.indexOf("<");
			puteinWriter.writeSingleParameter(Putein.POLCLASSKEY, Utilities.findBestProtein(puteinLine.substring(ind1, ind2), database.getPolProteins()), false);
		}
		putein.toPuteinFile(puteinWriter);
		writeInfo(puteinWriter);
		puteinWriter.writeComment("Execution time was " + (System.currentTimeMillis() - starttime) + " milliseconds");
		puteinWriter.close();
		
		Putein pu1 = putein;
		Putein pu2;
		while ((pu2 = pu1.straightenedPutein()) != null) {
			pu1 = pu2;
		}
		if (pu1 != putein) {
			putein = pu1;
			File[ ] newFiles = FileNamer.createPuteinAndOutputFiles(getString(STRANDKEY, PRIMARY), getString(CHAINNUMBERKEY, ""), getString(VIRUSGENUSKEY, ""), getString(GENEKEY, ""), true);
			puteinWriter = new ExecutorScriptWriter(newFiles[1], "Puteinview");
			puteinWriter.writeSingleParameter(NROFHITSKEY, String.valueOf(hitsfound), false);
			puteinWriter.writeSingleParameter("Straightened", YES, false);
			if (getString(TROUBLEKEY, null) != null) {
				puteinWriter.writeSingleParameter(TROUBLEKEY, getString(TROUBLEKEY, null), false);
			}
			if (RetroTectorEngine.getClusterMode() & putein.GENE.equalsIgnoreCase("Pol")) {
				String puteinLine = putein.mainLines[Putein.PUTEINLINEINDEX];
				String fourthLine = putein.mainLines[Putein.FOURTHLINEINDEX];
				int ind1 = fourthLine.indexOf(">") + 1;
				int ind2 = fourthLine.indexOf("<");
				puteinWriter.writeSingleParameter(Putein.POLCLASSKEY, Utilities.findBestProtein(puteinLine.substring(ind1, ind2), database.getPolProteins()), false);
			}
			putein.toPuteinFile(puteinWriter);
			writeInfo(puteinWriter);
			puteinWriter.writeComment("Execution time was " + (System.currentTimeMillis() - starttime) + " milliseconds");
			puteinWriter.close();

		}
		
		puteinFinished = true;
		
	} // end of finishPutein(PathClass)
	

	private final void forceFinishPutein() throws RetroTectorException, FinishedException {
		if (puteinFinished) {
			return;
		} else {
			if ("gag".equalsIgnoreCase(genename)) {
				GagProFitter gagprof = new GagProFitter(this, null);
				finishPutein(gagprof.BESTGAGTRAILER);
			} else if ("pro".equalsIgnoreCase(genename)) {
				ProPolFitter propolf = new ProPolFitter(this, null);
				finishPutein(propolf.BESTPROTRAILER);
			} else if ("pol".equalsIgnoreCase(genename)) {
				PolEnvFitter polenvf = new PolEnvFitter(this, null);
				finishPutein(polenvf.BESTPOLTRAILER);
			} else if ("env".equalsIgnoreCase(genename)) {
				EnvEndFitter envf = new EnvEndFitter();
				finishPutein(envf.BESTENVTRAILER);
			} else {
				haltError("Unrecognized Gene: " + genename);
			}
		}
	} // end of forceFinishPutein()
		
} // end of ORFID
