/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

/**
* Class to find sum of values inside part of an array.
*/
public class SumFinder {

  private final int PARTER; // Length of array fragments
  private float[ ] all; // the array
  private float[ ] partsum; // array of sums within fragments
  
  private SumFinder superfinder = null; // the SumFinder applied to partsum
  
/**
* Constructor.
* @param	indata	The array to search in.
* @param	level		The level of this in a hierarchy of SumFinder, 0 being top level.
* @param	parter	The length of the fragments into which indata is partitioned.
*/
  public SumFinder(float[ ] indata, int level, int parter) {
  
    all = indata;
    PARTER = parter;
    int parts = (all.length - 1) / parter + 1;
    partsum = new float[parts];
    
    float sum;
    for (int p=0; p<parts; p++) {
      sum = 0;
      for (int q=p*PARTER; q<Math.min((p+1)*PARTER, all.length); q++) {
        sum += all[q];
      }
      partsum[p] = sum;
    }
    if (level > 0) {
      superfinder = new SumFinder(partsum, level - 1, PARTER);
    }
  } // end of constructor(float[ ], int, int)
  
/**
* Finds sum in a range.
* @param	first	First index to sum over.
* @param	last	Last index to sum over.
* @return	Sum from first to last inclusive, or Float.NaN if something invalid.
*/
  public final float sum(int first, int last) {
    first = Math.max(0, first);
    last = Math.min(last, all.length - 1);
    if ((first >= all.length) || (last < 0) || (first > last)) {
      return Float.NaN;
    }
    
    int fi = first / PARTER + 1;
    int la = last / PARTER - 1;
    float sum = 0;
    if (la <= fi) { // first and last in same fragment
      for (int j=first; j<=last; j++) {
        sum += all[j];
      }
      return sum;
    }
    
    for (int i1=first; i1<fi*PARTER; i1++) {
      sum += all[i1];
    }
    
    if (superfinder == null) {
      for (int i2=fi; i2<=la; i2++) {
        sum += partsum[i2];
      }
    } else {
      sum += superfinder.sum(fi, la);
    }
    
    for (int i3=(la+1)*PARTER; i3<=last ; i3++) {
      sum += all[i3];
    }
    
    return sum;
  } // end of maxvalue(int, int)
  
} // end of SumFinder

