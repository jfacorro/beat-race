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
#include "fmod.h"
#include "fmod_dsp.h"
#include "fmod_errors.h"

FMOD_SYSTEM  *gSystem        = 0;
FMOD_SOUND	 *gSound         = 0;
FMOD_CHANNEL *gChannel       = 0;


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
	
	const jbyte *filename;
	filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
	
	FMOD_MODE mode = FMOD_HARDWARE | FMOD_CREATESTREAM | FMOD_OPENONLY | FMOD_2D;
	result = FMOD_System_CreateSound(gSystem, filename, mode, 0, &gSound);
	CHECK_RESULT(result);

	result = FMOD_System_PlaySound(gSystem, FMOD_CHANNEL_FREE, gSound, 0, &gChannel);
	CHECK_RESULT(result);
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



