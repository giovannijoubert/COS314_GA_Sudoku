COS314 Assignment 2 - Giovanni Joubert (u18009035)

The Java program can be run via the following terminal command (input parameters as first argument and problem file as second argument):
java sudokuGA.java inputparams.txt /problems/s15a.txt

The format of the input parameter file:
The argument name is followed by its value, each on a seperate line. e.g.

populationSize 200
tournamentSize 10
mutationRate 0.7
randomCount 10
elitismCount 30
maxIterations 10000
verbose
tofile
useoptimizations

A sample input parameter file is provided.

populationSize - size of the initial population to create and maintain
tournamentSize - amount of candidates to place in each tournament
mutationRate - % of the population to mutate
randomCount - amount of entirely random candidates to generate and inject in each generation
elitismCount - amount of best-candidates to force into the next generation
maxIterations - maximum amount of generations to run
verbose - toggle verbose output (average fitness score as the generations pass by) - uncomment on the provided file to use
tofile - write the average fitness score to a file - uncomment on the provided file to use
useoptimizations - complete the Hidden and Naked singles before running the GA (improves speed) 
