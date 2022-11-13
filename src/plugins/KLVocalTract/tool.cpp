/*
 *  tool.cpp
 *  Plugins
 *
 *  Created by Gerhard Eckel on 6/16/11.
 *  Copyright 2011 KUG IEM. All rights reserved.
 *
 */

#include "SC_PlugIn.h"
#include <cmath>
#include "tool.h"

// return a pointer to a SuperCollider buffer's data array. The input is the unit (ugen), and the number/id of the buffer.
float* loadFloatData(Unit* unit, uint32 bufnum)
{
	SndBuf *buf = NULL;
	World *world = unit->mWorld;
	if (bufnum < world->mNumSndBufs) buf = world->mSndBufs + bufnum;
	if (buf) {
		//unsigned int size = buf->samples;
		return buf->data;
	}
	return NULL;
}

// get buffer
BufferArray getBuffer(Unit* unit, uint32 bufnum)
{
	BufferArray result;
	SndBuf *buf = NULL;
	World *world = unit->mWorld;
	if (bufnum < world->mNumSndBufs) buf = world->mSndBufs + bufnum;
	if (buf)
	{
		result.bufNum = bufnum;
		result.bufData = buf->data;
		result.bufSize = buf->samples;
	}
	return result;
}

// set buffer
void setBuf(Unit* unit, uint32 bufnum, BufferArray& bufarr) {
	SndBuf *buf = NULL;
	World *world = unit->mWorld;
	if (bufnum < world->mNumSndBufs) buf = world->mSndBufs + bufnum;
	if (buf) {
		bufarr.bufNum  = bufnum;
		bufarr.bufSize = buf->samples;
		bufarr.bufData = buf->data;
	}
}


// Below added by Gerhard

// InterfaceTable

//extern "C"
//{
//	void load(InterfaceTable *inTable);
//}

//InterfaceTable *ft;

//these are defined in the relevant files
//extern void initVocalTractArea(InterfaceTable *it);
//extern void initFormFreq(InterfaceTable *it);

//void load(InterfaceTable *inTable)
//{
//	ft = inTable;
//	initVocalTractArea(inTable);
//	initFormFreq(inTable);
//}
