cmake_minimum_required(VERSION 3.16)
project(dijkstra LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(dijkstra_lib STATIC
    src/dijkstra.cpp
)
target_include_directories(dijkstra_lib PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

add_executable(dijkstra_demo
    src/main.cpp
)
target_link_libraries(dijkstra_demo PRIVATE dijkstra_lib)

include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/v1.14.0.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

enable_testing()
add_executable(dijkstra_tests
    tests/test_dijkstra.cpp
)
target_link_libraries(dijkstra_tests PRIVATE
    dijkstra_lib
    GTest::gtest_main
)
include(GoogleTest)
gtest_discover_tests(dijkstra_tests)
