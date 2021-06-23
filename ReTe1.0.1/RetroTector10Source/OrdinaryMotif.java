/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 16/9 -06
* Beautified 16/9 -06
*/
package retrotector;

import java.util.*;

/**
* Subclass of Motif, for motifs that generate a distinct raw score in any position.<BR>
* and only one hit per position.<BR>
*<BR>
* Subclasses must define the methods<BR>
*		public static String classType()<BR>
*		public Motif motifCopy() throws RetroTectorException<BR>
*		public float getRawScoreAt(int pos) throws RetroTectorException<BR>
*		protected float refreshScoreAt(int pos) throws RetroTectorException<BR>
*		public float getBestRawScore()<BR>
*		protected int getBasesLength()<BR>
*<BR>
* Subclasses should carefully consider overriding the methods<BR>
*		public void refresh(RefreshInfo theInfo) throws RetroTectorException<BR>
*		public void localRefresh(int firstpos, int lastpos) throws RetroTectorException<BR>
*		protected MotifHit makeMotifHitAt(int position) throws RetroTectorException<BR>
*		public MotifHit getMotifHitAt(int position) throws RetroTectorException<BR>
*		public String correspondingDNA(MotifHit theHit) throws RetroTectorException<BR>
*		public RVector getRVector()<BR>
*		protected float calculateRawThreshold(float sdfac, float cfact)<BR>
*
*	and defining the fields frameDefined, exactlyAligned and showAsBases
*/
public abstract class OrdinaryMotif extends Motif {

/**
* Number of hits to collect for statistics = 1000.
*/
	public static final int SAMPLESIZE = 1000;

/**
* Raw score must exceed this to make a MotifHit.
*/
  protected float rawThreshold = Float.NaN;
  
/**
* Preset value for rawThreshold.
*/
  protected float fixedThreshold = Float.NaN;
  
/**
* Normally MAXSCORE / (max possible raw score minus rawThreshold).
*/
  protected float rawFactor = Float.NaN;
  
/**
* Constructs an instance, mainly from data in Motifs.txt
* @param	data			String from Motifs.txt
* @param	database	Database of Motifs.txt
*/
  public OrdinaryMotif(String data, Database datab) throws RetroTectorException {
    super(data, datab);
    if (PARAM.length() > 0) {
      fixedThreshold = Utilities.decodeFloat(PARAM);
    }
  } // end of constructor(String, Database)
  
/**
* Constructs an instance, independently of Motifs.txt
*/
  public OrdinaryMotif() throws RetroTectorException {
    super();
	} // end of constructor()
	
/**
* Constructor
* @param	database	Database to use
* @param	motifID				->MOTIFID
* @param	motifType			->MOTIFTYPE
* @param	motifLevel		->MOTIFLEVEL
* @param	rvg						->MOTIFRVECTOR
* @param	motifSDFactor	->MOTIFSDFACTOR
* @param	motifSubGene	->MOTIFSUBGENE
* @param	motifGroup		->MOTIFGROUP
* @param	maxScore			->MAXSCORE
* @param	param					->PARAM
* @param	motifString		->MOTIFSTRING
* @param	motifOrigin		->MOTIFORIGIN
*/
  public OrdinaryMotif(Database database, int motifID, String motifType, String motifLevel, String rvg, float motifSDFactor, SubGene motifSubGene, MotifGroup motifGroup, float maxScore, String param, String motifString, String motifOrigin) throws RetroTectorException {
		super(database, motifID, motifType, motifLevel, rvg, motifSDFactor, motifSubGene, motifGroup, maxScore, param, motifString, motifOrigin);
	} // end of constructor(Database, int, String, String, String, float, SubGene, MotifGroup, float, String, String, String)
	
	
/**
* @param	pos	Position (internal in DNA) to match at.
* @return The raw score at pos, or Float.NaN if the position is unacceptable.
*/
  abstract public float getRawScoreAt(int pos) throws RetroTectorException;
  
/**
* @param	pos	Position (internal in DNA) to match at
* @return The raw score at pos, or NaN if the position is unacceptable or an ambiguous unit is encountered.
*/
  abstract protected float refreshScoreAt(int pos) throws RetroTectorException;
  
/**
* @return	An estimate of the highest raw score obtainable.
*/
  abstract public float getBestRawScore();
  
/**
* @return	The length of a hit by this Motif, expressed in DNA bases, or 0 if not predefinable.
*/
  abstract protected int getBasesLength();
  
/**
* Utility routine. Calculates threshold score as average + sdfac * SD.
* @param	sdfac		Factor for threshold calculation.
* @param	cfact		Bonus factor for conserved positions.
*	@return	Threshold as above, or Float.MAX_VALUE if all sampled raw scores were equal.
*/
	protected final float calculateRawThreshold(float sdfac, float cfact) throws RetroTectorException {
    AcidMatrix.refreshAcidMatrix(cfact);
    BaseMatrix.refreshBaseMatrix(cfact);
   	double sum = 0.0;
  	double sumsq = 0.0;
  	int n = 0;
  	float temp;
  	int r;
  	double aver;
  	double sd;
  	float step = (currentDNA.LASTVALID - currentDNA.FIRSTVALID - 1) / (1.0f * SAMPLESIZE);
  	float s = currentDNA.FIRSTVALID;
  	for (int i=0; i<SAMPLESIZE; i++) {
      r = (int) Math.round(s);
      temp = refreshScoreAt(r);
	  	if (!Float.isNaN(temp)) { // not NaN
	  		sum += temp;
	  		sumsq += temp * temp;
	  		n++;
	  	}
  		s += step;
  	}
  	aver = sum / n;
  	sd = Math.sqrt((sumsq - n * aver * aver) / (n - 1));
    if (sd == 0) {
      return Float.MAX_VALUE;
    } else {
      return (float) (aver + sd * sdfac);
    }
  } // end of calculateRawThreshold(float, float)

/**
* Reset Motif with new parameters.
* currentDNA, rawThreshold and rawFactor are set.
* @param	theInfo	RefreshInfo with required information.
*/
	public void refresh(RefreshInfo theInfo) throws RetroTectorException {
    
    currentDNA = theInfo.TARGETDNA;
    if (!Float.isNaN(fixedThreshold)) {  // not Float.NaN
      rawThreshold = fixedThreshold;
    } else {
      rawThreshold = Math.min(calculateRawThreshold(theInfo.SDFACTOR, theInfo.CFACTOR), 0.99f * getBestRawScore());
    }
    rawFactor = MAXSCORE / (getBestRawScore() - rawThreshold);
  } // end of refresh(RefreshInfo)

/**
* Dummy.
* @param	firstpos	The first (internal) positon in current DNA to prepare.
* @param	lastpos		The last (internal) positon in current DNA to prepare.
*/
  public void localRefresh(int firstpos, int lastpos) throws RetroTectorException {
  } // end of localRefresh(int, int)
  
/**
* @param	position	An (internal) positon in current DNA.
* @return	A MotifHit at position, or null.
*/
  public MotifHit getMotifHitAt(int position) throws RetroTectorException {
    return makeMotifHitAt(position);
  } // end of getMotifHitAt(int)

    
/**
* @param	position	An (internal) positon in current DNA.
* @return	A MotifHit or MotifHit array at position, or null.
*/
  public Object getMotifHitsAt(int position) throws RetroTectorException {
    return getMotifHitAt(position);
  } // end of getMotifHitAt(int)
  
/**
* @return	The minimum raw score for a hit.
*/
  public float getRawThreshold() {
    return rawThreshold;
  } // end of getRawThreshold()
  
/**
* @param	position	An (internal) position in DNA.
* @return	A hit by this Motif if there is one at position, otherwise null.
*/
  protected MotifHit makeMotifHitAt(int position) throws RetroTectorException {
    float sc = (getRawScoreAt(position) - rawThreshold) * rawFactor;
    if (sc > 0) {
			return new MotifHit(this, sc, position, position, position + getBasesLength() - 1, currentDNA);
    } else {
      return null;
    }
  } // end of makeMotifHitAt()
  
/**
*	Utility routine, helping motifCopy of subclasses.
*	currentDNA, frameDefined, rawThreshold and rawFactor are copied into mc.
*	@param	mc	An OrdinaryMotif to make a copy of this.
*/
  protected final void motifCopyHelp(OrdinaryMotif mc) throws RetroTectorException {
    super.motifCopyHelp(mc);
    mc.rawThreshold = rawThreshold;
    mc.rawFactor = rawFactor;
  } // end of motifCopyHelp(OrdinaryMotif)
  
}
