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

import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;

/**
* Proprietary FileChooser which can go inside .zip files.
*/
public class ZFileChooser extends JPanel implements ActionListener, MouseListener {

/**
* A Dialog showing a ZFileChooser.
*/
	public static class ZFileDialog extends JDialog implements ActionListener {
		
		private ZFileChooser chooser;
		private File chosenFile = null;
	
/***
* Constructor.
* @param	direc	The directory to show initially.
* @param	di		If true, show only directories.
*/
		public ZFileDialog(File direc, boolean di) {
			super((Frame) RetroTectorEngine.retrotector, true);
			if (di) {
				chosenFile = direc;
			}
			chooser = new ZFileChooser(this);
			chooser.setDirectory(direc, di);
			getContentPane().add(chooser);
			pack();
			setLocation(200, 200);
			setVisible(true);
		} // end of ZFileDialog.constructor(File, boolean)
		
/**
* @return	chosenFile.
*/
		public final File getChosenFile() {
			return	chosenFile;
		} // endf of  ZFileDialog.getChosenFile()


/**
* Closes this, possiblt setting chosenFile.
*/
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() == chooser.okButton) {
				chosenFile = chooser.getSelectedFile();
			}
			setVisible(false);
		} // end of ZFileDialog.actionPerformed
		
	} // end of ZFileDialog

/**
* For RetroTector to show a ZFileDialog thread-safely.
*/
	public static class ZFileDialogThread extends Thread {
	
		private boolean dir;
		
/***
* Constructor.
* @param	d	If true, show only directories.
*/
		ZFileDialogThread(boolean d) {
			dir = d;
		} // end of ZFileChooser.ZFileDialogThread.constructor(boolean)
	
/**
* Show.
*/
		public void run() {
			ZFileDialog zd = new ZFileDialog(RetroTectorEngine.currentDirectory(), dir);
			try {
				if (dir) {
					if (zd.chosenFile != null) {
						RetroTectorEngine.setCurrentDirectory(zd.chosenFile, "swps");
					}
				} else {
					if (zd.chosenFile != null) {
						RetroTectorEngine.setScriptField(zd.chosenFile, "swps");
					}
				}
			} catch (RetroTectorException rte) {
			}
		} // end of ZFileChooser.ZFileDialogThread.run
		
	} // end of ZFileChooser.ZFileDialogThread


/**
* "Select" button.
*/
	public JButton okButton = new JButton("Select");

/**
* "Cancel" button.
*/
	public JButton cancelButton = new JButton("Cancel");

	private JButton dirUpButton = new JButton("Directory^");
	private JButton gotoDirButton = new JButton("Go to directory");
	private JTextField dirPathField = new JTextField();
	private DefaultListModel listModel = new DefaultListModel();
	private JList fileList = new JList(listModel);
	private ActionListener listener;
	
	private File currentDirectory;
	private boolean showDirs;
	
	
/**
* Constructor.
* @param	exitListener	ActionListener to be notified when enough.
*/
	public ZFileChooser(ActionListener exitListener) {
		super();
		listener = exitListener;
		setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(gotoDirButton);
		gotoDirButton.addActionListener(this);
		buttonPanel.add(dirUpButton);
		dirUpButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(listener);
		buttonPanel.add(okButton);
		okButton.addActionListener(listener);
		add(buttonPanel, BorderLayout.SOUTH);
		dirPathField.setEditable(true);
		add(dirPathField, BorderLayout.NORTH);
		fileList.addMouseListener(this);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(fileList);
		sp.setViewportBorder(new LineBorder(Color.black));
		add(sp, BorderLayout.CENTER);
		setPreferredSize(new Dimension(500, 500));
	} // end of constructor(ActionListener)
	
	private final boolean isZip(File f) {
		String s = f.getName();
		return s.endsWith(".zip") | s.endsWith(".ZIP") | s.endsWith(".zip/") | s.endsWith(".ZIP/");
	} // end of isZip(File)

