/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/10 -06
*/

package retrotector;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import builtins.*;

/**
* General window to display graphics, with GIF save available.
*/
public class LTRviewPanel extends JCanvas implements AdjustmentListener {

	public static final int LEADERLENGTH = 10;
	public static final int TRAILERLENGTH = 10;
	public static final int VSPACE = 5;

	public final Font SMALLFONT;
	public final float SIZEFACTOR;
	public final int PIXELSPERBASE;
	final int PARTWIDTH;
	final LTRCandidate CANDIDATE;
	final DNA THEDNA;
	final int FIRSTPOS;
	final int LASTPOS;
	
	Database database = RetroTectorEngine.getCurrentDatabase();

	class LabeledPart {
	
		final int[ ] VPOS;
		float[ ] VALUES = null;
		float lim = -Float.MAX_VALUE;
	
		final String LABEL;
		int h;
		
		LabeledPart(String label, boolean useVPOS) {
			LABEL = label;
			if (useVPOS) {
				VPOS = new int[LASTPOS - FIRSTPOS + 1];
			} else {
				VPOS = null;
			}
			try {
				RetroTectorEngine.showProgress();
			} catch (RetroTectorException rte) {
			}
		} // end of constructor
		
		void drawVertical(String s, int x, int y, Graphics g) {
			for (int i=0; i<s.length(); i++) {
				g.drawString(s.substring(i, i+1), x + i * PIXELSPERBASE, y + sizeOf(i * 2));
			}
		} // end of drawVertical
		
		void paint(Graphics g) {
			g.setColor(Color.black);
			g.drawRect(0, 0, PARTWIDTH - 1, h + 2);
			if (VPOS != null) {
				for (int i=0; i<VPOS.length; i++) {
					g.drawLine(i * PIXELSPERBASE, VPOS[i], (i + 1) * PIXELSPERBASE - 1, VPOS[i]);
				}
			}
			if (lim != -Float.MAX_VALUE) {
				for (int i=0; i<VALUES.length; i++) {
					if (VALUES[i] >= lim) {
						g.setColor(Color.red);
						g.drawLine(i * PIXELSPERBASE + 3, 1, i * PIXELSPERBASE + 3, h );
						g.setColor(Color.black);
					}
				}
			}
		}
		
	} // end of LabeledPart


	class RfirstPart extends LabeledPart {

		private int gcode = Compactor.BASECOMPACTOR.charToIntId('g');
		private int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
	
		RfirstPart(String label) {
			super(label, true);
			h = sizeOf(20);
			VALUES = (float[ ]) ru5NetPart.VALUES.clone();
			for (int i=0; i<VPOS.length; i++) {
				if ((THEDNA.get2bit(FIRSTPOS + i) != gcode) || (THEDNA.get2bit(FIRSTPOS + i + 1) != ccode)) {
					VALUES[i] *= 0.5f;
				}
				VPOS[i] = 1 + (int) (h * (1 - VALUES[i]));
				lim = Math.max(lim, VALUES[i]);
			}
		} // end of constructor
		
		
	} // end of RfirstPart
	
	class RlastPart extends LabeledPart {
	
		private int acode = Compactor.BASECOMPACTOR.charToIntId('a');
		private int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
		
		RlastPart(String label) {
			super(label, true);
			h = sizeOf(20);
			VALUES = (float[ ]) u3rNetPart.VALUES.clone();
			int c;
			for (int i=0; i<VPOS.length; i++) {
				c = THEDNA.get2bit(FIRSTPOS + i);
				if (c == acode) {
					VALUES[i] *= 0.5f;
				} else if ((c == ccode) && (THEDNA.get2bit(FIRSTPOS + i + 1) == acode)) {
				} else {
					VALUES[i] *= 0.25f;
				}
				VPOS[i] = 1 + (int) (h * (1 - VALUES[i]));
				lim = Math.max(lim, VALUES[i]);
			}
		} // end of constructor
		
	} // end of RlastPart
	
	class ModifierPart extends LabeledPart {
			
