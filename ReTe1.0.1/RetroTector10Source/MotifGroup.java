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

import java.util.*;

/**
* A collection of MotifLitters (and thus of Motifs) expected to
* occur at the same place in a virus, and of which therefore
* there must be only one in a particular Chain.
* It defines a distance range from the hot spot of the containing SubGene
* plus optionally, distance ranges from other MotifGroups
*/
public class MotifGroup {
	
/**
* Name of this MotifGroup.
*/
	public final String MOTIFGROUPNAME;
	
/**
* The SubGene to which distance is defined.
*/
	public final SubGene SUBGENE;
	
/**
* The range of allowed distances to SubGene hotspot.
*/
	public final DistanceRange DISTANCERANGE;
	
/**
* True if this is a group of LTRMotifs.
*/
  public final boolean ISLTRMOTIFGROUP;
  
/**
* Index of this MotifGroup in SubGene.
*/
	public int index;

/**
* Allowed range of distance to start of Gene to which this belongs;
*/
	public DistanceRange geneStartDistance;

/**
* Allowed range of distance to end of Gene to which this belongs;
*/
	public DistanceRange geneEndDistance;
	
/**
* True if all Motifs in this have a precise location in Alignments.
* At present only true for AcidMotifs and SplitAcidMotifs.
*/
	public boolean groupExactlyAligned;
	
/**
* True if all MotifLitters have frameDefined = true;
*/
	public boolean groupFrameDefined;
	

  private final Database GROUPDATABASE; // the Database from which this was defined
  
