/*
* Copyright (©) 2000-2007, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/8 -07
* Beautified 21/8 -07
*/

package retrotector;

import builtins.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

/**
* The entry class for RetroTector, and also the main window.
*/
public class RetroTector extends JFrame implements RetroTectorInterface, ActionListener {

/**
* Progress indicator with moving stripes.
*/
	private class ProgressCanvas extends JCanvas {
	
		public final int WIDTH = 34;
		public final int HEIGHT = 10;
	
		private int stage = 1; // increments with movement
		
		private ProgressCanvas() {
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			setMinimumSize(new Dimension(WIDTH, HEIGHT));
			setMaximumSize(new Dimension(WIDTH, HEIGHT));
		} // end of ProgressCanvas.constructor()
	
/**
* As required by Component.
* Draws diagonal stripes in positions determined by stage.
*/
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setClip(0, 0, WIDTH, HEIGHT);
			for (int i=1; i<HEIGHT-1; i++) {
				g.drawLine(i + stage - 16, i, i + stage - 9, i);
				g.drawLine(i + stage, i, i + stage + 7, i);
				g.drawLine(i + stage + 16, i, i + stage + 23, i);
				g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
			}
		} // end of ProgressCanvas.paintComponent(Graphics)
		
/**
* Move one step.
*/
		private final void increment() {
			stage++;
			if (stage > 16) {
				stage = 1;
			}
			repaint();
		} // end of ProgressCanvas.increment()
		
	} // end of ProgressCanvas
	

/**
* For the particular use of SweepDNA, which should be awt-independent.
*/
	private class PadDialog extends JDialog implements ActionListener {

		private JButton noButton = new JButton("No");
		private JButton yesButton = new JButton("Yes, with");
		private JTextField tfbefore = new JTextField("15000");
		private JTextField tfafter = new JTextField("15000");
		private JTextField tfa = new JTextField("25");
		private JTextField tfc = new JTextField("25");
		private JTextField tfg = new JTextField("25");
		private JTextField tfrep = new JTextField("agctcg");
		private JPanel opan = new JPanel();
		private JPanel pan = new JPanel();
		private boolean doit = false;
		
		private PadDialog(int length) {
			super(RetroTector.this, "", true);
			opan.setLayout(new GridLayout(0, 1));
			pan.setLayout(new GridLayout(0, 2));
			noButton.addActionListener(this);
			yesButton.addActionListener(this);
			pan.add(noButton);
			pan.add(yesButton);
			pan.add(tfbefore);
			pan.add(new JLabel("bases before"));
			pan.add(tfafter);
			pan.add(new JLabel("bases after"));
			pan.add(tfa);
			pan.add(new JLabel("% a"));
			pan.add(tfc);
			pan.add(new JLabel("% c"));
			pan.add(tfg);
			pan.add(new JLabel("% g"));
			pan.add(new JLabel("Add repeat:"));
			pan.add(tfrep);
			String s1 = "This DNA is rather short for RetroTector (" + length + ")";
			String s2 = "Do you want to pad it with random bases?";
			opan.add(new JLabel(s1));
			opan.add(new JLabel(s2));
			getContentPane().add(opan, BorderLayout.NORTH);
			getContentPane().add(pan, BorderLayout.CENTER);
			setLocation(new Point(200, 100));
			pack();
			setVisible(true);
		} // end of PadDialog.constructor()
		
/**
* React to buttons.
*/
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() == noButton) {
				doit = false;
			} else {
				doit = true;
			}
			setVisible(false);
		} // end of PadDialog.actionPerformed(ActionEvent)
		
	} // end of PadDialog


/**
* A window with a text area where a DNA string may be pasted
* and three buttons. Buttons allow selection of strand and clearing.
* Used primarily by RetroVID, so it can be awt-independent.
*/
	private class DNAWindow extends JDialog implements ActionListener {
		private JTextArea ta; // for the DNA String
		private JButton bu1 = new JButton("Use primary strand");
		private JButton bu2 = new JButton("Use complementary strand");
		private JButton bu4 = new JButton("Clear");
		private JPanel pa = new JPanel(); // for the buttons
	
/**
* For DNA String.
*/
		private String completeDNA = null;

/**
* For strand choice.
*/
		private boolean primarystrand = true;
			
/**
* Creates a window as specified.
*/
		private DNAWindow() throws RetroTectorException {
			
			super(RetroTector.this, "Paste DNA below", true);
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
		} // end of DNAWindow.constructor()
		
/**
* Handles events from the buttons.
*/
		public void actionPerformed(ActionEvent ae) {
		
			if (ae.getSource() == bu4) { // clear
				ta.setText("");
				completeDNA = null;
				ta.setEditable(true);
				primarystrand = true;
			} else if (ae.getSource() == bu1) {
				primarystrand = true;
			} else if (ae.getSource() == bu2) {
				primarystrand = false;
			} else {
				return;
			}
			completeDNA = ta.getText();
			setVisible(false);

			return;
		} // end of DNAWindow.actionPerformed(ActionEvent)

	} // end of DNAWindow
	
  
