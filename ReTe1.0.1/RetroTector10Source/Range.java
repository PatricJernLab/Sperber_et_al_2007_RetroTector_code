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
* Defines an acceptable range for an integer quantity,
* typically the reference point of a chain component.
*/
public class Range {

/**
* General value for SPECIFICATION.
*/
	public static final int UNDEFINED = 0;

/**
* Value for SPECIFICATION when Range refers to beginning of a Chain.
*/
	public static final int CHAINSTART = 1;

/**
* Value for SPECIFICATION when Range refers to end of a Chain.
*/
	public static final int CHAINEND = 2;

/**
* Value for SPECIFICATION when Range refers to beginning of a Gene.
*/
	public static final int GENESTART = 3;

/**
* Value for SPECIFICATION when Range refers to end of a Gene.
*/
	public static final int GENEEND = 4;

/**
* Value for SPECIFICATION when Range refers to hotspot of a SubGeneHit.
*/
	public static final int SUBGENEHOTSPOT = 5;

/**
* Value for SPECIFICATION when Range refers to hotspot of a Litter hit.
*/
	public static final int LITTERHOTSPOT = 6;
	
/**
* Value for SPECIFICATION when Range refers to start of an LTR.
*/
	public static final int LTRSTART = 7;
	
/**
* Value for SPECIFICATION when Range refers to end of an LTR.
*/
	public static final int LTREND = 8;
	
/**
* @param	ranges	An array of Range.
* @return	Largest Range compatible with all Ranges in ranges, or null.
*/
	public final static Range consensus(Range[ ] ranges) throws RetroTectorException {
		if ((ranges == null) || (ranges.length == 0)) {
			return null;
		}
		if (ranges.length == 1) {
			return ranges[0];
		}
		if (ranges[0] == null) {
			return null;
		}
		int spec = ranges[0].SPECIFICATION;
		int min = ranges[0].RANGEMIN;
		int max = ranges[0].RANGEMAX;
		for (int i=1; i<ranges.length; i++) {
			if (ranges[i] == null) {
				return null;
			}
			if (ranges[i].SPECIFICATION != spec) {
				return null;
			}
			min = Math.max(min, ranges[i].RANGEMIN);
			max = Math.min(max, ranges[i].RANGEMAX);
			if (min > max) {
				return null;
			}
		}
		return new Range(spec, min, max);
	} // end of consensus(Range[ ])

/**
* @param	range1	A Range.
* @param	range2	A Range.
* @return	Largest Range compatible with range1 and range2, or null.
*/
	public final static Range consensus(Range range1, Range range2) throws RetroTectorException {
		if ((range1 == null) || (range2 == null) || (range1.SPECIFICATION != range2.SPECIFICATION)) {
			return null;
		}
		int min = Math.max(range1.RANGEMIN, range2.RANGEMIN);
		int max = Math.min(range1.RANGEMAX, range2.RANGEMAX);
		if (min > max) {
			return null;
		}
		return new Range(range1.SPECIFICATION, min, max);
	} // end of consensus(Range, Range)

/**
* @param	ranges	An array of Range.
* @param	range		A Range.
* @return	Largest Range compatible with all Ranges in ranges and range, or null.
*/
	public final static Range consensus(Range[ ] ranges, Range range) throws RetroTectorException {
		return consensus(consensus(ranges), range);
	} // end of consensus(Range[ ], Range)

/**
* @param	opiners	An array of RangeOpiner.
* @param	specification	An integer, useful as SPECIFICATION.
* @return	Largest Range compatible with all RangeOpiner in opiners, or null.
*/
	public final static Range consensus(RangeOpiner[ ] opiners, int specification) throws RetroTectorException {
		if ((opiners == null) || (opiners.length == 0) || (opiners[0] == null)) {
			return null;
		}
		Range range0 = opiners[0].rangeOpinion(specification);
		if (opiners.length == 1) {
			return range0;
		}
		if (range0 == null) {
			return null;
		}
		int min = range0.RANGEMIN;
		int max = range0.RANGEMAX;
		for (int i=1; i<opiners.length; i++) {
			if (opiners[i] == null) {
				return null;
			}
			range0 = opiners[i].rangeOpinion(specification);
			if (range0  == null) {
				return null;
			}
			min = Math.max(min, range0.RANGEMIN);
			max = Math.min(max, range0.RANGEMAX);
			if (min > max) {
				return null;
			}
		}
		return new Range(specification, min, max);
	} // end of consensus(RangeOpiner[ ])

/**
* @param	opiner1	A RangeOpiner.
* @param	opiner2	A RangeOpiner.
* @param	specification	An integer, useful as SPECIFICATION.
* @return	Largest Range compatible with opiner1 and opiner2, or null.
*/
	public final static Range consensus(RangeOpiner opiner1, RangeOpiner opiner2, int specification) throws RetroTectorException {
		if ((opiner1 == null) | (opiner2 == null)) {
			return null;
		}
		Range range1 = opiner1.rangeOpinion(specification);
		if (range1 == null) {
			return null;
		}
		Range range2 = opiner2.rangeOpinion(specification);
		if (range2 == null) {
			return null;
		}
		int min = Math.max(range1.RANGEMIN, range2.RANGEMIN);
		int max = Math.min(range1.RANGEMAX, range2.RANGEMAX);
		if (min > max) {
			return null;
		}
		return new Range(specification, min, max);
	} // end of consensus(RangeOpiner, RangeOpiner)

/**
* @param	opiners	An array of RangeOpiner.
* @param	opiner	A RangeOpiner.
* @param	specification	An integer, useful as SPECIFICATION.
* @return	Largest Range compatible with opiners and opiner, or null.
*/
	public final static Range consensus(RangeOpiner[ ] opiners, RangeOpiner opiner, int specification) throws RetroTectorException {
		if (opiner == null) {
			return null;
		}
		return consensus(consensus(opiners, specification), opiner.rangeOpinion(specification));
	} // end of consensus(Range[ ], Range)

/**
*	@param	r			A Range.
*	@param	limit	An integer.
*	@return	A Range like r, but with RANGEMIN >= limit, or null if limit>RANGEMAX.
*/
	public final static Range restrictRangeMin(Range r, int limit) throws RetroTectorException {
		if (limit > r.RANGEMAX) {
			return null;
		} else if (limit > r.RANGEMIN) {
			return new Range(r.SPECIFICATION, limit, r.RANGEMAX);
		} else {
			return r;
		}
	} // end of restrictRangeMin(Range, int)

/**
*	@param	r			A Range.
*	@param	limit	An integer.
*	@return	A Ramge like r, but with RANGEMAX <= limit, or null if limit < RANGEMIN.
*/
	public final static Range restrictRangeMax(Range r, int limit) throws RetroTectorException {
		if (limit < r.RANGEMIN) {
			return null;
		} else if (limit < r.RANGEMAX) {
			return new Range(r.SPECIFICATION, r.RANGEMIN, limit);
		} else {
			return r;
		}
	} // end of restrictRangeMax(Range, int)


/**
* Indicates which quantity, preferably using one of the obove.
*/
	public final int SPECIFICATION;

/**
* Lowest acceptable value.
*/
	public final int RANGEMIN;

/**
* Highest acceptable value.
*/
	public final int RANGEMAX;

/**
* Constructor
* @param	specification	see SPECIFICATION
* @param	rangemin	see RANGEMIN
* @param	rangemax	see RANGEMAX
*/
	public Range(int specification, int rangemin, int rangemax) throws RetroTectorException {
		if (rangemin > rangemax) {
			RetroTectorException.sendError(this, "" + rangemin + " larger than " + rangemax);
		}
		SPECIFICATION = specification;
		RANGEMIN = rangemin;
		RANGEMAX = rangemax;
	} // end of constructor(int, int, int)

/**
* Constructor for a Range at a specified DistanceRange from a specified position.
* @param	specification	see SPECIFICATION.
* @param	position	A position.
* @param	dr	A DistanceRange.
*/
	public Range(int specification, int pos, DistanceRange dr) throws RetroTectorException {
		SPECIFICATION = specification;
		RANGEMIN = pos + dr.LOWESTDISTANCE;
		RANGEMAX = pos + dr.HIGHESTDISTANCE;
	} // end of constructor(int, int, DistanceRange)

/**
* Constructor for a Range covering the length of a DNA.
* @param	specification	see SPECIFICATION.
* @param	dna	The DNA in question.
*/
	public Range(int specification, DNA dna) throws RetroTectorException {
		SPECIFICATION = specification;
		RANGEMIN = 0;
		RANGEMAX = dna.LENGTH - 1;
	} // end of constructor(int, DNA);
	

/**
* @param	r	a Range.
* @return	True if r is identical to this.
*/
	public final boolean doesEqual(Range r) {
		return (SPECIFICATION == r.SPECIFICATION) & (RANGEMIN == r.RANGEMIN) & (RANGEMAX == r.RANGEMAX);
	} // end of doesEqual(Range)
	
/**
* @return	An integer midway between RANGEMIN and RANGEMAX.
*/
	public final int middle() {
		return (RANGEMIN + RANGEMAX) / 2;
	} // end of middle()
	
/**
* @param	pos	A position.
* @return	True if pos contained in this.
*/
	public final boolean contains(int pos) {
		if ((pos < RANGEMIN) | (pos > RANGEMAX)) {
			return false;
		}
		return true;
	} // end of contains(int)
	
} // end of Range