/***
* @param	directory	The directory to show.
* @param	dirs			If true, show only directories.
* @return	True if successful.
*/
	public final boolean setDirectory(File directory, boolean dirs) {
		if (!directory.isDirectory()) {
			return false;
		}
		dirUpButton.setEnabled(dirs);
		Stack st = new Stack();
		File[ ] ff = directory.listFiles();
		for (int fi=0; fi<ff.length; fi++) {
			if (dirs) {
				if (ff[fi].isDirectory() | isZip(ff[fi])) {
					st.push(ff[fi].getName());
				}
			} else {
				if (!ff[fi].isDirectory() & !isZip(ff[fi])) {
					st.push(ff[fi].getName());
				}
			}
		}
		listModel.removeAllElements();
		for (int i=0; i<st.size(); i++) {
			listModel.addElement((String) st.elementAt(i));
		}
		currentDirectory = directory;
		showDirs = dirs;
		dirPathField.setText(currentDirectory.getAbsolutePath());
		return true;
	} // end of setDirectory(File, boolean)

/***
* Constructor.
* @param	directory	The directory to show if it contains anything.
* @param	dirs			If true, show only directories.
* @return	True if successful.
*/
	public final boolean tryDirectory(File directory, boolean dirs) {
		if (!directory.isDirectory()) {
			return false;
		}
		dirUpButton.setEnabled(dirs);
		Stack st = new Stack();
		File[ ] ff = directory.listFiles();
		for (int fi=0; fi<ff.length; fi++) {
			if (dirs) {
				if (ff[fi].isDirectory() | isZip(ff[fi])) {
					st.push(ff[fi].getName());
				}
			} else {
				if (!ff[fi].isDirectory() & !isZip(ff[fi])) {
					st.push(ff[fi].getName());
				}
			}
		}
		if (st.size() > 0) {
			listModel.removeAllElements();
			for (int i=0; i<st.size(); i++) {
				listModel.addElement((String) st.elementAt(i));
			}
			currentDirectory = directory;
			showDirs = dirs;
			dirPathField.setText(currentDirectory.getAbsolutePath());
			return true;
		} else {
			return false;
		}
	} // end of tryDirectory(File, boolean)
	
/**
* As required by ActionListener.
*/
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == gotoDirButton) {
			File f = new File(dirPathField.getText());
			if (f.exists() && f.isDirectory()) {
				setDirectory(f, showDirs);
			} else {
				Toolkit.getDefaultToolkit().beep();
				Utilities.doSleep(500);
				Toolkit.getDefaultToolkit().beep();
				Utilities.doSleep(500);
				Toolkit.getDefaultToolkit().beep();
			}
		} else if (ae.getSource() == dirUpButton) {
			File di = currentDirectory.getParentFile();
			if (di != null) {
				setDirectory(di, showDirs);
			}
		}
	} // end of actionPerformed
	
/**
* Handles double clicks.
*/
	public void mouseClicked(MouseEvent e) {
		int index = fileList.locationToIndex(e.getPoint());
		fileList.setSelectedIndex(index);
		File fi = getSelectedFile();
		if (isZip(fi)) {
			okButton.setEnabled(false);
		} else {
			okButton.setEnabled(true);
		}
		if (e.getClickCount() >= 2) {
			if (!tryDirectory(fi, showDirs)) {
				okButton.doClick();
			}
		}
	} // end of mouseClicked
	
/**
* @return	The selected File or ZFile.
*/
	public File getSelectedFile() {
		String sel = (String) fileList.getSelectedValue();
		if ((currentDirectory == null) | (sel == null)) {
			return null;
		}
		File fr = new File(currentDirectory, sel);
		if (isZip(fr) | (currentDirectory instanceof ZFile)) {
			try {
				fr = new ZFile(currentDirectory, sel);
			} catch (RetroTectorException rte) {
				RetroTectorEngine.displayError(rte);
			}
		}
		return fr;
	} // end of getSelectedFile()

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
	
} // end of ZFileChooser
