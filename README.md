Made using resources from https://github.com/dyn4j/dyn4j-samples.

## Contents:

src - java source code
results - temp folder which stores results of training that haven't yet been processed
parameterOptimization - folder containing results for each parameter along with "theBest" agents and data.xlsx with all measurements and graphs

## Requirements:

1. Java 21
2. Maven

## How to run:

(all locations are rooted in package src.main.java.org.dyn4j.samples)

1. Install everything required by pom.xml or have an IDE do it for you. (mvn install)
2. Thrust is the entry point to run the simulation. In Thrust::main you can change which agent you will run, or choose null if you want the player to play.
3. To run the training select the settings in file resources/settings.properties and then run demos.thrustTraining.ThrustCarouselSelection::main with a single argument of path to settings file.
   Training results go to the "results" folder with a timestamp of when the traning started.

## Other

In package demos you can see other training examples for different simulations, and run each one without settings.

BasketBall works same as Thrust if you want to play it as a player or as agent.
