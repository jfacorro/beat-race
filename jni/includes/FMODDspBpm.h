#ifndef _FMODDSPBPM_H_
#define _FMODDSPBPM_H_

#include "FMODDsp.h"
#include "FMODSound.h"
#include <BPMDetect.h>

using namespace soundtouch;

FMOD_RESULT F_CALLBACK dspCallback(FMOD_DSP_STATE *dsp_state, float *inbuffer, float *outbuffer, unsigned int length, int inchannels, int outchannels);

class FMODDspBpm : public FMODDsp
{
    public:

        FMODDspBpm(FMODSystem * system, FMODSound * sound);

        ~FMODDspBpm();

        float getBpmSoFar();

        float getBpm();

    private:

        BPMDetect * bpmdetect;

        FMODSound * sound;
};

#endif // _FMODDSPBPM_H_

