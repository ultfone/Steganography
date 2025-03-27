#include <jni.h>
#include <string>
#include "stb_image.h"
#include "stb_image_write.h"

using namespace std;

// Convert jstring to std::string
string jstringToString(JNIEnv *env, jstring jStr) {
    if (!jStr) return "";
    const char *chars = env->GetStringUTFChars(jStr, nullptr);
    string str(chars);
    env->ReleaseStringUTFChars(jStr, chars);
    return str;
}

// Hide message in image
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_steganography_MainActivity_encryptMessage(JNIEnv *env, jobject thiz, jstring message) {
    string msg = jstringToString(env, message);
    msg += '\0'; // Add null terminator

    int width, height, channels;
    unsigned char *img = stbi_load("/storage/emulated/0/Download/input.png", &width, &height, &channels, 0);
    if (!img) return env->NewStringUTF("Image not found!");

    int msgIndex = 0, msgSize = msg.size() * 8;
    for (int i = 0; i < width * height * channels; i++) {
        if (msgIndex < msgSize) {
            img[i] &= 0xFE; // Clear LSB
            img[i] |= (msg[msgIndex / 8] >> (7 - (msgIndex % 8))) & 1; // Embed bit
            msgIndex++;
        }
    }

    stbi_write_png("/storage/emulated/0/Download/output.png", width, height, channels, img, width * channels);
    stbi_image_free(img);

    return env->NewStringUTF("Message embedded!");
}

// Extract message from image
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_steganography_MainActivity_decryptMessage(JNIEnv *env, jobject thiz) {
    int width, height, channels;
    unsigned char *img = stbi_load("/storage/emulated/0/Download/output.png", &width, &height, &channels, 0);
    if (!img) return env->NewStringUTF("Image not found!");

    string extractedMsg;
    char currentChar = 0;
    int bitIndex = 0;

    for (int i = 0; i < width * height * channels; i++) {
        currentChar |= (img[i] & 1) << (7 - bitIndex);
        bitIndex++;

        if (bitIndex == 8) {
            if (currentChar == '\0') break; // Stop at null terminator
            extractedMsg += currentChar;
            currentChar = 0;
            bitIndex = 0;
        }
    }

    stbi_image_free(img);
    return env->NewStringUTF(extractedMsg.c_str());
}
