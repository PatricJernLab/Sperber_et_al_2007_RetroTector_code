/*
* Copyright (©) 2000-2008, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 23/4 2008
* Beautified 15/12 -06
*/
package builtins;

import retrotector.*;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
* Executor which collects results into a MySQL database.
*<PRE>
*     Parameters:
*
*   Generation
* The file generation to fetch data from.
* Default: 1
*
*   MinChainScore
* Chains with lower score than this are ignored
* Default: 250
* 
*   MinProcessScore
* Chains with lower score than this are not fully processed
* Default: 300
*
*   MinLTRPairScore
* LTR candidates with lower PairFactor than this are ignored
* Default: 0
* 
*		MultiPuteins
* If Yes, more than one putein of the same gene may be associated with a Chain.
* Default: Yes
*</PRE>
*/
public class CollectGenome extends SQLUser implements Utilities.FileTreater, ActionListener {

/**
* To aid identify identical candidates.
*/
	public static class LTRSummary {
	
/**
* From candidateFirst.
*/
		final int FIRST;
	
/**
* From candidateLast.
*/
		final int LAST;
	
/**
* From hotSpotPosition.
*/
		final int HOTSPOT;
	
/**
* From candidateFactor.
*/
		final float SCORE;

		int id = 0;
	
/**
* @param	cand	LTRCandidate to identify.
*/
		public LTRSummary(LTRCandidate cand) {
			FIRST = cand.LTRCANDIDATEDNA.externalize(cand.candidateFirst);
			LAST = cand.LTRCANDIDATEDNA.externalize(cand.candidateLast);
			HOTSPOT = cand.LTRCANDIDATEDNA.externalize(cand.hotSpotPosition);
			SCORE = cand.candidateFactor;
		} // end of LTRSummary.constructor(LTRCandidate)
		
/**
* @param	info	MotifHitGraphInfo to identify.
*/
		public LTRSummary(MotifHitGraphInfo info, DNA dna) {
			FIRST = dna.externalize(info.FIRSTPOS);
			LAST = dna.externalize(info.LASTPOS);
			HOTSPOT = dna.externalize(info.HOTSPOT);
			SCORE = Float.NaN;
		} // end of LTRSummary.constructor(MotifHitGraphInfo)
		
/**
* @return	True if all fields equal.
*/
		public boolean equals(Object o) {
			LTRSummary c = (LTRSummary) o;
			return (c.FIRST == FIRST) & (c.LAST == LAST) & (c.HOTSPOT == HOTSPOT);
		} // end of LTRSummary.equals(Object)
		
/**
* Compatible wit equals.
*/
		public int hashCode() {
			return FIRST - LAST + HOTSPOT;
		} // end of LTRSummary.hashCode()
		
	} // end of LTRSummary
	

/**
* To show contents of chromosomeStack.
*/
	private class TabModel extends AbstractTableModel {
	
/**
* @return 3 (Chromosome, Directory, Comment).
*/
		public int getColumnCount() {
			return 3;
		} // end of TabModel.getColumnCount()
		
/**
* @return	Size of chromosomeStack.
*/
		public int getRowCount() {
			return chromosomeStack.size();
		} // end of TabModel.getRowCount()
		
/**
* As required by AbstractTableModel.
*/
		public Object getValueAt(int row, int col) {
			String[ ] ss = (String[ ]) chromosomeStack.elementAt(row);
			return ss[col];
		} // end of TabModel.getValueAt()
		
	} // end of TabModel
	

/**
* To import contents of chromosomeStack from a text file.
*/
	private class ImportThread extends Thread {
	
/**
* Imports contents of chromosomeStack from a text file.
*/
		public void run() {
			ZFileChooser.ZFileDialog zd = new ZFileChooser.ZFileDialog(RetroTectorEngine.currentDirectory(), false);
			if (zd.getChosenFile() == null) {
				return;
			}
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(zd.getChosenFile()));
				String line;
				String[ ] ss;
				int ind;
				while ((line = br.readLine()) != null) {
					ss = new String[3];
					ind = line.indexOf("\t");
					ss[0] = line.substring(0, ind).trim();
					line = line.substring(ind + 1);
					ind = line.indexOf("\t");
					ss[1] = line.substring(0, ind).trim();
					ss[2] = line.substring(ind + 1).trim();
					chromosomeStack.push(ss);
				}
				br.close();
			} catch (IOException ioe) {
				RetroTectorEngine.displayError(new RetroTectorException("CollectGenome", "Trouble reading file"));
			}
			mainModel.fireTableChanged(new TableModelEvent(mainModel));
		} // end of ImportThread.run()
		
	} // end of ImportThread
	

	private static final String BACKQUOTE = "`"; // not in use at present

