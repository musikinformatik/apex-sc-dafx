// 2x2 matrix class

Matrix2x2 {
	var <row1, <row2; // row vectors

	*new {
		arg a11, a12, a21, a22;
		if ((a21==nil) && (a22==nil), {
			^super.new.init(a11, a12);
		}, {
			^super.new.init(Vector2D(a11, a12), Vector2D(a21, a22));
		})
		
	}

	init {
		arg r1, r2; // row1, row2
		row1 = r1;
		row2 = r2;
	}

	transpose {
		var tmp;
		tmp = row1.y;
		row1.y = row2.x;
		row2.x = tmp;
	}

	det {
		^((row1.x*row2.y) - (row1.y*row2.x));
	}

	* {
		arg bb;
		if (bb.class == Matrix2x2, {
			^Matrix2x2(
				(row1.x*bb.row1.x) + (row1.y*bb.row2.x),
				(row1.x*bb.row1.y) + (row1.y*bb.row2.y),
				(row2.x*bb.row1.x) + (row2.y*bb.row2.x),
				(row2.x*bb.row1.y) + (row2.y*bb.row2.y)
				);
		});
		if (bb.class == Vector2D, {
			^Vector2D(row1*bb, row2*bb);
		});
		if ((bb.class == SimpleNumber) || (bb.class == Integer) || (bb.class == Float), {
			^Matrix2x2(row1*bb, row2*bb);
		});
	}

	/ {
		arg bb;
		^Matrix2x2(row1/bb, row2/bb);
	}

	+ {
		arg bb;
		if (bb.class == Matrix2x2, {
			^Matrix2x2(row1 + bb.row1, row2 + bb.row2);
		});
	}

	- {
		arg bb;
		if (bb.class == Matrix2x2, {
			^Matrix2x2(row1 - bb.row1, row2 - bb.row2);
		});
	}

	// additive inverse
	neg {
		^Matrix2x2(row1.neg, row2.neg);
	}

	// multiplicative inverse
	inv {
		var dd;
		dd = this.det;
		if (dd != 0, {
			^(this.adj/dd);
		});
		//^nil; // probably not necessary
	}

	adj {
		^(Matrix2x2(row2.y, row1.y.neg, row2.x.neg, row1.x));
	}

	printOn {
		arg stream;
		stream << this.class.name << "\n";
		stream << "( " << row1.x << "  " << row1.y << " )\n";
		stream << "( " << row2.x << "  " << row2.y << " )";
	}

}                                       