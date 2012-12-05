Diffusing to a RESTful Diffuser Server Running Locally
======================================================

In this example, although we are running all processes on one computer, we are setting
diffusive up to diffuse code to a server running in a separate process. In the local
stand-alone example, all the code was run in a single process, and this occurred because
in the random_diffuser_strategy.xml configuration file, no end-points were specified.
If no end-points are specified, then the code will be run in the local diffuser. In
this example, we will add the local host's IP address as an end-point to the strategy. 
This will cause diffusive to diffuse the code to a RESTful diffuser server running at 
that end-point. And so, we will also need to run a RESTful diffuser server.


Broadly speaking there are two main tasks:
1. Start the diffuser server
2. Run the diffusive launcher specifying the code you would like to diffuse

Starting the Diffusive Server
-----------------------------
1. change to the distribution's "jars" directory
2. run the following command from the command-line (but first put it all on one line)

   java -jar Diffusive_Server_0.2.0.jar --config-dir=../examples/local/diffuser_server/ --class-path=../examples/example_0.2.0.jar


Run the Diffusive Launcher
--------------------------
1. edit the class path in the examples/local/launcher/random_diffuser_strategy.xml file
    <RandomDiffuserStrategyConfigXml>
        <clientEndpoints>
===>        <endPoint>http://your.ip.address:8182/diffusers</endPoint>
        </clientEndpoints>
        <randomSeed>3141592653</randomSeed>
    </RandomDiffuserStrategyConfigXml>
	
	Replace the "your.ip.address" with your actual IP address (not "localhost")

2. change to the distribution's "jars" directory
3. run the following command from the command-line (but first put it all on one line)

   java -jar Diffusive_Launcher_0.2.0.jar --config-dir=../examples/local/launcher/ --execute-class=org.microtitan.tests.threaded.MultiThreadedCalc --class-path=../examples/example_0.2.0.jar
