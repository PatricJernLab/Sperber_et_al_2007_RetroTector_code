/*
* Copyright (©) 2000-2005, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 12/3 -05
*/
package plugins;


import retrotector.*;
import builtins.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

public class FlankPair extends SQLUser {

	private class FlankData {
	
		final int ID;
		final int[ ] BASES;
		final long[ ] MERS;
		
		FlankData(int id, String s) throws RetroTectorException {
			ID = id;
			BASES = Utilities.encodeBaseString(s);
			MERS = Utilities.merize(BASES, 0, BASES.length - 1, MERLENGTH);
		}
	}

	public static final int MERLENGTH = 30;
	public static final String FLANKTYPEKEY = "FlankType";
	public static final String RAW = "Raw";
	public static final String SWEPT = "Swept";

/**
* Standard Executor constructor.
*/
	public FlankPair() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(FLANKTYPEKEY, RAW);
		explanations.put(FLANKTYPEKEY, RAW + " or " + SWEPT);
		orderedkeys.push(FLANKTYPEKEY);
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2005 03 14";
  } //end of version
	
	private Box mainPanel = Box.createVerticalBox();
	private JComboBox databaseBox1 = new JComboBox();
	private JLabel w1Label = new JLabel("where");
	private JTextField w1Field = new JTextField("Score>400", 50);
	private JComboBox databaseBox2 = new JComboBox();
	private JLabel w2Label = new JLabel("where");
	private JTextField w2Field = new JTextField("Score>400", 50);

