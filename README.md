# SOBA - Simple Objects for Bytecode Analysis 

This class library is a wrapper of ASM library to execute simple control-flow and data-flow analysis on Java bytecode.
Compared with WALA and Soot, this library implements relatively simple features.  Instead, the library requires a smaller number of parameters.

A Japanese article describes its technical background: https://doi.org/10.11309/jssst.33.4_4

## Build

The library uses Maven to build the system.

       mvn package


## Quick analysis

The `soba.util.debug.DumpClass` is an example main class that extracts control-flow, control dependence and data dependence from given classes.

        java -jar soba-0.2.0.jar path/to/ClassFiles

The command reports a list of instructions and their dependencies for each method in the given classes.


## Examples

The `example` directory provides example code to extract a call graph with a resolution of dynamic binding using CHA (a simple class hierarchy) and VTA (Variable Type Analysis, a kind of data-flow analysis).

