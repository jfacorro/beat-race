LOCAL_PATH := $(call my-dir)

#
# FMOD Ex Shared Library
# 
include $(CLEAR_VARS)

LOCAL_MODULE            := fmodex
LOCAL_SRC_FILES         := ../fmod/armeabi/libfmodex.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../fmod/inc $(LOCAL_PATH)/includes

include $(PREBUILT_SHARED_LIBRARY)

#
# Example Library
#
include $(CLEAR_VARS)

LOCAL_MODULE           := main
LOCAL_SRC_FILES        := main.cpp BPMDetect.cpp FIFOSampleBuffer.cpp PeakFinder.cpp
LOCAL_LDLIBS 		   := -llog
LOCAL_SHARED_LIBRARIES := fmodex

include $(BUILD_SHARED_LIBRARY)
