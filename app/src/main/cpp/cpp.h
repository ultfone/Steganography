#ifndef CPP_H
#define CPP_H

#include <vector>
#include <string>
#include <cstdio>  // Needed for FILE*

using namespace std;

std::vector<int> toBinary(std::string msg);
std::vector<unsigned char> convertImg(const std::string& file);
std::vector<int> furtherWorking(std::vector<int>& img, std::vector<int> message);
int ppmToOther(std::vector<int> Output, FILE* pipe, int width, int height);

#endif
