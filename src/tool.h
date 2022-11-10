/*

Defaults, simple functions, etc.

*/
#ifndef tool_h
#define tool_h

#include "SC_PlugIn.h"
#include <iostream>
#include <string>
#include <sstream>

// pi
#define pi 3.1415926535897932384626433832795028841971693993751

// signum(xx) macro [+1, 0, -1]
#define signum(xx) ((xx > 0) - (xx < 0))

struct BufferArray
{
	uint32 bufNum;
	float* bufData;
	int bufSize;
};

// returns the minimum of aa and bb
template <typename T>
T min(const T& aa, const T& bb)
{
	if (aa<bb) return aa;
	return bb;
}

// returns the maximum of aa and bb
template <typename T>
T max(const T& aa, const T& bb)
{
	if (aa<bb) return bb;
	return aa;
}

// convert number (or other variable) to string
template <typename T>
std::string to_string(const T& value)
{
    std::ostringstream s;
    s << value;
    return s.str();
}

float* loadFloatData(Unit* unit, uint32 bufnum);
BufferArray getBuffer(Unit* unit, uint32 bufnum);
void setBuf(Unit* unit, uint32 bufnum, BufferArray& bufarr);

//extern InterfaceTable *ft;

#endif

