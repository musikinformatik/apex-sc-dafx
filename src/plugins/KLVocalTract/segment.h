/*

Class for 2-dimensional segment or line, with intersection point calculation algorithms.

*/

#ifndef segment_h
#define segment_h

#include <cmath>
#include <limits>
#include "vector2d.h"
#include "matrix2x2.h"
#include "tool.h"

class Segment
{
	private:
		Vector2D p1, p2;
		float length;

		// calculate the length of the segment
		void calculateLength()
		{
			length = Vector2D::distance(p1, p2);
		}

	public:
	
		// parameterless constructor
		Segment()
		{
			p1 = Vector2D(0, 0);
			p2 = Vector2D(0, 0);
			calculateLength();
		}
		
		// new segment with the given endpoints
		Segment(Vector2D p1_, Vector2D p2_)
		{
			p1 = p1_;
			p2 = p2_;
			calculateLength();
		}
	
		// getter for p1
		Vector2D getP1()
		{
			return p1;
		}
		
		// getter for p2
		Vector2D getP2()
		{
			return p2;
		}
		
		// getter for length
		float getLength()
		{
			return length;
		}

		// average point of the endpoints
		Vector2D midPoint()
		{
			return Vector2D::divide(Vector2D::add(p1, p2), 2.0f);
		}

		// p2 - p1, vector between endpoints
		Vector2D getVector()
		{
			return Vector2D::subtract(p2, p1);
		}
		
		// return a point on the segment by a parameter mapped from 0 to 1
		Vector2D getPointAtNormal(float norm_pos)
		{
			if ((norm_pos > 1) || (norm_pos < 0))
			{
				return Vector2D::NaV();
			}
			return Vector2D::add(p1, Vector2D::multiply(norm_pos, Vector2D::subtract(p2, p1))); // p1 + norm_pos*(p2 - p1)
		}

		// return a point on the segment by its given position from 0 to the length of the segment
		Vector2D getPointAt(float pos)
		{
			if ((length == 0) && (pos == 0)) return p1;
			return getPointAtNormal(pos / length);
		}

		// calculates the intersection point between two lines (line1, line2)
		// in case the lines are parallel NaV is returned
		static Vector2D intersectLineLine(Segment line1, Segment line2)
		{
			Vector2D line1vector = line1.getVector(); // line1.p2 - line1.p1
			Vector2D line2vector = line2.getVector(); // line2.p2 - line2.p1
			Matrix2x2 aa = Matrix2x2(line1vector.xx, line2vector.xx, line1vector.yy, line2vector.yy);
			Vector2D bb = Vector2D::subtract(line2.p1, line1.p1);
			Vector2D cc;
			float detaa = aa.det();
			if (detaa != 0)
			{
				cc = Matrix2x2::product(aa.adj(), bb);
				return Vector2D::add(line1.p1, Vector2D::multiply(cc.xx / detaa, line1vector)); // line1.p1 + ((line1.p2 - line1.p1) * (cc.x / aa.det()))
			}
			return Vector2D::NaV();
		}
		
		// calculates the intersection point between a line and a segment
		// in case there is no intersection point*, NaV is returned
		// *(the line and the segment are parallel or the segment's two points are on the same side of the line)
		static Vector2D intersectLineSegment(Segment line, Segment segment)
		{
			Vector2D point = Segment::intersectLineLine(line, segment);
			if (!point.isNaV())
			{
				if (Vector2D::dot(Vector2D::subtract(point, segment.p1), Vector2D::subtract(point, segment.p2)) <= 0)
				{
					return point;
				}
			}
			return Vector2D::NaV();
		}
		
		// calculates the intersection point between two segments (segment1, segment2)
		// in case there is no intersection point*, NaV is returned
		// *(if the intersection point of the two lines on the two segments is not contained by both of the segments)
		static Vector2D intersectSegmentSegment(Segment segment1, Segment segment2)
		{
			Vector2D point = Segment::intersectLineSegment(segment1, segment2);
			if (!point.isNaV())
			{
				if (Vector2D::dot(Vector2D::subtract(point, segment1.p1), Vector2D::subtract(point, segment1.p2)) <= 0)
				{
					return point;
				}
			}
			return Vector2D::NaV();
		}
		
		// NaS segment [NaN, NaN], for error handling; "Not a Segment"
		static Segment NaS()
		{
			return Segment(Vector2D::NaV(), Vector2D::NaV());
		}
		
		// NullValue = NaS
		static Segment NullValue()
		{
			return Segment::NaS();
		}	
		
		// check wether it is "Not a Segment"
		bool isNaS()
		{
			if (p1.isNaV() || p2.isNaV()) return true;
			return false;
		}

		// to print it out to the terminal
		std::string toString()
		{
			if (this->isNaS()) return "[ Not a Segment ]";
			return "[ Segment between: " + p1.toString() + " and " + p2.toString() + " with length " + to_string(length) + " ]";
		}

};

#endif


