cmake_minimum_required(VERSION 3.22.1)
project("steganography")

add_library(steganography SHARED native-lib.cpp)

target_include_directories(steganography PRIVATE ${CMAKE_SOURCE_DIR}/stb)
target_link_libraries(steganography log)
