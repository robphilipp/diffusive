Diffusing Code to the Local Diffuser
====================================

This demo runs the Diffusive Launcher with a strategy that doesn't contain
any end-points. This causes the diffusive to use a LocalDiffuser. The code
is still diffused, however, only to the local diffuser.

The include configuration directory (config) contains the configuration
files for the application-attached diffuser and its strategy.

1. change to the distribution's "jars" directory
2. run the following command from the command-line (but first put it all on one line)
   java -jar Diffusive_Launcher_0.2.0.jar --config-dir=../examples/local_stand_alone/launcher/ --execute-class=org.microtitan.tests.threaded.MultiThreadedCalc --class-path=../examples/example_0.2.0.jar