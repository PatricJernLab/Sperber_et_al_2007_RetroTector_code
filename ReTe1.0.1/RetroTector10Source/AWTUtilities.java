/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 18/9 -06
* Beautified 18/9 -06
*/

package retrotector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.lang.reflect.*;

/**
* Various methods and fields of general interest using GUI.
*/
public class AWTUtilities {

/**
* To draw several Strings so that they do not crash.
*/
	public static class MultiLiner {
	
		private String[ ] strings; // strings drawn
		private int[ ] lines; // lines of strings
		private int[ ] lasts; // terminating positions of strings
		private int startY; // Y coordinate in g for first line
		private Graphics g; // Graphics to draw in
		private Font font; // Font of g
		private FontMetrics fontM; // FontMetrics of g
		private int lineOffset; // Height of fontM
		private int size = 0; // number of lines currently in use
		
/**
* Constructor.
* @param	maxnr	Maximum number of Strings to draw.
* @param	y			Y coordinate in gr for first line.
* @param	gr		Graphics to draw in.
*/
		public MultiLiner(int maxnr, int y, Graphics gr) {
			strings = new String[maxnr];
			lines = new int[maxnr];
			lasts = new int[maxnr];
			startY = y;
			g = gr;
			font = g.getFont();
			fontM = g.getFontMetrics();
			lineOffset = fontM.getHeight();
		} // end of MultiLiner.constructor(int, int, Graphics)
		
/**
* Draws a string in most suitable line.
* @param	s	The String to draw.
* @param	x	Horizontal position to draw in. Shoušd be >= previous x.
*/
		public final void enterString(String s, int x) {
			if (size == 0) {
				g.drawString(s, x, startY);
				strings[0] = s;
				lines[0] = 0;
				lasts[0] = x + SwingUtilities.computeStringWidth(fontM, s) + 1;
				size++;
				return;
			} else {
				for (int li=0;;li++) { // try lines from top
					boolean found = false;
					for (int i=0; i<size; i++) {
						if ((lines[i] == li) & (lasts[i] > x)) {
							found = true; // String in line i crashes
						}
					}
					if (!found) { // line li is free
						g.drawString(s, x, startY + li * lineOffset);
						strings[size] = s;
						lines[size] = li;
						lasts[size] = x + SwingUtilities.computeStringWidth(fontM, s) + 1;
						size++;
						return;
					}
				}
			}
		} // end of MultiLiner.enterString(String, int)
		
	}  // end of MultiLiner

/*
* To show a window thread-safely
*/
	private static class FrameShower implements Runnable {
	
		private final Window frame;
		private final Dimension size;
		
		private FrameShower(Window frame, Dimension size) {
			this.frame = frame;
			this.size = size;
		} // end of FrameShower.constructor(Window, Dimension)
		
		public void run() {
			frame.pack();
			if (size != null) {
				frame.setSize(size);
			}
			frame.setVisible(true);
		} // end of FrameShower.run()
		
	} // end of FrameShower

/**
* A red button with the label "Comment". Shows the comment on clicking.
*/
	public static class CommentButton extends JButton implements ActionListener {
	
		private String comment;
	
/**
* Constructor.
* @param	commentText	The comment to show.
*/
		public CommentButton(String commentText) {
			super("Comment");
			addActionListener(this);
			setForeground(Color.red);
			comment = commentText;
		} // end of CommentButton.constructor(String)

/**
* Shows the comment.
*/
		public void actionPerformed(ActionEvent ae) {
			showStringInFrame(comment, "Comment");
		} // end of CommentButton.actionPerformed
		
	} // end of CommentButton

/**
* To show graphics in a GraphicsJFrame, from which it may be saved.
*/
	public static class SaveGraphicsButton extends JButton implements ActionListener {
	
