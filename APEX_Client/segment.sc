// represents a segment by storing it's end points (and those can't be the same point)
// sometimes also represents a line wich goes through the two points
Segment {
	var <p1, <p2, <length;

	*new {
		arg uu, vv;
		^super.new.initSegment(uu, vv);
	}

	initSegment {
		arg uu, vv;
		if (uu == vv, {
			^nil;
		});
		p1 = uu;
		p2 = vv;
		length = this.calculateLength;
	}

	calculateLength {
		^((p2-p1).length);
	}

	// midPoint between the two endpoints
	midPoint {
		^((p2 + p1) / 2);
	}

	// vector with direction p2 - p1
	getVector {
		^(p2 - p1);
	}

	// returns the point of the segment at the position pos between p1 and p2
	getPointAt {
		arg pos;
		^this.getPointAtNormal(pos/this.length);
	}

	// same as getPointAt but the arg norm_pos should be mapped between 0 and 1
	getPointAtNormal {
		arg norm_pos;
		if ((norm_pos > 1) || (norm_pos < 0), {
			^nil;
		});
		^(p1 + ((p2 - p1)*norm_pos));
	}

	// calculates the intersection point between two lines (this, arg line)
	// in case of parallel lines it returns nil
	intersectLineLine {
		arg line;
		var aa, bb, cc;
		aa = Matrix2x2((p2-p1).x, (line.p2-line.p1).x, (p2-p1).y, (line.p2-line.p1).y);
		bb = line.p1-p1;
		if (aa.det != 0, {
			cc = aa.adj*bb; // make it faster: don't divide all 4 matrix elements by the determinant, only the end result's x coord
			^(p1 + ((p2 - p1) * (cc.x / aa.det)));
		}, {
			^nil;
		});		
	}

	// this is a line, arg segment is a segment
	// returns the intersection point
	// returns nil when the line and the segment are parallel to each other
	// returns nil when the intersection point is not on the segment
	intersectLineSegment {
		arg segment;
		var point;
		point = this.intersectLineLine(segment);
		if (point != nil, {
			if ((point - segment.p1) * (point - segment.p2) <= 0, {
				^point;
			});
		});
		^nil;
	}

	// this is a segment, arg segment is a segment
	// returns the interPsection point
	// returns nil when the two segments are parallel to each other
	// returns nil when the intersection point is not on at least one of the segment
	intersectSegmentSegment {
		arg segment;
		var point;
		point = this.intersectLineSegment(segment);
		if (point != nil, {
			if ((point - p1) * (point - p2) <= 0, {
				^point;
			});
		});
		^nil;
	}
}                                                    