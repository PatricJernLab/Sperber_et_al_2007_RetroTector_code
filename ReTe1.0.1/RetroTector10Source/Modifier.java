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

/**
* Superclass of classes associating each position in a DNA with a
* float value representing something.
*/
public abstract class Modifier extends ParameterUser {

/**
* True if values are related to reading frame.
*/
	public boolean FRAMED;

/**
* @param	position	An (internal) position in DNA.
* @return	The modifier value at position, which might be Float.NaN.
*/
	abstract public float modification(int position);

/**
* @return	Lowest modification value.
*/
	abstract public float getLowerLimit();

/**
* @return	Highest modification value.
*/
	abstract public float getUpperLimit();

/**
* @return	Average modification value over whole DNA, excluding Float.NaN.
*/
	abstract public float getAverage();

/**
* @return	The DNA to which this relates.
*/
	abstract public DNA getDNA();
  
/**
* @return	A complete array of modification values.
*/
  abstract public float[ ] getArray();

/**
* @param	first	First (internal) position to average over.
* @param	last	Last (internal) position to average over.
* @return	Average modification value from first to last, excluding Float.NaN.
*/
  public float getAverage(int first, int last) {
    float sum = 0;
    int n = 0;
    for (int i=first; i<=last; i++) {
      if (!Float.isNaN(modification(i))) {
        sum += modification(i);
        n++;
      }
    }
    if (n == 0) {
      return Float.NaN;
    }
    return sum / n;
  } // end of getAverage(int, int)
  
} // end of Modifier
