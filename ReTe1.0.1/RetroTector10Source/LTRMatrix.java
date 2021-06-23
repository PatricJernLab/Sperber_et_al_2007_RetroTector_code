/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

import retrotectorcore.*;

/**
* Interface to CoreLTRMatrix
*/
public class LTRMatrix {
	
/**
* Longest path end in first sequence.
*/
	public final int SEQ1END;

/**
* Longest path end in second sequence.
*/
	public final int SEQ2END;

	private CoreLTRMatrix coreLTRMatrix;
  
/**
* Constructor.
* @param	sequence1				Array of base codes.
* @param	sequence2				Array of base codes.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*/
	public LTRMatrix(int[ ] sequence1, int[ ] sequence2, float mismatchPenalty, float delPenalty, float decayFactor, float roof) {
	
		coreLTRMatrix = new CoreLTRMatrix(sequence1, sequence2, mismatchPenalty, delPenalty, decayFactor,  roof);
		
		SEQ1END = coreLTRMatrix.SEQ1END;
		SEQ2END = coreLTRMatrix.SEQ2END;
  } // end of constructor(int[ ], int[ ], float, float, float, float)

/**
* @param	seq2hook	A position in the seq2 direction.
* @return	Corresponding position on path in seq1 direction, or -1.
*/
	public final int seq1intercept(int seq2hook) {
		return coreLTRMatrix.seq1intercept(seq2hook);
	} // end of seq1intercept(int)
	
/**
* @param	seq1hook	A position in the seq1 direction.
* @return	Corresponding position on path in seq2 direction, or -1.
*/
	public final int seq2intercept(int seq1hook) {
		return coreLTRMatrix.seq2intercept(seq1hook);
	} // end of seq2intercept(int)

/**
* Not in use at present.
* @return	Value in remote corner of matrix.
*/
	public final float remoteCorner() {
		return coreLTRMatrix.remoteCorner();
	} // end of remoteCorner()

/**
* @return	Array of indices in sequence1 and sequence2 with identity. Not in use at present.
*/
	public final int[ ] lastIdentity() {
		return coreLTRMatrix.lastIdentity();
	} // end of lastIdentity()
	
} // end of LTRMatrix