		ModifierPart(Modifier mod, String label) {
			super(label, true);
			h = sizeOf(20);
			float mi = Float.MAX_VALUE;
			float ma = -Float.MAX_VALUE;
			VALUES = mod.getArray();
			for (int i=FIRSTPOS; i<=LASTPOS; i++) {
				mi = Math.min(mi, VALUES[i]);
				ma = Math.max(ma, VALUES[i]);
			}
			for (int j=0; j<VPOS.length; j++) {
				VPOS[j] = 1 +  h - (int) (h * (VALUES[FIRSTPOS + j] - mi) / (ma - mi));
			}
		} // end of ModifierPart.constructor

		public void paint(Graphics g) {
			super.paint(g);
		} // end of ModifierPart.paint

	} // end of ModifierPart
	
	class InrPart extends LabeledPart {
	
		final String TOSHOW;
		StringBuffer sb;
		
		InrPart(String label) throws RetroTectorException {
			super(label, false);
			h = sizeOf(11);
			sb = new StringBuffer(LASTPOS - FIRSTPOS + 1);
			for (int i=0; i<=LASTPOS - FIRSTPOS; i++) {
				sb.append(' ');
			}
			File f = RetroTectorEngine.getCurrentDatabase().getFile("Inrvariants.txt");
			Hashtable h = new Hashtable();
			ParameterFileReader pfr = new ParameterFileReader(f, h);
			pfr.readParameters();
			pfr.close();
			String[ ] siteStrings = (String[ ]) h.get("InrVariants");
		
			for (int s=0; s<siteStrings.length; s++) {
				doOne(siteStrings[s]);
			}
			TOSHOW = sb.toString();
		} // end of constructor

		private void doOne(String line) throws RetroTectorException {
			line = line.trim();
			byte[ ] pattern = new byte[line.length()];
			for (int i=0; i<pattern.length; i++) {
				pattern[i] = (byte) Compactor.BASECOMPACTOR.charToIntId(line.charAt(i));
			}

			AgrepContext context = new AgrepContext(pattern, FIRSTPOS, Math.min(THEDNA.LENGTH - pattern.length, LASTPOS), 0);
			int res = -1;
			while ((res = THEDNA.agrepFind(context)) != Integer.MIN_VALUE) {
				res = Math.abs(res) - FIRSTPOS;
				sb.replace(res, res + line.length(), line);
			}
		} // end of doOne
		
		public void paint(Graphics g) {
			super.paint(g);
			g.setFont(SMALLFONT);
			showString(TOSHOW, 0, sizeOf(8), g);
		} // end of paint
			
	} // end of InrPart
	
	class SequencePart extends LabeledPart {
			
		final char[ ] CHARS;
		
		SequencePart(String label) {
			super(label, false);
			String s = THEDNA.subString(FIRSTPOS, LASTPOS, true);
			CHARS = s.toCharArray();
			s = String.valueOf(THEDNA.externalize(FIRSTPOS));
			int sl = s.length();
			s = String.valueOf(THEDNA.externalize(LASTPOS));
			sl = Math.max(sl, s.length());
			h = sizeOf(17 + 2 * sl);
		} // end of SequencePart.constructor
		
		public void paint(Graphics g) {
			super.paint(g);
			g.setFont(SMALLFONT);
			int p;
			for (int i=0; i<CHARS.length; i++) {
				g.drawChars(CHARS, i, 1, i * PIXELSPERBASE, sizeOf(8));
				p = CANDIDATE.LTRCANDIDATEDNA.externalize(i + FIRSTPOS);
				if ((p % 100) == 0) {
					drawVertical(String.valueOf(p), i * PIXELSPERBASE, sizeOf(16), g);
				}
			}
		} // end of SequencePart.paint

	} // end of SequencePart
	
	
	class TranssitesPart extends LabeledPart {
	
		String[ ] hits;
		
		TranssitesPart(String label) throws RetroTectorException {
			super(label, false);
			hits = new String[LASTPOS - FIRSTPOS + 1];
			h = sizeOf(11);
			TransSiteModifier mod = THEDNA.getTransSiteModifier();
			mod.searchMatches(THEDNA, FIRSTPOS, LASTPOS, hits);
		} // end of constructor
		
		public void paint(Graphics g) {
			super.paint(g);
			g.setFont(SMALLFONT);
			for (int i=0; i<hits.length; i++) {
				if (hits[i] != null) {
					showString(hits[i], i * PIXELSPERBASE, sizeOf(8), g);
				}
			}
		}
		
	} // end of TranssitesPart
	
	class BaseNNPart extends LabeledPart {

