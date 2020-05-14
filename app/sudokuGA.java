import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Collections;
import java.util.List;



public class sudokuGA {
    public static int dimension = 9;
    public static void main(String[] args) {
       
        char[][] SudokuArray = new char[dimension][dimension];
        //read file
        try {
            File inputFile = new File("inputs/christi.txt");
            Scanner myReader = new Scanner(inputFile);
            int row = 0;
            int col = 0;
            while (myReader.hasNextLine()) {
              String data = myReader.nextLine();
              for (int i = 0; i < data.length(); i++){
                if(data.charAt(i) != ' '){
                    SudokuArray[row][col] = data.charAt(i);
                    col++;
                }
              }
              col = 0;
              row++;
            }
            myReader.close();
            
          } catch (FileNotFoundException e) {
            System.out.println("Couldn't open file.");
            e.printStackTrace();
          }

        System.out.println("INPUT SUDOKU");
        prettyPrint(SudokuArray);


        SudokuArray = fillPredeterminedNakedSingles(SudokuArray);
        SudokuArray = fillPredeterminedHiddenSingles(SudokuArray);

        System.out.println("Predetermined Filled SUDOKU");
        prettyPrint(SudokuArray);

        System.out.println("Random Chromosone");
        char chromosone[][] = generateChromosone(SudokuArray);
        prettyPrint(chromosone);

        System.out.println("FITNESS: " + determineFitness(chromosone));

    }

