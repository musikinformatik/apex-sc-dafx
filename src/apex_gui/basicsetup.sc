// It is responsible to store the basic setup informations
// Like: server, path to the data directory, parent window for GUI
// it is also responsible to store the buffers which are the interfaces between VTArea and Synthesizer (so the "area function")
BasicSetup {
	var <server, <path, <parent, <a_array, <x_array, <array_size, <buffer_allocated, setup, connectmap, refresh_run;

	*new {
		arg server_, path_, parent_, setupfname_;
		^super.new.initBasicSetup(server_, path_, parent_, setupfname_);
	}

	initBasicSetup {
		arg server_, path_, parent_, setupfname_;
		server = server_;
		path   = path_;
		parent = parent_;
		buffer_allocated = false;
		connectmap = Dictionary();
		setup = CSVNumArrayLoader.loadCSVtoDictionary(path++"/"++setupfname_);
		refresh_run = false;
	}

	// allocate a_array, x_array, array_size
	// after calling this, server.sync must be called
	// MAYBE THIS IS WHERE SHARED MEMORY COULD BE IMPLEMENTED
	allocateArrayBuffers {
		arg size;
		if (buffer_allocated == true, { ^"Buffers are already allocated!"; });
		server.sync;
		a_array = Buffer.alloc(server, size, 1);
		x_array = Buffer.alloc(server, size, 1);
		array_size = Buffer.alloc(server, 1, 1);
		server.sync;
		array_size.setn(0, [size]);
		server.sync;
		buffer_allocated = true;
	}

	freeArrayBuffers {
		if (buffer_allocated == false, { ^"Buffers are not allocated!"; });
		if (connectmap.size > 0, { ^"Resource busy!"; });
		server.sync;
		a_array.free;
		x_array.free;
		array_size.free;
		server.sync;
		buffer_allocated = false;
	}

	connect {
		arg obj;
		if (buffer_allocated == false, { ^"Buffers are not allocated!"; });
		connectmap[obj] = 1;
	}

	disconnect {
		arg obj;
		connectmap.removeAt(obj);
	}

	getVTSetupFname {
		^setup["vt_setup_fname"];
	}

	getSynthesizerSetupFname {
		^setup["synthesizer_setup_fname"];
	}

	getPlayBackFname {
		^setup["playback_fname"];
	}

	getFPS {
		^setup["fps"];
	}

	notifyRefresh {
		if (refresh_run == false, {
			refresh_run = true;
			Routine.run {
				connectmap.keysValuesDo {
					arg key, value;
					key.refresh;
					//server.sync;
				};
				server.sync;
				refresh_run = false;
			};
		});
	}

	startRefresh {
//		if (this.getFPS() > 0, { SystemClock.sched(0.0, { arg time; this.notifyRefresh; 1/this.getFPS() }); });
		if (this.getFPS() > 0, { AppClock.sched(0.0, { arg time; this.notifyRefresh; 1/this.getFPS() }); });
	}

	stopRefresh {
		AppClock.clear();
		//SystemClock.clear();
	}

	message {
		arg str;
		^"BasicSetup: "++str;
	}

}
