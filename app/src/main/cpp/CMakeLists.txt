cmake_minimum_required(VERSION 3.10.2)

# Set the library name
set(LIBRARY_NAME "ffmpeg-kit")

# Set the source files
set(SOURCES
        src/main/cpp/ffmpeg-kit.c
        src/main/cpp/ffmpeg-kit.h
)

# Add the library
add_library(${LIBRARY_NAME} SHARED ${SOURCES}
        avcodec
        avformat
        avutil
        swresample
        libavfilter
        libpostproc
        # Add more libraries as needed
)

# Set the include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

# Link the library against the FFmpeg libraries
target_link_libraries(${LIBRARY_NAME}
        avcodec
        avformat
        avutil
        swscale
)

# Set the CMake build flags
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Werror")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Werror")
