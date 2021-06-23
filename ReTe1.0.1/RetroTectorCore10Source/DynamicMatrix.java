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
import builtins.*;
import java.util.*;

/**
* Abstract parent of classes performing dynamic programming.
*/
abstract class DynamicMatrix {

	
/**
* DNA (x-direction) and master (y-direction) ranges to include.
*/
	public Utilities.Rectangle BOUNDS;

/**
* Parameters hailing from ORFID.
*/
	public ORFID.ParameterBlock PBLOCK;
	
/**
* True if current Gene is env.
*/
  public boolean isEnv;

/**
* True if current Gene is gag.
*/
  public boolean isGag;

/**
* True if current Gene is pol.
*/
  public boolean isPol;

/**
* True if current Gene is pro.
*/
  public boolean isPro;

	byte[ ][ ] links; // low 4 bits is skip in DNA, upper skip in alignment
// scores of partial paths.
	float[ ][ ] scoresums;
	
	Compactor ACIDCOMPACTOR = Compactor.ACIDCOMPACTOR;
	

// The actual score matrix.
	float[ ][ ] SCORES;

// Bitwise description of alignment rows corresponding to score.
	int[ ][ ] ROWCODES;

// score for non-aligned interpretation at each position
  float[ ] nonAlignedScores;
	
} // end of DynamicMatrix
