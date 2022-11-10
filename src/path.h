/*

Path class, for 2-dimensional paths. It is a subclass of a Vector2D array.

*/

#ifndef path_h
#define path_h

#include <cmath>
#include <limits>
#include "tool.h"
#include "segment.h"
#include "segmentarray.h"
#include "vector2d.h"
#include "matrix2x2.h"
#include "genericarray.h"
#include "coordspace.h"

class Path: public GenericArray<Vector2D>
{
	protected:
		float length; // "real" length of the path
		int start, count; // subpath selection variables
		
		bool length_dirty; // true if length contains incorrect length data
		
		// helper variables for position calculation when going through the path in one way
		int last_index;
		float last_pos;
		
		// recalculate the length of the (sub)path
		void recalcLength()
		{
			length = 0;
			Vector2D last = this->get(0);
			for (unsigned int ii = 1; ii < this->getCount(); ++ii)
			{
				Vector2D cur = this->get(ii);
				if (!(last.isNaV() || cur.isNaV()))
				{
					length = length + Vector2D::subtract(cur, last).length();
				}
				last = cur;
			}
			length_dirty = false;
		}
		
		// helper for constructor
		void setDefaults(unsigned int size)
		{
			last_index = 0;
			last_pos   = 0;
			start = 0;
			count = size;
			length_dirty = false;
		}
		
	public:

		// default parameterless constructor
		Path()
		{
			this->setDefaults(0);
		}

		// constructor, calling parent's constructor
		Path(unsigned int size): GenericArray<Vector2D>(size)
		{
			this->setDefaults(size);
			for (int ii = 0; ii < size; ++ii) data[ii] = Vector2D::NaV();
		}

		// constructor, calling parent's constructor
		Path(float* xx, float* yy, unsigned int size): GenericArray<Vector2D>(size)
		{
			this->setDefaults(size);
			for (int ii = 0; ii < size; ++ii) data[ii] = Vector2D(xx[ii], yy[ii]);
			this->recalcLength();
		}
		
		// copy constructor, copy the whole data, so if path was a subpath then the created one will be a subpath too
		Path(const Path& path): GenericArray<Vector2D>(path.size)
		{
			last_index = path.last_index;
			last_pos = path.last_pos;
			start = path.start;
			count = path.count;
			length = path.length;
			length_dirty = false;
			for (int ii = 0; ii < path.size; ++ii) data[ii] = path.data[ii];
		}

		// get an element by it's index
		Vector2D get(unsigned int index)
		{
			if ((index + start < size) && (index + start < start + count)) return data[index + start];
			return Vector2D::NullValue();
		}

		// set an element, and update the length of the path
		void set(Vector2D cur, unsigned int index)
		{
			if ((index + start < size) && (index + start < start + count)) {
				if (!length_dirty)
				{
					float old_length = 0; // path length between the non updated vector at index and it's neighbours
					float new_length = 0; // path length between the updated vector (cur) at index and it's neighbours
					Vector2D prev = this->get(index - 1); // neighbour 1
					Vector2D old  = this->get(index);     // old vector at index
					Vector2D next = this->get(index + 1); // neightbour 2
					if (!prev.isNaV()) new_length = new_length + Vector2D::distance(prev, cur);
					if (!next.isNaV()) new_length = new_length + Vector2D::distance(next, cur);
					if ((!prev.isNaV()) && (!old.isNaV())) old_length = old_length + Vector2D::distance(prev, old);
					if ((!next.isNaV()) && (!old.isNaV())) old_length = old_length + Vector2D::distance(next, old);
					length = length + new_length - old_length;
				}
				data[index+start] = cur;
				if (length_dirty) recalcLength();
			}
		}
		
		// set an element, without updating the length of the path (but the next time when the length will be needed it will be calculated)
		void setRaw(Vector2D cur, unsigned int index)
		{
			length_dirty = true;
			if ((index + start < size) && (index + start < start + count)) data[index + start] = cur;
		}
		
		// returns a part of the path wich have contains the same block of data but behaves like it would be just a sub path of the original
		Path subPath(unsigned int start_, unsigned int count_)
		{
			Path result;
			result.start = start_;
			result.count = count_;
			result.data = data;
			result.size = size;
			result.last_index = start;
			result.last_pos = 0;
			result.recalcLength();
			return result;
		}
		
		// getter for length
		float getLength()
		{
			if (length_dirty) recalcLength();
			return length;
		}

		// getter for count
		int getCount()
		{
			return count;
		}

		
		// linear transformation of the path
		void transform(CoordSpace space)
		{
			if (start + count <= size) for (unsigned int ii = start; ii < start + count; ++ii) data[ii]=space.get(data[ii]);
		}
		
		// returns the point on the path wich is at the arg pos position (pos must be between 0 and length)
		// uses previously stored data to speed up the process, so calling this multiple times with i.e. growing arg pos it will be faster
		Vector2D getPointAt(float pos)
		{
			if (length_dirty) recalcLength();
			Vector2D point;
			if ((pos < 0) || (pos > length)) return Vector2D::NaV();
			int direction = -1;
			if (pos >= last_pos) direction = 1;
			while ((last_index >= 0) && (last_index < (this->getCount() - 1)))
			{
				Segment segment = Segment(this->get(last_index), this->get(last_index + 1));
				point = segment.getPointAt(pos - last_pos);
				if (!point.isNaV()) return point;
				if (direction == 1)
				{
					last_pos = last_pos + segment.getLength();
				}
				if ((direction == -1) && (last_index > 0))
				{
					last_pos = last_pos - Segment(this->get(last_index - 1), this->get(last_index)).getLength();
				}
				last_index = last_index + direction;
			}
			return Vector2D::NaV();
		}
		