	private final String SOURCESTRING; // the dataLine from which this was created
  
// to store optional DistanceRanges to other MotifGroups
	private Hashtable groupDistances = null;

// the MotifLitters contained in this.
	private MotifLitter[ ] litters;
	private Stack templitters = new Stack(); // for temporary use
	
/**
* Constructor. Litters are added later with addLitter() and finalize().
* @param	name			Name of this MotifGroup.
* @param 	inString	One line from MotifGroups.txt (see readMotifGroups()).
* @param	database	The Database from which inString hails.
*/
	MotifGroup(String name, String inString, Database database) throws RetroTectorException {
    SOURCESTRING = inString;
		MOTIFGROUPNAME = name.trim();
    GROUPDATABASE = database;
		String[ ] parts = Utilities.splitString(inString);
		SUBGENE = GROUPDATABASE.getSubGene(parts[0]);
    ISLTRMOTIFGROUP = SUBGENE.ISLTRSUBGENE;
		DISTANCERANGE = new DistanceRange(parts[1]);
		for (int i=2; (i<parts.length) && !parts[i].startsWith("{"); i++) {
			if (groupDistances == null) {
				groupDistances = new Hashtable();
			}
			int ind = parts[i].indexOf(':');
			groupDistances.put(parts[i].substring(0, ind), new DistanceRange(parts[i].substring(ind + 1)));
		}
		groupExactlyAligned = true; // so far
		groupFrameDefined = true; // so far
	} // end of constructor(String, String, Database)

/**
* Adds one litter.
* @param	lit	The MotifLitter to add.
*/	
	final void addLitter(MotifLitter lit) {
		templitters.push(lit);
		groupExactlyAligned = groupExactlyAligned & lit.LITTEREXACTLYALIGNED;
		groupFrameDefined = groupFrameDefined & lit.litterFrameDefined;
	} // end of addLitter(MotifLitter)
	
/**
* Makes this final after adding Litters.
*/	
	final void finalizeMotifGroup() {
		litters = new MotifLitter[templitters.size()];
		templitters.copyInto(litters);
		templitters = null;
	} // end of finalizeMotifGroup()

/**
* Refreshes MotifLitters.
* @param	inf	RefreshInfo to send down the line.
*/
	public final void refresh(Motif.RefreshInfo inf) throws RetroTectorException {
    for (int i=0; i<litters.length; i++) {
      litters[i].refresh(inf);
    }
	} // end of refresh((Motif.RefreshIno)
  
/**
* @param	otherGroup	A MotifGroup.
* @return		Acceptable distance range to otherGroup, or null if not defined. See longDistanceTo().
*/
	public final DistanceRange distanceToGroup(MotifGroup otherGroup) {
		if (groupDistances == null) {
			return null;
		}
		return (DistanceRange) groupDistances.get(otherGroup.MOTIFGROUPNAME);
	} // end of distanceToGroup(MotifGroup)
	
/**
* @param	otherGroup	A MotifGroup.
* @return		Acceptable distance range to otherGroup, calculated if necessary by going through the SubGenes.
*/
	public final DistanceRange longDistanceTo(MotifGroup otherGroup) {
    DistanceRange dr = distanceToGroup(otherGroup);
    if (dr != null) {
      return dr;
    }
		
		int ld;
		int hd;
		if (SUBGENE == otherGroup.SUBGENE) {
			ld = DISTANCERANGE.LOWESTDISTANCE;
			hd = DISTANCERANGE.HIGHESTDISTANCE;
			dr = otherGroup.DISTANCERANGE.inverse();
			ld += dr.LOWESTDISTANCE;
			hd += dr.HIGHESTDISTANCE;
		} else {
			ld = DISTANCERANGE.LOWESTDISTANCE;
			hd = DISTANCERANGE.HIGHESTDISTANCE;
			SubGene sg = otherGroup.SUBGENE;
			dr = SUBGENE.distanceTo(sg);
			ld += dr.LOWESTDISTANCE;
			hd += dr.HIGHESTDISTANCE;
			dr = otherGroup.DISTANCERANGE.inverse();
			ld += dr.LOWESTDISTANCE;
			hd += dr.HIGHESTDISTANCE;
		}
    return new DistanceRange(ld, hd);
	} // end of longDistanceTo(MotifGroup)
	
/**
* @param	index	Array index of a MotifLitter.
* @return A particular MotifLitter or null.
*/
 	public final MotifLitter getLitter(int index) {
 		if (litters == null) {
 			return null;
 		}
		if ((index < 0) | (index >= litters.length)) {
			return null;
		} else {
			return litters[index];
		}
	} // end of getLitter(int)
		
/**
* Pushes all the Motif hits in the group within some limits.
* @param	theStack	The Stack to push onto.
* @param	pushAll		If true, use all hits, otherwise only detection motif hits.
* @param	firstPush	First internal position to use.
* @param	lastPush	Last internal position to use.
* @return	True if anything was pushed.
*/
	public final boolean pushGroupHits(Stack theStack, boolean pushAll, int firstPush, int lastPush) {
		if ((litters == null) || (litters.length == 0)) {
			return false;
		}
		boolean b = false;
		for (int f=0; f<litters.length; f++) {
			if (litters[f].pushHits(theStack, pushAll, firstPush, lastPush)) { // let the litters do the work
				b = true;
			}
		}
		return b;
	} // end of pushGroupHits(Stack, boolean, int, int)
	
/**
* Scores detection Motifs in all MotifLitters in this.
* @param	targetDNA	The DNA to score in.
* @param	scoreFirst	First internal position to score at.
* @param	scoreLast		Last internal position to score at.
*/
  public final void scoreMotifGroup(DNA targetDNA, int scoreFirst, int scoreLast) throws RetroTectorException {
    for (int i=0; i<litters.length; i++) {
      litters[i].scoreLitter(targetDNA, scoreFirst, scoreLast);
    }
  } // end of scoreMotifGroup(DNA, int, int)

/**
* Scores characterization Motifs in all MotifLitters in this within a range.
* @param	targetDNA	The DNA to score in.
* @param	scoreFirst	The first position to score at.
* @param	scoreLast		The last position to score at.
*/
  public final void scoreCharac(DNA targetDNA, int scoreFirst, int scoreLast) throws RetroTectorException {
    for (int i=0; i<litters.length; i++) {
      litters[i].scoreCharac(targetDNA, scoreFirst, scoreLast);
    }
  } // end of scoreCharac(DNA, int, int)
  
/**
* @return	A MotifGroup similar to this, refreshed but not applied.
*/
  final MotifGroup motifGroupCopy() throws RetroTectorException {
    MotifGroup result = new MotifGroup(MOTIFGROUPNAME, SOURCESTRING, GROUPDATABASE);
      for (int i=0; i<litters.length; i++) {
        result.addLitter(litters[i].motifLitterCopy());
      }
      result.finalizeMotifGroup();
      return result;
  } // end of motifGroupCopy()
  
} // end of MotifGroup
