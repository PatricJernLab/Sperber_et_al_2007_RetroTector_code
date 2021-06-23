/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 19/9 -06
* Beautified 19/9 -06
*/

package retrotector;

import java.util.*;
import java.io.*;
import backpropnn.*;

/**
* Neural net to use on DNA base sequence.
*/
public class BaseNet extends ParameterUser {

/**
* The value (typically 0.75) denoting presence of a base.
*/
  public final float BASEMARKER;
  
/**
* The value (typically -0.25) denoting absence of a base.
*/
  public final float NONBASEMARKER;

/**
* The backpropagation NN doing the work.
*/
  public final Net THENET;

/**
* The length of DNA this is meant to score.
*/
  public final int NROFBASES;

/**
* The minimum score to be regarded as a hit. Not used by BaseNNMotif!
*/
  public final float NNTHRESHOLD;

/**
* Constructor.
* @param	file	The file in Database defining the net.
*/
  public BaseNet(File file) throws RetroTectorException {
    parameters = new Hashtable();
		ParameterFileReader nnfile = new ParameterFileReader(file, parameters);
		nnfile.readParameters();
		nnfile.close();
		NONBASEMARKER = getFloat("NonBaseMarker", -0.25f);
		BASEMARKER = getFloat("BaseMarker", 0.75f);
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
		NROFBASES = layers[0] / 4;
		float gain = getFloat("Gain", Float.NaN);
		THENET = (Net) Net.buildNet(layers, weights, 0, gain);
    String verifyString = getString("VerifySequence", null);
    if (verifyString != null) {
      float verifyScore = getFloat("VerifyScore", Float.NaN);
      float[ ] netSource = new float[verifyString.length() * 4];
      for (int ns=0; ns<netSource.length; ns++) {
        netSource[ns] = NONBASEMARKER;
      }
      for (int p=0; p<verifyString.length(); p++) {
        int b = Compactor.BASECOMPACTOR.charToIntId(verifyString.charAt(p));
        if (b <= 3) {
          netSource[p * 4 + b] = BASEMARKER;
        }
      }
      float newScore = THENET.applyNet(netSource)[0];
      if (newScore != verifyScore) {
        RetroTectorException.sendError(this, "Net score mismatch: " + verifyScore + "  " + newScore);
      }
    }
        
  } // end of constructor(File)


	private float[ ] netSource;
	private int lastpos;
	private DNA dna;
	
/**
* Applies the net.
* @param	position	The (internal) position in the DNA to score at.
* @param	theDNA		The DNA to score in.
* @return	The net score, or Float.NaN if not inside the DNA.
*/
  public final float scoreNetAt(int position, DNA theDNA) {
		dna = theDNA;
		netSource = new float[NROFBASES * 4];
		lastpos = position;
		for (int ns=0; ns<netSource.length; ns++) {
			netSource[ns] = NONBASEMARKER;
		}
    if ((position < 0) | ((position + NROFBASES) >= theDNA.LENGTH)) {
      return Float.NaN;
    }
		for (int p=0; p<NROFBASES; p++) {
			int b = theDNA.get2bit(position + p);
			if (b <= 3) {
				netSource[p * 4 + b] = BASEMARKER;
			}
		}
    return THENET.applyNet(netSource)[0];
  } // end of scoreNetAt(int, DNA)

/**
* Applies the net at next position.
* @return	The net score, or Float.NaN if not inside the DNA.
*/
  public final float scoreNetAtNext() {
		lastpos++;
    try {
			if ((lastpos + NROFBASES) >= dna.LENGTH) {
				return Float.NaN;
			}
    } catch (NullPointerException e) {
			throw e;
		}
		System.arraycopy(netSource, 4, netSource, 0, netSource.length - 4);
		for (int ns=netSource.length-4; ns<netSource.length; ns++) {
			netSource[ns] = NONBASEMARKER;
		}
		int b = dna.get2bit(lastpos + NROFBASES - 1);
		if (b <= 3) {
			netSource[netSource.length-4 + b] = BASEMARKER;
		}
    return THENET.applyNet(netSource)[0];
  } // end of scoreNetAtNext()

/**
* Applies the net over a range of positions.
* @param	position1	The first (internal) position in the DNA to score at.
* @param	position2	The last (internal) position in the DNA to score at.
* @param	theDNA		The DNA to score in.
* @param	flar			Array to put scores in at indices position1 to position2.
*/
  public final void scoreNetOver(int position1, int position2, DNA theDNA, float[ ] flar) {
    flar[position1] = scoreNetAt(position1, theDNA);
		for (int p=position1+1; p<=position2; p++) {
      flar[p] = scoreNetAtNext();
    }
  } // end of scoreNetOver(int, int, DNA, float[ ])
  
}
