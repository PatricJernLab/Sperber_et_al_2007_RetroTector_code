/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 5/10 -06
* Beautified 5/10 -06
*/
package builtins;

import retrotector.*;
import java.util.*;

/**
* Motif subclass for 15 acid signal sequence, found by von Heijne matrix.
* Raw score threshold is default 5 but may be changed through SiSeqMotifdefaults,
* with parameter RawScoreThreshold.
* Hotspot is at next to last acid.
*/
public class SiSeqMotif extends OrdinaryMotif {

/**
* Key for parameter defining raw score threshold = "RawScoreThreshold".
* Default value is 5.
*/
	public static final String RAWSCORETHRESHOLDKEY = "RawScoreThreshold";

/**
* Ordered amino acid symbols.
*/
  static final String ACIDCHARS = "acdefghiklmnpqrstvwyz";

/**
* Average frequencies of amino acids.
*/
  static final float[ ] EUKARAVERAGES = {14.5f, 4.5f, 8.9f, 10f, 5.6f, 12.1f,3.4f, 7.4f, 11.3f, 12.1f, 2.7f, 7.1f, 7.4f, 6.3f, 7.6f, 11.4f, 9.7f, 11.1f, 1.8f, 5.6f, 1.0f};

/**
* Frequencies of amino acids in positions in signal sequence.
*/
  static final int[ ][ ] EUKARFREQUENCIES = {
  {16, 13, 14, 15, 20, 18, 18, 17, 25, 15, 47,  6, 80, 18,  6},
  { 3,  6,  9,  7,  9, 14,  6,  8,  5,  6, 19,  3,  9,  8,  3},
  { 0,  0,  0,  0,  0,  0,  0,  0,  5,  3,  0,  5,  0, 10, 11},
  { 0,  0,  0,  1,  0,  0,  0,  0,  3,  7,  0,  7,  0, 13, 14},
  {13,  9, 11, 11,  6,  7, 18, 13,  4,  5,  0, 13,  0,  6,  4},
  { 4,  4,  3,  6,  3, 13,  3,  2, 19, 34,  5,  7, 39, 10,  7},
  { 0,  0,  0,  0,  0,  1,  1,  0,  5,  0,  0,  6,  0,  4,  2},
  {15, 15,  8,  6, 11,  5,  4,  8,  5,  1, 10,  5,  0,  8,  7},
  { 0,  0,  0,  1,  0,  0,  1,  0,  0,  4,  0,  2,  0, 11,  9},
  {71, 68, 72, 79, 78, 45, 64, 49, 10, 23,  8, 20,  1,  8,  4},
  { 0,  3,  7,  4,  1,  6,  2,  2,  0,  0,  0,  1,  0,  1,  2},
  { 0,  1,  0,  1,  1,  0,  0,  0,  3,  3,  0, 10,  0,  4,  7},
  { 2,  0,  2,  0,  0,  4,  1,  8, 20, 14,  0,  1,  3,  0, 22},
  { 0,  0,  0,  1,  0,  6,  1,  0, 10,  8,  0, 18,  3, 19, 10},
  { 2,  0,  0,  0,  0,  1,  0,  0,  7,  4,  0, 15,  0, 12,  9},
  { 9,  3,  8,  6, 13, 10, 15, 16, 26, 11, 23, 17, 20, 15, 10},
  { 2, 10,  5,  4,  5, 13,  7,  7, 12,  6, 17,  8,  6,  3, 10},
  {20, 25, 15, 18, 13, 15, 11, 27,  0, 12, 32,  3,  0,  8, 17},
  { 4,  3,  3,  1,  1,  2,  6,  3,  1,  3,  0,  9,  0,  2,  0},
  { 0,  1,  4,  0,  0,  1,  3,  1,  1,  2,  0,  5,  0,  1,  7},
  { 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}
  };
  
/**
* Scores for amino acids in positions.
*/
  static float[ ][ ] scorematrix = null;
  
/**
* Sum of column score maxima.
*/
  public static float maxscore = 0;
  
/**
* Score for ambiguous acid.
*/
  static float ncode;
	