/**
* = 'Generation'.
*/
	public static final String GENERATIONKEY = "Generation";

/**
* = 'MinChainScore'.
*/
	public static final String MINCHAINSCOREKEY = "MinChainScore";

/**
* = 'MinProcessScore'.
*/
	public static final String MINPROCESSSCOREKEY = "MinProcessScore";

/**
* = 'MinLTRPairScore'.
*/
	public static final String MINLTRSCOREKEY = "MinLTRPairScore";

/**
* = 'MultiPuteins'.
*/
	public static final String MULTIPUTEINKEY = "MultiPuteins";

	private int generation;
	private int minChainScore;
	private float minLTPairScore;
	
	private Stack chromosomeStack = new Stack();

	
	private TabModel mainModel = new TabModel();
	private JTable chromosomeTable = new JTable(mainModel);
	private JPanel mainPanel = new JPanel();
	private Box topPanel = Box.createVerticalBox();
	private Box topPanel1 = Box.createHorizontalBox();
	private Box topPanel2 = Box.createHorizontalBox();
	private JPanel middlePanel = new JPanel();
	private CardLayout middleLayout = new CardLayout();
	private JPanel bottomPanel = new JPanel();
	private JPanel bottomPanel2 = new JPanel();
	private JPanel bottomPanel3 = new JPanel();
	private JLabel directoryLabel = new JLabel(" ");
	private JLabel chooseGenomeLabel = new JLabel("Choose genome");
	private JComboBox databaseBox = new JComboBox();
	private JButton addRowButton = new JButton("Add one row");
	private JButton deleteRowButton = new JButton("Delete last row");
	private JButton importButton = new JButton("Import");
	private ZFileChooser zChooser = new ZFileChooser(this);
	private JTextField longComment = new JTextField(50);
	private JTextField chromosomeField = new JTextField(MAXCHROMOSOMELENGTH);
	private JButton doAddButton = new JButton("Add row");

