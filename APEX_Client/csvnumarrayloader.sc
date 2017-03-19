CSVNumArrayLoader {
	var <csv_data;

	*new {
		arg fname;
		^super.new.initCSVNumArrayLoader(fname);
	}

	initCSVNumArrayLoader {
		arg fname, noreplace;
		var csv_raw;
		csv_raw = FileReader.read(fname, true, true, delimiter: $;);
		csv_data = Array(csv_raw.size);
		csv_raw.do({
			arg row;
			var new_row;
			new_row = Array(row.size);
			row.do({
				arg cell;
				new_row.add(cell.replace(",", ".").asFloat); // for swedish and hungarian notation :)
			});
			csv_data.add(new_row);
		});
	}

	rowDo {
		arg func;
		csv_data.do({
			arg row;
			func.value(row);
		});
	}

	rowCount {
		^csv_data.size;
	}

	size {
		^csv_data.size;
	}

	at {
		arg row, col;
		^csv_data[row][col];
	}
	
	*loadCSVtoArray {
		arg fname, column, startrow = 0;
		var cvs, arr, size, result;
		cvs = CSVNumArrayLoader(fname);
		size = cvs.rowCount() - startrow;
		arr = Array.new(size);
		(size).do({
			arg index;
			arr.add(cvs.at(index + startrow, column));
		});
		^arr;
	}
	
	
	// load from a specified format, like in "basicsetup.csv", "vtsetup.csv", "synthesizersetup.csv"
	*loadCSVtoDictionary {
		arg fname;
		var csv, result, key, value, type;
		csv = FileReader.read(fname, true, true, delimiter: $;);
		result = Dictionary(csv.size);
		csv.do({
			arg row;
			key = nil;
			value = nil;
			row.size.do({
				arg index;
				if (row[index] == "", {
					row[index] = nil;
				}, {
					row[index] = row[index].replace("\"", "");
				});
			});
			if (row.size >= 3, {
				key = row[0];
				type = row[1];
				if (type == "text", { value = row[2]; });
				if (type == "numeric", { value = row[2].replace(",", ".").asFloat; });
				if (type == "boolean", { if (row[2] == "true", { value = true; }, { value = false; }) });
				if (type == "array", {
					value = Array();
					(row.size - 2).do({
						arg index;
						if (row[index + 2] != nil, { value = value.add(row[index + 2]); });
					});
				});
				if (type == "comment", { key = nil; value = nil; });
			});
			if ((key != nil) && (value != nil), {
				result.put(key, value);
			});
		});
		^result;
	}
	
	*loadCSVRowstoDictionaryArrays {
		arg fname;
		var csv, indexmap, result, cell;
		result = nil;
		csv = FileReader.read(fname, true, true, delimiter: $;);
		if (csv.size>0, {
			result = Dictionary(csv[0].size);
			indexmap = Dictionary(csv[0].size);
			csv[0].size.do({
				arg index;
				cell = csv[0][index].replace("\"", "");
				result.put(cell, Array(csv.size));
				indexmap.put(index, cell);
			});
			(csv.size - 1).do({
				arg row_index;
				row_index = row_index + 1;
				csv[row_index].size.do({
					arg col_index;
					cell = csv[row_index][col_index].replace("\"", "");
					result[indexmap[col_index]].add(cell.replace(",", ".").asFloat);
				});
			});
		});
		^result;
	}
	
}
