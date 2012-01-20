/*===============================================================================================
 Effects Example
 Copyright (c), Firelight Technologies Pty, Ltd 2011.

 This example shows how to apply some of the built in software effects to sounds.
 This example filters the global mix.  All software sounds played here would be filtered in the
 same way.
 To filter per channel, and not have other channels affected, simply replace system->addDSP with
 channel->addDSP.
 Note in this example you don't have to add and remove units each time, you could simply add them
 all at the start then use DSP::setActive to toggle them on and off.
===============================================================================================*/

#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <BPMDetect.h>
#include "FMODSystem.h"
#include "FMODSound.h"
#include "FMODDspBpm.h"
#include "fmod.h"
#include "fmod_dsp.h"
#include "fmod_errors.h"

//#define BUFF_SIZE   262144
#define BUFF_SIZE   5120

using namespace soundtouch;

#ifdef __cplusplus
extern "C" {
#endif

FMODSystem	*sys;
FMODSound	*sound;

void Java_com_facorro_beatrace_fmod_System_cInit(JNIEnv *env, jobject thiz)
{
	sys = new FMODSystem();
	sys->init();
}

void Java_com_facorro_beatrace_fmod_System_cUpdate(JNIEnv *env, jobject thiz)
{
	sys->update();
}

void Java_com_facorro_beatrace_fmod_System_cStop(JNIEnv *env, jobject thiz)
{
	delete sys;
}

/**
 * Sound wrapper methods
 */
jfloat Java_com_facorro_beatrace_fmod_Sound_cOpen(JNIEnv *env, jobject thiz, jstring jfilename)
{
	const char *filename;
	filename = env->GetStringUTFChars(jfilename, NULL);

	sound = new FMODSound(sys, filename, true);

	return sound->getFrequency();
}

jfloat Java_com_facorro_beatrace_fmod_Sound_cClose(JNIEnv *env, jobject thiz, jstring jfilename)
{
	delete sound;
}

void Java_com_facorro_beatrace_fmod_Sound_cPlay(JNIEnv *env, jobject thiz)
{
	sound->play();
}

void Java_com_facorro_beatrace_fmod_Sound_cPause(JNIEnv *env, jobject thiz)
{
	sound->pause();
}

jfloat Java_com_facorro_beatrace_fmod_Sound_cGetFrequency(JNIEnv *env, jobject thiz)
{
	sound->getFrequency();
}

void Java_com_facorro_beatrace_fmod_Sound_cSetFrequency(JNIEnv *env, jobject thiz, jfloat freq)
{
	sound->setFrequency(freq);
}

void Java_com_facorro_beatrace_fmod_Sound_cGetSize(JNIEnv *env, jobject thiz)
{
	sound->getSize();
}

void Java_com_facorro_beatrace_fmod_Sound_cGetRead(JNIEnv *env, jobject thiz)
{
	sound->getRead();
}

void Java_com_facorro_beatrace_fmod_Sound_cGetLength(JNIEnv *env, jobject thiz)
{
	sound->getLength();
}

jint Java_com_facorro_beatrace_fmod_Sound_cGetLengthInMilis(JNIEnv *env, jobject thiz)
{
	return sound->getLengthInMilis();
}

jint Java_com_facorro_beatrace_fmod_Sound_cGetPosition(JNIEnv *env, jobject thiz)
{
	return sound->getPosition();
}

unsigned int enoughSamples = 0;
unsigned int samplesTotal = 0;

jfloat Java_com_facorro_beatrace_fmod_Sound_cGetBpm(JNIEnv *env, jobject thiz)
{
    int channels = sound->getChannels();
    int numBytes = sizeof(SAMPLETYPE);

    SAMPLETYPE samples[BUFF_SIZE];
    char * buffer = new char[BUFF_SIZE * numBytes];
    unsigned int bufferSizeInBytes = BUFF_SIZE * numBytes;

    BPMDetect bpm(channels, (int)sound->getFrequency());

    bool done = false;

    unsigned int read = 0;
    unsigned int readTotal = 0;
    samplesTotal = 0;
    enoughSamples = bpm.windowLen * 2500;

    while(!done)
    {
        read = sound->readData(buffer, bufferSizeInBytes);

        for(unsigned int i = 0; i < bufferSizeInBytes; i+= numBytes)
        {
			unsigned int index = i / numBytes;
			SAMPLETYPE sample = ((buffer[i + 3] & 0xFF) << 24) | ((buffer[i + 2] & 0xFF) << 16) | ((buffer[i + 1] & 0xFF) << 8) | (buffer[i] & 0xFF);
			samples[index] = sample;
        }

        unsigned int numsamples = read / sizeof(SAMPLETYPE);
        samplesTotal += numsamples;

        bpm.inputSamples(samples, numsamples / channels);

        readTotal += read;

        if(
        	read < BUFF_SIZE * sizeof(SAMPLETYPE)
        	|| samplesTotal > enoughSamples
        )
        {
            done = true;
        }
    }

    sound->seekData(0);

    delete buffer;

    return bpm.getBpm();
}

jint Java_com_facorro_beatrace_fmod_Sound_cGetEnoughSamples(JNIEnv *env, jobject thiz)
{
	return enoughSamples;
}

jint Java_com_facorro_beatrace_fmod_Sound_cGetProcessedSamples(JNIEnv *env, jobject thiz)
{
	return samplesTotal;
}

#ifdef __cplusplus
}
#endif
