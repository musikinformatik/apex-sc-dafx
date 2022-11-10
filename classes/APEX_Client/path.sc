// TODO: intersection functions are mess.
Path {
	var <points, <length;
	var last_index, last_pos;

	// parameters should be:
	// 1. pp is a Segment: the path will be the segment, so it will contain only two points
	// 2. pp is an array, yy is not an array: pp is considered to be a Vector2D array containing the points of the path
	// 3. pp is an array and yy is an array too: pp and yy are considered to be arrays of the coordinates of the points (x, y)
	*new {
		arg pp, yy;
		^super.new.initPath(pp, yy);
	}

	initPath {
		arg pp, yy;
		var xx;
		if (((pp.class == Array) || (pp.class == FloatArray)) && ((yy.class == Array) || (yy.class == FloatArray)), { // if the parameters are two arrays for the coordinates of the points
			xx = pp;
			pp = Array(xx.size);
			xx.size.do({
				arg index;
				pp.add(Vector2D(xx[index], yy[index]));
			});
		});
		if (pp.class == Segment, {
			points = [pp.p1, pp.p2];
			length = pp.length;
		}, {
			points = pp;
			this.calculateLength;
		});
		last_index = 0;
		last_pos = 0;
	}

	calculateLength {
		var prev_point;
		prev_point = nil;
		length = 0;
		points.do({
			arg point;
			if (prev_point != nil, {
				length = length + (prev_point-point).length;
			});
			prev_point = point;
		});
	}

	at {
		arg index;
		^this.points[index];
	}

	size{
		^this.points.size;
	}

	transform {
		arg space;
		var newpoints;
		newpoints = Array(this.points.size);
		this.points.do({
			arg point;
			newpoints.add(space.get(point));
		});
		^Path(newpoints);
	}

	// the line array contains one or more lines, the order is important!
	// this function does not search for all intersections!
	// for the intersection check we first check the 1. line with 1. segment, if there is no intersection then check 1. line 2. segment, ...
	// if we find an intersecting segment with index m, then we check 2. line with m. segment, 2. line with m+1. segment, ...
	// TODO: currently it doesn't contain a line-segment intersection check to make it faster
	orderedLineArrayIntersection {
		arg line_array, complete;
		var point_array;
		var point;
		var ii, kk; // ii - line index, kk - segment index
		var intersect;
		point_array = Array(line_array.size);
		ii = 0;
		kk = 1;
		while ({ii<line_array.size}, {
			point_array.add(nil); // if we have a line that don't have an intersection with any segment, then put a nil to the corresponding place in the result array
			intersect = false;
			if (complete == true, {
				kk = 1;
			});
			while ({(intersect == false) && (kk<points.size)}, {
				point = line_array[ii].intersectLineSegment(Segment(points[kk], points[kk-1]));
				if (point != nil, {
					point_array[ii]=point;
					intersect = true;
				}, {
					kk = kk + 1;
				});
			});
			ii = ii + 1;
		});
		^point_array;
	}

	// line vs. all segments of the path intersection
	// if midpoint is given, then it calculates all intersection points and choosees the closest one
	// it contains a line-segment intersection check before calculating the intersection point
	// if segmentsegment is true, then segment vs path intersection will be tested
	// if getpathpos is true, then instead of returning the intersection point, it returns the position of the point along the path (so getPointAt(lineIntersection(getpathpos: true)) = lineIntersection should be true)
	// returns nil if there is no intersection, returns the point if getpathpos is not true, if getpathpos is true: return the position
	lineIntersection {
		arg line, midpoint, segmentsegment, getpathpos;
		var point, cur_point, min_distance, cur_distance, pathpos, lastpos;
		var v1, v2, nline, nline2;
		var kk; // segment index
		point = nil;
		min_distance = 0;
		pathpos = 0;
		lastpos = 0;
		kk = 1;
		nline = (line.p2 - line.p1).rotate90;
		v2 = ((points[0] - line.p1)*nline).sign;
		while ({(kk<points.size)}, {
			v1 = v2;
			v2 = ((points[kk] - line.p1)*nline).sign;
			if (segmentsegment == true, { // if we check only segment vs segment: if the other way they don't intersect, make v1 and v2 equal not to enter the intersection branch
				nline2 = (points[kk]-points[kk-1]).rotate90;
				if (((line.p1 - points[kk-1])*nline2).sign == ((line.p2 - points[kk-1])*nline2).sign, {
					v1 = v2;
				});
			});
			if ((v1 != v2), { // intersection!
				cur_point = line.intersectLineLine(Segment(points[kk], points[kk-1]));
				if (getpathpos == true, {
					pathpos = lastpos + (cur_point-points[kk-1]).length;
				});
				if ((midpoint.class == Vector2D),{
					cur_distance = (cur_point - midpoint).length;
					if ((point == nil) || (cur_distance < min_distance),{
						min_distance = cur_distance;
						point = cur_point;
					});
				}, {
					if (getpathpos == true, {
						^pathpos;
					}, {
						^cur_point;
					});
				});
			}, {
				lastpos = lastpos + (points[kk]-points[kk-1]).length;
				kk = kk + 1;
			});
		});
		if (getpathpos == true, {
			^pathpos;
		}, {
			^point;
		});
	}

	// line array vs. all segment of the path intersection
	// it calls the lineIntersection method
	// it gives back the closest intersections to the midpoint_array ith point for the ith line (if midpoint_array is given and is an array)
	// if segmentsegment is true, then segment vs path intersection will be tested
	// the result is an array of intersection points, or if getpathpos is true, then the result is an array of positions along the path
	// if there was no intersection between the path and the ith line then the result's ith position will be nil
	lineArrayIntersection {
		arg line_array, midpoint_array, segmentsegment, getpathpos;
		var ii;
		var point_array;
		point_array = Array(line_array.size);
		if (midpoint_array.class != Array, {
			midpoint_array = Array(line_array.size);
		});
		ii = 0;
		while ({ii<line_array.size}, {
			point_array.add(nil); // if we have a line that don't have an intersection with any segment, then put a nil to the corresponding place in the result array
			point_array[ii]=this.lineIntersection(line_array[ii], midpoint_array[ii], segmentsegment, getpathpos);
			ii = ii + 1;
		});
		^point_array;
	}

	// returns the point on the path wich is at the arg pos position (pos must be between 0 and length)
	// uses previously stored data to speed up the process, so calling this multiple times with i.e. growing arg pos it will be faster
	getPointAt {
		arg pos;
		var direction, point;
		if ((pos > length) || (pos < 0), {
			^nil;
		});
		if (pos >= last_pos, {
			direction = 1;
		}, {
			direction = 1.neg;
		});
		while ({(last_index >= 0) && (last_index < (points.size - 1))}, {
			point = Segment(points[last_index], points[last_index + 1]).getPointAt(pos - last_pos);
			//point.postln;
			if (point != nil, {
				^point;
			});
			if (direction == 1, {
				last_pos = last_pos + Segment(points[last_index], points[last_index+1]).length;
			});
			if ((direction == 1.neg) && (last_index > 0), {
				last_pos = last_pos - Segment(points[last_index-1], points[last_index]).length;
			});
			last_index = last_index + direction;
		});
		^nil;
	}

	// same as getPointAt but it's argument must be between 0 and 1
	getPointAtNormal {
		arg norm_pos;
		^this.getPointAt(norm_pos*this.length);
	}

	// load path from CSV file
	loadFromCSVFile {
		arg fname;
		var csv;
		var point_array;
		csv = CSVNumArrayLoader(fname);
		point_array = Array(csv.rowCount);
		csv.rowDo({
			arg row;
			point_array.add(Vector2D(row[0], row[1]));
		});
		this.initPath(point_array);
	}

	concat {
		arg path;
		var newpoints;
		newpoints = this.points;
		path.points.do({
			arg element;
			newpoints = newpoints.add(element);
		});
		^(Path(newpoints));
	}

}
