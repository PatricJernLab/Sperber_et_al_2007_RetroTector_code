/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 8/12 -06
* Beautified 8/12 -06
*/

package retrotector;

/**
* Vectors representing the relationship to virus genera.
*/
public class RVector {

	private class ToSort implements Scorable {
	
		final char C;
		final float SCO;
		
		ToSort(char c, float sco) {
			C = c;
			SCO = sco;
		} // end of ToSort.constructor(char, float)
		
		public float fetchScore() {
			return SCO;
		} // end of ToSort.fetchScore()
		
	} // end of ToSort
	

/**
* Letters representing virus genera.
*/
	public static final char[ ] RVCHARS = {'A', 'B', 'C', 'D', 'E', 'L', 'S', 'G', 'O'};
	
/**
* Length of RVCHARS.
*/
	public static final int RVSIZE = RVCHARS.length;
	
/**
* String of RVCHARS.
*/
	public static final String RVSTRING = new String(RVCHARS);
	
/**
* Base vectors of the virus genera.
*/
	private static final float[ ][ ] RVBASE = {
				{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
 				{0.6666667f, 0.74535596f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
 				{0.33333334f, 0.1490712f, 0.93094933f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
 				{0.33333334f, 0.1490712f, 0.5728919f, 0.7337994f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
 				{0.33333334f, 0.1490712f, 0.21483447f, 0.104828484f, 0.8997354f, 0.0f, 0.0f, 0.0f, 0.0f},
 				{0.0f, 0.0f, 0.0f, 0.45425677f, -0.052925613f, 0.8892973f, 0.0f, 0.0f, 0.0f},
 				{0.0f, 0.0f, 0.35805744f, -0.27954262f, -0.052925613f, 0.13964172f, 0.87826526f, 0.0f, 0.0f},
        {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f},
        {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}
 	};
 	
/**
* @param	c	A character representing a virus genus.
* @return 	The index in RVCHARS of c, or -1.
*/
	public static final int rvindex(char c) {
 		return RVSTRING.indexOf(Character.toUpperCase(c));
 	} // end of rvindex(char)
 	
/**
* @param	c	A character representing a virus genus.
* @return	The particular base vector related to that virus genus.
*/
 	public static final float[ ] baseVector(char c) {
 		int i = rvindex(c);
 		if (i < 0) {
 			return null;
 		}
 		return (float[ ]) RVBASE[i].clone();
 	} // end of baseVector(char)
 	
 	
 	private float[ ] vector = null; // contents of an instance
 	
/**
* Constructor, making a zero RVector.
*/
 	public RVector() {
 		vector = new float[RVSIZE];
 		for (int i=0; i<RVSIZE; i++) {
 			vector[i] = 0.0f;
 		}
 	} // end of constructor()

/**
* Constructor for RVector with specified components.
* @param	rv	Float array specifying the components.
*/
 	public RVector(float[ ] rv) {
 		vector = new float[RVSIZE];
 		System.arraycopy(rv, 0, vector, 0, RVSIZE);
 	} // end of constructor(float[ ])
 	
/**
* @return	The components of this vector.
*/
 	public final float[ ] getContent() {
 		return (float[ ]) vector.clone();
 	} // end of getContent()
 		
 	
// utility for RVector(String). sp is binary vector. 
 	private final float[ ] rvSpec(float[ ] sp) {
 		float[ ] newrv = new float[RVSIZE];
 		float spec;
 		for (int ba=0; ba<RVSIZE; ba++) {
 			spec = sp[ba];
 			for (int i=0; i<ba; i++) {
 				spec -= RVBASE[ba][i] * newrv[i];
 			}
 			newrv[ba] = spec / RVBASE[ba][ba];
		}
		return newrv;
	} // end of rvSpec(float[ ])
	
/**
* Constructor for RVector equidistant from some base vectors.
* If ss contains no valid character, a RetroTectorException is thrown.
* @param	ss	String, whose characters identify the vectors
*/
 	public RVector(String ss) throws RetroTectorException {
 		String s = ss.trim();
 		int index;
 		if ((s == null) || (s.length() == 0)) {
 			throw new RetroTectorException("RVector", "Empty argument line");
 		}
 		
 		if (s.length() == 1) {
 			vector = baseVector(s.charAt(0));
 			if (vector == null) {
 				throw new RetroTectorException("RVector", "Useless line", ss);
 			}
 			return;
 		}
 		
 		float[ ] temp = new float[RVSIZE] ;
 		for (int i=0; i<RVSIZE; i++) {
 			temp[i] = 0.0f;
 		}
 		for (int i=0; i<s.length(); i++) {
 			index = rvindex(s.charAt(i));
 			if (index >= 0) {
 				temp[index] = 1.0f;
 			}
 		}
 		vector = rvSpec(temp);
 		if (modulus() == 0) {
 			throw new RetroTectorException("RVector", "Useless line", ss);
 		}
 		normalize();
 	} // end of constructor(String)
 	
/**
* Add another RVector.
* @param	term	The RVector to add to this.
*/
 	public final void plus(RVector term) {
 		for (int i=0; i<RVSIZE; i++) {
 			vector[i] += term.vector[i];
 		}
 	} // end of plus(RVector)
 	
/**
* Add another RVector, multiplied by a factor.
* @param	term	The RVector to add to this.
* @param	factor	The coefficient to multiply term by before adding.
*/
 	public final void plus(RVector term, float factor) {
 		for (int i=0; i<RVSIZE; i++) {
 			vector[i] += term.vector[i] * factor;
 		}
 	} // end of plus(RVector, float)
 	
 	private final float sProduct(float[ ] factor) {
 	
 		float sum = 0.0f;
 		for (int i=0; i<RVSIZE; i++) {
 			sum += vector[i] * factor[i];
 		}
 		return sum;
 	} // end of sProduct(float[ ])
 	
/**
* Scalar product with another RVector.
* @param	factor	The RVector to multiply by.
* @return	The scalar product.
*/
 	public final float sProduct(RVector factor) {
 		return sProduct(factor.vector);
 	} // end of sProduct(RVector)
 	
/**
* @return	Modulus of this vector.
*/
 	public final float modulus() {
 		return (float) Math.sqrt(sProduct(this));
 	} // end of modulus()
 	
/**
* Multiply with a constant.
* @param	coeff	The constant to multiply by.
*/
 	public final void multiplyBy(float coeff) {
 		for (int i=0; i<RVSIZE; i++) {
 			vector[i] *= coeff;
 		}
 	} // end of multiplyBy(float)
			 	
/**
* Reduce modulus to 1.
*/
 	public final void normalize() {
 		multiplyBy( 1 / modulus());
 	} // end of normalize()
 	
/**
* @return	A new RVector identical to this.
*/
 	public final RVector copy() {
 		return new RVector(vector);
 	} // end of copy()
 	
/**
* A String of characters, representing the base vector with
* the highest scalar product with this, and those within 80% of that.
* @return A String as described.
*/
 	public final String rvGenus() throws RetroTectorException {
 		return rvGenus(0.8f);
 	} // end of rvGenus()
 	
/**
* Returns a String of characters, representing the base vector with
* the highest scalar product, and those within a specified fraction of that.
* @param	fract	The fraction as specified.
* @return A String as described.
*/
 	public final String rvGenus(float fract) throws RetroTectorException {
	
		ToSort[ ] tosort = new ToSort[RVSIZE];
 		for (int i=0; i<RVSIZE; i++) {
			tosort[i] = new ToSort(RVCHARS[i], sProduct(RVBASE[i]));
		}
		Utilities.sort(tosort);
		
 		StringBuffer sb = new StringBuffer();
		for (int so=0; (so<tosort.length) && (tosort[so].SCO / tosort[0].SCO >= fract); so++) {
			sb.append(tosort[so].C);
		}
 	
 		return sb.toString();
 	} // end of rvGenus(float)
 	
/**
* @param	rv	An RVector
* @return	Cosine of angle between this and rv.
*/
 	public final float cosine(RVector rv) {
 		float m1 = rv.modulus();
 		float m2 = modulus();
 		float p = sProduct(rv);
 		return p / m1 / m2;
 	} // end of cosine(RVector)
 	
/**
* @param	dim	Index of a base RVector
* @return	Cosine of angle between this and the specified base RVector.
*/
 	public final float directionCosine(int dim) {
 		return cosine(RVBASE[dim]);
 	} // end of directionCosine(int)
 	
 	private final float cosine(float[ ] f) {
 		return cosine(new RVector(f));
 	} // end of cosine(float[ ])
 	
/**
* @param	rv	An RVector.
* @return	true if rv identical to this.
*/
 	public final boolean sameAs(RVector rv) {
    for (int i=0; i<RVSIZE; i++) {
      if (vector[i] != rv.vector[i]) {
        return false;
      }
    }
    return true;
  } // end of sameAs(RVector)
  
/**
* For debugging. Not in use at present.
*/
 	public String toString() {
 		StringBuffer sb = new StringBuffer(10000);
 		for (int i=0; i<RVSIZE; i++) {
 			sb.append(" ");
 			sb.append(Utilities.twoDecimals(vector[i]));
 		}
 		return sb.toString();
 	} // end of toString()

} // end of RVector
