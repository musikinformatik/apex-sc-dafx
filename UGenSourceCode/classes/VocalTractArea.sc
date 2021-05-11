VocalTractArea : UGen {
	*kr {
		arg skip, pc1, pc2, pc3, front_xx, front_yy, positionarray, back_xx, back_yy, alpha, beta, mri1_xx, mri1_yy, mri2_xx, mri2_yy, planes_xx, planes_yy, a_array, x_array, array_size, setup, param;
		^this.multiNew('control', skip, pc1, pc2, pc3, front_xx, front_yy, positionarray, back_xx, back_yy, alpha, beta, mri1_xx, mri1_yy, mri2_xx, mri2_yy, planes_xx, planes_yy, a_array, x_array, array_size, setup, param)
	}
}

