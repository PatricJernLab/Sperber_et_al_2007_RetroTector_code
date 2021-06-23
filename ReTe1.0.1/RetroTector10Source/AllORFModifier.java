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

import java.io.*;
import java.util.*;
import builtins.*;

/**
* Modifier sublass representing the score assigned in the ORFID algorithm when doing
* nonaligned scoring, assuming default ORFID parameters (as influenced by Configuration.txt).
*/
public class AllORFModifier extends Modifier {

	
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

/**
* Constructor.
* @param	theDNA	DNA this relates to.
*/
 	public AllORFModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = true;
    parameters = new Hashtable();

		String[ ] lines = (String[ ]) RetroTectorEngine.configurationTable.get("ORFIDdefaults");
		if (lines != null) {
      String line;
      String key;
      int index;
      for (int l=0; l<lines.length; l++) {
        line = lines[l];
        if (line.trim().length() > 0) {
          index = line.indexOf(ParameterFileReader.SINGLEPARAMTERMINATOR);
          if (index > 0) {
            key = line.substring(0, index).trim();
            parameters.put(key, line.substring(index + 1).trim());
          }
        }
      }
		}

    float nonAlignedScore = Utilities.decodeFloat(getString(Executor.NONALIGNEDSCOREKEY, ORFID.NONALIGNEDDEFAULT));
    float stopCodonFactor = Utilities.decodeFloat(getString(Executor.STOPCODONFACTORKEY, ORFID.STOPCODONDEFAULT));
    float orfHexamerFactor = Utilities.decodeFloat(getString(Executor.ORFHEXAMERFACTORKEY, ORFID.ORFHEXAMERDEFAULT));
    float nonOrfHexamerFactor = Utilities.decodeFloat(getString(Executor.NONORFHEXAMERFACTORKEY, ORFID.NONORFHEXAMERDEFAULT));
    float glycSiteFactor = Utilities.decodeFloat(getString(Executor.GLYCSITEFACTORKEY, ORFID.GLYCSITEDEFAULT));
    
 		modifiers = new float[theDNA.LENGTH];
    StopCodonModifier stopmod = theDNA.getStopCodonModifier();
    ORFHexamerModifier orfmod = theDNA.getORFHexamerModifier();
    NonORFHexamerModifier norfmod = theDNA.getNonORFHexamerModifier();
    GlycSiteModifier glycmod = theDNA.getGlycSiteModifier();
    
    for (int i=0; i<modifiers.length; i++) {
      modifiers[i] = nonAlignedScore + stopCodonFactor * stopmod.modification(i) + orfHexamerFactor * orfmod.modification(i) + nonOrfHexamerFactor * orfmod.modification(i) + glycSiteFactor * glycmod.modification(i);
    }
    
    float lolim = Float.MAX_VALUE;
 		float hilim = -Float.MAX_VALUE;
 		float sum = 0.0f;
 		int n = 0;
 		for (int po=0; po<modifiers.length; po++) {
 			lolim = Math.min(lolim, modifiers[po]);
 			hilim = Math.max(hilim, modifiers[po]);
 			sum += modifiers[po];
 			n++;
 		}
 		AVERAGE = sum / n;
 		LOWERLIMIT = lolim;
 		UPPERLIMIT = hilim;
 	} // end of constructor(DNA)
 	
/**
* @param	position	An (internal) position in DNA.
* @return	The modifier value at position.
*/
	public float modification(int position) {
		return modifiers[position];
	} // end of modification(int)

/**
* @return	Lowest modification value.
*/
	public float getLowerLimit() {
		return LOWERLIMIT;
	} // end of getLowerLimit()

/**
* @return	Highest modification value.
*/
	public float getUpperLimit() {
		return UPPERLIMIT;
	} // end of getUpperLimit()

/**
* @return	Average modification value over whole DNA.
*/
	public float getAverage() {
		return AVERAGE;
	} // end of getAverage()

/**
* @return	The DNA to which this relates.
*/
	public DNA getDNA() {
		return PARENTDNA;
	} // end of getDNA()

/**
* @return	A complete array of modification values.
*/
  public float[ ] getArray() {
    return (float[ ]) modifiers.clone();
  } // end of getArray()

}