// public static fields
/**
* =". Copyright (©) G. O. Sperber & J. Blomberg 2000-2007".
*/
	public static final JLabel COPYRIGHTLABEL = new JLabel(RetroTectorEngine.COPYRIGHTSTRING, SwingConstants.CENTER);

/**
* ="Clear error messages".
*/
	public static final String CLEARERRORSLABEL = "Clear error messages";

/**
* ="Abort".
*/
	public static final String ABORTLABEL = "Abort";

/**
* ="Stop SweepScripts".
*/
	public static final String STOPSWEEPSCRIPTSLABEL = "Stop SweepScripts";

/**
* ="Quit".
*/
	public static final String QUITLABEL = "Quit";

	
/**
* Entry point.
* Parameters may be:
*		L:|first part of file name|	Log file name will be this + version number + .txt, if first parameter
*		|Executor name|							That executor is executed
*		quit												RetroTector quits
*		D:|directory name|					New working directory is set
*		E:|script name|							That executor script is executed
*		F:|file name|								Parameters as above are picked from file (name should be path
*														unless file is in same directory as RetroTector.jar)
* @param	strings	Array of arameters, as above
*/
	public static void main(String[ ] strings) {
		System.setProperty("apple.laf.useScreenMenuBar", "true"); // Menus at top on Mac
		try {
			RetroTectorEngine.getConfiguration();
			RetroTectorEngine.retrotector = new RetroTector(Executor.collectExecutors((String[ ]) RetroTectorEngine.configurationTable.get(RetroTectorEngine.EXECMENUKEY)));
    } catch (RetroTectorException rte) {
			System.err.println(rte.toString());
		}
		RetroTectorEngine.main(strings);
	} // end of main(String[ ])
	
/**
* Reacts to ActionEvents.
*/
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == clearErrorMenuItem) {
			errorPanel.removeAll();
			errorPane.setPreferredSize(new Dimension(0, 0));
			warningPanel.removeAll();
			warningPane.setPreferredSize(new Dimension(0, 0));
			pack();
			return;
		}
			
		if (ae.getSource() == abortMenuItem) { // abort clicked. set flag
			if (RetroTectorEngine.currentThread != null) {
				RetroTectorEngine.currentThread.abortFlag = true;
			}
			return;
		}
		
		if (ae.getSource() == stopSweepScriptsMenuItem) { // set flag
			Executor.abortSweepScripts = true;
			return;
		}
		
		if (ae.getSource() == quitMenuItem) { // quit clicked
			RetroTectorEngine.doQuit();
		}
	
		if (RetroTectorEngine.isExecuting()) {
			return; // don't react to other menus while executor is running
		}

		if (ae.getSource() == chooseScriptButton) {
			(new ZFileChooser.ZFileDialogThread(false)).start();
			return;
		}

		if (ae.getSource() == chooseDirectoryButton) {
			(new ZFileChooser.ZFileDialogThread(true)).start();
			return;
		}

		if (ae.getSource() == upDirectoryButton) {
			try {
				RetroTectorEngine.setCurrentDirectory(RetroTectorEngine.currentDirectory().getParentFile(), "swps");
			} catch (RetroTectorException re) {
			}
			return;
		}

		if (ae.getSource() == executeScriptButton) {
			RetroTectorEngine.executeScript(true);
			return;
		}

		if (ae.getSource() == viewFileButton) {
			try {
				FileViewFrame viewFrame = new FileViewFrame(RetroTectorEngine.currentScript);
			} catch (RetroTectorException rse) {
				RetroTectorEngine.displayError(rse);
			}
			return;
		}

		if (ae.getSource() instanceof JMenuItem) {
			JMenuItem mi = (JMenuItem) ae.getSource();
			Container co = mi.getParent();
			if (co == executorMenu.getPopupMenu()) {
				try {
					RetroTectorEngine.executeClass(mi.getText(), true);
				} catch (RetroTectorException rte) {
					displayerror(rte, RetroTectorEngine.ERRORLEVEL);
				}
				return;
			}
		}

	} // end of actionPerformed(ActionEvent)


