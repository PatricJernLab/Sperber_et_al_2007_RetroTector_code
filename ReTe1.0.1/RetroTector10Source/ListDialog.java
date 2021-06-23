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
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

/**
* Primarily used by Genomeview.
* Shows a list of Strings and allows reordering and selection.
*/
public class ListDialog extends JDialog implements ActionListener, ListSelectionListener {

	private JList theList;
	private JButton toTopButton = new JButton("Move selection to top");
	private JButton selectAllButton = new JButton("Select all");
	private JButton exitButton = new JButton("Return selection");
	private JButton cancelButton = new JButton("Cancel");
	private Box buttonBox = Box.createVerticalBox();
	
	private DefaultListModel listModel = new DefaultListModel();
	
	private String[ ] result = null;
	
/**
* Constructor if you want multiple interval selection.
* @param		owner			Calling Frame.
* @param		inStrings	Array of Strings to manipulate.
*/
	public ListDialog(Frame owner, String[ ] inStrings) {
		this(owner, inStrings, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	} // end of constructor(Frame, String[ ])
	
/**
* Constructor.
* @param		owner					Calling Frame.
* @param		inStrings			Array of Strings to manipulate.
* @param		selectionMode	Selection chooser according to ListSelectionModel.
*/
	public ListDialog(Frame owner, String[ ] inStrings, int selectionMode) {
	
		super(owner, true);
		Container rootPane = getContentPane();
		rootPane.setLayout(new BoxLayout(rootPane, BoxLayout.X_AXIS));
		
		for (int i=0; i<inStrings.length; i++) {
			listModel.addElement(inStrings[i]);
		}
		theList = new JList(listModel);
		theList.setSelectionMode(selectionMode);
		theList.addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(theList);
		rootPane.add(scrollPane);
		
		toTopButton.addActionListener(this);
		buttonBox.add(toTopButton);
		selectAllButton.addActionListener(this);
		buttonBox.add(selectAllButton);
		exitButton.setEnabled(false);
		exitButton.addActionListener(this);
		buttonBox.add(exitButton);
		cancelButton.addActionListener(this);
		buttonBox.add(cancelButton);
		rootPane.add(buttonBox);

		pack();
		setVisible(true);
		
	} // end of constructor(Frame, String[ ], int)
	
/**
* @return		Array of selected Strings.
*/
	public final String[ ] getResult() {
		return result;
	} // end of getResult()

/**
* As required by ActionListener.
*/
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == toTopButton) {
			Object[ ] o = theList.getSelectedValues();
			for (int i=0; i<o.length; i++) {
				listModel.removeElement(o[i]);
			}
			for (int i=0; i<o.length; i++) {
				listModel.insertElementAt(o[i], i);
			}
		} else if (ae.getSource() == selectAllButton) {
			theList.setSelectionInterval(0, listModel.getSize() - 1);
		} else if (ae.getSource() == exitButton) {
			Object[ ] o = theList.getSelectedValues();
			result = new String[o.length];
			for (int i=0; i<o.length; i++) {
				result[i] = (String) o[i];
			}
			setVisible(false);
		} else if (ae.getSource() == cancelButton) {
			result = null;
			setVisible(false);
		}
	} // end of actionPerformed(ActionEvent)

/**
* As required by ListSelectionListener.
*/
	public void valueChanged(ListSelectionEvent e) {
		if (theList.getSelectedIndices().length > 0) {
			exitButton.setEnabled(true);
		} else {
			exitButton.setEnabled(false);
		}
	} // end of valueChanged(ListSelectionEvent)
	
}