/**
* As required by Executor.
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		runFlag = false;
		connect();
		databaseBox.setEditable(true);
		databaseBox.addItem("");
		try {
			ResultSet rs = executeSQLQuery("show databases");
			while (rs.next()) {
				databaseBox.addItem(rs.getString(1));
			}
		} catch (SQLException se) {
			throw new RetroTectorException("CollectGenome", se);
		}
		chooseGenomeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		topPanel1.add(chooseGenomeLabel);
		topPanel1.add(databaseBox);
		addRowButton.addActionListener(this);
		topPanel2.add(addRowButton);
		deleteRowButton.addActionListener(this);
		topPanel2.add(deleteRowButton);
		importButton.addActionListener(this);
		topPanel2.add(importButton);
		topPanel.add(topPanel1);
		topPanel.add(topPanel2);
		
		zChooser.setDirectory(RetroTectorEngine.currentDirectory(), true);
		zChooser.setPreferredSize(new Dimension(500, 300));
		chromosomeTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
		chromosomeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel colMod = chromosomeTable.getColumnModel();
		TableColumn col = colMod.getColumn(0);
		col.setPreferredWidth(100);
		col.setHeaderValue("Chromosome");
		col = colMod.getColumn(1);
		col.setPreferredWidth(600);
		col.setHeaderValue("Directory");
		col = colMod.getColumn(2);
		col.setPreferredWidth(600);
		col.setHeaderValue("Comment");
		middlePanel.setLayout(middleLayout);
		middlePanel.add(new JScrollPane(chromosomeTable), "Table");
		middlePanel.add(zChooser, "Chooser");
		bottomPanel.setLayout(new GridLayout(0, 1));
		bottomPanel.add(directoryLabel);
		bottomPanel2.add(new JLabel("Long comment"));
		bottomPanel2.add(longComment);
		bottomPanel.add(bottomPanel2);
		bottomPanel3.add(new JLabel("Chromosome"));
		bottomPanel3.add(chromosomeField);
		doAddButton.addActionListener(this);
		bottomPanel3.add(doAddButton);
		bottomPanel.add(bottomPanel3);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(middlePanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		RetroTectorEngine.doParameterWindow(this, mainPanel);
		interactive = true;
	} // end of initialize()
	
/**
* Standard Executor constructor.
*/
	public CollectGenome() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(GENERATIONKEY, "1");
		explanations.put(GENERATIONKEY, "The file generation to analyze");
		orderedkeys.push(GENERATIONKEY);
		parameters.put(MINCHAINSCOREKEY, "250");
		explanations.put(MINCHAINSCOREKEY, "Chains with lower score than this are ignored");
		orderedkeys.push(MINCHAINSCOREKEY);
		parameters.put(MINPROCESSSCOREKEY, "300");
		explanations.put(MINPROCESSSCOREKEY, "Chains with lower score than this are not fully processed");
		orderedkeys.push(MINPROCESSSCOREKEY);
		parameters.put(MINLTRSCOREKEY, "0");
		explanations.put(MINLTRSCOREKEY, "LTR candidate pairs with lower PairFactor than this are ignored");
		orderedkeys.push(MINLTRSCOREKEY);
		parameters.put(MULTIPUTEINKEY, YES);
		explanations.put(MULTIPUTEINKEY, "If Yes, more than one putein of the same gene may be associated with a Chain");
		orderedkeys.push(MULTIPUTEINKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2008 04 23";
  } // end of version()
	
/**
* String to be included by ExecutorThread in the error message caused
* by a Java Runtime Exception.
*/
	public final String runtimeExceptionComment() {
		if (currentSubdirectory == null) {
			return "running CollectGenome";
		} else {
			return "running CollectGenome in " + currentSubdirectory.getPath();
		}
	} // end of runtimeExceptionComment()

