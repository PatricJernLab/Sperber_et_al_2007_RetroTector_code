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

import retrotectorcore.*;
import java.util.*;
import builtins.*;

/**
* Interface to CoreLeadingDynamicMatrix.
*/
public class LeadingDynamicMatrix {

	private CoreLeadingDynamicMatrix coreLeadingDynamicMatrix;
	
/**
* Constructor.
* @param	rect				Defines segment of DNA (x and width) and alignment (y and height) to work in.
* @param	block				Collection of useful parameters.
* @param	put					Putein to receive result.
* @param	insideLimit	A position relative to BOUNDS.x. Paths ending no earlier are prioritized.
*/
	public LeadingDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock block, Putein put, int insideLimit) throws RetroTectorException, FinishedException {
	
		try {
			coreLeadingDynamicMatrix = new CoreLeadingDynamicMatrix(rect, block, put, insideLimit);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreLeadingDynamicMatrix, "LeadingDynamicMatrix", rte);
		}
		
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock, Putein, int)
	
/**
* @return	Array of possible Paths, in topological order.
*/
	public final PathClass[ ] getPaths() {
		return coreLeadingDynamicMatrix.getPaths();
	} // end of getPaths()
	
/**
* @return	The highest scoring Path.
*/
	public final PathClass getBestPath() throws RetroTectorException {
		return coreLeadingDynamicMatrix.getBestPath();
	} // end of getBestPath()
	
/**
* @param	posInDNA		First base position (internal) in PathClass.
* @return	Such a PathClass, or null.
*/
	public final PathClass getPathStartingAt(int posInDNA) throws RetroTectorException {
		try {
			return coreLeadingDynamicMatrix.getPathStartingAt(posInDNA);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreLeadingDynamicMatrix, "TrailingDynamicMatrix", rte);
		}
		return null;
	} // end of getPathStartingAt(int)
	
}  // end of LeadingDynamicMatrix
