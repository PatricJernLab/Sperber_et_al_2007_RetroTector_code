/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/10 -06
* Beautified 12/10 -06
*/
package retrotector;

import builtins.*;
import java.util.*;
import java.io.*;

/**
* Superclass of Putein and Xon.
*/
public abstract class AcidSequence implements Scorable {

/**
* Index of line with Motif hit markers = 0.
*/
	public static final int MOTIFLINEINDEX = 0;

/**
* Index of line with putein = 1.
*/
	public static final int PUTEINLINEINDEX = 1;

/**
* Index of line with frame numbers = 2.
*/
	public static final int FRAMELINEINDEX = 2;

/**
* Index of fourth line = 3.
*/
	public static final int FOURTHLINEINDEX = 3;

/**
* Index of line with position markers = 4.
*/
	public static final int POSLINEINDEX = 4;

/**
*	Parameter package.
*/
	public final ORFID.ParameterBlock PBLOCK;
	
/**
* Path of input file.
*/
	public String inFilePath = "";

/**
* For Chainview to store a Color.
*/
	public Object color;
	
/**
* Internal position in DNA thought to be first in "real" putein".
*/
	public int estimatedFirst;
	
/**
* Internal position in DNA thought to be last in "real" putein".
*/
	public int estimatedLast;
	
/**
* Overall number of acids.
*/
	int totalLength;

/**
* Number of acids in "real" putein.
*/
	int insideLength;

/**
* Number of acids in "real" putein obtained through alignment.
*/
	int alignedacidsinside;

/**
* Overall sum of scores of acids.
*/
	float scoresum;

/**
* Sum of scores of acids in "real" putein.
*/
	float scoresuminside;

/**
* Overall number of stop codons.
*/
	int stopcount;
	
/**
* Number of stop codons in "real" putein.
*/
	public int stopcountinside;

/**
* Overall number of ambiguous acids.
*/
	int ambigcount;

/**
* Number of ambiguous acids in "real" putein.
*/
	int ambigcountinside;
	
/**
* Overall number of frame shifts.
*/
	int shiftcount;

/**
* Number of frame shifts in "real" putein.
*/
	public int shiftcountinside;

/**
* Number of acids in longest run of non-shifts in "real" putein.
*/
	int bestRunLength;

/**
* Start position of longest run of non-shifts in "real" putein.
*/
	int bestRunPos;
	
/**
* The central contents.
*/
	protected PathElement[ ] path;

// temporary path contents
	protected Stack pathStack = new Stack();
	
/**
* Contents of "Putein" or equivalent parameter in output file.
*/
	public String[ ] mainLines = new String[5];
	
/**
* For input file contents.
*/
	protected Hashtable ht;

/**
* For DNA sequence.
*/
	protected StringBuffer codonBuffer = new StringBuffer(1000);

/**
* Constructor. More actions will be required.
* @param	pBlock	ORFID.ParameterBlock with parameters.
*/
	public AcidSequence(ORFID.ParameterBlock pBlock) {
		PBLOCK = pBlock;
	} // end of constructor()

/**
* Constructor.
* @param	inFile	A File with contents according to ParameterStream.
* @param	pBlock	ORFID.ParameterBlock with parameters.
*/
	public AcidSequence(File inFile, ORFID.ParameterBlock pBlock) throws RetroTectorException {
		if (inFile.length() <= 0) {
			throw new RetroTectorException("AcidSequence", "Empty file: " + inFile.getPath());
		}
		PBLOCK = pBlock;
		inFilePath = inFile.getPath();
		ht = new Hashtable();
		ParameterFileReader re = new ParameterFileReader(inFile, ht);
		re.readParameters();
		re.close();
		String[ ] inLines = (String[ ]) ht.get(Putein.PUTEINKEY);
		if (inLines == null) {
			inLines = (String[ ]) ht.get(Xon.XONKEY);
		}
		int startPos = Integer.parseInt((String) ht.get(Putein.ESTIMATEDSTARTPOSITIONKEY));
		startPos = PBLOCK.TARGETDNA.internalize(startPos);
		estimatedFirst = startPos;
		pathStack = new Stack();
		while (shorten(inLines) != '>') { // read off leader
		}
		
		int frame;
		int pos = startPos; // continualy update FROMDNA
		int p;
		boolean shifthere;
		while (inLines[FOURTHLINEINDEX].charAt(0) != '<') { // read one char at a time up to trailer
			shifthere = false;
			p = pos;
			try {
				frame = inLines[FRAMELINEINDEX].charAt(0) - '0'; // get frame marker
			} catch (StringIndexOutOfBoundsException se) {
				throw se;
			}
			while (PBLOCK.TARGETDNA.frameOf(p) != frame) {
				shifthere = true;
				p++; // step up to proper frame in DNA
			}
			if (p > pos) { // make 1 or 2 base element
				pushElement(new PathElement(pos, p, 0, 0, 0, 0));
				pos = p;
			}

// unusual case A: insertion of more than 2 bases			
			while (PBLOCK.TARGETDNA.getAcid(pos) != Character.toLowerCase(inLines[PUTEINLINEINDEX].charAt(0))) {
				pushElement(new PathElement(pos, pos + 1, 0, 0, 0, 0));
				pushElement(new PathElement(pos + 1, pos + 2, 0, 0, 0, 0));
				pushElement(new PathElement(pos + 2, pos + 3, 0, 0, 0, 0));
				pos += 3;
			}
			
			if ((addressOn == 0) & (inLines[POSLINEINDEX].charAt(0) != ' ')) { // address marker starting
				int ind = inLines[POSLINEINDEX].indexOf(' ');
// even more unusual case, not caught by A because of repeating acid
				if (ind > 0) {
					String val = "" + Integer.parseInt(inLines[POSLINEINDEX].substring(0, ind));
					String val2 = String.valueOf(PBLOCK.TARGETDNA.externalize(pos));
					int scount = 0;
					while (!val2.endsWith(val)) {
						pushElement(new PathElement(pos, pos + 1, 0, 0, 0, 0));
						pos++;
						val2 = String.valueOf(PBLOCK.TARGETDNA.externalize(pos));
						scount++;
						if (scount > 10 ) {
							RetroTectorException.sendError(this, "Putein address mismatch at " + val2, val, "in " + inFilePath);
						}
					}
				}
			}
			pushElement(new PathElement(pos, pos + 3, 0, 0, 0, 0));
			estimatedLast = pos + 2;
			insideLength++;
			if (PBLOCK.TARGETDNA.getAcid(pos) == 'z') {
				stopcountinside++;
			}
			if (shifthere) {
				shiftcountinside++;
			}
			pos += 3;
			shorten(inLines);
		}
		
		path = new PathElement[pathStack.size()];
		pathStack.copyInto(path);
		pathStack = null;
	} // end of constructor(File, ORFID.ParameterBlock)

