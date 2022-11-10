/*

Float array class. Subclass of GenericArray<float>

*/

#ifndef floatarray_h
#define floatarray_h

#include "genericarray.h"
#include <cmath>
#include <limits>

class FloatArray: public GenericArray<float>
{
	public:
	
		// default parameterless constructor
		FloatArray()
		{
			data = 0;
		}
		
		// constructor, creates a new array with the given size
		FloatArray(unsigned int size): GenericArray<float>(size)
		{
		}
		
		// copy constructor, copies an existing float array
		FloatArray(const FloatArray& arr): GenericArray<float>(arr.size)
		{
			for (unsigned int ii = 0; ii < arr.size; ++ii) this->data[ii] = arr.data[ii];
		}
		
		// constructor, copies size number of floats from a float*
		FloatArray(float* data, unsigned int size): GenericArray<float>(size)
		{
			for (unsigned int ii = 0; ii < size; ++ii)
			{
				this->data[ii] = data[ii];
			}
		}
		
		// direct access, because sometimes it's needed
		float* getData()
		{
			return data;
		}
		
		// deallocate
		void Free()
		{
			if (data != 0) delete [] data;
		}
};

#endif