/**
* As required by FileTreater. Only directories are accepted.
* @param	f	A File to treat.
* @return	f.
*/
	public final File treatFile(File f) throws RetroTectorException {
		try {
			if (f == null) {
				return null;
			}
			if (!f.isDirectory()) {
				return f;
			}
			currentSubdirectory = f; // for SQLUser
			String[ ] fileNameList = currentSubdirectory.list();
			StringBuffer command; // for SQL command
			String dnaStrand;
			String co; // for another SQL command

			int iff;
			int il;
			int ih;
			int irs;
			ResultSet rset;
			int sf;
			int sl;
			String cg;
			String ge;

			Hashtable tableFromRetroVIDScript = null;
			String[ ] keysFromRetroVIDScript;
			String[ ] keysFromChainsFile;
			String companionKey;
			LTRCandidate compCandidate = null;
			String[ ] sss;
			LTRSummary ident, ident2;
			
			ParameterFileReader re;
			
			try {
	// do LTRs
				for (int ssi=0; ssi<fileNameList.length; ssi++) {
					if (FileNamer.getRetroVIDScriptGeneration(fileNameList[ssi]) == generation) {
						tableFromRetroVIDScript = new Hashtable();
						currentLTRFile = Utilities.getReadFile(currentSubdirectory, fileNameList[ssi]);
						currentRetroVIDScriptName = currentLTRFile.getName();
						re = new ParameterFileReader(currentLTRFile, tableFromRetroVIDScript);
						re.readParameters();
						keysFromRetroVIDScript = re.close();
						makeDNA(tableFromRetroVIDScript.get(DNAFILEKEY)); // makes both strands
						
						for (int ki=0; ki<keysFromRetroVIDScript.length; ki++) {
							if (keysFromRetroVIDScript[ki].startsWith(SINGLELTRSUFFIX) | keysFromRetroVIDScript[ki].endsWith("LTR")) {
								if (keysFromRetroVIDScript[ki].startsWith(SINGLELTRSUFFIX)) {
									ltrCompanionHotspot = Integer.MIN_VALUE;
									companionKey = null;
									dnaStrand = keysFromRetroVIDScript[ki].substring(SINGLELTRSUFFIX.length(), SINGLELTRSUFFIX.length() + 1);
								} else {
									if (keysFromRetroVIDScript[ki].endsWith(LTR5KEY)) {
										companionKey = keysFromRetroVIDScript[ki].substring(0, keysFromRetroVIDScript[ki].length() - 4) + LTR3KEY;
									} else {
										companionKey = keysFromRetroVIDScript[ki].substring(0, keysFromRetroVIDScript[ki].length() - 4) + LTR5KEY;
									}
									dnaStrand = keysFromRetroVIDScript[ki].substring(0, 1);
								}
								if (dnaStrand.equals("P")) {
									currentDNA = primaryDNA;
								} else if (dnaStrand.equals("S")) {
									currentDNA = secondaryDNA;
								} else {
									haltError("Erroneous parameter value ", keysFromRetroVIDScript[ki]);
								}
								if (companionKey != null) {
									sss = (String[ ]) tableFromRetroVIDScript.get(companionKey);
									if (sss == null) {
										ltrCompanionHotspot = Integer.MIN_VALUE;
									} else {
										compCandidate = new LTRCandidate(currentDNA, sss);
										ltrCompanionHotspot = currentDNA.externalize(compCandidate.hotSpotPosition);
									}
								}
								currentLTRCandidate = new LTRCandidate(currentDNA, (String[ ]) tableFromRetroVIDScript.get(keysFromRetroVIDScript[ki]));
								if ((compCandidate == null) || ((currentLTRCandidate.candidateFactor + compCandidate.candidateFactor) >= minLTPairScore)) {
									ident = new LTRSummary(currentLTRCandidate);
									ident2 = (LTRSummary) candTable.get(ident);
									if ((ident2 == null) || (ident2.SCORE != ident.SCORE)) { // no similar found
										candTable.put(ident, ident);
										command = new StringBuffer("insert into " + LTRSNAME + " (");
										command.append(ltrColumns[0].columnName);
										for (int fi=1; fi<ltrColumns.length; fi++) {
											command.append(",");
											command.append(ltrColumns[fi].columnName);
										}
										command.append(") values (");
										command.append(ltrColumns[0].valueText());
										for (int ff=1; ff<ltrColumns.length; ff++) {
											command.append(",");
											command.append(ltrColumns[ff].valueText());
										}
										command.append(")");
										executeSQLCommand(command);
										try {
											ResultSet rsu = executeSQLQuery("select max(id) from " + LTRSNAME);
											rsu.next();
											ident.id = rsu.getInt(1);
										} catch (SQLException se) {
											haltError(se);
										}
									}
								}
							}
						}
					}
				}
		// done LTRs
		
				ParameterFileReader rep;
		
		// do Puteins
				for (int ssi=0; ssi<fileNameList.length; ssi++) {
					if ((FileNamer.getPuteinFileGeneration(fileNameList[ssi]) == generation) || (FileNamer.getEnvTraceFileGeneration(fileNameList[ssi]) == generation)) {
						isEnvTrace = FileNamer.getEnvTraceFileGeneration(fileNameList[ssi]) == generation;
						currentPuteinFile = Utilities.getReadFile(currentSubdirectory, fileNameList[ssi]);
						if (currentPuteinFile.length() > 0) {
							currentPuteinTable = new Hashtable();
							rep = new ParameterFileReader(currentPuteinFile, currentPuteinTable);
							rep.readParameters();
							rep.close();
							
							String dnaFileName = (String) currentPuteinTable.get(DNAFILEKEY);
							if (dnaFileName != null) { // for old type Putein file
								makeDNA(dnaFileName);
							}
							if (isEnvTrace) {
								dnaStrand = FileNamer.getStrandFromEnvTrace(currentPuteinFile.getName());
							} else {
								dnaStrand = FileNamer.getStrandFromPutein(currentPuteinFile.getName());
							}
							if (dnaStrand.equals("P")) {
								currentDNA = primaryDNA;
							} else if (dnaStrand.equals("S")) {
								currentDNA = secondaryDNA;
							} else {
								haltError("Erroneous Putein file name", currentPuteinFile.getName());
							}
							if (!isEnvTrace) {
								currentPutein = new Putein(currentPuteinFile, new ORFID.ParameterBlock(currentDNA));
			
								sf = currentDNA.externalize(currentPutein.estimatedFirst);
								sl = currentDNA.externalize(currentPutein.estimatedLast);
								cg = currentPutein.GENUS;
								ge = currentPutein.GENE;
								co = "select id from " + PUTEINSNAME + " where Chromosome = " + BLIP + currentChromosome + BLIP + " and EstimatedFirst = " + sf + " and EstimatedLast = " + sl + " and PuteinGenus = \'" + cg + "\' and Gene = \'"  + ge + BLIP;
								currentPuteinDuplicator = -1;
// is there a similar one?
								try {
									rset = executeSQLQuery(co);
									rset.first();
									currentPuteinDuplicator = rset.getInt("id");
								} catch (SQLException e) {
								}
							}
							command = new StringBuffer("insert into " + PUTEINSNAME + " (");
							command.append(puteinColumns[0].columnName);
							for (int fi=1; fi<puteinColumns.length; fi++) {
								command.append(",");
								command.append(puteinColumns[fi].columnName);
							}
							command.append(") values (");
							command.append(puteinColumns[0].valueText());
							for (int ff=1; ff<puteinColumns.length; ff++) {
								command.append(",");
								command.append(puteinColumns[ff].valueText());
							}
							command.append(")");
							executeSQLCommand(command);
						}
					}
				}
		// done Puteins
		
		ParameterFileReader rec;
		
		// do Chains, and LTRs in Chains file
				for (int ssi=0; ssi<fileNameList.length; ssi++) {
					if (FileNamer.getChainsFileGeneration(fileNameList[ssi]) == generation) {
						currentChainsFile = Utilities.getReadFile(currentSubdirectory, fileNameList[ssi]);
						if (currentChainsFile.length() <= 0) {
							throw new RetroTectorException("Empty chains file", currentChainsFile.getPath());
						}
						currentChainTable = new Hashtable();
						rec = new ParameterFileReader(currentChainsFile, currentChainTable);
						rec.readParameters();
						keysFromChainsFile = rec.close();
						makeDNA(currentChainTable.get(DNAFILEKEY));
						for (int ic=0; ic<keysFromChainsFile.length; ic++) {
							if (keysFromChainsFile[ic].endsWith("LTR")) {
								if (keysFromChainsFile[ic].endsWith(LTR5KEY)) {
									companionKey = keysFromChainsFile[ic].substring(0, keysFromChainsFile[ic].length() - 4) + LTR3KEY;
								} else {
									companionKey = keysFromChainsFile[ic].substring(0, keysFromChainsFile[ic].length() - 4) + LTR5KEY;
								}
								dnaStrand = keysFromChainsFile[ic].substring(0, 1);
								if (dnaStrand.equals("P")) {
									currentDNA = primaryDNA;
								} else if (dnaStrand.equals("S")) {
									currentDNA = secondaryDNA;
								} else {
									haltError("Erroneous parameter value ", keysFromChainsFile[ic]);
								}
								sss = (String[ ]) currentChainTable.get(companionKey);
								if (sss == null) {
									ltrCompanionHotspot = Integer.MIN_VALUE;
								} else {
									compCandidate = new LTRCandidate(currentDNA, sss);
									ltrCompanionHotspot = currentDNA.externalize(compCandidate.hotSpotPosition);
								}
								currentLTRCandidate = new LTRCandidate(currentDNA, (String[ ]) currentChainTable.get(keysFromChainsFile[ic]));
								ident = new LTRSummary(currentLTRCandidate);
								ident2 = (LTRSummary) candTable.get(ident);
								if ((ident2 == null) || (ident2.SCORE != ident.SCORE)) { // no similar
									candTable.put(ident, ident);
									currentLTRFile = currentChainsFile;
									command = new StringBuffer("insert into " + LTRSNAME + " (");
									command.append(ltrColumns[0].columnName);
									for (int fi=1; fi<ltrColumns.length; fi++) {
										command.append(",");
										command.append(ltrColumns[fi].columnName);
									}
									command.append(") values (");
									command.append(ltrColumns[0].valueText());
									for (int ff=1; ff<ltrColumns.length; ff++) {
										command.append(",");
										command.append(ltrColumns[ff].valueText());
									}
									command.append(")");
									executeSQLCommand(command);
									try {
										ResultSet rsu = executeSQLQuery("select max(id) from " + LTRSNAME);
										rsu.next();
										ident.id = rsu.getInt(1);
									} catch (SQLException se) {
										haltError(se);
									}
								}
							}
						}
						for (int ic=0; ic<keysFromChainsFile.length; ic++) {
							if (keysFromChainsFile[ic].startsWith("Chain")) {
								currentChainName = keysFromChainsFile[ic];
								dnaStrand = currentChainName.substring("Chain".length(), "Chain".length() + 1);
								if (dnaStrand.equals("P")) {
									currentDNA = primaryDNA;
								} else if (dnaStrand.equals("S")) {
									currentDNA = secondaryDNA;
								} else {
									haltError("Erroneous parameter value ", keysFromChainsFile[ic]);
								}
								currentChainGraphInfo = new ChainGraphInfo((String[ ]) currentChainTable.get(currentChainName), currentDNA);
								if (currentChainGraphInfo.SCORE >= minChainScore) {
										currentFirstUsedPos = currentChainGraphInfo.FIRSTBASEPOS;
										currentLastUsedPos = currentChainGraphInfo.LASTBASEPOS;
										current5LTR = null;
										currentPBS = null;
										currentPPT = null;
										current3LTR = null;

										iff = currentDNA.externalize(currentChainGraphInfo.firstInCore());
										il = currentDNA.externalize(currentChainGraphInfo.lastInCore());
										cg = currentChainGraphInfo.GENUS;
										if (currentDNA == primaryDNA) {
											co = "select id from " + CHAINSNAME + " where Chromosome = " + BLIP + currentChromosome + BLIP + " and Strand = \'P\' and (CoreFirst between " + iff + " and " + il + ") or (CoreLast between " + iff + " and " + il + ") or (" + iff + " between CoreFirst and CoreLast)";
										} else {
											co = "select id from " + CHAINSNAME + " where Chromosome = " + BLIP + currentChromosome + BLIP + " and Strand = \'S\' and (CoreFirst between " + il + " and " + iff + ") or (CoreLast between " + il + " and " + iff + ") or (" + iff + " between CoreLast and CoreFirst)";
										}
										currentChainOverlapper = null;
										try {
											rset = executeSQLQuery(co);
											while (rset.next()) {
												if (currentChainOverlapper == null) {
													currentChainOverlapper = String.valueOf(rset.getInt("id"));
												} else {
													currentChainOverlapper = currentChainOverlapper + ", " + rset.getInt("id");
												}
											}
										} catch (SQLException e) {
										}
										for (int j=0; j<currentChainGraphInfo.subgeneinfo.length; j++) {
											if (currentChainGraphInfo.subgeneinfo[j].SUBGENENAME.equals(LTR5KEY)) {
												current5LTR = currentChainGraphInfo.subgeneinfo[j].motifhitinfo[0];
											} else if (currentChainGraphInfo.subgeneinfo[j].SUBGENENAME.equals(SubGene.PBS)) {
												currentPBS = currentChainGraphInfo.subgeneinfo[j].motifhitinfo[0];
											} else if (currentChainGraphInfo.subgeneinfo[j].SUBGENENAME.equals(SubGene.PPT)) {
												currentPPT = currentChainGraphInfo.subgeneinfo[j].motifhitinfo[0];
											} else if (currentChainGraphInfo.subgeneinfo[j].SUBGENENAME.equals(LTR3KEY)) {
												current3LTR = currentChainGraphInfo.subgeneinfo[j].motifhitinfo[0];
											}
										}
										
										command = new StringBuffer("insert into " + CHAINSNAME + " values (");
										command.append(chainColumns[0].valueText());
										for (int ff=1; ff<chainColumns.length; ff++) {
											command.append(",");
											command.append(chainColumns[ff].valueText());
										}
										command.append(")");
										executeSQLCommand(command);
//									}
								}
							}
						}
					}
				}
	// done Chains
			} catch (SQLException sqle) {
				haltError(sqle);
			}
			RetroTectorEngine.setInfoField(currentSubdirectory.getPath() + " done");
		} catch (RetroTectorException re) {
			RetroTectorEngine.displayError(re);
		}
		return f;
	} // end of treatFile(File)
	
