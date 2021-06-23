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

import java.io.*;
import java.util.*;

/**
* Class representing one of the subdirectories of the Database directory.
*/
public class Database {

/**
* = "Last changed".
*/
	public static final String LASTCHANGED = "Last changed";

/**
* Name of file in Database with Motif information (="Motifs.txt").
*/
	public static final String MOTIFFILENAME = "Motifs.txt";
	
/**
* Key of parameter in Motifs.txt defining parameters = "Motifs".
*/
  public static final String MOTIFSKEY = "Motifs";

/**
* Key of parameter in Motifs.txt defining Kozak motif = "KozakMotif".
*/
  public static final String KOZAKMOTIFKEY = "KozakMotif";

/**
* Key of parameter in Motifs.txt of early LTR motifs = "LTR1Motifs".
*/
  public static final String LTR1MOTIFSKEY = "LTR1Motifs";

/**
* Key of parameter in Motifs.txt of late LTR motifs = "LTR2Motifs".
*/
  public static final String LTR2MOTIFSKEY = "LTR2Motifs";
  
/**
* Key of parameter in Motifs.txt of components of coincidence Motifs = "ComponentMotifs".
*/
  public static final String COMPONENTMOTIFSKEY = "ComponentMotifs";
  
/**
* Key of parameter in Motifs.txt of SpliceAcceptorMotif = "SpliceAcceptorMotif".
*/
  public static final String SPLICEACCEPTORMOTIFKEY = "SpliceAcceptorMotif";
  
/**
* Key of parameter in Motifs.txt of SpliceDonorMotif = "SpliceDonorMotif".
*/
  public static final String SPLICEDONORMOTIFKEY = "SpliceDonorMotif";
  
/**
* Key of parameter in Motifs.txt of SlipperyMotif = "SlipperyMotif".
*/
  public static final String SLIPPERYMOTIFKEY = "SlipperyMotif";
  
/**
* Key of parameter in Motifs.txt of ProteaseCleavageMotif = "ProteaseCleavageMotif".
*/
  public static final String PROTEASECLEAVAGEMOTIFKEY = "ProteaseCleavageMotif";
  
/**
* Key of parameter in Motifs.txt of PseudoKnotMotif = "PseudoKnotMotif".
*/
  public static final String PSEUDOKNOTMOTIFKEY = "PseudoKnotMotif";
  
/**
* Name of file in Database with SubGene distance information (="SubGenes.txt").
*/
	public static final String SUBGENEFILENAME = "SubGenes.txt";

/**
* Key of parameter with SubGene names = "Names";
*/
	public static final String SUBGENENAMESKEY = "Names";
	
/**
* Name of file in Database with information about relations between
* SubGenes, MotifGroups and Motif classes (="MotifGroups.txt").
*/
	public static final String MOTIFGROUPFILENAME = "MotifGroups.txt";
	
/**
* Name of file in Database with Gene information (="Genes.txt").
*/
	public static final String GENEFILENAME = "Genes.txt";
	
/**
* Name of file in Database with reference information = "forretrotector.txt".
*/
	public static final String FORRETROTECTORFILENAME = "forretrotector.txt";
	
/**
* Key of parameter in forretrotector.txt of refrvs = "refrvs".
*/
  public static final String REFRVSFKEY = "refrvs";
  
/**
* Key of parameter in forretrotector.txt of repbasetemplates = "repbasetemplates".
*/
  public static final String REPBASETEMPLATESKEY = "repbasetemplates";
  
/**
* Key of parameter in forretrotector.txt of polproteins = "polproteins".
*/
  public static final String POLPROTEINSKEY = "polproteins";
	
// to store contents of forretrotector.txt
	private Utilities.TemplatePackage[ ] repbasetemplates;
	private Utilities.TemplatePackage[ ] refrvs;
	private String[ ][ ] polproteins;
	private static final int POLNAMEINDEX = 1;
	private static final int POLSEQUENCEINDEX = 0;
  

// for all subdirectories of Database
  private static Hashtable allDatabases = new Hashtable();

/**
*	@param	name	The name of a subdirectory of Database.
*	@return	A Database object representing that subdirectory.
*/
  public final static Database getDatabase(String name) throws RetroTectorException {
    Database db = (Database) allDatabases.get(name);
    if (db == null) {
			db = new Database(name);
      allDatabases.put(name, db);
    }
    return db;
  } // end of getDatabase(String)
	
/**
* @return	Database/Ordinary directory.
*/
  public final static Database getOrdinaryDatabase() throws RetroTectorException {
		return getDatabase(Executor.ORDINARYDATABASE);
	} // end of getOrdinaryDatabase()

/**
* Utility routine constructing a Motif instance from contents of Motif.dataLine.
*/
  public final static Motif getOneMotif() throws RetroTectorException {
    String n = Motif.dataLine.substring(Motif.MOTIFIDLENGTH, Motif.MOTIFIDLENGTH + Motif.MOTIFSEQ_TYPELENGTH).trim(); //get seq_type field
    Class c = (Class) Motif.motifClassTable.get(n);
    if (c == null) {
      throw new RetroTectorException("Motif", "Motif type not identified", n);
    }
    Motif result = null;
    try {
      result = (Motif) c.newInstance();
    } catch (Exception e) {
      if (e instanceof RetroTectorException) {
				merrcount++;
				Utilities.outputString("  Motif " + Motif.dataLine.substring(0, Motif.MOTIFIDLENGTH) + " could not be created");
      } else {
        throw new RetroTectorException("Motif", "Could not make Motif from", Motif.dataLine);
      }
    }
    return result;
  } // end of getOneMotif()
	
