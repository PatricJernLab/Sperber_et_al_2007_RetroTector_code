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
* Dynamic programming matrix, primarily for ALU searching.  May also be used for LINE searching etc.
*/
public class CoreALUMatrix {

/**
* Marker for dead end of path.
*/
  public static final float DEAD = Float.MAX_VALUE;

/**
* Mass of dead.
*/
  static final float[ ] DEADS = new float[11000];
  
// fills with DEAD
  static {
    for (int d=0; d<DEADS.length; d++) {
      DEADS[d] = DEAD;
    }
  } // end of static initializer

/**
* Utility interface to constructor.
* @param	alu							ALU, as base codes.
* @param	start1					Index of where to start in ALU.
* @param	dna2						DNA to search in.
* @param	start2					Internal position to start at in dna2.
* @param	length2					Length of sequence to use in dna2.
* @param	forwards				If true, go towards higher (internal) positions.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*	@return	Relevant CoreALUMatrix, or null if penalty exceeded roof.
*/
  public static CoreALUMatrix getCoreALUMatrix(int[ ] alu, int start1, DNA dna2, int start2, int length2, boolean forwards, float mismatchPenalty, float delPenalty, float decayFactor, float roof) {
	
    int[ ] aluseq = null;
    int[ ] seq2 = null;
    int fact = 1;
// find how long stretches to compare
    if (!forwards) {
      fact = -1;
      aluseq = new int[start1 + 1];
			length2 = Math.min(length2, start2);
    } else {
      aluseq = new int[alu.length - start1];
			length2 = Math.min(length2, dna2.LENGTH - start2);
		}
// fill aluseq
		for (int i1=0; i1<aluseq.length; i1++) {
			aluseq[i1] = alu[start1 + fact * i1];
		}
// fill seq2
    try {
      seq2 = new int[length2];
      for (int i2=0; i2<seq2.length; i2++) {
        seq2[i2] = dna2.get2bit(start2 + fact * i2);
      }
    } catch (ArrayIndexOutOfBoundsException e) { // for debugging
      return null;
    }
    try {
			return new CoreALUMatrix(aluseq, seq2, mismatchPenalty, delPenalty, decayFactor, roof);
		} catch (Exception e) {
			return null;
		}
  } // end of getCoreALUMatrix(int[ ], int, DNA, int, int, boolean, float, float, float, float)
	
  
/**
* Index of longest path end in DNA sequence.
*/
  public int SEQ2END;

// sequence from ALU as base codes
  private int[ ] aluseq;
// sequence to compare to aluseq, as base codes
  private int[ ] seq2;

// scores of best paths, with ALU along first index
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
* Throws RetroTectorException("Unsuccessful") if end of sequence1 not reached.
* @param	sequence1				Array of base codes from ALU.
* @param	sequence2				Array of base codes from DNA.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*/
  public CoreALUMatrix(int[ ] sequence1, int[ ] sequence2, float mismatchPenalty, float delPenalty, float decayFactor, float roof) throws RetroTectorException {
    aluseq = sequence1;
    seq2 = sequence2;
    matrix = new float[aluseq.length][seq2.length];
    this.mismatchPenalty = mismatchPenalty;
    this.delPenalty = delPenalty;
    this.decayFactor = decayFactor;
    this.roof = roof;
  
// fill first row with DEAD
    System.arraycopy(DEADS, 0, matrix[0], 0, matrix[0].length);
    matrix[0][0] = 0;
    
    boolean deadend = false; // true if no progress possible
    boolean filled = false; // true if opposite corner reached

    int sofar1 = 0; // index of next position to handle in aluseq dimension
    int sofar2 = 0; // index of next position to handle in seq2 dimension
    int free1 = 0; // index of first non-dead position in aluseq dimension
    int free2 = 0; // index of first non-dead position in seq2 dimension

// build matrix
    while (!filled & !deadend) { // expand by one line and one column, if possible
      boolean dend = true; // provisional value for deadend
      if (sofar1 < aluseq.length) {
        sofar1++;
        if (sofar1 < matrix.length) { // fill next line of matrix with DEAD
          System.arraycopy(DEADS, 0, matrix[sofar1], 0, matrix[sofar1].length);
        }
      }
      if (sofar2 < seq2.length) {
        sofar2++;
      }
      if (sofar2 < seq2.length) { // fill new column
        for (int so1=free1; so1<sofar1; so1++) { // those with DEAD to the left useless
          if ((matrix[so1][sofar2] = scoreAt(so1, sofar2)) != DEAD) {
            dend = false;
          } else if (free1 == so1) {
            free1++;
          }
        }
      }
      if (sofar1 < aluseq.length) { // fill new line
        for (int so2=free2; so2<sofar2; so2++) { // those with DEAD below useless
          if ((matrix[sofar1][so2] = scoreAt(sofar1, so2)) != DEAD) {
            dend = false;
          } else if (free2 == so2) {
            free2++;
          }
        }
      }
      if ((sofar1 < aluseq.length) & (sofar2 < seq2.length)) { // fill corner element
        if ((matrix[sofar1][sofar2] = scoreAt(sofar1, sofar2)) != DEAD) {
          dend = false;
        }
      }
      deadend = dend;
      if ((sofar1 == aluseq.length) & (sofar2 == seq2.length)) { // matrix filled
        filled = true;
      }
    }
    
    if (sofar1 < aluseq.length) { // end of ALU template was not reached
			throw new RetroTectorException("Unsuccessful");
		}
    SEQ2END = sofar2 - 1;
  } // end of constructor(int[ ], int[ ], float, float, float, float)
  
/**
* @return	Index of element in sequence2 where best path ends
*/
  public final int intercept() {
    
    int interc = 0;
// find lowest score in last column
    float minmat = matrix[matrix.length - 1][0];
    for (int i=1; i<=SEQ2END; i++) {
      if (matrix[matrix.length - 1][i] < minmat) {
        minmat = matrix[matrix.length - 1][i];
        interc = i;
      }
    }
    
    return interc;
  } // end of intercept()

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
      if (aluseq[s1] != seq2[s2]) {
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
  
} // end of CoreALUMatrix
