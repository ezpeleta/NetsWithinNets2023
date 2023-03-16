MODEL LAUNCHING PROCEDURE :

1) Open the Powershell in the directory containing the Java script Eval.java
2) Execute "javac Eval.java"
3) Open Renew from the Powershell (path of the directory containing the Renew software adding \renew ) 
4) Open "system_net.rnw" directly in Renew
5) Simulate Step by Step (Ctrl+I) or completely (Ctrl+R)

COLOR CODE :
-Blue: System Net
-Green: Elements used to launch a simulation
-Red: Elements used to end a simulation
-Magenta: Processing of the information ==> Best solution




Some remarks : 
-At all times, you can observe the state of the robots nets by right-clicking the number contained by the Master Net place and double-clicking on one robot. 
-You can simulate by choosing the fired place. After having fired "init", you can double click on a transition si and choose what transition to fire. 
-This simulation is done for an LTL formula which implies the visit of three regions of interest ("a", "b", "c"), but requiring the visit of region "c" before visintg "a". The team of 3 robots evolves in an environment with 3 regions of interest from which two of them overlap.


Addional remarks:
-The system will be executed as many times as the initial marking of place "numSim".
-All the traces will be accumulated to the file named "log.txt".
-Besides storing in "log.txt" the solutions found, the "best" one will be in place named "bestSol". Being initially "", each time a new solution is obtained, it is compared with the current best, by means of the function "select_best" in the class "Eval". In its current version, "best" means "shorter" in terms of number of chars, but this is so just to check the feasability. The function could/should be much more sophisticated
-This example contains two different types of robots: two as the initial example, and a second one which is not allowed to visit the intersection area
-It has also been added a way of giving names to robots for an easier understanding of the solutions

