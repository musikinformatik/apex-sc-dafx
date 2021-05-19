// PluginKLVocalTract.cpp
// Alex McLean (alex@slab.org)

#include "SC_PlugIn.hpp"
#include "KLVocalTract.hpp"

static InterfaceTable* ft;

namespace KLVocalTract {

KLVocalTract::KLVocalTract() {
    mCalcFunc = make_calc_function<KLVocalTract, &KLVocalTract::next>();
    next(1);
}

void KLVocalTract::next(int nSamples) {
    const float* input = in(0);
    const float* gain = in(1);
    float* outbuf = out(0);

    // simple gain function
    for (int i = 0; i < nSamples; ++i) {
        outbuf[i] = input[i] * gain[i];
    }
}

} // namespace KLVocalTract

PluginLoad(KLVocalTractUGens) {
    // Plugin magic
    ft = inTable;
    registerUnit<KLVocalTract::KLVocalTract>(ft, "KLVocalTract", false);
}
