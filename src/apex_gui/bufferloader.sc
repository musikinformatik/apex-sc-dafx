BufferLoader {
	
	*allocBuffer {
		arg server, size, channelnum;
		var result;
		result = Buffer.alloc(server, size, channelnum);
		server.sync;
		^result;
	}
	
	*arraytoBuffer {
		arg server, array;
		var result;
		result = Buffer.alloc(server, array.size, 1);
		server.sync;
		result.setn(0, array);
		server.sync;
		^result;
	}
	
	*loadBuffer {
		arg server, fname, column, startrow = 0;
		var result, result_array, arrays, count, size;
		
		// load multiple csv arrays into one buffer
		if (fname.class == Array, {
			count = fname.size;
			arrays = Array(count);
			size = 0;
			count.do({
				arg index;
				arrays.add(CSVNumArrayLoader.loadCSVtoArray(fname[index], column[index], startrow[index]));
				size = size + arrays[index].size;
			});
			result_array = Array(size);
			arrays.do({
				arg array;
				array.do({
					arg element;
					result_array.add(element);
				});
			});
		}, { // load single csv arrays into one buffer
			result_array = CSVNumArrayLoader.loadCSVtoArray(fname, column, startrow);
			size = result_array.size;
		});
		//result_array.postln;
		result = Buffer.alloc(server, size, 1);
		server.sync;
		result.setn(0, result_array);
		server.sync;
		^result;
	}
	
}
