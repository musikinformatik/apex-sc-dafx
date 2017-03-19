//Usual Disclaimer:
/*
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
//////////////////////////////////////////////////////////////////////////////////
// Start the APEX-SC System
// Give complete path to where the folder '/data' in the distribution exists e.g:
Apex2GUI.new(Server.default, "data".resolveRelative);
Server.default.makeGui;

//////////////////////////////////////////////////////////////////////////////////
// Testing KLVocalTract synthesizer
//
// DAFx15 Paper Examples
//
// In addition see the examples for NTube from Nick Collins SLUGens examples:
// https://github.com/supercollider/sc3-plugins
//
////////////////////////////////////////////////////////////////////////////////

// Useful for analysis Etc.
Server.default.makeGui;
FreqScope.new();

(
////////////////////////////////////////////////////////////////////////////////
//
// Quarter Wave Resonator (QWR), 44 sections, 44 in file.
// Corresponds to Result in Fig 6 of paper
//
////////////////////////////////////////////////////////////////////////////////
var areassource= [ 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0 ];
var areas;
var loss, karray, delayarray;

areas= Array.fill(44,{|i| areassource[i]});

// Set for lossless, internally - controlled via boundaries only
loss=1.0;
karray= Array.fill(43,{|i| (areas[i]-areas[i+1])/(areas[i]+areas[i+1])});

// Let c = 343m/s
// Let L = 0.176m - both values from Jack Mullen's Thesis
// 0.176/343 = 0.0005131195335277
// Tested at 192kHz

delayarray= Array.fill(44,{0.0005131195335277/44.0});

{
KLVocalTract.ar(WhiteNoise.ar(0.25),loss, `karray, `delayarray,1.0)
}.play

)

(
////////////////////////////////////////////////////////////////////////////////
//
// Vowel Synthesis /a/ - Bart, 44 sections, 44 in file
// Noise used as excitation
// Corresponds to result in Fig 7 of paper (dark grey lines)
//
////////////////////////////////////////////////////////////////////////////////
var areassource= [ 0.45, 0.2, 0.26, 0.21, 0.32, 0.3, 0.33, 1.05, 1.12, 0.85, 0.63, 0.39, 0.26, 0.28, 0.23, 0.32, 0.29, 0.28, 0.4, 0.66, 1.2, 1.05, 1.62, 2.09, 2.56, 2.78, 2.86, 3.02, 3.75, 4.6, 5.09, 6.02, 6.55, 6.29, 6.27, 5.94, 5.28, 4.7, 3.87, 4.13, 4.25, 4.27, 4.69, 5.03 ];
var areas;
var loss, karray, delayarray;

areas= Array.fill(44,{|i| areassource[i]});

// Set for lossless, internally - controlled via boundaries only
loss=1.0;
karray= Array.fill(43,{|i| (areas[i]-areas[i+1])/(areas[i]+areas[i+1])});

// Let c = 343m/s
// Let L = 0.176m - both from Mullen's Thesis
// 0.176/343 = 0.0005131195335277 - Vocal Tract Delay Length
// Tested at 192kHz - to get more than 2 samples delay per tube section

delayarray= Array.fill(44,{0.0005131195335277/44.0});

{
KLVocalTract.ar(WhiteNoise.ar(0.25),loss, `karray, `delayarray,1.0)
}.play

)

(
/////////////////////////////////////////////////////////////////////////////////
//
// Vowel Synthesis /a/ - Bart, 44 sections, 44 in file
// Uses SC implementation of Glottal Source Excitation (from Fig 5 of paper)
// Corresponds to result in Fig 7 of paper (light grey lines)
//
////////////////////////////////////////////////////////////////////////////////
var areassource= [ 0.45, 0.2, 0.26, 0.21, 0.32, 0.3, 0.33, 1.05, 1.12, 0.85, 0.63, 0.39, 0.26, 0.28, 0.23, 0.32, 0.29, 0.28, 0.4, 0.66, 1.2, 1.05, 1.62, 2.09, 2.56, 2.78, 2.86, 3.02, 3.75, 4.6, 5.09, 6.02, 6.55, 6.29, 6.27, 5.94, 5.28, 4.7, 3.87, 4.13, 4.25, 4.27, 4.69, 5.03 ];
var areas;
var loss, karray, delayarray;

areas= Array.fill(44,{|i| areassource[i]});

// Set for lossless, internally - controlled via boundaries only
loss=1.0;
karray= Array.fill(43,{|i| (areas[i]-areas[i+1])/(areas[i]+areas[i+1])});

// Let c = 343m/s
// Let L = 0.176m - both from Mullen's Thesis
// 0.176/343 = 0.0005131195335277 - Vocal Tract Delay Length
// Tested at 192kHz - to get more than 2 samples delay per tube section

delayarray= Array.fill(44,{0.0005131195335277/44.0});

//{
//KLVocalTract.ar(WhiteNoise.ar(0.25),loss, `karray, `delayarray,1.0)
//}.play

{
var fo, flow, rqRes;
//fo = 100;
//rqRes = 0.1;
fo = MouseX.kr(20, 80).midicps;
rqRes = MouseY.kr(0.1, 10, 1).reciprocal;

KLVocalTract.ar(ApexSource01.ar(fo,rqRes,1.4,2),loss, `karray, `delayarray,1.0)
}.play

)




