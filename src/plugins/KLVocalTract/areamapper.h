#ifndef areamapper_h
#define areamapper_h

#include <cmath>
#include <limits>
#include "tool.h"
#include "segment.h"
#include "segmentarray.h"
#include "vector2d.h"
#include "matrix2x2.h"
#include "genericarray.h"
#include "coordspace.h"

class AreaMapper
{
	private:
		SegmentArray planes;
		float plane11_pos; // after the 11th plane we use a different rule

	public:
		Path alphabeta;
		Path positionarray;
	
		// parameterless constructor
		AreaMapper()
		{
		}

		// constructor
		AreaMapper(Path alphabeta_, SegmentArray mri_planes_)
		{
			alphabeta = alphabeta_;
			planes = SegmentArray(mri_planes_); // copy it, then we can free it as well
			positionarray = Path(planes.getSize());
		}
		
		void initMidline(Path midline)
		{
			Path tmp = Path();
			midline.lineArrayIntersection(planes, tmp, false, true, positionarray);
			plane11_pos = positionarray.get(10).xx; // 11th plane's index is 10
			tmp.Free();
		}
		
		// return the alpha and beta values at a given position
		Vector2D getAlphaBeta(float pos)
		{
			unsigned int ii;
			Vector2D prev, next;
			prev = Vector2D::NaV();
			next = Vector2D::NaV();
			for (unsigned int ii = 0; ii < positionarray.getCount(); ++ii)
			{
				next = positionarray.get(ii);
				if ((!prev.isNaV()) && (!next.isNaV()) && (prev.xx < pos) && (next.xx > pos))
				{
					// ^((alphabeta_path.points[ii]*(pos-prev))+(alphabeta_path.points[ii-1]*(next-pos)))/(next-prev);
					return Vector2D::divide(Vector2D::add(Vector2D::multiply((pos - prev.xx), alphabeta.get(ii)), Vector2D::multiply((next.xx - pos), alphabeta.get(ii - 1))), (next.xx - prev.xx));
				}
				else
				{
					if ((!prev.isNaV()) && (pos < prev.xx))
					{
						return alphabeta.get(ii - 1);
					}
				}
				prev = next;
			}
			return alphabeta.get(positionarray.getCount() - 1);
		}
		
		// get the area; pos - position, dd - diameter
		float getArea(float pos, float dd)
		{
			Vector2D ab = this->getAlphaBeta(pos);
			if (ab.isNaV())
			{
				return 0;
			}
			if (pos < plane11_pos) return (ab.xx*pow(dd, ab.yy));
			if (pos >= plane11_pos) return (ab.xx*pow(dd, ab.yy)); // TODO: use a different rule here
		}

		// deallocate
		void Free()
		{
			alphabeta.Free();
			planes.Free();
			positionarray.Free();
		}
		
		
};

#endif

