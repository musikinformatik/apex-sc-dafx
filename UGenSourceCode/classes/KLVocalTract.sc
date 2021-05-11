//SLUGens released under the GNU GPL as extensions for SuperCollider 3, by Nick Collins http://composerprogrammer.com/index.html

KLVocalTract : UGen
{
	*ar { arg input=0, lossarray=1.0, karray, delaylengtharray, mul = 1.0, add = 0.0;
	var allargs;
	var lossarrayfix;

	lossarrayfix= if(lossarray.value.isKindOf(Collection),{lossarray.value},{lossarray.value.dup(delaylengtharray.value.size+1)});

	allargs= ['audio', input]++(lossarrayfix)++(karray.value)++(delaylengtharray.value);

	//[allargs.size, allargs].postln;
	^this.multiNew(*allargs).madd(mul, add);
	}
}



