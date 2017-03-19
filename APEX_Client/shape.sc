Shape {
	var <>surface;

	*new {
		arg surf;
		^super.new.initShape(surf);
	}

	initShape {
		arg surf;
		surface = surf;
	}

	draw {
	}
}       