/**
* Executes as specified above.
*/
	public final String execute() throws RetroTectorException {
		try {
			setDatabase((String) databaseBox.getSelectedItem());
			if (preexisted) {
				if (!RetroTectorEngine.doQuestion("The genome " + databaseBox.getSelectedItem() + " already exists.\nAre you sure that you want to add to it?")) {
					return "";
				}
				if (RetroTectorEngine.doQuestion("If there are already data from these chromosomes in this database, do you want them removed?")) {
					String[ ] toda;
					for (int tod=0; tod<chromosomeStack.size(); tod++) {
						toda = (String[ ]) chromosomeStack.elementAt(tod);
						executeSQLCommand("delete from Chains where Chromosome=" + BLIP + toda[0] + BLIP);
						executeSQLCommand("delete from Puteins where Chromosome=" + BLIP + toda[0] + BLIP);
						executeSQLCommand("delete from LTRs where Chromosome=" + BLIP + toda[0] + BLIP);
						executeSQLCommand("delete from Chromosomes where Chromosome=" + BLIP + toda[0] + BLIP);
					}
				}
			}
			generation = getInt(GENERATIONKEY, 1);
			minChainScore = getInt(MINCHAINSCOREKEY, 250);
			minProcessScore = getInt(MINPROCESSSCOREKEY, 300);
			minLTPairScore = getFloat(MINLTRSCOREKEY, 0.0f);
			multiPuteins = getString(MULTIPUTEINKEY, YES).equals(YES);

			StringBuffer command;
			if (!preexisted) {
				command = new StringBuffer("create table " + LTRSNAME + " (id int auto_increment");
				for (int i=0; i<ltrColumns.length; i++) {
					command.append(",");
					command.append(ltrColumns[i].headerText());
				}
				command.append(",primary key (id, " + CHROMOSOME + "))");
				executeSQLCommand(command);
	
				command = new StringBuffer("create table " + PUTEINSNAME + " (id int auto_increment");
				for (int i=0; i<puteinColumns.length; i++) {
					command.append(",");
					command.append(puteinColumns[i].headerText());
				}
				command.append(",primary key (id, " + CHROMOSOME + "))");
				executeSQLCommand(command);
	
				command = new StringBuffer("create table " + CHAINSNAME + " (");
				command.append(chainColumns[0].headerText());
				for (int i=1; i<chainColumns.length; i++) {
					command.append(",");
					command.append(chainColumns[i].headerText());
				}
				command.append(",primary key (id, " + CHROMOSOME + "))");
				executeSQLCommand(command);

				command = new StringBuffer("create table " + CHROMOSOMESNAME + " (id int auto_increment");
				for (int i=0; i<chromosomeColumns.length; i++) {
					command.append(",");
					command.append(chromosomeColumns[i].headerText());
				}
				command.append(",primary key (id, " + CHROMOSOME + "))");
				executeSQLCommand(command);
	
			} else {
				try {
					ResultSet rs = executeSQLQuery("select max(id) from " + CHAINSNAME);
					rs.next();
					latestChainID = rs.getInt(1);
				} catch (SQLException se) {
					haltError(se);
				}
			}

			String[ ] ss;
			for (int i=0; i<chromosomeStack.size(); i++) {
				candTable = new Hashtable();
				ss = (String[ ]) chromosomeStack.elementAt(i);
				rootDirectory = Utilities.getReadFile(ss[1]);
				currentChromosome = ss[0];
				currentChromosomeLength = 0;
				currentLongComment = ss[2];
				Utilities.treatFilesIn(rootDirectory, this);
				command = new StringBuffer("insert into " + CHROMOSOMESNAME + " (");
				command.append(chromosomeColumns[0].columnName);
				for (int fi=1; fi<chromosomeColumns.length; fi++) {
					command.append(",");
					command.append(chromosomeColumns[fi].columnName);
				}
				command.append(") values (");
				command.append(chromosomeColumns[0].valueText());
				for (int ff=1; ff<chromosomeColumns.length; ff++) {
					command.append(",");
					command.append(chromosomeColumns[ff].valueText());
				}
				command.append(")");
				executeSQLCommand(command);
			}
			disconnect();
		} catch (SQLException e) {
			haltError(e);
		}
		return "";
	} // end of execute()
	
