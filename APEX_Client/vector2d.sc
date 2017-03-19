// 2D vector class

Vector2D : Point {
	*new {
		^super.new;
	}

	* {
		arg bb;
		if (bb.class == Vector2D, {
			^((x*bb.x)+(y*bb.y));
		});
		if ((bb.class == SimpleNumber) || (bb.class == Integer) || (bb.class == Float), {
			^Vector2D(x*bb, y*bb);
		});
		if (bb.class == Matrix2x2, {
			^(bb*this);
		});
	}

	neg {
		^Vector2D(x.neg, y.neg);
	}

	- {
		arg bb;
		^Vector2D(x - bb.x, y - bb.y);
	}

	+ {
		arg bb;
		^Vector2D(x + bb.x, y + bb.y);
	}

	/ {
		arg bb;
		^Vector2D(x/bb, y/bb);
	}

	abs {
		^(this.dist(0, 0));
	}

	length {
		^(this.dist(0, 0));
	}

	// rotate the vector by 90 degrees
	rotate90 {
		^(Vector2D(y.neg, x));
	}

	rotate {
		arg alpha;
		var cosa, sina;
		cosa = alpha.cos;
		sina = alpha.sin;
		^(Vector2D((cosa * x) - (sina * y), (sina * x) + (cosa * y)));
	}

	normal {
		^(this/this.length);
	}

	point {
		^Point(x, y);
	}
}     