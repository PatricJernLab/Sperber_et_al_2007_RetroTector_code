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
* Class to find maximum value inside part of an array.
*/
public class TopFinder {

/**
* The index of the maximum most recently found by maxvalue.
*/
  public int maxpos;

  private final int PARTER; // Length of array fragments
  private float[ ] all; // the array
  private float[ ] partmax; // array of values of maxima within fragments
  private int[ ] partmaxpos; // array of indices of maxima within fragments
  
  private TopFinder superfinder = null; // the TopFinder applied to partmax
  
/**
* Constructor.
* @param	indata	The array to search in.
* @param	level		The level of this in a hierarchy of TopFinders, 0 being top level.
* @param	parter	The length of the fragments into which indata is partitioned.
*/
  public TopFinder(float[ ] indata, int level, int parter) {
  
    all = indata;
    PARTER = parter;
    int parts = (all.length - 1) / parter + 1;
    partmax = new float[parts];
    partmaxpos = new int[parts];
    
    float max;
    int pos;
    for (int p=0; p<parts; p++) {
      max = - Float.MAX_VALUE;
      pos = -1;
      for (int q=p*PARTER; q<Math.min((p+1)*PARTER, all.length); q++) {
        if (all[q] > max) {
          max = all[q];
          pos = q;
        }
      }
      partmax[p] = max;
      partmaxpos[p] = pos;
    }
    if (level > 0) {
      superfinder = new TopFinder(partmax, level - 1, PARTER);
    }
  } // end of constructor(float[ ], int, int)
  
/**
* Finds maximum in a range, setting maxpos to index of it, or -1.
* @param	first	First index to search in.
* @param	last	Last index to search in.
* @return	Highest value between first and last inclusive, or Float.NaN if something invalid.
*/
  public final float maxvalue(int first, int last) {
    first = Math.max(0, first);
    last = Math.min(last, all.length - 1);
    if ((first >= all.length) || (last < 0) || (first > last)) {
      maxpos = -1;
      return Float.NaN;
    }
    
    int fi = first / PARTER + 1;
    int la = last / PARTER - 1;
    float max = -Float.MAX_VALUE;
    int pos = -1;
    if (la <= fi) { // first and last in same fragment
      for (int j=first; j<=last; j++) {
        if (all[j] > max) {
          max = all[j];
          pos = j;
        }
      }
      maxpos = pos;
      return max;
    }
    
    for (int i1=first; i1<fi*PARTER; i1++) {
      if (all[i1] > max) {
        max = all[i1];
        pos = i1;
      }
    }
    
    if (superfinder == null) {
      for (int i2=fi; i2<=la; i2++) {
        if (partmax[i2] > max) {
          max = partmax[i2];
          pos = partmaxpos[i2];
        }
      }
    } else {
      float ma = superfinder.maxvalue(fi, la);
      if (ma > max) {
        max = partmax[superfinder.maxpos];
        pos = partmaxpos[superfinder.maxpos];
      }
    }
    
    for (int i3=(la+1)*PARTER; i3<=last ; i3++) {
      if (all[i3] > max) {
        max = all[i3];
        pos = i3;
      }
    }
    
    maxpos = pos;
    return max;
  } // end of maxvalue(int, int)

/**
* Collects maxima within segments.
*	@param	into	Array, typically of same size as indata. Each element receives maximum within size positions forward.
*	@param	size	Segment length.
*/
  public final void sweep(float[ ] into, int size) {
    float tempmax = maxvalue(0, size - 1);
    into[0] = tempmax;
    for (int p=1; p<all.length; p++) {
      if (all[p - 1] >= tempmax) {
        tempmax = maxvalue(p, p + size - 1);
      } else if (((p + size - 1) < all.length) && (all[p + size - 1] > tempmax)) {
        tempmax = all[p + size - 1];
      }
      into[p] = tempmax;
    }
  } // end of sweep(float[ ], int)
  
} // end of TopFinder

