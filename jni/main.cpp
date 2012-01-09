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
FMODDspBpm	*dsp;

jfloat Java_com_facorro_beatrace_SongPlayerActivity_cBegin(JNIEnv *env, jobject thiz, jstring jfilename)
{
	const char *filename;
	filename = env->GetStringUTFChars(jfilename, NULL);

	sys = new FMODSystem();
	sys->init();

	sound = new FMODSound(sys, filename, true);
	dsp = new FMODDspBpm(sys, sound);

	sound->play();

	return sound->getFrequency();
}

void Java_com_facorro_beatrace_SongPlayerActivity_cUpdate(JNIEnv *env, jobject thiz)
{
	sys->update();
}

void Java_com_facorro_beatrace_SongPlayerActivity_cEnd(JNIEnv *env, jobject thiz)
{
	delete sound;
	delete dsp;
	delete sys;
}

void Java_com_facorro_beatrace_SongPlayerActivity_cPause(JNIEnv *env, jobject thiz)
{
	sound->pause();
}

void Java_com_facorro_beatrace_SongPlayerActivity_cSetFrequency(JNIEnv *env, jobject thiz, jfloat freq)
{
	sound->setFrequency(freq);
}

jfloat Java_com_facorro_beatrace_SongPlayerActivity_cGetBpmSoFar(JNIEnv *env, jobject thiz)
{
	return dsp->getBpmSoFar();
}

jfloat Java_com_facorro_beatrace_SongPlayerActivity_cGetBpm(JNIEnv *env, jobject thiz)
{
	__android_log_print(ANDROID_LOG_ERROR, "fmod", "Starting Bpm Extraction...");

    int channels = sound->getChannels();
    int numBytes = sizeof(SAMPLETYPE);

    SAMPLETYPE samples[BUFF_SIZE];
    char * buffer = new char[BUFF_SIZE * numBytes];
    unsigned int bufferSizeInBytes = BUFF_SIZE * numBytes;

    BPMDetect bpm(channels, (int)sound->getFrequency());

    bool done = false;

    unsigned int read = 0;
    unsigned int readTotal = 0;
    unsigned int samplesTotal = 0;
    unsigned int enoughSamples = bpm.windowLen * 2500;


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

    	__android_log_print(ANDROID_LOG_ERROR, "fmod", "Calculating Bpm... # read: %d - Window: %d", samplesTotal, bpm.windowLen);

        readTotal += read;

        if(
        	read < BUFF_SIZE * sizeof(SAMPLETYPE)
        	|| samplesTotal > enoughSamples
        )
        {
            done = true;
        }
    }

    __android_log_print(ANDROID_LOG_ERROR, "fmod", "Total Bytes Read: (%d)", readTotal);

    sound->seekData(0);

    delete buffer;

    return bpm.getBpm();
}

#ifdef __cplusplus
}
#endif
