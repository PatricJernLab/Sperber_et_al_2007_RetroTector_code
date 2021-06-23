/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

import builtins.*;

/**
* Superclass for Path in various core matrices.
*/
public abstract class PathClass extends ParameterUser implements Scorable  {

/**
* Key for the coefficient to be applied to the score for PuteinEndMotif = "PuteinEndFactor";
*/
	public static final String PUTEINENDFACTORKEY = "PuteinEndFactor";

/**
* Key for the penalty to be applied for late shift = "LateShiftPenalty";
*/
	public static final String LATESHIFTPENALTYKEY = "LateShiftPenalty";

/**
* Key for the longest run to be seen as a late shift = "LateShiftMaxLength";
*/
	public static final String LATESHIFTMAXLENGTHKEY = "LateShiftMaxLength";

/**
* Key for the shortest run before to be considered a late shift = "PreFrameMinLength";
*/
	public static final String PREFRAMEMINLENGTHKEY = "PreFrameMinLength";

/**
* Lower case letter defining virus genus.
*/
	public char pathGenusChar;

/**
* Score (max 1.0) assigned by PuteinStartMotif or PuteinEndMotif.
*/
	public float alignmentScore = 0;

/**
* ROWORIGIN of Alignment row that produced alignmentScore.
*/
	public String alignmentScoreRow = null;
	
/**
* Weight for alignmentScore.
*/
	public float alignmentFactor = 3.0f;

/**
* Score (max 1.0) assigned by KozakMotif.
*/
	public float kozakScore = 0;

/**
* Weight for kozakScore.
*/
	public float kozakFactor = 0.5f;

/**
* Score (max 2.0) assigned by SlipperyMotif + PseudoKnotMotif.
*/
	public float shifterScore = 0;
	
/**
* Weight for shifterScore.
*/
	public float shifterFactor = 0.5f;

/**
* Position of SlipperyMotif hit in SlipperyMotif + PseudoKnotmotif combination corresponding to shifterScore.
*/
	public int shifterPos = Integer.MIN_VALUE;

/**
* Score (max 1.0) assigned by SpliceAcceptorMotif.
*/
	public float acceptorScore = 0;

/**
* Weight for acceptorScore.
*/
	public float acceptorFactor = 0.7f;

/**
* Position of SpliceAcceptorMotif hit corresponding to acceptorScore.
*/
	public int acceptorPos = Integer.MIN_VALUE;

/**
* Score (max 1.0) assigned by SiSeqMotif.
*/
	public float siseqScore = 0;

/**
* Weight for siseqScore.
*/
	public float siseqFactor = 0.5f;

/**
* Substring of acidString corresponding to acceptorScore.
*/
	public String siseqString = null;
	
/**
* Score (max 1.0) assigned by ProteaseCleavageMotif.
*/
	public float cleavageScore = 0;
	
/**
* Weight for cleavageScore.
*/
	public float cleavageFactor = 0.5f;

/**
* Score (max 1.0) assigned for terminating stop codon.
*/
	public float stopScore = 0;

/**
* Weight for stopScore.
*/
	public float stopFactor = 0.5f;

/**
* Bonus set by constructor if path starts (ends) within preferred range.
*/
	public float insideLimitsBonus = 0;

/**
* True if a longer Path without FIRSTSHIFT is available.
*/
	public boolean majored = false;

/**
* Penalty if majored is true.
*/
	public float majoredPenalty = -1.0f;
	
/**
* True if a late shift is present.
*/
	public boolean lateShift = false;

/**
* Penalty for late shift.
*/
	public float lateShiftPenalty = 0.0f;
	
/**
* True if this ends with a frame shift.
*/
	public boolean shiftEnd = false;

/**
* String of symbols	for the amino acids in the Path.
*/
	public String acidString;

/**
* The total score of acidString.
*/
	public float acidStringScore;
	
/**
* True if scores have been updated by ORFID.
*/
	private boolean scoreFinalized = false;

/**
* To be returned by fetchScore().
*/
	private float scoreToFetch = -Float.MAX_VALUE;
	
/**
* Default string description.
*/
	public String toString() {
		if (!scoreFinalized) {
			return null;
		}
		StringBuffer des = new StringBuffer();
		des.append("\nPutein string of length=");
		des.append(acidString.length());
		des.append(" and total score=");
		des.append(acidStringScore);
		des.append("\n");
		des.append(acidString);
		des.append("\nyielding average=");
		des.append(acidStringScore / (acidString.length() + 1));
		if (insideLimitsBonus == 0) {
			des.append("\nNot inside limits=0");
		} else {
			des.append("\nInside limits=");
			des.append(insideLimitsBonus);
		}
		if (alignmentScore != 0) {
			des.append("\nFor fit to alignment ");
			if (alignmentScoreRow != null) {
				des.append("(");
				des.append(alignmentScoreRow);
				des.append(") ");
			}
			des.append(alignmentScore);
			des.append("*");
			des.append(alignmentFactor);
		}
		if (kozakScore != 0) {
			des.append("\nKozak score=");
			des.append(kozakScore);
			des.append("*");
			des.append(kozakFactor);
		}
		if (shifterScore != 0) {
			des.append("\nFor SlipperyMotif at ");
			des.append(getParameterBlock().TARGETDNA.externalize(shifterPos));
			des.append(" ");
			des.append(shifterScore);
			des.append("*");
			des.append(shifterFactor);
		}
		if (acceptorScore != 0) {
			des.append("\nFor SpliceAcceptorMotif at ");
			des.append(getParameterBlock().TARGETDNA.externalize(acceptorPos));
			des.append(" ");
			des.append(acceptorScore);
			des.append("*");
			des.append(acceptorFactor);
		}
		if (siseqScore != 0) {
			des.append("\nvon Heijne score with ");
			des.append(siseqString);
			des.append(" =");
			des.append(siseqScore);
			des.append("*");
			des.append(siseqFactor);
		}
		if (cleavageScore != 0) {
			des.append("\nFor cleavage site=");
			des.append(cleavageScore);
			des.append("*");
			des.append(cleavageFactor);
		}
		if (stopScore != 0) {
			des.append("\nFor stop codon ");
			des.append(stopScore);
			des.append("*");
			des.append(stopFactor);
		}
		if (majored) {
			des.append("\nMajored:");
			des.append(majoredPenalty);
		}
		if (lateShift) {
			des.append("\nFor late shift:");
			des.append(lateShiftPenalty);
		}
		des.append("\nYielding path score = ");
		des.append(fetchScore());
		if (shiftEnd) {
			des.append("\nEnds with shift");
		}
		return des.toString();
	} // end of toString()

/**
* @return	The actual PathElements.
*/
	abstract public PathElement[ ] getPath();

/**
* @return	The number of stop codons in the Path.
*/
	abstract public float getStopCount();
	
/**
* @return	The number of shifts in the Path.
*/
	abstract public float getShiftCount();
	
/**
* @return	The gene of the Putein where this Path belongs.
*/
	abstract public String getGeneName();

/**
* @return	The ORFID.ParameterBlock used.
*/
	abstract public ORFID.ParameterBlock getParameterBlock();
	
/**
* @param ac	An AcidSequence to add this to.
*/
	public void pushTo(AcidSequence ac) throws RetroTectorException {
		PathElement[ ] pe = getPath();
		for (int i=0; i<pe.length; i++) {
			ac.pushElement(pe[i]);
		}
	} // end of pushTo(AcidSequence)
		
/**
*	@return	The first absolute internal position in TARGETDNA in this.
*/
	public int getFirstInDNA() {
		return getPath()[0].FROMDNA;
	} // end of getFirstInDNA()

/**
*	@return	The last absolute internal position in TARGETDNA in this.
*/
	public int getLastInDNA() {
		PathElement[ ] pe = getPath();
		return pe[pe.length - 1].TODNA - 1;
	} // end of getFirstInDNA()
	
/**
*	@return	The last PathElelemnt this.
*/
	public PathElement getLastElement() {
		PathElement[ ] pe = getPath();
		return pe[pe.length - 1];
	} // end of getLastElement()
	
/**
* @return	The total score of the Path.
*/
	public float fetchScore() {
		return scoreToFetch;
	} // end of fetchScore()
	
/**
* To calculate scoreToFetch.
*/
	public void finalizePath() {
		float x = acidStringScore / (acidString.length() + 1);
		x += insideLimitsBonus;
		x += alignmentScore * alignmentFactor;
		x += kozakScore * kozakFactor;
		x += stopScore * stopFactor;
		x += shifterScore * shifterFactor;
		x += cleavageScore * cleavageFactor;
		x += acceptorScore * acceptorFactor;
		x += siseqScore * siseqFactor;
		if (majored) {
			x += majoredPenalty;
		}
		if (lateShift) {
			x += lateShiftPenalty;
		}
		scoreToFetch = x;
		scoreFinalized = true;
	} // end of finalizePath()
	
/**
* @return	True if finalizePath has been done
*/
	public boolean isFinalized() {
		return scoreFinalized;
	} // end of isFinalized()
	
} // end of PathClass
