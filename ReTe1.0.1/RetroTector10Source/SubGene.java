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

import java.io.*;
import java.util.*;


/**
* A collection of MotifGroups with a defined hotspot.
*/
public class SubGene {

/**
* Largest number of SubGenes allowed (=20);
*/
  public static final int MAXSUBGENENUMBER = 20;

/**
* = "PBS";
*/
	public static final String PBS = "PBS";
	
/**
* = "PPT";
*/
	public static final String PPT = "PPT";
	

/**
* Name of SubGene
*/
	public final String SUBGENENAME;

/**
* Unique single bit code.
*/
	public final int SUBGENEMASK;
	
/**
* Index of this SubGene in subGeneNames.
*/
	public final int SUBGENEINDEX;
  
/**
* True if this is an LTR.
*/
  public final boolean ISLTRSUBGENE;
  
// The databse from which this was defined.
  private final Database SUBGENEDATABASE;

// the actual contents
	private MotifGroup[ ] groups;
// for assembly of MotifGroups
	private Stack tempGroups = new Stack();

// allowed ranges of distances to other SubGenes, by name or SubGene.
	private Hashtable distances = new Hashtable();

	private SubGeneHit[ ] allHits = null; // final
	private Stack tempsubgenehits = null; // and temporary storage for hits with this SubGene
	
	
/**
* Constructor.
* @param	name			The name of this SubGene.
* @param	number		The index of this SubGene in SubGeneNames.
* @param	database	The Databased in use.
*/
	SubGene(String name, int number, Database database) throws RetroTectorException {
		SUBGENENAME = name;
    SUBGENEDATABASE = database;
    if (name.equalsIgnoreCase(Executor.LTR5KEY) | name.equalsIgnoreCase(Executor.LTR3KEY)) {
      ISLTRSUBGENE = true;
    } else {
      ISLTRSUBGENE = false;
    }
		SUBGENEMASK = (1 << number);
		SUBGENEINDEX = number;
	} // end of constructor(String, int, Database)
	
/**
* @return	Number of SubGeneHits with this SubGene, or -1.
*/
	public final int nrOfHits() {
		if (allHits == null) {
			return -1;
		}
		return allHits.length;
	} // end of nrOfHits()

/**
* Empties this SubGene of all SubGeneHits and refeshes its parts.
* @param	inf	RefreshInfo to send down the line.
*/
	public final void refresh(Motif.RefreshInfo inf) throws RetroTectorException {
		allHits = null;
		tempsubgenehits = null;
    for (int i=0; i<groups.length; i++) {
      groups[i].refresh(inf);
    }
	} // end of refresh(Motif.RefreshInfo)
	
/**
* Scores all detection Motifs in this.
* @param	targetDNA		The DNA to score in.
* @param	scoreFirst	First internal position to score at.
* @param	scoreLast		Last internal position to score at.
*/
  public final void scoreSubGene(DNA targetDNA, int scoreFirst, int scoreLast) throws RetroTectorException {
    for (int i=0; i<groups.length; i++) {
      groups[i].scoreMotifGroup(targetDNA, scoreFirst, scoreLast);
    }
  } // end of scoreSubGene(DNA, int, int)

/**
* Scores all characterization Motifs in this within a range.
* @param	targetDNA		The DNA to score in.
* @param	scoreFirst	First position to score at.
* @param	scoreLast		Last position to score at.
*/
  public final void scoreCharac(DNA targetDNA, int scoreFirst, int scoreLast) throws RetroTectorException {
    for (int i=0; i<groups.length; i++) {
      groups[i].scoreCharac(targetDNA, scoreFirst, scoreLast);
    }
  } // end of scoreCharac(DNA, int, int)
  
/**
* Collects all valid SubGeneHits for this SubGene, ordering them by descending score.
* @param	frameBonus	Factor to multiply score by if all MotifHits in same reading frame.
* @param	firstScore	First internal position to use.
* @param	lastScore		Last internal position to use.
* @return	True if any SubGeneHits were found.
*/
 	public final boolean collectSubGeneHits(float frameBonus, int firstScore, int lastScore) throws RetroTectorException {
	 		
		if (groups.length == 0) {
			return false;
		}
		
		collectHits(frameBonus, true, firstScore, lastScore);
		allHits = new SubGeneHit[tempsubgenehits.size()];
		tempsubgenehits.copyInto(allHits);
		Utilities.sort(allHits);

		tempsubgenehits = null; // throw it to the garbage collector
		return true;		
	} // end of collectSubGeneHits(float, int, int)
	
// does the hard work for collectSubGeneHits
	private void collectHits(float frameBonus, boolean getAll, int firstScore, int lastScore) throws RetroTectorException {
		Stack mainStack = new Stack();
		
		MotifGroup tempgroup;
		MotifHit temphit;
		int fam;
		int pos;
		tempsubgenehits = new Stack();
		SubGeneHit.resetStack();
// try starting with each group
		for (int group=0; group < groups.length; group++) {
			mainStack.push(groups[group]); // push group
// the internal workings of this mechanism are not quite simple. Seems to work though
			groups[group].pushGroupHits(mainStack, getAll, firstScore, lastScore); // push all its hits
			while (mainStack.size() > 0) {
				if (mainStack.peek() instanceof MotifGroup) { // no hits found
					if (!SubGeneHit.empty()) {
						SubGeneHit gh = new SubGeneHit(frameBonus);
						insertHit(gh); // make a new SubGeneHit and try it
						SubGeneHit.pop();
					}
					mainStack.pop(); // no hits found
				} else { // hits found
					temphit = (MotifHit) mainStack.pop();
					pos = temphit.MOTIFHITHOTSPOT;
					tempgroup = temphit.PARENTMOTIF.MOTIFGROUP;
					if (SubGeneHit.pushMotifHit(temphit)) { // move one hit to hit stack
						mainStack.push(tempgroup); // push its group
						for (fam = tempgroup.index + 1; fam < groups.length; fam++) { // push all successor hits with right distance
							groups[fam].pushGroupHits(mainStack, getAll, firstScore, lastScore);
						}
					}
				}
			}
		}
	} // end of collectHits(float, boolean, int, int)

/**
* @param	index	Array index in groups
* @return	The MotifGroup with that index, or null
*/
 	public final MotifGroup getMotifGroup(int index) {
		if ((index < 0) | (index >= groups.length)) {
			return null;
		} else {
			return groups[index];
		}
	} // end of getMotifGroup(int)

/**
* @return Array of all Motif in this.
*/
	public final Motif[ ] getAllMotifs() {
		MotifGroup mg;
		MotifLitter ml;
		Motif m;
		Stack st = new Stack();
		for (int group=0; group<groups.length; group++) {
			mg = groups[group];
			for (int litter=0; (ml=mg.getLitter(litter)) != null; litter++) {
				for (int motif=0; (m=ml.getMotif(motif)) != null; motif++) {
					st.push(m);
				}
			}
		}
		Motif[ ] result = new Motif[st.size()];
		st.copyInto(result);
		return result;
			
	} // end of getAllMotifs()
	
/**
* @param	sg	Another SubGene.
* @return	The acceptable range of the distance between a hit in this and a hit in sg.
*/
	public final DistanceRange distanceTo(SubGene sg) {
    Object o = distances.get(sg.SUBGENENAME);
		return (DistanceRange) o;
	} // end of distanceTo(SubGene)

/**
* Adds to hitstack the best subGeneHitLimit SubGeneHits in this subgene
* which are between firstPush and lastPush, and correctly positioned relative
* to refHit. If refHit is null, all hits within range are pushed.
* @param	hitstack				A Stack to push hits onto.
* @param	refHit					A SubGeneHit to define position, or null.
* @param	subGeneHitLimit	Max number of hits to push.
* @param	firstPush				First hit position to push
* @param	lastPush				Last hit position to push
* @return	True if any SubGeneHits were added
*/
	public final boolean pushSubGeneHits(Stack hitstack, SubGeneHit refHit, int subGeneHitLimit, int firstPush, int lastPush) {
		if (allHits == null) {
			return false;
		}
		
		boolean returncode = false;
		
		SubGeneHit sgh;
		if (refHit == null) { //  push all hits
			for (int g=0; g<allHits.length; g++) {
        sgh = allHits[g];
        if ((sgh.SGHITHOTSPOT >= firstPush) & (sgh.SGHITHOTSPOT <= lastPush)) {
          hitstack.push(sgh);
          returncode = true;
        }
			}
			return returncode;
		}
		
		SubGene refSubGene = refHit.PARENTSUBGENE;
		int refpos = refHit.SGHITHOTSPOT;
		int posdiff;
		DistanceRange dist = refSubGene.distanceTo(this);
				
		int counter = subGeneHitLimit; // upper limit on number of hits pushed
		for (int g=0; g<allHits.length; g++) { // try one hit at a time
			sgh = allHits[g];
			posdiff = sgh.SGHITHOTSPOT - refpos; // actual distance to that hit
      if ((sgh.SGHITHOTSPOT >= firstPush) && (sgh.SGHITHOTSPOT <= lastPush) && dist.containsDistance(posdiff)) { // is distance OK?
				hitstack.push(sgh); // yes, push hit
				returncode = true;
				counter--; // decrement pushed counter
			}
			if (counter <= 0) {
				return returncode;
			}
		}
		return returncode;
	} // end of pushSubGeneHits(Stack, SubGeneHit, int, int, int)
	
/**
* Create Hashtable of allowed ranges of distances to other SubGenes, accessible through SubGene or its name.
* For Database to use.
* @param	distString	A line of distance specifiers, from SubGenes.txt.
*/
	final void makeDistances(String distString) throws RetroTectorException {
		String[ ] parts = Utilities.splitString(distString);
		for (int i=0; i<parts.length; i++) {
			distances.put(SUBGENEDATABASE.subGeneNames[i], new DistanceRange(parts[i]));
			distances.put(SUBGENEDATABASE.getSubGene(SUBGENEDATABASE.subGeneNames[i]), new DistanceRange(parts[i]));
		}
	} // end of makeDistances(String)
	
/**
* Adds a MotifGroup in the process of building a SubGene.
* @param	gr	A MotifGroup to add.
*/
	final void addGroup(MotifGroup gr) {
		tempGroups.push(gr);
	} // end of addGroup(MotifGroup)
	
/**
* Makes final SubGene, having added all MotifGroups.
*/
	final void finalizeSubGene() {
		groups = new MotifGroup[tempGroups.size()];
		tempGroups.copyInto(groups);
		tempGroups = null;
		for (int i=0; i<groups.length; i++) {
			groups[i].index = i;
		}
	} // end of finalizeSubGene

// are all MotifHits in gh2 also in gh1?
	private final boolean container(SubGeneHit gh1, SubGeneHit gh2) {
		MotifHit mh;
		for (int h=0; (mh=gh2.getMotifHit(h)) != null; h++) {
			if (!gh1.containsMotifHit(mh)) {
				return false;
			}
		}
		return true;
	} // end of container(SubGeneHit, SubGeneHit)
	
/**
* Tries to add one more SubGeneHit to this SubGene. It is accepted if
* it is not contained in a superior earlier SubGeneHit of the same rvgenus,
* and does not have lower score than another SubGeneHit with the same
* rvgenus and position. It may kick out a worse SubGeneHit.
* @param	theHit	The SubGeneHit to try.
* @return	True if theHit was accepted.
*/
 	private final boolean insertHit(SubGeneHit theHit) {

		if (tempsubgenehits == null) {
			tempsubgenehits = new Stack();
		}

		if (tempsubgenehits.size() == 0) { // no SubGeneHits there so far. Add theHit
			tempsubgenehits.push(theHit);
			return true;
		}

// hits already present. Check theHit against each with lower position
		SubGeneHit compHit;
		float score = theHit.fetchScore();
		int pos = theHit.SGHITHOTSPOT;
		String rvgenus = theHit.RVGENUS;
		boolean mustInsert = false;
		for (int index=0; index<tempsubgenehits.size(); index++) {
			compHit = (SubGeneHit) tempsubgenehits.elementAt(index);
// is theHit contained by one with better score?
			if (compHit.RVGENUS.equals(rvgenus)) {
				if ((compHit.fetchScore() > score) && !mustInsert && container(compHit, theHit)) {
					return false;
				}
// is there one with same position?
				if ((compHit.SGHITHOTSPOT == pos)) {
					if ((compHit.fetchScore() >= score) & !mustInsert) { // then one must die
						return false;
					} else {
						tempsubgenehits.removeElementAt(index--);
						mustInsert = true;
					}
				} else {
// is there one contained by this?
          if ((compHit.fetchScore() <= score) && container(theHit, compHit)) {
            tempsubgenehits.removeElementAt(index--);
          }
        }
			}
		}
		tempsubgenehits.push(theHit);
		return true;
	} // end of insertHit(SubGeneHit)
	
/**
* @return	A SubGene similar to this.
*/
  public final SubGene subGeneCopy() throws RetroTectorException {
    SubGene result = new SubGene(SUBGENENAME, SUBGENEINDEX, SUBGENEDATABASE);
    for (int i=0; i<groups.length; i++) {
      result.addGroup(groups[i].motifGroupCopy());
    }
    result.finalizeSubGene();
    return result;
  } // end of subGeneCopy()
  
} // end of SubGene