/**
* As required by ActionListener.
*/
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == deleteRowButton) {
			if (chromosomeStack.size() > 0) {
				chromosomeStack.pop();
				mainModel.fireTableChanged(new TableModelEvent(mainModel));
			}
		} else if (ae.getSource() == addRowButton) {
			String su = (String) databaseBox.getSelectedItem();
			if (su.trim().length() > 0) {
				directoryLabel.setText("");
				chromosomeField.setText("");
				longComment.setText("");
				middleLayout.last(middlePanel);
			}
		} else if (ae.getSource() == importButton) {
			(new ImportThread()).start();
		} else if (ae.getSource() == zChooser.okButton) {
			String pa = zChooser.getSelectedFile().getPath();
			if (zChooser.getSelectedFile() instanceof ZFile) {
				pa = pa + "/";
			} else {
				pa = pa + File.separator;
			}
			directoryLabel.setText(pa);
			middleLayout.first(middlePanel);
		} else if (ae.getSource() == zChooser.cancelButton) {
			middleLayout.first(middlePanel);
		} else if (ae.getSource() == doAddButton) {
			String[ ] ss = new String[3];
			ss[0] = chromosomeField.getText();
			ss[1] = directoryLabel.getText();
			ss[2] = longComment.getText();
			chromosomeStack.push(ss);
			mainModel.fireTableChanged(new TableModelEvent(mainModel));
		}
	} // end of actionPerformed(ActionEvent)

// makes currentDNAFile, primaryDNA, secondaryDNA and currentChromosomeLength
	private void makeDNA(Object dnaFileName) throws RetroTectorException {
		String fileName = (String) dnaFileName;
		File tryDNA = Utilities.getReadFile(currentSubdirectory, fileName);
		if (tryDNA.equals(currentDNAFile)) {
			return;
		}
		currentDNAFile = tryDNA;
		primaryDNA = new DNA(currentDNAFile, true);
		secondaryDNA = new DNA(currentDNAFile, false);
		currentDNA = null;
		currentChromosomeLength = Math.max(currentChromosomeLength, primaryDNA.externalize(primaryDNA.LASTVALID));
	} // end of makeDNA(Object)
		
} // end of CollectGenome
