Controller {

	var server, path, window, basicsetup, <vtsetup, <synthesizersetup, <playback;

	*new {
		arg server_, basicsetup_;
		^super.new.initControl(server_, basicsetup_);
	}

	initControl {
		arg server_, basicsetup_;
		server = server_;
		basicsetup = basicsetup_;
		vtsetup = VTSetup(basicsetup);
		synthesizersetup = SynthesizerSetup(basicsetup, vtsetup);
		playback = PlayBack(basicsetup, vtsetup, synthesizersetup);
	}

	boot {
		arg callback; // call this when boot is ready
		Routine.run {
			vtsetup.allocateBuffers();
			synthesizersetup.allocateBuffers();
			basicsetup.allocateArrayBuffers(vtsetup.buffer_size);
			vtsetup.play;
			synthesizersetup.play;
			// Loads initial files for articulation
			playback.play;
			basicsetup.startRefresh();
			callback.value();
		};
	}

	halt {
		arg callback; // call this when halt is ready
		Routine.run {
			basicsetup.stopRefresh();
			playback.stop;
			synthesizersetup.stop;
			vtsetup.stop;
			vtsetup.freeBuffers;
			synthesizersetup.freeBuffers;
			basicsetup.freeArrayBuffers();
			callback.value();
		};
	}

	quit {
		arg callback;
		this.halt(callback);
	}

}
