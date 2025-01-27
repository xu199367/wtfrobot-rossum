cmake_minimum_required(VERSION 2.6)
include($ENV{ROS_ROOT}/core/rosbuild/rosbuild.cmake)
project(tutorialROSOpenCV)

# Set the build type.  Options are:
#  Coverage       : w/ debug symbols, w/o optimization, w/ code-coverage
#  Debug          : w/ debug symbols, w/o optimization
#  Release        : w/o debug symbols, w/ optimization
#  RelWithDebInfo : w/ debug symbols, w/ optimization
#  MinSizeRel     : w/o debug symbols, w/ optimization, stripped binaries
#set(ROS_BUILD_TYPE RelWithDebInfo)

rosbuild_init()

#set the default path for built executables to the "bin" directory
set(BUILD_SHARED_LIBS true)
set(CMAKE_BUILD_TYPE DEBUG)
set(EXECUTABLE_OUTPUT_PATH ${PROJECT_BINARY_DIR}/bin)
#set the default path for built libraries to the "lib" directory
set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/lib)

# OpenCV
find_package(OpenCV 2.4 REQUIRED)

# Boost
find_package(Boost COMPONENTS filesystem program_options system)

# Sources and headers
file(GLOB srcs src/*.cpp)
include_directories("${PROJECT_SOURCE_DIR}/include")
include_directories("${PROJECT_BINARY_DIR}/include")

# Configure headers
configure_file(
    "${PROJECT_SOURCE_DIR}/include/tpofinder/configure.h.in"
    "${PROJECT_BINARY_DIR}/include/tpofinder/configure.h")

# Library construction
rosbuild_add_library(${PROJECT_NAME} ${srcs})
target_link_libraries(${PROJECT_NAME} ${OpenCV_LIBS})
target_link_libraries(${PROJECT_NAME} ${Boost_LIBRARIES})

# Executables
rosbuild_add_executable(model_homography apps/model_homography.cpp)
rosbuild_add_executable(sequence_homography apps/sequence_homography.cpp)
rosbuild_add_executable(invert_homography apps/invert_homography.cpp)
rosbuild_add_executable(tpofind apps/tpofind.cpp)
rosbuild_add_executable(objrec apps/objrec.cpp)
target_link_libraries(objrec ${PROJECT_NAME})
target_link_libraries(model_homography ${PROJECT_NAME})
target_link_libraries(sequence_homography ${PROJECT_NAME})
target_link_libraries(invert_homography ${PROJECT_NAME})
target_link_libraries(tpofind ${PROJECT_NAME})

# Data
file(COPY "${PROJECT_SOURCE_DIR}/data" DESTINATION "${PROJECT_BINARY_DIR}")


#uncomment if you have defined messages
#rosbuild_genmsg()
#uncomment if you have defined services
#rosbuild_gensrv()

#common commands for building c++ executables and libraries
#rosbuild_add_library(${PROJECT_NAME} src/example.cpp)
#target_link_libraries(${PROJECT_NAME} another_library)
#rosbuild_add_boost_directories()
#rosbuild_link_boost(${PROJECT_NAME} thread)
#rosbuild_add_executable(example examples/example.cpp)
#target_link_libraries(example ${PROJECT_NAME})
