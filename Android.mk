LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_EMMA_COVERAGE_FILTER := +com.android.calendar.*

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)

# unbundled
LOCAL_STATIC_JAVA_LIBRARIES := \
        android-common \
        libchips \
        colorpicker \
        android-opt-datetimepicker \
        android-opt-timezonepicker \
        android-support-v4 \
        android-support-v7-appcompat \
        android-support-v13 \
        calendar-common

LOCAL_SDK_VERSION := current

LOCAL_RESOURCE_DIR += \
    $(TOP)/frameworks/opt/chips/res \
    $(TOP)/frameworks/opt/colorpicker/res \
    $(TOP)/frameworks/opt/datetimepicker/res \
    $(TOP)/frameworks/opt/timezonepicker/res \
	$(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := Calendar

LOCAL_PROGUARD_FLAG_FILES := proguard.flags \
                             ../../../frameworks/opt/datetimepicker/proguard.flags

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.ex.chips \
    --extra-packages com.android.colorpicker \
    --extra-packages com.android.datetimepicker \
    --extra-packages com.android.timezonepicker \
    --extra-packages android.support.v7.appcompat

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