	static {makeMatrix();}
  
// according to von Heijne's description.
  private static void makeMatrix() {
    scorematrix = new float[64][15]; // 64 codons by 15 acid positions

    for (int row=0; row<64; row++) {
      int ro=ACIDCHARS.indexOf(Compactor.ACIDCOMPACTOR.intToCharId(row));
      for (int x=0; x<15; x++) {
        if (EUKARFREQUENCIES[ro][x] == 0) {
          if ((x == 10) || (x == 12)) {
            scorematrix[row][x] = 1.0f / 161.0f;
          } else {
            scorematrix[row][x] = 1.0f / EUKARAVERAGES[ro];
          }
        } else {
          scorematrix[row][x] = EUKARFREQUENCIES[ro][x] / EUKARAVERAGES[ro];
        }
        scorematrix[row][x] = (float) Math.log(scorematrix[row][x]);
			}
    }
    for (int xx=0; xx<15; xx++) {
      float maxs = -Float.MAX_VALUE;
      for (int yy=0; yy<64; yy++) {
        maxs = Math.max(maxs, scorematrix[yy][xx]);
      }
      maxscore += maxs;
    }
    int nc = Compactor.ACIDCOMPACTOR.charToIntId('z');
// this sets value for ambiguous acid = value for stop codon. Is that right?
    ncode = scorematrix[nc][0];
  } // end of makeMatrix()
  
/**
* @return	String identifying this class = "SSQ".
*/
	public static final String classType() {
		return "SSQ";
	} // end of classType()
	
/**
*	@param	s	A 15-char String of amino acid symbols.
*	@return	The raw score of this Motif against s.
*/
	public final static float scoreAcidString(String s) throws RetroTectorException {
		if (s.length() != 15) {
			throw new RetroTectorException("SiSeqMotif.scoreAcidString", "String length not 15 " + s);
		}
    float sum = 0;
    int temp;
    for (int p=0; p<=14; p++) {
      temp = Compactor.ACIDCOMPACTOR.charToIntId(s.charAt(p));;
      if (temp < 64) {
        sum += scorematrix[temp][p];
      } else {
        sum += ncode;
      }
    }
    return sum;
	} // end of scoreAcidString(String)
		
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
 */
  public SiSeqMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
		if (!MOTIFTYPE.equals(classType())) {
			RetroTectorException.sendError(this, "Type mismatch in Motif " + MOTIFID, MOTIFTYPE, classType());
		}

    frameDefined = true;
    
    if (scorematrix == null) {
      makeMatrix();
    }
    parameters = new Hashtable();
    parameters.put(RAWSCORETHRESHOLDKEY, "5");
    getDefaults();
  } // end of constructor()

/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    SiSeqMotif result = new SiSeqMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()
  
/**
* @param	pos	Position (internal in DNA) to match at.
* @return	The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  public final float getRawScoreAt(int pos) throws RetroTectorException {
    if ((pos < 39) | (pos >= (currentDNA.LENGTH - 8))) {
      return Float.NaN;
    }
    float sum = 0;
    int temp;
    int po;
    for (int p=0; p<=14; p++) {
      po = pos + 3 * (p - 13);
      temp = currentDNA.get6bit(po);
      if (temp < 64) {
        sum += scorematrix[temp][p];
      } else {
        sum += ncode;
      }
    }
    return sum;
  } // end of getRawScoreAt(int)
  
/**
* @param	pos	Position (internal in DNA) to match at
* @return	getRawScoreAt(pos).
*/
  protected final float refreshScoreAt(int pos) throws RetroTectorException {
    return getRawScoreAt(pos);
  } // end of refreshScoreAt()

/**
* @return	The highest raw score obtainable.
*/
  public final float getBestRawScore() {
    return maxscore;
  } // end of getBestRawScore()
  
/**
 * @return	The number of acids * 3 in the Motif = 45.
 */
 	public final int getBasesLength() {
  	return 45;
  } // end of getBasesLength()

/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold, rawFactor and cfactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    fixedThreshold = getFloat(RAWSCORETHRESHOLDKEY, 5.0f);
    super.refresh(theInfo);
  } // end of refresh(RefreshInfo)

/**
* @param	position	An (internal) position in DNA.
* @return	A hit by this Motif if there is one at position, otherwise null.
*/
  protected final MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    float sc = (getRawScoreAt(position) - rawThreshold) * rawFactor;
    if (sc <= 0) {
      return null;
    } else {
      return new MotifHit(this, sc, position, position - 39, position + 5, currentDNA);
    }
  } // end of makeMotifHitAt(int)
  
} // end of SiSeqMotif
