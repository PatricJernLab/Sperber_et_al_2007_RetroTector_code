/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/11 -06
* Beautified 22/11 -06
*/
package builtins;

import retrotector.*;
import retrotectorcore.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.image.*;

/**
* Executor to display Chains graphically, one at a time.
* <PRE>
*     Parameters:
*
*   InputFile
* The name of the file to display.
* Default: ""
*</PRE>
*/
public class Chainview extends Executor {

/**
* Window for the presentation of Chains. It has a menu bar with
* menus for up to 20 Chain numbers each, a text part with a description
* of the Chain, and a scrollable ChainCanvas for graphic display
* of MotifHits in the Chain, puteins, XXons and Xons.
* To the right there are check boxes to control the graphic display.
* A button for saving ChainCanvas as a file at the bottom.
*/
	class ChainWindow extends CloseableJFrame implements ActionListener, MouseListener, ChangeListener {

// info related to part of a Putein, XXon or Xon in a reading frame
		private class SequencePart {
		
			private final Rectangle R; 	// rectangle in ChainCanvas to react to cursor
			private final String ID; // descriptive string
			private final AcidSequence PUTEIN; // containing AcidSequence
			
			private SequencePart(Rectangle r, String id, AcidSequence putein) {
				R = r;
				ID = id;
				PUTEIN = putein;
			} // end of Chainview.ChainWindow.SequencePart.constructor(Rectangle, String, AcidSequence)
			
			private final String react(Point p) {
				if (R.contains(p)) {
					return ID;
				} else {
					return null;
				}
			} // end of Chainview.ChainWindow.SequencePart.react(Point)
			
		} // end of Chainview.ChainWindow.SequencePart
	
	
	// any entity to be shown in ChainCanvas if selected in parameter window
		private abstract class MarkSubject implements Icon {
		
/**
* To be shown in parameter window together with this as Icon.
*/
			JCheckBox checkBox;
			
/**
* First, last and hotspot internal positions in DNA for parts.
*/
			int[ ][ ] parts;
			
/**
* As required by Icon.
*/
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Rectangle r = new Rectangle(x, y, getIconWidth(), getIconHeight());
				drawSubjectPart(g, r);
			} // end of Chainview.ChainWindow.MarkSubject.paintIcon(Component, Graphics, int, int)
	
/**
* Draws one part.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			abstract void drawSubjectPart(Graphics g, Rectangle bounds);
			
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			abstract void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException ;
			
/**
* Draws all parts.
* @param	g	Graphics to draw in.
*/
			void drawAll(Graphics g) {
				if (parts == null) {
					return;
				}
				for (int i=0; i<parts.length; i++) {
					drawSubjectPart(g, theCanvas.getRectangle(parts[i][0], parts[i][1], parts[i][2]));
				}
			} // end of Chainview.ChainWindow.MarkSubject.drawAll(Graphics)
			
			private boolean isActive() {
				return checkBox.isSelected();
			} // end of Chainview.ChainWindow.MarkSubject.isActive()
			
		} // end of Chainview.ChainWindow.MarkSubject
		
		
/**
* MarkSubject for Puteins.
*/
		private class PuteinSubject extends MarkSubject {
	
			private Putein[ ] puteins = null;
			private SequencePart[ ] puteinParts = null;
			
