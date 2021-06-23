/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 17/10 -06
* Beautified 17/10 -06
*/
package plugins;

import retrotector.*;
import builtins.*;

import java.io.*;
import java.util.*;

/**
* Executor to check consistency of a Database file.
* The result goes to text output and to error log file.
*<PRE>
*     Parameters:
*
*   Database
* The name of the Database subdirectory to check.
*</PRE>
*/
public class CheckDatabase extends Executor {

	private Database database;

/**
* Standard Executor constructor.
*/
	public CheckDatabase() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DATABASEKEY, ORDINARYDATABASE);
		explanations.put(DATABASEKEY, "The Database subdirectory");
		orderedkeys.push(DATABASEKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 17";
  } // end of version()
  
	private int ali = 0; // alignment file counter
	
/**
* Execute as specified above.
*/
	public String execute() throws RetroTectorException {

		database = Database.getDatabase(getString(DATABASEKEY, ORDINARYDATABASE));
		String fileName;
		String[ ] ss = database.getFileList();
		for (int s=0; s<ss.length; s++) {
			fileName = ss[s];
			if (fileName.endsWith("Alignment.txt") | fileName.endsWith("alignment.txt")) {
				doOneAlignment(fileName);
			}
		}
		outputString(" " + ali + " Alignments checked");
		outputString("");
		outputString("    Checking SubGenes.txt");
		doSubGenes();
		outputString("");
		outputString("    Checking MotifGroups.txt");
		doMotifGroups();
		outputString("");
		outputString("    Checking Genes.txt");
		checkDate("Genes.txt");
	  return "";
	} // end of execute()
	
	
// checks that file modification date agrees with "Last changed" date in it.
	private void checkDate(String fileName) throws RetroTectorException {
		File fi = database.getFile(fileName);
		long dal = fi.lastModified();
		Date da = new Date(dal);
		Calendar cal = Calendar.getInstance();
		cal.setTime(da);

		BufferedReader tr = null;
		try {
			tr = new BufferedReader(new FileReader(fi));
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "There was trouble opening ", fileName);
		}
		
		String line;
		String[ ] ss = null;
		int index;
		int[ ] daints = null;
		try {
			while (((line = tr.readLine()) != null) & (daints == null)) { // search for line with "Last changed" in it
				if ((index = line.indexOf(Database.LASTCHANGED)) >= 0) {
					ss = Utilities.splitString(line.substring(index));
					if (ss.length >= 5) {
						daints = new int[3];
						try {
							daints[0] = Utilities.decodeInt(ss[2]);
							daints[1] = Utilities.decodeInt(ss[3]);
							daints[2] = Utilities.decodeInt(ss[4]);
						} catch (RetroTectorException e) {
							daints = null;
						}
					}
				}
			}
			tr.close();
		} catch (IOException e) {
		}
		
		if (daints == null) {
			outputString("No valid changed date");
		} else {
			if ((daints[0] != cal.get(Calendar.YEAR)) |
					(daints[1] != cal.get(Calendar.MONTH) + 1) |
					(daints[2] != cal.get(Calendar.DATE))) {
				outputString("Modification date mismatch:");
				outputString("    " + ss[2] + " " + ss[3] + " " + ss[4] + " should be");
				outputString("    " + Utilities.swedishDate(da));
			}
		}
	} // end of checkDate(String)
		
// checks that SubGenes.txt is antisymmetric
	private void doSubGenes() throws RetroTectorException {
		checkDate("SubGenes.txt");
		SubGene sg1;
		SubGene sg2;
		for (int i=0; (sg1=database.getSubGene(i)) != null; i++) {
			for (int j=i+1; (sg2=database.getSubGene(j)) != null; j++) {
				if (!sg1.distanceTo(sg2).inverse().doesEqual(sg2.distanceTo(sg1))) {
					outputString("Distance mismatch between " + sg1.SUBGENENAME + " and " + sg2.SUBGENENAME);
				}
			}
		}
	} // end of doSubGenes()
	
