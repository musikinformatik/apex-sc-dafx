// Shape defined by path segments
// it stores extra drawing informations, currently only the color of the object

PathShape : Shape {
	var <path, <color;

	*new {
		arg pp, cc, surf;
		^super.new(surf).initPathShape(pp, cc);
	}

	initPathShape {
		arg pp, cc;
		path = pp;
		color = cc;
	}

	draw {
		var prev_point;
		prev_point = nil;
		path.points.do ({
			arg point;
			if (prev_point != nil, {
				surface.segment(prev_point, point);
			});
			prev_point = point;
		});
		surface.strokeColor(color);
		surface.flush();
	}
}  
