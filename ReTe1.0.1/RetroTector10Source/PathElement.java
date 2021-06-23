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

/**
* Data defining one step in an ORFID path.
*/
public class PathElement {

/**
* Position (internal) in DNA where this element starts.
*/
	public final int FROMDNA;
	
/**
* Position (internal) in DNA where this element ends.
*/
	public final int TODNA;

/**
* Position in alignment where this element starts.
*/
	public final int FROMMASTER;

/**
* Position in alignment where this element ends.
*/
	public final int TOMASTER;

/**
* Score associated with this element.
*/
	public final float SCORE;
	
/**
* Bitwise description of alignment rows corresponding to score.
*/
	public final int ROWCODE;

/**
* True if this step is in both DNA and alignment.
*/
	public boolean isRegular = false;

	private boolean isInsertion = false;
	private boolean isDeletion = false;
	
/**
* Constructor.
* @param	fromD	Position (internal) in DNA where this element starts.
* @param	toD		Position (internal) in DNA where this element ends.
* @param	fromM	Position in alignment where this element starts.
* @param	toM		Position in alignment where this element ends.
* @param	sc		Score associated with this element.
* @param	rowC	Bitwise description of alignment rows corresponding to score.
*/
	public PathElement(int fromD, int toD, int fromM, int toM, float sc, int rowC) throws RetroTectorException {
		FROMDNA = fromD;
		TODNA = toD;
		FROMMASTER = fromM;
		TOMASTER = toM;
		SCORE = sc;
		ROWCODE = rowC;
		checkContents();
	} // end of constructor (int, int, int, int, int, int)
	
	private final void checkContents() throws RetroTectorException {
		if (TODNA < FROMDNA) {
			throw new RetroTectorException(Utilities.className(this), "Faulty Path element");
		}
		if (TOMASTER < FROMMASTER) {
			throw new RetroTectorException(Utilities.className(this), "Faulty Path element");
		}
		if (((TODNA - FROMDNA) == 3) & ((TOMASTER - FROMMASTER) == 1)) {
			isRegular = true;
			return;
		}
		
// irregular element has no score
		if (ROWCODE != 0) {
			throw new RetroTectorException(Utilities.className(this), "Faulty Path element");
		} else if ((SCORE != 0) & ((TODNA - FROMDNA) != 3)) {
			throw new RetroTectorException(Utilities.className(this), "Faulty Path element");
		} else if (((TODNA - FROMDNA) <= 3) & ((TOMASTER - FROMMASTER) == 0)) {
			isInsertion = true;
		} else if (((TODNA - FROMDNA) < 3) & ((TOMASTER - FROMMASTER) == 1)) {
			isDeletion = true;
		} else {
			throw new RetroTectorException(Utilities.className(this), "Faulty Path element");
		}
	} // end of checkContents()
	
}// end of PathElement
