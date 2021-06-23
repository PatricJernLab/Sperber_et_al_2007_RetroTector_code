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

import java.io.*;
import java.util.*;

/**
* Class representing a gene, ie gag, pro, pol or env.
*/
public class Gene {

/**
* @param	groupname	Name of a MotifGroup.
* @return The Gene in which the MotifGroup belongs, or null.
*/
	public final static Gene getGeneOf(String groupName) {
		
		Database database = RetroTectorEngine.getCurrentDatabase();
		if (database.getGene("Gag").groupHere(groupName)) {
			return database.getGene("Gag");
		}
		if (database.getGene("Pro").groupHere(groupName)) {
			return database.getGene("Pro");
		}
		if (database.getGene("Pol").groupHere(groupName)) {
			return database.getGene("Pol");
		}
		if (database.getGene("Env").groupHere(groupName)) {
			return database.getGene("Env");
		}
		return null;
		
	} // end of getGeneOf(String)

/**
* Name of this Gene (Gag, Pro, Pol or Env).
*/
	public final String GENENAME;

/**
* The constituent MotifGroups.
*/
	public final MotifGroup[ ] GENEMOTIFGROUPS;
  
  private final Database GENEDATABASE; // the Database from which this was defined
	
/**
* Constructor. Also sets geneStartDistance and geneEndDistance in constituent MotifGroups.
* @param	name			The name of this Gene.
* @param	mogr			String[ ] describing the MotifGroups.
* @param	database	The Database from which mogr hails.
*/
	Gene(String name, Object mogr, Database database) throws RetroTectorException {
		GENENAME = name;
    GENEDATABASE = database;
		String[ ] mg = (String[ ]) mogr;
		GENEMOTIFGROUPS = new MotifGroup[mg.length];
		for (int p=0; p<GENEMOTIFGROUPS.length; p++) {
			String[ ] ss = Utilities.splitString(mg[p]);
			MotifGroup g = GENEDATABASE.getMotifGroup(ss[0]);
			if (g == null) {
				RetroTectorException.sendError(this, "The MotifGroup " + ss[0], "is mentioned in Genes.txt but not defined in MotifGroups.txt");
			}
			g.geneStartDistance = new DistanceRange(ss[1]);
			g.geneEndDistance = new DistanceRange(ss[2]);
			GENEMOTIFGROUPS[p] = g;
		}
	} // end of constructor(String, Object, Database)
	
/**
* @param	theChain	A Chain.
* @return Stack of MotifHits in theChain, belonging in this.
*/
	public final Stack findHitsInChain(Chain theChain) {
	
		Stack theStack = new Stack();
		
		MotifHit mh;
		
    for (int s=0; s<GENEMOTIFGROUPS.length; s++) {
      mh = theChain.hitInMotifGroup(GENEMOTIFGROUPS[s]);
      if (mh != null) {
        theStack.push(mh);
      }
    }
		return theStack;

	} // end of findHitsInChain(Chain)

/**
* @param	groupname	Name of a MotifGroup.
* @return true if the MotifGroup belongs here.
*/
	public final boolean groupHere(String groupname) {
		for (int i=0; i<GENEMOTIFGROUPS.length; i++) {
			if (GENEMOTIFGROUPS[i].MOTIFGROUPNAME.equals(groupname)) {
				return true;
			}
		}
		return false;
	} // end of groupHere(String)
	
	public String toString() {
		String s = "Gene " + GENENAME;
		for (int i=0; i<GENEMOTIFGROUPS.length; i++) {
			s = s + "\n  " + GENEMOTIFGROUPS[i].MOTIFGROUPNAME + " " + GENEMOTIFGROUPS[i].geneStartDistance.toString() + " " + GENEMOTIFGROUPS[i].geneEndDistance.toString();
		}
		return s;
	} // end of toString()
	
} // end of Gene