    private static int determineFitness(char[][] sudokuArray){
        int errors = 0;

     

        //columns
        for (int col = 0; col < dimension; col++) {
            char[] currentCol = sudokuCol(sudokuArray, col);
            List<Character> checked = new ArrayList<Character>(); 
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    if(currentCol[i] == currentCol[j] && i != j && checked.contains(currentCol[i]) == false ){
                        errors++;
                        checked.add(currentCol[i]);
                    }
                }
               
            }
        }

        return errors;
    }

    private static char[][] copySudokuArray(char[][] toClone){
        char clone[][] = new char[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                clone[i][j] = toClone[i][j];
            }
        }
        return clone;
    }

    private static char[][] generateChromosone(char[][] fixedSudokuArray){
        char generatedChromosone[][] = copySudokuArray(fixedSudokuArray);
        int placementCounter;
        boolean placed;
        for(int row = 0; row < dimension; row++){
            placementCounter = 1;
            for(int col = 0; col < dimension; col++){
                placed = false; //need to place a valid char in every col
                if(fixedSudokuArray[row][col] != '0')
                    continue; // skip the predetermined (fixed) cells
                while(!placed){
                    if(! (new String(sudokuRow(generatedChromosone, row)).contains(String.valueOf(placementCounter)))){ //check if row contains placementCounter
                        generatedChromosone[row][col] = (char)(placementCounter + '0'); //place it
                        placed = true;
                    } else {
                        placementCounter++; //increase placement as it already exists in row
                    }
                }  
            }
            generatedChromosone = shuffleRow(fixedSudokuArray, generatedChromosone, row);
        }


        return generatedChromosone;
    }

    private static char[][] shuffleRow(char[][] fixedSudokuArray, char[][] inputSudokuArray, int rowNo){
        char row[] = sudokuRow(inputSudokuArray, rowNo);

        List<Character> shuffleList = new ArrayList<Character>();
        for (char c : row) {
            shuffleList.add(c);
        }

        Collections.shuffle(shuffleList);

        for(int col = 0; col < dimension; col++){
            if (shuffleList.get(col) != fixedSudokuArray[rowNo][col] && fixedSudokuArray[rowNo][col] != '0') //put fixed positions back
            {
                //swop the value that should be fixed with where the fixed one moved
                shuffleList.set(shuffleList.indexOf(fixedSudokuArray[rowNo][col]), shuffleList.get(col)); 
                shuffleList.set(col, fixedSudokuArray[rowNo][col]); 

            }
        }

        char newSodukuArray[][] = copySudokuArray(inputSudokuArray);

        for (int col = 0; col < dimension; col++) {
            newSodukuArray[rowNo][col] = shuffleList.get(col);
        }

        return newSodukuArray;
    }



    //return a char[][] BLOCK from the passed in sudokuArray
    private static char[][] sudokuBlock(char[][] sudokuArray, int index){
        char[][] block = new char[dimension][dimension];
        int blockstart_x = 0;
        int blockstart_y = 0;

        switch(index){
            case  0:
                blockstart_x = 0;
                blockstart_y = 0;
                break;
            case 1:
                blockstart_x = 3;
                blockstart_y = 0;
                break;
            case 2:
                blockstart_x = 6;
                blockstart_y = 0;
                break;
            case 3:
                blockstart_x = 0;
                blockstart_y = 3;
                break;
            case 4:
                blockstart_x = 3;
                blockstart_y = 3;
                break;
            case 5:
                blockstart_x = 6;
                blockstart_y = 3;
                break;
            case 6:
                blockstart_x = 0;
                blockstart_y = 6;
                break;
            case 7:
                blockstart_x = 3;
                blockstart_y = 6;
                break;
            case 8:
                blockstart_x = 6;
                blockstart_y = 6;
                break;
        }

        for(int x = 0; x < 3; x++)
            for(int y = 0; y < 3; y++)
              block[x][y] = sudokuArray[blockstart_y+x][blockstart_x+y];

        return block;
    }

    //return a char[] COL from the passed in sudokuArray
    private static char[] sudokuCol(char[][] sudokuArray, int index){
        char[] col = new char[9];
        for(int i = 0; i < dimension; i++){
            col[i] = sudokuArray[i][index];
        }

        return col;
    }

    //return a char[] ROW from the passed in sudokuArray
    private static char[] sudokuRow(char[][] sudokuArray, int index){
        char[] row = new char[9];
        for(int i = 0; i < dimension; i++){
            row[i] = sudokuArray[index][i];
        }

        return row;
    }

    private static char[][] fillPredeterminedNakedSingles(char[][] sudokuArray){
        boolean[][][] cellBools = new boolean[9][9][9];
        //false values mean they are available

        boolean improvement = false; 
        cellBools = pencilPossibles(sudokuArray); //calculate pencil

    do{
        improvement = false;
        for(int row = 0; row < dimension; row++){
            for(int col = 0; col < dimension; col++){
                if(sudokuArray[row][col] == '0'){
                    boolean tripwire = false;
                    for(int possibles = 0; possibles < dimension; possibles++){
                        if(!cellBools[row][col][possibles]){
                            for(int moreThanOne = 0; moreThanOne < dimension ; moreThanOne++)
                                if(!cellBools[row][col][moreThanOne] && moreThanOne != possibles)
                                    tripwire = true;
                        
                        
                            if(!tripwire){
                                sudokuArray[row][col] = (char)(possibles+1 + '0');  
                                cellBools = pencilPossibles(sudokuArray); //calculate pencil
                                improvement = true;
                            }
                        }
                    } 
                }    
            }   
        }
    } while(improvement);

        return (sudokuArray);
    }

    private static char[][] fillPredeterminedHiddenSingles(char[][] inputSudokuArray){
        char[][] sudokuArray1 = copySudokuArray(inputSudokuArray);
        boolean[][][] cellBools = new boolean[9][9][9];
        cellBools = pencilPossibles(sudokuArray1);

        for(int row = 0; row < dimension; row++){
            for(int col = 0; col < dimension; col++){ //iterate over every cell
                if(sudokuArray1[row][col] == '0'){ //work only on empty cells
                    for(int cellPencilCounter = 0; cellPencilCounter < dimension; cellPencilCounter++){ //check every item in [row][col][0..9]
                        boolean rowFound = false;
                        boolean colFound = false;
                        boolean sgFound = false;

                        if(cellBools[row][col][cellPencilCounter] == false){ //only check numbers that are viable in current cell
                            for(int innerCounter = 0; innerCounter < dimension; innerCounter++){ //compare to everything in row+col
                                if(colFound && rowFound)
                                    break;
                                if(cellBools[row][innerCounter][cellPencilCounter] == false && sudokuArray1[row][innerCounter] == '0' && innerCounter != col) //check cols
                                    colFound = true;
                                if(cellBools[innerCounter][col][cellPencilCounter] == false && sudokuArray1[innerCounter][col] == '0' && innerCounter != row) //check rows
                                    rowFound = true;
                            }
                            //check subgroup
                            int sgRow = row/3*3;
                            int sgCol = col/3*3;
                            for (int localRow = sgRow; localRow < sgRow+3; localRow++)
                                for(int localCol = sgCol; localCol < sgCol+3; localCol++)
                                    if((cellBools[localRow][localCol][cellPencilCounter] == false) && (localCol != col || localRow != row) && (sudokuArray1[localRow][localCol] == '0')) 
                                        sgFound = true;
                            
                            if(!rowFound || !colFound || !sgFound){ //write pencil as pen
                                sudokuArray1[row][col] = (char)(cellPencilCounter+1 + '0');
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

    private static boolean[][][] pencilPossibles(char[][] sudokuArray){
        boolean[][][] cellBools = new boolean[9][9][9];
        for(int row = 0; row < dimension; row++){
            for(int col = 0; col < dimension; col++){
                if(sudokuArray[row][col] == '0'){ //empty cell
                    for(int innerCounter = 0; innerCounter < dimension; innerCounter++){ //check every item in row+col
                        if(sudokuArray[row][innerCounter] != '0') // check col
                            cellBools[row][col][Character.getNumericValue(sudokuArray[row][innerCounter])-1] = true;

                        if(sudokuArray[innerCounter][col] != '0') // check row
                            cellBools[row][col][Character.getNumericValue(sudokuArray[innerCounter][col])-1] = true;
                    }

                     //check subgroup
                     int sgRow = row/3*3;
                     int sgCol = col/3*3;
                     for (int localRow = sgRow; localRow < sgRow+3; localRow++)
                         for(int localCol = sgCol; localCol < sgCol+3; localCol++){
                             if(sudokuArray[localRow][localCol] != '0')
                                 cellBools[row][col][Character.getNumericValue(sudokuArray[localRow][localCol])-1] = true;
                         }
                }
            }
        }
        return cellBools;
    }

    private static void prettyPrint(char[][] sudokuArray){
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if(sudokuArray[i][j] == '0')
                    System.out.print("-"  + " ");
                else 
                    System.out.print(sudokuArray[i][j] + " ");
                if((j+1) % 3 == 0)
                    System.out.print(" ");
            }
            System.out.println("");
            if((i+1) % 3 == 0)
                System.out.println("");
        }
    }

}