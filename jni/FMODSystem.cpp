#ifndef _FMODSystem_CPP_
#define _FMODSystem_CPP_

#include "FMODSystem.h"

FMODSystem::FMODSystem()
{
    FMODError::eval(FMOD_System_Create(&this->fmod_system));
}

FMODSystem::~FMODSystem()
{
    FMODError::eval(FMOD_System_Release(this->fmod_system));
}

FMOD_SYSTEM * FMODSystem::getSystem()
{
    return this->fmod_system;
}

void FMODSystem::init()
{
    FMODError::eval(FMOD_System_Init(this->fmod_system, 32, FMOD_INIT_NORMAL, 0));
}

void FMODSystem::update()
{
    FMODError::eval(FMOD_System_Update(this->fmod_system));
}

#endif
