SurfaceView : Surface {
	var parent, <view, shape_list, <>surface;
	var <>mouseDownAction, <>mouseMoveAction, <>mouseUpAction;

	*new {
		arg pp, rr, ss; // pp - parent, rr - rect, ss - space
		^super.new(ss, rr).initSurfaceView(pp);
	}

	initSurfaceView {
		arg pp;
		parent = pp;
		mouseDownAction = {};
		mouseMoveAction = {};
		// if (pp.class == SCUserView) { pp.throw };
		[pp, rect].postln;
		view = UserView(pp, rect);
		view.drawFunc = {
			this.draw;
		};
		view.mouseDownAction = {
			arg view, x, y, modifiers, buttonNumber, clickCount;
			mouseDownAction.value(view, x, y, modifiers, buttonNumber, clickCount);
		};
		view.mouseMoveAction = {
			arg view, x, y, modifiers, buttonNumber, clickCount;
			mouseMoveAction.value(view, x, y, modifiers, buttonNumber, clickCount);
		};
		view.mouseUpAction = {
			arg view, x, y, modifiers, buttonNumber, clickCount;
			mouseUpAction.value(view, x, y, modifiers, buttonNumber, clickCount);
		};
		shape_list = List();
	}

	addShape {
		arg shape;
		shape.surface = this;
		shape_list.add(shape);
		view.refresh;
		^(shape_list.size - 1);
	}

	replaceShape {
		arg shape, index;
		shape.surface = this;
		shape_list.put(index, shape);
		view.refresh;
		^index;
	}

	clearShapes {
		shape_list = List();
		view.refresh;
	}

	draw {
		//"refresh".postln;
		shape_list.do({
			arg shape;
			shape.draw;
		});
	}

	space_ {
		arg newspace;
		space = newspace;
		view.refresh;
	}

}                                              
