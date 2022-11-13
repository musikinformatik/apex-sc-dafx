/*

Class for solid walls. Subclass of Path (there are no extra methods in it).

*/

#ifndef wall_h
#define wall_h

#include <cmath>
#include <limits>
#include "tool.h"
#include "floatarray.h"
#include "vector2d.h"
#include "matrix2x2.h"
#include "genericarray.h"
#include "coordspace.h"

class Wall: public Path
{
	public:
		// parameterless constructor
		Wall(): Path()
		{
		}
	
		// constructor, clone a path
		Wall(Path path): Path(path)
		{
			path.Free();
		}
	
		// deallocation
		void Free()
		{
			if (data != 0) delete [] data;
		}
};

#endif