	private int addressOn = 0; // >0 if just removed char in address line not  blank
	
// removes first char of all strings, setting addressOn and returning char removed from marker line
	private final char shorten(String[ ] ss) {
		char c = ss[FOURTHLINEINDEX].charAt(0);
		if (ss[POSLINEINDEX].charAt(0) == ' ') {
			addressOn = 0;
		} else {
			addressOn++;
		}
		for (int i=0; i<ss.length; i++) {
			ss[i] = ss[i].substring(1);
		}
		return c;
	} // end of shorten(String[ ])

/**
* @return	Integer pairs with position in DNA and frame for beginning and end of each frame run.
*/
	public final int[ ][ ] drawCoordinates() {
		Stack st = new Stack();
		int[ ] i2;
		for (int i=0; i<path.length; i++) {
			if (path[i].TODNA == path[i].FROMDNA + 3) {
				i2 = new int[2];
				i2[0] = path[i].FROMDNA;
				i2[1] = PBLOCK.TARGETDNA.frameOf(i2[0]);
				st.push(i2);
			}
		}
		
		int p0;
		int p1;
		int p2;
		for (int j=st.size()-2; j>0; j--) {
			p0 = ((int[ ]) st.elementAt(j - 1))[1];
			p1 = ((int[ ]) st.elementAt(j))[1];
			p2 = ((int[ ]) st.elementAt(j + 1))[1];
			if ((p0 == p1) & (p2 == p1)) {
				st.remove(j);
			}
		}
		int[ ][ ] result = new int[st.size()][ ];
		st.copyInto(result);
		return result;
	} // end of drawCoordinates()
	
	
/**
* Tries to add one element.
* @param	pe	The PathElement to add.
* @return true if pe was added.
*/
	public final boolean pushElement(PathElement pe) throws RetroTectorException {
		if (pathStack.empty()) {
			if (pe.FROMMASTER != 0) {
				RetroTectorException.sendError(this, "Path does not start at 0 in Alignment");
			}
		} else {
			PathElement p = (PathElement) pathStack.peek();
			if ((pe.FROMDNA != p.TODNA) | (pe.FROMMASTER != p.TOMASTER)) {
				RetroTectorException.sendError(this, "Path element mismatch", " " + p.TODNA + ":" + pe.FROMDNA, " " + p.TOMASTER + ":" + pe.FROMMASTER);
			}
		}
		
		pathStack.push(pe);
		return true;
	} // end of pushElement(PathElement)
	
/**
* @return	Array of positions in DNA of all the acids.
*/
	public final int[ ] getAcidPositions() {
		Stack st = new Stack();
		for (int i=0; i<path.length; i++) {
			if (path[i].TODNA == path[i].FROMDNA + 3) {
				st.push(new Integer(path[i].FROMDNA));
			}
		}
		int[ ] result = new int[st.size()];
		for (int r=0; r<result.length; r++) {
			result[r] = ((Integer) st.elementAt(r)).intValue();
		}
		return result;
	} // end of getAcidPositions()
	
/**
* Makes AcidSequence ready for use.
* @param	estimatedfirst	Position where "real" putein is suggested to start (or less).
* @param	estimatedlast		Position where "real" putein is suggested to end (or more).
*/
	public final void finishSequence(int estimatedfirst, int estimatedlast) throws RetroTectorException {
		PathElement pe;
// avoid going into leading padding
		if ((PBLOCK.TARGETDNA.FIRSTUNPADDED >= 0) & (PBLOCK.TARGETDNA.FIRSTUNPADDED > estimatedfirst)) {
			int p = 0;
			pe = (PathElement) pathStack.elementAt(p);
			while ((pe.FROMDNA < PBLOCK.TARGETDNA.FIRSTUNPADDED) & (pe.TODNA < estimatedlast)) {
				p++;
				try {
					pe = (PathElement) pathStack.elementAt(p);
				} catch (ArrayIndexOutOfBoundsException ae) {
					throw ae;
				}
			}
			estimatedFirst = pe.FROMDNA;
		} else {
			estimatedFirst = estimatedfirst;
		}
// avoid going into trailing padding
		if ((PBLOCK.TARGETDNA.LASTUNPADDED >= 0) & (PBLOCK.TARGETDNA.LASTUNPADDED < estimatedlast)) {
			int p = pathStack.size() - 1;
			pe = (PathElement) pathStack.elementAt(p);
			while ((pe.TODNA > PBLOCK.TARGETDNA.LASTUNPADDED) & (pe.FROMDNA > estimatedfirst)) {
				p--;
				pe = (PathElement) pathStack.elementAt(p);
			}
			estimatedLast = pe.TODNA;
		} else {
			estimatedLast = estimatedlast;
		}
		pe = (PathElement) pathStack.peek();
		while (pe.TODNA != pe. FROMDNA + 3) { // not acid, discard at end
			pathStack.pop();
			pe = (PathElement) pathStack.peek();
		}
		path = new PathElement[pathStack.size()];
		pathStack.copyInto(path);
		pathStack = null;
		makeMainParameter();
	} // end of finishSequence(int, int)
	
// makes mainLines and a lot of parameters
	private final void makeMainParameter() throws RetroTectorException {
		if (PBLOCK.MASTER != null) {
			PBLOCK.MASTER.reset();
		}
		PBLOCK.TARGETDNA.setMark(path[0].FROMDNA);

		try {
			PBLOCK.DATABASE.KOZAKMOTIF.refresh(new Motif.RefreshInfo(PBLOCK.TARGETDNA, 3.0f, 3f, null));
		} catch (NullPointerException npe) {
			throw npe;
		}
		
		StringBuffer motifStringBuffer = new StringBuffer(1000); // For Motif hit info
		StringBuffer puteinStringBuffer = new StringBuffer(1000); // For amino acids in putein
		StringBuffer frameStringBuffer = new StringBuffer(1000); // For reading frame
    StringBuffer fourthLineBuffer = new StringBuffer(1000); // for fourth line
    StringBuffer positionsBuffer = new StringBuffer(1000); // for position line

    StringBuffer kozakBuffer = new StringBuffer(1000); // for Kozak motif hits
		
		totalLength = 0;
		insideLength = 0;
		alignedacidsinside = 0;
		scoresum = 0;
		scoresuminside = 0;
		stopcount = 0;
		stopcountinside = 0;
		ambigcount = 0;
		ambigcountinside = 0;
		shiftcount = 0;
		shiftcountinside = 0;
		bestRunLength = -1;
		bestRunPos = -1;

		boolean inside = false;
		char currFrame = '0'; // not defined yet
		char currFrameInside = '0';
		MotifHitInfo inf;
		char d;
		char cn;
		int runLength = 0;
		int runPos = -1;
		
// start all lines with blank resrve space
		motifStringBuffer.append(" ");
		puteinStringBuffer.append(" ");
		frameStringBuffer.append(" ");
		fourthLineBuffer.append(" ");
		positionsBuffer.append(" ");
		kozakBuffer.append(" ");
		
		try {
			for (int i=0; i<path.length; i++) {
				if (path[i].FROMDNA != PBLOCK.TARGETDNA.getMark()) { // incoherent path
					RetroTectorException.sendError(this, "DNA mark mismatch", " " + path[i].FROMDNA + ":" + PBLOCK.TARGETDNA.getMark());
				}
				if ((PBLOCK.MASTER != null) && (path[i].FROMMASTER != PBLOCK.MASTER.getAcidMark())) { // incoherent path
					RetroTectorException.sendError(this, "Alignment mark mismatch", " " + path[i].FROMMASTER + ":" + PBLOCK.MASTER.getAcidMark());
				}
				
// change from leader to real putein?
				if (!inside && (path[i].FROMDNA >= estimatedFirst) && (path[i].FROMDNA < estimatedLast)) {
					inside = true;
					runPos = path[i].FROMDNA;
					fourthLineBuffer.deleteCharAt(fourthLineBuffer.length() - 1);
					fourthLineBuffer.append(">");
				}
// change from real putein to trailer?
				if (inside && (path[i].FROMDNA > estimatedLast)) {
					inside = false;
					fourthLineBuffer.append("<");
				}
				
				if (path[i].TODNA == (path[i].FROMDNA + 3)) { // acid?
// make cn frame number and check for shift
					cn = '1';
					if (PBLOCK.TARGETDNA.frameOf(PBLOCK.TARGETDNA.getMark()) == 2) {
						cn = '2';
					}
					if (PBLOCK.TARGETDNA.frameOf(PBLOCK.TARGETDNA.getMark()) == 3) {
						cn = '3';
					}
					if ((currFrame != '0') && (currFrame != cn)) {
						shiftcount++;
					}
					currFrame = cn;

// check for shift in "real" putein
					if (inside) {
						if ((currFrameInside != '0') && (currFrameInside != cn)) { // shift
							shiftcountinside++;
							if (runLength > bestRunLength) {
								bestRunLength = runLength;
								bestRunPos = runPos;
							}
							runLength = 0;
							runPos = path[i].FROMDNA;
						} else {
							runLength++;
						}
						currFrameInside = cn;
					}
					frameStringBuffer.append(cn);
					
// stop codon?
					d = PBLOCK.TARGETDNA.getAcid(PBLOCK.TARGETDNA.getMark());
					if (d == Compactor.STOPCHAR) {
						stopcount++;
						if (inside) {
							stopcountinside++;
						}
					}
					
// ambiguous acid?
					if (d == 'x') {
						ambigcount++;
						if (inside) {
							ambigcountinside++;
						}
					}
					
					if (path[i].TOMASTER == (path[i].FROMMASTER + 1)) {
						if (PBLOCK.MASTER != null) { // keep master up to date
							PBLOCK.MASTER.suggestCurrentRow(path[i].ROWCODE);
							PBLOCK.MASTER.getCurrentRow().hitcount++; // keep count of used AlignmentRow
							if (d == PBLOCK.MASTER.currentAcid()) {
								d = Character.toUpperCase(d);
							}
							PBLOCK.MASTER.advanceMasterAcid();
						}
						if (inside) {
							alignedacidsinside++;
						}
					}
					puteinStringBuffer.append(d);
					if (inside) {
						int m = PBLOCK.TARGETDNA.getMark();
						codonBuffer.append(PBLOCK.TARGETDNA.getBase(m++));
						codonBuffer.append(PBLOCK.TARGETDNA.getBase(m++));
						codonBuffer.append(PBLOCK.TARGETDNA.getBase(m++));
					}
					if (PBLOCK.DATABASE.KOZAKMOTIF.getMotifHitAt(path[i].FROMDNA - 6) != null) {
						kozakBuffer.append("!");
					}
					
					if (totalLength % 30 == 0) {
						positionsBuffer.append(PBLOCK.TARGETDNA.externalize(path[i].FROMDNA));
					}
					
					PBLOCK.TARGETDNA.advanceCodon(' ');
					totalLength++;
					scoresum += path[i].SCORE;
					if (inside) {
						insideLength++;
						scoresuminside += path[i].SCORE;
					}

					char c = ' ';
					if (PBLOCK.HITSINFO != null) {
						for (int oh=0; ((inf = PBLOCK.HITSINFO.getHitInfo(oh)) != null) & (c == ' '); oh++) {
							c = inf.charAt(path[i].FROMDNA);
						}
					}
					motifStringBuffer.append(c);
				} else if ((path[i].TOMASTER == (path[i].FROMMASTER + 1)) & (path[i].TODNA == path[i].FROMDNA)) {
					if (PBLOCK.MASTER != null) {
						PBLOCK.MASTER.advanceMasterAcid();
					}
				} else if ((path[i].TOMASTER == path[i].FROMMASTER) & (path[i].TODNA == (path[i].FROMDNA + 2))) {
					PBLOCK.TARGETDNA.advanceBase();
					PBLOCK.TARGETDNA.advanceBase();
				} else if ((path[i].TOMASTER == path[i].FROMMASTER) & (path[i].TODNA == (path[i].FROMDNA + 1))) {
					PBLOCK.TARGETDNA.advanceBase();
				} else {
					RetroTectorException.sendError(this, "Erroneous path element");
				}
				
				while (motifStringBuffer.length() < puteinStringBuffer.length()) {
					motifStringBuffer.append(" ");
				}
				while (fourthLineBuffer.length() < puteinStringBuffer.length()) {
					fourthLineBuffer.append(" ");
				}
				while (positionsBuffer.length() < puteinStringBuffer.length()) {
					positionsBuffer.append(" ");
				}
				while (kozakBuffer.length() < puteinStringBuffer.length()) {
					kozakBuffer.append(" ");
				}
			}
		} catch (FinishedException fe) {
			RetroTectorException.sendError(this, "End of alignment or DNA reached");
		}
		
// end reached
		puteinStringBuffer.append(" ");
		if (inside) {
			fourthLineBuffer.append("<");
		}
		while (motifStringBuffer.length() < puteinStringBuffer.length()) {
			motifStringBuffer.append(" ");
		}
		while (frameStringBuffer.length() < puteinStringBuffer.length()) {
			frameStringBuffer.append(" ");
		}
		while (fourthLineBuffer.length() < puteinStringBuffer.length()) {
			fourthLineBuffer.append(" ");
		}
		while (positionsBuffer.length() < puteinStringBuffer.length()) {
			positionsBuffer.append(" ");
		}
		while (kozakBuffer.length() < puteinStringBuffer.length()) {
			kozakBuffer.append(" ");
		}
		Utilities.updateStringBuffer(fourthLineBuffer, kozakBuffer.toString()); // add Kozak markers
		
		if (runLength > bestRunLength) {
			bestRunLength = runLength;
			bestRunPos = runPos;
		}

		mainLines[MOTIFLINEINDEX] = motifStringBuffer.toString();
		mainLines[PUTEINLINEINDEX] = puteinStringBuffer.toString();
		mainLines[FRAMELINEINDEX] = frameStringBuffer.toString();
		mainLines[FOURTHLINEINDEX] = fourthLineBuffer.toString();
		mainLines[POSLINEINDEX] = positionsBuffer.toString();
	} // end of makeMainParameter()
	
/**
* Not in use at present.
*/
	protected void checkParameter(int parameter, String key) throws RetroTectorException {
		String s = (String) ht.get(key);
		if (s == null) {
			return;
		}
		if (!s.equals(String.valueOf(parameter))) {
			RetroTectorException.sendError(this, "Parameter mismatch in", inFilePath, key, s, "" + parameter);
		}
	} // end of checkParameter(int, String);
	
/**
* Not in use at present.
*/
	protected void checkParameter(String parameter, String key) throws RetroTectorException {
		String s = (String) ht.get(key);
		if (s == null) {
			return;
		}
		if (!s.equals(parameter)) {
			RetroTectorException.sendError(this, "Parameter mismatch in ", inFilePath, key, s, parameter);
		}
	} // end of checkParameter(int, String);
	
}
