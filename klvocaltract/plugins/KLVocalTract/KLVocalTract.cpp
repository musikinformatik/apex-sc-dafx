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

//UGens by Nick Collins
//SLUGens released under the GNU GPL as extensions for SuperCollider 3, by Nick Collins http://composerprogrammer.com/index.html
// KLVocalTract based on the NTube model from above - adapted by Damian Murphy. 

#include "SC_PlugIn.h"
#include <stdio.h>

static InterfaceTable *ft;

struct KLVocalTract : public Unit
{
	int numtubes;
	float ** delayright, ** delayleft; //tubes
	int position; //can be same for all lines!
	int maxlength, modulo;
	float delayconversion;
	float f1in, f1out;	//averaging filters f1, f2 for frequency dependent losses; need a storage slot for previous values
	float f2in, f2out;

	//convenience variables for copying particular input data
	float * losses;
	float * scattering;
	float * delays;

	float * rightouts;
	float * leftouts;

};

extern "C" {

	void KLVocalTract_next(KLVocalTract *unit, int inNumSamples);
	void KLVocalTract_Ctor(KLVocalTract* unit);
	void KLVocalTract_Dtor(KLVocalTract* unit);

}

////output= NTube.ar(input, loss, karray, delaylengtharray);
void KLVocalTract_Ctor(KLVocalTract* unit) {

	int i,j;

	int numinputs = unit->mNumInputs;
	int numtubes= (numinputs-1)/3;  //NOW 1+ (N+1) + N-1 + N 3N+1//WAS 1+1+N-1+N = 2N+1
	unit->numtubes= numtubes;

	if(numtubes<2) {
		printf("too few tubes! only %d \n", numtubes);
		return;
	}

	unit->maxlength= 1024; //no frequencies below about 50 Hz for an individual section
	unit->modulo= unit->maxlength-1;

	unit->delayconversion= unit->mRate->mSampleRate; //multiplies delay time in seconds to make delay time in samples

	//printf("num tubes only %d and delayconversion %f \n", numtubes, unit->delayconversion);

	unit->delayright= (float**)RTAlloc(unit->mWorld, numtubes * sizeof(float *));
	unit->delayleft= (float**)RTAlloc(unit->mWorld, numtubes * sizeof(float *));

	for (i=0; i<numtubes; ++i) {

		unit->delayright[i]= 	(float*)RTAlloc(unit->mWorld, unit->maxlength * sizeof(float));
		unit->delayleft[i]= 	(float*)RTAlloc(unit->mWorld, unit->maxlength * sizeof(float));

		float * pointer1 = 	unit->delayright[i];
		float * pointer2 = 	unit->delayleft[i];

		for (j=0; j<unit->maxlength; ++j) {
			pointer1[j]= 0.0;
			pointer2[j]= 0.0;
		}

	}

	unit->losses= (float*)RTAlloc(unit->mWorld, (numtubes+1) * sizeof(float));
	unit->scattering= (float*)RTAlloc(unit->mWorld, (numtubes-1) * sizeof(float));
	unit->delays= (float*)RTAlloc(unit->mWorld, numtubes * sizeof(float));

	unit->rightouts= (float*)RTAlloc(unit->mWorld, numtubes * sizeof(float));
	unit->leftouts= (float*)RTAlloc(unit->mWorld, numtubes * sizeof(float));

	unit->position=0;

	unit->f1in= 0.0;
	unit->f1out= 0.0;
	unit->f2in=0.0;
	unit->f2out=0.0;

	SETCALC(KLVocalTract_next);
}

void KLVocalTract_Dtor(KLVocalTract* unit) {

	int i;

	for (i=0; i<unit->numtubes; ++i) {

		RTFree(unit->mWorld, unit->delayright[i]);
		RTFree(unit->mWorld, unit->delayleft[i]);
	}

	RTFree(unit->mWorld, unit->delayright);
	RTFree(unit->mWorld, unit->delayleft);

	RTFree(unit->mWorld, unit->scattering);
	RTFree(unit->mWorld, unit->delays);
	RTFree(unit->mWorld, unit->losses);

	RTFree(unit->mWorld, unit->rightouts);
	RTFree(unit->mWorld, unit->leftouts);

}

