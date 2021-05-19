FormFreq : UGen {
	*kr {
		arg skip, area_bufnum, xcor_bufnum, form_bufnum, bw_bufnum, cc, internendcorr, openatleft, male, array_size;
		^this.multiNew('control', skip, area_bufnum, xcor_bufnum, form_bufnum, bw_bufnum, cc, internendcorr, openatleft, male, array_size)
	}
}

