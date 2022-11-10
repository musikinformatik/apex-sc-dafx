# Vocal Tract Modelling

> by Murphy, Shelley and Ternström - November 2015

This repository is copied from <http://www-users.york.ac.uk/~dtm3/vocaltract.html> for the ease of installation.

Note: the repository contains classes with very general names that are likely to clash with other extension libraries.

![Screenshot of GUI](screenshot.png)

## How to build

Because signing macOS binaries/libraries is a rather tedious task we can not provide them for you, therefore you need to build this by yourself.
For this you will need `cmake` and a source code copy of [SuperCollider](github.com/supercollider/supercollider) locally available.

After this create a `build` folder

```shell
mkdir build
cd build
```

and configure the build properly

```shell
cmake -DSC_PATH=<path_to_supercollider_source_code> ..
cmake -DCMAKE_BUILD_TYPE=RELEASE ..
```

and build it via

```shell
make
```

After this copy the files

source | location
--- | ---
`build/KLVocalTract.scx` | `apex/KLVocalTract.scx`
`build/VocalTractArea.scx` | `apex/VocalTractArea.scx`
`classes/` | `apex/classes`

where `apex` is a a new directory under the SuperCollider user extensions folder which can be located via `Platform.userExtensionDir.openOS` but defaults to `~/Library/Application Support/SuperCollider/Extensions`.

After this open `DAFx15.scd` and follow its content.

## A Guide to this directory

file | description
--- | ---
README.md | This file.
DAFx15.sc | A SuperCollider Script for testing and running APEX-SC and KLVocalTract including how to generate the examples used in the DAFx Paper
data | APEX data - the line to run APEX-SC in DAFx15.sc needs to point to this folder.
DAFx15_MurphyEtAl_FinalUpdated.pdf | The DAFx15 paper on which this work is based - updated from that available at http://www.ntnu.edu/web/dafx15/ to fix a few typos.
classes | The Client Side SuperCollider Classes for the two server side UGens used in this implementation. Need to go in same directory as APEX_Cient files, e.g: /Library/Application Support/SuperCollider/Extensions
classes/APEX_Client | These are the additional APEX Client side SuperCollider Classes. They need to go somewhere SuperCollider can see them when compiling and running - typically on the Mac: /Library/Application Support/SuperCollider/Extensions
src | Source code to build all three UGens. There is the required cmake file and the source code here, plus a helpful README file where I made some notes to get SuperCollider compiled from source and then these new UGens compiled.

## Acknowledgements

The authors are indebted to Björn Lindblom and Christine Ericsdotter, both at Stockholm University, and to Johan Sundberg at KTH, for generously sharing their expertise and data, painstakingly acquired for the original APEX Windows 3.1 implementation, which was written by Johan Stark. The groundwork for the SuperCollider implementation of APEX was done in 2011 in the M.Sc. thesis of author Jani. The extended version presented here was developed in 2014- 2015 by authors Murphy and Ternström. We are grateful also to Gerhard Eckel and Ludvig Elblaus for advice and assistance with SuperCollider. The work was supported in part by the Swedish Research Council (VR), contract D-2013-0378.

## License

GPL-2.0
