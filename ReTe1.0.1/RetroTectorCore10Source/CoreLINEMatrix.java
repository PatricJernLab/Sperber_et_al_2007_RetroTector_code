/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 24/9 -06
* Beautified 24/9 -06
*/

package retrotectorcore;

import retrotector.*;

/**
* Dynamic programming matrix similar to CoreALUMatrix but with only two last rows in memory
* and accepting alignments shorter then the template.
*/
public class CoreLINEMatrix {
	

/**
* Marker for dead end of path.
*/
	public static final float DEAD = Float.MAX_VALUE;

/**
* Longest path end in template.
*/
	public final int TEMPLATEEND;

/**
* Longest path end in searched sequence.
*/
	public final int SEARCHEND;
	
/**
* Coordinates of last position in template and sequence of three contiguouus identities in path.
*/
	public final int[ ] LASTTRIAD;

// first of sequences of base codes
	private int[ ] template;
// sequence of base codes to compare to template
	private int[ ] search;

	private float[ ] latestRow;
	private float[ ] newRow;
// penalty for mismatching bases
	private float mismatchPenalty;
// penalty for deletion of 1 base in either sequence
	private float delPenalty;
// decay of penalty when bases identical
	private float decayFactor;
// when penalty passes this, goodbye
	private float roof;
	
	private int rowchar;
	

/**
* Constructor.
* @param	template				Array of base codes.
* @param	search					Array of base codes.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*/
	public CoreLINEMatrix(int[ ] template, int[ ] search, float mismatchPenalty, float delPenalty, float decayFactor, float roof) {
		this.template = template;
		this.search = search;
		this.mismatchPenalty = mismatchPenalty;
		this.delPenalty = delPenalty;
		this.decayFactor = decayFactor;
		this.roof = roof;
		int templatelength = template.length;
		float[ ] deads = new float[templatelength];
		for (int d=0; d<deads.length; d++) {
			deads[d] = DEAD;
		}
	
// fill first row with DEAD
		newRow = (float[ ]) deads.clone();
		int lastfirstfree = 0;
		int lastlastfree = 1;
		float f;
		newRow[0] = 0;
		for (int i=1; (i<templatelength) && ((f=newRow[i - 1] * decayFactor + delPenalty) <= roof); i++) {
			newRow[i] = f;
			lastlastfree = i;
		}
		
		boolean deadend = false; // true if no progress possible
		int newRowNr = 1;
		int lf;
		float rowmin;
		int rowminPos;
		int[ ] lastTriad = null;
		
		while (!deadend && (newRowNr < search.length)) {
			deadend = true; // provisional value for deadend
			latestRow = newRow;
			newRow = (float[ ]) deads.clone();
			rowchar = search[newRowNr];
			rowmin = DEAD;
			rowminPos = -1;
			lf = 0;
			for (int i=lastfirstfree; (i<templatelength) && (((f=scoreAt(i)) != DEAD) | (i<=lastlastfree)); i++) {
				newRow[i] = f;
				if (f < rowmin) {
					rowmin = f;
					rowminPos = i;
				}
				if ((f == DEAD) & (i == lastfirstfree)) {
					lastfirstfree++;
				}
				if (f != DEAD) {
					lf = i;
					deadend = false;
				}
			}
			lastlastfree = lf;
			if ((rowminPos > 1) && (newRowNr > 1) && (rowchar == template[rowminPos]) && (search[newRowNr - 1] == template[rowminPos - 1]) && (search[newRowNr - 2] == template[rowminPos - 2])) {
				if (lastTriad == null) {
					lastTriad = new int[2];
				}
				lastTriad[0] = rowminPos;
				lastTriad[1] = newRowNr;
			}
			newRowNr++;
		}
		
		if (newRowNr >= search.length) {
			latestRow = newRow;
			SEARCHEND = search.length - 1;
		} else {
			SEARCHEND = newRowNr - 2;
		}
		float minf = DEAD;
		int mini = -1;
		for (int i=0; i<latestRow.length; i++) {
			if (latestRow[i] < minf) {
				minf = latestRow[i];
				mini = i;
			}
		}
		TEMPLATEEND = mini;
		LASTTRIAD = lastTriad;
	} // end of constructor(int[ ], int[ ], float, float, float, float)
	
  
// appropriate contents of one cell in matrix
	private final float scoreAt(int s1) {
		float f;
		float f2;
		if (s1 == 0) {
			f = latestRow[0] * decayFactor + delPenalty;
		} else {
			f = latestRow[s1 - 1] * decayFactor;
			if (template[s1] != rowchar) {
				f += mismatchPenalty;
			}
			f2 = latestRow[s1] * decayFactor + delPenalty;
			f = Math.min(f, f2);
			f2 = newRow[s1 - 1] * decayFactor + delPenalty;
			f = Math.min(f, f2);
		}
		if (f > roof) {
			return DEAD;
		} else {
			return f;
		}
	} // end of scoreAt(int)
		
} // end of CoreLINEMatrix

