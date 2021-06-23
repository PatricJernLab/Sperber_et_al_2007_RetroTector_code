/*
 * Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
 */

/**
 *   For RetroTector 1.0
 *
 * @author  G. Sperber
 * @version 5/11 -06
 */
package builtins;

import retrotector.*;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
* Makes a graphic display of an output file from ORFID.
*<PRE>
*     Parameters:
*   PuteinFile
* The name of the Putein file to display.
*
*   FontSize
* The size of the font to use.
* Default: 8
*</PRE>
*/
public class ORFIDview extends Executor {

/**
* Specification of where a character or number should be drawn.
* For use of ORFIDview.
*/
	class EInf {
		
		final int X;
		final int Y;
		final char C;
		final long N;
		
/**
* Constructor for a character.
* @param	xx	Horizontal position.
* @param	yy	Vertical position.
* @param	cc	The character.
*/
		EInf(int xx, int yy, char cc) {
			X = xx;
			Y = yy;
			C = cc;
			N = 0;
		} // end of constructor(int, int, char)
		
/**
* Constructor for a longint.
* @param	xx	Horizontal position.
* @param	yy	Vertical position.
* @param	nn	The longint.
*/
		EInf(int xx, int yy, long nn) {
			X = xx;
			Y = yy;
			N = nn;
			C = (char) 0;
		} // end of constructor(int, int, long)
		
	} // end of EInf
	

/**
* Canvas to show sequence names in parallell with main Canvas.
*/
	class NCanv extends JCanvas {

		private ORFIDview parent;
		private int fontSize;
		
		NCanv(ORFIDview par) {
			parent = par;
			fontSize = parent.fontSize;
		} // end of constructor
		
		public Dimension getPreferredSize() {
			return new Dimension(100, (parent.actualMasters + 1) * 2 * fontSize);
		} // end of getPreferredSize
		
		public Dimension getMinimumSize() {
			return getPreferredSize();
		} // end of getMinimumSize
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Font bigFont = new Font("Courier", Font.PLAIN, fontSize * 2);
			g.setFont(bigFont);
			
			for (int n=0; n<parent.actualMasters; n++) {
				g.drawString(parent.names[n], 5, 20 + fontSize * n *2);
			}
		} // end of paint
		
	} // end of NCanv


/**
* Canvas to show contents of ORFID output file.
*/
	class VCanv extends JCanvas {

		final ORFIDview PARENT;
		final int WIDTH;
		final int HEIGHT;
		final Font BIGFONT;
		final Font SMALLFONT;
		
	/**
	* Constructor.
	* @param	par	The ORFIDview using this.
	*/
		VCanv(ORFIDview par) {
			PARENT = par;
			WIDTH = PARENT.horPos + 50;
			HEIGHT = PARENT.fontSize * (16 + 2*PARENT.actualMasters);
			BIGFONT = new Font("Courier", Font.PLAIN, PARENT.fontSize * 2);
			SMALLFONT = new Font("Courier", Font.PLAIN, PARENT.fontSize);
		} // end of constructor
		
		public Dimension getPreferredSize() {
			return new Dimension(WIDTH, HEIGHT);
		} // end of getPreferredSize
		
		public Dimension getMinimumSize() {
			return new Dimension(WIDTH, 320);
		} // end of getMinimumSize
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			char[ ] ca = new char[1];
			EInf ei;
			int i;
			
			g.setFont(BIGFONT);
			for (i=0; i<PARENT.warners.length; i++) {
				ei = PARENT.warners[i];
				ca[0] = ei.C;
				g.drawChars(ca, 0, 1, ei.X, ei.Y);
			}
			
			for (int m=0; m<PARENT.masters.length; m++) {
				if (PARENT.masters[m] != null) {
					for (i=0; i<PARENT.masters[m].length; i++) {
						ei = PARENT.masters[m][i];
						ca[0] = ei.C;
						g.drawChars(ca, 0, 1, ei.X, ei.Y);
					}
				}
			}
			
			for (i=0; i<PARENT.putein.length; i++) {
				ei = PARENT.putein[i];
				ca[0] = ei.C;
				g.drawChars(ca, 0, 1, ei.X, ei.Y);
			}
			
			g.setFont(SMALLFONT);
			for (i=0; i<PARENT.masterPositions.length; i++) {
				ei = PARENT.masterPositions[i];
				g.drawString("" + ei.N, ei.X, ei.Y);
			}

			for (i=0; i<PARENT.masterNrs.length; i++) {
				ei = PARENT.masterNrs[i];
				g.drawString("" + ei.N, ei.X, ei.Y);
			}
			
			for (int b=0; b<PARENT.bases.length; b++) {
				if (PARENT.bases[b] != null) {
					for (i=0; i<PARENT.bases[b].length; i++) {
						ei = PARENT.bases[b][i];
						ca[0] = ei.C;
						g.drawChars(ca, 0, 1, ei.X, ei.Y);
					}
				}
			}
			
			for (i=0; i<PARENT.acids.length; i++) {
				ei = PARENT.acids[i];
				ca[0] = ei.C;
				g.drawChars(ca, 0, 1, ei.X, ei.Y);
			}
			
			g.setColor(Color.red);
			for (i=0; i<PARENT.dnaPositions.length; i++) {
				ei = PARENT.dnaPositions[i];
				g.drawString("" + ei.N, ei.X, ei.Y);
			}
		} // end of paint
		
	} // end of VCanv

	
