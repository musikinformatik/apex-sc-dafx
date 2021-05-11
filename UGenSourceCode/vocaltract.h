/*

Vocal tract class, it can set the vocal organs's shapes, and it calculates the area function.

*/

#ifndef vocaltract_h
#define vocaltract_h

#include <cmath>
#include <limits>
#include "tool.h"
#include "segment.h"
#include "segmentarray.h"
#include "vector2d.h"
#include "matrix2x2.h"
#include "genericarray.h"
#include "coordspace.h"
#include "areamapper.h"

class VocalTract: public Path
{

	private:
		int max_res; // maximal number of intersecting sections along midline
		float resolution; // distance between two points along the midline for which the areas are calculated
											// we use equal distances between each area values,
											// but sometimes it can happen to miss some values,
											// because the given segment may not intersect the tongue shape or back wall (area would be infinite?)
		
//		int res; // current number of intersecting sections along midline
		
	public:
	
		Front front;
		Wall back;
		
		AreaMapper areamapper;
		SegmentArray planes;
		Path backpoints;
		Path frontpoints;
		Path midline;
	
		// constructor
		VocalTract(Front& front_, Wall& back_, SegmentArray& planes_, AreaMapper areamapper_, float resolution_, int bufsize): Path(bufsize)
		{
			front = front_;
			back = back_;
			planes = planes_;
			areamapper = areamapper_;
			backpoints = Path(planes.getSize());
			Path tmp = Path();
			back.lineArrayIntersection(planes, tmp, false, false, backpoints);
			resolution = resolution_;
			
			// allocate memory for the (a)rea, distance (x) /* (d)iameter and segment -> it was just to visualize, at the moment we don't need these */ arrays
			// I assume that midline path is always shorter than the max of front and back wall lengths
			// it's better to allocate memory only once, even though then we don't know the exact size, and we have to overestimate it, since memory allocation is slow
			max_res = max(front.getLength(), back.getLength()) / resolution + 1;
			midline = Path(planes.getSize());
			frontpoints = Path(planes.getSize());
		}
		
		// calculates the front shape, sets everything
		void setFrontShape(Vector2D c1, Vector2D c2, Vector2D c3, Vector2D ttip, float jaw, float larynx_height)
		{
			this->front.setShape(c1, c2, c3, ttip, jaw, larynx_height);
		}
		
		// calculate the areas of the vocal tract at various, equal distant places
		void calculateAreas()
		{
			int cur_res;
			int count = 0;
			this->count = this->getSize();
			Vector2D prev_point = Vector2D::NaV();
			Vector2D point, point_b, point_f;
			Segment segment;
			float pos;
			Path submidline;
			front.lineArrayIntersection(planes, backpoints, true, false, frontpoints);
			for (unsigned int ii = 0; ii < backpoints.getCount(); ++ii)
			{
				Vector2D point1, point2;
				point1 = backpoints.get(ii);
				point2 = frontpoints.get(ii);
				if ((!point1.isNaV()) && (!point2.isNaV()))
				{
					midline.set(Vector2D::divide(Vector2D::add(point1, point2), 2.0f), count); // midpoint
					++count;
				}
			}
			submidline = midline.subPath(0, count);
			cur_res = (int)round((submidline.getLength() / resolution) + 1);
			areamapper.initMidline(submidline);
			count = 0;
			for (unsigned int ii = 0; ii < cur_res; ++ii)
			{
				pos = ii * resolution;
				point = submidline.getPointAt(pos);
				if ((!point.isNaV()) && (!prev_point.isNaV()))
				{
					// point_t = vtfront.body_path.lineIntersection(Segment((prev_point + point) / 2, ((prev_point + point) / 2) + ((point - prev_point).rotate90)), prev_point);
					Vector2D tmp = Vector2D::divide(Vector2D::add(prev_point, point), 2.0f);
					segment = Segment(tmp, Vector2D::add(tmp, Vector2D::subtract(point, prev_point).rotate90()));
					point_f = front.lineIntersection(segment, prev_point, false, false);
					point_b = back.lineIntersection(segment, prev_point, false, false);
					if ((!point_f.isNaV()) && (!point_b.isNaV()))
					{
						Vector2D result = Vector2D(0, 0);
						result.xx = areamapper.getArea(pos, Vector2D::subtract(point_f, point_b).length()) / 1000.0f; // areas, in cm^2 (originally it's in mm^2)
						result.yy = pos / 10.0f; // diameter, in cm (originally it's mm)
						this->setRaw(result, count);
						++count;
					}
				}
				prev_point = point;
			}
			this->count = count;
		}
		
		// deallocate
		void Free()
		{
			front.Free();
			back.Free();
			planes.Free();
			areamapper.Free();
			if (data!=0) delete [] data;
		}
};

#endif