// checks that distances in MotifGroups.txt are consistent
	private void doMotifGroups() throws RetroTectorException {
		checkDate("MotifGroups.txt");
		MotifGroup mg1;
		MotifGroup mg2;
		Enumeration en1;
		Enumeration en2;
		en1 = database.enumerateMotifGroups();
		while (en1.hasMoreElements()) {
			mg1 = (MotifGroup) en1.nextElement();
			if ((mg1.geneStartDistance != null) && (mg1.geneStartDistance.HIGHESTDISTANCE > 0)) {
				outputString(mg1.MOTIFGROUPNAME + ": Invalid distance to Gene start " + mg1.geneStartDistance.toString());
			}
			if ((mg1.geneEndDistance != null) && (mg1.geneEndDistance.LOWESTDISTANCE < 0)) {
				outputString(mg1.MOTIFGROUPNAME + ": Invalid distance to Gene end " + mg1.geneEndDistance.toString());
			}
			en2 = database.enumerateMotifGroups();
			while (en2.hasMoreElements()) {
				mg2 = (MotifGroup) en2.nextElement();
				if (mg1 != mg2) {
					if (mg1.distanceToGroup(mg2) != null) {
						if (!mg1.distanceToGroup(mg2).inverse().doesEqual(mg2.distanceToGroup(mg1))) {
							outputString("Distance mismatch between " + mg1.MOTIFGROUPNAME + " and " + mg2.MOTIFGROUPNAME);
						}
					} else if (mg2.distanceToGroup(mg1) != null) {
						outputString("Distance mismatch between " + mg2.MOTIFGROUPNAME + " and " + mg1.MOTIFGROUPNAME);
					}
				}
			}
		}							
	} // end of doMotifGroups()

  private void doOneAlignment(String fileName) throws RetroTectorException {
		outputString(" ");
    outputString("  Checking alignment " + fileName);
		checkDate(fileName);
		Alignment aln = new Alignment(database.getFile(fileName));
		
// search for MotifGroup anchors and check for validity
		Enumeration el = database.enumerateMotifGroups();
    MotifGroup mg;
    int mm = 0;
    MotifGroup[ ] mgroups = new MotifGroup[database.NROFMOTIFGROUPS]; // to contain all MotifGroups
		while (el.hasMoreElements()) {
      mg = (MotifGroup) el.nextElement();
      mgroups[mm++] = mg;
		}

		int index = 0;
		int in;
		String sx;
		MotifLitter lit;
		while ((index = aln.MOTIFLINE.indexOf("<", index)) >= 0) { // check that all MotifGroup markers in MOTIFLINE are valid
			in = aln.MOTIFLINE.indexOf(">", index);
			sx = aln.MOTIFLINE.substring(index + 1, in);
			mg = database.getMotifGroup(sx);
			if (mg == null) {
				outputString("Unrecognised MotifGroup name: " + sx);
			}
			index++;
		}
		index = 0;
		while ((index = aln.MOTIFLINE2.indexOf("<", index)) >= 0) { // check that all MotifGroup markers in MOTIFLINE2 are valid
			in = aln.MOTIFLINE2.indexOf(">", index);
			sx = aln.MOTIFLINE2.substring(index + 1, in);
			mg = database.getMotifGroup(sx);
			if (mg == null) {
				outputString("Unrecognised MotifGroup name: " + sx);
			}
			index++;
		}


// check that interMotifGroup distances are consistent with MotifGroups.txt
    for (int m1=0; m1<mgroups.length; m1++) {
      for (int m2=m1+1; m2<mgroups.length; m2++) {
        if (m1 != m2) {
          int pos1 = aln.positionOf(mgroups[m1].MOTIFGROUPNAME);
          int pos2 = aln.positionOf(mgroups[m2].MOTIFGROUPNAME);
          if ((pos1 >= 0) & (pos2 >= 0)) {
            for (int row=0; row<aln.NROFROWS; row++) {
              int dist = 3 * aln.getRow(row).nonDashesBetween(pos1, pos2);
              if (dist != Integer.MIN_VALUE) {
                DistanceRange dr = mgroups[m1].longDistanceTo(mgroups[m2]);
                if ((dr != null) && (!dr.containsDistance(dist))) {
                  outputString(mgroups[m1].MOTIFGROUPNAME + " " + mgroups[m2].MOTIFGROUPNAME + " distance " + dist + " outside " + dr.toString() + " in " + aln.getRow(row).ROWORIGIN);
                }
              }
            }
          }
        }
      }
    }
		ali++;
  } // end of doOneAlignment(String)
	
	private void outputString(String s) {
		Utilities.outputString(s);
		RetroTectorEngine.toLogFile(s);
	} // end of outputString(String)

} // end of CheckDatabase