		BaseNNPart(BaseNet bn, String label, int offset) throws RetroTectorException {
			super(label, true);
			h = sizeOf(20);
			VALUES = new float[VPOS.length];
			VALUES[0] = bn.scoreNetAt(FIRSTPOS - offset, THEDNA);
			for (int i=1; i<VALUES.length; i++) {
				VALUES[i] = bn.scoreNetAtNext();
			}
			for (int i=0; i<VPOS.length; i++) {
				VPOS[i] = 1 + (int) (h * (1 - VALUES[i]));
				lim = Math.max(lim, VALUES[i]);
			}
		} // end of BaseNNPart.constructor
		
		public void paint(Graphics g) {
			super.paint(g);
		} // end of paint
		
	} // end of BaseNNPart


	class BaseMatrixPart extends LabeledPart {

		
		BaseMatrixPart(BaseWeightMatrix bn, int offset, String label) throws RetroTectorException {
			super(label, true);
			h = sizeOf(20);
			VALUES = new float[LASTPOS - FIRSTPOS + 1];
			for (int i=0; i<VALUES.length; i++) {
				VALUES[i] = THEDNA.baseMatrixScoreAt(bn, FIRSTPOS + i - offset);
			}
			float mi = Float.MAX_VALUE;
			float ma = -Float.MAX_VALUE;
			for (int i=0; i<VALUES.length; i++) {
				if (!Float.isNaN(VALUES[i])) {
					mi = Math.min(mi, VALUES[i]);
					ma = Math.max(ma, VALUES[i]);
				}
			}
			for (int i=0; i<VPOS.length; i++) {
				VPOS[i] = 1 + h - (int) (h * (VALUES[i] - mi)/(ma - mi));
				lim = Math.max(lim, VALUES[i]);
			}
		} // end of BaseNNPart.constructor
		
		public void paint(Graphics g) {
			super.paint(g);
		} // end of paint
		
	} // end of BaseNNPart


	class U3RU5Part extends LabeledPart {
	
		final int RANGEMAX = 100;
		final int RANGEMIN = 10;
		final Point[ ] MARKERS;
		
		U3RU5Part(String label) {
			super(label, true);
			h = sizeOf(20);
			int la = Math.min(LASTPOS, THEDNA.LENGTH - ru5Net.NROFBASES - 1);
			float[ ] ru5 = new float[la - FIRSTPOS + 1];
			ru5[0] = ru5Net.scoreNetAt(FIRSTPOS, THEDNA);
			for (int i=1; i<ru5.length; i++) {
				ru5[i] = ru5Net.scoreNetAtNext();
			}
			TopFinder finder = new TopFinder(ru5, 2, 10);
			float[ ] vpos = new float[LASTPOS - FIRSTPOS + 1];
			la = Math.min(LASTPOS, THEDNA.LENGTH - u3rNet.NROFBASES - 1);
			vpos[0] = u3rNet.scoreNetAt(FIRSTPOS, THEDNA) + finder.maxvalue(RANGEMIN, RANGEMAX);
			for (int j=FIRSTPOS+1; j<=la; j++) {
				vpos[j - FIRSTPOS] = u3rNet.scoreNetAtNext() + finder.maxvalue(j - FIRSTPOS + RANGEMIN, j - FIRSTPOS + RANGEMAX);
			}
			float mi = Float.MAX_VALUE;
			float ma = -Float.MAX_VALUE;
			for (int i=0; i<vpos.length; i++) {
				if (!Float.isNaN(vpos[i])) {
					mi = Math.min(mi, vpos[i]);
					ma = Math.max(ma, vpos[i]);
				}
			}
			float lim = mi + 0.8f * (ma - mi);
			Stack st = new Stack();
			for (int j=0; j<VPOS.length; j++) {
				if (!Float.isNaN(vpos[j])) {
					VPOS[j] = 1 + h - (int) (h * (vpos[j] - mi) / (ma - mi));
					if (vpos[j] >= lim) {
						finder.maxvalue(j + RANGEMIN, j + RANGEMAX);
						if (finder.maxpos >= 0) {
							st.push(new Point(finder.maxpos, j + u3rNet.NROFBASES - 1));
						}
					}
				}
			}
			MARKERS = new Point[st.size()];
			st.copyInto(MARKERS);
		} // end of U3RU5Part.constructor
		
