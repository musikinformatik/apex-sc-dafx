Apex2GUI {
	var server, topWindow;
	var basicsetup, controller, synthesizer;
	var stopButton, quitButton, tractCfg, synthCfg, tractCtr, tractView;

	*new {
		arg server_, path;
		^super.new.init(server_, path);
	}

	init { arg s, path;
		server = s;
		server.boot;
		server.waitForBoot({
			topWindow = Window.new("Apex2", 600@600);
			topWindow.addToOnClose({ this.quit; });
			basicsetup = BasicSetup.new(server, path, topWindow, "basicsetup.csv");
			controller = Controller.new(server, basicsetup);

			this.createPanel(topWindow);

			controller.boot( { this.startDraw; {stopButton.value = 1}.defer; } );
		});
	}

	createPanel { arg view;

		stopButton = Button(view, 50@30)
			.states_([ ["Wait!"], ["Stop"], ["Start"] ])
			.action_({ arg butt;
			if (butt.value == 0, { butt.value.postln; butt.value = 0; controller.boot( { this.startDraw; {butt.value = 1}.defer; });});
			if (butt.value == 2, { butt.value.postln; butt.value = 0; this.stopDraw; controller.halt( { {butt.value = 2}.defer; } );});
			if (butt.value == 1, { butt.value.postln; butt.value = 0; })
		});

		quitButton = Button(view, 50@30)
		.states_([["Quit"]])
		.action_({ arg butt; this.quit });

		tractCfg = TractConfigPanel.new(view, controller);
		synthCfg = SynthConfigPanel.new(view, controller);
		tractCtr = TractControlPanel.new(view, controller);
		tractView = TractViewPanel.new(view, controller, basicsetup);

		view.layout = VLayout(
			HLayout([stopButton, a: \topLeft, s: 0], [quitButton, a: \topLeft, s: 0], nil),
			HLayout([tractCtr.tcView, s: 1], [VLayout([tractView.tcView, s:1],[tractView.areaView, s:1]), s: 1], [VLayout([tractCfg.tcView, s: 1],[synthCfg.tcView, s: 1], [nil, s: 4]), s: 1] );
		);

		view.front;
	}

	startDraw {
		tractView.startDraw;
		synthCfg.startDraw;
	}

	stopDraw {
		tractView.stopDraw;
		synthCfg.stopDraw;
	}

	quit {
		this.stopDraw;
		controller.quit({ server.quit; {topWindow.close}.defer; });
	}

}

TractViewPanel {
	var <areaView, vtview, <tcView;
	var ctr, bsc, inproc_flag, num, axfplot;

	*new { arg parent, controller, basicsetup;
		^super.new.init(parent, controller, basicsetup);
	}

	init {arg wParent, controller, basicsetup;
		ctr = controller;
		bsc = basicsetup;
		vtview = SurfaceView(wParent, Rect(300, 50, 250, 400), CoordSpace(Matrix2x2(2.5, 0, 0, -2.5), Vector2D(10, 150)));
		vtview.view.minSize_(250@400);
		tcView = vtview.view;
		tcView.background = Color(0.9, 0.9, 0.8);

		areaView = View.new(tcView,Rect(0,450,250,150));
		areaView.minSize_(250@150);
		areaView.background = Color(1.0, 1.0, 1.0);

		axfplot = Plotter("Ax", Rect(0,0,250,194), parent: areaView).plotMode = \steps;

		inproc_flag = false;
	}

	startDraw {
		if (ctr.vtsetup.getFPS() > 0, { AppClock.sched(0.0, { arg time; this.draw; 1/ctr.vtsetup.getFPS() }); });
	}

	stopDraw {
		AppClock.clear();
	}

	draw {
		if (inproc_flag == false, {
			inproc_flag = true;
			Routine.run({
				var frontpath, backpath;

				// Get Number of AxFunctions for plotting
				bsc.array_size.get(0, action: {arg axNo; num = axNo });

				frontpath = ctr.vtsetup.loadFront();
				backpath = ctr.vtsetup.loadBack();
				vtview.clearShapes();
				vtview.addShape(PathShape(frontpath, Color.blue));
				vtview.addShape(PathShape(backpath, Color.black));

				bsc.a_array.getn(0,num,action: {arg array; this.axCalc(array)});

				inproc_flag = false;
			}, clock:AppClock)
		});
	}
	axCalc {
		arg array;
		var a;
		a = array;
		{axfplot.value = a.reverse;
			axfplot.maxval = 2.0}.defer;
	}
}

