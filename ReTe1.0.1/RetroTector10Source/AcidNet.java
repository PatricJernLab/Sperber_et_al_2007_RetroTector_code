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

import java.util.*;
import java.io.*;
import backpropnn.*;

/**
* Neural net to use on codon sequences.
*/
public class AcidNet extends ParameterUser {

/**
* The backpropagation NN doing the work.
*/
  public final Net THENET;
  
/**
* String of characters symbolizing amino acids.
*/
  public final String ACIDCODES;
  
/**
* Length of ACIDCODES.
*/
  public final int CODESETSIZE;

/**
* 64 by CODESETSIZE array of similarity between codons and acids.
*/
  public final float[ ][ ] MATRIX;
  
/**
* Number of codons this is meant to score.
*/
  public final int NROFCODONS;

/**
* The length of DNA this is meant to score = NROFCODONS * 3.
*/
  public final int NROFBASES;

/**
* The minimum score to be regarded as a hit. Not in use at present.
*/
  public final float NNTHRESHOLD;

/**
* Constructor.
* @param	file	The file, presumably in Database, defining the net.
*/
  public AcidNet(File file) throws RetroTectorException {
    parameters = new Hashtable();
		ParameterFileReader nnfile = new ParameterFileReader(file, parameters);
		nnfile.readParameters();
		nnfile.close();
    ACIDCODES = getString("AcidCodes", null);
    CODESETSIZE = ACIDCODES.length();
    float[ ][ ] matrix = new float[CODESETSIZE][CODESETSIZE];
    String[ ] ss = getStringArray("Matrix");
    for (int i=0; i<CODESETSIZE; i++) {
      String[ ] sss = Utilities.splitString(ss[i]);
      for (int j=0; j<CODESETSIZE; j++) {
        matrix[i][j] = Utilities.decodeFloat(sss[j]);
      }
    }
    Compactor compactor = Compactor.ACIDCOMPACTOR;
    MATRIX = new float[64][CODESETSIZE];
    for (int c=0; c<64; c++) {
      MATRIX[c] = matrix[ACIDCODES.indexOf(compactor.intToCharId(c))];
    }
		float overridingThreshold = getFloat("Threshold", Float.NaN);
    if (!Float.isNaN(overridingThreshold)) {
      NNTHRESHOLD = overridingThreshold;
    } else {
      NNTHRESHOLD = getFloat("RandomMean", 0.0f) + 5 * getFloat("RandomSD", 0.0f);
    }
		String[ ] ws = getStringArray("Weights");
		float[ ] weights = new float[ws.length];
		for (int w=0; w<ws.length; w++) {
			weights[w] = Utilities.decodeFloat(ws[w]);
		}
		String[ ] ls = getStringArray("Layers");
		int[ ] layers = new int[ls.length];
		for (int l=0; l<ls.length; l++) {
			layers[l] = Utilities.decodeInt(ls[l]);
		}
		NROFCODONS = layers[0] / CODESETSIZE;
    NROFBASES = NROFCODONS * 3;
		float gain = getFloat("Gain", Float.NaN);
		THENET = (Net) Net.buildNet(layers, weights, 0, gain);
    String verifyString = getString("VerifySequence", null);
    if (verifyString != null) {
      float verifyScore = getFloat("VerifyScore", Float.NaN);
      float[ ] netSource = new float[verifyString.length() * CODESETSIZE];
      for (int p=0; p<verifyString.length(); p++) {
        System.arraycopy(MATRIX[compactor.charToIntId(verifyString.charAt(p))], 0, netSource, p * CODESETSIZE, CODESETSIZE);
      }
      float newScore = THENET.applyNet(netSource)[0];
      if (newScore != verifyScore) {
        RetroTectorException.sendError(this, "Net score mismatch: " + verifyScore + "  " + newScore);
      }
    }
        
  } // end of constructor
	
	private float[ ] netSource;
	private int lastpos;
	
/**
* Applies the net.
* @param	position	The (internal) position in the DNA to start at.
* @param	theDNA		The DNA to score in.
* @return	The net score, or Float.NaN if not inside the DNA.
*/
  public final float scoreNetAt(int position, DNA theDNA) {
    if ((position < 0) | ((position + NROFBASES) >= theDNA.LENGTH)) {
      return Float.NaN;
    }
		netSource = new float[NROFCODONS * CODESETSIZE];
    int g;
		for (int p=0; p<NROFCODONS; p++) {
      g = theDNA.get6bit(position + 3 * p);
      if (g < 64) {
        System.arraycopy(MATRIX[g], 0, netSource, p * CODESETSIZE, CODESETSIZE);
      }
		}
		lastpos = position;
    return THENET.applyNet(netSource)[0];
  } // end of scoreNetAt(int, DNA)
  
}
