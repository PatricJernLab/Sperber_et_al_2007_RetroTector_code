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
 
import java.util.*;

/**
* A collection of motifs with identical TYPE, RVGENUS, SUBGENE, MOTIFGROUP and exactlyAligned.
*/
public class MotifLitter {

/**
* From seq_type in Motif database.
*/
	public final String LITTERTYPE;
	
/**
* From rvgenus in Motif database. Specifies the virus genus(s) to which this Litter belongs.
*/
	public final String LITTERRVGENUS;
	
/**
* From subgene in Motif database. The SubGene to which this Litter belongs.
*/
	public final SubGene LITTERSUBGENE;
	
/**
* From motifgroup in Motif database. Specifies MotifGroup  to which this Litter belongs.
*/
	public final MotifGroup LITTERMOTIFGROUP;

/**
* True if this consists of an LTRMotif.
*/
  public final boolean ISLTRLITTER;
	
/**
* True if Motifs in this have a precise location in Alignments.
* At present only true for AcidMotifs.
*/
	public final boolean LITTEREXACTLYALIGNED;

/**
* True if all Motifs have frameDefined = true;
*/
	public boolean litterFrameDefined;
	
  private final Database LITTERDATABASE; // the Database from which this was defined
	private Motif[ ] littermembers = null; // the members
	private Vector templitter = new Vector(); // for temporary storage
	private RVector rvector = null; // see getRVector

// hits by all detection Motifs in this.
	private MotifHit[ ] detectionHits = null;

// hits by all characterization Motifs in this within some range.
	private MotifHit[ ] characHits = null;
	
/**
* Constructs a rudimentary instance. More motifs must be added using addMotif,
* and the procedure concluded with finalizeLitter before instance ready for use.
* @param	seed	A first Motif to define characteristics.
*/
	MotifLitter(Motif seed) throws RetroTectorException {
		templitter.addElement(seed);
		LITTERTYPE = seed.MOTIFTYPE;
		LITTERRVGENUS = seed.MOTIFRVGENUS;
		LITTERSUBGENE = seed.MOTIFSUBGENE;
		LITTERMOTIFGROUP = seed.MOTIFGROUP;
    ISLTRLITTER = LITTERMOTIFGROUP.ISLTRMOTIFGROUP;
    LITTERDATABASE = seed.MOTIFDATABASE;
		LITTEREXACTLYALIGNED = seed.exactlyAligned;
		litterFrameDefined = true; // so far
	} // end of constructor(Motif)
	
/**
* Adds one Motif, if of the right kind.
* @param	theMotif	The Motif to try to add.
* @return	True if it was accepted.
*/
 	final boolean addMotif(Motif theMotif) throws RetroTectorException {
    if (ISLTRLITTER) {
      return false;
    }
		if (!(theMotif.MOTIFTYPE.equals(LITTERTYPE))) {
			return false;
		}
		if (!theMotif.MOTIFRVGENUS.equals(LITTERRVGENUS)) {
			return false;
		}
		if (theMotif.MOTIFSUBGENE != LITTERSUBGENE) {
			return false;
		}
		if (theMotif.MOTIFGROUP != LITTERMOTIFGROUP) {
			return false;
		}
		if (theMotif.MOTIFDATABASE != LITTERDATABASE) {
			return false;
		}
		if (theMotif.exactlyAligned != LITTEREXACTLYALIGNED) {
			return false;
		}
		if (!theMotif.frameDefined) {
			litterFrameDefined = false;
		}
		templitter.addElement(theMotif);
		return true;
	} // end of addMotif(Motif)
	
/**
* Makes instance ready for use.
*/
 	final void finalizeLitter() throws RetroTectorException {
    if (ISLTRLITTER && (templitter.size() != 1)) {
      RetroTectorException.sendError(this, "More than one Motif in LTR Motif litter");
    }
		littermembers = new Motif[templitter.size()];
		for (int i=0; i<littermembers.length; i++) {
			littermembers[i] = (Motif) templitter.elementAt(i);
		}
		rvector = new RVector(LITTERRVGENUS);
		templitter = null; // to garbage
	} // end of finalizeLitter()
	
/**
* Pushes hits in no particular order on a Stack of MotifHits.
* @param	hitstack	The stack to push onto.
* @param	pushAll		If true, use all hits, otherwise only detection motif hits.
* @param	firstPush	First internal position to use.
* @param	lastPush	Last internal position to use.
* @return false If no hits were added, otherwise true.
*/
	final boolean pushHits(Stack hitstack, boolean pushAll, int firstPush, int lastPush) {

		boolean returncode = false;
				
    if (detectionHits != null) {
      for (int p=0; p<detectionHits.length; p++) {
        if ((detectionHits[p].MOTIFHITHOTSPOT >= firstPush) & (detectionHits[p].MOTIFHITHOTSPOT <= lastPush)) {
          hitstack.push(detectionHits[p]);
          returncode = true;
        }
      }
    }

    if (pushAll && (characHits != null)) {
      for (int p = 0; p<characHits.length; p++) {
        if ((characHits[p].MOTIFHITHOTSPOT >= firstPush) & (characHits[p].MOTIFHITHOTSPOT <= lastPush)) {
          hitstack.push(characHits[p]);
          returncode = true;
        }
      }
    }

		return returncode;
	} // end of pushHits(Stack, boolean, int, int)
  
/**
*  @return	The number of Motifs in this.
*/
	public final int nrOfMotifs() {
		return littermembers.length;
	} // end of nrOfMotifs()
	
/**
* @param	index	Array index of Motif.
* @return The indexed Motif or null.
*/
 	public final Motif getMotif(int index) {
		if ((index < 0) | (index >= littermembers.length)) {
			return null;
		} else {
			return littermembers[index];
		}
	} // end of getMotif(int)
	
/**
* @return	A copy of the RVector of this Litter.
*/
	public final RVector getRVector() {
		return rvector.copy();
	} // end of getRVector()


/**
* Removes old Hits and refreshes Motifs.
* @param	inf	RefreshInfo to use in Motifs.
*/
	public final void refresh(Motif.RefreshInfo inf) throws RetroTectorException {
		detectionHits = null;
    characHits = null;
    for (int i=0; i<littermembers.length; i++) {
      RetroTectorEngine.setInfoField("Refreshing Motif " + littermembers[i].MOTIFID + " in " + inf.TARGETDNA.strandString() + "strand");
      littermembers[i].refresh(inf);
    }
	} // end of refresh(Motif.RefreshInfo)

/**
* @return	Total number of Motif hits.
*/
	public final int nrOfHits() {
		if (detectionHits == null) {
      if (characHits == null) {
        return 0;
      }
      return characHits.length;
		} else {
      int r = detectionHits.length;
      if (characHits != null) {
        r += characHits.length;
      }
			return r;
		}
	} // end of nrOfHits()
	
		
/**
* Scores all hits by detection Motifs in this litter, discarding those with score
* less than threshold, and retaining only the best hit at each position.
* @param	targetDNA		The DNA to score in.
* @param	scoreFirst	First internal position to score at.
* @param	scoreLast		Last internal position to score at.
*/
 	public final void scoreLitter(DNA targetDNA, int scoreFirst, int scoreLast) throws RetroTectorException {
 		if (detectionHits != null) {
 			throw new RetroTectorException(Utilities.className(this), "Litter not empty");
 		}
 			
    for (int i=0; i<littermembers.length; i++) {
      if (littermembers[i].MOTIFLEVEL.equals(Motif.DETECTIONLEVEL)) {
        littermembers[i].localRefresh(scoreFirst, scoreLast);
      }
    }
    
		float score = -Float.MAX_VALUE;
		float topscore; // the highest score at one position
		Stack tempHits = new Stack();
		int counter = 1000; // progress counter
    Motif m;
		Object o;
    MotifHit mh;
		MotifHit[ ] mhs;
		MotifHit topMotifHit; // the MotifHit with topscore
    
    for (int pos = scoreFirst; pos<=scoreLast; pos++) {
      topscore = -Float.MAX_VALUE;
      topMotifHit = null;
      for (int lit=0; lit<littermembers.length; lit++) { // get best hit at this position
        m = littermembers[lit];
        if (m.MOTIFLEVEL.equals(Motif.DETECTIONLEVEL)) {
					o = m.getMotifHitsAt(pos);
					if (o instanceof MotifHit) {
						mh = (MotifHit) o;
						if ((mh != null) && (mh.MOTIFHITSCORE > topscore)) {
							topscore = mh.MOTIFHITSCORE;
							topMotifHit = mh;
						}
					} else if (o instanceof MotifHit[ ]) {
						mhs = (MotifHit[ ]) o;
						for (int i=0; i<mhs.length; i++) {
							mh = mhs[i];
							if ((mh != null) && (mh.MOTIFHITSCORE > topscore)) {
								topscore = mh.MOTIFHITSCORE;
								topMotifHit = mh;
							}
						}
					}
        }
      }
      if (topscore > 0.0f) {
        tempHits.push(topMotifHit);
      }
      if (--counter <= 0) {
        counter = 1000;
        RetroTectorEngine.showProgress();
      }
    }
// all valid hits now in tempHits. Transfer to detectionHits.		
    detectionHits = new MotifHit[tempHits.size()];
    tempHits.copyInto(detectionHits);
    tempHits = null; // throw into garbage

	} // end of scoreLitter(DNA, int, int)
	
/**
* Scores all hits by characterization motifs in this litter inside a range,
* discarding those with score less than threshold, and retaining only
* the best hit at each position.
* @param	targetDNA	the DNA to score in.
* @param	scoreFirst	First internal position to score at.
* @param	scoreLast		Last internal position to score at.
*/
 	public final void scoreCharac(DNA targetDNA, int scoreFirst, int scoreLast) throws RetroTectorException {
 			
    for (int i=0; i<littermembers.length; i++) {
      if (littermembers[i].MOTIFLEVEL.equals(Motif.CHARACTERIZATIONLEVEL)) {
        littermembers[i].localRefresh(scoreFirst, scoreLast);
      }
    }
    
		float score = -Float.MAX_VALUE;
		float topscore; // the highest score at one position
		Stack tempHits = new Stack();
		int counter = 1000; // progress counter
    Motif m;
		Object o;
    MotifHit mh;
		MotifHit[ ] mhs;
		MotifHit topMotifHit; // the MotifHit with topscore

    for (int pos = scoreFirst; pos<=scoreLast; pos++) {
      topscore = -Float.MAX_VALUE;
      topMotifHit = null;
      for (int lit=0; lit<littermembers.length; lit++) { // get best hit at this position
        m = littermembers[lit];
        if (m.MOTIFLEVEL.equals(Motif.CHARACTERIZATIONLEVEL)) {
					o = m.getMotifHitsAt(pos);
					if (o instanceof MotifHit) {
						mh = (MotifHit) o;
						if ((mh != null) && (mh.MOTIFHITSCORE > topscore)) {
							topscore = mh.MOTIFHITSCORE;
							topMotifHit = mh;
						}
					} else if (o instanceof MotifHit[ ]) {
						mhs = (MotifHit[ ]) o;
						for (int i=0; i<mhs.length; i++) {
							mh = mhs[i];
							if ((mh != null) && (mh.MOTIFHITSCORE > topscore)) {
								topscore = mh.MOTIFHITSCORE;
								topMotifHit = mh;
							}
						}
					}
        }
      }
      if (topscore > 0.0f) {
        tempHits.push(topMotifHit);
      }
      if (--counter <= 0) {
        counter = 1000;
        RetroTectorEngine.showProgress();
      }
    }
// all valid hits now in tempHits. Transfer to detectionHits.		
    characHits = new MotifHit[tempHits.size()];
    tempHits.copyInto(characHits);
    tempHits = null; // throw into garbage

	} // end of scoreCharac(DNA, int, int)
	
/**
* @return	A String describing this Litter.
*/
	public final String signature() {
		return LITTERMOTIFGROUP.MOTIFGROUPNAME + ":" + LITTERRVGENUS;
	} // end of signature()
	
/**
* @return	A MotifLitter like this, refreshed but not scored.
*/
  final MotifLitter motifLitterCopy() throws RetroTectorException {
    MotifLitter result = new MotifLitter(littermembers[0].motifCopy());
    for (int i=1; i<littermembers.length; i++) {
      result.addMotif(littermembers[i].motifCopy());
    }
    result.finalizeLitter();
    return result;
  } // end of motifLitterCopy()
	
} // end of MotifLitter
