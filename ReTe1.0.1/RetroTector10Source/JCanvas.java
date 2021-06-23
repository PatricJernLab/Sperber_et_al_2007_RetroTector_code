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

import javax.swing.*;
import java.awt.*;

/**
* JPanel with white background.
*/
public class JCanvas extends JPanel {

/**
* Whitewashes the clip area.
* @param	g	Graphics as required.
*/
	public void paintComponent(Graphics g) {
		Color co = g.getColor();
		g.setColor(Color.white);
		Rectangle r = g.getClipBounds();
		g.fillRect(r.x, r.y, r.width, r.height);
		g.setColor(co);
	} // end of paintComponent(Graphics)
	
} // end of JCanvas