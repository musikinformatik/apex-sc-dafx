/*

Vocal tract class, it can set the vocal organs's shapes, and it calculates the area function.

*/

#ifndef vtparam_h
#define vtparam_h

#include <cmath>
#include <limits>
#include "SC_PlugIn.h"
#include "tool.h"
#include "vector2d.h"

class VTParam
{
	
	private:

		uint32 a_array;
		uint32 x_array;
		float* array_size;
		float* setup;
		float* param;
		uint32 front_xx;
		uint32 front_yy;
		int a_bufsize;
	
	public:

		VTParam(Unit* unit, uint32 a_array_, uint32 x_array_, uint32 array_size_, uint32 setup_, uint32 param_, uint32 front_xx_, uint32 front_yy_)
		{
			a_array = a_array_;
			x_array = x_array_;
			array_size = loadFloatData(unit, array_size_);
			setup = loadFloatData(unit, setup_);
			param = loadFloatData(unit, param_);
			front_xx = front_xx_;
			front_yy = front_yy_;
			a_bufsize = getBuffer(unit, a_array_).bufSize;
		}
		
		int getSetupPlaneNumber() // *plane number
		{
			return (int) setup[0];
		}

		float getSetupPlaneWidth() // *plane widths
		{
			return setup[1];
		}

		float getSetupResolution() // spatial resolution of the area function (distance between two value)
		{
			return setup[2];
		}

		Vector2D getParamC1() // tongue PCA C1 component
		{
			return Vector2D(param[0], param[1]);
		}

		Vector2D getParamC2() // tongue PCA C2 component
		{
			return Vector2D(param[2], param[3]);
		}
		
		Vector2D getParamC3() // tongue PCA C3 component
		{
			return Vector2D(param[4], param[5]);
		}
	
		Vector2D getParamTT() // tongue tip position relative to lower jaw
		{
			return Vector2D(param[6], param[7]);
		}
		
		float getParamJaw() // jaw opening in mm
		{
			return param[8];
		}

		float getParamLarynxHeight() // larynx height in mm
		{
			return param[9];
		}
		
		int getResultArraySize() // get the size of the result array
		{
			return array_size[0];
		}
		
		void setResultArraySize(int num) // set the size of the result array
		{
			array_size[0] = num;
		}

		uint32 getAreaBufID() // get area array float pointer
		{
			return a_array;
		}
		
		uint32 getPositionBufID() // get position array float pointer
		{
			return x_array;
		}

		uint32 getFrontXBufID() // get front path x array float pointer
		{
			return front_xx;
		}

		uint32 getFrontYBufID() // get front path y array float pointer
		{
			return front_yy;
		}

		int getAreaBufSize() // return the size of the area buffer (it is an upper bound for result array size)
		{
			return a_bufsize;
		}
		
};

#endif

