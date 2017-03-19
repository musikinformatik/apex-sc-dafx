/* Voice source functions for Apex2
   Sten Ternström

#01: This pseudo-ugen implements a time-reversed glottal flow model, in effect an L-F model without a return phase. The time reversal is of no consequence for the power spectrum, but it does change the waveform.

The internal dependencies on fo serve to keep the output amplitude more constant when fo is varied. For output as sound (pressure), a +6dB/oct radiation characteristic is added.

The Blip (band-limited impulse) generator is limited to 200 harmonics by default. This truncates the spectrum at low values of fo ( fo < (fs/2)/200 ). If this is a problem, add an increased numharm: argument to the Blip call.

Parameters:

fo   	fundamental frequency of oscillation in Hz
invQ 	reciprocal of Q of "glottal formant" resonance
		(affects spectral slope: hi Q => more high frequencies)
scale   scale factor of glottal formant frequency re. fo
mul  	gain multiplier  (linear)

*/

ApexSource01 {
	*ar { arg fo=100, invQ=0.1, scale=1.4, mul=1;
		var flow;
		flow = RLPF.ar(Blip.ar(fo, mul: 10000), scale*fo, invQ, invQ/fo);
		^HPZ1.ar(flow, mul);   // +6 dB/octave
	}
}
