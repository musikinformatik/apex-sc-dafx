// PluginKLVocalTract.hpp
// Alex McLean (alex@slab.org)

#pragma once

#include "SC_PlugIn.hpp"

namespace KLVocalTract {

class KLVocalTract : public SCUnit {
public:
    KLVocalTract();

    // Destructor
    // ~KLVocalTract();

private:
    // Calc function
    void next(int nSamples);

    // Member variables
};

} // namespace KLVocalTract
