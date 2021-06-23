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

/**
* Window which will be discarded when close box is clicked.
*/
public class CloseableJFrame extends JFrame {
	
/**
* Constructor.
* @param	s	Title of window.
*/
	public CloseableJFrame(String s) {
		super(s);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	} // end of constructor(String)

}