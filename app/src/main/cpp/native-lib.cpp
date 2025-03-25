#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_steganography_MainActivity_encryptMessage(JNIEnv *env, jobject, jstring input) {
    const char *inStr = env->GetStringUTFChars(input, 0);
    std::string encrypted(inStr);

    // Simple encryption: shift each character by 3
    for (char &c : encrypted) {
        c += 3;
    }

    env->ReleaseStringUTFChars(input, inStr);
    return env->NewStringUTF(encrypted.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_steganography_MainActivity_decryptMessage(JNIEnv *env, jobject, jstring input) {
    const char *inStr = env->GetStringUTFChars(input, 0);
    std::string decrypted(inStr);

    // Simple decryption: shift each character back by 3
    for (char &c : decrypted) {
        c -= 3;
    }

    env->ReleaseStringUTFChars(input, inStr);
    return env->NewStringUTF(decrypted.c_str());
}
