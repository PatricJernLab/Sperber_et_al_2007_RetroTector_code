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
* Interface to CoreLINEMatrix
*/
public class LINEMatrix {
	
/**
* Longest path end in template.
*/
	public final int TEMPLATEEND;

/**
* Longest path end in searched sequence.
*/
	public final int SEARCHEND;

/**
* Coordinates of last position in last sequence of three identities in path.
*/
	public final int[ ] LASTTRIAD;

	private CoreLINEMatrix coreLINEMatrix;
  
/**
* Constructor.
* @param	template				Array of base codes.
* @param	search					Array of base codes.
* @param	mismatchPenalty	Penalty for mismatching bases.
* @param	delPenalty			Penalty for deletion of 1 base in either sequence.
* @param	decayFactor			Decay of penalty when bases identical.
* @param	roof						When penalty exceeds this, goodbye.
*/
	public LINEMatrix(int[ ] template, int[ ] search, float mismatchPenalty, float delPenalty, float decayFactor, float roof) {
	
		coreLINEMatrix = new CoreLINEMatrix(template, search, mismatchPenalty, delPenalty, decayFactor,  roof);
		
		TEMPLATEEND = coreLINEMatrix.TEMPLATEEND;
		SEARCHEND = coreLINEMatrix.SEARCHEND;
		LASTTRIAD = coreLINEMatrix.LASTTRIAD;
  } // end of constructor(int[ ], int[ ], float, float, float)
	

} // end of LTRMatrix
