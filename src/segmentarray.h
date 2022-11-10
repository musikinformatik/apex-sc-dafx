/*

Segment array class, subclass of GenericArray<Segment>

*/

#ifndef segmentarray_h
#define segmentarray_h

#include "segment.h"
#include "genericarray.h"
#include "path.h"
#include <cmath>
#include <limits>

class SegmentArray: public GenericArray<Segment>
{
	public:

		// parameterless constructor
		SegmentArray(): GenericArray<Segment>(0)
		{
		}

		// constructor
		SegmentArray(unsigned int size): GenericArray<Segment>(size)
		{
		}
		
		// copy constructor, copy the whole data, so if path was a subpath then the created one will be a subpath too
		SegmentArray(const SegmentArray& arr): GenericArray<Segment>(arr.size)
		{
			for (int ii = 0; ii < arr.size; ++ii) data[ii] = arr.data[ii];
		}
		
		// deallocation
		void Free()
		{
			if (data != 0) delete [] data;
		}
		
		// get an element by it's index
		Segment get(unsigned int index)
		{
			if (index<size) return data[index];
			return Segment::NullValue();
		}
};

#endif