TractConfigPanel {
	var ezPlanes, ezPlanesWidth, ezResolution, ezSkip, <tcView;
	var csPlanes, csPlanesWidth, csResolution, csSkip;

	*new { arg parent, controller;
		^super.new.init(parent, controller);
	}

	init {arg wParent, ctr;
		tcView = View.new(wParent, 200@140);
		tcView.minSize_(200@140);

		csPlanes = ControlSpec.new(10, 35, \lin, 1, ctr.vtsetup.getPlanesNum);
		ezPlanes = EZNumber.new(tcView, Rect(0, 0, 200, 26), "No. of planes", csPlanes, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.vtsetup.setPlanesNum(v.value)} );

		csPlanesWidth = ControlSpec.new(60, 110, \lin, 0.2, ctr.vtsetup.getPlanesWidth);
		ezPlanesWidth = EZNumber.new(tcView, Rect(0, 30, 200, 26), "Planes width", csPlanesWidth, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.vtsetup.setPlanesWidth(v.value)} );

		csResolution = ControlSpec.new(1, 13.5, \lin, 0.001, ctr.vtsetup.getResolution);
		ezResolution = EZNumber.new(tcView, Rect(0, 60, 200, 26), "Resolution", csResolution, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.vtsetup.setResolution(v.value)} );

		csSkip = ControlSpec.new(1, 37, \lin, 1, ctr.vtsetup.getSkip);
		ezSkip = EZNumber.new(tcView, Rect(0, 90, 200, 26), "Skip", csSkip, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.vtsetup.setSkip(v.value)} );
	}

}

SynthConfigPanel {
	var ezSex, ezEnd, ezOpen, ezSpeed, ezSkip, ezFund, ezAmp, <tcView;
	var csSex, csEnd, csOpen, csSpeed, csSkip, csFund, csAmp;
	var ctr; // controller
	var formtext; // show formant frequencies

	*new { arg parent, controller;
		^super.new.init(parent, controller);
	}

	init {arg wParent, controller;
		ctr = controller;
		tcView = View.new(wParent, 200@300);
		tcView.minSize_(200@300);

		csSex = ControlSpec.new(0, 1, \lin, 1, ctr.synthesizersetup.getSpeakerSex);
		ezSex = EZNumber.new(tcView, Rect(0, 0, 200, 26), "Speaker Sex", csSex, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.synthesizersetup.setSex(v.value)} );

		csEnd = ControlSpec.new(0, 1, \lin, 1, ctr.synthesizersetup.getInternEndCorr);
		ezEnd = EZNumber.new(tcView, Rect(0, 30, 200, 26), "End Correction", csEnd, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.synthesizersetup.setEnd(v.value)} );

		csOpen = ControlSpec.new(0, 1, \lin, 1, ctr.synthesizersetup.getOpenAtLeft);
		ezOpen = EZNumber.new(tcView, Rect(0, 60, 200, 26), "Open at Left", csOpen, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.synthesizersetup.setOpen(v.value)} );

		csSpeed = ControlSpec.new(30000, 50000, \lin, 100, ctr.synthesizersetup.getSoundSpeed);
		ezSpeed = EZNumber.new(tcView, Rect(0, 90, 200, 26), "Speed of Sound (c)", csSpeed, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.synthesizersetup.setSpeed(v.value)} );

		csSkip = ControlSpec.new(1, 30, \lin, 1, ctr.synthesizersetup.getSkip);
		ezSkip = EZNumber.new(tcView, Rect(0, 120, 200, 26), "Skip", csSkip, labelWidth: 120, numberWidth: 50)
		.action = ( { |v| ctr.synthesizersetup.setSkip(v.value)} );

		csFund = ControlSpec.new(10, 210, \lin, 1, ctr.synthesizersetup.getF0);
		ezFund = EZSlider.new(tcView, Rect(0, 150, 200, 26), "f0", csFund, labelWidth: 70, numberWidth: 48)
		.action = ( { |v| v.value.postln; ctr.synthesizersetup.setF0(v.value)} );

		csAmp = ControlSpec.new(0, 1, \lin, 0.05, ctr.synthesizersetup.getAmplitude);
		ezAmp = EZSlider.new(tcView, Rect(0, 180, 200, 26), "Amplitude", csAmp, labelWidth: 70, numberWidth: 48)
		.action = ( { |v| ctr.synthesizersetup.setAmplitude(v.value)} );

		formtext = StaticText(tcView, Rect(0, 210, 200, 60));
	}

	startDraw {
		if (ctr.synthesizersetup.getFPS() > 0, { AppClock.sched(0.0, { arg time; this.draw; 1/ctr.synthesizersetup.getFPS() }); });
	}

	stopDraw {
		AppClock.clear();
	}

	draw {
		var str;
		ctr.synthesizersetup.formarray.do( {|v, i| if(i<4, {str = str++format("F%=%\n", i+1, v.round(0.1))})});
		formtext.string = str;
	}

}
TractControlPanel {
	var ezC1, ezC2, ezC3, ezTT, ezLarynx, ezJaw, <tcView;
	var csJaw, csLarynx;

	*new { arg parent, controller;
		^super.new.init(parent, controller);
	}

	init {arg wParent, ctr;
		tcView = View.new(wParent, 200@600);
		tcView.minSize_(200@600);

		ezC1 = ApexXYPad.new(tcView,ctr,"c1",0@0);
		ezC2 = ApexXYPad.new(tcView,ctr,"c2",0@150);
		ezC3 = ApexXYPad.new(tcView,ctr,"c3",0@300);
		ezTT = ApexXYPad.new(tcView,ctr,"tt",0@450, 0, 30, -10, 30);

		csLarynx = ControlSpec.new(15, -10, \lin, 0.1, ctr.vtsetup.getLarynxHeight);
		ezLarynx = EZSlider.new(tcView, Rect(175, 0, 25, 292)," Lryx", csLarynx, labelWidth: 0, numberWidth: 25, labelHeight: 22, layout:\vert);
		ezLarynx.action = { |ez| ctr.vtsetup.setParam("larynx_height", ez.value)} ;
		ezLarynx.font_(Font("Helvetica",10));

		csJaw = ControlSpec.new(25, 0, \lin, 0.1, ctr.vtsetup.getJaw);
		ezJaw = EZSlider.new(tcView, Rect(175, 300, 25, 292)," Jaw ", csJaw, labelWidth: 0, numberWidth: 25, labelHeight: 22, layout:\vert);
		ezJaw.action = { |ez| ctr.vtsetup.setParam("jaw", ez.value)} ;
		ezJaw.font_(Font("Helvetica",10));
	}

}

