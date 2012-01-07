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
#include "fmod.h"
#include "fmod_dsp.h"
#include "fmod_errors.h"

//#define BUFF_SIZE   262144
#define BUFF_SIZE   5120

using namespace soundtouch;

#ifdef __cplusplus
extern "C" {
#endif

FMOD_SYSTEM  *gSystem        = 0;
FMOD_SOUND	 *gSound         = 0;
FMOD_CHANNEL *gChannel       = 0;
float		 currentFreq	 = 0;

void setFrequency(float freq);


void CHECK_RESULT(FMOD_RESULT result)
{
	if (result != FMOD_OK)
	{
		__android_log_print(ANDROID_LOG_ERROR, "fmod", "FMOD error! (%d) %s", result, FMOD_ErrorString(result));
		exit(-1);
	}
}

void Java_com_facorro_beatrace_SongPlayerActivity_cBegin(JNIEnv *env, jobject thiz, jstring jfilename)
{
	FMOD_RESULT result = FMOD_OK;

	result = FMOD_System_Create(&gSystem);
	CHECK_RESULT(result);

	result = FMOD_System_Init(gSystem, 32, FMOD_INIT_NORMAL, 0);
	CHECK_RESULT(result);
	
	const char *filename;
	filename = env->GetStringUTFChars(jfilename, NULL);
	
	FMOD_MODE mode = FMOD_HARDWARE | FMOD_CREATESTREAM | FMOD_OPENONLY | FMOD_2D;
	result = FMOD_System_CreateSound(gSystem, filename, mode, 0, &gSound);
	CHECK_RESULT(result);

	result = FMOD_System_PlaySound(gSystem, FMOD_CHANNEL_FREE, gSound, 1, &gChannel);
	CHECK_RESULT(result);
	
	FMOD_Channel_GetFrequency(gChannel, &currentFreq);
}

void Java_com_facorro_beatrace_SongPlayerActivity_cUpdate(JNIEnv *env, jobject thiz)
{
	FMOD_RESULT	result = FMOD_OK;

	result = FMOD_System_Update(gSystem);
	CHECK_RESULT(result);
}

void Java_com_facorro_beatrace_SongPlayerActivity_cEnd(JNIEnv *env, jobject thiz)
{
	FMOD_RESULT result = FMOD_OK;

	result = FMOD_Sound_Release(gSound);
	CHECK_RESULT(result);

	result = FMOD_System_Release(gSystem);
	CHECK_RESULT(result);
}

void Java_com_facorro_beatrace_SongPlayerActivity_cPause(JNIEnv *env, jobject thiz)
{
	FMOD_RESULT result = FMOD_OK;
	FMOD_BOOL paused = 0;

	result = FMOD_Channel_GetPaused(gChannel, &paused);
	CHECK_RESULT(result);

	result = FMOD_Channel_SetPaused(gChannel, !paused);
	CHECK_RESULT(result);
}

jboolean Java_com_facorro_beatrace_SongPlayerActivity_cGetPaused(JNIEnv *env, jobject thiz)
{
	FMOD_RESULT result = FMOD_OK;
	FMOD_BOOL paused = 0;

	result = FMOD_Channel_GetPaused(gChannel, &paused);
	CHECK_RESULT(result);

	return paused;
}

void Java_com_facorro_beatrace_SongPlayerActivity_cSlower(JNIEnv *env, jobject thiz)
{
	currentFreq -= 100;
	setFrequency(currentFreq);
}

void Java_com_facorro_beatrace_SongPlayerActivity_cFaster(JNIEnv *env, jobject thiz)
{
	currentFreq += 100;
	setFrequency(currentFreq);
}

void setFrequency(float freq)
{
	FMOD_RESULT result = FMOD_OK;
	
	result = FMOD_Channel_SetFrequency(gChannel, currentFreq);
	
	CHECK_RESULT(result);
}

float getFrequency()
{
	FMOD_RESULT result = FMOD_OK;

	float freq = 0;

	result = FMOD_Channel_GetFrequency(gChannel, &freq);

	CHECK_RESULT(result);

	return freq;
}

int getChannels()
{
	FMOD_RESULT result = FMOD_OK;

	int channels = 0;
	result = FMOD_Sound_GetFormat(gSound, 0, 0, &channels, 0);
	CHECK_RESULT(result);

	return channels;
}

unsigned int readData(void * buffer, unsigned int length)
{
    unsigned int read = 0;

    FMOD_RESULT result = FMOD_Sound_ReadData(gSound, buffer, length, &read);

    if(result != FMOD_ERR_FILE_EOF)
    {
    	CHECK_RESULT(result);
    }

    return read;
}

void seekData(unsigned int position)
{
    FMOD_Sound_SeekData(gSound, position);
}

jfloat Java_com_facorro_beatrace_SongPlayerActivity_cGetBpm(JNIEnv *env, jobject thiz)
{
	__android_log_print(ANDROID_LOG_ERROR, "fmod", "Starting Bpm Extraction...");

    int channels = getChannels();
    int numBytes = sizeof(SAMPLETYPE);

    SAMPLETYPE samples[BUFF_SIZE];
    char * buffer = new char[BUFF_SIZE * numBytes];
    unsigned int bufferSizeInBytes = BUFF_SIZE * numBytes;

    BPMDetect bpm(channels, (int)getFrequency());

    bool done = false;

    unsigned int read = 0;
    unsigned int readTotal = 0;

    while(!done)
    {
        read = readData(buffer, bufferSizeInBytes);

    	__android_log_print(ANDROID_LOG_ERROR, "fmod", "Calculating Bpm...(%d)", read);

        for(unsigned int i = 0; i < bufferSizeInBytes; i+= numBytes)
        {
			unsigned int index = i / numBytes;
			SAMPLETYPE sample = ((buffer[i + 3] & 0xFF) << 24) | ((buffer[i + 2] & 0xFF) << 16) | ((buffer[i + 1] & 0xFF) << 8) | (buffer[i] & 0xFF);
			samples[index] = sample;
        }

        if(read < BUFF_SIZE * sizeof(SAMPLETYPE))
        {
            done = true;
        }

        unsigned int numsamples = read / sizeof(SAMPLETYPE);

        bpm.inputSamples(samples, numsamples / channels);

        readTotal += read;
    }

    __android_log_print(ANDROID_LOG_ERROR, "fmod", "Total Bytes Read: (%d)", readTotal);

    seekData(0);

    delete buffer;

    return bpm.getBpm();
}

#ifdef __cplusplus
}
#endif
