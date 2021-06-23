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
* Dynamic programming matrix for LTR pairing.
*/
public class CoreLTRMatrix {
	

/**
* Marker for dead end of path.
*/
	public static final float DEAD = Float.MAX_VALUE;

/**
* Mass of dead.
*/
	static final float[ ] DEADS = new float[LTRPair.MAXLEADING + LTRPair.MAXTRAILING + 100];
	
	static {
		for (int d=0; d<DEADS.length; d++) {
			DEADS[d] = DEAD;
		}
	} // end of static initializer


/**
* Longest path end in first sequence.
*/
	public int SEQ1END;

/**
* Longest path end in second sequence.
*/
	public int SEQ2END;

// first of sequences of base codes
	private int[ ] seq1;
// sequence of base codes to compare to seq1
	private int[ ] seq2;

// seq1 along first index, seq2 second
	private float[ ][ ] matrix;
// penalty for mismatching bases
	private float mismatchPenalty;
// penalty for deletion of 1 base in either sequence
	private float delPenalty;
// decay of penalty when bases identical
	private float decayFactor;
// when penalty passes this, goodbye
	private float roof;
	

/**
* Constructor.
* @param	sequence1				Array of base codes.
* @param	sequence2				Array of base codes.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*/
	public CoreLTRMatrix(int[ ] sequence1, int[ ] sequence2, float mismatchPenalty, float delPenalty, float decayFactor, float roof) {
		seq1 = sequence1;
		seq2 = sequence2;
		matrix = new float[seq1.length][seq2.length];
		this.mismatchPenalty = mismatchPenalty;
		this.delPenalty = delPenalty;
		this.decayFactor = decayFactor;
		this.roof = roof;
	
// fill first row with DEAD
		System.arraycopy(DEADS, 0, matrix[0], 0, matrix[0].length);
		matrix[0][0] = 0;
		
		boolean deadend = false; // true if no progress possible
		boolean filled = false; // true if opposite corner reached

		int sofar1 = 0; // index of next position to handle in seq1 dimension
		int sofar2 = 0; // index of next position to handle in seq2 dimension
		int free1 = 0; // index of first non-dead position in seq1 dimension
		int free2 = 0; // index of first non-dead position in seq2 dimension

		while (!filled & !deadend) {
			boolean dend = true; // provisional value for deadend
			if (sofar1 < seq1.length) {
				sofar1++;
				if (sofar1 < matrix.length) { // fill next line of matrix with DEAD
					System.arraycopy(DEADS, 0, matrix[sofar1], 0, matrix[sofar1].length);
				}
			}
			if (sofar2 < seq2.length) {
				sofar2++;
			}
			if (sofar2 < seq2.length) { // fill new column
				for (int so1=free1; so1<sofar1; so1++) {
					if ((matrix[so1][sofar2] = scoreAt(so1, sofar2)) != DEAD) {
						dend = false;
					} else if (free1 == so1) {
						free1++;
					}
				}
			}
			if (sofar1 < seq1.length) { // fill new line
				for (int so2=free2; so2<sofar2; so2++) {
					if ((matrix[sofar1][so2] = scoreAt(sofar1, so2)) != DEAD) {
						dend = false;
					} else if (free2 == so2) {
						free2++;
					}
				}
			}
			if ((sofar1 < seq1.length) & (sofar2 < seq2.length)) { // fill corner
				if ((matrix[sofar1][sofar2] = scoreAt(sofar1, sofar2)) != DEAD) {
					dend = false;
				}
			}
			deadend = dend;
			if ((sofar1 == seq1.length) & (sofar2 == seq2.length)) {
				filled = true;
			}
		}
		
		SEQ1END = sofar1 - 1;
		SEQ2END = sofar2 - 1;
	} // end of constructor(int[ ], int[ ], float, float, float, float)
	
/**
* @param	seq2hook	A position in the seq2 direction.
* @return	Corresponding position on path in seq1 direction, or -1.
*/
	public final int seq1intercept(int seq2hook) {
		if (seq2hook > SEQ2END) {
			return -1;
		}
		
		int intercept = 0;
// find lowest score in seq2hook line
		float minmat = matrix[0][seq2hook];
		for (int i=1; i<=SEQ1END; i++) {
			if (matrix[i][seq2hook] < minmat) {
				minmat = matrix[i][seq2hook];
				intercept = i;
			}
		}
		
		return intercept;
	} // end of seq1intercept(int)
	
/**
* @param	seq1hook	A position in the seq1 direction.
* @return	Corresponding position on path in seq2 direction, or -1.
*/
	public final int seq2intercept(int seq1hook) {
		if (seq1hook > SEQ1END) {
			return -1;
		}
		
		int intercept = 0;
// find lowest score in seq1hook column
		float minmat = matrix[seq1hook][0];
		for (int i=1; i<=SEQ2END; i++) {
			if (matrix[seq1hook][i] < minmat) {
				minmat = matrix[seq1hook][i];
				intercept = i;
			}
		}
		
		return intercept;
	} // end of seq2intercept()

/**
* @return	Array of indices in sequence1 and sequence2 with identity. Not in use at present.
*/
	public final int[ ] lastIdentity() {
		int[ ] result = new int[2];
		int isum;
		int i1;
		int i2;
		if (seq1[SEQ1END] == seq2[SEQ2END]) {
			result[0] = SEQ1END;
			result[1] = SEQ2END;
			return result;
		} else {
			int id = 2;
			for (isum=SEQ1END+SEQ2END-1; isum>0; isum--) {
				i2 = SEQ2END;
				i1 = isum - i2;
				for (int i=0; i<id; i++) {
					if ((i2 >= 0) && (i1 <= SEQ1END) && (seq1[i1] == seq2[i2]) && (matrix[i1][i2] != DEAD)) {
						result[0] = i1;
						result[1] = i2;
						return result;
					}
				}
				id++;
			}
			return null;
		}
	} // end of lastIdentity
	
/**
* @return	Value in remote corner of matrix.
*/
	public final float remoteCorner() {
		float[ ] f =  matrix[matrix.length - 1];
		return f[f.length - 1];
	} // end of remoteCorner()
  
// appropriate contents of one cell in matrix
	private final float scoreAt(int s1, int s2) {
		float f;
		float f2;
		if (s1 == 0) {
			f = matrix[0][s2 - 1] * decayFactor + delPenalty;
		} else if (s2 == 0) {
			f = matrix[s1 - 1][0] * decayFactor + delPenalty;
		} else {
			f = matrix[s1 - 1][s2 - 1] * decayFactor;
			if (seq1[s1] != seq2[s2]) {
				f += mismatchPenalty;
			}
			f2 = matrix[s1][s2 - 1] * decayFactor + delPenalty;
			f = Math.min(f, f2);
			f2 = matrix[s1 - 1][s2] * decayFactor + delPenalty;
			f = Math.min(f, f2);
		}
		if (f > roof) {
			return DEAD;
		} else {
			return f;
		}
	} // end of scoreAt(int, int)
	
} // end of CoreLTRMatrix

