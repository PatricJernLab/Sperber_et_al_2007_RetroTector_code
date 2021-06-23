/*
* Copyright (©) 2000-2006 Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 30/9 -06
* Beautified 30/9 -06
*/
package builtins;

import retrotector.*;

import java.util.*;
import java.io.*;

/**
* Executor to search for all hits with a particular Motif, or all Motifs
* (if MotifIDNumber < 0), or all Motifs between two numbers.
*<PRE>
*     Parameters:
*
*   DNAFile
* The name of the DNA file in the current directory to search in.
* If it is empty, all ordinarily named DNA files in the current directory will be searched.
* Default: name of current directory + '.txt'.
*
*   Parameter
* The parameter in Motif.txt where the Motif is defined.
* Default: Motifs
*
*   MotifIDNumber
* The number (according to Motifs.txt) of the Motif to search for,
* or two numbers to indicate a range of Motifs to search for,
* or -1 to search for all.
* Default: -1.
*
*   ConservationFactor
* The bonus for highly conserved positions in Motifs.
* Default: 2.
*
*   SDFactor
* The factor to multiply standard deviation of scores by
* when setting score thresholds.
* Default: 5.5.
*</PRE>	
*/
public class CollectMotifHits extends Executor implements Utilities.FileTreater {

/**
*	Key for parameter in Motifs.txt = "Parameter".
*/
	public static final String PARAMETERLABEL = "Parameter";
  
/**
* Key for the Motif number(s) = "MotifIDNumber".
*/
	public static final String MOTIFIDLABEL = "MotifIDNumber";

  private Motif[ ] allMotifs;

/**
* Standard Executor constructor.
*/
	public CollectMotifHits() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put(PARAMETERLABEL, Database.MOTIFSKEY);
		explanations.put(PARAMETERLABEL, "Parameter in Motifs.txt");
		orderedkeys.push(PARAMETERLABEL);
		parameters.put(MOTIFIDLABEL, "-1");
		explanations.put(MOTIFIDLABEL, "ID number of Motif");
		orderedkeys.push(MOTIFIDLABEL);
		parameters.put(CFACTKEY, "2");
		explanations.put(CFACTKEY, "Bonus factor for highly conserved positions");
		orderedkeys.push(CFACTKEY);
		parameters.put(SDFACTORKEY, "5.5");
		explanations.put(SDFACTORKEY, "To multiply SD with when setting threshold");
		orderedkeys.push(SDFACTORKEY);
	} // end of constructor.
	
	private float cfact; // bonus factor for conserved positions
	private float sdFactor; // To multiply SD with when setting threshold
	private Motif theMotif;
	
	private File currentDir;
	private int overallHits;
	
// search for theMotif in one strand
	private final void doStrand(DNA targetDNA) throws RetroTectorException {
		int counter = 0;
		float score;
    MotifHit mh;
		Object o;
		MotifHit[ ] mhs;
		theMotif.refresh(new Motif.RefreshInfo(targetDNA, cfact, sdFactor, null));
    theMotif.localRefresh(0, targetDNA.LENGTH);
    if (theMotif instanceof OrdinaryMotif) {
      Utilities.outputString("Threshold=" + ((OrdinaryMotif) theMotif).getRawThreshold());
    }
		for (int pos=0; pos<targetDNA.LENGTH; pos++) {
      o = theMotif.getMotifHitsAt(pos);
			if (o != null) {
				if (o instanceof MotifHit) {
					mh = (MotifHit) o;
					if (mh.MOTIFHITSCORE >= 0) {
						counter++;
						Utilities.outputString("Hit at " + targetDNA.externalize(mh.MOTIFHITHOTSPOT) + "(" + targetDNA.externalize(mh.MOTIFHITFIRST) + "-" + targetDNA.externalize(mh.MOTIFHITLAST) + ") score=" + mh.MOTIFHITSCORE + " " + mh.PARENTMOTIF.correspondingDNA(mh));
					}
				} else if (o instanceof MotifHit[ ]) {
					mhs = (MotifHit[ ]) o;
					for (int mi=0; mi<mhs.length; mi++) {
						mh = mhs[mi];
						if (mh.MOTIFHITSCORE >= 0) {
							counter++;
							Utilities.outputString("Hit at " + targetDNA.externalize(mh.MOTIFHITHOTSPOT) + "(" + targetDNA.externalize(mh.MOTIFHITFIRST) + "-" + targetDNA.externalize(mh.MOTIFHITLAST) + ") score=" + mh.MOTIFHITSCORE + " " + mh.PARENTMOTIF.correspondingDNA(mh));
						}
					}
				}
			}
		}
		Utilities.outputString(counter + " hits in strand");
		overallHits += counter;
	} // end of doStrand(DNA)
	
