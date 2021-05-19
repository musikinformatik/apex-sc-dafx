/*

Template array class.

*/

#ifndef genericarray_h
#define genericarray_h

#include <cmath>
#include <limits>

template<typename T> class GenericArray
{
	protected:
		unsigned int size;
		T* data;
		
	
	public:
		
		// parameterless constructor
		GenericArray()
		{
			data = 0;
		}
		
		// constructor, allocate memory space
		GenericArray(unsigned int size_)
		{
			size = size_;
			data = new T[size];
		}
		
		// destructor, doesn't deallocate memory here, becouse local temporary variables would be deallocated automatically causing double free problems
		~GenericArray()
		{
		}
		
		// deallocate the data array - this way we can use non-pointer classes without having problem with double deallocations
		void Free()
		{
			if (data!=0) delete [] data;
		}
		
		// get an element by it's index
		T get(unsigned int index)
		{
			if (index<size) return data[index];
		}
		
		// set an element at the given index
		void set(T tt, unsigned int index)
		{
			if (index<size) data[index] = tt;
		}
		
		// getter for container size
		unsigned int getSize()
		{
			return size;
		}
		
};

#endif

