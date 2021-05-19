/*

Class for the front side of the vocal tract. In reality this includes all the shape changing organs.

*/

#ifndef front_h
#define front_h

#include <cmath>
#include <limits>
#include "tool.h"
#include "floatarray.h"
#include "vector2d.h"
#include "matrix2x2.h"
#include "genericarray.h"
#include "coordspace.h"

class Front: public Path
{
	protected:
		Path larynx, blade_over, blade_under, body, tongue, mfloor, jaw_relative; // these are just subpaths
		Path nn, blade_under_orig, larynx_orig, mfloor_orig, orig; // these are in a seperate paths, these should remain unchanged
		FloatArray pc1, pc2, pc3;

		// set tongue body shape in jaw coordinate system
		void setBodyShape(Vector2D c1, Vector2D c2, Vector2D c3)
		{
			for (unsigned int ii = 0; ii < body.getCount(); ++ii)
			{
				// point = nn.points[xx] + (c1*pc.at(xx, 0)) + (c2*pc.at(xx, 1)) + (c3*pc.at(xx, 2));
				body.setRaw(Vector2D::add(Vector2D::add(Vector2D::add(nn.get(ii), Vector2D::multiply(pc1.get(ii), c1)), Vector2D::multiply(pc2.get(ii), c2)), Vector2D::multiply(pc3.get(ii), c3)) , ii);
			}
		}

		// tongue tip must be in jaw coordinate system
		void setTongueTip(Vector2D ttip)
		{
			// contour over - Hermite interpolation with two points and one derivative
			// dd = (points[length - 2].y -points[length - 1].y) / (points[length - 2].x -points[length - 1].x);
			float dd = (body.get(body.getCount() - 2).yy - body.get(body.getCount() - 1).yy) / (body.get(body.getCount() - 2).xx - body.get(body.getCount() - 1).xx);
			float x1 = body.get(body.getCount() - 1).xx; // x1 = points[length - 1].x;
			float y1 = body.get(body.getCount() - 1).yy; // y1 = points[length - 1].y;
			float x0 = ttip.xx; // x0 = tip.x;
			float y0 = ttip.yy; // y0 = tip.y;
			float pos = 0;
			for (unsigned int ii = 0; ii < blade_over.getCount(); ++ii)
			{
				// pos = x0 + ((blade_over.size - index) * ((x1 - x0) / (blade_over.size)));
				pos = x0 + (blade_over.getCount() - ii) * ((x1 - x0) / blade_over.getCount());
				// points.add(Vector2D(pos, x0 + (((y1 - y0) / (x1 - x0)) * (pos - x0)) + (((dd - ((y1 - y0) / (x1 - x0))) / (x1 - x0)) * (pos - x0) * (pos - x1)) ));
				blade_over.setRaw(Vector2D(pos, y0 + (((y1 - y0) / (x1 - x0)) * (pos - x0)) + (((dd - ((y1 - y0) / (x1 - x0))) / (x1 - x0)) * (pos - x0) * (pos - x1))), ii);
			}
			
			// contour under
			Vector2D floor   = blade_under_orig.get(blade_under_orig.getCount() - 1);
			Vector2D ref_tip = blade_under_orig.get(0);
			float xratio = (ttip.xx - floor.xx) / (ref_tip.xx - floor.xx);
			float yratio = (ttip.yy - floor.yy) / (ref_tip.yy - floor.yy);
			for (unsigned int ii = 0; ii < blade_under.getCount(); ++ii)
			{
				//blade_under.setRaw(Vector2D(2*ii, 2*ii), ii);
				blade_under.setRaw(Vector2D(floor.xx + xratio*(blade_under_orig.get(ii).xx - floor.xx), floor.yy + yratio*(blade_under_orig.get(ii).yy - floor.yy)), ii);
			}
		}