/**
* Changes	the script file currently specified in main window script field.
*	@param	scriptFile	The new File.
* @param	password		Known only to SweepScripts.
* @return	True if successful.
*/
	public final boolean setscriptField(final File scriptFile, String password) {
		if (password.equals("swps")) {
			Runnable r = new Runnable() {
				public void run() {
					scriptField.setText(scriptFile.getPath());
				}
			};
			AWTUtilities.doInEventThread(r);
			return true;
		}
		return false;
	}	// end of setscriptField(File, String)
		
/**
* Displays any message in main window executor field.
* @param	s					Message to display.
* @param	password	Known only to SweepScripts.
* @return	True if successful.
*/
	public final boolean setexecutorField(final String s, String password) {
		if (password.equals("swps")) {
			Runnable r = new Runnable() {
				public void run() {
					executorField.setText(s);
				}
			};
			AWTUtilities.doInEventThread(r);
			return true;
		}
		return false;
	} // end of setexecutorField(String, String)

/**
* Changes	the directory currently specified in main window directory field.
*	@param	theDirectory	The new directory.
* @param	password			Known only to SweepScripts.
* @return	True if successful.
*/
	public final boolean setcurrentDirectory(final File theDirectory, String password) throws RetroTectorException {
		if (password.equals("swps")) {
			if (!theDirectory.isDirectory()) {
				throw new RetroTectorException("RetroTector", theDirectory.getPath(), "is not a directory");
			}
			Runnable r = new Runnable() {
				public void run() {
					directoryField.setText(theDirectory.getAbsolutePath());
				}
			};
			AWTUtilities.doInEventThread(r);
			return true;
		}
		return false;
	} // end of setcurrentDirectory(File, String)

/**
* Signal that an executor is running, or none. Also empties infoField.
* @param	ex	The executor, or null.
*/
	public final void setexecuting(final Executor ex) {
		if (ex == null) {
			Runnable r = new Runnable() {
				public void run() {
					executorMenu.setEnabled(true);
					executorField.setText(RetroTectorEngine.READYMESSAGE);
					infoField.setText("");
				}
			};
			AWTUtilities.doInEventThread(r);
		} else {
			Runnable r = new Runnable() {
				public void run() {
					executorMenu.setEnabled(false);
					executorField.setText(ex.className() + " executing");
					infoField.setText("");
				}
			};
			AWTUtilities.doInEventThread(r);
		}
	} // end of setexecuting(Executor)
	
// adds s to pa
	private final void addALabel(String s, JPanel pa) {
		JLabel la = new JLabel(s);
		Font fo = new Font("Times", Font.BOLD, 16);
		la.setFont(fo);
		la.setForeground(Color.white);
		pa.add(la);
	} // end of addALabel(String, JPanel)
	
/**
* Standard way to react to RetroTectorExceptions. 
* Displays it in the error panel.
* @param	rse		The RetroTectorException to display.
* @param	level	0=error; 1=warning; 2=notice.
*/
	public final void displayerror(RetroTectorException rse, final int level) {
		Color bgColor;
		String ostring = "ERROR!!!";
		if (level == RetroTectorEngine.WARNINGLEVEL) {
			ostring = "Warning!!";
      beep();
      Utilities.doSleep(500);
      beep();
			bgColor = Color.blue;
		} else if (level == RetroTectorEngine.NOTICELEVEL) {
			ostring = "Notice!";
			bgColor = Color.green;
		} else {
      beep();
      Utilities.doSleep(500);
      beep();
      Utilities.doSleep(500);
      beep();
			bgColor = Color.red;
    }

		final JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(0, 1));
		pan.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		pan.setBackground(bgColor);
		pan.setForeground(Color.white);
		addALabel(ostring, pan);
		for (int i=0; i<rse.getSize(); i++) {
			addALabel(rse.messagePart(i), pan);
		}
		Runnable r = new Runnable() {
			public void run() {
				if (level == RetroTectorEngine.ERRORLEVEL) {
					errorPanel.add(pan);
					errorPane.setPreferredSize(new Dimension(100, 100));
				} else {
					warningPanel.add(pan);
					warningPane.setPreferredSize(new Dimension(100, 100));
				}
				pack();
				validate();
			}
		};
		AWTUtilities.doInEventThread(r);
			
	} // end of displayerror(RetroTectorException, int)
	
	
