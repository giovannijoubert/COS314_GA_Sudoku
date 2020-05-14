import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class sudokuGA {
    public static int dimension = 9;

    public static void main(String[] args) {

        char[][] SudokuArray = new char[dimension][dimension];

        if (args.length != 2) {
            System.out.println(
                    "Please input parameters as follows: \n \t 1. full path to parameter file \n \t 2. full path to problem file");
            System.exit(0);
        }

        String parameterFilePath = args[0];
        String inputFilePath = args[1];

        // GA parameters (passed in by seperate param file - 1st arg of program)
        int populationSize = 0;
        int tournamentSize = 0;
        int mutationCount = 0;
        int randomCount = 0;
        int elitismCount = 0;
        int maxIterations = 0;
        boolean verbose = false;
        boolean tofile = false;
        boolean useoptimizations = false;

        // read parameter file
        try {
            File inputFile = new File(parameterFilePath);
            Scanner myReader = new Scanner(inputFile);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] fileArguments = data.split(" ", 10);

                for (int i = 0; i < fileArguments.length; i++) {
                    if (fileArguments[i].equals("populationSize"))
                        populationSize = Integer.parseInt(fileArguments[i + 1]);
                    else if (fileArguments[i].equals("tournamentSize"))
                        tournamentSize = Integer.parseInt(fileArguments[i + 1]);
                    else if (fileArguments[i].equals("mutationRate"))
                        mutationCount = (int) (populationSize * Double.parseDouble(fileArguments[i + 1]));
                    else if (fileArguments[i].equals("randomCount"))
                        randomCount = Integer.parseInt(fileArguments[i + 1]);
                    else if (fileArguments[i].equals("elitismCount"))
                        elitismCount = Integer.parseInt(fileArguments[i + 1]);
                    else if (fileArguments[i].equals("maxIterations"))
                        maxIterations = Integer.parseInt(fileArguments[i + 1]);
                    else if (fileArguments[i].equals("verbose"))
                        verbose = true;
                    else if (fileArguments[i].equals("tofile"))
                        tofile = true;
                    else if (fileArguments[i].equals("useoptimizations"))
                        useoptimizations = true;
                }
            }

            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Couldn't open parameter file.");
            e.printStackTrace();
            System.exit(0);
        }

        // basic input checking

        if (randomCount > populationSize) {
            System.out.println("Random Count cannot be larger than Population Size.");
            System.exit(0);
        }

        if (tournamentSize > populationSize) {
            System.out.println("Tournament Size cannot be larger than Population Size.");
            System.exit(0);
        }

        if (mutationCount > populationSize) {
            System.out.println("Mutation Rate shoud range from 0 - 1.");
            System.exit(0);
        }

        // read problem file
        try {
            File inputFile = new File(inputFilePath);
            Scanner myReader = new Scanner(inputFile);
            int row = 0;
            int col = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                for (int i = 0; i < data.length(); i++) {
                    if (data.charAt(i) != ' ') {
                        SudokuArray[row][col] = data.charAt(i);
                        col++;
                    }
                }
                col = 0;
                row++;
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Couldn't open problem file.");
            e.printStackTrace();
            System.exit(0);
        }

        if (tofile) {
            try {
                // clear the AVGFitness file

                FileWriter fwOb = new FileWriter("avgFitness.txt", false);
                PrintWriter pwOb = new PrintWriter(fwOb, false);
                pwOb.flush();
                pwOb.close();
                fwOb.close();
            } catch (IOException e) {
                // exception handling left as an exercise for the reader
            }
        }

        System.out.println("INPUT SUDOKU");
        prettyPrint(SudokuArray);

        // fill easy to determine
        if (useoptimizations) {
            SudokuArray = fillPredeterminedNakedSingles(SudokuArray);
            SudokuArray = fillPredeterminedHiddenSingles(SudokuArray);
        }

        // generate initial population
        Population myPop = new Population(SudokuArray, populationSize);

        // prevent local minimum
        int previousBest = 0;
        int noChange = 0;

        // GA Loop
        for (int i = 0; i < maxIterations; i++) {
            if (noChange < 30)
                myPop.nextGeneration(tournamentSize, randomCount, mutationCount, elitismCount);
            else {
                myPop.nextGeneration(tournamentSize, populationSize / 3, mutationCount, populationSize / 5);
                noChange = 0;
            }

            // prevent stagnation by introducing new chromosones

            if (previousBest == myPop.getBestFitnessScore())
                noChange++;

            previousBest = myPop.getBestFitnessScore();

            if (verbose)
                System.out.println("GEN (" + myPop.getGeneration() + ") \t AVG FIT: (" + myPop.getAvgFitnessScore()
                        + ") \t BEST FIT: (" + myPop.getBestFitnessScore() + ")");

            // print average to file
            if (tofile) {
                try (FileWriter fw = new FileWriter("avgFitness.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {
                    out.println(myPop.getAvgFitnessScore());
                } catch (IOException e) {
                    // exception handling left as an exercise for the reader
                }

            }

            // found solution before maxIterations
            if (myPop.getBestFitnessScore() == 0)
                break;
        }

        // print best solution
        System.out.println("BEST SOLUTION");
        prettyPrint(myPop.getBestFitnessChromo().getDNA());

        System.out.println("SOLUTION FITNESS: " + myPop.getBestFitnessScore() + " (0 being perfect solution) ");

        System.out.println("Number of Generations: " + myPop.getGeneration());

    }

    private static char[][] copySudokuArray(char[][] toClone) {
        char clone[][] = new char[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                clone[i][j] = toClone[i][j];
            }
        }
        return clone;
    }

    private static int getBlockStartX(int index) {
        int blockstart_x = 0;

        switch (index) {
            case 0:
                blockstart_x = 0;
                break;
            case 1:
                blockstart_x = 0;
                break;
            case 2:
                blockstart_x = 0;
                break;
            case 3:
                blockstart_x = 3;
                break;
            case 4:
                blockstart_x = 3;
                break;
            case 5:
                blockstart_x = 3;
                break;
            case 6:
                blockstart_x = 6;
                break;
            case 7:
                blockstart_x = 6;
                break;
            case 8:
                blockstart_x = 6;
                break;
        }

        return blockstart_x;

    }

    private static int getBlockStartY(int index) {
        int blockstart_y = 0;

        switch (index) {
            case 0:
                blockstart_y = 0;
                break;
            case 1:
                blockstart_y = 3;
                break;
            case 2:
                blockstart_y = 6;
                break;
            case 3:
                blockstart_y = 0;
                break;
            case 4:
                blockstart_y = 3;
                break;
            case 5:
                blockstart_y = 6;
                break;
            case 6:
                blockstart_y = 0;
                break;
            case 7:
                blockstart_y = 3;
                break;
            case 8:
                blockstart_y = 6;
                break;
        }
        return blockstart_y;
    }

    // return a char[][] BLOCK from the passed in sudokuArray
    private static char[][] sudokuBlock(char[][] sudokuArray, int index) {
        char[][] block = new char[dimension][dimension];

        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                block[x][y] = sudokuArray[getBlockStartX(index) + x][getBlockStartY(index) + y];

        return block;
    }

    // return a char[] COL from the passed in sudokuArray
    private static char[] sudokuCol(char[][] sudokuArray, int index) {
        char[] col = new char[9];
        for (int i = 0; i < dimension; i++) {
            col[i] = sudokuArray[i][index];
        }

        return col;
    }

    // return a char[] ROW from the passed in sudokuArray
    private static char[] sudokuRow(char[][] sudokuArray, int index) {
        char[] row = new char[9];
        for (int i = 0; i < dimension; i++) {
            row[i] = sudokuArray[index][i];
        }

        return row;
    }

    private static char[][] fillPredeterminedNakedSingles(char[][] sudokuArray) {
        boolean[][][] cellBools = new boolean[9][9][9];
        // false values mean they are available

        boolean improvement = false;
        cellBools = pencilPossibles(sudokuArray); // calculate pencil

        do {
            improvement = false;
            for (int row = 0; row < dimension; row++) {
                for (int col = 0; col < dimension; col++) {
                    if (sudokuArray[row][col] == '0') {
                        boolean tripwire = false;
                        for (int possibles = 0; possibles < dimension; possibles++) {
                            if (!cellBools[row][col][possibles]) {
                                for (int moreThanOne = 0; moreThanOne < dimension; moreThanOne++)
                                    if (!cellBools[row][col][moreThanOne] && moreThanOne != possibles)
                                        tripwire = true;

                                if (!tripwire) {
                                    sudokuArray[row][col] = (char) (possibles + 1 + '0');
                                    cellBools = pencilPossibles(sudokuArray); // calculate pencil
                                    improvement = true;
                                }
                            }
                        }
                    }
                }
            }
        } while (improvement);

        return (sudokuArray);
    }

    private static char[][] fillPredeterminedHiddenSingles(char[][] inputSudokuArray) {
        char[][] sudokuArray1 = copySudokuArray(inputSudokuArray);
        boolean[][][] cellBools = new boolean[9][9][9];
        cellBools = pencilPossibles(sudokuArray1);

        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) { // iterate over every cell
                if (sudokuArray1[row][col] == '0') { // work only on empty cells
                    for (int cellPencilCounter = 0; cellPencilCounter < dimension; cellPencilCounter++) { // check every
                                                                                                          // item in
                                                                                                          // [row][col][0..9]
                        boolean rowFound = false;
                        boolean colFound = false;
                        boolean sgFound = false;

                        if (cellBools[row][col][cellPencilCounter] == false) { // only check numbers that are viable in
                                                                               // current cell
                            for (int innerCounter = 0; innerCounter < dimension; innerCounter++) { // compare to
                                                                                                   // everything in
                                                                                                   // row+col
                                if (colFound && rowFound)
                                    break;
                                if (cellBools[row][innerCounter][cellPencilCounter] == false
                                        && sudokuArray1[row][innerCounter] == '0' && innerCounter != col) // check cols
                                    colFound = true;
                                if (cellBools[innerCounter][col][cellPencilCounter] == false
                                        && sudokuArray1[innerCounter][col] == '0' && innerCounter != row) // check rows
                                    rowFound = true;
                            }
                            // check subgroup
                            int sgRow = row / 3 * 3;
                            int sgCol = col / 3 * 3;
                            for (int localRow = sgRow; localRow < sgRow + 3; localRow++)
                                for (int localCol = sgCol; localCol < sgCol + 3; localCol++)
                                    if ((cellBools[localRow][localCol][cellPencilCounter] == false)
                                            && (localCol != col || localRow != row)
                                            && (sudokuArray1[localRow][localCol] == '0'))
                                        sgFound = true;

                            if (!rowFound || !colFound || !sgFound) { // write pencil as pen
                                sudokuArray1[row][col] = (char) (cellPencilCounter + 1 + '0');
                                cellBools = pencilPossibles(sudokuArray1);
                                break;
                            }

                        }

                    }
                }
            }
        }

        return sudokuArray1;
    }

    private static boolean[][][] pencilPossibles(char[][] sudokuArray) {
        boolean[][][] cellBools = new boolean[9][9][9];
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                if (sudokuArray[row][col] == '0') { // empty cell
                    for (int innerCounter = 0; innerCounter < dimension; innerCounter++) { // check every item in
                                                                                           // row+col
                        if (sudokuArray[row][innerCounter] != '0') // check col
                            cellBools[row][col][Character.getNumericValue(sudokuArray[row][innerCounter]) - 1] = true;

                        if (sudokuArray[innerCounter][col] != '0') // check row
                            cellBools[row][col][Character.getNumericValue(sudokuArray[innerCounter][col]) - 1] = true;
                    }

                    // check subgroup
                    int sgRow = row / 3 * 3;
                    int sgCol = col / 3 * 3;
                    for (int localRow = sgRow; localRow < sgRow + 3; localRow++)
                        for (int localCol = sgCol; localCol < sgCol + 3; localCol++) {
                            if (sudokuArray[localRow][localCol] != '0')
                                cellBools[row][col][Character.getNumericValue(sudokuArray[localRow][localCol])
                                        - 1] = true;
                        }
                }
            }
        }
        return cellBools;
    }

    private static void prettyPrint(char[][] sudokuArray) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (sudokuArray[i][j] == '0')
                    System.out.print("-" + " ");
                else
                    System.out.print(sudokuArray[i][j] + " ");
                if ((j + 1) % 3 == 0)
                    System.out.print(" ");
            }
            System.out.println("");
            if ((i + 1) % 3 == 0)
                System.out.println("");
        }
    }

    private static class Population {
        private List<Chromosone> population = new ArrayList<Chromosone>();
        int popSize;
        int bestFitnessScore = 999;
        int Generation = 0;
        char[][] fixedSudokuArray = new char[dimension][dimension];

        private List<Chromosone> getPopulation() {
            return population;
        }

        private int getGeneration() {
            return Generation;
        }

        private Population(char[][] _fixedSudokuArray, int _popSize) {
            popSize = _popSize;
            fixedSudokuArray = _fixedSudokuArray;

            for (int i = 0; i < popSize; i++) {
                population.add(new Chromosone(fixedSudokuArray));
            }
        }

        /**
         * 
         * @param tournamentSize
         * @param RandomCount
         * @param MutationCount
         * @param ElitismCount
         */
        private void nextGeneration(int tournamentSize, int RandomCount, int MutationCount, int ElitismCount) {
            List<Chromosone> newPopulation = new ArrayList<Chromosone>();
            Generation++;

            for (int tournamentsCounter = 0; tournamentsCounter < popSize / tournamentSize; tournamentsCounter++) { // amount
                                                                                                                    // of
                                                                                                                    // tournaments
                                                                                                                    // to
                                                                                                                    // run
                List<Integer> checked = new ArrayList<Integer>();
                List<Chromosone> Tournament = new ArrayList<Chromosone>();

                Random rand;
                Integer addToTournament = 0;

                for (int tournamentNo = 0; tournamentNo < tournamentSize; tournamentNo++) {
                    Tournament = new ArrayList<Chromosone>();
                    do {
                        rand = new Random();
                        addToTournament = rand.nextInt(popSize);
                    } while (checked.contains(addToTournament));
                    checked.add(addToTournament); // don't add the same chromo from previous population twice

                    Tournament.add(population.get(addToTournament)); // add random Chromo from previous population to
                                                                     // tournament
                }

                Collections.sort(Tournament); // put Chromosone with best fitness score in front

                newPopulation.add(Tournament.get(0)); // add best Chromosone to new population

            }

            // fill the rest of the population with crossover
            newPopulation = Crossover(newPopulation);

            if (MutationCount > 0) {
                newPopulation = Mutation(newPopulation, MutationCount);
                newPopulation = smartMutation(newPopulation, MutationCount);
            }

            // replace worst chromosones with new random ones
            if (RandomCount > 0)
                newPopulation = FreshChromosones(newPopulation, RandomCount);

            // replace worst with elite
            Collections.sort(population);
            Collections.sort(newPopulation);
            for (int i = 0; i < ElitismCount; i++) {
                newPopulation.set(newPopulation.size() - i - 1, population.get(i));
            }

            newPopulation.set(newPopulation.size() - 1, getBestFitnessChromo());

            population = newPopulation;
        }

        private List<Chromosone> smartMutation(List<Chromosone> inputPopulation, int MutationCount) {
            List<Chromosone> mutationPopulation = new ArrayList<Chromosone>(inputPopulation);

            for (int i = 0; i < MutationCount * 10; i++) {
                Random rand = new Random();
                int chosenOne = rand.nextInt(mutationPopulation.size());
                Chromosone mutateMe = mutationPopulation.get(chosenOne);

                for (int rowToMutate = 0; rowToMutate < dimension; rowToMutate++) {
                    List<Integer> hasDuplicate = new ArrayList<Integer>();

                    for (int col = 0; col < dimension; col++) {
                        for (int row = 0; row < dimension; row++) {
                            if (mutateMe.getDNA()[row][col] == mutateMe.getDNA()[rowToMutate][col]
                                    && rowToMutate != row) {
                                if (!hasDuplicate.contains(col))
                                    hasDuplicate.add(col);
                            }
                        }
                    }

                    // only do smart mutate if it not a fixed value and there are enough to swop
                    // with
                    if (hasDuplicate.size() > 1 && fixedSudokuArray[rowToMutate][hasDuplicate.get(0)] == '0'
                            && fixedSudokuArray[rowToMutate][hasDuplicate.get(1)] == '0') {
                        char[] mutatedRow = sudokuRow(mutateMe.getDNA(), rowToMutate);
                        char temp = mutatedRow[hasDuplicate.get(0)];
                        mutatedRow[hasDuplicate.get(0)] = mutatedRow[hasDuplicate.get(1)];
                        mutatedRow[hasDuplicate.get(1)] = temp;

                        mutateMe.setRow(mutatedRow, rowToMutate);

                        mutationPopulation.set(chosenOne, mutateMe);
                    }
                }
            }

            return mutationPopulation;
        }

        private List<Chromosone> Mutation(List<Chromosone> inputPopulation, int MutationCount) {
            List<Chromosone> mutationPopulation = new ArrayList<Chromosone>(inputPopulation);

            for (int i = 0; i < MutationCount; i++) {
                Random rand = new Random();

                mutationPopulation.get(rand.nextInt(mutationPopulation.size())).shuffleRow(fixedSudokuArray,
                        rand.nextInt(9));
                ;
            }

            return mutationPopulation;
        }

        // replace worst performing Chromosones
        private List<Chromosone> FreshChromosones(List<Chromosone> inputPopulation, int RandomCount) {
            List<Chromosone> randomPopulation = new ArrayList<Chromosone>(inputPopulation);

            Collections.sort(randomPopulation);

            for (int i = 0; i < RandomCount; i++) {
                randomPopulation.set(randomPopulation.size() - i - 1, new Chromosone(fixedSudokuArray));
            }
            return randomPopulation;
        }

        private List<Chromosone> Crossover(List<Chromosone> inputPopulation) {

            Chromosone parentA;
            Chromosone parentB;

            List<Chromosone> crossoverPopulation = new ArrayList<Chromosone>(inputPopulation);

            while (crossoverPopulation.size() < popSize) {
                Random rand;
                rand = new Random();
                parentA = inputPopulation.get(rand.nextInt(inputPopulation.size()));
                parentB = inputPopulation.get(rand.nextInt(inputPopulation.size()));

                Chromosone newChromo = new Chromosone(fixedSudokuArray);

                for (int row = 0; row < dimension; row++) {
                    boolean chooseParent = new Random().nextBoolean();
                    if (chooseParent) {
                        // parentA
                        newChromo.setRow(sudokuRow(parentA.getDNA(), row), row);
                    } else {
                        // parentB
                        newChromo.setRow(sudokuRow(parentB.getDNA(), row), row);
                    }
                }

                crossoverPopulation.add(newChromo);

            }

            return crossoverPopulation;

        }

        private int getBestFitnessScore() {
            // if(bestFitnessScore == 999)
            getBestFitnessChromo();
            return bestFitnessScore;
        }

        private double getAvgFitnessScore() {
            int total = 0;
            for (int i = 0; i < popSize; i++) {
                total += population.get(i).determineFitness();
            }
            return ((double) total) / popSize;
        }

        private Chromosone getBestFitnessChromo() {
            Chromosone best = population.get(0);
            for (int i = 0; i < popSize; i++) {
                if (population.get(i).determineFitness() < best.determineFitness())
                    best = population.get(i);
            }
            bestFitnessScore = best.determineFitness();
            return best;
        }

    }

    private static class Chromosone implements Comparable<Chromosone> {
        private char[][] DNA;

        private char[][] getDNA() {
            return DNA;
        }

        private void setDNA(char[][] _dna) {
            DNA = _dna;
        }

        private void setRow(char[] newRow, int rowIndex) {
            for (int i = 0; i < newRow.length; i++) {
                DNA[rowIndex][i] = newRow[i];
            }
        }

        private void shuffleRow(char[][] fixedSudokuArray, int rowNo) {
            char row[] = sudokuRow(DNA, rowNo);

            List<Character> shuffleList = new ArrayList<Character>();
            for (char c : row) {
                shuffleList.add(c);
            }

            Collections.shuffle(shuffleList);

            for (int col = 0; col < dimension; col++) {
                if (shuffleList.get(col) != fixedSudokuArray[rowNo][col] && fixedSudokuArray[rowNo][col] != '0') // put
                                                                                                                 // fixed
                                                                                                                 // positions
                                                                                                                 // back
                {
                    // swop the value that should be fixed with where the fixed one moved
                    shuffleList.set(shuffleList.indexOf(fixedSudokuArray[rowNo][col]), shuffleList.get(col));
                    shuffleList.set(col, fixedSudokuArray[rowNo][col]);

                }
            }

            char newSodukuArray[][] = copySudokuArray(DNA);

            for (int col = 0; col < dimension; col++) {
                newSodukuArray[rowNo][col] = shuffleList.get(col);
            }

            DNA = newSodukuArray;
        }

        private Integer determineFitness() {
            int errors = 0;

            // rows are always a valid permutation of 1..9

            // columns
            for (int col = 0; col < dimension; col++) {
                char[] currentCol = sudokuCol(DNA, col);
                List<Integer> checked = new ArrayList<Integer>();
                for (int i = 0; i < dimension; i++) {
                    for (int j = 0; j < dimension; j++) {
                        if (currentCol[i] == currentCol[j] && i != j && checked.contains(j) == false) {
                            errors++;
                            checked.add(j);
                        }
                    }

                }
            }

            // blocks
            for (int block = 0; block < dimension; block++) {
                char[][] currentBlock = sudokuBlock(DNA, block);
                List<Integer> checkedRow = new ArrayList<Integer>();
                List<Integer> checkedCol = new ArrayList<Integer>();
                for (int row1 = 0; row1 < 3; row1++) {
                    for (int col1 = 0; col1 < 3; col1++) {
                        for (int row2 = 0; row2 < 3; row2++) {
                            for (int col2 = 0; col2 < 3; col2++) {
                                for (int j = 0; j < dimension; j++) {
                                    if (currentBlock[row1][col1] == currentBlock[row2][col2]
                                            && !(row1 == row2 && col1 == col2) && (checkedRow.contains(row2) == false
                                                    && checkedCol.contains(col2) == false)) {
                                        errors++;
                                        checkedRow.add(row2);
                                        checkedCol.add(col2);
                                    }
                                }
                            }
                        }
                    }
                }

            }

            return errors;
        }

        private Chromosone(char[][] fixedSudokuArray) {
            char generatedChromosone[][] = copySudokuArray(fixedSudokuArray);
            int placementCounter;
            boolean placed;
            for (int row = 0; row < dimension; row++) {
                placementCounter = 1;
                for (int col = 0; col < dimension; col++) {
                    placed = false; // need to place a valid char in every col
                    if (fixedSudokuArray[row][col] != '0')
                        continue; // skip the predetermined (fixed) cells
                    while (!placed) {
                        if (!(new String(sudokuRow(generatedChromosone, row))
                                .contains(String.valueOf(placementCounter)))) { // check if row contains
                                                                                // placementCounter
                            generatedChromosone[row][col] = (char) (placementCounter + '0'); // place it
                            placed = true;
                        } else {
                            placementCounter++; // increase placement as it already exists in row
                        }
                    }
                }
                DNA = generatedChromosone;
                shuffleRow(fixedSudokuArray, row);
                generatedChromosone = DNA;
            }

            DNA = generatedChromosone;
        }

        @Override
        public int compareTo(Chromosone c) {
            return this.determineFitness().compareTo(c.determineFitness());
        }
    }

}