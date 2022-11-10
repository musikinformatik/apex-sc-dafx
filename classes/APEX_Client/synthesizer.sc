Synthesizer {

	var synth1, synth2, server, bsc, ax_arraysize, ax_array, vt,c,tract_delsamps_const;

	*new {
		arg server_, basicsetup, vtsetup, soundspeed;
		^super.new.initSynthesizer(server_, basicsetup, vtsetup, soundspeed);
	}

	initSynthesizer {
		arg server_, basicsetup, vtsetup, soundspeed;
		server = server_;
		bsc = basicsetup;
		vt = vtsetup;
		c = soundspeed;

		// Estimated ax_arraysize and filled array as these are not initialised as yet
		// 16 Ax functions = spatial sampling of 8mm

		ax_arraysize = 64;
		ax_array = Array.fill(ax_arraysize,1.0);

		// vtsetup.getResolution = spatial sampling in mm; c = cm/s
		// tract_delsamps_const = no of samples per Ax secion at given speed of sound.
		tract_delsamps_const = (vtsetup.getResolution)/(c*10.0);
		// check we have greater than 2 samples per tube length for NTube
		(tract_delsamps_const*192000).postln;

		/*SynthDef(\formfreq, {
			arg ampl, f0, f1, f2, f3, f4, breathing;
			var result, envAmp, envFreq;
			envAmp  = Env.new((breathing * [0, 1, 0.5, 0, 0]) + (1 - breathing), (breathing * [0.3, 0.6, 0.2, 1]) + (1 - breathing), 'sine', 3, 0);
			envFreq = Env.new((breathing * [0.3, 1, 0.8, 0.2, 0.2]) + (1 - breathing), (breathing * [0.3, 0.6, 0.2, 1]) + (1 - breathing), 'welch', 3, 0);
			result = RLPF.ar(RLPF.ar(RLPF.ar(RLPF.ar(Saw.ar(f0*EnvGen.kr(envFreq, 1, doneAction: 2), ampl*EnvGen.kr(envAmp)), f4, 0.1), f3, 0.1), f2, 0.1), f1, 0.1);
			Out.ar(0, result);
		}).send(server);*/

		SynthDef(\tubetract, {
			arg f0;
			var loss, result, karray, delayarray,tract_delay_samples;
			loss =1.0;

			// check formants and length
			// coarse tube - is this good enough?
			// what about other formants?
			// additional source?
			// look at how NTube handles these values and how it corresponds to speed of sound

			karray= Control.names([\k_val]).kr(Array.fill(ax_arraysize-1,{|i| (ax_array[i]-ax_array[i+1])/(ax_array[i]+ax_array[i+1])}) );
			delayarray= Control.names([\del_val]).kr(Array.fill(ax_arraysize,tract_delsamps_const) );

			result = KLVocalTract.ar(ApexSource01.ar(f0,0.1,1.4,2),loss, `karray, `delayarray , 1.0);

			Out.ar(1, result);
		}).send(server);

	}

	play {
		server.sync;
		//synth1 = Synth.new(\formfreq);
		synth2 = Synth.new(\tubetract, [\f0, 110]);
		server.sync;
	}

	refresh {
		arg ampl, f0, f1, f2, f3, f4, breathing;
		var k, d;

		bsc.array_size.get(0, action: {arg axNo; ax_arraysize = axNo });
		bsc.server.sync;
		bsc.a_array.loadToFloatArray(0,ax_arraysize,action: {arg array; ax_array = array});

		k = Array.fill(ax_arraysize-1,{|i| (ax_array[i]-ax_array[i+1])/(ax_array[i]+ax_array[i+1])});
		d = Array.fill(ax_arraysize, tract_delsamps_const);

		//synth1.set(\ampl, ampl, \f0, f0, \f1, f1, \f2, f2, \f3, f3, \f4, f4, \breathing, breathing);
		synth2.setn(\f0, f0, \k_val, k,\del_val, d  );

	}

	refreshF0 {
		arg f0;
		//synth1.set(\f0, f0);
		synth2.set(\f0, f0);
	}

	refreshAmplitude {
		arg ampl;
		//synth1.set(\ampl, ampl);
	}

	refreshBreathing {
		arg breathing;
		//synth1.set(\breathing, breathing);
	}

	stop {
		//synth1.free;
		synth2.free;
	}

}
