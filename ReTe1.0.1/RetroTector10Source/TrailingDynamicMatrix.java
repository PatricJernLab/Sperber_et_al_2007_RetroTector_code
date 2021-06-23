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

import retrotectorcore.*;
import builtins.*;

/**
* Interface to CoreTrailingDynamicMatrix.
*/
public class TrailingDynamicMatrix {

	private CoreTrailingDynamicMatrix coreTrailingDynamicMatrix;
	
/**
* Constructor.
* @param	rect				Defines segment of DNA (x and width) and alignment (y and height) to work in.
* @param	block				Collection of useful parameters.
* @param	put					Putein to receive result.
* @param	insideLimit	A position relative to BOUNDS.x. Paths ending no earlier are prioritized.
*/
	public TrailingDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock block, Putein put, int insideLimit) throws RetroTectorException, FinishedException {
	
		try {
			coreTrailingDynamicMatrix = new CoreTrailingDynamicMatrix(rect, block, put, insideLimit);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreTrailingDynamicMatrix, "TrailingDynamicMatrix", rte);
		}
		
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock, Putein, int)
	
/**
* @return	Array of possible Paths, in topological order.
*/
	public final PathClass[ ] getPaths() {
		return coreTrailingDynamicMatrix.getPaths();
	} // end of getPaths()
	
/**
* @return	The highest scoring Path.
*/
	public final PathClass getBestPath() throws RetroTectorException {
		return coreTrailingDynamicMatrix.getBestPath();
	} // end of getBestPath()
	
/**
* @param	posInDNA		Last base position (internal) in PathClass.
* @return	Such a Path, or null.
*/
	public final PathClass getPathEndingAt(int posInDNA) throws RetroTectorException {
		try {
			return coreTrailingDynamicMatrix.getPathEndingAt(posInDNA);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreTrailingDynamicMatrix, "TrailingDynamicMatrix", rte);
		}
		return null;
	} // end of getPathEndingAt(int)
	
	
} // end of TrailingDynamicMatrix
