#ifndef _FMODError_H_
#define _FMODError_H_

#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>
#include <fmod_errors.h>
#include "Exception.h"

class FMODError
{
    public:

    void static eval(FMOD_RESULT result)
    {
        if (result != FMOD_OK)
        {
            //printf("FMOD error! (%d) %s\n", result, FMOD_ErrorString(result));
            //exit(-1);
            const char * message = FMOD_ErrorString(result);
            __android_log_print(ANDROID_LOG_ERROR, "fmod", message);
        }
    }
};

#endif