		public void paint(Graphics g) {
			super.paint(g);
			int t = sizeOf(2);
			int b = sizeOf(18);
			int m = sizeOf(2);
			int f;
			int l;
			g.setColor(Color.red);
			for (int mm=0; mm<MARKERS.length; mm++) {
				f = MARKERS[mm].x * PIXELSPERBASE;
				l = MARKERS[mm].y * PIXELSPERBASE;
				g.drawLine(f, t, f, b);
				g.drawLine(f, m, l, m);
				g.drawLine(l, t, l, b);
				m += 2;
				if (m > b) {
					m = t;
				}
				if (g.getColor() == Color.red) {
					g.setColor(Color.blue);
				} else {
					g.setColor(Color.red);
				}
			}
		} // end of U3RU5Part.paint
		
	} // end of U3RU5Part

	class RepeatPart extends LabeledPart {
	
		final int[ ][ ] REPS;
		
		RepeatPart(String label) {
			super(label, false);
			h = sizeOf(20);
			REPS = new int[CANDIDATE.directrepeats.length][3];
			for (int i=0; i<REPS.length; i++) {
				REPS[i][0] = CANDIDATE.directrepeats[i].STARTPOS1 - FIRSTPOS;
				REPS[i][1] = CANDIDATE.directrepeats[i].STARTPOS2 - FIRSTPOS;
				REPS[i][2] = CANDIDATE.directrepeats[i].RLENGTH;
			}
			h = 3 * REPS.length;;
		} // end of RepeatPart.constructor
		
		public void paint(Graphics g) {
			int vp = 1;
			super.paint(g);
			for (int i=0; i<REPS.length; i++) {
				g.drawLine(REPS[i][0] * PIXELSPERBASE, vp, (REPS[i][0] + REPS[i][2]) * PIXELSPERBASE - 1, vp);
				g.drawLine(REPS[i][1] * PIXELSPERBASE, vp, (REPS[i][1] + REPS[i][2]) * PIXELSPERBASE - 1, vp);
				g.drawLine(REPS[i][0] * PIXELSPERBASE, vp + 1, (REPS[i][1] + REPS[i][2]) * PIXELSPERBASE - 1, vp + 1);
				if (g.getColor() == Color.black) {
					g.setColor(Color.red);
				} else if (g.getColor() == Color.red) {
					g.setColor(Color.blue);
				} else {
					g.setColor(Color.black);
				}
				vp+=3;
			}
		} // end of RepeatPart.paint
		
	} // end of RepeatPart
	
	
	class LabelList extends JCanvas {
	
		Dimension dim = new Dimension();
		
	
		LabelList(LTRCanvas parent) {
			JLabel l;
			for (int i=0; i<PARTS.length; i++) {
				l = new JLabel(PARTS[i].LABEL);
				dim.width = Math.max(dim.width, l.getWidth());
				dim.height += l.getHeight();
			}
			dim.width += 10;
		} // end of LabelList.constructor
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int hh = 0;
			int voffset = PANE.getVerticalScrollBar().getValue();
			g.translate(0, -voffset);
			for (int p=0; p<PARTS.length; p++) {
				hh += PARTS[p].h + VSPACE;
				g.drawString(PARTS[p].LABEL, 5, hh - 10);
			}
			g.drawLine(149, 0, 149, 1000);
		} // end of LabelList.paintComponent
		
		public Dimension getPreferredSize() {
			return new Dimension(150, 100);
		} // end of LabelList.getPreferredSize

