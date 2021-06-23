/*
* Copyright (©) 2000-2005, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 18/4 -05
* Beautified 18/4 -05
*/

package retrotector;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
* A window with a text area where a DNA string may be pasted
* and two buttons. Buttons allow selection of strand and clearing.
* Used by RetroVID.
*/
public class DNAWindow extends JDialog implements ActionListener {
	private JTextArea ta; // for the DNA String
	private JButton bu1 = new JButton("Search in primary strand");
	private JButton bu2 = new JButton("Search in complementary strand");
	private JButton bu4 = new JButton("Clear");
	private JPanel pa = new JPanel(); // for the buttons
	
/**
* For DNA String.
*/
	public String completeDNA = null;

/**
* For strand choice.
*/
	public boolean primarystrand = true;
			
/**
* Creates a window as specified.
*/
 	public DNAWindow() throws RetroTectorException {
		
		super((Frame) RetroTectorEngine.retrotector, "Paste DNA below", true);
		ta = new JTextArea(20,60 );
		getContentPane().add(ta, BorderLayout.CENTER);
		bu1.addActionListener(this);
		pa.add(bu1);
		bu2.addActionListener(this);
		pa.add(bu2);
		bu4.addActionListener(this);
		pa.add(bu4);
		getContentPane().add(pa, BorderLayout.SOUTH);
		if (RetroTectorEngine.isMacOS()) { // activate cmd-V
			Hashtable actions = new Hashtable();
			Action[] actionsArray = ta.getActions();
			for (int i = 0; i < actionsArray.length; i++) {
				Action act = actionsArray[i];
				actions.put(act.getValue(Action.NAME), act);
			}
			Keymap map = ta.addKeymap("MyBindings", ta.getKeymap());
			KeyStroke cmdV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.META_MASK);
			Action paste = (Action) actions.get(DefaultEditorKit.pasteAction);
			map.addActionForKeyStroke(cmdV, paste);
			ta.setKeymap(map);
		}
    setLocation(100, 100);
		AWTUtilities.showFrame(this, null);
  } // end of constructor
  
/**
* Handles events from the buttons.
*/
 	public void actionPerformed(ActionEvent ae) {
	
		if (ae.getSource() == bu4) { // clear
			ta.setText("");
			completeDNA = null;
			ta.setEditable(true);
			primarystrand = true;
			return;
		}
		
		if (ae.getSource() == bu1) {
			primarystrand = true;
		} else if (ae.getSource() == bu2) {
			primarystrand = false;
		} else {
			return;
		}
		completeDNA = ta.getText();
		setVisible(false);

		return;
	} // end of actionPerformed

} // end of DNAWindow
