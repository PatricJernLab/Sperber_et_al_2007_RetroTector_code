/*
* @(#)Matrix.java	1.0 97/07/22
* 
* Copyright (©) 1997-2006, Tore Eriksson, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
*
* @author  Tore Eriksson & G. Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

/**
* An all-purpose class for similarity matrices.
* Subclassed for different types of matrices.
*/
public abstract class Matrix {

/**
* The "official" AcidMatrix.
*/
	public static AcidMatrix acidMATRIX = null;

/**
* The "official" BaseMatrix.
*/
	public static BaseMatrix baseMATRIX = null;

/**
* First index is valid codon or base code as got from Motif.
* Second index is codon or base code as got from a Compactor.
*/
  public float[ ][ ] floatMatrix;

} // end of Matrix