/**
* Emit standard warning sound.
*/
	public final void beep() {
		Toolkit.getDefaultToolkit().beep();
	} // end of beep
  
/**
* @param	s	Message to display in infoField.
*/
	public final void setinfoField(final String s) {
		Runnable r = new Runnable() {
			public void run() {
				infoField.setText(s);
			}
		};
		AWTUtilities.doInEventThread(r);
	} // end of setinfoField(String)
	
	private Runnable progressShower = new Runnable() {
		public void run() {
			progCanvas.increment();
		}
	};
	
/**
* Step progress indicator.
*/
	public final void showprogress() throws RetroTectorException {
		AWTUtilities.doInEventThread(progressShower);
	} // end of showprogress()
		
/**
* Display a ParameterWindow.
*	@param	ex	The Executor whose ParameterWindow is to be shown.
* @param comp	A JComponent to add at the bottom.
*/
	public void doparameterWindow(Executor ex, Object comp) {
		JComponent jc = (JComponent) comp;
		ParameterJFrame pw = new ParameterJFrame(ex, jc);
		pw.pack();
		Dimension d = pw.getSize();
		int jdh = 0;
		if (jc != null) {
			jdh = jc.getSize().height;
		}
		pw.setSize(d.width, Math.min(300 + jdh, d.height));
		pw.setVisible(true);
	} // end of doparameterWindow(Executor, Object)

/**
* Used primarily by RetroVID, which should be awt-independent.
* Display a DNAWindow.
*	@return	The DNA input to the DNAWindow.
*/
	public DNA doDNAwindow() throws RetroTectorException {
		DNAWindow dnaw = new DNAWindow();
		DNA dna = new DNA(dnaw.completeDNA, "", dnaw.primarystrand);
		dnaw.dispose();
		return dna;
	} // end of doDNAwindow()

/**
* Shows a confirm dialog.
* @param	question	A String to show.
* @return	true if the Yes button was clicked.
*/
	public boolean doquestion(String question) {
		int rep = JOptionPane.showConfirmDialog(this, question);
		return (rep == JOptionPane.YES_OPTION);
	} // end of doquestion(String)
	
/**
* So that SweepDNA may be awt-independent.
* Shows a PadDialog.
* @param	length	Current length of DNA.
* @return	null if the No button was clicked, parameters otherwise.
*/
	public String[ ] dopadQuestion(int length) throws RetroTectorException {
		PadDialog pd = new PadDialog(length);
		if (pd.doit) {
			String[ ] result = new String[6];
			result[SweepDNA.BEFOREINDEX] = pd.tfbefore.getText();
			result[SweepDNA.AFTERINDEX] = pd.tfafter.getText();
			result[SweepDNA.APERCINDEX] = pd.tfa.getText();
			result[SweepDNA.CPERCINDEX] = pd.tfc.getText();
			result[SweepDNA.GPERCINDEX] = pd.tfg.getText();
			result[SweepDNA.REPEATINDEX] = pd.tfrep.getText();
			return result;
		} else {
			return null;
		}
	} // end of dopadQuestion(int)
	

// private fields	
// Material for window
	private JMenuBar fullmb = new JMenuBar();
	private JMenu controlMenu = new JMenu("Control");
	private JMenuItem clearErrorMenuItem = new JMenuItem(CLEARERRORSLABEL);
	private JMenuItem abortMenuItem = new JMenuItem(ABORTLABEL);
	private JMenuItem stopSweepScriptsMenuItem = new JMenuItem(STOPSWEEPSCRIPTSLABEL);
	private JMenuItem quitMenuItem = new JMenuItem(QUITLABEL);
	private JMenu executorMenu = new JMenu("Execute");

	private JTextField directoryField = new JTextField(80); // for full name of current directory
	private JButton chooseDirectoryButton = new JButton("Choose directory");
	private JButton upDirectoryButton = new JButton("Directory ^");
	private JPanel directoryButtonsPanel = new JPanel();
	private JPanel directoryPanel = new JPanel();

	private JTextField scriptField = new JTextField(80);
	private JButton chooseScriptButton = new JButton("Choose script");
	private JButton executeScriptButton = new JButton("Execute script");
	private JButton viewFileButton = new JButton("View file");
	private JPanel scriptButtonsPanel = new JPanel();
	private JPanel scriptPanel = new JPanel();

	private JPanel centerPanel = new JPanel(); // where most things are
	private Box southBox = Box.createVerticalBox(); // for error messages and warnings
	private Box warningPanel = Box.createVerticalBox(); // for warnings and messages
	private JScrollPane warningPane = new JScrollPane(warningPanel);
	private Box errorPanel = Box.createVerticalBox(); // for error messages
	private JScrollPane errorPane = new JScrollPane(errorPanel);
	private JTextField executorField = new JTextField(80); // to show what is executing
	private ProgressCanvas progCanvas = new ProgressCanvas();
	private JTextField infoField = new JTextField(80); // for info from executor
	private JPanel progPanel = new JPanel();
		