/**
* Horizontal space for one small character.
*/
	int horStep;
	
/**
* Font size.
*/
	int fontSize = 8;
	
/**
* Number of rows in master Alignment.
*/
	int actualMasters;
	
/**
* Asterisks or dashes marking non-smooth part.
*/
	EInf[ ] warners;
	
/**
* Acid positions in master Alignment.
*/
	EInf[ ] masterPositions;
	
/**
* Master alignment sequences.
*/
	EInf[ ][ ] masters;
	
/**
* DNA bases in their reading frames.
*/
	EInf[ ][ ] bases = new EInf[3][ ];
	
/**
* Codons interpreted as acids.
*/
	EInf[ ] acids;

/**
* The resulting putein.
*/
	EInf[ ] putein;
	
/**
* Number of current sequence in alignment.
*/
	EInf[ ] masterNrs;
	
/**
* Base numbers in DNA (external, but that does not matter).
*/
	EInf[ ] dnaPositions;

/**
* Names of sequences in master Alignment.
*/
	String[ ] names = new String[Alignment.MAXMASTERS];

/**
* Current horizontal drawing position.
*/
	int horPos;
	
	private CloseableJFrame bigFrame = new CloseableJFrame(""); // main display window
	private JDialog smallFrame = new JDialog(bigFrame); // sequence names display window
	
/**
* Constructor.
*/
	public ORFIDview() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(INPUTFILEKEY, "");
		explanations.put(INPUTFILEKEY, "The ORFID output file to use");
		orderedkeys.push(INPUTFILEKEY);
		parameters.put(FONTSIZEKEY, "8");
		explanations.put(FONTSIZEKEY, "The minor font size to use");
		orderedkeys.push(FONTSIZEKEY);
	} // end of constructor
	
// Converts a stack into an array
	private EInf[ ] toAr(Stack s) {
		EInf[ ] ei = new EInf[s.size()];
		s.copyInto(ei);
		return ei;
	} // end of toAr

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 05";
  } //end of version
  
  
