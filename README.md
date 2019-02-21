# iFogSim
A Toolkit for Modeling and Simulation of Resource Management Techniques in Internet of Things, Edge and Fog Computing Environments

# iFogSim Tutorial Examples
 Access from <A href="https://github.com/Cloudslab/iFogSimTutorials">https://github.com/Cloudslab/iFogSimTutorials</A>

## IMPORTANT
Please check the `improv` branch for latest changes. Master branch has been left intact until complete testing.

## How to run iFogSim ?

* Create a Java project in Eclipse.
  * Do not create a `module-info.java`.
* Inside the project directory, initialize an empty Git repository with the following command
```
git init
```
* Add the Git repository of iFogSim as the `origin` remote.
```
git remote add origin https://github.com/Cloudslab/iFogSim
```
* Pull the contents of the repository to your machine.
```
git pull origin master
```
* Include the JARs (except the CloudSim ones) to your Eclipse project.
  * In Eclipse 2018-12, right click on the project -> Build Path -> Configure Build Path -> Classpath -> Add JARs.
  * Select `jars/guava-18.0.jar`, `jars/json-simple-1.1.1.jar` and `jars/commons-math3-3.5.jar`.
* Run the example files (e.g. VRGame.java) to get started.

# References
1. Harshit Gupta, Amir Vahid Dastjerdi , Soumya K. Ghosh, and Rajkumar Buyya, <A href="http://www.buyya.com/papers/iFogSim.pdf">iFogSim: A Toolkit for Modeling and Simulation of Resource Management Techniques in Internet of Things, Edge and Fog Computing Environments</A>, Software: Practice and Experience (SPE), Volume 47, Issue 9, Pages: 1275-1296, ISSN: 0038-0644, Wiley Press, New York, USA, September 2017.

2. Redowan Mahmud and Rajkumar Buyya, <A href="http://www.buyya.com/papers/iFogSim-Tut.pdf">Modelling and Simulation of Fog and Edge Computing Environments using iFogSim Toolkit</A>, Fog and Edge Computing: Principles and Paradigms, Wiley Press, New York, USA, 2018 (in press).