		// same as getPointAt but it's argument must be between 0 and 1
		Vector2D getPointAtNormal(float norm_pos)
		{
			if (length_dirty) recalcLength();
			return this->getPointAt(norm_pos * this->length);
		}

		// line vs. all segments of the path intersection
		// if midpoint is given, then it calculates all intersection points and choosees the closest one
		// it contains a line-segment intersection check before calculating the intersection point
		// if segmentsegment is true, then segment vs path intersection will be tested
		// if getpathpos is true, then instead of returning the intersection point, it returns the position of the point along the path (so getPointAt(lineIntersection(getpathpos: true)) = lineIntersection should be true)
		// returns NaV if there is no intersection, returns the point if getpathpos is not true, if getpathpos is true: return the position in the result's x coorinate
		Vector2D lineIntersection(Segment line, Vector2D midpoint, bool segmentsegment, bool getpathpos)
		{
			Vector2D point, cur_point;
			float min_distance, cur_distance, pathpos, lastpos;
			int v1, v2;
			Vector2D nline, nline2;
			unsigned int kk; // segment index
			
			point = Vector2D::NaV();
			min_distance = 0;
			pathpos = 0;
			lastpos = 0;
			kk = 1;
			nline = line.getVector().rotate90();
			v2 = signum(Vector2D::dot(Vector2D::subtract(this->get(0), line.getP1()), nline)); // signum((this->get(0) - line.getP1()) * nline)
			while (kk < this->getCount())
			{
				v1 = v2;
				v2 = signum(Vector2D::dot(Vector2D::subtract(this->get(kk), line.getP1()), nline)); // signum((this->get(0) - line.getP1()) * nline)
				if (segmentsegment) // if we check only segment vs segment: if the other way they don't intersect, make v1 and v2 equal not to enter the intersection branch
				{
					nline2 = Vector2D::subtract(this->get(kk), this->get(kk - 1)).rotate90();
					if (signum(Vector2D::dot(Vector2D::subtract(line.getP1(), this->get(kk - 1)), nline2)) == signum(Vector2D::dot(Vector2D::subtract(/*!!*/line.getP2()/*!!*/, this->get(kk - 1)), nline2))) // signum((line.getP1() - this->get(kk-1))*nline2) == signum(( !!-> line.getP2() <-!! - this->get(kk-1))*nline2)
					{
						v1 = v2; // set the two to be equal, not to enter the intersection part
					}
				}
				if (v1 != v2) // intersection!
				{
					cur_point = Segment::intersectLineLine(line, Segment(this->get(kk), this->get(kk - 1)));
					if (getpathpos)
					{
						pathpos = lastpos + (Vector2D::subtract(cur_point, this->get(kk - 1)).length());
					}
					if (midpoint.isNaV())
					{
						if (getpathpos) return Vector2D(pathpos, 0);
						return cur_point;
					}
					else
					{
						cur_distance = Vector2D::subtract(cur_point, midpoint).length();
						if (point.isNaV() || (cur_distance < min_distance))
						{
							min_distance = cur_distance;
							point = cur_point;
						}
						kk = kk + 1; // !!!!????
					}
				}
				else
				{
					lastpos = lastpos + Vector2D::subtract(this->get(kk), this->get(kk-1)).length();
					kk = kk + 1;
				}
			}
			if (getpathpos) return Vector2D(pathpos, 0);
			return point;
		}
		
		// line array vs. all segment of the path intersection
		// it calls the lineIntersection method
		// it gives back the closest intersections to the midpoint_array ith point for the ith line (if midpoint_array is given and is an array)
		// if segmentsegment is true, then segment vs path intersection will be tested
		// the result is an array of intersection points, or if getpathpos is true, then the result is an array of positions along the path
		// if there was no intersection between the path and the ith line then the result's ith position will be NaV	
		void lineArrayIntersection(SegmentArray& line_array, Path& midpoint_array, bool segmentsegment, bool getpathpos, Path& result)
		{
			for (unsigned int ii = 0; ii < line_array.getSize(); ++ii)
			{
				result.setRaw(this->lineIntersection(line_array.get(ii), midpoint_array.get(ii), segmentsegment, getpathpos), ii);
			}
		}

		// generate planes perpendicular to the curve
		void generatePlanes(SegmentArray& result, float length)
		{
			int line_count = result.getSize();
			Vector2D point, segment_vector;
			Vector2D prev_point = this->getPointAtNormal(0);
			for (unsigned int ii = 0; ii < line_count; ++ii)
			{
				point = this->getPointAtNormal((float)(ii + 1) / (float) line_count);
				segment_vector = Vector2D::subtract(point, prev_point).rotate90().normal();
				Vector2D tmp1 = Vector2D::divide(Vector2D::add(prev_point, point), 2.0f);
				Vector2D tmp2 = Vector2D::multiply(length/2.0f, segment_vector);
				// lines.add(Segment(((prev_point+point) / 2) - (segment_vector * (length/2)), ((prev_point+point) / 2) + (segment_vector * (length/2))));
				result.set(Segment(Vector2D::subtract(tmp1, tmp2), Vector2D::add(tmp1, tmp2)), ii);
				prev_point = point;
			}
		}

		// export into two float arrays
		void exportToArray(float* xx, float* yy, unsigned int size)
		{
			for (unsigned int ii = 0; ii < size; ++ii)
			{
				xx[ii] = this->get(ii).xx;
				yy[ii] = this->get(ii).yy;
			}
		}

		// to print it out to the terminal
		std::string toString()
		{
			std::string result = "Path:\n";
			result = result + "Count: " + to_string(this->getCount()) + "\n";
			for (unsigned int ii = 0; ii < this->getCount(); ++ii)
			{
				result = result + to_string(ii) + ": " + this->get(ii).toString() + "\n";
			}
			return result;
		}
		
};

#endif