		private JCanvas comp;
	
/**
* Constructor.
* @param	c	A JCanvas which defines what is to be saved.
*/
		public SaveGraphicsButton(JCanvas c) {
			super("Save graphics as file");
			addActionListener(this);
			comp = c;
		} // end of SaveGraphicsButton.constructor(JCanvas)

/**
* Opens the GraphicsJFrame.
*/
		public void actionPerformed(ActionEvent ae) {
			int h = comp.getHeight();
			int w = comp.getWidth();
			GraphicsJFrame gfr = new GraphicsJFrame("Save form", comp);
		} // end of SaveGraphicsButton.actionPerformed
		
	} // end of SaveGraphicsButton

/**
* @param	col		A Color.
* @param	alpha	Alpha 0 - 255.
* @return	col with the required alpha.
*/
	public final static Color	setAlpha(Color col, int alpha) {
		return new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
	} // end of setAlpha(Color, int)
	
/**
* Executes in event thread and waits for completion.
* @param	toRun	A Runnable whose run() will be executed.
*/
	public final static void doInEventThread(Runnable toRun) {
	
		if (EventQueue.isDispatchThread()) {
			toRun.run();
		} else {
			try {
				EventQueue.invokeAndWait(toRun);
			} catch (InterruptedException ie) {
			} catch (InvocationTargetException ite) {
			}
		}
		
	} // end of doInEventThread(Runnable)

/**
* Shows a String in a simple window.
* @param	s			The String to show.
* @param	title	Title for the window.
*/
	public final static void showStringInFrame(String s, String title) {
		JTextArea ar = new JTextArea();
		ar.setLineWrap(true);
		ar.setText(s);
		CloseableJFrame frame = new CloseableJFrame(title);
		frame.getContentPane().add(new JScrollPane(ar), BorderLayout.CENTER);
		showFrame(frame, new Dimension(300, 200));
	} // end of showStringInFrame(String, String)
	
/**
* Shows a String in a simple window.
* @param	s			The String to show.
* @param	title	Title for the window.
* @param	where	Point for upper left corner.
*/
	public final static void showStringInFrame(String s, String title, Point where) {
		JTextArea ar = new JTextArea();
		ar.setLineWrap(true);
		ar.setText(s);
		CloseableJFrame frame = new CloseableJFrame(title);
		frame.getContentPane().add(new JScrollPane(ar), BorderLayout.CENTER);
    frame.setLocation(where);
		showFrame(frame, new Dimension(300, 200));
	} // end of showStringInFrame(String, String, Point)
	
/**
* Utility method to show a Frame thread-safe.
* @param	frame	The Frame to show.
* @param	size	The size to set for the Frame, or null.
*/
	public final static void showFrame(Window frame, Dimension size) {
		Runnable runner = new FrameShower(frame, size);
		EventQueue.invokeLater(runner);
	} // end of showFrame(Window, Dimension)
	
/**
* Utility method to validate a Container thread-safe.
* @param	container	The Container to validate.
*/
	public final static void validateContainer(final Container container) {
		Runnable r = new Runnable() {
			public void run() {
				container.validate();
			}
		};
		AWTUtilities.doInEventThread(r);
	} // end of validateContainer(Container)
	
/**
* Draws a diagonal line inside a Rectangle.
* @param	g				The Graphics to draw in.
* @param	rect		The Rectangle to crosshatch
* @param	col			The Color to draw with
* @param	startat	The start point.
* @param	downlef	Draw downleft if true.
*/
	public final static void diagonalInRect(Graphics g, Rectangle rect, Color col, Point startat, boolean downlef) {
	
		if (!rect.contains(startat)) {
			return;
		}
		g.setColor(col);
		int len;
		if (downlef) {
			len = Math.min(rect.y + rect.height - startat.y, startat.x - rect.x);
			g.drawLine(startat.x, startat.y, startat.x - len, startat.y + len);
		} else {
			len = Math.min(rect.y + rect.height - startat.y, rect.x + rect.width - startat.x);
			g.drawLine(startat.x, startat.y, startat.x + len, startat.y + len);
		}
	} // end of diagonalInRect(Graphics, Rectangle, Color, Point, boolean)

/**
* Crosshatches a rectangle.
* @param	g				The Graphics to draw in.
* @param	rect		The Rectangle to crosshatch
* @param	col			The Color to draw with
* @param	spacing	The horizontal distance between lines.
*/
	public final static void hatchRect(Graphics g, Rectangle rect, Color col, int spacing) {
		for (int i=0; i<rect.width; i+=spacing) {
			diagonalInRect(g, rect, col, new Point(rect.x + i, rect.y), true);
			diagonalInRect(g, rect, col, new Point(rect.x + i, rect.y), false);
		}
		for (int i=spacing; i<=rect.height; i+=spacing) {
			diagonalInRect(g, rect, col, new Point(rect.x, rect.y + i), false);
		}
		for (int i=spacing-(rect.width % spacing); i<=rect.height; i+=spacing) {
			diagonalInRect(g, rect, col, new Point(rect.x + rect.width - 1, rect.y + i), true);
		}
		
	} // end of hatchRect(Graphics, Rectangle, Color, int)
	
} // end of AWTUtilities