/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
* Window to adjust parameters before running an Executor.
*/
public class ParameterJFrame extends JDialog implements ActionListener, FocusListener {
	
/**
* Utility class: a text field linked to a key.
*/
	class LTextField extends JTextField {

/**
*	Key identifying this field.
*/
		final String KEY;
		
/**
* Constructor.
* @param	contents	Text to show.
* @param	k					Key identifying this field.
*/
		LTextField(String contents, String k) {
			super(contents);
			KEY = k;
		} // end of LTextField.constructor(String, String)
		
	} // end of LTextField
	
/**
* Utility class: a window showing an explanation.
*/
	class EDialog extends JDialog implements ActionListener {
	
    private JButton closeButton = new JButton("Close");

/**
* Constructor.
* @param	parent				A Dialog as required by Dialog.
* @param	explanations	Table of explanations.
* @param	key						The key to explain.
*/
		EDialog(JDialog parent, Hashtable explanations, String key) {
			super(parent, key);
			String e = (String) explanations.get(key);
			getContentPane().add(new JLabel(e), BorderLayout.CENTER);
			closeButton.addActionListener(this);
			getContentPane().add(closeButton, BorderLayout.SOUTH);
			pack();
			setVisible(true);
		} // end of EDialog.constructor(JDialog, Hashtable, String)
		
/**
* Kill this.
* @param	ae	Can only be from Close button.
*/
		public void actionPerformed(ActionEvent ae) {
			setVisible(false);
			dispose();
		} // end of EDialog.actionPerformed(ActionEvent)

	} // end of EDialog
			
	private JPanel centerPanel = new JPanel(); // all-containing panel
	private JPanel parameterPanel = new JPanel(); // panel for parameters
	
	private JPanel newParameterPanel = new JPanel(); // for creating new parameters
	private JLabel keyLabel = new JLabel("Key");
	private JTextField keyField = new JTextField(20); // field for new key
	private JLabel valueLabel = new JLabel("Value");
	private JTextField valueField = new JTextField(20); // field for new value
	private JButton newParameterButton = new JButton("Add parameter");

	private JPanel controlPanel = new JPanel(); // for Select a file, Run and Cancel buttons
	private JButton selectFileButton = new JButton("Select a file");
	private JButton runButton = new JButton("Run");
	private JButton cancelButton = new JButton("Cancel");
	
	private Executor executor;
	
	private LTextField focusField = null;
	
/**
* Constructor.
* @param exec	The Executor for whom this window works.
*/
	public ParameterJFrame(Executor exec) {
		this(exec, null);
	} // end of constructor(Executor)
	
/**
* Constructor.
* @param exec	The Executor for whom this window works.
* @param comp	A JComponent to add at the bottom.
*/
	public ParameterJFrame(Executor exec, JComponent comp) {	
		super((Frame) RetroTectorEngine.retrotector, "Parameters for " + exec.className() + " " + exec.version(), true);
		executor = exec;
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		if (comp != null) {
			centerPanel.add(comp);
		}

// make parameterPanel
		parameterPanel.setLayout(new GridLayout(0, 2)); // keep everything in two columns
		String thiskey;
		String thisvalue;
		JButton b;
		LTextField tf;
// add a button and a textfield for each parameter
		if (executor.orderedkeys != null) {
			for (int k=0; k<executor.orderedkeys.size(); k++) {
				thiskey = (String) executor.orderedkeys.elementAt(k);
				thisvalue = (String) executor.parameters.get(thiskey);
				if (thisvalue == null) {
					thisvalue = "";
				}
				tf = new LTextField(thisvalue, thiskey);
				tf.addFocusListener(this);
				b = new JButton(thiskey);
				b.addActionListener(this);
				parameterPanel.add(b);
				parameterPanel.add(tf);
			}
		}

// make newParameterPanel
		newParameterPanel.add(keyLabel);
		newParameterPanel.add(keyField);
		newParameterPanel.add(valueLabel);
		newParameterPanel.add(valueField);
		newParameterButton.addActionListener(this);
		newParameterPanel.add(newParameterButton);
		centerPanel.add(newParameterPanel);

// make controlPanel
		selectFileButton.addActionListener(this);
		controlPanel.add(selectFileButton);
		cancelButton.addActionListener(this);
		controlPanel.add(cancelButton);
		runButton.addActionListener(this);
		controlPanel.add(runButton);
		centerPanel.add(controlPanel);
		
		getContentPane().add(centerPanel, BorderLayout.SOUTH);
		JScrollPane sc = new JScrollPane(parameterPanel);
		getContentPane().add(sc, BorderLayout.CENTER);

		getRootPane().setDefaultButton(runButton);
		
	} // end of constructor(Executor, JComponent)
	
/**
* As required by ActionListener.
*/
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() instanceof JButton) {
// abort command
			if (ae.getSource() == cancelButton) {
				executor.runFlag = false;
				setVisible(false);
				dispose();
			} else if (ae.getSource() == runButton) {
// update parameters and run
				Component[ ] cs = parameterPanel.getComponents();
				for (int c=0; c<cs.length; c++) {
					if (cs[c] instanceof LTextField) {
						LTextField l = (LTextField) cs[c];
						executor.parameters.put(l.KEY, l.getText());
					}
				}
				executor.runFlag = true;
				setVisible(false);
				dispose();
			} else if (ae.getSource() == selectFileButton) {
				ZFileChooser.ZFileDialog zd = new ZFileChooser.ZFileDialog(RetroTectorEngine.currentDirectory(), false);
				if (zd.getChosenFile() != null) {
					String s = zd.getChosenFile().getName();
					if (focusField != null) {
						focusField.setText(s);
					}
				}
			} else if (ae.getSource() == newParameterButton) {
// add new parameter
				executor.parameters.put(keyField.getText(), valueField.getText());
				parameterPanel.add(new JLabel(keyField.getText()));
				parameterPanel.add(new JTextField(valueField.getText()));
				pack();
			} else {
// show explanation
				JButton b = (JButton) ae.getSource();
				EDialog ed = new EDialog(this, executor.explanations, b.getText());
				ed.setLocation(100, 100);
			}
		}
	} // end of actionPerformed
	
/**
* As required by FocusListener. The TextField generating the Event is memorized.
*/
	public void focusGained(FocusEvent e) {
		if (e.getSource() instanceof LTextField) {
			focusField = (LTextField) e.getSource();
		}
	} // end of focusGained(FocusEvent)
	
/**
* As required by FocusListener.
*/
	public void focusLost(FocusEvent e) {
	}

} // end of ParameterJFrame
