Surface {
	var <>space, <rect; // coordinate space

	*new {
		arg ss, rr;
		^super.new.initSurface(ss, rr);
	}

	initSurface {
		arg ss, rr;
		space = ss;
		rect = rr;
	}

	// changes the stroke color
	// from the last flush this will be the stroke color
	// if it is changed more than one times before a flush, always the last value will be the stroke color for all objects drawn since the last flush
	strokeColor {
		arg stroke;
		Pen.strokeColor = stroke;
	}

	// changes the fill color
	// from the last flush this will be the fill color
	// if it is changed more than one times before a flush, always the last value will be the fill color for all objects drawn since the last flush
	fillColor {
		arg fill;
		Pen.fillColor = fill;
	}

	// draws a line
	line {
		arg p1, p2;
		var left, top, right, bottom; // walls
		if (p1 == p2, {
			^nil;
		});
		left = nil;
		top = nil;
		right = nil;
		bottom = nil;
		p1 = space.get(p1);
		p2 = space.get(p2);
		// left
		if ((p1.x-p2.x) != 0, {
			left = (((0-p1.x)/(p1.x-p2.x))*(p1.y-p2.y))+p1.y;
			left = Vector2D(0, left);
		});
		// top
		if ((p1.y-p2.y) != 0, {
			top = (((0-p1.y)/(p1.y-p2.y))*(p1.x-p2.x))+p1.x;
			top = Vector2D(top, 0);
		});
		// right
		if ((p1.x-p2.x) != 0, {
			right = (((rect.width-p1.x)/(p1.x-p2.x))*(p1.y-p2.y))+p1.y;
			right = Vector2D(rect.width, right);
		});
		// bottom
		if ((p1.y-p2.y) != 0, {
			bottom = (((rect.height-p1.y)/(p1.y-p2.y))*(p1.x-p2.x))+p1.x;
			bottom = Vector2D(bottom, rect.height);
		});
		if (((p1-p2).x.abs>(p1-p2).y.abs), {
			Pen.line(left, right);
		}, {
			Pen.line(top, bottom);
		});
		//"line".postln;
	}

	// draws a segment
	segment {
		arg uu, vv;
		uu = space.get(uu);
		vv = space.get(vv);
		Pen.line(uu, vv);
		//"segment".postln;
	}

	// draws a point
	point {
		arg point, size;
		point = space.get(point);
		Pen.line(Vector2D(point.x - (size/2), point.y), Vector2D(point.x + (size/2), point.y));
		Pen.line(Vector2D(point.x, point.y - (size/2)), Vector2D(point.x, point.y + (size/2)));
		//"point".postln;
	}

	// flushes to the screen the drawn objects
	flush {
		Pen.draw(4);
	}
}                                     