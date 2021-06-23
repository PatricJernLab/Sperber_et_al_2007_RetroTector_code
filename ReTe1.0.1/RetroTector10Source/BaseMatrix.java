/*
* @(#)BaseMatrix.java	1.0 97/07/22
* 
* Copyright (©) 1997-2006, Tore Eriksson, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Tore Eriksson & G. Sperber
* @version 19/9 -06
* Beautified 19/9 -06
*/

package retrotector;

/**
* DNA base similarity matrix.
*/
public final class BaseMatrix extends Matrix {

/**
* Creates a new Matrix.baseMATRIX if its present CONSERVEDFACTOR different from newCFactor.
* @param	newCFactor	New value for CONSERVEDFACTOR.
* @return	New Matrix.baseMATRIX.
*/
  public final static BaseMatrix refreshBaseMatrix(float newCFactor) {
    if ((Matrix.baseMATRIX == null) || (Matrix.baseMATRIX.CONSERVEDFACTOR != newCFactor)) {
      Matrix.baseMATRIX = new BaseMatrix(newCFactor);
    }
    return Matrix.baseMATRIX;
  } // end of refreshBaseMatrix(float)
  
  
/* Symmetric 5x5 matrix of similarity between base codes. */
 private static final byte[ ][ ] SCOREMATRIX = {
		{4,-2,-2,-2,0},
		{-2,4,-2,-2,0},
		{-2,-2,4,-2,0},
		{-2,-2,-2,4,0},
		{0, 0, 0, 0, 0}
	};

/**
* Score assigned to indefinite base = 0.
*/
  public static final float INDEFINITESCORE = 0.0f;

/**
* Bonus factor for conserved base (uppercase in Motif string).
*/
  public final float CONSERVEDFACTOR;

/**
* Constructor. Only accessible through refreshBaseMatrix.
*/
  private BaseMatrix(float cfac) {
		CONSERVEDFACTOR = cfac;
// 4 normal and 4 conserved bases x 4 ordinary bases and ambiguous base.
		floatMatrix = new float[8][5];
		for (int x=0; x<4; x++) {
			for (int y=0; y<4; y++) {
				floatMatrix[x][y] = SCOREMATRIX[x][y] * 1.0f;
				floatMatrix[x + 4][y] = SCOREMATRIX[x][y] * CONSERVEDFACTOR;
			}
			floatMatrix[x][4] = INDEFINITESCORE;
			floatMatrix[x + 4][4] = INDEFINITESCORE;
		}
  } // end of constructor(float)

}