/**
* As required by Executor.
*/
	public void initialize(ParameterFileReader script) throws RetroTectorException {
		runFlag = false;
		connect();
		try {
			ResultSet rs = executeSQLQuery("show databases");
			while (rs.next()) {
				databaseBox1.addItem(rs.getString(1));
				databaseBox2.addItem(rs.getString(1));
			}
		} catch (SQLException se) {
			throw new RetroTectorException("FlankPair", se);
		}
		Box box1 = Box.createHorizontalBox();
		box1.add(databaseBox1);
		box1.add(w1Label);
		box1.add(w1Field);
		mainPanel.add(box1);
		Box box2 = Box.createHorizontalBox();
		box2.add(databaseBox2);
		box2.add(w2Label);
		box2.add(w2Field);
		mainPanel.add(box2);
		RetroTectorEngine.doParameterWindow(this, mainPanel);
		interactive = true;
		runFlag = true;
	} // end of initialize
	
	ResultSet rs1;
	ResultSet rs2;
	FlankData[ ] preFlanks;
	FlankData[ ] postFlanks;
	
	Stack finds = new Stack();
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
	
		try {
			String fType = getString(FLANKTYPEKEY, RAW);
			if (!fType.equals(RAW) & !fType.equals(SWEPT)) {
				haltError(FLANKTYPEKEY + " must be " + RAW + " or " + SWEPT);
			}
			Statement stat1 = getNewStatement();
			String db1 = (String) databaseBox1.getSelectedItem();
			executeSQLCommand("use " + db1);
			String command = "select PreFlank" + fType + ",PostFlank" + fType + ",id,Strand,Chromosome,ChainInfo,Overlapper,LTRDivergence from Chains";
			if (w1Field.getText().trim().length() > 0) {
				command = command + " where " + w1Field.getText().trim();
			}
			rs1 = executeSQLQuery(stat1, command);
			
			Statement stat2 = getNewStatement();
			String db2 = (String) databaseBox2.getSelectedItem();
			executeSQLCommand("use " + db2);
			command = "select PreFlank" + fType + ",PostFlank" + fType + ",id from Chains";
			if (w2Field.getText().trim().length() > 0) {
				command = command + " where " + w2Field.getText().trim();
			}
			rs2 = executeSQLQuery(stat2, command);
			
			Stack preStack = new Stack();
			Stack postStack = new Stack();
			int id;
			String s1;
			while (rs2.next()) {
				id = rs2.getInt(3);
				s1 = rs2.getString(1);
				preStack.push(new FlankData(id, s1));
				s1 = rs2.getString(2);
				postStack.push(new FlankData(id, s1));
			}
			preFlanks = new FlankData[preStack.size()];
			preStack.copyInto(preFlanks);
			postFlanks = new FlankData[postStack.size()];
			postStack.copyInto(postFlanks);
			
			ResultSet  rs3;
			int ov;
			float ltrD;
			FlankData fdd;
			while (rs1.next()) {
				finds = new Stack();
				id = rs1.getInt(3);
				s1 = rs1.getString(1);
				goThrough(new FlankData(id, s1));
				s1 = rs1.getString(2);
				goThrough(new FlankData(id, s1));
				System.out.println();
				System.out.println("Chain " + id + " in strand " + rs1.getString("Strand") + " in chromosome " + rs1.getString("Chromosome") + " in " + db1);
				try {
					ov = rs1.getInt("Overlapper");
					System.out.println("  Overlaps Chain " + ov);
				} catch (SQLException	sqle) {
				}
				try {
					ltrD = rs1.getFloat("LTRDivergence");
					System.out.println("  LTRDivergence: " + ltrD);
				} catch (SQLException	sqle) {
				}
				System.out.println();
				System.out.println(rs1.getString("ChainInfo"));
				System.out.println();
				System.out.println();
				for (int i=0; i<finds.size(); i++) {
					fdd = (FlankData) finds.elementAt(i);
					command = "select Strand,Chromosome,ChainInfo,Overlapper,LTRDivergence from Chains where id=" + fdd.ID;
					rs3 = executeSQLQuery(command);
					rs3.next();
					System.out.println("Chain " + fdd.ID + " in strand " + rs3.getString("Strand") + " in chromosome " + rs3.getString("Chromosome") + " in " + db2);
					try {
						ov = rs3.getInt("Overlapper");
						System.out.println("  Overlaps Chain " + ov);
					} catch (SQLException	sqle) {
					}
					try {
						ltrD = rs3.getFloat("LTRDivergence");
						System.out.println("  LTRDivergence: " + ltrD);
					} catch (SQLException	sqle) {
					}
					System.out.println();
					System.out.println(rs3.getString("ChainInfo"));
					System.out.println();
					System.out.println();
				}
				System.out.println("-----------------------------------------------------------------------------");
			}
		} catch (SQLException sqle) {
			haltError(sqle);
		}
		
		return "";
	} // end of execute
	
	void goThrough(FlankData fd) throws RetroTectorException {
		for (int i2=0; i2<preFlanks.length; i2++) {
			compare(fd, preFlanks[i2]);
		}
		for (int i2=0; i2<postFlanks.length; i2++) {
			compare(fd, postFlanks[i2]);
		}
	} // end of goThrough
	
	void compare(FlankData fd1, FlankData fd2) throws RetroTectorException {
			
		int ifi;
		int ila;
		boolean found;
		FlankData fd;
		for (int i1=0; i1<fd1.MERS.length - MERLENGTH; i1+=MERLENGTH) {
			if (fd1.MERS[i1] >= 0) {
				for (int i2=0; i2<fd2.MERS.length-MERLENGTH; i2++) {
					if (fd1.MERS[i1] == fd2.MERS[i2]) {
						for (ifi=0; (ifi<i1) && (ifi<i2) && (fd1.BASES[i1-ifi-1]<=3) && (fd1.BASES[i1-ifi-1]==fd2.BASES[i2-ifi-1]); ifi++) {
						}
						for (ila=MERLENGTH-1; (ila+i1+1<fd1.BASES.length) && (ila+i2+1<fd1.BASES.length) && (fd1.BASES[ila+i1+1]<=3) && (fd1.BASES[ila+i1+1]==fd2.BASES[ila+i2+1]); ila++) {
						}
						if (ila+ifi>=59) {
							found = false;
							for (int ii=0; !found & (ii < finds.size()); ii++) {
								fd = (FlankData) finds.elementAt(ii);
								if (fd.ID == fd2.ID) {
									found = true;
								}
							}
							if (!found) {
								finds.push(fd2);
							}
						}
					}
				}
			}
		}
		
	} // end of compare
	
}