	private static int merrcount = 0; // to keep count of failed motifs


/**
* The name of the directory defining this.
*/
  public final String DATABASENAME;

/**
* The directory defining this.
*/
  public final File DATABASESOURCE;

/**
* Number of MotifGroups found.
*/
  public final int NROFMOTIFGROUPS;

/**
* Array of all instances of subclasses of Motif defined under MOTIFSKEY in Motifs.txt.
*/
  public final Motif[ ] ALLMOTIFS;

/**
* Array of all instances of subclasses of Motif defined under LTR1MOTIFSKEY in Motifs.txt.
*/
  public final OrdinaryMotif[ ] LTR1MOTIFS;

/**
* Array of all instances of subclasses of Motif defined under LTR2MOTIFSKEY in Motifs.txt.
*/
  public final OrdinaryMotif[ ] LTR2MOTIFS;

/**
* Hashtable of all instances of subclasses of Motif defined under COMPONENTMOTIFSKEY in Motifs.txt.
*/
  public final Hashtable COMPONENTMOTIFS = new Hashtable();
	
/**
* Kozak consensus motif.
*/
  public final OrdinaryMotif KOZAKMOTIF;

/**
* Array of all MotifLitters.
*/
	public MotifLitter[ ] allLitters;
	
/**
* The names of all SubGenes.
*/
	public String[ ] subGeneNames;

/**
* The Motif for the 5'LTR.
*/
  public Motif ltr5Motif = null;

/**
* The Motif for the 3'LTR.
*/
  public Motif ltr3Motif = null;
	
/**
* SubGene containing ltr5Motif.
*/
  public SubGene ltr5SubGene = null;

/**
* SubGene containing lt35Motif.
*/
  public SubGene ltr3SubGene = null;
	
/**
* Names of all Genes.
*/
	public String[ ] geneNames = new String[4];

// to get Genes by name
	private Hashtable allGenes = new Hashtable();

// to get MotifGroups by name
	private Hashtable allMotifGroups = new Hashtable();
	
// to get SubGenes by name.
	private Hashtable allSubGenes = new Hashtable();
	
// to read contents of Motifs.txt into
	private Hashtable motifsParameterTable = new Hashtable();
	
// to read contents of forretrotector.txt int
	private Hashtable forretrotectorTable = new Hashtable();
  

// constructor
  private Database(String name) throws RetroTectorException {
    DATABASENAME = name;
    DATABASESOURCE = new File(Utilities.databaseDirectory, name);
		if (!DATABASESOURCE.exists()) {
			throw new RetroTectorException("Database", "Database subdirectory " + name + " does not exist");
		}

// read motif hierarchy. Correct order is important
    RetroTectorEngine.setInfoField("Reading SubGenes");
    readSubGenes();
    RetroTectorEngine.setInfoField("Reading MotifGroups");
    NROFMOTIFGROUPS = readMotifGroups();
    RetroTectorEngine.setInfoField("Reading Genes");
    readGenes();
    RetroTectorEngine.setInfoField("Reading Motifs");
		merrcount = 0;
		ParameterFileReader reader = new ParameterFileReader(getFile(MOTIFFILENAME), motifsParameterTable);
		reader.readParameters();
		reader.close();
    Motif mo;

		String[ ] mlines = (String[ ]) motifsParameterTable.get(COMPONENTMOTIFSKEY);
		if (mlines != null) {
      for (int i=0; i<mlines.length; i++) {
        Motif.dataLine = mlines[i];
        Motif.motifDataBase = this;
        mo = getOneMotif();
        COMPONENTMOTIFS.put(new Integer(mo.MOTIFID), mo);
      }
		}

		mlines = (String[ ]) motifsParameterTable.get(MOTIFSKEY);
		Stack mstack = new Stack();
		Motif mot;
		for (int i=0; i<mlines.length; i++) {
			Motif.dataLine = mlines[i];
			Motif.motifDataBase = this;
      mot = getOneMotif();
			if (mot != null) {
				mstack.push(mot);
				if (mot.MOTIFSUBGENE == ltr5SubGene) {
					if (ltr5Motif != null) {
						RetroTectorException.sendError(this, "Duplicate 5LTR Motif");
					} else {
						ltr5Motif = mot;
					}
				}
				if (mot.MOTIFSUBGENE == ltr3SubGene) {
					if (ltr3Motif != null) {
						RetroTectorException.sendError(this, "Duplicate 3LTR Motif");
					} else {
						ltr3Motif = mot;
					}
				}
			}
		}
		if (merrcount > 0) {
			RetroTectorEngine.displayError(new RetroTectorException("Database", "" + merrcount + " Motifs could not be created"), RetroTectorEngine.WARNINGLEVEL);
		}
		ALLMOTIFS = new Motif[mstack.size()];
		mstack.copyInto(ALLMOTIFS);

		mlines = (String[ ]) motifsParameterTable.get(LTR1MOTIFSKEY);
		LTR1MOTIFS = new OrdinaryMotif[mlines.length];
		for (int i=0; i<mlines.length; i++) {
			Motif.dataLine = mlines[i];
			Motif.motifDataBase = this;
      LTR1MOTIFS[i] = (OrdinaryMotif) getOneMotif();
		}

		mlines = (String[ ]) motifsParameterTable.get(LTR2MOTIFSKEY);
		LTR2MOTIFS = new OrdinaryMotif[mlines.length];
		for (int i=0; i<mlines.length; i++) {
			Motif.dataLine = mlines[i];
			Motif.motifDataBase = this;
      LTR2MOTIFS[i] = (OrdinaryMotif) getOneMotif();
		}

		mlines = (String[ ]) motifsParameterTable.get(KOZAKMOTIFKEY);
    Motif.dataLine = mlines[0];
    Motif.motifDataBase = this;
    KOZAKMOTIF = (OrdinaryMotif) getOneMotif();
    Motif.dataLine = null;

		Utilities.outputString(ALLMOTIFS.length + " Motifs constructed");
    RetroTectorEngine.setInfoField("Building MotifLitters");
    collectLitters();
    RetroTectorEngine.setInfoField("Finalizing SubGenes");
    finalizeAllSubGenes();
    RetroTectorEngine.setInfoField("Finalizing MotifGroups");
    finalizeAllGroups();
		
// read forretrotector,txt
		try {
			reader = new ParameterFileReader(getFile(FORRETROTECTORFILENAME), forretrotectorTable);
			reader.readParameters();
			reader.close();
			int ind;
			String[ ] ss = (String[ ]) forretrotectorTable.get(REFRVSFKEY);
			refrvs = new Utilities.TemplatePackage[ss.length];
			for (int i=0; i<ss.length; i++) {
				ind = ss[i].indexOf("\t");
				refrvs[i] = new Utilities.TemplatePackage(ss[i].substring(0, ind), ss[i].substring(ind + 1), 11, false, "");
			}
			ss = (String[ ]) forretrotectorTable.get(REPBASETEMPLATESKEY);
			repbasetemplates = new Utilities.TemplatePackage[ss.length * 2];
			String repsh;
			String cl;
			String seq;
			int p = 0;
			for (int i=0; i<ss.length; i++) {
				ind = ss[i].indexOf("\t");
				repsh = ss[i].substring(0, ind);
				ss[i] = ss[i].substring(ind + 1);
				ind = ss[i].indexOf("\t");
				cl = ss[i].substring(0, ind);
				seq = ss[i].substring(ind + 1);
				repbasetemplates[p++] = new Utilities.TemplatePackage(repsh, seq, 11, false, cl);
				repbasetemplates[p++] = new Utilities.TemplatePackage(repsh + "<", seq, 11, true, cl);
			}
			ss = (String[ ]) forretrotectorTable.get(POLPROTEINSKEY);
			polproteins = new String[ss.length][2];
			for (int i=0; i<ss.length; i++) {
				ind = ss[i].indexOf("\t");
				polproteins[i][POLNAMEINDEX] = ss[i].substring(0, ind);
				polproteins[i][POLSEQUENCEINDEX] = ss[i].substring(ind + 1);
			}
			forretrotectorTable = null;
		} catch (RetroTectorException re) {
		}
			
  } // end of constructor(String)
	
/**
*	@param	fileName	Name of a file.
*	@return	A file with that name, from the subdirectory represented by this, or
* if there is none, from Ordinary.
*/
	public final File getFile(String fileName) throws RetroTectorException {
		File f = new File(DATABASESOURCE, fileName);
		if (f.exists()) {
			return f;
		}
// sedarch for same name, case insensitive
		String[ ] flist = getFileList();
		for (int fi=0; fi<flist.length; fi++) {
			if (fileName.equalsIgnoreCase(flist[fi])) {
				return new File(DATABASESOURCE, flist[fi]);
			}
		}
		if (Executor.ORDINARYDATABASE.equals(DATABASESOURCE.getName())) {
			RetroTectorException.sendError(this, fileName + " does not exist in Ordinary");
		}
		return getOrdinaryDatabase().getFile(fileName);
	} // end of getFile(String)

/**
*	@return	The names of all the files in the subdirectory represented by this.
*/
	public final String[ ] getFileList() {
		return DATABASESOURCE.list();
	} // end of getFileList()
	
/**
* @param	name	A SubGene name.
* @return	The SubGene with name name, or null.
*/
	public final SubGene getSubGene(String name) {
		return (SubGene) allSubGenes.get(name);
	} // end of getSubGene(String)
	
/**
* @param	index	An integer index in subGeneNames.
* @return	The SubGene in question, or null.
*/
	public final SubGene getSubGene(int index) {
		if ((index < 0) | (index >= subGeneNames.length)) {
			return null;
		}
		return (SubGene) allSubGenes.get(subGeneNames[index]);
	} // end of getSubGene(int)

/**
* Reads information from SubGenes.txt.
*/
	private final void readSubGenes() throws RetroTectorException {
	  Hashtable table = new Hashtable();
		ParameterFileReader reader = new ParameterFileReader(getFile(SUBGENEFILENAME), table);
	  reader.readParameters();
	  reader.close();
	  subGeneNames = Utilities.splitString((String) table.get(SUBGENENAMESKEY)); // get SubGene names
    if (subGeneNames.length > SubGene.MAXSUBGENENUMBER) {
      RetroTectorException.sendError(this, "Number of SubGenes exceeds " + SubGene.MAXSUBGENENUMBER);
    }
	  for (int i=0; i<subGeneNames.length; i++) {
      SubGene sg = new SubGene(subGeneNames[i], i, this);
	  	allSubGenes.put(subGeneNames[i], sg);
      if (subGeneNames[i].equals(Executor.LTR5KEY)) {
        if (ltr5SubGene != null) {
          RetroTectorException.sendError(this, "Duplicate 5LTR SubGene");
        } else {
          ltr5SubGene = sg;
        }
      }
      if (subGeneNames[i].equals(Executor.LTR3KEY)) {
        if (ltr3SubGene != null) {
          RetroTectorException.sendError(this, "Duplicate 3LTR SubGene");
        } else {
          ltr3SubGene = sg;
        }
      }
	  }
	  for (int i=0; i<subGeneNames.length; i++) { // add distance information to each
	  	String distanceString = (String) table.get(subGeneNames[i]);
	  	SubGene sg = getSubGene(subGeneNames[i]);
	  	sg.makeDistances(distanceString);
	  }
	} // end of readSubGenes()
	
/**
* Make final form of all SubGenes.
*/
	private final void finalizeAllSubGenes() {
		for (int i=0; i<subGeneNames.length; i++) {
			getSubGene(subGeneNames[i]).finalizeSubGene();
		}
		Utilities.outputString(subGeneNames.length + " SubGenes constructed");
	} // end of finalizeAllSubGenes()
	
/**
* @return	An Enumeration of all MotifGroups.
*/
	public final Enumeration enumerateMotifGroups() {
		return allMotifGroups.elements();
	} // end of enumerateMotifGroups()
	
/**
* Reads all MotifGroups from file MotifGroups.txt.
* Lines in this have the format:
* |name|: |SubGene name| |Range specifier|
*	optionally extended by fields of format
* |MotifGroup name|:|Range specifier|
* @return	Number of MotifGroups read.
*/
	private final int readMotifGroups() throws RetroTectorException {
		Hashtable table = new Hashtable();
		ParameterFileReader reader = new ParameterFileReader(getFile(MOTIFGROUPFILENAME), table);
		String name;
    MotifGroup mg;
    int count = 0;
		while ((name = reader.readOneParameter()) != null) {
      mg = new MotifGroup(name, (String) table.get(name), this);
      mg.SUBGENE.addGroup(mg);
			allMotifGroups.put(name, mg);
      count++;
		}
		reader.close();
    return count;
	} // end of readMotifGroups()
	
/**
* Executes finalize() on all MotifGroups.
*/
	private final void finalizeAllGroups() {
		Enumeration e = allMotifGroups.elements();
		while (e.hasMoreElements()) {
			((MotifGroup) e.nextElement()).finalizeMotifGroup();
		}
	} // end of finalizeAllGroups()

/**
* @return MotifGroup with the specified name, or null
*/
	public final MotifGroup getMotifGroup(String name) {
		return (MotifGroup) allMotifGroups.get(name);
	} // end of getMotifGroup(String)

/**
* @return	Gene with name name, or null.
*/
	public final Gene getGene(String name) {
		return (Gene) allGenes.get(name);
	} // end of getSubGene(String)

/**
* Reads Genes from Genes.txt.
*/
	private final void readGenes() throws RetroTectorException {
	  Hashtable table = new Hashtable();
		ParameterFileReader reader = new ParameterFileReader(getFile(GENEFILENAME), table);
		String fgName;
		int p = 0;
		while ((fgName = reader.readOneParameter()) != null) {
			geneNames[p++] = fgName;
		}
		reader.close();
		for (int pp=0; pp<geneNames.length; pp++) {
			String sp = geneNames[pp];
			allGenes.put(sp, new Gene(sp, table.get(sp), this));
		}
	} // end of readGenes()
	
/**
* For debugging.
*/
	void printGenes() {
		for (int i=0; i<geneNames.length; i++) {
			Utilities.debugPrint(null, getGene(geneNames[i]).toString());
		}
	} // end of printGenes()

/**
* Contructs all Motiflitters, ALLMOTIFS being available.
*/
	private final void collectLitters() throws RetroTectorException {
// construct all litters.
 		Stack templitters = new Stack(); // stack of MotifLitters
 		MotifLitter templitter;
		for (int m=0; m<ALLMOTIFS.length; m++) { // go through all Motifs
			Enumeration e = templitters.elements();
			boolean found = false;
			mlsearch: while (e.hasMoreElements()) { // does this motif belong to any litter?
				templitter = (MotifLitter) e.nextElement();
				if (templitter.addMotif(ALLMOTIFS[m])) {
					found = true; // yes
					continue mlsearch; // is this needed?
				}
			}
			if (!found) { // no, make new litter
				templitters.push(new MotifLitter(ALLMOTIFS[m]));
			}
		}
// all motifs tried. Make litters array out of templitters Stack.
		allLitters = new MotifLitter[templitters.size()];
		for (int l=0; l<allLitters.length; l++) {
			MotifLitter ml = (MotifLitter) templitters.elementAt(l);
			ml.finalizeLitter();
			allLitters[l] = ml; // put into array
			ml.LITTERMOTIFGROUP.addLitter(ml); // and into MotifGroup
		}
		Utilities.outputString(allLitters.length + " MotifLitters constructed");
	} // end of collectLitters()
	
/**
*	@param	s	Hopefully the key of a parameter in Motifs.txt.
*	@return	The parameter in question.
*/
	public final Object getMotifParameter(String s) {
		return motifsParameterTable.get(s);
	} // end of getMotifParameter(String)

/**
*	@param	s	Hopefully the key of a parameter in Motifs.txt.
*	@return	The first Motif in the parameter in question.
*/
	public final Motif getFirstMotif(String s) throws RetroTectorException {
		String[ ] mlines = (String[ ]) getMotifParameter(s);
		Motif.dataLine = mlines[0];
		Motif.motifDataBase = this;
		return getOneMotif();
	} // end of getFirstMotif(String)
	
/**
*	@param	paramet			Hopefully the key of a parameter in Motifs.txt.
*	@param	motifGroup	Name of a MotifGroup.
*	@return	All Motifs in the parameter and MotifGroup in question.
*/
	public final Motif[ ] getMotifs(String paramet, String motifGroup) throws RetroTectorException {
		String[ ] mlines = (String[ ]) getMotifParameter(paramet);
		Stack st = new Stack();
		Motif.motifDataBase = this;
		for (int i=0; i<mlines.length; i++) {
			Motif.dataLine = mlines[i];
			if (Motif.motifGroupPart(Motif.dataLine).equals(motifGroup)) {
				st.push(getOneMotif());
			}
		}
		Motif[ ] result = new Motif[st.size()];
		st.copyInto(result);
		return result;
	} // end of getMotifs(String, String)
	
/**
* @return polproteins
*/
	public final String[ ][ ] getPolProteins() {
		return polproteins;
	} // end of getPolProteins()
	
/**
* @return repbasetemplates
*/
	public final Utilities.TemplatePackage[ ] getRepBaseTemplates() {
		return repbasetemplates;
	} // end of getRepBaseTemplates()
	
/**
* @return refrvs
*/
	public final Utilities.TemplatePackage[ ] getRefRVs() {
		return refrvs;
	} // end of getRefRVs()
	
}
