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
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
* Window to view and edit a text file in.
*/
public class FileViewFrame extends CloseableJFrame implements ActionListener {

	private String fileN;
	private JButton saveButton = new JButton("Save");
	private JTextArea tf;
	
/**
* Constructor.
* @param	f	The file to view.
*/
	public FileViewFrame(File f) throws RetroTectorException {
		super(f.getName());
		fileN = f.getPath();
		BufferedReader theReader = null;
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			if (f instanceof ZFile) {
				ZFile zf = (ZFile) f;
				theReader = new BufferedReader(new InputStreamReader(zf.ZIPFILE.getInputStream(zf.ZIPENTRY)));
			} else {
				theReader = new BufferedReader(new FileReader(f));
			}
			line = theReader.readLine();
			while (line != null) {
				sb.append(line);
				sb.append('\n');
				line = theReader.readLine();
			}
			theReader.close();
		} catch (Exception e) {
			RetroTectorException.sendError(this, "Trouble reading from", fileN);
		}
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("");
		JMenuItem item = new JMenuItem("Save");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		item.addActionListener(this);
		menu.add(item);
		mb.add(menu);
		setJMenuBar(mb); // connect cmd-S to Save operation
		tf = new JTextArea(sb.toString(), 10, 50);
		if (RetroTectorEngine.isMacOS()) { // activate cmd-V, cmd-X and cmd-C
			Hashtable actions = new Hashtable();
			Action[] actionsArray = tf.getActions();
			for (int i = 0; i < actionsArray.length; i++) {
				Action act = actionsArray[i];
				actions.put(act.getValue(Action.NAME), act);
			}
			Keymap map = tf.addKeymap("MyBindings", tf.getKeymap());
			KeyStroke cmdV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.META_MASK);
			Action paste = (Action) actions.get(DefaultEditorKit.pasteAction);
			map.addActionForKeyStroke(cmdV, paste);
			KeyStroke cmdX = KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.META_MASK);
			Action cut = (Action) actions.get(DefaultEditorKit.cutAction);
			map.addActionForKeyStroke(cmdX, cut);
			KeyStroke cmdC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.META_MASK);
			Action copy = (Action) actions.get(DefaultEditorKit.copyAction);
			map.addActionForKeyStroke(cmdC, copy);
			tf.setKeymap(map);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK));
		}
		
		getContentPane().add(new JScrollPane(tf), BorderLayout.CENTER);
		saveButton.addActionListener(this);
		getContentPane().add(saveButton, BorderLayout.SOUTH);
		AWTUtilities.showFrame(this, null);
	} // end of constructor(File)

/**
* Saves file if Save button clicked.
*/
	public void actionPerformed(ActionEvent ae) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileN)));
			bw.write(tf.getText());
			bw.close();
		} catch (Exception e) {
			RetroTectorEngine.displayError(new RetroTectorException(Utilities.className(this), "Trouble saving to", fileN));
		}
	} // end of actionPerformed(ActionEvent)
	
} // end of FileViewFrame
