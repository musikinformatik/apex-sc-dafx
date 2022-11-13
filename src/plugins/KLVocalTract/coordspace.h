/*

A class for coordinate transformation and translation.

*/

#ifndef coordspace_h
#define coordspace_h

#include <cmath>
#include <limits>
#include "vector2d.h"
#include "matrix2x2.h"

class CoordSpace
{
	private:
		Matrix2x2 aa;
		Vector2D bb;
	
	public:
		
		// constructor
		CoordSpace(Matrix2x2 aa_, Vector2D bb_)
		{
			aa = aa_;
			bb = bb_;
		}
		
		// linear transformation
		Vector2D get(Vector2D xx)
		{
			return Vector2D::add(Matrix2x2::product(aa, xx), bb);
		}

		// inverse transofrmation
		Vector2D inv(Vector2D yy)
		{
			return Matrix2x2::product(aa.inv(), Vector2D::subtract(yy, bb));
		}
		
};

#endif

