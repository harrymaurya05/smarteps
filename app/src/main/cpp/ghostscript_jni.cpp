/**
 * Ghostscript JNI Bridge for Android
 *
 * This native library provides a bridge between Java/Kotlin and Ghostscript C API.
 * Ghostscript is used to render EPS files to raster formats and convert to PDF.
 *
 * Build Requirements:
 * - Ghostscript library (libgs.so) for Android
 * - NDK r21 or later
 *
 * License: AGPL v3 (as required by Ghostscript)
 * See: https://www.ghostscript.com/
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <cstring>

// Ghostscript API headers (uncomment when libgs is available)
// #include "ghostscript/iapi.h"
// #include "ghostscript/ierrors.h"

#define LOG_TAG "GhostscriptJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Ghostscript instance (when library is linked)
// static void *gs_instance = nullptr;

/**
 * Get Ghostscript version
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_epsviewer_util_GhostscriptWrapper_nativeGetVersion(
    JNIEnv* env,
    jobject /* this */) {

    LOGD("Getting Ghostscript version");

    // When Ghostscript is linked, use actual API:
    /*
    gsapi_revision_t revision;
    if (gsapi_revision(&revision, sizeof(revision)) == 0) {
        std::string version = std::to_string(revision.revision);
        return env->NewStringUTF(version.c_str());
    }
    */

    // Placeholder: Return mock version
    LOGW("Ghostscript library not linked - returning mock version");
    return env->NewStringUTF("10.02.1-mock");
}

/**
 * Helper: Execute Ghostscript with arguments
 */
static bool executeGhostscript(const std::vector<std::string>& args) {
    // When Ghostscript is linked:
    /*
    void *instance = nullptr;
    int code = gsapi_new_instance(&instance, nullptr);
    if (code < 0) {
        LOGE("Failed to create Ghostscript instance: %d", code);
        return false;
    }

    // Convert args to char* array
    std::vector<char*> argv;
    argv.push_back((char*)"gs"); // Program name
    for (const auto& arg : args) {
        argv.push_back((char*)arg.c_str());
    }

    code = gsapi_init_with_args(instance, argv.size(), argv.data());

    gsapi_exit(instance);
    gsapi_delete_instance(instance);

    return code == 0 || code == gs_error_Quit;
    */

    // Placeholder: Log and return false
    LOGW("Ghostscript not linked - cannot execute");
    for (const auto& arg : args) {
        LOGD("  arg: %s", arg.c_str());
    }
    return false;
}

/**
 * Render EPS to PNG
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_epsviewer_util_GhostscriptWrapper_nativeRenderToPng(
    JNIEnv* env,
    jobject /* this */,
    jstring inputPath,
    jstring outputPath,
    jint dpi) {

    const char* input = env->GetStringUTFChars(inputPath, nullptr);
    const char* output = env->GetStringUTFChars(outputPath, nullptr);

    LOGI("Rendering EPS to PNG: %s -> %s at %ddpi", input, output, dpi);

    std::vector<std::string> args = {
        "-dNOPAUSE",
        "-dBATCH",
        "-dSAFER",
        "-sDEVICE=png16m",
        "-r" + std::to_string(dpi),
        "-o" + std::string(output),
        std::string(input)
    };

    bool result = executeGhostscript(args);

    env->ReleaseStringUTFChars(inputPath, input);
    env->ReleaseStringUTFChars(outputPath, output);

    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Render EPS to JPEG
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_epsviewer_util_GhostscriptWrapper_nativeRenderToJpeg(
    JNIEnv* env,
    jobject /* this */,
    jstring inputPath,
    jstring outputPath,
    jint dpi,
    jint quality) {

    const char* input = env->GetStringUTFChars(inputPath, nullptr);
    const char* output = env->GetStringUTFChars(outputPath, nullptr);

    LOGI("Rendering EPS to JPEG: %s -> %s at %ddpi, quality=%d", input, output, dpi, quality);

    std::vector<std::string> args = {
        "-dNOPAUSE",
        "-dBATCH",
        "-dSAFER",
        "-sDEVICE=jpeg",
        "-dJPEGQ=" + std::to_string(quality),
        "-r" + std::to_string(dpi),
        "-o" + std::string(output),
        std::string(input)
    };

    bool result = executeGhostscript(args);

    env->ReleaseStringUTFChars(inputPath, input);
    env->ReleaseStringUTFChars(outputPath, output);

    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Convert EPS to PDF
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_epsviewer_util_GhostscriptWrapper_nativeConvertToPdf(
    JNIEnv* env,
    jobject /* this */,
    jstring inputPath,
    jstring outputPath) {

    const char* input = env->GetStringUTFChars(inputPath, nullptr);
    const char* output = env->GetStringUTFChars(outputPath, nullptr);

    LOGI("Converting EPS to PDF: %s -> %s", input, output);

    std::vector<std::string> args = {
        "-dNOPAUSE",
        "-dBATCH",
        "-dSAFER",
        "-sDEVICE=pdfwrite",
        "-dEPSCrop",
        "-o" + std::string(output),
        std::string(input)
    };

    bool result = executeGhostscript(args);

    env->ReleaseStringUTFChars(inputPath, input);
    env->ReleaseStringUTFChars(outputPath, output);

    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Execute custom Ghostscript command
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_epsviewer_util_GhostscriptWrapper_nativeExecute(
    JNIEnv* env,
    jobject /* this */,
    jobjectArray argsArray) {

    jsize argc = env->GetArrayLength(argsArray);
    std::vector<std::string> args;

    for (jsize i = 0; i < argc; i++) {
        jstring jarg = (jstring)env->GetObjectArrayElement(argsArray, i);
        const char* arg = env->GetStringUTFChars(jarg, nullptr);
        args.push_back(arg);
        env->ReleaseStringUTFChars(jarg, arg);
        env->DeleteLocalRef(jarg);
    }

    LOGI("Executing custom Ghostscript command with %d args", (int)args.size());

    bool result = executeGhostscript(args);

    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Get EPS bounding box
 */
extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_epsviewer_util_GhostscriptWrapper_nativeGetBoundingBox(
    JNIEnv* env,
    jobject /* this */,
    jstring inputPath) {

    const char* input = env->GetStringUTFChars(inputPath, nullptr);

    LOGD("Getting bounding box for: %s", input);

    // When Ghostscript is linked, use bbox device:
    /*
    std::vector<std::string> args = {
        "-dNOPAUSE",
        "-dBATCH",
        "-dSAFER",
        "-sDEVICE=bbox",
        std::string(input)
    };

    // Execute and parse stderr output for %%BoundingBox
    // Return [llx, lly, urx, ury]
    */

    env->ReleaseStringUTFChars(inputPath, input);

    // Placeholder: Return mock bounding box
    LOGW("Ghostscript not linked - cannot extract bounding box");
    jintArray result = env->NewIntArray(4);
    jint bbox[] = {0, 0, 612, 792}; // Default letter size
    env->SetIntArrayRegion(result, 0, 4, bbox);

    return result;
}