/**
* As required by Executor.
*/
	public String execute() throws RetroTectorException
 {
	
// get input file if any
		String s1 = getString(INPUTFILEKEY, "");
		if (s1.length() > 0) {
			File f = Utilities.getReadFile(RetroTectorEngine.currentDirectory(), s1);
			ParameterFileReader reader = new ParameterFileReader(f, parameters);
			reader.readParameters();
			reader.close();
		}
    int hitsfound = getInt(ORFID.NROFHITSKEY, 0);
    if (hitsfound <= 0) {
      RetroTectorEngine.displayError(new RetroTectorException("ORFIDview", "No hits were found"), RetroTectorEngine.NOTICELEVEL);
      return "No hits";
    }
		fontSize = getInt(FONTSIZEKEY, 8);
		horStep = (fontSize * 2) / 3;
		
		
// read off sequence names
		names = getStringArray(MASTERNAMESKEY);
		actualMasters = names.length;

// stacks to take input
		Stack warnersS = new Stack();
		Stack masterPositionsS = new Stack();
		masters = new EInf[actualMasters][ ];
		Stack[ ] mastersS = new Stack[actualMasters];
		Stack[ ] basesS = new Stack[3];
		Stack acidsS = new Stack();
		Stack puteinS = new Stack();
		Stack masterNrsS = new Stack();
		Stack dnaPositionsS = new Stack();
		for (int s = 0; s<actualMasters; s++) {
			mastersS[s] = new Stack();
		}
		for (int ss = 0; ss<3; ss++) {
			basesS[ss] = new Stack();
		}

		String tempS;
		long tempL;
		horPos = 10;
		int extraStep = 0;
		String[ ] contents = getStringArray("ORFIDoutput");
		String li;
// read lines one by one
		for (int l=0; l<contents.length; l++) { // EOF?
			li = contents[l];
			if (!li.startsWith("{")) {
				if (li.length() > (40 + 2 * actualMasters)) {
					li = li.substring(0, 40 + 2 * actualMasters);
				}
				if (li.length() < (35 + 2 * actualMasters)) {
					StringBuffer sb = new StringBuffer(li); // fill up to max length
					while (sb.length() < (35 + 2 * actualMasters)) {
						sb.append(' ');
					}
					li = new String(sb);
				}
				if (li.substring(1, 6).equals("Start")) { // trigger line
	//				bigFrame.setTitle(fileName + ": " + li.trim());
//					l++;
				} else {
					tempS = li.substring(23 + 2 * actualMasters).trim();
					if (tempS.length() > 0) { // base pos. in DNA
						tempL = Long.parseLong(tempS);
						dnaPositionsS.push(new EInf(horPos, fontSize * 2, tempL));
					}
					tempS = li.substring((21 + 2 * actualMasters), (23 + 2 * actualMasters)).trim();
					if (tempS.length() > 0) { // current sequence number
						tempL = Long.parseLong(tempS);
						masterNrsS.push(new EInf(horPos, fontSize * 4, tempL));
					}
					if (li.charAt(19 + 2 * actualMasters) != ' ') { // acid in putein
						puteinS.push(new EInf(horPos, fontSize * 6, li.charAt(19 + 2 * actualMasters)));
					}
					if (li.charAt(17 + 2 * actualMasters) != ' ') { // codon interpretation
						acidsS.push(new EInf(horPos, fontSize * 8, li.charAt(17 + 2 * actualMasters)));
					}
					for (int b=0; b<3; b++) { // dna base
						if (li.charAt(9 + 2 * actualMasters + 2 * b) != ' ') {
							basesS[b].push(new EInf(horPos, fontSize * (11 - b), li.charAt(9 + 2 * actualMasters + 2 * b)));
						}
					}
					for (int m=0; m<actualMasters; m++) {
						if (li.charAt(9 + 2 * m) != ' ') { // acid in alignment sequence
							mastersS[m].push(new EInf(horPos, fontSize * (13 + 2*m), li.charAt(9 + 2 * m)));
						}
					}
					tempS = li.substring(1, 9).trim();
					if (tempS.length() > 0) { // acid pos in alignment
						tempL = Long.parseLong(tempS);
						masterPositionsS.push(new EInf(horPos, fontSize * (12 + 2*actualMasters), tempL));
					}
					if (li.charAt(0) != ' ') { // asterisk
						warnersS.push(new EInf(horPos, fontSize * (14 + 2*actualMasters), li.charAt(0)));
						extraStep = horStep / 2; // make room
					}
				}
				horPos += horStep;
				horPos += extraStep;
				extraStep = 0;
			}
		}
		
// make arrays of stacks
		warners = toAr(warnersS);
		masterPositions = toAr(masterPositionsS);
		for (int mm=0; mm<actualMasters; mm++) {
			masters[mm] = toAr(mastersS[mm]);
			mastersS[mm] = null;
		}
		for (int bb=0; bb<3; bb++) {
			bases[bb] = toAr(basesS[bb]);
			basesS[bb] = null;
		}

		acids = toAr(acidsS);
		putein = toAr(puteinS);
		masterNrs = toAr(masterNrsS);
		dnaPositions = toAr(dnaPositionsS);
// throw stacks to garbage collector
		warnersS = null;
		masterPositionsS = null;
		acidsS = null;
		puteinS = null;
		masterNrsS = null;
		dnaPositionsS = null;
		
// set up windows to use data here
		VCanv ovc = new VCanv(this);
		bigFrame.getContentPane().add(new JScrollPane(ovc), BorderLayout.CENTER);
		bigFrame.setLocation(150, 50);
		AWTUtilities.showFrame(bigFrame, new Dimension(600, 350));

		NCanv nc = new NCanv(this);
		smallFrame.getContentPane().add(nc, BorderLayout.CENTER);
		AWTUtilities.showFrame(smallFrame, null);

		return "";
	} // end of execute

}