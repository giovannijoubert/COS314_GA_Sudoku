import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;



public class sudokuGA {
    public static void main(String[] args) {
        int dimension = 9;
        char[][] SudokuArray = new char[dimension][dimension];
        //read file
        try {
            File inputFile = new File("inputs/demo.txt");
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
        SudokuArray = fillPredeterminedHiddenSingles(SudokuArray);
        SudokuArray = fillPredeterminedHiddenSingles(SudokuArray);

        System.out.println("Predetermined Filled SUDOKU");
        prettyPrint(SudokuArray);
       
    }

    private static char[][] fillPredeterminedNakedSingles(char[][] sudokuArray){
        boolean[][][] cellBools = new boolean[9][9][9];
        //false values mean they are available

        boolean improvement = false; 
        cellBools = pencilPossibles(sudokuArray); //calculate pencil

    do{
        improvement = false;
        for(int row = 0; row < 9; row++){
            for(int col = 0; col < 9; col++){
                if(sudokuArray[row][col] == '0'){
                    boolean tripwire = false;
                    for(int possibles = 0; possibles < 9; possibles++){
                        if(!cellBools[row][col][possibles]){
                            for(int moreThanOne = 0; moreThanOne < 9 ; moreThanOne++)
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

    private static char[][] fillPredeterminedHiddenSingles(char[][] sudokuArray){
        char[][] sudokuArray1 = sudokuArray;
        boolean[][][] cellBools = new boolean[9][9][9];
        cellBools = pencilPossibles(sudokuArray1);

        for(int row = 0; row < 9; row++){
            for(int col = 0; col < 9; col++){ //iterate over every cell
                if(sudokuArray1[row][col] == '0'){ //work only on empty cells
                    for(int cellPencilCounter = 0; cellPencilCounter < 9; cellPencilCounter++){ //check every item in [row][col][0..9]
                        boolean rowFound = false;
                        boolean colFound = false;
                        boolean sgFound = false;

                        if(cellBools[row][col][cellPencilCounter] == false){ //only check numbers that are viable in current cell
                            for(int innerCounter = 0; innerCounter < 9; innerCounter++){ //compare to everything in row+col
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
        for(int row = 0; row < 9; row++){
            for(int col = 0; col < 9; col++){
                if(sudokuArray[row][col] == '0'){ //empty cell
                    for(int innerCounter = 0; innerCounter < 9; innerCounter++){ //check every item in row+col
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
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
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