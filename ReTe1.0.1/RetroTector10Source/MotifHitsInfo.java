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
* Information about a number of MotifHits, for ORFID.
*/
public class MotifHitsInfo {

/**
* Average score of MotifHits.
*/
	public final float AVERSCORE;
	
/**
* Sum of squared scores of MotifHits.
*/
	public final float SSSCORES;
	
/*
* MotifHits, ordered by position.
*/
	private final MotifHitInfo[ ] THEINFOS;

/**
* Constructor using a Stack of MotifHits.
* @param	inStack	That Stack.
* @param	theDNA	The DNA to use.
*/
	public MotifHitsInfo(Stack inStack, DNA theDNA) throws RetroTectorException {
	
		THEINFOS = new MotifHitInfo[inStack.size()];
		int tp = 0;
		float sum = 0.0f;
		float ssum = 0.0f;
		int n = 0;
		while (inStack.size() > 0) { // transfer to THEINFOS, in order
			int in = Integer.MAX_VALUE;
			int ind = -1;
			int p;
			for (int i=0; i<inStack.size(); i++) { // find lowest position
				if ((p = (((MotifHit) inStack.elementAt(i)).MOTIFHITHOTSPOT)) < in) {
					in = p;
					ind = i;
				}
			}
			THEINFOS[tp] = new MotifHitInfo((MotifHit) inStack.elementAt(ind), tp, theDNA);
			sum += THEINFOS[tp].SCORE;
			ssum += THEINFOS[tp].SCORE * THEINFOS[tp].SCORE;
			n++;
			tp++;
			inStack.removeElementAt(ind);
		}
		AVERSCORE = sum / n;
		SSSCORES = ssum;
	} // end of constructor(Stack, DNA)
	
/**
* Constructor using a String array, as given by toStrings.
* @param	inStrings		That String array.
*	@param	theDNA			The DNA to use.
*	@param	master			The Alignment to use.
*	@param	scriptName	Name of relevant ORFID script.
*	@param	database		The relevant Database.
*/
	public MotifHitsInfo(String[ ] inStrings, DNA theDNA, Alignment master, String scriptName, Database database) throws RetroTectorException {
		THEINFOS = new MotifHitInfo[inStrings.length];
		for (int i=0; i<inStrings.length; i++) {
			THEINFOS[i] = new MotifHitInfo(inStrings[i], i + 1, theDNA, master);
		}

		boolean inorder;
		boolean reordered = false;
		do { // check that order in alignment agrees with order in array and repair minor disorders
// by marking one as out of order
			inorder = true;
			for (int ii=1; ii<THEINFOS.length; ii++) {
				if ((THEINFOS[ii].POSINMASTER >= 0) & 
							THEINFOS[ii].usefulToORFID(database) & 
							(THEINFOS[ii - 1].POSINMASTER >= 0) & 
							THEINFOS[ii - 1].usefulToORFID(database) & 
							(THEINFOS[ii].POSINMASTER <= THEINFOS[ii - 1].POSINMASTER)) { // this one before previous
					if (THEINFOS[ii].SCORE > THEINFOS[ii - 1].SCORE) { // which one is best?
						THEINFOS[ii - 1].outOfOrder = true;
					} else {
						THEINFOS[ii].outOfOrder = true;
					}
					inorder = false;
					reordered = true;
				}
			}
		} while (!inorder);
		if (reordered) {
			RetroTectorEngine.displayError(new RetroTectorException("MotifHitsInfo", "Hit out of order disregarded in", scriptName), RetroTectorEngine.WARNINGLEVEL);
		}

		float sum = 0.0f;
		float ssum = 0.0f;
		int n = 0;
		for (int i=0; i<THEINFOS.length; i++) {
			if ((THEINFOS[i].POSINMASTER >= 0) & THEINFOS[i].usefulToORFID(database)) {
				sum += THEINFOS[i].SCORE;
				ssum += THEINFOS[i].SCORE * THEINFOS[i].SCORE;
				n++;
			}
		}
		if (n == 0) {
			AVERSCORE = 0;
		} else {
			AVERSCORE = sum / n;
		}
		SSSCORES = ssum;
	} // end of constructor(String[ ], DNA, Alignment)
	
/**
* @return	String array description, acceptable by constructor.
*/
	public final String[ ] toStrings() {
		String[ ] outs = new String[THEINFOS.length];
		for (int i=0; i<THEINFOS.length; i++) {
			outs[i] = THEINFOS[i].toString();
		}
		return outs;
	} // end of toStrings()
	
/**
* @return	String array description including hit indices.
*/
	public final String[ ] toStringsIndexed() {
		String[ ] outs = new String[THEINFOS.length];
		for (int i=0; i<THEINFOS.length; i++) {
			outs[i] = THEINFOS[i].toStringIndexed();
		}
		return outs;
	} // end of toStringsIndexed()
	
/**
* @return	Number of individual HitInfos.
*/
	public final int nrOfHitInfos() {
		return THEINFOS.length;
	} // end of nrOfHitInfos()
	
/**
* @param	index	Number of one MotifHitInfo.
* @return	That MotifHitInfo, or null.
*/
	public final MotifHitInfo getHitInfo(int index) {
		if ((index < 0) || (index >= THEINFOS.length)) {
			return null;
		}
		return THEINFOS[index];
	} // end of getHitInfo(int)
	
/**
* For debugging.
* @return	True if correct order.
*/
	public final boolean inOrder(Database database) {
		int ind = Integer.MIN_VALUE;
		for (int i=0; i<THEINFOS.length; i++) {
			if ((THEINFOS[i].POSINMASTER >= 0) && THEINFOS[i].usefulToORFID(database)) {
				if (THEINFOS[i].POSINMASTER <= ind) {
					return false;
				} else {
					ind = THEINFOS[i].POSINMASTER;
				}
			}
		}
		return true;
	} // end of inOrder(Database)

/**
*	@param	database	The relevant Database.
*	@return	True if usefulToORFID true for any MotifHitInfo.
*/
	public final boolean usefulToORFID(Database database) {
		for (int i=0; i<THEINFOS.length; i++) {
			if (THEINFOS[i].usefulToORFID(database)) {
				return true;
			}
		}
		return false;
	} // end of usefulToORFID(Database)
	
} // end of MotifHitsInfo
