/*
 SuperCollider real time audio synthesis system
 Copyright (c) 2002 James McCartney. All rights reserved.
 http://www.audiosynth.com
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

// VocalTractArea by Mátyás Jani, updated by Damian Murphy.

#include "SC_PlugIn.h"
#include <cmath>
#include "tool.h"
#include "vtparam.h"
#include "vector2d.h"
#include "matrix2x2.h"
#include "segment.h"
#include "path.h"
#include "front.h"
#include "wall.h"
#include "floatarray.h"
#include "vocaltract.h"
#include "areamapper.h"

// InterfaceTable contains pointers to functions in the host (server).
static InterfaceTable *ft;

//
struct VocalTractArea : public Unit
{
	uint32 front_xx, front_yy;

	Front front;
	Wall back;
	VocalTract vocaltract;
	
	VTParam param;
	
	int pos, skip;
};

// declare unit generator functions
extern "C"
{
	void load(InterfaceTable *inTable);
	void VocalTractArea_next_k(VocalTractArea *unit, int inNumSamples);
	void VocalTractArea_Ctor(VocalTractArea* unit);
	void VocalTractArea_Dtor(VocalTractArea* unit);
};

// load a float array from a buffer
FloatArray loadFloatArray(VocalTractArea* unit, uint32 bufnum)
{
	FloatArray result;
	SndBuf *buf = NULL;
	World *world = unit->mWorld;
	if (bufnum < world->mNumSndBufs) buf = world->mSndBufs + bufnum;
	if (buf) {
		unsigned int size = buf->samples;
		result = FloatArray(buf->data, size);
	}
	return result;
}

// load segment from two buffers
SegmentArray loadSegmentArray(VocalTractArea* unit, uint32 p1_xx, uint32 p1_yy, uint32 p2_xx, uint32 p2_yy)
{
	SegmentArray result;
	SndBuf *p1x = NULL;
	SndBuf *p1y = NULL;
	SndBuf *p2x = NULL;
	SndBuf *p2y = NULL;
	World *world = unit->mWorld;
	if (p1_xx < world->mNumSndBufs) p1x = world->mSndBufs + p1_xx;
	if (p1_yy < world->mNumSndBufs) p1y = world->mSndBufs + p1_yy;
	if (p2_xx < world->mNumSndBufs) p2x = world->mSndBufs + p2_xx;
	if (p2_yy < world->mNumSndBufs) p2y = world->mSndBufs + p2_yy;
	if (p1x && p1y && p2x && p2y) {
		unsigned int size = min(min(min(p1x->samples, p1y->samples), p2x->samples), p2y->samples);
		result = SegmentArray(size);
		for (unsigned int ii = 0; ii < size; ++ii)
		{
			result.set(Segment(Vector2D(p1x->data[ii], p1y->data[ii]), Vector2D(p2x->data[ii], p2y->data[ii])), ii);
		}
	}
	return result;
}


// load path from two buffers
Path loadPath(VocalTractArea* unit, uint32 bufnum_xx, uint32 bufnum_yy)
{
	Path result;
	SndBuf *bufx = NULL;
	SndBuf *bufy = NULL;
	World *world = unit->mWorld;
	if (bufnum_xx < world->mNumSndBufs) bufx = world->mSndBufs + bufnum_xx;
	if (bufnum_yy < world->mNumSndBufs) bufy = world->mSndBufs + bufnum_yy;
	if (bufx && bufy) {
		unsigned int size = min(bufx->samples, bufy->samples);
		result = Path(bufx->data, bufy->data, size);
	}
	return result;
}

// save path to two buffers
void savePath(VocalTractArea* unit, Path path, uint32 bufnum_xx, uint32 bufnum_yy)
{
	SndBuf *bufx = NULL;
	SndBuf *bufy = NULL;
	World *world = unit->mWorld;
	if (bufnum_xx < world->mNumSndBufs) bufx = world->mSndBufs + bufnum_xx;
	if (bufnum_yy < world->mNumSndBufs) bufy = world->mSndBufs + bufnum_yy;
	if (bufx && bufy) {
		unsigned int size = min(min(bufx->samples, bufy->samples), (int)path.getCount());
		path.exportToArray(bufx->data, bufy->data, size);
	}
}

//////////////////////////////////////////////////////////////////
// Ctor is called to initialize the unit generator.
// It only executes once.
// A Ctor usually does 3 things.
// 1. set the calculation function.
// 2. initialize the unit generator state variables.
// 3. calculate one sample of output.
void VocalTractArea_Ctor(VocalTractArea* unit)
{
	// only control rate is allowed
	SETCALC(VocalTractArea_next_k);

	unit->skip           = (uint32)IN0( 0); unit->pos = unit->skip;
	uint32 pc1           = (uint32)IN0( 1);
	uint32 pc2           = (uint32)IN0( 2);
	uint32 pc3           = (uint32)IN0( 3);
	uint32 front_xx      = (uint32)IN0( 4);
	uint32 front_yy      = (uint32)IN0( 5);
	uint32 positionarray = (uint32)IN0( 6);
	uint32 back_xx       = (uint32)IN0( 7);
	uint32 back_yy       = (uint32)IN0( 8);
	uint32 alpha         = (uint32)IN0( 9);
	uint32 beta          = (uint32)IN0(10);
	uint32 mri1_xx       = (uint32)IN0(11);
	uint32 mri1_yy       = (uint32)IN0(12);
	uint32 mri2_xx       = (uint32)IN0(13);
	uint32 mri2_yy       = (uint32)IN0(14);
	uint32 planes_xx     = (uint32)IN0(15);
	uint32 planes_yy     = (uint32)IN0(16);
	uint32 a_array       = (uint32)IN0(17);
	uint32 x_array       = (uint32)IN0(18);
	uint32 array_size    = (uint32)IN0(19);
	uint32 setup         = (uint32)IN0(20);
	uint32 param         = (uint32)IN0(21);

	unit->param = VTParam(unit, a_array, x_array, array_size, setup, param, front_xx, front_yy);

	Path frontpath  = loadPath(unit, unit->param.getFrontXBufID(), unit->param.getFrontYBufID());
	Path backpath   = loadPath(unit, back_xx, back_yy);
	Path alphabeta  = loadPath(unit, alpha, beta);
	Path planespath = loadPath(unit, planes_xx, planes_yy);
	SegmentArray mri_planes = loadSegmentArray(unit, mri1_xx, mri1_yy, mri2_xx, mri2_yy);
	SegmentArray planes = SegmentArray(unit->param.getSetupPlaneNumber()); // get these from setup?
	planespath.generatePlanes(planes, unit->param.getSetupPlaneWidth());

	
	FloatArray pc1_arr = loadFloatArray(unit, pc1);
	FloatArray pc2_arr = loadFloatArray(unit, pc2);
	FloatArray pc3_arr = loadFloatArray(unit, pc3);
	FloatArray positionarray_arr = loadFloatArray(unit, positionarray);
	
	// for tongue over we need only it's size
	Front front = Front(frontpath, positionarray_arr, pc1_arr, pc2_arr, pc3_arr);
	Wall back  = Wall(backpath);
	AreaMapper areamapper = AreaMapper(alphabeta, mri_planes);
	unit->vocaltract = VocalTract(front, back, planes, areamapper, unit->param.getSetupResolution(), unit->param.getAreaBufSize());
//	unit->vocaltract.front = Front(tunder, Path(8), tbody, larynx, pc1_arr, pc2_arr, pc3_arr);

	planespath.Free();
	mri_planes.Free();
}

//////////////////////////////////////////////////////////////////
// Dtor is called to destroy the unit generator.
void VocalTractArea_Dtor(VocalTractArea* unit)
{
//	unit->front.Free();
//	unit->back.Free();
	unit->vocaltract.Free();
}

//////////////////////////////////////////////////////////////////
// calculation function for a control rate frequency argument
void VocalTractArea_next_k(VocalTractArea *unit, int inNumSamples)
{

	// get the pointer to the output buffer
	float *out = OUT(0);

	//unit->front.setShape(Vector2D(0, 0), Vector2D(0, 0), Vector2D(0, 0), Vector2D(0, 0), 0.0, 0.0);
	
	--unit->pos;
	if (unit->pos<=0)
	{
		// parameters: c1, c2, c3, ttip, jaw, larynx_height
		unit->vocaltract.setFrontShape(unit->param.getParamC1(), unit->param.getParamC2(), unit->param.getParamC3(), unit->param.getParamTT(), unit->param.getParamJaw(), unit->param.getParamLarynxHeight());

		unit->vocaltract.calculateAreas();
		savePath(unit, unit->vocaltract.front, unit->param.getFrontXBufID(), unit->param.getFrontYBufID());

		savePath(unit, unit->vocaltract, unit->param.getAreaBufID(), unit->param.getPositionBufID());

		unit->param.setResultArraySize(unit->vocaltract.getCount());
		unit->pos = unit->skip;
	}
}
////////////////////////////////////////////////////////////////////
// the load function is called by the host when the plug-in is loaded
// gerhard
//void initVocalTractArea(InterfaceTable *inTable)
//{
//	DefineSimpleUnit(VocalTractArea);
//}
////////////////////////////////////////////////////////////////////
// original
//void load(InterfaceTable *inTable)
PluginLoad(VocalTractArea)
{
	ft = inTable;
	DefineSimpleUnit(VocalTractArea);
}
////////////////////////////////////////////////////////////////////