ApexXYPad {
	var tX,t,tY;
	var cstX, cstY;

	*new { arg parent, controller, label, anchorpoint,  minX = -85, maxX = 85, minY = -65, maxY = 65;
		^super.new.init(parent, controller, label, anchorpoint, minX, maxX, minY, maxY);
	}

	init {arg wParent, ctr, lbl, pt, minX, maxX, minY, maxY;
		cstX = ControlSpec.new(minX, maxX, \lin, 1,ctr.vtsetup.getParam(lbl++"x"));
		cstY = ControlSpec.new(minY, maxY, \lin, 1,ctr.vtsetup.getParam(lbl++"y"));

		t = Slider2D(wParent, Rect(pt.x+25, pt.y, 120, 120));
		t.x = cstX.unmap(ctr.vtsetup.getParam(lbl++"x"));
		t.y = cstX.unmap(ctr.vtsetup.getParam(lbl++"y"));
		t.action = {|sl|
			tX.value = cstX.map(sl.x); ctr.vtsetup.setParam(lbl++"x", tX.value);
			tY.value = cstY.map(sl.y); ctr.vtsetup.setParam(lbl++"y", tY.value);
		};

		tX = EZSlider.new(wParent, Rect(pt.x+25, pt.y+120, 147, 22), nil, cstX, labelWidth: 0, numberWidth: 25);
		tX.action = { |ez| t.x = cstX.unmap(ez.value); ctr.vtsetup.setParam(lbl++"x", ez.value)} ;
		tX.font_(Font("Helvetica",10));

		tY = EZSlider.new(wParent, Rect(pt.x, pt.y, 25, 141), nil, cstY, labelWidth: 0, numberWidth: 25, layout:\vert);
		tY.action = { |ez| t.y = cstY.unmap(ez.value); ctr.vtsetup.setParam(lbl++"y", ez.value)} ;
		tY.font_(Font("Helvetica",10));

		StaticText(wParent, Rect(pt.x+147, pt.y+100, 200, 25)) .string = lbl;
	}

}



	