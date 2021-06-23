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

/**
* Modifier sublass representing proximity to GT pairs.
*/
public class GTModifier extends Modifier {

/**
* Profile of influence of hit at distances. Initialized to Gaussian with sd = 50;
*/
	static float[ ] weightProfile;

// SD of weightProfile
	private static int sd = 50;

	static {
		int f = (int) (sd * Math.sqrt(2 * Math.log(100)));
		weightProfile = new float[f + 1];
		for (int ff=0; ff<=f; ff++) {
			weightProfile[ff] = (float) Math.exp(-ff * ff * 0.5f / (sd * sd));
		}
	} // end of static initializer

	
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

/**
* Constructor.
* @param	theDNA	DNA this relates to.
*/
 	public GTModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = false;

 		modifiers = new float[theDNA.LENGTH];
 
   	int gcode = Compactor.BASECOMPACTOR.charToIntId('g');
  	int tcode = Compactor.BASECOMPACTOR.charToIntId('t');
    int p;
    for (int pos = 1; pos < theDNA.LENGTH; pos++) {
      if ((theDNA.get2bit(pos - 1) == gcode) && (theDNA.get2bit(pos) == tcode)) {
        modifiers[pos] += weightProfile[0];
        for (int diff=1; diff<weightProfile.length; diff++) {
          p = pos - diff;
          if (p >= 0) {
            modifiers[p] += weightProfile[diff];
          }
          p = pos + diff;
          if (p<modifiers.length) {
            modifiers[p] += weightProfile[diff];
          }
        }
      }
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
	public final float modification(int position) {
    return modifiers[position];
	} // end of modification(int)

/**
* @return	Lowest modification value.
*/
	public final float getLowerLimit() {
		return LOWERLIMIT;
	} // end of getLowerLimit()

/**
* @return	Highest modification value.
*/
	public final float getUpperLimit() {
		return UPPERLIMIT;
	} // end of getUpperLimit()

/**
* @return	Average modification value over whole DNA.
*/
	public final float getAverage() {
		return AVERAGE;
	} // end of getAverage()

/**
* @return	The DNA to which this relates.
*/
	public final DNA getDNA() {
		return PARENTDNA;
	} // end of getDNA()
	
/**
* @return	A complete array of modification values.
*/
  public final float[ ] getArray() {
    return (float[ ]) modifiers.clone();
  } // end of getArray()
  
} // end of GTModifier
