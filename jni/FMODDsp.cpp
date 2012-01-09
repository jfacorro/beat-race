#ifndef _FMODDSP_CPP_
#define _FMODDSP_CPP_

#include "FMODDsp.h"
#include "FMODError.h"

FMODDsp::FMODDsp(FMODSystem * system, FMOD_DSP_DESCRIPTION description)
{
    FMODError::eval(FMOD_System_CreateDSP(system->getSystem(), &description, &this->fmod_dsp));
}

FMODDsp::FMODDsp(FMODSystem * system, FMOD_DSP_TYPE type)
{
    FMODError::eval(FMOD_System_CreateDSPByType(system->getSystem(), type, &this->fmod_dsp));
}

FMODDsp::~FMODDsp()
{
    FMODError::eval(FMOD_DSP_Release(this->fmod_dsp));
}

FMOD_DSP * FMODDsp::getDsp()
{
    return this->fmod_dsp;
}

void FMODDsp::setBypass(bool bypass)
{
    FMODError::eval(FMOD_DSP_SetBypass(this->fmod_dsp, bypass));
}

#endif // _FMODDSP_CPP_

