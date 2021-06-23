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
* Class describing a range of acceptable distances, in bases.
*/
public class DistanceRange {

/**
*	@param	dr1	A DistanceRange.
*	@param	dr1	Another DistanceRange.
*	@return	A DistanceRange, a vector sum of dr1 and dr2.
*/
	public final static DistanceRange rangeSum(DistanceRange dr1, DistanceRange dr2) {
		return new DistanceRange(dr1.LOWESTDISTANCE + dr2.LOWESTDISTANCE, dr1.HIGHESTDISTANCE + dr2.HIGHESTDISTANCE);
	} // end of rangeSum(DistanceRange, DistanceRange)
	
/**
*	@param	dr1	A DistanceRange.
*	@param	dr1	Another DistanceRange.
*	@return	A DistanceRange covering dr1 and dr2.
*/
	public final static DistanceRange rangeUnion(DistanceRange dr1, DistanceRange dr2) {
		return new DistanceRange(Math.min(dr1.LOWESTDISTANCE, dr2.LOWESTDISTANCE), Math.max(dr1.HIGHESTDISTANCE, dr2.HIGHESTDISTANCE));
	} // end of rangeUnion(DistanceRange, DistanceRange)
	
/**
* Lowest acceptable distance in bases.
*/
 	public final int LOWESTDISTANCE;

/**
* Highest acceptable distance in bases.
*/
	public final int HIGHESTDISTANCE;
	
/**
* True if distances were specified in acids. Used only by CoincidenceMotif.
*/
  public final boolean ACIDTYPE;
  
/**
* Constructor
* @param	inString	String of the form |low limit|<|high limit| or |low limit|-|high limit|
* or |limit|. In the first case, bases are meant, in the others acids.
*/
	public DistanceRange(String inString) throws RetroTectorException {
		String errString = "Erroneous distance String: " + inString;
		int lowestd = Integer.MIN_VALUE;
		int highestd = Integer.MIN_VALUE;
		inString = inString.trim();
		int index = inString.indexOf('<');
		if (index < 1) {
      ACIDTYPE = true;
      index = inString.indexOf('-', 1);
      if (index < 0) {
        try {
          lowestd = 3 * Utilities.decodeInt(inString);
          highestd = lowestd;
        } catch (Exception e) {
          RetroTectorException.sendError(this, errString);
        }
      } else {
        try {
          lowestd = 3 * Utilities.decodeInt(inString.substring(0, index));
          highestd = 3 * Utilities.decodeInt(inString.substring(index + 1));
        } catch (Exception e) {
          RetroTectorException.sendError(this, errString);
        }
      }
    } else {
      ACIDTYPE = false;
      try {
        lowestd = Utilities.decodeInt(inString.substring(0, index));
        highestd = Utilities.decodeInt(inString.substring(index + 1));
      } catch (Exception e) {
        RetroTectorException.sendError(this, errString);
      }
    }
		if (lowestd > highestd) {
			RetroTectorException.sendError(this, errString);
		}
		LOWESTDISTANCE = lowestd;
		HIGHESTDISTANCE = highestd;
	} // end of constructor(String)

/**
* Direct constructor. ACIDTYPE will be false.
* @param	lowestd	-> LOWESTDISTANCE
* @param	highestd	-> HIGHESTDISTANCE
*/
  public DistanceRange(int lowestd, int highestd) {
    ACIDTYPE = false;
    LOWESTDISTANCE = lowestd;
    HIGHESTDISTANCE = highestd;
  } // end of constructor(int, int)

/**
* @param	o	An offset.
* @return	A DistanceRange	offset from this one by o.
*/
  public final DistanceRange offset(int o) {
    return new DistanceRange(LOWESTDISTANCE + o, HIGHESTDISTANCE + o);
  } // end of offset(int)
	
/**
* @return	A DistanceRange, the negative of this. ACIDTYPE will be false.
*/
  public final DistanceRange inverse() {
    return new DistanceRange(-HIGHESTDISTANCE, -LOWESTDISTANCE);
  } // end of inverse()
  
/**
* @param	dist	An integer (normally representing a distance).
* @return	True if dist within this range.
*/		
	public final boolean containsDistance(int dist) {
		if ((dist < LOWESTDISTANCE) | (dist > HIGHESTDISTANCE)) {
			return false;
		}
		return true;
	} // end of containsDistance(int)
	
/**
* @param	dr	A DistanceRange.
* @return	True if LOWESTDISTANCE and HIGHESTDISTANCE are identical in dr and this.
*/
	public final boolean doesEqual(DistanceRange dr) {
		if (dr == null) {
			return false;
		}
		if (dr.LOWESTDISTANCE != LOWESTDISTANCE) {
			return false;
		}
		if (dr.HIGHESTDISTANCE != HIGHESTDISTANCE) {
			return false;
		}
		return true;
	} // end of doesEqual(DistanceRange)

/**
* @return	String of the form |low limit|<|high limit|
*/
	public String toString() {
		return "" + LOWESTDISTANCE + "<" + HIGHESTDISTANCE;
	} // end of toString()
	
} // end of DistanceRange
