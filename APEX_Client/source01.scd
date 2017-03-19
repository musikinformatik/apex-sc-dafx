

(
{
	var fo, flow, rqRes, gain;

	fo = MouseX.kr(20, 80).midicps;
	rqRes = MouseY.kr(0.1, 10, 1).reciprocal;
	gain = 1;

	flow = ApexSource01.ar(fo, rqRes, mul: gain);

	Out.ar(0, flow);
}.play;
)


STD_main.new();

