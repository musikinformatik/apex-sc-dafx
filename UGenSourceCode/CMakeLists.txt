cmake_minimum_required (VERSION 2.8)

include_directories(${SC_PATH}/include/plugin_interface)
include_directories(${SC_PATH}/include/common)
include_directories(${SC_PATH}/external_libraries/libsndfile/)

set(plugin_sources
        VocalTractArea.cpp
	KLVocalTract.cpp
)

#set(plugins "")

set(CMAKE_SHARED_MODULE_PREFIX "")
if(APPLE OR WIN32)
set(CMAKE_SHARED_MODULE_SUFFIX ".scx")
endif()

#This adapted a bit from the SC3 membranes make file for multiplt source files

foreach(plugin ${plugin_sources})
	string(REPLACE .cpp "" plugin_name ${plugin} )
		add_library(${plugin_name} MODULE ${plugin}; tool.cpp)
	list(APPEND plugins ${plugin_name})
endforeach(plugin)
