// Affine transformation to transform the drawable objects into the drawing space

CoordSpace {
	var matrix, vector; // y = matrix*x+vector

	*new {
		arg aa, bb;
		^super.new.init(aa, bb);
	}

	init {
		arg aa, bb;
		matrix = aa;
		vector = bb;
	}

	get {
		arg xx;
		^((matrix*xx)+vector);
	}

	inv {
		arg yy;
		^((matrix.inv)*(yy-vector));
	}

	// <1 zoom in, >1 zoom out
	zoom {
		arg lambda;
		^CoordSpace(matrix*lambda, vector);
	}

	rotate {
		arg alpha;
		^CoordSpace(Matrix2x2(cos(alpha), -1*sin(alpha), sin(alpha), cos(alpha))*matrix, vector);
	}

	translate {
		arg vv;
		^CoordSpace(matrix, vector+vv);
	}

}             