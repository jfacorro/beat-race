#ifndef _FMODSound_CPP_
#define _FMODSound_CPP_

#include "FMODSound.h"
#include "FMODError.h"

FMODSound::FMODSound(FMODSystem * sys, const char * filename, bool stream)
{
    FMOD_MODE mode = stream ? FMOD_CREATESTREAM : FMOD_CREATESAMPLE;
    mode = mode | FMOD_SOFTWARE | FMOD_OPENONLY;

    this->fmod_system = sys->getSystem();

    FMODError::eval(FMOD_System_CreateSound(this->fmod_system, filename, mode, 0, &this->fmod_sound));

    FMODError::eval(FMOD_System_PlaySound(this->fmod_system, FMOD_CHANNEL_FREE, this->fmod_sound, true, &this->fmod_channel));
}

FMODSound::~FMODSound()
{
    FMODError::eval(FMOD_Sound_Release(this->fmod_sound));
}

void FMODSound::play()
{
    FMODError::eval(FMOD_Channel_SetPaused(this->fmod_channel, false));
}

void FMODSound::pause()
{
    FMODError::eval(FMOD_Channel_SetPaused(this->fmod_channel, true));
}

void FMODSound::setFrequency(float freq)
{
    FMODError::eval(FMOD_Channel_SetFrequency(this->fmod_channel, freq));
}

float FMODSound::getFrequency()
{
    float freq = 0;

    FMODError::eval(FMOD_Channel_GetFrequency(this->fmod_channel, &freq));

    return freq;
}

unsigned int FMODSound::getLength()
{
    unsigned int length = 0;

    FMODError::eval(FMOD_Sound_GetLength(this->fmod_sound, &length, FMOD_TIMEUNIT_PCMBYTES));

    return length;
}

FMOD_MODE FMODSound::getMode()
{
    FMOD_MODE mode;

    FMODError::eval(FMOD_Sound_GetMode(this->fmod_sound, &mode));

    return mode;
}

int FMODSound::getChannels()
{
    int channels = 0;
    FMODError::eval(FMOD_Sound_GetFormat(this->fmod_sound, 0, 0, &channels, 0));
    return channels;
}

int FMODSound::getBitDepth()
{
    int bits = 0;
    FMODError::eval(FMOD_Sound_GetFormat(this->fmod_sound, 0, 0, 0, &bits));
    return bits;
}

void FMODSound::addDsp(FMODDsp * dsp)
{
    FMODError::eval(FMOD_Channel_AddDSP(this->fmod_channel, dsp->getDsp(), 0));
}

unsigned int FMODSound::readData(void * buffer, unsigned int length)
{
    unsigned int read = 0;

    FMOD_RESULT result = FMOD_Sound_ReadData(this->fmod_sound, buffer, length, &read);

    if(result != FMOD_OK && result != FMOD_ERR_FILE_EOF)
    {
        FMODError::eval(result);
    }

    return read;
}

void FMODSound::seekData(unsigned int position)
{
    FMOD_Sound_SeekData(this->fmod_sound, position);
}

#endif
