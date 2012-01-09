#ifndef _FMODDSP_H_
#define _FMODDSP_H_

#include <fmod.h>
#include "FMODSystem.h"

class FMODDsp
{
    public:

        FMODDsp(FMODSystem * system, FMOD_DSP_DESCRIPTION description);
        FMODDsp(FMODSystem * system, FMOD_DSP_TYPE type);
        FMODDsp() {};

        ~FMODDsp();

        FMOD_DSP * getDsp();

        void setBypass(bool bypass);

    protected:

        FMOD_DSP * fmod_dsp;
};

#endif // _FMODDSP_H_