		// set the jaw opening: if the whole tongue is calculated in the jaw coordinate system, then should it be called
		// TEMPORARY SOLUTION YET
		void setJawOpening(float jaw)
		{
			float jaw_angle = ((0.5 * jaw) + 7) * (pi/180); // calculate jaw angle in radian
			Vector2D translation = Vector2D::multiply(jaw, Vector2D(sin(jaw_angle), -cos(jaw_angle)));
			for (unsigned int ii = 0; ii < jaw_relative.getCount(); ++ii)
			{
				Vector2D point = Vector2D::add(jaw_relative.get(ii), translation);
				point = point.rotate(jaw_angle);
				// Vector2D point = tongue.get(ii);
				// point = Vector2D::add(point, Vector2D(-112.1701, -25.1788));
				// point = point.rotate(jaw_angle*(pi/180));
				// point = Vector2D::add(point, Vector2D(112.1701, 25.1788));
				jaw_relative.setRaw(point, ii);
			}
		}

		void setMouthFloor()
		{
			for (unsigned int ii = 0; ii < mfloor.getCount(); ++ii)
			{
				mfloor.setRaw(mfloor_orig.get(ii), ii);
			}
		}
		
		// set the larynx
		void setLarynxHeight(float height)
		{
			CoordSpace space = CoordSpace(Matrix2x2(1, 0, 0, 1), Vector2D(59, -49.7 - height)); // TODO: load default larynx position from file?
			for (unsigned int ii = 0; ii < larynx_orig.getCount(); ++ii)
			{
				larynx.setRaw(space.get(larynx_orig.get(ii)), ii);
			}
		}

	public:
	
		// parameterless constructor
		Front(): Path()
		{
		}

		// constructor
		// Front will be a path, concatenates all of these subpaths
		Front(Path frontpath, FloatArray positionarray, FloatArray pc1, FloatArray pc2, FloatArray pc3): Path(frontpath.getCount())
		{
			for (int ii = 0; ii < frontpath.getCount(); ++ii) this->setRaw(frontpath.get(ii), ii);
			this->larynx       = this->subPath(0, (int)positionarray.get(0));
			this->body         = this->subPath((int)positionarray.get(0), (int)(positionarray.get(1) - positionarray.get(0)));
			this->blade_over   = this->subPath((int)positionarray.get(1), (int)(positionarray.get(2) - positionarray.get(1)));
			this->blade_under  = this->subPath((int)positionarray.get(2), (int)(positionarray.get(3) - positionarray.get(2)));
			this->mfloor       = this->subPath((int)positionarray.get(3), (int)(positionarray.get(4) - positionarray.get(3)));
			this->tongue       = this->subPath((int)positionarray.get(0), (int)(positionarray.get(3) - positionarray.get(0)));
			this->jaw_relative = this->subPath((int)positionarray.get(0), (int)(positionarray.get(4) - positionarray.get(0)));
			
			this->orig             = frontpath;
			this->larynx_orig      = this->orig.subPath(0, (int)positionarray.get(0));
			this->mfloor_orig      = this->orig.subPath((int)positionarray.get(3), (int)(positionarray.get(4) - positionarray.get(3)));
			this->nn               = this->orig.subPath((int)positionarray.get(0), (int)(positionarray.get(1) - positionarray.get(0)));
			this->blade_under_orig = this->orig.subPath((int)positionarray.get(2), (int)(positionarray.get(3) - positionarray.get(2)));
			this->pc1 = pc1;
			this->pc2 = pc2;
			this->pc3 = pc3;
		}
		
		// calculates the front shape, sets everything
		void setShape(Vector2D c1, Vector2D c2, Vector2D c3, Vector2D ttip, float jaw, float larynx_height)
		{
			setBodyShape(c1, c2, c3);
			setMouthFloor();
			setTongueTip(ttip); // TODO: should the tongue tip position relative to the fixed upper jaw coordinate system?
			setJawOpening(jaw);
			setLarynxHeight(larynx_height);
		}
		
		// deallocate memory
		void Free()
		{
			pc1.Free();
			pc2.Free();
			pc3.Free();
			orig.Free();
			if (data != 0) delete [] data;
		}
};

#endif