			private PuteinSubject() {
				checkBox = new JCheckBox("Puteins", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.PuteinSubject.constructor
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 4;
			} // end of Chainview.ChainWindow.PuteinSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.PuteinSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack puteinStack = new Stack();
				Stack partStack = new Stack();
				Putein put;
				int[ ][ ] coord;
				String idString;
				String[ ] fileNames = currentDir.list();
				for (int i=0; i<fileNames.length; i++) {
					if (FileNamer.isPuteinFileFromChain(fileNames[i], theCanvas.theChainInfo.tag)) { // Putein from this Chain?
						try {
							put = new Putein(Utilities.getReadFile(currentDir, fileNames[i]), new ORFID.ParameterBlock(targetDNA));
							overallFirst = Math.min(overallFirst, put.estimatedFirst);
							overallLast = Math.max(overallLast, put.estimatedLast);
							put.color = Color.black;
							puteinStack.push(put);
							coord = put.drawCoordinates();
							idString = put.GENE + " " + 
										targetDNA.externalize(put.estimatedFirst) + "-" +
										targetDNA.externalize(put.estimatedLast) + " Shifts:" +
										put.shiftcountinside + " Stops:" +
										put.stopcountinside + " Score:" + Math.round(put.fetchScore());
							for (int putt=0; putt<coord.length-1; putt++) {
								partStack.push(new SequencePart(
										new Rectangle(theCanvas.basToPos(coord[putt][0]),
												16 - theCanvas.FRAMEHEIGHT + (theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT) * coord[putt][1],
												theCanvas.basToPos(coord[putt + 1][0]) - theCanvas.basToPos(coord[putt][0]), 12),
										idString,
										put
										)
								);
							}
						} catch (RetroTectorException rte) {
							RetroTectorEngine.displayError(rte, RetroTectorEngine.WARNINGLEVEL);
						}
					}
				}
				puteins = new Putein[puteinStack.size()];
				puteinStack.copyInto(puteins);
				puteinParts = new SequencePart[partStack.size()];
				partStack.copyInto(puteinParts);
			} // end of Chainview.ChainWindow.PuteinSubject.refreshSubject(DNA, int, int)
	
/**
* Draws all puteins with three lines of colour determined by putein.
* @param	g	Graphics to draw in.
*/
			final void drawAll(Graphics g) {
				if (puteins != null) {
					for (int pu=0; pu<puteins.length; pu++) {
						if (!(puteins[pu].color == Color.red)) { // paint red ones last
							paintPuteinLine(g, puteins[pu], 19 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, puteins[pu], 22 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, puteins[pu], 25 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
					for (int pu=0; pu<puteins.length; pu++) {
						if (puteins[pu].color == Color.red) {
							paintPuteinLine(g, puteins[pu], 19 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, puteins[pu], 22 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, puteins[pu], 25 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
				}
			} // end of Chainview.ChainWindow.PuteinSubject.drawAll(Graphics)
					
/**
* Draws one part with three black lines.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2 + 3, bounds.x + bounds.width, bounds.y + bounds.height / 2 + 3);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2 , bounds.x + bounds.width, bounds.y + bounds.height / 2);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2 - 3, bounds.x + bounds.width, bounds.y + bounds.height / 2 - 3);
			} // end of Chainview.ChainWindow.PuteinSubject.drawSubjectPart(Graphics, Rectangle)
			
			private final void paintPuteinLine(Graphics g, Putein put, int vOffset, int vFactor) {
				int[ ][ ] coord = put.drawCoordinates();
				g.setColor((Color) put.color);
				for (int i=0; i<coord.length-1; i++) {
					g.drawLine(theCanvas.basToPos(coord[i][0]), vOffset + vFactor * coord[i][1], theCanvas.basToPos(coord[i + 1][0]), vOffset + vFactor * coord[i + 1][1]);
				}
			} // end of Chainview.ChainWindow.PuteinSubject.paintPuteinLine(Graphics, Putein, int, int)
	
		} // end of Chainview.ChainWindow.PuteinSubject
	
	
/**
* MarkSubject for Puteins.
*/
		private class EnvTraceSubject extends MarkSubject {
	
			private Xon[ ] xxons = null;
			private SequencePart[ ] xxonParts = null;
			
			
			private EnvTraceSubject() {
				checkBox = new JCheckBox("EnvTrace", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.EnvTraceSubject.constructor
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 12;
			} // end of Chainview.ChainWindow.EnvTraceSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.EnvTraceSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack xxonStack = new Stack();
				Stack partStack = new Stack();
				Xon xxon;
				int[ ][ ] coord;
				String idString;
				String[ ] fileNames = currentDir.list();
				for (int i=0; i<fileNames.length; i++) {
					if ((FileNamer.isEnvTraceFileFromChain(fileNames[i], theCanvas.theChainInfo.tag)) | (FileNamer.isEnvTraceFileFromChain(fileNames[i], theCanvas.theChainInfo.number))) {
						try {
							xxon = new Xon(Utilities.getReadFile(currentDir, fileNames[i]), new ORFID.ParameterBlock(targetDNA));
							overallFirst = Math.min(overallFirst, xxon.estimatedFirst);
							overallLast = Math.max(overallLast, xxon.estimatedLast);
							xxon.color = Color.black;
							xxonStack.push(xxon);
							coord = xxon.drawCoordinates();
							idString = "EnvTrace " + 
										targetDNA.externalize(xxon.estimatedFirst) + "-" +
										targetDNA.externalize(xxon.estimatedLast) + " Shifts:" +
										xxon.shiftcountinside + " Stops:" +
										xxon.stopcountinside + " Score:" + Math.round(xxon.fetchScore());
							for (int putt=0; putt<coord.length-1; putt++) {
								partStack.push(new SequencePart(
										new Rectangle(theCanvas.basToPos(coord[putt][0]),
												16 - theCanvas.FRAMEHEIGHT + (theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT) * coord[putt][1],
												theCanvas.basToPos(coord[putt + 1][0]) - theCanvas.basToPos(coord[putt][0]), 12),
										idString,
										xxon
										)
								);
							}
						} catch (RetroTectorException rte) {
							RetroTectorEngine.displayError(rte, RetroTectorEngine.WARNINGLEVEL);
						}
					}
				}
				xxons = new Xon[xxonStack.size()];
				xxonStack.copyInto(xxons);
				xxonParts = new SequencePart[partStack.size()];
				partStack.copyInto(xxonParts);
			} // end of Chainview.ChainWindow.EnvTraceSubject.refreshSubject(DNA, int, int)
	
/**
* Draws all puteins with three lines of colour determined by putein.
* @param	g	Graphics to draw in.
*/
			final void drawAll(Graphics g) {
				if (xxons != null) {
					for (int pu=0; pu<xxons.length; pu++) {
						if (!(xxons[pu].color == Color.red)) { // paint red ones last
							paintPuteinLine(g, xxons[pu], 19 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, xxons[pu], 22 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, xxons[pu], 25 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
					for (int pu=0; pu<xxons.length; pu++) {
						if (xxons[pu].color == Color.red) {
							paintPuteinLine(g, xxons[pu], 19 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, xxons[pu], 22 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintPuteinLine(g, xxons[pu], 25 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
				}
			} // end of Chainview.ChainWindow.EnvTraceSubject.drawAll(Graphics)
					
/**
* Draws one part with three black lines.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				dashedHorizontal(g, bounds.x, bounds.x + bounds.width, bounds.y + bounds.height / 2 + 3, 4);
				dashedHorizontal(g, bounds.x, bounds.x + bounds.width, bounds.y + bounds.height / 2 , 4);
				dashedHorizontal(g, bounds.x, bounds.x + bounds.width, bounds.y + bounds.height / 2 - 3, 4);
			} // end of Chainview.ChainWindow.EnvTraceSubject.drawSubjectPart(Graphics, Rectangle)
			
			private final void paintPuteinLine(Graphics g, Xon put, int vOffset, int vFactor) {
				int[ ][ ] coord = put.drawCoordinates();
				g.setColor((Color) put.color);
				for (int i=0; i<coord.length-1; i++) {
					dashedHorizontal(g, theCanvas.basToPos(coord[i][0]), theCanvas.basToPos(coord[i + 1][0]), vOffset + vFactor * coord[i][1], 4);
				}
			} // end of Chainview.ChainWindow.EnvTraceSubject.paintPuteinLine(Graphics, Putein, int, int)
	
		} // end of Chainview.ChainWindow.EnvTraceSubject
	
	
/**
* MarkSubject for XXons.
*/
		private class XXonSubject extends MarkSubject {
	
			private Xon[ ] xxons = null;
			private SequencePart[ ] xxonParts = null;
			
	
			private XXonSubject() {
				checkBox = new JCheckBox("XXons", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.XXonSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 4;
			} // end of Chainview.ChainWindow.XXonSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.XXonSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack xxonStack = new Stack();
				Stack partStack = new Stack();
				Xon xxon;
				int[ ][ ] coord;
				String idString;
				String[ ] fileNames = currentDir.list();
				for (int i=0; i<fileNames.length; i++) {
					if (FileNamer.isXXonFileFromChain(fileNames[i], theCanvas.theChainInfo.tag)) {
						try {
							xxon = new Xon(Utilities.getReadFile(currentDir, fileNames[i]), new ORFID.ParameterBlock(targetDNA));
							overallFirst = Math.min(overallFirst, xxon.estimatedFirst);
							overallLast = Math.max(overallLast, xxon.estimatedLast);
							xxon.color = Color.black;
							xxonStack.push(xxon);
							coord = xxon.drawCoordinates();
							idString = "XXon " + 
										targetDNA.externalize(xxon.estimatedFirst) + "-" +
										targetDNA.externalize(xxon.estimatedLast) + " Shifts:" +
										xxon.shiftcountinside + " Stops:" +
										xxon.stopcountinside + " Score:" + Math.round(xxon.fetchScore());
							for (int putt=0; putt<coord.length-1; putt++) {
								partStack.push(new SequencePart(
										new Rectangle(theCanvas.basToPos(coord[putt][0]),
												16 - theCanvas.FRAMEHEIGHT + (theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT) * coord[putt][1],
												theCanvas.basToPos(coord[putt + 1][0]) - theCanvas.basToPos(coord[putt][0]), 12),
										idString,
										xxon
										)
								);
							}
						} catch (RetroTectorException rte) {
							RetroTectorEngine.displayError(rte, RetroTectorEngine.WARNINGLEVEL);
						}
					}
				}
				xxons = new Xon[xxonStack.size()];
				xxonStack.copyInto(xxons);
				xxonParts = new SequencePart[partStack.size()];
				partStack.copyInto(xxonParts);
			} // end of Chainview.ChainWindow.XXonSubject.refreshSubject(DNA, int, int)
	
/**
* Draws all XXons with two lines of colour determined by XXon.
* @param	g	Graphics to draw in.
*/
			final void drawAll(Graphics g) {
				if (xxons != null) {
					for (int pu=0; pu<xxons.length; pu++) {
						if (!(xxons[pu].color == Color.red)) { // paint red ones last
							paintXXonLine(g, xxons[pu], 20 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintXXonLine(g, xxons[pu], 23 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
					for (int pu=0; pu<xxons.length; pu++) {
						if (xxons[pu].color == Color.red) {
							paintXXonLine(g, xxons[pu], 20 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
							paintXXonLine(g, xxons[pu], 23 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
				}
			} // end of Chainview.ChainWindow.XXonSubject.drawAll(Graphics)
			
/**
* Draws one part with two black lines.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2 + 1, bounds.x + bounds.width, bounds.y + bounds.height / 2 + 1);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2 - 2, bounds.x + bounds.width, bounds.y + bounds.height / 2 - 2);
			} // end of Chainview.ChainWindow.XXonSubject.drawSubjectPart(Graphics, Rectangle)
			
			private final void paintXXonLine(Graphics g, Xon xxon, int vOffset, int vFactor) {
				int[ ][ ] coord = xxon.drawCoordinates();
				g.setColor((Color) xxon.color);
				for (int i=0; i<coord.length-1; i++) {
					g.drawLine(theCanvas.basToPos(coord[i][0]), vOffset + vFactor * coord[i][1], theCanvas.basToPos(coord[i + 1][0]), vOffset + vFactor * coord[i + 1][1]);
				}
			} // end of Chainview.ChainWindow.XXonSubject.paintXXonLine(Graphics g, Xon xxon, int vOffset, int vFactor)
	
		} // end of Chainview.ChainWindow.XXonSubject
	
	
/**
* MarkSubject for Xons.
*/
		private class XonSubject extends MarkSubject {
	
			private Xon[ ] xons = null;
			private SequencePart[ ] xonParts = null;
			
	
			private XonSubject() {
				checkBox = new JCheckBox("Xons", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.XonSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 4;
			} // end of Chainview.ChainWindow.XonSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.XonSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack xonStack = new Stack();
				Stack partStack = new Stack();
				Xon xon;
				int[ ][ ] coord;
				String idString;
				String[ ] fileNames = currentDir.list();
				for (int i=0; i<fileNames.length; i++) {
					if (FileNamer.isXonFileFromChain(fileNames[i], theCanvas.theChainInfo.tag)) {
						try {
							xon = new Xon(Utilities.getReadFile(currentDir, fileNames[i]), new ORFID.ParameterBlock(targetDNA));
							overallFirst = Math.min(overallFirst, xon.estimatedFirst);
							overallLast = Math.max(overallLast, xon.estimatedLast);
							xon.color = Color.black;
							xonStack.push(xon);
							coord = xon.drawCoordinates();
							idString = "Xon " + 
										targetDNA.externalize(xon.estimatedFirst) + "-" +
										targetDNA.externalize(xon.estimatedLast) + " Shifts:" +
										xon.shiftcountinside + " Stops:" +
										xon.stopcountinside + " Score:" + Math.round(xon.fetchScore());
							for (int putt=0; putt<coord.length-1; putt++) {
								partStack.push(new SequencePart(
										new Rectangle(theCanvas.basToPos(coord[putt][0]),
												16 - theCanvas.FRAMEHEIGHT + (theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT) * coord[putt][1],
												theCanvas.basToPos(coord[putt + 1][0]) - theCanvas.basToPos(coord[putt][0]), 12),
										idString,
										xon
										)
								);
							}
						} catch (RetroTectorException rte) {
							RetroTectorEngine.displayError(rte, RetroTectorEngine.WARNINGLEVEL);
						}
					}
				}
				xons = new Xon[xonStack.size()];
				xonStack.copyInto(xons);
				xonParts = new SequencePart[partStack.size()];
				partStack.copyInto(xonParts);
			} // end of Chainview.ChainWindow.XonSubject.refreshSubject(DNA, int, int)
	
/**
* Draws all Xons with one line of colour determined by Xon.
* @param	g	Graphics to draw in.
*/
			final void drawAll(Graphics g) {
				if (xons != null) {
					for (int pu=0; pu<xons.length; pu++) {
						if (!(xons[pu].color == Color.red)) { // paint red ones last
							paintXon(g, xons[pu], 22 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
					for (int pu=0; pu<xons.length; pu++) {
						if (xons[pu].color == Color.red) {
							paintXon(g, xons[pu], 22 - theCanvas.FRAMEHEIGHT, theCanvas.FRAMEHEIGHT + theCanvas.FONTHEIGHT);
						}
					}
				}
			} // end of Chainview.ChainWindow.XonSubject.drawAll(Graphics)
			
/**
* Draws one part with one black line.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2, bounds.x + bounds.width, bounds.y + bounds.height / 2);
			} // end of Chainview.ChainWindow.XonSubject.drawSubjectPart(Graphics, Rectangle)
			
			private final void paintXon(Graphics g, Xon xon, int vOffset, int vFactor) {
				int[ ][ ] coord = xon.drawCoordinates();
				g.setColor((Color) xon.color);
				for (int i=0; i<coord.length-1; i++) {
					g.drawLine(theCanvas.basToPos(coord[i][0]), vOffset + vFactor * coord[i][1], theCanvas.basToPos(coord[i + 1][0]), vOffset + vFactor * coord[i + 1][1]);
				}
			} // end of Chainview.ChainWindow.XonSubject.paintXon(Graphics g, Xon xxon, int vOffset, int vFactor)
	
		} // end of Chainview.ChainWindow.XonSubject
	
	
/**
* MarkSubject for start codons.
*/
		private class StartCodonSubject extends MarkSubject {
		
			private StartCodonSubject() {
				checkBox = new JCheckBox("Start codon", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.StartCodonSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 1;
			} // end of Chainview.ChainWindow.StartCodonSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.StartCodonSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) {
				int startcount = 0;
				int[ ] startc = targetDNA.getStartCodons();
				for (int p=0; (p<startc.length) && (startc[p] <= last); p++) {
					if (startc[p] >= first) {
						startcount++;
					}
				}
				parts = new int[startcount][3];
				startcount = 0;
				for (int p=0; (p<startc.length) && (startc[p] <= last); p++) {
					if (startc[p] >= first) {
						try {
							parts[startcount][0] = startc[p];
							parts[startcount][1] = startc[p] + 2;
							parts[startcount++][2] = startc[p];
						} catch (ArrayIndexOutOfBoundsException e) {
							throw e;
						}
					}
				}
			} // end of Chainview.ChainWindow.StartCodonSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with dashed blue vertical line.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.blue);
				int i;
				for (i=5; i<=bounds.height; i+=5) {
					g.drawLine(bounds.x, bounds.y + i - 5, bounds.x, bounds.y + i - 2);
				}
				g.drawLine(bounds.x, bounds.y + i - 5, bounds.x, bounds.y + bounds.height);
			} // end of Chainview.ChainWindow.StartCodonSubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.StartCodonSubject
		
		
/**
* MarkSubject for TGA stop codons.
*/
		private class TGAStopCodonSubject extends MarkSubject {
		
			private TGAStopCodonSubject() {
				checkBox = new JCheckBox("TGA stop codon", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.TGAStopCodonSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 1;
			} // end of Chainview.ChainWindow.TGAStopCodonSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.TGAStopCodonSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) {
				Stack s = new Stack();
				int [ ] is;
				int[ ] stopc = targetDNA.getStopCodons();
				for (int p=0; (p<stopc.length) && (stopc[p] <= last); p++) {
					if ((stopc[p] >= first) && (targetDNA.get6bit(stopc[p]) == ORFID.TGACODON)) {
						is = new int[3];
						is[0] = stopc[p];
						is[1] = stopc[p] + 2;
						is[2] = stopc[p];
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.TGAStopCodonSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with red vertical line in upper half.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.red);
				g.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height / 2);
			} // end of Chainview.ChainWindow.TGAStopCodonSubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.TGAStopCodonSubject
		
	
/**
* MarkSubject for TAG stop codons.
*/
		private class TAGStopCodonSubject extends MarkSubject {
		
			private TAGStopCodonSubject() {
				checkBox = new JCheckBox("TAG stop codon", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.TAGStopCodonSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 1;
			} // end of Chainview.ChainWindow.TAGStopCodonSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.TAGStopCodonSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) {
				Stack s = new Stack();
				int [ ] is;
				int[ ] stopc = targetDNA.getStopCodons();
				for (int p=0; (p<stopc.length) && (stopc[p] <= last); p++) {
					if ((stopc[p] >= first) && (targetDNA.get6bit(stopc[p]) == ORFID.TAGCODON)) {
						is = new int[3];
						is[0] = stopc[p];
						is[1] = stopc[p] + 2;
						is[2] = stopc[p];
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.TAGStopCodonSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with vertical red line.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.red);
				g.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
			} // end of Chainview.ChainWindow.TAGStopCodonSubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.TAGStopCodonSubject
		
	
/**
* MarkSubject for TAA stop codons.
*/
		private class TAAStopCodonSubject extends MarkSubject {
		
			private TAAStopCodonSubject() {
				checkBox = new JCheckBox("TAA stop codon", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.TAAStopCodonSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 1;
			} // end of Chainview.ChainWindow.TAAStopCodonSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.TAAStopCodonSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) {
				Stack s = new Stack();
				int [ ] is;
				int[ ] stopc = targetDNA.getStopCodons();
				for (int p=0; (p<stopc.length) && (stopc[p] <= last); p++) {
					if ((stopc[p] >= first) && (targetDNA.get6bit(stopc[p]) == ORFID.TAACODON)) {
						is = new int[3];
						is[0] = stopc[p];
						is[1] = stopc[p];
						is[2] = stopc[p];
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.TAAStopCodonSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with vertical red line in lower half.
* @param	g				Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.red);
				g.drawLine(bounds.x, bounds.y + bounds.height / 2, bounds.x, bounds.y + bounds.height);
			} // end of Chainview.ChainWindow.TAAStopCodonSubject.drawSubjectPart
			
		} // end of Chainview.ChainWindow.TAAStopCodonSubject(Graphics, Rectangle)
		
	
/**
* MarkSubject for glycosylation sites.
*/
		private class GlycSiteSubject extends MarkSubject {
		
			private GlycSiteSubject() {
				checkBox = new JCheckBox("Glycosylation site", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.GlycSiteSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 2;
			} // end of Chainview.ChainWindow.GlycSiteSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.GlycSiteSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) {
				Stack s = new Stack();
				int [ ] is;
				int[ ] glycc = targetDNA.getGlycosyls();
				for (int p=0; (p<glycc.length) && (glycc[p] <= last); p++) {
					if (glycc[p] >= first) {
						is = new int[3];
						is[0] = glycc[p];
						is[1] = glycc[p] + 8; // ?
						is[2] = glycc[p];
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.GlycSiteSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with thick green line in upper half.
* @param	g				Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.green);
				g.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height / 2);
				g.drawLine(bounds.x + 1, bounds.y, bounds.x + 1, bounds.y + bounds.height / 2);
			} // end of Chainview.ChainWindow.GlycSiteSubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.GlycSiteSubject
		
	
/**
* MarkSubject for slippery sequences.
*/
		private class SlipperySubject extends MarkSubject {
		
			private SlipperySubject() {
				checkBox = new JCheckBox("Slippery sequence", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.SlipperySubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 4;
			} // end of Chainview.ChainWindow.SlipperySubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT ;
			} // end of Chainview.ChainWindow.SlipperySubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack s = new Stack();
				int [ ] is;
				SlipperyMotif slipperyMotif = (SlipperyMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SLIPPERYMOTIFKEY);
				slipperyMotif.refresh(new Motif.RefreshInfo(targetDNA, 0, 0, null));
				MotifHit mh;
				for (int p=first; p<=last; p++) {
					if ((mh = slipperyMotif.getMotifHitAt(p)) != null) {
						is = new int[3];
						is[0] = mh.MOTIFHITFIRST;
						is[1] = mh.MOTIFHITLAST;
						is[2] = mh.MOTIFHITHOTSPOT;
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.SlipperySubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with a black S.
* @param	g				Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawLine(bounds.x + bounds.width / 2, bounds.y, bounds.x + bounds.width / 2, bounds.y); // to avoid MacOS bug
				g.drawArc(bounds.x, bounds.y, bounds.width - 1, bounds.height / 3, 0, 270);
				g.drawArc(bounds.x, bounds.y + bounds.height / 3, bounds.width - 1, bounds.height / 3, 90, -270);
			} // end of Chainview.ChainWindow.SlipperySubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.SlipperySubject
		
	
/**
* MarkSubject for pseudoknots.
*/
		private class PseudoKnotSubject extends MarkSubject {
		
			private PseudoKnotSubject() {
				checkBox = new JCheckBox("Pseudoknot", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.PseudoKnotSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return ChainCanvas.FRAMEHEIGHT / 2;
			} // end of Chainview.ChainWindow.PseudoKnotSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT;
			} // end of Chainview.ChainWindow.PseudoKnotSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack s = new Stack();
				int [ ] is;
				PseudoKnotMotif pseudoknotMotif = (PseudoKnotMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.PSEUDOKNOTMOTIFKEY);
				pseudoknotMotif.refresh(new Motif.RefreshInfo(targetDNA, 0, 0, null));
				MotifHit mh;
				for (int p=first; p<=last; p++) {
					if ((mh = pseudoknotMotif.getMotifHitAt(p)) != null) {
						is = new int[3];
						is[0] = mh.MOTIFHITFIRST;
						is[1] = mh.MOTIFHITLAST;
						is[2] = mh.MOTIFHITHOTSPOT;
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.PseudoKnotSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with black 8.
* @param	g				Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawOval(bounds.x, bounds.y, bounds.width - 1, bounds.height / 2 - 1);
				g.drawOval(bounds.x, bounds.y + bounds.height / 2 - 1, bounds.width - 1, bounds.height / 2 - 1);
			} // end of Chainview.ChainWindow.PseudoKnotSubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.PseudoKnotSubject
		
	
/**
* MarkSubject for splice acceptors.
*/
		private class SpliceAcceptorSubject extends MarkSubject {
		
			private SpliceAcceptorSubject() {
				checkBox = new JCheckBox("Splice acceptor", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.SpliceAcceptorSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 4;
			} // end of Chainview.ChainWindow.SpliceAcceptorSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT ;
			} // end of Chainview.ChainWindow.SpliceAcceptorSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack s = new Stack();
				int [ ] is;
				SpliceAcceptorMotif spliceAcceptorMotif = (SpliceAcceptorMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SPLICEACCEPTORMOTIFKEY);
				spliceAcceptorMotif.refresh(new Motif.RefreshInfo(targetDNA, 0, 0, null));
				MotifHit mh;
				for (int p=first; p<=last; p++) {
					if ((mh = spliceAcceptorMotif.getMotifHitAt(p)) != null) {
						is = new int[3];
						is[0] = mh.MOTIFHITFIRST;
						is[1] = mh.MOTIFHITLAST;
						is[2] = mh.MOTIFHITHOTSPOT;
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.SpliceAcceptorSubject.refreshSubject(DNA, int, int)
			
/**
* Draws one part with black backslash.
* @param	g				Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
			} // end of Chainview.ChainWindow.SpliceAcceptorSubject.drawSubjectPart(Graphics, Rectangle)
			
		} // end of Chainview.ChainWindow.SpliceAcceptorSubject
		
	
/**
* MarkSubject for splice donors.
*/
		private class SpliceDonorSubject extends MarkSubject {
		
			private SpliceDonorSubject() {
				checkBox = new JCheckBox("Splice donor", true);
				checkBox.addChangeListener(theWindow);
			} // end of Chainview.ChainWindow.SpliceDonorSubject.constructor()
	
/**
* As required by Icon.
*/
			public final int getIconWidth() {
				return 4;
			} // end of Chainview.ChainWindow.SpliceDonorSubject.getIconWidth()
	
/**
* As required by Icon.
*/
			public final int getIconHeight() {
				return ChainCanvas.FRAMEHEIGHT ;
			} // end of Chainview.ChainWindow.SpliceDonorSubject.getIconHeight()
	
/**
* Recalculates fields with a new DNA segment.
* @param	targetDNA	The DNA containing the segment.
* @param	first			(internal) first position in targetDNA of the segment.
* @param	last			(internal) last position in targetDNA of the segment.
*/
			final void refreshSubject(DNA targetDNA, int first, int last) throws RetroTectorException {
				Stack s = new Stack();
				int [ ] is;
				SpliceDonorMotif spliceDonorMotif = (SpliceDonorMotif) RetroTectorEngine.getCurrentDatabase().getFirstMotif(Database.SPLICEDONORMOTIFKEY);
				spliceDonorMotif.refresh(new Motif.RefreshInfo(targetDNA, 0, 0, null));
				MotifHit mh;
				for (int p=first; p<=last; p++) {
					if ((mh = spliceDonorMotif.getMotifHitAt(p)) != null) {
						is = new int[3];
						is[0] = mh.MOTIFHITFIRST;
						is[1] = mh.MOTIFHITLAST;
						is[2] = mh.MOTIFHITHOTSPOT;
						s.push(is);
					}
				}
				parts = new int[s.size()][3];
				s.copyInto(parts);
			} // end of Chainview.ChainWindow.SpliceDonorSubject.refreshSubject(DNa, int, int)
			
/**
* Draws one part with black slash.
* @param	g	Graphics to draw it in.
* @param	bounds	Rectangle in g to fill.
*/
			final void drawSubjectPart(Graphics g, Rectangle bounds) {
				g.setColor(Color.black);
				g.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y);
			} // end of Chainview.ChainWindow.SpliceDonorSubject.drawSubjectPart
			
		} // end of Chainview.ChainWindow.SpliceDonorSubject(Graphics, Rectangle)
		
	
	
/**
* Scrollable Canvas showing MotifHits etc etc according to position in DNA
* and reading frame.
*/
		class ChainCanvas extends JCanvas implements MouseListener, MouseMotionListener {
	

// to run Puteinview
			private class PuteinviewActivator extends Thread {
			
				private String path = null;
				
/**
* @param		pa Path of file to display.
*/
				PuteinviewActivator(String pa) {
					path = pa;
				} // end of ChainCanvas.PuteinviewActivator.constructor(String)
		
				public void run() {
					try {
						File f = new File(path);
						String s = f.getName();
						Puteinview putv = new Puteinview();
						putv.getParameterTable().put(PUTEINFILEKEY, s);
						putv.runFlag = true;
						putv.execute();
					} catch (RetroTectorException rte) {
						RetroTectorEngine.displayError(rte);
					}
				} // end of ChainCanvas.PuteinviewActivator.run()
			
			} // end of ChainCanvas.PuteinviewActivator
		

/**
* Max height of characters (I hope) = 15.
*/
			public static final int FONTHEIGHT = 15;
	
/**
* Height of rectangle marking Motifhit = 20.
*/
			public static final int FRAMEHEIGHT = 20;
	
/**
* Preferred width of ChainCanvas = 300.
*/
			public static final int DISPWIDTH = 300;
	
/**
* Room for last MotifHit = 100.
*/
			public static final float ENDSPACE = 100.0f;
	
/**
* Horizontal tick spacing = 500 bases.
*/
			public static final int TICKDISTANCE = 500;
	
/**
* Width (in pixels) of total graphical information = 2500. Needed by ChainWindow.
*/
			public static final int TOTALWIDTH = 2500;
	
			private ChainGraphInfo theChainInfo; // the info to build the display on
			private float horfactor; // factor between base number and pixel coordinate
			
			private DNA targetDNA; // the DNA of the Chain
			private int first; // first (internal) base position of chain
			private int last; // last (internal) base position of chain
			
			private Rectangle ltr5Rect = null; // where 5'LTR is displayed
			private Rectangle ltr3Rect = null; // where 3'LTR is displayed
			private MotifHitGraphInfo ltr5Info = null;
			private MotifHitGraphInfo ltr3Info = null;
	
/**
* New canvas.
* @param	tDNA				The DNA in use.
*/
			private ChainCanvas(DNA tDNA) throws RetroTectorException {
				targetDNA = tDNA;
				setBackground(Color.white);
				addMouseMotionListener(this);
				addMouseListener(this);
				setPreferredSize(new Dimension(TOTALWIDTH, FONTHEIGHT + 3 * (FRAMEHEIGHT + FONTHEIGHT) + 15));
				setMinimumSize(new Dimension(DISPWIDTH, FONTHEIGHT + 3 * (FRAMEHEIGHT + FONTHEIGHT) + 15));
			} // end of Chainview.ChainWindow.ChainCanvas.constructor(DNA)
			
/**
* Change canvas to show the specified Chain.
* @param	c			The ChainGraphInfo to display.
* @param	tDNA	The DNA in use.
*/
			public final void useChain(ChainGraphInfo c, DNA tDNA) throws RetroTectorException {
				targetDNA = tDNA;
				theChainInfo = c;
				LTRCandidate cand;
				first = theChainInfo.FIRSTBASEPOS;
				overallFirst = first;
				cand = getCandidate(chainSignature + CoreLTRPair.KEY5LTR);
				if (cand != null) {
					overallFirst = Math.min(overallFirst, cand.getVeryFirst());
				}
				last = theChainInfo.LASTBASEPOS;
				overallLast = last;
				cand = getCandidate(chainSignature + CoreLTRPair.KEY3LTR);
				if (cand != null) {
					overallLast = Math.max(overallLast, cand.getVeryLast());
				}
	// these will be handled by doPaint
				ltr5Rect = null;
				ltr3Rect = null;
				ltr5Info = null;
				ltr3Info = null;
				
				horfactor = (TOTALWIDTH - ENDSPACE) / (last - first + 1); // preliminary, needed by refreshSubject

// to redefine overallFirst and overallLast
				puteinSubject.refreshSubject(targetDNA, first, last);
				envTraceSubject.refreshSubject(targetDNA, first, last);
				xxonSubject.refreshSubject(targetDNA, first, last);
				xonSubject.refreshSubject(targetDNA, first, last);
							
				first = Math.max(overallFirst - 10, 0);
				last = Math.min(overallLast + 10, targetDNA.LENGTH - 1);
				horfactor = (TOTALWIDTH - ENDSPACE) / (last - first + 1);

				for (int m=0; m<markSubjects.length; m++) {
					markSubjects[m].refreshSubject(targetDNA, first, last);
				}
				
				repaint();
			} // end of Chainview.ChainWindow.ChainCanvas.useChain(ChainGraphInfo, DNA)
							
/**
* As required by JComponent.
*/
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				doPaint(g);
			} // end of Chainview.ChainWindow.ChainCanvas.paintComponent(Graphics)
			
	// Converts (internal) base number in DNA to horizontal pixel coordinate.
			private final int basToPos(int bas) {
				return (int) Math.round((bas - first) * horfactor);
			} // end Chainview.ChainWindow.ChainCanvas.basToPos(int)
			
	// Converts  horizontal pixel coordinate into approximate (internal) base number in DNA.
			private final int posToBas(int pos) {
				return (int) Math.round(pos / horfactor + first);
			} // end of Chainview.ChainWindow.ChainCanvas.posToBas(int)
			
	// rectangle displaying base positions first to last, in frame of hotspot
			private final Rectangle getRectangle(int first, int last, int hotspot) {
				int frame = targetDNA.frameOf(hotspot) - 1;
				return new Rectangle(basToPos(first), FONTHEIGHT + 12 + (FRAMEHEIGHT + FONTHEIGHT) * frame, basToPos(last) - basToPos(first) + 1, FRAMEHEIGHT);
			} // end of Chainview.ChainWindow.ChainCanvas.getRectangle(int, int, int)
			
// draw green squiggle to mark break
			private final void drawBreakMarker(Graphics g, int hstart, int v, int hend) {
				g.setColor(Color.green);
				int o = 5;
				while (hstart < hend - 5) {
					g.drawLine(hstart, v, hstart + 5, v + o);
					v = v + o;
					o = -o;
					hstart += 5;
				}
				int d = (hend - hstart) * o / 5;
				g.drawLine(hstart, v, hend, v + o);
			} // end of drawBreakMarker(Graphics, int, int, int)
				
			
	// does the actual drawing
			private final void doPaint(Graphics g) {
				g.setColor(Color.black);
				g.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				for (int f=0; f<4; f++) { // draw horizontal lines subdividing display
					g.drawLine(0, FONTHEIGHT + 11 + f * (FRAMEHEIGHT + FONTHEIGHT), TOTALWIDTH, FONTHEIGHT + 11 + f*(FRAMEHEIGHT + FONTHEIGHT));
					g.drawLine(0, FONTHEIGHT + 12 + f * (FRAMEHEIGHT + FONTHEIGHT), TOTALWIDTH, FONTHEIGHT + 12 + f*(FRAMEHEIGHT + FONTHEIGHT));
					g.drawLine(0, FONTHEIGHT + 12 + FRAMEHEIGHT + f*(FRAMEHEIGHT + FONTHEIGHT), TOTALWIDTH, FONTHEIGHT + 12 + FRAMEHEIGHT + f * (FRAMEHEIGHT + FONTHEIGHT));
				}
	
	// draw active MarkSubjects
				for (int i=0; i<markSubjects.length; i++) {
					if (markSubjects[i].isActive()) {
						markSubjects[i].drawAll(g);
					}
				}
				
		// set position ticks
				g.setColor(Color.black);
				int tick = (first / TICKDISTANCE) * TICKDISTANCE + TICKDISTANCE;
				while (tick < last ) {
					int th = basToPos(tick);
					g.drawLine(th, FONTHEIGHT + 12, th, FONTHEIGHT + 2);
					g.drawString(String.valueOf(targetDNA.externalize(tick)), th, FONTHEIGHT);
					tick += TICKDISTANCE;
				}
				
	// ALU and LINE markers
				for (int co=0; co<(targetDNA.TRANSLATOR.CONTIGS.length-1); co++) {
					int coo = basToPos(targetDNA.TRANSLATOR.CONTIGS[co].LASTINTERNAL);
					dashedVertical(g, 0, 12 + 4 * (FRAMEHEIGHT + FONTHEIGHT), coo, 10);
					g.drawString(targetDNA.TRANSLATOR.CONTIGS[co].AFTERINSERT.INSERTIDENTIFIER, coo + 2, 10 + 4 * (FRAMEHEIGHT + FONTHEIGHT));
				}
				
	// draw MotifHits as rectangles with width showing Motif length, and text
	// showing score and MotifGroup of identifying Motif
				SubGeneHitGraphInfo currentSubGeneHitInfo;
				MotifHitGraphInfo currentMotifHitInfo;
				Rectangle r;
				LTRCandidate c;
				g.setColor(Color.black);
				for (int ghitindex=0; ghitindex<theChainInfo.subgeneinfo.length; ghitindex++) {
					currentSubGeneHitInfo = theChainInfo.subgeneinfo[ghitindex];
					for (int mhitindex=0; mhitindex<currentSubGeneHitInfo.motifhitinfo.length; mhitindex++) {
						currentMotifHitInfo = currentSubGeneHitInfo.motifhitinfo[mhitindex];
						if (currentMotifHitInfo.MOTIFDESCRIPTION.startsWith("5LT")) {
							c = getCandidate(chainSignature + CoreLTRPair.KEY5LTR);
							if (c == null) {
								r = getRectangle(currentMotifHitInfo.FIRSTPOS, currentMotifHitInfo.LASTPOS, currentMotifHitInfo.HOTSPOT);
								drawRectangle(r, g);
							} else {
								r = getRectangle(currentMotifHitInfo.FIRSTPOS, currentMotifHitInfo.LASTPOS, currentMotifHitInfo.HOTSPOT);
								drawRectangle(r, g);
								r.grow(-1, -1);
								drawRectangle(r, g);
								r.grow(-1, -1);
								drawRectangle(r, g);
								r = getRectangle(c.getVeryFirst(), c.getVeryLast(), currentMotifHitInfo.HOTSPOT);
							}
							ltr5Rect = new Rectangle(r);
							ltr5Info = currentMotifHitInfo;
						} else if (currentMotifHitInfo.MOTIFDESCRIPTION.startsWith("3LT")) {
							c = getCandidate(chainSignature + CoreLTRPair.KEY3LTR);
							if (c == null) {
								r = getRectangle(currentMotifHitInfo.FIRSTPOS, currentMotifHitInfo.LASTPOS, currentMotifHitInfo.HOTSPOT);
								drawRectangle(r, g);
							} else {
								r = getRectangle(currentMotifHitInfo.FIRSTPOS, currentMotifHitInfo.LASTPOS, currentMotifHitInfo.HOTSPOT);
								drawRectangle(r, g);
								r.grow(-1, -1);
								drawRectangle(r, g);
								r.grow(-1, -1);
								drawRectangle(r, g);
								r = getRectangle(c.getVeryFirst(), c.getVeryLast(), currentMotifHitInfo.HOTSPOT);
							}
							ltr3Rect = new Rectangle(r);
							ltr3Info = currentMotifHitInfo;
						} else {
							r = getRectangle(currentMotifHitInfo.FIRSTPOS, currentMotifHitInfo.LASTPOS, currentMotifHitInfo.HOTSPOT);
							drawRectangle(r, g);
						}
						r.grow(-1, -1);
						drawRectangle(r, g);
						g.drawString(String.valueOf(currentMotifHitInfo.SCORE), r.x + 2, r.y + r.height - 2);
						String d = currentMotifHitInfo.MOTIFDESCRIPTION.substring(0, currentMotifHitInfo.MOTIFDESCRIPTION.indexOf(':'));
						g.drawString(d, r.x + 2, r.y + r.height - 1 + FONTHEIGHT);
					}
				}
				
				int[ ][ ] breaks = theChainInfo.breaks();
				for (int i=0; i<breaks.length; i++) {
					drawBreakMarker(g, basToPos(breaks[i][0]), FONTHEIGHT + 2, basToPos(breaks[i][1]));
				}
			} // end of Chainview.ChainWindow.ChainCanvas.doPaint(Graphics)
	
/**
* As required by MouseMotionListener. Updates 'where' and 'description' field.
* Makes Puteins, XXons and Xons red if cursor close.
*/
			public void mouseMoved(MouseEvent e) {
				String s;
				theWindow.where.setText(String.valueOf(targetDNA.externalize(posToBas(e.getX()))));
				for (int p=puteinSubject.puteinParts.length-1; p>=0; p--) {
					s = puteinSubject.puteinParts[p].react(e.getPoint());
					if (s != null) {
						theWindow.description.setText(s);
						puteinSubject.puteinParts[p].PUTEIN.color = Color.red;
						repaint();
						return;
					}
				}
				for (int p=envTraceSubject.xxonParts.length-1; p>=0; p--) {
					s = envTraceSubject.xxonParts[p].react(e.getPoint());
					if (s != null) {
						theWindow.description.setText(s);
						envTraceSubject.xxonParts[p].PUTEIN.color = Color.red;
						repaint();
						return;
					}
				}
				for (int p=xxonSubject.xxonParts.length-1; p>=0; p--) {
					s = xxonSubject.xxonParts[p].react(e.getPoint());
					if (s != null) {
						theWindow.description.setText(s);
						xxonSubject.xxonParts[p].PUTEIN.color = Color.red;
						repaint();
						return;
					}
				}
				for (int p=xonSubject.xonParts.length-1; p>=0; p--) {
					s = xonSubject.xonParts[p].react(e.getPoint());
					if (s != null) {
						theWindow.description.setText(s);
						xonSubject.xonParts[p].PUTEIN.color = Color.red;
						repaint();
						return;
					}
				}
	// mouse not touching any Putein
				theWindow.description.setText("");
				for (int p=0; p<puteinSubject.puteinParts.length; p++) {
					puteinSubject.puteinParts[p].PUTEIN.color = Color.black;
				}
				for (int p=0; p<envTraceSubject.xxonParts.length; p++) {
					envTraceSubject.xxonParts[p].PUTEIN.color = Color.black;
				}
				for (int p=0; p<xxonSubject.xxonParts.length; p++) {
					xxonSubject.xxonParts[p].PUTEIN.color = Color.black;
				}
				for (int p=0; p<xonSubject.xonParts.length; p++) {
					xonSubject.xonParts[p].PUTEIN.color = Color.black;
				}
				repaint();
			} // end of Chainview.ChainWindow.ChainCanvas.mouseMoved(MouseEvent)
			
/**
* As required by MouseMotionListener.
*/
			public void mouseDragged(MouseEvent e) {
			} // end of Chainview.ChainWindow.ChainCanvas.mouseDragged
			
/**
* Starts Puteinview with relevant component.
*/
			public void mouseClicked(MouseEvent e) {
				String s;
				for (int p=puteinSubject.puteinParts.length-1; p>=0; p--) {
					s = puteinSubject.puteinParts[p].react(e.getPoint());
					if (s != null) {
						PuteinviewActivator pav = new PuteinviewActivator(puteinSubject.puteinParts[p].PUTEIN.inFilePath);
						pav.start();
						return;
					}
				}
				for (int p=envTraceSubject.xxonParts.length-1; p>=0; p--) {
					s = envTraceSubject.xxonParts[p].react(e.getPoint());
					if (s != null) {
						PuteinviewActivator pav = new PuteinviewActivator(envTraceSubject.xxonParts[p].PUTEIN.inFilePath);
						pav.start();
						return;
					}
				}
				for (int p=xxonSubject.xxonParts.length-1; p>=0; p--) {
					s = xxonSubject.xxonParts[p].react(e.getPoint());
					if (s != null) {
						PuteinviewActivator pav = new PuteinviewActivator(xxonSubject.xxonParts[p].PUTEIN.inFilePath);
						pav.start();
						return;
					}
				}
				for (int p=xonSubject.xonParts.length-1; p>=0; p--) {
					s = xonSubject.xonParts[p].react(e.getPoint());
					if (s != null) {
						PuteinviewActivator pav = new PuteinviewActivator(xonSubject.xonParts[p].PUTEIN.inFilePath);
						pav.start();
						return;
					}
				}
			} // end of Chainview.ChainWindow.ChainCanvas.mouseClicked(MouseEvent)
		
/**
* As required by MouseListener.
*/
			public void mouseEntered(MouseEvent e) {
			} // end of Chainview.ChainWindow.ChainCanvas.mouseEntered
			
/**
* As required by MouseListener.
*/
			public void mouseExited(MouseEvent e) {
				theWindow.where.setText("");
			} // end of Chainview.ChainWindow.ChainCanvas.mouseExited
			
/**
* As required by MouseListener.
*/
			public void mousePressed(MouseEvent e) {
			} // end of Chainview.ChainWindow.ChainCanvas.mousePressed
			
/**
* As required by MouseListener.
*/
			public void mouseReleased(MouseEvent e) {
			} // end of Chainview.ChainWindow.ChainCanvas.mouseReleased
	
		} // end of Chainview.ChainWindow.ChainCanvas
	
	
	// A4 version of ChainCanvas
		private class ChainSaveA4Canvas extends JCanvas {
		
			private ChainCanvas theCanvas;
			
			ChainSaveA4Canvas(ChainCanvas cc) {
				theCanvas = cc;
				setPreferredSize(new Dimension(540, 750));
				setMinimumSize(new Dimension(540, 750));
			} // end of Chainview.ChainWindow.ChainSaveA4Canvas.constructor(ChainCanvas)
	
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.gray);
				g.drawLine(500, 0, 500, 750);
				for (int p=0; p<5; p++) {
					theCanvas.doPaint(g);
					g.translate(-500, 150);
				}
			} // end of Chainview.ChainWindow.ChainSaveA4Canvas.paintComponent(Graphics)
			
		} // end of Chainview.ChainWindow.ChainSaveA4Canvas

	// horizontal version of ChainCanvas
		private class ChainSaveCanvas extends JCanvas {
		
			private ChainCanvas theCanvas;
			
			ChainSaveCanvas(ChainCanvas cc) {
				theCanvas = cc;
				setPreferredSize(theCanvas.getPreferredSize());
				setMinimumSize(theCanvas.getMinimumSize());
			} // end of Chainview.ChainWindow.ChainSaveCanvas.constructor(ChainCanvas)
				
	/**
	* As required by JComponent.
	*/
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				theCanvas.doPaint(g);
			} // end of Chainview.ChainWindow.ChainSaveCanvas.paintComponent(Graphics)
			
		} // end of Chainview.ChainWindow.ChainSaveCanvas


// ChainWindow fields
		private ChainCanvas theCanvas; // for graphic display
	  private JScrollPane thePane; // to contain theCanvas
		private JTextArea theArea = new JTextArea(15,50); // for text display
    private Box botomPanel = Box.createHorizontalBox();
    private JPanel bottomPanel = new JPanel();
    private Box centerPanel = Box.createVerticalBox();
    private JComboBox sgChoice = new JComboBox(); // to show acceptable SubGene hotspot positions
		private int firstvalid = -1;
		
		private DNA useDNA;
		private JButton pseudoGeneButton = new JButton("Pseudogene");
		private JButton saveA4Button = new JButton("Save as file, A4");
		private JButton saveButton = new JButton("Save as file");
		private JTextField where = new JTextField(10); // for base position corresponding to cursor
		private JTextField description = new JTextField(70); // for description of AcidSequence pointed to
		private String chainSignature = null;
		private JPanel markerPanel = new JPanel();
	
		private MarkSubject[ ] markSubjects;
		private PuteinSubject puteinSubject;
		private EnvTraceSubject envTraceSubject;
		private XXonSubject xxonSubject;
		private XonSubject xonSubject;
		
		private File psFile; // PseudoGene
				
/**
* Creates the window and sets up menus for a maximum of 100 Chains.
* Shows information about the first Chain in the text and graphics parts.
*	@param	title	The title of this.
*/
	 	ChainWindow(String title) throws RetroTectorException {
		
			super(title);
			JMenuBar mb = new JMenuBar();
			int nrOfChains = 0; // number of chains to show in menus
			while ((nrOfChains<graphinfo.length) && (nrOfChains < 100) && (graphinfo[nrOfChains] != null)) {
				nrOfChains++;
			}

			markSubjects = new MarkSubject[13];
			markSubjects[0] = new StartCodonSubject();
			markSubjects[1] = new TGAStopCodonSubject();
			markSubjects[2] = new TAGStopCodonSubject();
			markSubjects[3] = new TAAStopCodonSubject();
			markSubjects[4] = new GlycSiteSubject();
			markSubjects[5] = new SlipperySubject();
			markSubjects[6] = new PseudoKnotSubject();
			markSubjects[7] = new SpliceAcceptorSubject();
			markSubjects[8] = new SpliceDonorSubject();
			markSubjects[9] = puteinSubject = new PuteinSubject();
			markSubjects[10] = envTraceSubject = new EnvTraceSubject();
			markSubjects[11] = xxonSubject = new XXonSubject();
			markSubjects[12] = xonSubject = new XonSubject();

			markerPanel.setLayout(new GridLayout(0, 1));
			JPanel jp;
			for (int i=0; i<markSubjects.length; i++) {
				jp = new JPanel();
				jp.add(markSubjects[i].checkBox);
				jp.add(new JLabel(markSubjects[i]));
				markerPanel.add(jp);
			}
			
			theArea.setFont(new Font("Courier", Font.PLAIN, 10));
	 // set up menus of max 20 Chain numbers
	 		if (nrOfChains > 0) {
	 			int mi = 0; // chain counter
	 			String menuTitle = "";
				Stack theStack = new Stack();
				JMenuItem mim;
				while (mi < nrOfChains) {
					if (firstvalid == -1) {
						firstvalid = mi + 1;
					}
					mim = new JMenuItem(graphinfo[mi].number);
					mim.addActionListener(this);
					theStack.push(mim);
					if (menuTitle.equals("")) {
						menuTitle = graphinfo[mi].number + " - ";
					}
					if (theStack.size() >= 20) { // make one menu
						JMenu m = new JMenu(menuTitle + graphinfo[mi].number);
						for (int i=0; i<theStack.size(); i++) {
							m.add((JMenuItem) theStack.elementAt(i));
						}
						mb.add(m);
						menuTitle = "";
						theStack = new Stack();
					}
					mi++;
				}
				if (theStack.size() > 0) { // make last menu
					JMenu m = new JMenu(menuTitle);
					for (int i=0; i<theStack.size(); i++) {
						m.add((JMenuItem) theStack.elementAt(i));
					}
					mb.add(m);
				}
				setJMenuBar(mb);
				
	// set up canvas to show first Chain
				shiftMenu(0);
				theCanvas = new ChainCanvas(useDNA);
				theCanvas.addMouseListener(this);
				thePane = new JScrollPane(theCanvas);
				thePane.setPreferredSize(new Dimension(100, theCanvas.getPreferredSize().height + 10 + thePane.getHorizontalScrollBar().getPreferredSize().height));
				theCanvas.useChain(graphinfo[0], useDNA);
      } else {
				theArea.setText("No chains");
			}
				
			centerPanel.add(new JScrollPane(theArea));
			if (thePane != null) {
				centerPanel.add(thePane);
			}
        
			pseudoGeneButton.addActionListener(this);
			pseudoGeneButton.setForeground(Color.red);
			pseudoGeneButton.setVisible(false);
      botomPanel.add(pseudoGeneButton);
			saveA4Button.addActionListener(this);
      botomPanel.add(saveA4Button);
			saveButton.addActionListener(this);
      botomPanel.add(saveButton);
      botomPanel.add(where);
			sgChoice.addItem(" ");
      botomPanel.add(sgChoice);
			bottomPanel.setLayout(new GridLayout(0, 1));
      bottomPanel.add(botomPanel);
			description.setEditable(false);
      bottomPanel.add(description);
			getContentPane().add(centerPanel, BorderLayout.CENTER);
			getContentPane().add(bottomPanel, BorderLayout.SOUTH);
			getContentPane().add(markerPanel, BorderLayout.EAST);
			setLocation(200, 50);
			AWTUtilities.showFrame(this, null);
		} // end of Chainview.ChainWindow.constructor(String)

// react to new menu choice
		private final void shiftMenu(int menupp) {
			currentgraphinfo = graphinfo[menupp];
			if (graphinfo[menupp].number.startsWith("S")) {
				useDNA = secDNA;
			} else {
				useDNA = primDNA;
			}
			String[ ] dStrings = graphinfo[menupp].toStrings(useDNA);
			String com = graphinfo[menupp].LTRCOMMENT;
			int comi = com.indexOf('>');
			if (comi >= 0) {
				comi = com.length() - comi + 10;
			}
			String[ ] tStrings = useDNA.formSubString(useDNA.forceInside(graphinfo[menupp].FIRSTBASEPOS - comi), useDNA.forceInside(graphinfo[menupp].LASTBASEPOS + comi));
			
			theArea.setText(graphinfo[menupp].number + "\n" + Utilities.collapseLines(dStrings) + "\n\n" + Utilities.collapseLines(tStrings));
			theArea.setCaretPosition(0);

			psFile = new File(RetroTectorEngine.currentDirectory(), "Pseudogene_" + graphinfo[menupp].number + "_001.txt");
			if (psFile.exists()) {
				pseudoGeneButton.setVisible(true);
			} else {
				pseudoGeneButton.setVisible(false);
			}
			
			chainSignature = graphinfo[menupp].number;
			sgChoice.removeAll();
			Range ra;
			for (int s=0; s<database.subGeneNames.length; s++) {
				String na = database.subGeneNames[s];
				SubGene sg = database.getSubGene(na);
				ra = null;
				try {
					ra = graphinfo[menupp].allowedRange(sg, database);
				} catch (RetroTectorException rte) {
				}
				if (ra == null) {
					sgChoice.addItem(na + " null");
				} else {
					sgChoice.addItem(na + " " + useDNA.externalize(ra.RANGEMIN) + "<" + useDNA.externalize(ra.RANGEMAX));
				}
			}
		} // end of shiftMenu(int)
		
// find LTR candidate in RetroVID script
		private final LTRCandidate getCandidate(String name) {
			Object o = parameters.get(name);
			if (o == null) { // all tried?
				return null;
			}
			try {
				String[ ] ss = (String[ ]) o;
				return new LTRCandidate(useDNA, ss);
			} catch (Exception e) {
				return null;
			}
		} // end of Chainview.ChainWindow.getCandidate(String)
		
/**
* Handles menu events and Save as file button events.
*/
	 	public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() instanceof JMenuItem ) {
	// switch to another Chain
	  		String menutext = ((JMenuItem) ae.getSource()).getText();
	  		String menunr = menutext;
				chainSignature = menutext;
	  		int menupp = 0;
	  		for (int menup=0; menup<graphinfo.length; menup++) {
	  			if (graphinfo[menup].number.equals(menunr)) {
	  				menupp = menup;
	  			}
	  		}
				shiftMenu(menupp);
				
        try {
					theCanvas.useChain(graphinfo[menupp], useDNA);
				} catch (RetroTectorException re) {
				}
	 			validate();
	 			repaint();
	 		} else if (ae.getSource() == saveA4Button) {
				JCanvas jca = new ChainSaveA4Canvas(theCanvas);
				GraphicsJFrame gfr = new GraphicsJFrame("Save form", jca);
	 		} else if (ae.getSource() == saveButton) {
				JCanvas jca = new ChainSaveCanvas(theCanvas);
				GraphicsJFrame gfr = new GraphicsJFrame("Save form", jca);
	 		} else if (ae.getSource() == pseudoGeneButton) {
				try {
					FileViewFrame viewFrame = new FileViewFrame(psFile);
				} catch (RetroTectorException rse) {
					RetroTectorEngine.displayError(rse);
				}
	 		}

		} // end of Chainview.ChainWindow.actionPerformed(ActionEvent)
			
/**
* As required by MouseListener.
* If click in an LTR rectangle, show same window as LTRview.
*/
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() == theCanvas) {
				LTRviewPanel pan;
				LTRCandidate c = null;
				if ((theCanvas.ltr5Rect != null) && (theCanvas.ltr5Rect.contains(e.getPoint()))) {
					c = getCandidate(chainSignature + CoreLTRPair.KEY5LTR);
				}
				if ((theCanvas.ltr3Rect != null) && (theCanvas.ltr3Rect.contains(e.getPoint()))) {
					c = getCandidate(chainSignature + CoreLTRPair.KEY3LTR);
				}
				if (c != null) {
					try {
						CloseableJFrame theWindow = new CloseableJFrame("");
						theWindow.getContentPane().add(pan = new LTRviewPanel(c, theWindow, 1.5f));
						theWindow.setLocation(100, 100);
						AWTUtilities.showFrame(theWindow, new Dimension(600, pan.WHEIGHT));
					} catch (RetroTectorException rte) {
						RetroTectorEngine.displayError(rte, RetroTectorEngine.ERRORLEVEL);
					}
				}
			}
		} // end of Chainview.ChainWindow.mouseClicked(MouseEvent)

	
/**
* As required by MouseListener.
*/
		public void mouseEntered(MouseEvent e) {
		} // end of Chainview.ChainWindow.mouseEntered
		
/**
* As required by MouseListener.
*/
		public void mouseExited(MouseEvent e) {
		} // end of Chainview.ChainWindow.mouseExited
		
/**
* As required by MouseListener.
*/
		public void mousePressed(MouseEvent e) {
		} // end of Chainview.ChainWindow.mousePressed
		
/**
* As required by MouseListener.
*/
		public void mouseReleased(MouseEvent e) {
		} // end of Chainview.ChainWindow.mouseReleased

/**
* As required by ChangeListener. A MarkSubject was activated or deactivated.
*/
		public void stateChanged(ChangeEvent e) {
			Runnable r = new Runnable() {
				public void run() {
					theCanvas.repaint();
				}
			};
			AWTUtilities.doInEventThread(r);
		} // end of Chainview.ChainWindow.stateChanged(ChangeEvent)

	} // end of Chainview.ChainWindow
	

/**
* To draw a vertical dashed line.
* @param	g						The Graphics to draw in.
* @param	top					The y coordinate of the top of the line.
* @param	bottom			The y coordinate of the bottom of the line.
* @param	hpos				The x coordinate of the line.
* @param	dashlength	The length of the dashes and gaps.
*/
	public static final void dashedVertical(Graphics g, int top, int bottom, int hpos, int dashlength) {
		for (int y=top; y<=bottom; y+=2*dashlength) {
			g.drawLine(hpos, y, hpos, Math.min(bottom, y + dashlength));
		}
	} // end of Chainview.dashedVertical(Graphics, int, int, int, int)
	
/**
* To draw a horizontal dashed line.
* @param	g						The Graphics to draw in.
* @param	left				The x coordinate of the left end of the line.
* @param	right				The x coordinate of the right end of the line.
* @param	vpos				The y coordinate of the line.
* @param	dashlength	The length of the dashes and gaps.
*/
	public static final void dashedHorizontal(Graphics g, int left, int right, int vpos, int dashlength) {
		for (int x=left; x<=right; x+=2*dashlength) {
			g.drawLine(x, vpos, Math.min(right, x + dashlength), vpos);
		}
	} // end of Chainview.dashedHorizontal(Graphics, int, int, int, int)
	
/**
* Like Graphics.drawRect with a Rectangle argument.
* @param	r	The Rectangle to draw.
* @param	g	The Graphics to draw in.
*/
	public static void drawRectangle(Rectangle r, Graphics g) {
		g.drawRect(r.x, r.y, r.width, r.height);
	} // end of Chainview.drawRectangle(Rectangle, Graphics)

	
	static int overallFirst = Integer.MAX_VALUE;
	static int overallLast = -Integer.MAX_VALUE;

	private ChainWindow theWindow;
	private int maxn = -1; // highest Chain number encountered in file
  private Database database;
	private DNA primDNA = null;
	private DNA secDNA = null;
	private ChainGraphInfo[ ] graphinfo;
	private ChainGraphInfo currentgraphinfo = null;
	private	File currentDir;
	
/**
* Standard Executor constructor.
*/
	public Chainview() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(INPUTFILEKEY, "");
		explanations.put(INPUTFILEKEY, "The RetroVID output file to use");
		orderedkeys.push(INPUTFILEKEY);

	} // end of Chainview.constructor()
		
/**
* Reads parameters, from script or parameter window.
* @param	script	the script to fetch from, or null
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		super.initialize(script);
		if (runFlag && (script != null)) {
			maxn = script.getMaxchainnumber();
		}
	} // end of Chainview.initialize(ParameterFileReader)
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 22";
  } // end of Chainview.version
  
/**
* Execute as specified above.
*/
	public final String execute() throws RetroTectorException {
	
		String wTitle;
		String s1 = getString(INPUTFILEKEY, "");
    if (s1.length() > 0) {
      File f = Utilities.getReadFile(RetroTectorEngine.currentDirectory(), s1);
			wTitle = s1;
      ParameterFileReader reader = new ParameterFileReader(f, parameters);
      reader.readParameters();
      maxn = reader.getMaxchainnumber();
      reader.close();
    } else {
			wTitle = getString(SCRIPTPATHKEY, "");
		}

		String dnaFileName = getString(DNAFILEKEY, "");

    RetroTectorEngine.setCurrentDatabase(getString(DATABASEKEY, Executor.ORDINARYDATABASE));
    database = RetroTectorEngine.getCurrentDatabase();
		
		currentDir = RetroTectorEngine.currentDirectory();
		
		Stack chainStack = new Stack();
		ChainGraphInfo cgi;

    primDNA = getDNA(dnaFileName, true);
    secDNA = getDNA(dnaFileName, false);
    
    Object o;
    int count = 100;
// collect all chain descriptions
    for (int chainnr=1; (chainnr<=maxn) & (count > 0); chainnr++) {
      String s = ParameterFileReader.CHAINP + chainnr;
      if ((o = parameters.get(s)) != null) {
        String[ ] ss = getStringArray(s);
        cgi = new ChainGraphInfo(ss, primDNA);
        cgi.number = "P" + chainnr;
				cgi.tag = "P" + zeroLead(chainnr, 90000);
        chainStack.push(cgi);
        count--;
      }
      s = ParameterFileReader.CHAINS + chainnr;
      if ((o = parameters.get(s)) != null) {
        String[ ] ss = getStringArray(s);
        cgi = new ChainGraphInfo(ss, secDNA);
        cgi.number = "S" + chainnr;
				cgi.tag = "S" + zeroLead(chainnr, 90000);
        chainStack.push(cgi);
        count--;
      }
    }

    graphinfo = new ChainGraphInfo[chainStack.size()];
    chainStack.copyInto(graphinfo);
    Utilities.sort(graphinfo); // sort by score
		if (graphinfo.length > 0) {
			currentgraphinfo = graphinfo[0];
		}
    theWindow = new ChainWindow(new File(wTitle).getName());		
		return "";
	} // end of Chainview.execute()
	 
} // end of Chainview
