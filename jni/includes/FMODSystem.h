#ifndef _FMODSystem_H_
#define _FMODSystem_H_

#include <fmod.h>
#include "FMODError.h"

class FMODSystem
{
    private:

    FMOD_SYSTEM * fmod_system;

    public:

    FMODSystem();

    ~FMODSystem();

    FMOD_SYSTEM * getSystem();

    void init();

    void update();
};

#endif
