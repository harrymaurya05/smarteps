cmake_minimum_required(VERSION 3.22.1)

project("epsviewer_ghostscript")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add compiler flags
add_compile_options(-Wall -Wextra -Werror=return-type)

# Find log library
find_library(log-lib log)

# Create the JNI bridge library
add_library(
    gs
    SHARED
    ghostscript_jni.cpp
)

# Link libraries
target_link_libraries(
    gs
    ${log-lib}
)

# When Ghostscript is available, uncomment to link:
# find_library(ghostscript-lib gs PATHS ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
# target_link_libraries(gs ${ghostscript-lib})

# Include directories (uncomment when Ghostscript headers are available)
# target_include_directories(gs PRIVATE ${CMAKE_SOURCE_DIR}/include)