// do one Motif
	private final void makeOne(int nr, String dnaName) throws RetroTectorException {
		theMotif = allMotifs[0];
		
		int m;
		for (m=1; (m<allMotifs.length) & (theMotif.MOTIFID != nr); m++) {
			theMotif = allMotifs[m];
		}
		if (m>=allMotifs.length) {
			theMotif = allMotifs[allMotifs.length - 1];
			if (theMotif.MOTIFID != nr) {
				return;
			}
		}
		if (dnaName.length() > 0) {
			DNA primDNA = getDNA(dnaName, true);
			DNA secDNA = getDNA(dnaName, false);
			Utilities.outputString("    Motif nr " + theMotif.MOTIFID);
			Utilities.outputString("  Primary strand");
			doStrand(primDNA);
			
			Utilities.outputString(" Secondary strand");
			doStrand(secDNA);
		} else {
			Utilities.outputString("      Motif nr " + theMotif.MOTIFID);
			currentDir = RetroTectorEngine.currentDirectory();
			overallHits = 0;
			Utilities.treatFilesIn(RetroTectorEngine.currentDirectory(), this);
			RetroTectorEngine.setCurrentDirectory(currentDir, "swps");
			Utilities.outputString("        Overall " + overallHits + " with Motif nr " + theMotif.MOTIFID);
		}
	} // end of makeOne(int, String)
	
/**
* Collects instances of Motif subclasses defined by relevant parameter in Motifs.txt.
*/
	private final void collectMotifs() throws RetroTectorException {
	  Hashtable table = new Hashtable();
    Database database = RetroTectorEngine.getCurrentDatabase();
		ParameterFileReader reader = new ParameterFileReader(database.getFile(Database.MOTIFFILENAME), table);
		reader.readParameters();
		reader.close();
		String[ ] mlines = (String[ ]) table.get(getString(PARAMETERLABEL, Database.MOTIFSKEY));
		allMotifs = new Motif[mlines.length];
		for (int i=0; i<mlines.length; i++) {
			Motif.dataLine = mlines[i];
			Motif.motifDataBase = database;
      String n = Motif.seq_typePart(); //get seq_type field
      Class c = (Class) Motif.motifClassTable.get(n);
      if (c == null) {
        haltError("Motif type not identified", n);
      }
      Motif result = null;
      try {
        result = (Motif) c.newInstance();
      } catch (Exception e) {
        if (e instanceof RetroTectorException) {
          haltError((RetroTectorException) e);
        } else {
          haltError("Could not make Motif from", Motif.dataLine);
        }
      }
      allMotifs[i] = result;
		}
	} // end of collectMotifs()
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 09 30";
  } //end of version
  
/**
* Executes, using parameters above.
*/
	public final String execute() throws RetroTectorException {
	
		cfact = getFloat(CFACTKEY, 2.0f);
		sdFactor = getFloat(SDFACTORKEY, 5.5f);
    collectMotifs();
		Utilities.outputString("Database is " + RetroTectorEngine.getCurrentDatabase().DATABASENAME);
    String mo = getString(MOTIFIDLABEL, "");
    int ind = mo.trim().indexOf(' ');
    if (ind >= 0) {
      int id1 = Utilities.decodeInt(mo.trim().substring(0, ind));
      int id2 = Utilities.decodeInt(mo.trim().substring(ind + 1).trim());
      for (int id3=id1; id3<=id2; id3++) {
        makeOne(id3, getString(DNAFILEKEY, ""));
      }
    } else {
			int id = getInt(MOTIFIDLABEL, -1);
			if (id >= 0) {
				makeOne(id, getString(DNAFILEKEY, ""));
			} else {
				for (int i=0; i<=9999; i++) {
					makeOne(i, getString(DNAFILEKEY, ""));
				}
			}
		}
		
		return "";
	} // end of execute()
	
/**
* As required by FileTreater.
*/
	public final File treatFile(File f) throws RetroTectorException {
		if (f.getParent() == null) {
			return f;
		}
		if (!f.getName().equals(f.getParentFile().getName() + FileNamer.TXTTERMINATOR)) {
			return f;
		}
		if (currentDir == f.getParentFile()) {
			return f;
		}
		Utilities.outputString("    " + f.getPath());
		RetroTectorEngine.setCurrentDirectory(f.getParentFile(), "swps");
		DNA primDNA = getDNA(f.getName(), true);
		DNA secDNA = getDNA(f.getName(), false);
		Utilities.outputString(" Primary strand");
		doStrand(primDNA);
			
		Utilities.outputString(" Secondary strand");
		doStrand(secDNA);
		return f;
	} // end of treatFile(File)
	
} // end of CollectMotifHits