void KLVocalTract_next(KLVocalTract *unit, int inNumSamples) {

	int i,j;

	int numtubes= unit->numtubes; // N of these

	//value to store
	float * in= IN(0);
	float * out= OUT(0);

	float ** right= unit->delayright;
	int pos= unit->position;
	float ** left= unit->delayleft;

	//GET FREQUENCIES AND SCATTERING COEFFICIENTS
	float * losses= unit->losses;                       // N+1 of these
	float * scatteringcoefficients= unit->scattering;   // N-1 of these
	float * delays= unit->delays;                       // N = Number of tubes, Tube section delay in seconds 

	int arg=1;

	//used to be single argument
	//float loss= (float)ZIN0(1);

	for (i=0; i<(numtubes+1); ++i)	{

		losses[i]= ZIN0(arg);
		++arg;
	}

	//	for (i=0; i<(numtubes+1); ++i)	{
	//
	//		printf("loss %d is %f ",i, losses[i]);
	//	}
	//	printf("\n");

	for (i=0; i<(numtubes-1); ++i) {

		scatteringcoefficients[i]= ZIN0(arg);
		++arg;
	}

	int maxlength= unit->maxlength;
	float maxlengthf= (float) maxlength;
	float maxlengthfminus1= (float) (maxlength-1);
	int modulo= unit->modulo;

	float delayconv= unit->delayconversion;

	for (i=0; i<numtubes; ++i) {

		float delayinsec= ZIN0(arg);
		float delayinsamples= delayconv*delayinsec;

		if(delayinsamples<0.0) delayinsamples=0.0;
		if(delayinsamples>maxlengthfminus1) delayinsamples= maxlengthfminus1;

		delays[i]= delayinsamples; //ZIN0(arg);

		//printf("delay %d is %f \n", i, delays[i]);
		++arg;
	}

	//have to store filter state around loop; probably don't need to store output, but oh well
	float f1in=unit->f1in; 
	float f2in=unit->f2in;
	float f2out=unit->f2out;
	float f1out=unit->f1out;

	float * delayline;
	float * delayline2;
	float past;
	int pos1, pos2;
	float interp; //for linear interpolation of position

	float * rightouts= unit->rightouts;
	float * leftouts= unit->leftouts;

	for (i=0; i<inNumSamples; ++i) {

		//update all outs

		for (j=0; j<numtubes; ++j) {

			//calculate together since share position calculation, same delay length in each tube section
			delayline= right[j];
			delayline2= left[j];

			past = fmod(pos+maxlengthf- delays[j], maxlengthf);

			pos1= past; //round down
			interp= past-pos1;
			pos2= (pos1+1)&modulo;

			//printf("check tube %d for sample %d where pos1 %d pos2 %d interp %f \n",j,i,pos1,pos2, interp);

			//printf("%p %p \n",delayline, delayline2);

			//int h;
			//			for (h=0; h<maxlength; ++h) {
			//				printf("%f ",delayline[h]);
			//			}
			//			printf("\n");
			//			for (h=0; h<maxlength; ++h) {
			//				printf("%f ",delayline2[h]);
			//			}
			//			printf("\n");
			//
			//linear interpolation to allow non sample frequencies
			rightouts[j]= ((1.0-interp)*delayline[pos1]) + (interp*delayline[pos2]);
			leftouts[j]= ((1.0-interp)*delayline2[pos1]) + (interp*delayline2[pos2]);

		}

		//printf("got to here! %d \n",i);

		//output value, right going output of right-most tube (lip end)
		out[i]=rightouts[numtubes-1];

		//NTube based: including filters at the ends:

		//update all filters
        
        //Filter1 = f1 = Glottis End
		// f1out = 0.969*(leftouts[0]);
        // 
        //f1out= losses[0]*0.5*(f1in+leftouts[0]);
		//f1in= leftouts[0];
        
        // Filter2 = f2 = Lip End
        // Simple LPF
        //f2out= losses[numtubes]*(0.5*f2in+0.5*rightouts[numtubes-1]);
		//f2in= rightouts[numtubes-1];

        // NOTE: Setting ends as follows gives closed pipe QWR behaviour.
        f1out = leftouts[0];
        f2out = rightouts[numtubes-1];
        
        // Lip Reflection coefficient, from Mullen
        f2out = -0.9*rightouts[numtubes-1];
        // Glottis reflection coefficient from Mullen
        f1out = 0.969*leftouts[0];
        
		delayline= right[0];
		delayline2= left[numtubes-1];

        // what value of pos?
        // Input goes here
		delayline[pos]= in[i]+f1out;
		delayline2[pos]= f2out;

		//then update all other ins via numtubes-1 scattering junctions

		//printf("got to here 2! %d \n",i);

		for (j=0; j<(numtubes-1); ++j) {

			float k = scatteringcoefficients[j];

			delayline= right[j+1];
			delayline2= left[j];

			//Version one: no internal friction, too long
			//delayline[pos]= rightouts[j]*(1+k)+ ((-k)*leftouts[j+1]);
			//delayline2[pos]= rightouts[j]*k+ ((1-k)*leftouts[j+1]);

            //Version 2 as used in NTube
			//float loss= losses[j+1];
			//always a loss at interface to avoid continual recirculation; separate internal loss parameter?
			//delayline[pos]= rightouts[j]*(1+k)+ (loss*(-k)*leftouts[j+1]);
			//delayline2[pos]= (rightouts[j]*k*loss)+ ((1-k)*leftouts[j+1]);

            //Version 3 for speed, see e.g. Mullen's thesis
            float w = k*(rightouts[j]-leftouts[j+1]);
            delayline[pos]= rightouts[j]+w;
			delayline2[pos]= leftouts[j+1]+w;
            
			//calculate inputs of all delays
			//d2right[d2rightpos]= d1rightout*(1+k)+ ((-k)*d2leftout);
			//d1left[d1leftpos]= d1rightout*k+ ((1-k)*d2leftout);

		}

		//update common delay line position pointer
		//update position
		pos= (pos+1)&modulo;

		//printf("got to here 3! %d %d \n",i,pos);
	}

	unit->f1in= f1in;
	unit->f2in= f2in;
	unit->f2out=f2out;
	unit->f1out= f1out;

	unit->position= pos;
}

PluginLoad(KLVocalTract)
{

	ft = inTable;
    	
    DefineDtorCantAliasUnit(KLVocalTract);
}
