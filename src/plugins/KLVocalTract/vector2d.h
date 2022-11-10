/*

2-dimensional vector class.

*/

#ifndef vector2d_h
#define vector2d_h

#include <cmath>
#include <limits>
#include "tool.h"

class Vector2D
{
	private:
	
		// check whether the the float is NaN
		static bool custom_isnan(float var)
		{
			volatile float d = var;
			return d != d;
		}

	public:
		float xx, yy;
		
		// parameterless constructor
		Vector2D()
		{
			xx = 0;
			yy = 0;
		}
	
		// new vector with the given coordinates
		Vector2D(float xx_, float yy_)
		{
			xx = xx_;
			yy = yy_;
		}
		
		// NaV vector [NaN; NaN], for error handling; "Not a Vector"
		static Vector2D NaV()
		{
			return Vector2D(std::numeric_limits<float>::quiet_NaN(), std::numeric_limits<float>::quiet_NaN());
		}
		
		// NullValue = NaV
		static Vector2D NullValue()
		{
			return Vector2D::NaV();
		}
		
		// vector + vector
		static Vector2D add(Vector2D aa, Vector2D bb)
		{
			return Vector2D(aa.xx + bb.xx, aa.yy + bb.yy);
		}
	
		// vector - vector
		static Vector2D subtract(Vector2D aa, Vector2D bb)
		{
			return Vector2D(aa.xx - bb.xx, aa.yy - bb.yy);
		}
	
		// <vector; vector>
		static float dot(Vector2D aa, Vector2D bb)
		{
			return aa.xx*bb.xx + aa.yy*bb.yy;
		}
	
		// real*vector
		static Vector2D multiply(float lambda, Vector2D aa)
		{
			return Vector2D(lambda*aa.xx, lambda*aa.yy);
		}
	
		// (1/real) * vector
		static Vector2D divide(Vector2D aa, float lambda)
		{
			return Vector2D(aa.xx/lambda, aa.yy/lambda);
		}
	
		// (eucledian) distance between point aa and point bb
		static float distance(Vector2D aa, Vector2D bb)
		{
			return subtract(aa, bb).length();
		}
	
		// (eucledian) length of the vector
		float length()
		{
			return sqrt(xx*xx + yy*yy);
		}
	
		// (eucledian) length of the vector
		float abs()
		{
			return sqrt(xx*xx + yy*yy);
		}
	
		// (-1) * vector
		Vector2D neg()
		{
			return Vector2D(-xx, -yy);
		}
	
		// rotate vector by 90 degrees anticlockwise
		Vector2D rotate90()
		{
			return Vector2D(-yy, xx);
		}
	
		// rotate vector by angle radians
		Vector2D rotate(float angle)
		{
			float sina = sin(angle);
			float cosa = cos(angle);
			return Vector2D((cosa * xx) - (sina * yy), (sina * xx) + (cosa * yy));
		}
	
		// normal = vector / vector.length
		Vector2D normal()
		{
			return divide((*this), this->length());
		}
		
		// check wether it is "Not a Vector"
		bool isNaV()
		{
			if (Vector2D::custom_isnan(xx) || Vector2D::custom_isnan(yy)) return true;
			return false;
		}
		
		// to print it out to the terminal
		std::string toString()
		{
			if (this->isNaV()) return "[ Not a Vector ]";
			return "[ " + to_string(xx) + "; " + to_string(yy) + " ]";
		}

};

#endif

