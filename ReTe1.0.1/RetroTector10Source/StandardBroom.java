/*
* Copyright (©) 2000-2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Göran Sperber
* @version 29/9 -06
* Beautified 29/9 -06
*/
package retrotector;

import retrotectorcore.*;

import java.util.*;

/**
* Subclass of Broom using a consensus sequence and a poly-A tail.
*/
public abstract class StandardBroom extends Broom {

/**
* Key for length of sequence for initial search = "MerSize".
*/
	public static final String MERSIZEKEY = "MerSize";

/**
* Key for step by which sequence for initial search is moved = "MerStep".
*/
	public static final String MERSTEPKEY = "MerStep";

/**
* Key for error tolerance in dynamic programming = "Tolerance".
*/
	public static final String TOLERANCEKEY = "Tolerance";

/**
* Key for shortest acceptable a-tail length = "TailMinLength".
*/
	public static final String TAILMINKEY = "TailMinLength";
	
/**
* Code for 'a'.
*/
	public static final int ACODE = Compactor.BASECOMPACTOR.charToIntId('a');
	
/**
* Code for 't'.
*/
	public static final int TCODE = Compactor.BASECOMPACTOR.charToIntId('t');
	
/**
* @return	Sequence of prototype.
*/
	protected abstract String dirtString();
	
/**
* Length of sequence for initial search.
*/
	protected int merSize = 17;

/**
* Step by which sequence for initial search is moved.
*/
	protected int merStep = 43;

/**
* Error tolerance in dynamic programming.
*/
	protected float tolerance = 10.0f;
	
/**
* Shortest acceptable a-tail length.
*/
	protected int tailMin = -1;

/**
* @return	Length of sequence for initial search.
*/
	public final int getMerSize() {
		return merSize;
	} // end of getMerSize()
	
/**
* @return	Step by which sequence for initial search is moved.
*/
	public final int getMerStep() {
		return merStep;
	} // end of getMerStep()
	
/**
* @return	Error tolerance in dynamic programming.
*/
	public float getTolerance() {
		return tolerance;
	} // end of getTolerance()
	
/**
* @return	Minimum length of a-tail.
*/
	public final int getTailMin() {
		return tailMin;
	} // end of getTailMin()
	
/**
* @return	TemplatePackage representing primary strand of prototype.
*/
	protected final Utilities.TemplatePackage getDirtPackage() throws RetroTectorException {
		return new Utilities.TemplatePackage(getDirtName(), dirtString(), getMerSize(), false, null);
	} // end of getDirtPackage()
	
/**
* @return	TemplatePackage representing complementary strand of prototype.
*/
	protected final Utilities.TemplatePackage getComplementaryDirtPackage() throws RetroTectorException {
		return new Utilities.TemplatePackage(getCDirtName(), dirtString(), getMerSize(), true, null);
	} // end of getComplementaryDirtPackage()

/**
* @param		inDNA		DNA to search in.
* @return	inDNA with finds inserted.
*/
	public DNA findDirt(DNA inDNA) throws RetroTectorException {
		if (getTolerance() < 0) {
			return inDNA;
		}
		DNA dna = findDirt(inDNA, getDirtPackage());
		return findDirt(dna, getComplementaryDirtPackage());
	} // end of findDirt(DNA)
	
// searches for two non-a bases with less distance than TAILMIN between them, the last
// being at startat or later
// returns the last position before the second one
	private final int atail(int startat, DNA dna) {
		if (getTailMin() < 0) {
			return startat - 1;
		}
		int previous = Integer.MIN_VALUE;
		for (int p=dna.forceInside(startat - getTailMin()); p<startat; p++) {
			if (dna.get2bit(p) != ACODE) {
				previous = p;
			}
		}
		for (int pp=startat; pp<dna.LENGTH; pp++) {
			if (dna.get2bit(pp) != ACODE) {
				if ((pp - previous) < getTailMin()) {
					return pp - 1;
				} else {
					previous = pp;
				}
			}
		}
		return dna.LENGTH - 1;
	} // end of atail(int, DNA)
	
// searches for two non-t bases with less distance than TAILMIN between them, the first
// being at startat or earlier
// returns the last position after the second one
	private final int ttail(int startat, DNA dna) {
		if (getTailMin() < 0) {
			return startat + 1;
		}
		int previous = Integer.MAX_VALUE;
		for (int p=dna.forceInside(startat + getTailMin()); p>startat; p--) {
			if (dna.get2bit(p) != TCODE) {
				previous = p;
			}
		}
		for (int pp=startat; pp>=0; pp--) {
			if (dna.get2bit(pp) != TCODE) {
				if ((previous - pp) < getTailMin()) {
					return pp + 1;
				} else {
					previous = pp;
				}
			}
		}
		return 0;
	} // end of ttail(int, DNA)
	
/**
* @param		inDNA		DNA to search in.
* @param		pack		Utilities.TemplatePackage from one strand of prototype.
* @return	inDNA with finds inserted.
*/
	public DNA findDirt(DNA inDNA, Utilities.TemplatePackage pack) throws RetroTectorException {
		DNA currDNA = inDNA;
		LINEMatrix matrix1;
		LINEMatrix matrix2;
		int[ ] basecodes = currDNA.getBaseCodes();
		int[ ] newbasecodes;
		long[ ] dnamers = Utilities.merize(basecodes, 0, basecodes.length - 1, getMerSize(), true);
		int[ ] dnapart;
		int[ ] dirtpart;
		int[ ] idpair1;
		int[ ] idpair2;
		int linen = 1; // number of next dirt
		boolean again;
		long searchmer;
		
		for (int merpos = getMerSize(); merpos<(pack.TEMPLATEMERS.length-2*getMerSize()); merpos += getMerStep()) {
			searchmer = pack.TEMPLATEMERS[merpos];
			if (searchmer >= 0) {
				for (int result=0; result<dnamers.length - getMerSize(); result++) {
					if (dnamers[result] == searchmer) { // got one
						dirtpart = new int[merpos];
						for (int i=0; i<merpos; i++) {
							dirtpart[merpos - i - 1] = pack.TEMPLATECODES[i];
						}
						dnapart = new int[result];
						for (int i=0; i<result; i++) {
							dnapart[result - i - 1] = basecodes[i];
						}
						matrix1 = new LINEMatrix(dirtpart, dnapart,  1.0f, 1.0f, 0.95f, getTolerance()); // align backwards
						idpair1 = matrix1.LASTTRIAD;
						if (idpair1 == null) {
							idpair1 = new int[2];
							idpair1[0] = matrix1.TEMPLATEEND;
							idpair1[1] = matrix1.SEARCHEND;
						}
						
						dirtpart = new int[pack.TEMPLATECODES.length - merpos - getMerSize()];
						System.arraycopy(pack.TEMPLATECODES, merpos + getMerSize(), dirtpart, 0, dirtpart.length);
						dnapart = new int[basecodes.length - result - getMerSize()];
						System.arraycopy(basecodes, result + getMerSize(), dnapart, 0, dnapart.length);
						matrix2 = new LINEMatrix(dirtpart, dnapart,  1.0f, 1.0f, 0.95f, getTolerance()); // align forwards
						idpair2 = matrix2.LASTTRIAD;
						if (idpair2 == null) {
							idpair2 = new int[2];
							idpair2[0] = matrix2.TEMPLATEEND;
							idpair2[1] = matrix2.SEARCHEND;
						}
						int firstpos = result - idpair1[1] - 1;
						int lastpos = result + getMerSize() + idpair2[1];
						if (pack.TEMPLATECOMPLEMENTED) {
							firstpos = ttail(firstpos - 1, currDNA);
							if (currDNA.get2bit(firstpos) != TCODE) {
								firstpos++;
							}
						} else {
							lastpos = atail(lastpos + 1, currDNA);
							if (currDNA.get2bit(lastpos) != ACODE) {
								lastpos--;
							}
						}
						if ((lastpos - firstpos) >= 100) {
							try {
								Utilities.outputString(pack.TEMPLATENAME + " found at " + currDNA.externalize(firstpos) + " to " + currDNA.externalize(lastpos));
								currDNA = new DNA(currDNA, firstpos, lastpos, pack.TEMPLATENAME + " " + linen++ + " at " + (merpos - idpair1[0] - 1));
								incrementCount();
								newbasecodes = new int[currDNA.LENGTH];
								System.arraycopy(basecodes, 0, newbasecodes, 0, firstpos);
								try {
									System.arraycopy(basecodes, lastpos + 1, newbasecodes, firstpos, newbasecodes.length - firstpos);
								} catch (ArrayIndexOutOfBoundsException ae) {
									throw ae;
								}
								basecodes = newbasecodes;
								dnamers = Utilities.merize(basecodes, 0, basecodes.length - 1, getMerSize(), true);
								doneCount++;
							} catch (RetroTectorException e) {
							}
						}
					}
				}
			}
		}
		return currDNA;
	} // end of findDirt(DNA, Utilities.TemplatePackage)

}
