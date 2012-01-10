#ifndef _FMODSound_H_
#define _FMODSound_H_

#include "FMODSystem.h"
#include "FMODError.h"
#include "FMODDsp.h"

class FMODSound
{
    private:

    FMOD_SOUND * fmod_sound;
    FMOD_CHANNEL * fmod_channel;
    FMOD_SYSTEM * fmod_system;

    public:

    //FMODSound(FMODSystem * sys, const char * filename);

    FMODSound(FMODSystem * sys, const char * filename, bool stream = true);

    ~FMODSound();

    void play();

    void pause();

    void setFrequency(float freq);

    float getFrequency();

    unsigned int getLength();

    FMOD_MODE getMode();

    int getChannels();

    int getBitDepth();

    void addDsp(FMODDsp * dsp);

    unsigned int readData(void * buffer, unsigned int length);

    void seekData(unsigned int position);

};

#endif