/**
* Constructor. Builds main window.
* @param	executorStack	Stack of Executor names.
*/
	private RetroTector(Stack executorStack) throws RetroTectorException {
	
// set up error panel
		warningPanel.setBackground(Color.black);
		warningPanel.setForeground(Color.white);
		southBox.add(warningPane);
		errorPanel.setBackground(Color.black);
		errorPanel.setForeground(Color.white);
		southBox.add(errorPane);
		getContentPane().add(southBox, BorderLayout.SOUTH);

// Make menus
		clearErrorMenuItem.addActionListener(this);
		controlMenu.add(clearErrorMenuItem);
		abortMenuItem.addActionListener(this);
		controlMenu.add(abortMenuItem);
		stopSweepScriptsMenuItem.addActionListener(this);
		controlMenu.add(stopSweepScriptsMenuItem);
		SweepScripts.stopSweepScriptsMenuItem = stopSweepScriptsMenuItem;
		stopSweepScriptsMenuItem.setEnabled(false);
		quitMenuItem.addActionListener(this);
		controlMenu.add(quitMenuItem);
		fullmb.add(controlMenu);

		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		getContentPane().add(COPYRIGHTLABEL, BorderLayout.NORTH); // warn rats

		directoryPanel.setLayout(new BoxLayout(directoryPanel, BoxLayout.Y_AXIS));
		directoryPanel.setBorder(BorderFactory.createMatteBorder(3, 3, 2, 3, Color.black));
		directoryField.setEditable(false);
		directoryPanel.add(directoryField);
		chooseDirectoryButton.addActionListener(this);
		directoryButtonsPanel.add(chooseDirectoryButton);
		upDirectoryButton.addActionListener(this);
		directoryButtonsPanel.add(upDirectoryButton);
		directoryPanel.add(directoryButtonsPanel);
		centerPanel.add(directoryPanel);

		scriptPanel.setLayout(new BoxLayout(scriptPanel, BoxLayout.Y_AXIS));
		scriptPanel.setBorder(BorderFactory.createMatteBorder(1, 3, 1, 3, Color.black));
		scriptField.setEditable(false);
		scriptPanel.add(scriptField);
		chooseScriptButton.addActionListener(this);
		scriptButtonsPanel.add(chooseScriptButton);
		executeScriptButton.addActionListener(this);
		scriptButtonsPanel.add(executeScriptButton);
		viewFileButton.addActionListener(this);
		scriptButtonsPanel.add(viewFileButton);
		scriptPanel.add(scriptButtonsPanel);
		centerPanel.add(scriptPanel);
		
		progPanel.setLayout(new BoxLayout(progPanel, BoxLayout.Y_AXIS));
		progPanel.setBorder(BorderFactory.createMatteBorder(2, 3, 3, 3, Color.black));
		executorField.setEditable(false);
		progPanel.add(executorField);
		Box box = Box.createHorizontalBox();
		box.add(progCanvas);
		box.add(Box.createHorizontalGlue());
		progPanel.add(box);
		infoField.setEditable(false);
		progPanel.add(infoField);
		centerPanel.add(progPanel);
		
		getContentPane().add(centerPanel, BorderLayout.CENTER);
	
		for (int i=0; i<executorStack.size(); i++) {
			String ms = (String) executorStack.elementAt(i);
			JMenuItem mi = new JMenuItem(ms);
			mi.addActionListener(this);
			executorMenu.add(mi);
		}
		executorStack = null; // throw to garbage collector
		Utilities.outputString("Adding Executor menu");
		fullmb.add(executorMenu);
		
		setJMenuBar(fullmb);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(50, 50);
		Utilities.outputString("Packing");
		AWTUtilities.showFrame(this, null);
	} // end of constructor(Stack)

} // end of RetroTector
