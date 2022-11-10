SynthesizerSetup {

	var basicsetup, synthesizersetup, synthesizer, vtsetup;
	var <buffer_allocated;
	var onplay, synth;
	var form, bw; // buffers
	var <formarray; // Array of the formants, loaded back from the server

	*new {
		arg basicsetup_, vtsetup_, forcereload;
		^super.new.initSynthesizerSetup(basicsetup_, vtsetup_, forcereload);
	}

	initSynthesizerSetup {
		arg basicsetup_, vtsetup_, forcereload;
		basicsetup = basicsetup_;
		vtsetup = vtsetup_;
		onplay = false;
		buffer_allocated = false;
		if ((synthesizersetup.class != Dictionary) || (forcereload == true), {
			synthesizersetup = CSVNumArrayLoader.loadCSVtoDictionary(basicsetup.path++"/"++basicsetup.getSynthesizerSetupFname());
		});
		synthesizer = Synthesizer(basicsetup.server, basicsetup, vtsetup, this.getSoundSpeed());
		formarray = Array();
	}

	getBufferSize {
		^synthesizersetup["buffersize"];
	}

	getSkip {
		^synthesizersetup["skip"];
	}

	getSoundSpeed {
		^synthesizersetup["c"];
	}

	getSpeakerSex {
		^synthesizersetup["sex"];
	}

	getInternEndCorr {
		^synthesizersetup["internendcorr"];
	}

	getOpenAtLeft {
		^synthesizersetup["openatleft"];
	}

	getF0 {
		^synthesizersetup["f0"];
	}

	getFPS {
		^synthesizersetup["fps"];
	}

	getAmplitude {
		^synthesizersetup["amplitude"];
	}

	getBreathing {
		^synthesizersetup["breathing"];
	}

	setF0 {
		arg f0;
		synthesizersetup["f0"] = f0;
		if (onplay == true,	{ synthesizer.refreshF0(f0); });
	}

	setAmplitude {
		arg ampl;
		synthesizersetup["amplitude"] = ampl;
		if (onplay == true,	{ synthesizer.refreshAmplitude(ampl); });
	}

	setBreathing {
		arg breathing;
		synthesizersetup["breathing"] = breathing;
		if (onplay == true,	{ synthesizer.refreshBreathing(breathing); });
	}

	setSetupArray {
		arg skip, soundspeed, sex, internendcorr, openatleft;
		synthesizersetup["skip"] = skip;
		synthesizersetup["c"] = soundspeed;
		synthesizersetup["sex"] = sex;
		synthesizersetup["internendcorr"] = internendcorr;
		synthesizersetup["openatleft"] = openatleft;
	}

	setSkip {
		arg skip;
		synthesizersetup["skip"] = skip;
	}

	setSpeed {
		arg soundspeed;
		synthesizersetup["c"] = soundspeed;
	}

	setSex {
		arg sex;
		synthesizersetup["sex"] = sex;
	}

	setEnd {
		arg internendcorr;
		synthesizersetup["internendcorr"] = internendcorr;
	}

	setOpen {
		arg openatleft;
		synthesizersetup["openatleft"] = openatleft;
	}

	allocateBuffers {
		if (buffer_allocated == true, { ^"Buffers are already allocated!"; });
		if (onplay == true, { ^"VT synth is already running!"; });
		if (basicsetup.buffer_allocated == true, { ^"Interface buffers already allocated in BasicSetup!"; });
		basicsetup.server.sync;
		form = Buffer.alloc(basicsetup.server, this.getBufferSize(), 1);
		bw = Buffer.alloc(basicsetup.server, this.getBufferSize(), 1);
		basicsetup.server.sync;
		buffer_allocated = true;
	}

	freeBuffers {
		if (onplay == true, { this.stop(); });
		if (buffer_allocated == false, { ^"Buffers were not allocated!"; });
		buffer_allocated = false;
		basicsetup.server.sync;
		form.free;
		bw.free;
		basicsetup.server.sync;
	}

	play {
		if (buffer_allocated == false, { ^"Buffers are not allocated!"; });
		if (onplay == true, { ^"Already playing!"; });
		if (basicsetup.buffer_allocated == false, { ^"Interface buffers not allocated in BasicSetup!"; });
		basicsetup.server.sync;
		basicsetup.connect(this);

//		synth = { FormFreq.kr(this.getSkip(), basicsetup.a_array, basicsetup.x_array, form, bw, this.getSoundSpeed(), this.getInternEndCorr(), this.getOpenAtLeft(), this.getSpeakerSex(), basicsetup.array_size) }.play;
		basicsetup.server.sync;
		synthesizer.play;
		onplay = true;
	}

	stop {
		if (onplay == false, { ^"Not playing!"; });
		"free".postln;
		basicsetup.server.sync;
//		synth.free;
		synthesizer.stop;
		basicsetup.server.sync;
		basicsetup.disconnect(this);
		onplay = false;
	}

	refresh {
		if (onplay == false, { ^"Not playing!"; });
		//basicsetup.server.sync; // TODO: is it correct this way?
		form.loadToFloatArray(action: { arg array; formarray = array; });
		if (formarray.size > 3, { synthesizer.refresh(this.getAmplitude(), this.getF0(), formarray[0], formarray[1], formarray[2], formarray[3], this.getBreathing()); } );
	}

	message {
		arg str;
		^"SynthesizerSetup: "++str;
	}

}
