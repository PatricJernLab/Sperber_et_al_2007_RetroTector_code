/*
* Copyright (©) 2000-2007, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/11 -07
* Beautified 21/11 -07
*/

package retrotector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
* General window to display graphics, with JPEG, PNG and EPS save available.
*/
public class GraphicsJFrame extends CloseableJFrame implements ActionListener {

	private final JPanel THECANVAS;

	private JPanel buttonPanel = new JPanel();
	private JButton jpegButton = new JButton("Save as JPEG file");
	private JButton pngButton = new JButton("Save as PNG file");
	private JButton epsButton = new JButton("Save as EPS file at size%");
	private JTextField sizeField = new JTextField("100");
	private JScrollPane thePane; // to contain theCanvas

/**
* Constructor.
* @param	title	Title of window.
* @param	canv	The JPanel or JCanvas to display.
*/
	public GraphicsJFrame(String title, JPanel canv) {
		super(title);
		THECANVAS = canv;
		pngButton.addActionListener(this);
		buttonPanel.add(pngButton);
		epsButton.addActionListener(this);
		buttonPanel.add(epsButton);
		buttonPanel.add(sizeField);
		jpegButton.addActionListener(this);
		buttonPanel.add(jpegButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		thePane = new JScrollPane(THECANVAS);
		getContentPane().add(thePane, BorderLayout.CENTER);
		AWTUtilities.showFrame(this, new Dimension(550, 300));
	} // end of constructor(String, JPanel)

/**
* Constructor. Used only by LTRview.
* @param	title	Title of window.
* @param	canv	The JPanel or JCanvas to display.
* @param	show	Show window at once if true.
*/
	public GraphicsJFrame(String title, JPanel canv, boolean show) {
		super(title);
		THECANVAS = canv;
		pngButton.addActionListener(this);
		buttonPanel.add(pngButton);
		epsButton.addActionListener(this);
		buttonPanel.add(epsButton);
		buttonPanel.add(sizeField);
		jpegButton.addActionListener(this);
		buttonPanel.add(jpegButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		thePane = new JScrollPane(THECANVAS);
		getContentPane().add(thePane, BorderLayout.CENTER);
		if (show) {
			AWTUtilities.showFrame(this, new Dimension(300, 200));
		}
	} // end of constructor(String, JPanel, boolean)
	
/**
* Makes window visible.
* @param	dim	Dimension to set, or null.
*/
	public final void showUp(Dimension dim) {
		AWTUtilities.showFrame(this, dim);
	} // end of showUp(Dimension)

/**
* As required by ActionListener. Reacts to JPEG, PNG or EPS save button.
*/
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == jpegButton) {
			Dimension dim = THECANVAS.getPreferredSize();
			BufferedImage im = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = im.createGraphics();
			g.setClip(0, 0, dim.width, dim.height);
			Color co = g.getColor();
			g.setColor(Color.white);
			Rectangle r = g.getClipBounds();
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(co);
//			g.clearRect(0, 0, dim.width, dim.height);
			THECANVAS.paint(g);
			JPEGSaveThread th = new JPEGSaveThread(im);
			th.start();
		} else if (ae.getSource() == pngButton) {
			Dimension dim = THECANVAS.getPreferredSize();
			BufferedImage im = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = im.createGraphics();
			g.setClip(0, 0, dim.width, dim.height);
			Color co = g.getColor();
			g.setColor(Color.white);
			Rectangle r = g.getClipBounds();
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(co);
//			g.clearRect(0, 0, dim.width, dim.height);
			THECANVAS.paint(g);
			PNGSaveThread  th = new PNGSaveThread(im);
			th.start();
		} else if (ae.getSource() == epsButton) {
			EPSSaveThread ept = new EPSSaveThread(THECANVAS, Integer.parseInt(sizeField.getText()));
			ept.start();
		}
	} // end of actionPerformed(ActionEvent)
	
} // end of GraphicsJFrame
