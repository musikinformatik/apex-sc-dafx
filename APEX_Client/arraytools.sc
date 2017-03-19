ArrayTools {
	
	*mergeArrays {
		arg arrays;
		var result_array, position_array, count, size;
		count = arrays.size;
		position_array = Array(count);
		size = 0;
		arrays.do({
			arg array;
			size = size + array.size;
			position_array.add(size);
		});
		result_array = Array(size);
		arrays.do({
			arg array;
			array.do({
				arg element;
				result_array.add(element);
			});
		});
		^[result_array, position_array];
	}
	
	*fillExisting {
		arg array, obj;
		array.size.do({
			arg index;
			array.put(index, obj);
		});
		^array;
	}
	
	*fillNew {
		arg size, obj;
		var array;
		array = Array.newClear(size);
		array = ArrayTools.fillExisting(array, obj);
		^array;
	}
	
}
