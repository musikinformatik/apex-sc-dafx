/*

2 by 2 matrix class.
Elements are stored as row vectors (Vector2D).

*/
#ifndef matrix2x2_h
#define matrix2x2_h

#include <cmath>
#include <limits>
#include "vector2d.h"
#include "tool.h"

class Matrix2x2
{
	public:
		Vector2D row1, row2;
		
		// parameterless constructor
		Matrix2x2()
		{
			row1 = Vector2D(0, 0);
			row2 = Vector2D(0, 0);
		}
		
		// new matrix by the given rows
		Matrix2x2(Vector2D row1_, Vector2D row2_)
		{
			row1 = row1_;
			row2 = row2_;
		}
		
		// new matrix by the given elements
		Matrix2x2(float a11, float a12, float a21, float a22)
		{
			row1 = Vector2D(a11, a12);
			row2 = Vector2D(a21, a22);
		}

		// [0, 0; 0, 0]		
		static Matrix2x2 Zero()
		{
			return Matrix2x2();
		}

		// [1, 0; 0, 1]
		static Matrix2x2 Identity()
		{
			return Matrix2x2(1, 0, 0, 1);
		}
		
		// NaM matrix [NaN, NaN; NaN, NaN], for error handling; "Not a Matrix"
		static Matrix2x2 NaM()
		{
			return Matrix2x2(std::numeric_limits<float>::quiet_NaN(), std::numeric_limits<float>::quiet_NaN(),
												std::numeric_limits<float>::quiet_NaN(), std::numeric_limits<float>::quiet_NaN());
		}

		// NullValue = NaM
		static Matrix2x2 NullValue()
		{
			return Matrix2x2::NaM();
		}
		
		// matrix * matrix
		static Matrix2x2 product(Matrix2x2 aa, Matrix2x2 bb)
		{
			return Matrix2x2(
											(aa.row1.xx*bb.row1.xx) + (aa.row1.yy*bb.row2.xx),
											(aa.row1.xx*bb.row1.yy) + (aa.row1.yy*bb.row2.yy),
											(aa.row2.xx*bb.row1.xx) + (aa.row2.yy*bb.row2.xx),
											(aa.row2.xx*bb.row1.yy) + (aa.row2.yy*bb.row2.yy)
											);
		}
		
		// matrix * vector, linear map
		static Vector2D product(Matrix2x2 aa, Vector2D bb)
		{
			return Vector2D(Vector2D::dot(aa.row1, bb), Vector2D::dot(aa.row2, bb));
		}
		
		// number * matrix, multiply by a number
		static Matrix2x2 multiply(float lambda, Matrix2x2 aa)
		{
			return Matrix2x2(aa.row1.xx*lambda, aa.row1.yy*lambda, aa.row2.xx*lambda, aa.row2.yy*lambda);
		}
		
		// (1/lambda) * matrix
		static Matrix2x2 divide(Matrix2x2 aa, float lambda)
		{
			return Matrix2x2(aa.row1.xx/lambda, aa.row1.yy/lambda, aa.row2.xx/lambda, aa.row2.yy/lambda);
		}
		
		// matrix + matrix
		static Matrix2x2 add(Matrix2x2 aa, Matrix2x2 bb)
		{
			return Matrix2x2(aa.row1.xx + bb.row1.xx, aa.row1.yy + bb.row1.yy, aa.row2.xx + bb.row2.xx, aa.row2.yy + bb. row2.yy);
		}

		// matrix - matrix
		static Matrix2x2 subtract(Matrix2x2 aa, Matrix2x2 bb)
		{
			return Matrix2x2(aa.row1.xx - bb.row1.xx, aa.row1.yy - bb.row1.yy, aa.row2.xx - bb.row2.xx, aa.row2.yy - bb. row2.yy);
		}
		
		// (-1) * matrix
		Matrix2x2 neg()
		{
			return Matrix2x2(row1.neg(), row2.neg());
		}
		
		// det(matrix), determinant
		float det()
		{
			return (row1.xx*row2.yy) - (row1.yy*row2.xx);
		}
		
		// transpose(matrix)
		Matrix2x2 transpose()
		{
			return Matrix2x2(row1.xx, row2.xx, row1.yy, row2.yy);
		}
		
		// adj(matrix), (1/det(matrix))*adj(matrix) = inv(matrix)
		Matrix2x2 adj()
		{
			return Matrix2x2(row2.yy, -row1.yy, -row2.xx, row1.xx);
		}
		
		// inv(matrix), inv(matrix)*matrix=
		Matrix2x2 inv()
		{
			float det = this->det();
			if (det!=0)
			{
				return Matrix2x2(row2.yy / det, -row1.yy / det, -row2.xx / det, row1.xx / det);
			}
			else
			{
				// return a [NaN, NaN; NaN, NaN]
				return Matrix2x2::NaM();
			}
		}
		
		// check wether it is "Not a Matrix"
		bool isNaM()
		{
			if (row1.isNaV() || row2.isNaV()) return true;
			return false;
		}
		
		// to print it out to the terminal
		std::string toString()
		{
			if (this->isNaM()) return "[ Not a Matrix ]";
			std::string a11, a12, a21, a22;
			a11 = to_string(row1.xx);
			a12 = to_string(row1.yy);
			a21 = to_string(row2.xx);
			a22 = to_string(row2.yy);
			while (a11.length() < a21.length()) a11 = " " + a11;
			while (a21.length() < a11.length()) a21 = " " + a21;
			while (a12.length() < a22.length()) a12 = " " + a12;
			while (a22.length() < a12.length()) a22 = " " + a22;			
			return "[ " + a11 + " " + a12 + " ]\n[ " + a21 + " " + a22 + " ]";
		}
		
};

#endif
