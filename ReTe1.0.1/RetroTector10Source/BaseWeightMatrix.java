/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 20/9 -06
* Beautified 20/9 -06
*/
package retrotector;

/**
* Class representing a base weight matrix. It is scored by DNA.baseMatrixScoreAt().
*/
public class BaseWeightMatrix {

/**
* The number of rows in the matrix.
*/
	public final int LENGTH;
	
/**
* The highest possible score = sum of row maxima.
*/
	public final float MAXSCORE;
	
/**
* The weights, in a [4][LENGTH] matrix, indexed by DNA base codes according to base Compactor.
*/
	final float[ ][ ] WEIGHTS;
	
/**
* Constructor, using a set of lines like
* g   a   t   c
* 4   0   53  6
* 0   61  2   0
* 0   0   63  0
* 0   61  2   0
* 0   62  1   0
* 12  43  1   7
* 11  29  15  8
*
* @param	lines	A String array according to the above.
*/
	public BaseWeightMatrix(String[ ] lines) throws RetroTectorException {
		LENGTH = lines.length - 1;
		WEIGHTS = new float[4][LENGTH];
		int[ ] columnCodes = new int[4];
		String[ ] ss = Utilities.splitString(lines[0]);
		for (int i=0; i<4; i++) {
			columnCodes[i] = Compactor.BASECOMPACTOR.charToIntId(ss[i].charAt(0));
		}
		float maxscore = 0.0f;
		for (int li=1; li<lines.length; li++) {
			ss = Utilities.splitString(lines[li]);
			for (int ii=0; ii<4; ii++) {
				WEIGHTS[columnCodes[ii]][li - 1] = Utilities.decodeFloat(ss[ii]);
			}
			maxscore += Math.max(Math.max(WEIGHTS[0][li - 1], WEIGHTS[1][li - 1]), 
							Math.max(WEIGHTS[2][li - 1], WEIGHTS[3][li - 1]));
		}
		MAXSCORE = maxscore;
	} // end of constructor(String[ ])
	
}