		public Dimension getMinimumSize() {
			return new Dimension(100, 100);
		} // end of LabelList.getMinimumSize
		
} // end of LabelList

	class LTRCanvas extends JCanvas {
			
		Dimension dim = new Dimension();
		int sp;
		
		private LabeledPart addPart(LabeledPart part) {
			dim.width = PARTWIDTH;
			dim.height += (part.h + VSPACE);
			return part;
		}
		
		LTRCanvas() throws RetroTectorException {
			for (int p=0; p<PARTS.length; p++) {
				addPart(PARTS[p]);
				if (PARTS[p] == sequencePart) {
					sp = dim.height - VSPACE - 2;
				}
			}
		} // end of LTRCanvas.constructor
		
		public Dimension getPreferredSize() {
			return dim;
		} // end of LTRCanvas.getPreferredSize
		
		public Dimension getMinimumSize() {
			return new Dimension(100, 100);
		} // end of LTRCanvas.getMinimumSize
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			doPaint(g);
		} // end of paintComponent
		
		private void doPaint(Graphics g) {
			int h = (CANDIDATE.candidateFirst - FIRSTPOS) * PIXELSPERBASE;
			g.drawLine(h, 0, h, 1000);
			h = (CANDIDATE.hotSpotPosition - FIRSTPOS) * PIXELSPERBASE;
			g.drawLine(h, 0, h, 1000);
			h = (CANDIDATE.candidateLast - FIRSTPOS) * PIXELSPERBASE;
			g.drawLine(h, 0, h, 1000);
			g.setColor(Color.blue);
			h = (rfirst - FIRSTPOS) * PIXELSPERBASE;
			g.drawLine(h, 0, h, 1000);
			h = (rlast - FIRSTPOS) * PIXELSPERBASE;
			g.drawLine(h, 0, h, 1000);
			g.setColor(Color.black);
			for (int co=0; co<(THEDNA.TRANSLATOR.CONTIGS.length-1); co++) {
				int hh = THEDNA.TRANSLATOR.CONTIGS[co].LASTINTERNAL;
				if ((hh >= FIRSTPOS) & (hh<= LASTPOS)) {
					int coo = (THEDNA.TRANSLATOR.CONTIGS[co].LASTINTERNAL - FIRSTPOS + 1) * PIXELSPERBASE;
					Chainview.dashedVertical(g, 0, 1000, coo, 10);
					g.drawString(THEDNA.TRANSLATOR.CONTIGS[co].AFTERINSERT.INSERTIDENTIFIER, coo + 2, sp);
				}
			}
			
			for (int p=0; p<PARTS.length; p++) {
				PARTS[p].paint(g);
				g.translate(0, PARTS[p].h + VSPACE);
			}
		} // end of paint
		
	} // end of LTRCanvas
	
	
	public final int WHEIGHT;
	
	JScrollPane PANE;
	
	final LTRCanvas LTRCANVAS;
	LabelList LABELLIST;
	
	final JFrame PARENTFRAME;
	
	BaseNet u3rNet;
	BaseNet ru5Net;
	
	Hashtable ltriddataTable = new Hashtable();
	
	BaseMatrixPart tataaPart;
	BaseMatrixPart meme50Part;
	BaseNNPart u3rNetPart;
	BaseNNPart ru5NetPart;
	RfirstPart rFirstPart;
	RlastPart rLastPart;
	ModifierPart polyAPart;
	InrPart inrPart;
	BaseNNPart u5NetPart;
	BaseNNPart u3NetPart;
	TranssitesPart transsitesPart;
	SequencePart sequencePart;
	RepeatPart repeatPart;
	ModifierPart transsitePart;
	ModifierPart cpgPart;
	ModifierPart spl8Part;
	
	int rfirst = -1;
	int rlast = -1;
	
	final LabeledPart[ ] PARTS;

	
	public LTRviewPanel( LTRCandidate cand, JFrame parent, float sizefactor) throws RetroTectorException {
		SIZEFACTOR = sizefactor;
		PIXELSPERBASE = sizeOf(4);
		SMALLFONT = new Font("Courier", Font.PLAIN, sizeOf(8));
		CANDIDATE = cand;
		THEDNA = CANDIDATE.LTRCANDIDATEDNA;
		if (CANDIDATE.directrepeats.length <= 0) {
			CANDIDATE.makeDirectRepeats();
		}
		FIRSTPOS = THEDNA.forceInside(CANDIDATE.candidateFirst - LEADERLENGTH);
		LASTPOS = THEDNA.forceInside(CANDIDATE.candidateLast + TRAILERLENGTH);
		PARTWIDTH = PIXELSPERBASE * (LASTPOS - FIRSTPOS + 1);
		u3rNet = new BaseNet(database.getFile("U3R120NN.txt"));
		try{
			ru5Net = new BaseNet(database.getFile("RU5120NN.txt"));
		} catch (RetroTectorException re) {
			ru5Net = new BaseNet(database.getFile("RU5120NN3.txt"));
		}
		ParameterFileReader reader = new ParameterFileReader(database.getFile(LTRID.LTRIDDATAFILENAME), ltriddataTable);
		reader.readParameters();
		reader.close();
		setLayout(new BorderLayout());
		Stack partStack = new Stack();
		meme50Part = new BaseMatrixPart(new BaseWeightMatrix((String[ ]) ltriddataTable.get(LTRID.MEME50MATRIXKEY)), 0, "MEME50");
		partStack.push(meme50Part);
		tataaPart = new BaseMatrixPart(new BaseWeightMatrix((String[ ]) ltriddataTable.get(LTRID.TATAAMATRIXKEY)), 0, "TATAA");
		partStack.push(tataaPart);
		ru5NetPart = new BaseNNPart(ru5Net, "RU5 neural net", 0);
		partStack.push(ru5NetPart);
		inrPart = new InrPart("Inrsites");
		partStack.push(inrPart);
		u3rNetPart = new BaseNNPart(u3rNet, "U3R neural net", u3rNet.NROFBASES - 1);
		partStack.push(u3rNetPart);
		polyAPart = new ModifierPart(THEDNA.getREndModifier(), "REndModifier");
		partStack.push(polyAPart);
		sequencePart = new SequencePart("Sequence");
		partStack.push(sequencePart);
		u5NetPart = new BaseNNPart(new BaseNet(database.getFile("U5NN.txt")), "U5 neural net", 0);
		partStack.push(u5NetPart);
		u3NetPart = new BaseNNPart(new BaseNet(database.getFile("U3NN.txt")), "U3 neural net", 0);
		partStack.push(u3NetPart);
		transsitesPart = new TranssitesPart("Transcr. sites");
		partStack.push(transsitesPart);
		repeatPart = new RepeatPart("Direct repeats");
		partStack.push(repeatPart);
		transsitePart = new ModifierPart(THEDNA.getTransSiteModifier(), "TransSiteModifier");
		partStack.push(transsitePart);
		cpgPart = new ModifierPart(THEDNA.getCpGModifier(), "CpGModifier");
		partStack.push(cpgPart);
		spl8Part = new ModifierPart(THEDNA.getSplitOctamerModifier(), "SplitOctamerModifier");
		partStack.push(spl8Part);
		PARTS = new LabeledPart[partStack.size()];
		partStack.copyInto(PARTS);
		LTRCANVAS = new LTRCanvas();
		PANE = new JScrollPane(LTRCANVAS);
		PANE.getVerticalScrollBar().addAdjustmentListener(this);
		add(PANE, BorderLayout.CENTER);
		LABELLIST = new LabelList(LTRCANVAS);
		add(LABELLIST, BorderLayout.WEST);
		PARENTFRAME = parent;
		WHEIGHT = LTRCANVAS.getPreferredSize().height + 75;
		
/*
		TopFinder lastfinder = new TopFinder(rLastPart.VALUES, 2, 10);
		float xmax = -Float.MAX_VALUE;
		int maxi1 = -1;
		float x;
		int signalPos = CANDIDATE.hotSpotPosition - FIRSTPOS;
		for (int i1=Math.max(0, signalPos-110); i1<=signalPos-10; i1++) {
			x = rFirstPart.VALUES[i1] + lastfinder.maxvalue(signalPos + 10, i1 + 110);
			if (x > xmax) {
				xmax = x;
				maxi1 = i1;
			}
		}
		rfirst = maxi1;
		lastfinder.maxvalue(signalPos + 10, rfirst + 110);
		rlast = lastfinder.maxpos;
*/
		RMotif rmot = (RMotif) database.getFirstMotif("RMotif");
		rmot.setEnds(FIRSTPOS, LASTPOS);
		rmot.refresh(new Motif.RefreshInfo(THEDNA, 0, 0, null));
		MotifHit hit = rmot.getMotifHitAt(CANDIDATE.hotSpotPosition);
		if (hit != null) {
			rfirst = hit.MOTIFHITFIRST;
			rlast = hit.MOTIFHITLAST;
		}
	} // end of constructor
	
	private void showString(String s, int x, int y, Graphics g) {
		for (int i=0; i<s.length(); i++) {
			g.drawString(s.substring(i, i+1), x + i * PIXELSPERBASE, y);
		}
	} // end of showString

	int sizeOf(float x) {
		return (int) (x * SIZEFACTOR);
	} // end of sizeOf
	
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		LABELLIST.repaint();
	} // end of adjustmentValueChanged
	
}
