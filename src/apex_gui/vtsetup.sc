VTSetup {

	var basicsetup, vtsetup;
	var <buffer_size;
	var <buffer_allocated;
	var onplay, synth;
	var frontarray_xx, frontarray_yy, backarray_xx, backarray_yy;
	var a,b;
	//var <frontpath, <backpath;

	//buffers:
	var pc1, pc2, pc3, front_xx, front_yy, positionarray, back_xx, back_yy, alpha, beta, mri1_xx, mri1_yy, mri2_xx, mri2_yy, planes_xx, planes_yy, setup, param;

	*new {
		arg basicsetup_;
		^super.new.initVTSetup(basicsetup_);
	}

	initVTSetup {
		arg basicsetup_, forcereload;
		basicsetup = basicsetup_;
		onplay = false;
		buffer_allocated = false;
		if ((vtsetup.class != Dictionary) || (forcereload == true), {
			vtsetup = CSVNumArrayLoader.loadCSVtoDictionary(basicsetup.path++"/"++basicsetup.getVTSetupFname());
		});
		//frontpath = Path([]);
		//backpath = Path([]);
		frontarray_xx = Array();
		frontarray_yy = Array();
		backarray_xx = Array();
		backarray_yy = Array();
	}

	getFname {
		arg str;
		^vtsetup["fname"][vtsetup[str]];
	}

	getSetupArray {
		^[vtsetup["planes_num"], vtsetup["planes_width"], vtsetup["resolution"], vtsetup["skip"]];
	}

	getParamArray {
		^[vtsetup["c1x"], vtsetup["c1y"], vtsetup["c2x"], vtsetup["c2y"], vtsetup["c3x"], vtsetup["c3y"], vtsetup["ttx"], vtsetup["tty"], vtsetup["jaw"], vtsetup["larynx_height"]];
	}

	getParam { arg str;
	    ^vtsetup[str] ;
	}

	getC1 {
		^Vector2D(vtsetup["c1x"], vtsetup["c1y"]);
	}

	getC2 {
		^Vector2D(vtsetup["c2x"], vtsetup["c2y"]);
	}

	getC3 {
		^Vector2D(vtsetup["c3x"], vtsetup["c3y"]);
	}

	getTT {
		^Vector2D(vtsetup["ttx"], vtsetup["tty"]);
	}

	getJaw {
		^vtsetup["jaw"];
	}

	getLarynxHeight {
		^vtsetup["larynx_height"];
	}

	getPlanesNum {
		^vtsetup["planes_num"];
	}

	getPlanesWidth {
		^vtsetup["planes_width"];
	}

	getResolution {
		^vtsetup["resolution"];
	}

	getSkip {
		^vtsetup["skip"];
	}

	getFPS {
		^vtsetup["fps"];
	}

	setParam {
		arg str, val;
		vtsetup[str] = val;
	}

	setParamArray {
		arg c1, c2, c3, tt, jaw, larynx_height;
		vtsetup["c1x"] = c1.x;
		vtsetup["c1y"] = c1.y;
		vtsetup["c2x"] = c2.x;
		vtsetup["c2y"] = c2.y;
		vtsetup["c3x"] = c3.x;
		vtsetup["c3y"] = c3.y;
		vtsetup["ttx"] = tt.x;
		vtsetup["tty"] = tt.y;
		vtsetup["jaw"] = jaw;
		vtsetup["larynx_height"] = larynx_height;
		//this.sendParam();
	}

	setParamArrayRefresh {
		arg c1, c2, c3, tt, jaw, larynx_height;
		this.setParamArray(c1, c2, c3, tt, jaw, larynx_height);
		basicsetup.notifyRefresh;
	}

	setSetupArray {
		arg planes_num, planes_width, resolution, skip;
		vtsetup["planes_num"] = planes_num;
		vtsetup["planes_width"] = planes_width;
		vtsetup["resolution"] = resolution;
		vtsetup["skip"] = skip;
	}

	loadFront {
		//var frontarray_xx, frontarray_yy;
		if (buffer_allocated == false, { ^Path(Segment(Vector2D(0, 0), Vector2D(0, 0))) });
		//basicsetup.server.sync;
		//frontarray_xx = Array();
		//frontarray_yy = Array();
		front_xx.loadToFloatArray(action: { arg array; frontarray_xx = array; });
		front_yy.loadToFloatArray(action: { arg array; frontarray_yy = array; });
		basicsetup.server.sync;
		^Path(frontarray_xx, frontarray_yy);
	}

	loadBack {
		//var backarray_xx, backarray_yy;
		if (buffer_allocated == false, { ^Path(Segment(Vector2D(0, 0), Vector2D(0, 0))) });
		//basicsetup.server.sync;
		//backarray_xx = Array();
		//backarray_yy = Array();
		back_xx.loadToFloatArray(action: { arg array; backarray_xx = array; });
		back_yy.loadToFloatArray(action: { arg array; backarray_yy = array; });
		basicsetup.server.sync;
		^Path(backarray_xx, backarray_yy);
	}

	sendSetup {
		if (onplay == true, { ^"VT synth is already running!"; });
		if (buffer_allocated == false, { ^"Buffers were not allocated!"; });
		if (basicsetup.buffer_allocated == true, { ^"Interface buffers already allocated in BasicSetup!"; });
		//basicsetup.server.sync; // it is important to sync before we can send data to the buffers
		setup.setn(0, this.getSetupArray());
	}

	sendParam {
		if (buffer_allocated == false, { ^"Buffers were not allocated!"; });
		//basicsetup.server.sync; // it is important to sync before we can send data to the buffers
		param.setn(0, this.getParamArray()); // [c1.x, c1.y, c2.x, c2.y, c3.x, c3.y, tip.x, tip.y, jaw, larynx_height, 1]
	}

	allocateBuffers {
		var size, frontarray_xx, frontarray_yy, path, server, front;
		if (buffer_allocated == true, { ^"Buffers are already allocated!"; });
		if (onplay == true, { ^"VT synth is already running!"; });
		if (basicsetup.buffer_allocated == true, { ^"Interface buffers already allocated in BasicSetup!"; });


		server = basicsetup.server;
		path = basicsetup.path;

		frontarray_xx = ArrayTools.mergeArrays ( [
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("larynx"), 0),
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("tongue_n"), 0),
															ArrayTools.fillNew(vtsetup["tongue_over_points"], 0),
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("tongue_blade_under"), 0),
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("mouth_floor"), 0)]);

		frontarray_yy = ArrayTools.mergeArrays ( [
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("larynx"), 1),
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("tongue_n"), 1),
															ArrayTools.fillNew(vtsetup["tongue_over_points"], 0),
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("tongue_blade_under"), 1),
															CSVNumArrayLoader.loadCSVtoArray(path++"/"++this.getFname("mouth_floor"), 1)]);

		positionarray = frontarray_xx[1];
		frontarray_xx = frontarray_xx[0];
		frontarray_yy = frontarray_yy[0];
		size = frontarray_xx.size;

		front_xx = BufferLoader.arraytoBuffer(server, frontarray_xx);
		front_yy = BufferLoader.arraytoBuffer(server, frontarray_yy);
		positionarray = BufferLoader.arraytoBuffer(server, positionarray);

		pc1 = BufferLoader.loadBuffer(server, path++"/"++this.getFname("tongue_pc"), 0, 0);
		pc2 = BufferLoader.loadBuffer(server, path++"/"++this.getFname("tongue_pc"), 1, 0);
		pc3 = BufferLoader.loadBuffer(server, path++"/"++this.getFname("tongue_pc"), 2, 0);
		back_xx = BufferLoader.loadBuffer(server, path++"/"++this.getFname("posterior_wall"), 0, 0);
		back_yy = BufferLoader.loadBuffer(server, path++"/"++this.getFname("posterior_wall"), 1, 0);
		alpha = BufferLoader.loadBuffer(server, path++"/"++this.getFname("alpha_beta"), 0, 0);
		beta = BufferLoader.loadBuffer(server, path++"/"++this.getFname("alpha_beta"), 1, 0);
		mri1_xx = BufferLoader.loadBuffer(server, path++"/"++this.getFname("mri"), 0, 0);
		mri1_yy = BufferLoader.loadBuffer(server, path++"/"++this.getFname("mri"), 1, 0);
		mri2_xx = BufferLoader.loadBuffer(server, path++"/"++this.getFname("mri"), 2, 0);
		mri2_yy = BufferLoader.loadBuffer(server, path++"/"++this.getFname("mri"), 3, 0);
		planes_xx = BufferLoader.loadBuffer(server, path++"/"++this.getFname("planes_curve"), 0, 0);
		planes_yy = BufferLoader.loadBuffer(server, path++"/"++this.getFname("planes_curve"), 1, 0);

		setup = Buffer.alloc(server, 3, 1);
		param = Buffer.alloc(server, 11, 1); // 10 is enough

		front = Path(frontarray_xx, frontarray_yy);
		//buffer_size = (2*front.length) / this.getResolution(); // Why so big?
		buffer_size = (front.length) / this.getResolution(); // Try This - seems ok
		basicsetup.server.sync;
		buffer_allocated = true;
		this.sendSetup(true);
		this.sendParam(true);
		basicsetup.server.sync;
	}

	freeBuffers {
		if (onplay == true, { this.stop(); });
		if (buffer_allocated == false, { "Buffers were not allocated!"; });
		buffer_allocated = false;
		basicsetup.server.sync;
		pc1.free;
		pc2.free;
		pc3.free;
		front_xx.free;
		front_yy.free;
		positionarray.free;
		back_xx.free;
		back_yy.free;
		alpha.free;
		beta.free;
		mri1_xx.free;
		mri1_yy.free;
		mri2_xx.free;
		mri2_yy.free;
		planes_xx.free;
		planes_yy.free;
		setup.free;
		param.free;
		basicsetup.server.sync;
	}

	freeVTSetup {
		this.freeBuffers();
	}

	setResolution {
		arg resolution;
		vtsetup["resolution"] = resolution;
		this.sendSetup();
	}

	setPlanesNum {
		arg num;
		vtsetup["planes_num"] = num;
		this.sendSetup();
	}

	setPlanesWidth {
		arg width;
		vtsetup["planes_width"] = width;
		this.sendSetup();
	}

	setSkip {
		arg skip;
		vtsetup["skip"] = skip;
		this.sendSetup();
	}

	play {
		if (buffer_allocated == false, { ^"Buffers are not allocated!"; });
		if (onplay == true, { ^"Already playing!"; });
		if (basicsetup.buffer_allocated == false, { ^"Interface buffers not allocated in BasicSetup!"; });
		if (this.getResolution() < 0.5, { ^"Resolution must be greater than 0.5!"; });
		basicsetup.server.sync;
		basicsetup.connect(this);
		synth = { VocalTractArea.kr(this.getSkip(), pc1, pc2, pc3, front_xx, front_yy, positionarray, back_xx, back_yy, alpha, beta, mri1_xx, mri1_yy, mri2_xx, mri2_yy, planes_xx, planes_yy, basicsetup.a_array, basicsetup.x_array, basicsetup.array_size, setup, param) }.play;

		basicsetup.server.sync;
		this.loadFront();
		onplay = true;
	}

	stop {
		//basicsetup.a_array.loadToFloatArray(action: {arg array; a=array;{a.plot;}.defer; "done".postln;});
		//basicsetup.x_array.loadToFloatArray(action: {arg array; b=array;{b.plot;}.defer; "done".postln;});
		//basicsetup.array_size.get(0, {|msg| msg.value.postln});
		if (onplay == false, { ^"Not playing!"; });
		"free".postln;
		basicsetup.server.sync;
		synth.free;
		basicsetup.server.sync;
		basicsetup.disconnect(this);
		onplay = false;
	}

	refresh {
		if (onplay == false, { ^"Not playing!"; });
		this.sendParam(); // it will not sync!
	}

	message {
		arg str;
		^"VTSetup: "++str;
	}

}
