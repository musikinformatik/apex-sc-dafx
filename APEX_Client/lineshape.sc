LineShape : Shape {
	var line, <color;

	*new {
		arg ll, cc;
		^super.new.initLineShape(ll, cc);
	}

	initLineShape {
		arg ll, cc;
		line = ll;
		color = cc;
	}

	draw {
		if (line != nil, {
			surface.line(line.p1, line.p2);
			surface.strokeColor(color);
			surface.flush;
		});
	}
} 