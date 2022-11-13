PlayBack {

	var basicsetup, vtsetup, synthesizersetup, record, starttime, recordindex, onplay;

	*new {
		arg basicsetup_, vtsetup_, synthesizersetup_;
		^super.new.initPlayBack(basicsetup_, vtsetup_, synthesizersetup_);
	}

	initPlayBack {
		arg basicsetup_, vtsetup_, synthesizersetup_, forcereload = false;
		basicsetup = basicsetup_;
		vtsetup = vtsetup_;
		synthesizersetup = synthesizersetup_;
		if ((record.class != Dictionary) || (forcereload == true), {
			record = CSVNumArrayLoader.loadCSVRowstoDictionaryArrays(basicsetup.path++"/"++basicsetup.getPlayBackFname());
		});
		if (basicsetup.getPlayBackFname().class != String, {
			record = Dictionary();
			record["time"] = Array();
		});
		onplay = false;
	}

	play {
		if (onplay == true, { ^"Already playing!"; });
		basicsetup.connect(this);
//		starttime = SystemClock.beats;
		starttime = AppClock.beats;
		"playback".postln;
		recordindex = 0;
		onplay = true;
	}

	stop {
		if (onplay == false, { ^"Not playing!"; });
		basicsetup.disconnect(this);
		onplay = false;
	}

	getRecordSize {
		^record["time"].size;
	}

	interpolatedValue {
		arg index, time, valuename;
		var tprev, tnext, valueprev, valuenext;
		if (recordindex + 1 >= this.getRecordSize(), { ^0; });
		tprev = record["time"][recordindex];
		tnext = record["time"][recordindex + 1];
		valueprev = record[valuename][recordindex];
		valuenext = record[valuename][recordindex + 1];
		^(((valueprev * (tnext - time)) + (valuenext * (time - tprev))) / (tnext - tprev));
	}

	refresh {
		var time, position_found;
//		time = SystemClock.beats - starttime;
		time = AppClock.beats - starttime;
		// Seek to the current position
		position_found = false;
		while({(recordindex + 1 < this.getRecordSize) && (position_found == false)}, {
			position_found = ((time < record["time"][recordindex + 1]) && (time >= record["time"][recordindex]));
			if (position_found == false, { recordindex = recordindex + 1; });
		});
		if (recordindex + 1 < this.getRecordSize, {
			vtsetup.setParamArray(
				Vector2D(this.interpolatedValue(recordindex, time, "c1x"), this.interpolatedValue(recordindex, time, "c1y")),
				Vector2D(this.interpolatedValue(recordindex, time, "c2x"), this.interpolatedValue(recordindex, time, "c2y")),
				Vector2D(this.interpolatedValue(recordindex, time, "c3x"), this.interpolatedValue(recordindex, time, "c3y")),
				Vector2D(this.interpolatedValue(recordindex, time, "ttx"), this.interpolatedValue(recordindex, time, "tty")),
				this.interpolatedValue(recordindex, time, "jaw"),
				this.interpolatedValue(recordindex, time, "larynx_height")
			);
			synthesizersetup.setF0(this.interpolatedValue(recordindex, time, "f0"));
			synthesizersetup.setAmplitude(this.interpolatedValue(recordindex, time, "amplitude"));
			synthesizersetup.setBreathing(this.interpolatedValue(recordindex, time, "breathing"));
		});
	}
}
