Vector2DInput {

	var <vector, xx, yy, parent, rect, space, surfaceview, <>onChange, <label, labeltext, positiontext, locky, color, round;

	*new {
		arg pp, rr, ss, vv, ll="", locky_ = false, cc = Color.blue, round_ = false;
		^super.new.initVector2DInput(pp, rr, ss, vv, ll, locky_, cc, round_);
	}

	initVector2DInput {
		arg pp, rr, ss, vv, ll, locky_, cc, round_;
		color = cc;
		label = ll;
		round = round_;
		space = ss;
		parent = pp;
		rect = rr;
		locky = locky_;
		onChange = {};
		vector = Vector2D(0, 0);
		if (vv.class == Vector2D, {
			vector = vv;
		});

		labeltext = StaticText(pp, Rect(rect.left, rect.top, rect.width, 20));
		labeltext.string = label;
		
		surfaceview = SurfaceView(parent, rect, ss);
		surfaceview.mouseDownAction = {
			arg view, x, y, modifiers, buttonNumber, clickCount;
			this.changePosition(x, y);
		};
		surfaceview.mouseMoveAction = surfaceview.mouseDownAction;
		this.draw;
	}

	changePosition {
		arg x, y;
		if (locky == true, { y = space.get(vector).y; });
		if (x > rect.width) { x = rect.width; };
		if (x < 0) { x = 0; };
		if (y > rect.height) { y = rect.height; };
		if (y < 0) { y = 0; };
		if ((x!=xx) || (y!=yy)) {
			xx = x;
			yy = y;
			vector = space.inv(Vector2D(xx, yy));
			if (round == true, { vector = Vector2D(vector.x.round, vector.y.round); });
			this.draw;
			onChange.value(vector);
		}
	}

	setVector {
		arg vv, do_call;
		vector = vv;
		this.draw;
		if (do_call != false, {
			onChange.value(vector);
		});
	}

	draw {
		var strvalue;
		if (locky == true, {
			strvalue = ": "++vector.x;
		}, {
			strvalue = ": (" ++ vector.x ++ ", " ++ vector.y ++ ")";
		});
		labeltext.string = label ++ strvalue;
		surfaceview.clearShapes;
		surfaceview.addShape(LineShape(Segment(Vector2D(0, 0), Vector2D(1, 0)), Color.black));
		if (locky == true, {
			surfaceview.addShape(PathShape(Path(Segment(Vector2D(0, 0), Vector2D(0, (rect.height/2).neg))), Color.black));
		}, {
			surfaceview.addShape(LineShape(Segment(Vector2D(0, 0), Vector2D(0, 1)), Color.black));
		});
		surfaceview.addShape(CrossShape(vector, 20, color));
	}
}       
