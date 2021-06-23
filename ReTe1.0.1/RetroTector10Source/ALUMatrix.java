/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 18/9 -06
* Beautified 18/9 -06
*/

package retrotector;

import retrotectorcore.*;

/**
* Interface to CoreALUMatrix
*/
public class ALUMatrix {

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
*	@return	Relevant ALUMatrix, or null if penalty exceeded roof.
*/
  public static ALUMatrix getALUMatrix(int[ ] alu, int start1, DNA dna2, int start2, int length2, boolean forwards, float mismatchPenalty, float delPenalty, float decayFactor, float roof) {
	
		CoreALUMatrix c = CoreALUMatrix.getCoreALUMatrix(alu, start1, dna2, start2, length2, forwards, mismatchPenalty, delPenalty, decayFactor, roof);
		
		if (c == null) {
			return null;
		} else {
			return new ALUMatrix(c);
		}
	
  } // end of getALUMatrix(int[ ], int, DNA, int, int, boolean, float, float, float, float)
	
	
	private CoreALUMatrix coreALUMatrix;
  
	private ALUMatrix(CoreALUMatrix cam) {
		coreALUMatrix = cam;
	} // end of ALUMatrix(CoreALUMatrix)

/**
* Constructor.
* @param	sequence1				Array of base codes from ALU.
* @param	sequence2				Array of base codes from DNA.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*/
  public ALUMatrix(int[ ] sequence1, int[ ] sequence2, float mismatchPenalty, float delPenalty, float decayFactor, float roof) throws RetroTectorException {
	
		try {
			coreALUMatrix = new CoreALUMatrix(sequence1, sequence2, mismatchPenalty, delPenalty, decayFactor, roof);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreALUMatrix, "ALUMatrix", rte);
		}
		
  } // end of constructor(int[ ], int[ ], float, float, float, float)
  
/**
* @return	Index of element in sequence2 where best path ends
*/
  public int intercept() {
		int i = coreALUMatrix.intercept();
		return i;
  } // end of intercept()

/**
* @return	Value in remote corner of matrix. Not in use at present.
*/
	public float remoteCorner() {
		return coreALUMatrix.remoteCorner();
	} // end of remoteCorner()

}