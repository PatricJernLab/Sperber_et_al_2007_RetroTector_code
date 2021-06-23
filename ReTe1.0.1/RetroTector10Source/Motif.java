/*
* Copyright (©) 2000-2006, Gšran Sperber & Tore Eriksson. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Tore Eriksson & Gšran Sperber
* @version 16/9 -06
* Beautified 16/9 -06
*/
package retrotector;

import java.util.*;
import java.lang.reflect.*;


/**
 * Class defining the properties and methods of motifs.<BR>
 *<BR>
 * Subclasses must define the methods<BR>
 *		public static String classType()<BR>
 *		public void refresh(RefreshInfo theInfo) throws RetroTectorException<BR>
 *		public void localRefresh(int firstpos, int lastpos) throws RetroTectorException<BR>
 *		public Motif motifCopy() throws RetroTectorException<BR>
 *<BR>
 * Subclasses should carefully consider overriding the methods<BR>
 *		public MotifHit getMotifHitAt(int position) throws RetroTectorException<BR>
 *		public String correspondingDNA(MotifHit theHit) throws RetroTectorException<BR>
 *		public RVector getRVector()<BR>
 *
 *	and defining the fields frameDefined, exactlyAligned and showAsBases
 */
public abstract class Motif extends ParameterUser {

/**
* Information which may be needed by Motif.refresh();
*/
	public static class RefreshInfo {
	
/**
* DNA to refresh from.
*/
		public final DNA TARGETDNA;
		
/**
* Bonus factor for conserved positions.
*/
		public final float CFACTOR;
	
/**
* Coefficient for standard deviation when setting threshold.
*/
		public final float SDFACTOR;
		
/**
* Table with LTRPair descriptions, for LTRMotif.
*/
		public final Hashtable LTRTABLE;
	
/**
* Constructor.
* @param	targetDNA			See TARGETDNA.
* @param	cfactor				See CFACTOR.
* @param	sdfactor			See SDFACTOR.
* @param	ltrtable			See LTRTABLE.
*/
		public RefreshInfo(DNA targetDNA, float cfactor, float sdfactor, Hashtable ltrtable) {
			TARGETDNA = targetDNA;
			CFACTOR = cfactor;
			SDFACTOR = sdfactor;
			LTRTABLE = ltrtable;
		} // end of RefreshInfo.constructor(DNA, float, float, Hashtable)
		
	} // end of RefreshInfo


/**
* Utility class for the use of SplitAcidMotif, describing one of its parts.
*/
  public class SplitAcidComponent {

/**
* The acid pattern.
*/
    public final byte[ ] PATTERN;

/**
* The range of its offset from next part.
*	Is + in PRECOMPONENTS, - in POSTCOMPONENTS.
*/
    public final DistanceRange OFFSET;

/**
* The scores resulting from this and underlying SplitAcidComponents at each position.
*/
    public float[ ] outscores;

// for score in DNA + maximum from preceding SplitAcidComponent
    private float[ ] inscores;

// to find maximum in inscores
    private TopFinder finder = null;


/**
* Constructor for PRECOMPONENTS
* @param	s				String of form xyz(1-2)
* @param	motifid	MOTIFID of calling Motif, for error message.
*/
    public SplitAcidComponent(String s, int motifid) throws RetroTectorException {
      int lind = s.indexOf("(");
      int rind = s.indexOf(")");
      if ((lind <= 0) | (rind < lind) | (rind != (s.length() - 1))) {
        RetroTectorException.sendError(this, "Syntax error in", s);
      }
      String pat = s.substring(0, lind);
      OFFSET = (new DistanceRange(s.substring(lind + 1, rind))).offset(pat.length() * 3);
      PATTERN = Motif.acidPattern(pat, Compactor.ACIDCOMPACTOR, motifid);
    } // end of SplitAcidComponent.constructor(String, int)

/**
* Constructor for POSTCOMPONENTS
* @param	s				String of form (1-2)xyz.
* @param	offs		3 times preceding pattern length.
* @param	motifid	MOTIFID of calling Motif, for error message.
*/
    public SplitAcidComponent(String s, int offs, int motifid) throws RetroTectorException {
      int lind = s.indexOf("(");
      int rind = s.indexOf(")");
      if (lind != 0) {
        RetroTectorException.sendError(this, "Syntax error in", s);
      }
      String pat = s.substring(rind + 1);
      OFFSET = (new DistanceRange(s.substring(lind + 1, rind))).offset(offs).inverse();
      PATTERN = Motif.acidPattern(pat, Compactor.ACIDCOMPACTOR, motifid);
    } // end of SplitAcidComponent.constructor(String, int, int)

/**
* Calculates outscores.
* @param	dna							The relevant DNA.
* @param	subComponent		Preceding SplitAcidComponent in hierarchy.
* @param	cfact						Bonus factor for conserved acids.
* @param	stopscoreInDNA	Score for stop codon as in DNA.acidRawScoreAt().
*/
    public final void makeScores(DNA dna, SplitAcidComponent subComponent, float cfact, float stopscoreInDNA) {
      inscores = new float[dna.LENGTH];
      int ii;
// collext outscores from preceding component
      if (subComponent != null) {
        if (subComponent.OFFSET.HIGHESTDISTANCE >= 0) { // PRECOMPONENT
          for (int i=subComponent.OFFSET.HIGHESTDISTANCE; i<dna.LENGTH; i++) {
            inscores[i] = subComponent.outscores[i - subComponent.OFFSET.HIGHESTDISTANCE];
          }
        } else { // POSTCOMPONENT
          for (int i=0; i<(dna.LENGTH + subComponent.OFFSET.HIGHESTDISTANCE); i++) {
            inscores[i] = subComponent.outscores[i - subComponent.OFFSET.HIGHESTDISTANCE];
          }
        }
      }
// add scores for PATTERN
      for (int i=0; i<(dna.LENGTH - PATTERN.length * 3); i++) {
        inscores[i] += dna.acidRawScoreAt(PATTERN, i, cfact, stopscoreInDNA );
      }
// put maxima into outscores
      finder = new TopFinder(inscores, 2, 10);
      outscores = new float[dna.LENGTH];
      finder.sweep(outscores, OFFSET.HIGHESTDISTANCE - OFFSET.LOWESTDISTANCE + 1);
    } // end of SplitAcidComponent.makeScores(DNA, SplitAcidComponent, float, float)

/**
* @param	pos	A position in DNA.
* @return	Index of highest value in inscores within OFFSET relative to pos.
*/
    public final int maxRelativeTo(int pos) {
      finder.maxvalue(pos - OFFSET.HIGHESTDISTANCE, pos - OFFSET.LOWESTDISTANCE);
      return finder.maxpos;
    } // end of SplitAcidComponent.maxRelativeTo(int)

/**
* Removes finder, inscores and outscores.
*/
    public final void clean() {
      inscores = null;
      outscores = null;
      finder = null;
    } // end of SplitAcidComponent.clean()
    
  } // end of SplitAcidComponent



// public static fields
/**
* In MOTIFLEVEL for detection Motif (="D").
*/
	public static final String DETECTIONLEVEL = "D";

/**
* In MOTIFLEVEL for characterization Motif (="C").
*/
	public static final String CHARACTERIZATIONLEVEL = "C";
	
/**
* String to use in Motifs.txt when SubGene or MotifGroup is not defined = "????".
*/
	public static final String UNKNOWNSTRING = "????";

/**
* Hashtable of all Motif subclasses found.
*/
	public static Hashtable motifClassTable;
	
/**
* Used for communication with subclasses.
*/
	public static String dataLine = null;

/**
* Used for communication with subclasses.
*/
	public static Database motifDataBase = null;

// non-public static fields
/**
* Length of motifid field in Motifs.txt = 11.
*/
	static final int MOTIFIDLENGTH = 11;

/**
* Length of seq_type field in Motifs.txt = 5.
*/
	static final int MOTIFSEQ_TYPELENGTH = 5;

/**
* Length of motiflevel field in Motifs.txt = 2.
*/
	static final int MOTIFLEVELELENGTH = 2;

/**
* Length of virus genus field in Motifs.txt = 25.
*/
	static final int MOTIFRVECTORLENGTH = 25;

/**
* Length of individual SDFactor field in Motifs.txt = 5.
*/
	static final int MOTIFSDFACTORLENGTH = 5;

/**
* Length of subgene field in Motifs.txt = 10.
*/
	static final int MOTIFSUBGENENAMELENGTH = 10;

/**
* Length of motifgroup field in Motifs.txt = 5.
*/
	static final int MOTIFGROUPLENGTH = 5;

/**
* Length of maxscore field in Motifs.txt = 5.
*/
	static final int MAXSCORELENGTH = 5;

/**
* Length of parameter field in Motifs.txt = 5.
*/
	static final int PARAMLENGTH = 5;

/**
* Length of motif field in Motifs.txt = 55.
*/
	static final int MOTIFSTRINGLENGTH = 55;
	
  
// static methods
/**
* Must be overridden by subclasses.
* @return	String identifying the class. Same as found in column 2 of Motifs.txt.
*/
	public static String classType() {
   return null;
  } // end of classType
  
/**
* @param	acidString	String of amino acid symbols.
* @param	compactor		Compactor to use.
* @param	motifid			MOTIFID of calling Motif, for error message.
* @return	Array of acid codes, with ambiguous acids as -1 and bit 6 set for uppercase characters.
*/
  public final static byte[ ] acidPattern(String acidString, Compactor compactor, int motifid) throws RetroTectorException {
    byte[ ] pattern = new byte[acidString.length()];
    int xi;
    for (int i=0; i<acidString.length(); i++) {
      xi = compactor.charToIntId(acidString.charAt(i));
      if (xi < 0) {
        throw new RetroTectorException("Motif", "Illegal character in Motif " + motifid, acidString.charAt(i) + " in", acidString);
      } else if (xi > 63) { // ambiguous acid in strand
        pattern[i] = -1;
      } else {
        if ( Character.isUpperCase( acidString.charAt( i ) ) ) {
          xi |= 64; // Set bit to mark conserved position
        }
        pattern[i] = (byte) xi;
      }
    }
    return pattern;
  } // end of acidPattern(String, Compactor, int)

/**
* @param	pattern	As created by acidPattern().
* @param	matrix	The current AcidMatrix.
* @return	The maximum score of pattern,
*/
  public final static float acidPatternMaxScore(byte[ ] pattern, AcidMatrix matrix) {

    float bestScore = 0;
    for (int i=0; i<pattern.length; i++) {
      if (pattern[i] >= 0) {
        bestScore += matrix.floatMatrix[ pattern[i]][ pattern[i] & 63];
      }
    }
    return bestScore;
  } // end of acidPatternMaxScore(byte[ ], AcidMatrix)
                               
/**
* Collects Motif subclasses (or, to be precise, classes with a 'classType' method)
* from PluginManager.BUILTANDPLUGINS.
*/
	static final void collectMotifClasses() throws RetroTectorException {
		motifClassTable = new Hashtable();
		Class c;
		String na; // for error message
    int ind;
		for (int i=0; i<PluginManager.BUILTANDPLUGINS.length; i++) {
      c = PluginManager.BUILTANDPLUGINS[i];
      na = c.getName();
      ind = na.lastIndexOf(".");
      na = na.substring(ind + 1);
      if (Utilities.subClassOf(c, "retrotector.Motif")) {
				String ty = null;
				try {
					Method m = c.getMethod("classType", null);
					Object o = m.invoke(null, new Object[0]); // it had a classType method
					ty = (String) o;
					if (motifClassTable.containsKey(ty)) {
						throw new RetroTectorException("Motif", "Duplicate Motif type", ty);
					}
					motifClassTable.put(ty, c);
				} catch (NoSuchMethodException nsme) { // it was not a Motif subclass
				} catch (Exception e) {
					throw new RetroTectorException("Motif", "Could not get method classType in", na);
				}
			}
		}
	} // end of collectMotifClasses()
  
/**
* @return	The part of dataLine that should contain the type identifier.
*/
  public final static String seq_typePart() {
    return dataLine.substring(MOTIFIDLENGTH, MOTIFIDLENGTH + MOTIFSEQ_TYPELENGTH).trim();
  } // end of seq_typePart()
	
/**
* @param	s	A String, presumably a line from Motifs.txt.
* @return	The part of s that should contain the SubGene name.
*/
	public final static String subGenePart(String s) {
		int i = MOTIFIDLENGTH + MOTIFSEQ_TYPELENGTH + MOTIFLEVELELENGTH + MOTIFRVECTORLENGTH + MOTIFSDFACTORLENGTH; 
		return s.substring(i, i + MOTIFSUBGENENAMELENGTH).trim();
	} // end of subGenePart()
	
/**
* @param	s	A String, presumably a line from Motifs.txt.
* @return	The part of s that should contain the MotifGroup name.
*/
	public final static String motifGroupPart(String s) {
		int i = MOTIFIDLENGTH + MOTIFSEQ_TYPELENGTH + MOTIFLEVELELENGTH + MOTIFRVECTORLENGTH + MOTIFSDFACTORLENGTH  + MOTIFSUBGENENAMELENGTH; 
		return s.substring(i, i + MOTIFGROUPLENGTH).trim();
	} // end of motifGroupPart()
	
// public fields
/**
 * From motifid in Motif database. Mainly useful for debugging and in CoincidenceMotifs.
 */
 	public final int MOTIFID;

/**
 * From seq_type in Motif database.
 */
	public final String MOTIFTYPE;

/**
* "D" for detection Motif, "C" for characterisation Motif.
*/
	public final String MOTIFLEVEL;
	
/**
 * From rvgenus in Motif database. Specifies the virus genus(es) to which the motif belongs.
 */
	public final String MOTIFRVGENUS;

/**
* sdfactor for this Motif.
*/
	public final float MOTIFSDFACTOR;

/**
 * From subgene in Motif database. The SubGene to which the Motif belongs.
 */
	public final SubGene MOTIFSUBGENE;
	
/**
 * From motifgroup in Motif database. Specifies MotifGroup if any.
 */
	public final MotifGroup MOTIFGROUP;
	
/**
* Maximal score defined by Motif database.
*/
	public final float MAXSCORE;

/**
* Optional parameter from	Motif database.
*/
	public final String PARAM;

/**
 * From motif in Motif database. Typically the actual bases or acids in the motif, in text format.
 */
	public final String MOTIFSTRING;
	
/**
 * From origin in Motif database. A comment identifying the motif.
 */
	public final String MOTIFORIGIN;
	
/**
* RVector of this Motif.
*/
	public final RVector MOTIFRVECTOR;

/**
* The Database where this belongs.
*/
  public final Database MOTIFDATABASE;

/**
* A copy of the Dataline defining this.
*/
  protected final String SOURCESTRING;

/**
* True if reading frame of MotifHit is meaningful. Set by subclass constructor.
*/
	public boolean frameDefined;
	
/**
* True if Motif can have a unique position in an Alignment.
* At present true only for AcidMotifs and SplitAcidMotif.
*/
	public boolean exactlyAligned = false;
	
/*
* True if the String representation should use base codes.
*/
	public boolean showAsBases = false;
  

/**
* For the use of subclasses that prefer to make all hits at refresh time.
*/
  protected Hashtable motifHitTable;
  
/**
* What it sounds like. Set by refresh().
*/
  protected DNA currentDNA;
  
/**
* Constructor for dummy instance.
*/
	public Motif() {
		MOTIFID = -1;
		MOTIFTYPE = null;
		MOTIFLEVEL = null;
		MOTIFRVECTOR = null;
		MOTIFSDFACTOR = Float.NaN;
		MOTIFRVGENUS = null;
		MOTIFSUBGENE = null;
		MOTIFGROUP = null;
		MAXSCORE = Float.NaN;
		PARAM = null;
		MOTIFSTRING = null;
		MOTIFORIGIN = null;
    MOTIFDATABASE = null;
    SOURCESTRING = null;
	} // end of constructor()

/**
* Constructs an instance, mainly from data in Motifs.txt
* @param	data			String from Motifs.txt
* @param	database	Database of Motifs.txt
*/
  public Motif(String data, Database database) throws RetroTectorException {
    SOURCESTRING = data;
    MOTIFDATABASE = database;
	  int index = 0;
		MOTIFID = Utilities.decodeInt(data.substring(index, (index += MOTIFIDLENGTH)));
		MOTIFTYPE = data.substring(index, (index += MOTIFSEQ_TYPELENGTH)).trim();
		MOTIFLEVEL = data.substring(index, (index += MOTIFLEVELELENGTH)).trim();
		String rvg = data.substring(index, (index += MOTIFRVECTORLENGTH)).trim();
		if (rvg.length() == 0) {
			rvg = RVector.RVSTRING;
		}
		MOTIFRVECTOR = new RVector(rvg);
    String sdf = data.substring(index, (index += MOTIFSDFACTORLENGTH)).trim();
    if (sdf.length() == 0) {
      MOTIFSDFACTOR = Float.NaN;
    } else {
      MOTIFSDFACTOR = Utilities.decodeFloat(sdf);
    }
		MOTIFRVGENUS = MOTIFRVECTOR.rvGenus();
		String s = data.substring(index, (index += MOTIFSUBGENENAMELENGTH)).trim();
    if (s.equals(UNKNOWNSTRING)) {
      MOTIFSUBGENE = null;
    } else {
      MOTIFSUBGENE = MOTIFDATABASE.getSubGene(s);
      if (MOTIFSUBGENE == null) {
        RetroTectorException.sendError(this, "Undefined SubGene in Motif list:" + s);
      }
    }
		s = data.substring(index, (index += MOTIFGROUPLENGTH)).trim();
    if (s.equals(UNKNOWNSTRING)) {
      MOTIFGROUP = null;
    } else {
      MOTIFGROUP = MOTIFDATABASE.getMotifGroup(s);
      if (MOTIFGROUP == null) {
        RetroTectorException.sendError(this, "Undefined MotifGroup in Motif list:" + s);
      }
		}
			
		MAXSCORE = Float.valueOf(data.substring(index, (index += MAXSCORELENGTH)).trim()).floatValue();
		PARAM = data.substring(index, (index += PARAMLENGTH)).trim();
		MOTIFSTRING = data.substring(index, (index += MOTIFSTRINGLENGTH)).trim();
		MOTIFORIGIN = data.substring(index).trim();
    
  } // end of constructor(String, Database)

/**
* Constructor
* @param	database	Database to use
* @param	motifID				->MOTIFID
* @param	motifType			->MOTIFTYPE
* @param	motifLevel		->MOTIFLEVEL
* @param	rvg						->MOTIFRVECTOR
* @param	motifSDFactor	->MOTIFSDFACTOR
* @param	motifSubGene	->MOTIFSUBGENE
* @param	motifGroup		->MOTIFGROUP
* @param	maxScore			->MAXSCORE
* @param	param					->PARAM
* @param	motifString		->MOTIFSTRING
* @param	motifOrigin		->MOTIFORIGIN
*/
  public Motif(Database database, int motifID, String motifType, String motifLevel, String rvg, float motifSDFactor, SubGene motifSubGene, MotifGroup motifGroup, float maxScore, String param, String motifString, String motifOrigin) throws RetroTectorException {
    SOURCESTRING = null;
    MOTIFDATABASE = database;
		MOTIFID = motifID;
		MOTIFTYPE = motifType;
		MOTIFLEVEL = motifLevel;
		if (rvg.length() == 0) {
			rvg = RVector.RVSTRING;
		}
		MOTIFRVECTOR = new RVector(rvg);
		MOTIFSDFACTOR = motifSDFactor;
		MOTIFRVGENUS = MOTIFRVECTOR.rvGenus();
		MOTIFSUBGENE = motifSubGene;
		MOTIFGROUP = motifGroup;
		MAXSCORE = maxScore;
		PARAM = param;
		MOTIFSTRING = motifString;
		MOTIFORIGIN = motifOrigin;
	} // end of constructor(Database, int, String, String, String, float, SubGene, MotifGroup, float, String, String, String)
	
	
// public methods
/**
* @param	A MotifHit, presumably of this Motif.
* @return	A String representation of the DNA corresponding to theHit.
*/
	public String correspondingDNA(MotifHit theHit) {
		return theHit.SOURCEDNA.subString(theHit.MOTIFHITFIRST, theHit.MOTIFHITLAST, showAsBases);
	} // end of correspondingDNA(MotifHit)
	
	
/**
* Makes necessary preparation before scoring in a DNA. Sets currentDNA.
* @param	theInfo	RefreshInfo containing necessary information.
*/
	abstract public void refresh(RefreshInfo theInfo) throws RetroTectorException;

/**
* Makes further preparations before scoring within a part of currentDNA.
* @param	firstpos	The first (internal) positon in current DNA to prepare.
* @param	lastpos		The last (internal) positon in current DNA to prepare.
*/
  abstract public void localRefresh(int firstpos, int lastpos) throws RetroTectorException;
    
/**
* Returns a MotifHit or MotifHit[ ] at position, or null.
*/
  public Object getMotifHitsAt(int position) throws RetroTectorException {
    Integer in = new Integer(position);
    return motifHitTable.get(in);
  } // end of getMotifHitsAt(int)

/**
* Returns a single MotifHit at position, or null.
*/
  public MotifHit getMotifHitAt(int position) throws RetroTectorException {
		Object o = getMotifHitsAt(position);
		if (o instanceof MotifHit) {
			return (MotifHit) o;
		} else {
			return null;
		}
	} // end of getMotifHitAt(int)
		
/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  abstract public Motif motifCopy() throws RetroTectorException;

/**
* @return	Utility routine, helping motifCopy of subclasses. Replicates frameDefined and currentDNA.
*/
  protected final void motifCopyHelp(Motif mc) throws RetroTectorException {
    mc.frameDefined = frameDefined;
    mc.currentDNA = currentDNA;
  } // end of motifCopy(Motif)
  
/**
* @return	Typically a copy of MOTIFRVECTOR.
*/
	public RVector getRVector() {
		return MOTIFRVECTOR.copy();
  } // end of getRVector()
	
/**
* @param	position	A position (internal in DNA), presumably the hotspot position of a MotifHit.
* @return	The range of acceptable positions for hotspot of a SubGeneHit containing that MotifHit.
*/
	public final Range subGeneHotspotRange(int position) throws RetroTectorException {
    if (MOTIFGROUP == null) {
      return null;
    } else {
      return new Range(Range.SUBGENEHOTSPOT, position, MOTIFGROUP.DISTANCERANGE);
    }
	} // end of subGeneHotspotRange(int)

/**
* To help find out if a MotifHit of this type may be added to a SubGeneHit being built.
* @param	motifHit	A MotifHit of this type.
* @return	The acceptable Range of the hotspot of a SubGeneHit to which it belongs.
*/
	public final Range acceptedRange(MotifHit motifHit) throws RetroTectorException {
		checkClass(motifHit);
		if (SubGeneHit.peekRange() == null) { // no hit yet
			return motifHit.SUBGENEHITHOTSPOTOPINION;
		}
		return Range.consensus(SubGeneHit.peekRange(), motifHit.SUBGENEHITHOTSPOTOPINION);
	} // end of acceptedRange(MotifHit)

  
/**
* Throws a RetroTectorException if a MotifHit is not of this Motif.
* @param	hit	The MotifHit to check.
*/
	protected final void checkClass(MotifHit hit) throws RetroTectorException {
		if (hit.PARENTMOTIF != this) {
			RetroTectorException.sendError(this, "Motif type mismatch", "" + hit.PARENTMOTIF.MOTIFID + "<>" + this.MOTIFID);
		}
	} // end of checkClass(MotifHit
  
}
