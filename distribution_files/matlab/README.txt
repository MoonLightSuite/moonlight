-- Quick Info --
The Sensor Network Example script is  examples/SpatialExamples/SensorNetworkPaper/sensorNetworkExample.m
Other examples are contained in the folder examples/. 
The Virtual Machine has been set up, please run the configuration script init.m
contained in this folder before using the tool. 
Consider that the installed version of Matlab will expire on 27/02/2020.

-- Moonlight INSTALLATION --
Required software: 
- Matlab (version >=2019b) with Statistics and Machine Learning Toolbox
- Java JDK 1.8
There is no installation phase, just execute the init.m each time before using the tool.
This file configures the MoonLight environment. 

-- VIRTUAL MACHINE --
This virtual machine contains all the required software. 
If you have Matlab and you want to try MoonLight on your pc just copy/paste
this folder and execute init.m

-- Moonlight RUN--
Remember to run the init.m file to configure the MoonLight environment.

-- Folder Structure --
In this folder you have: 

examples/  It is a folder containing all the relevant examples

moonlight/ which contains the core of the tool.
    |_____ jar/ contains the Java executable of MoonLight
    |_____ matlab/ catains the Matlab executable of Moonlight which handle 
    |              the communication with the Java Core. 
    |_____ script/ is a utility folder use at runtime to load the moonlight scripts

init.m     is a script which adds the moonlight folder to the MATLAB path and set 
           an environmental variable 