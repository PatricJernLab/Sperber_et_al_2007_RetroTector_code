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
 * Interface, mainly for the use of quicksort.
 */
public interface Scorable {

/**
* @return	A score, or Float.NaN as invalid
*/
	public abstract float fetchScore();

} // end of Scorable
