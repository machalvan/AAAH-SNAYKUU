package bot;

import gameLogic.*;
import org.tensorflow.*;
import org.tensorflow.Tensor;

import java.util.Arrays;

public class DeepRed implements Brain {
    private GameState gameState;
    private Snake self;
    private int boardHeight;
    private int boardWidth;
    private final int VIEW_DISTANCE = 2;
    private final int NUM_OF_INPUTS = (int) Math.pow(VIEW_DISTANCE * 2 + 1, 2);
    private final int INPUT_GRID_SIDE = (int) Math.sqrt(NUM_OF_INPUTS);
    private final int NUM_OF_GRIDS = 5;

    private final int EMPTY = 0;
    private final int SELF = 1;
    private final int OTHER_HEAD = 2;
    private final int OTHER_BODY = 3;
    private final int WALL = -1; //4;  // or dead snake
    private final int FRUIT = 9; //5;

    private final boolean HOT_ENCODED = true;
    private final String MODEL_DIR = "modelsThatAreGood/5x5_hotEncoded";
    private final SavedModelBundle MODEL = SavedModelBundle.load(MODEL_DIR, "serve");

    public Direction getNextMove(Snake self, GameState gameState) {
        this.self = self;
        this.gameState = gameState;
        this.boardHeight = gameState.getBoard().getHeight();
        this.boardWidth = gameState.getBoard().getWidth();

        System.out.println("Hello World! I'm using tensorflow version " + TensorFlow.version());

        int[][] inputData = getInputData();

        if (HOT_ENCODED) {
            float[] hotEncodedInput = new float[NUM_OF_INPUTS * NUM_OF_GRIDS];
            int[] squares = new int[]{WALL, SELF, OTHER_HEAD, OTHER_BODY, FRUIT};

            for (int i = 0; i < squares.length; i++) {
                int square = squares[i];

                for (int j = 0; j < inputData.length; j++) {
                    int[] inputDataRow = inputData[j];

                    for (int k = 0; k < inputDataRow.length; k++) {
                        int cell = inputDataRow[k];
                        hotEncodedInput[i * squares.length * inputDataRow.length + j * inputDataRow.length + k] = cell == square ? 1 : 0;
                    }
                }
            }

            float[][] hotEncodedInputData = new float[1][NUM_OF_INPUTS * NUM_OF_GRIDS];
            hotEncodedInputData[0] = hotEncodedInput;


            for (int j = 0; j < hotEncodedInput.length; j++) {
                float cell = hotEncodedInput[j];
                System.out.print((int) cell + " ");

                if ((j + 1) % NUM_OF_GRIDS == 0) {
                    System.out.println();
                }

                if ((j + 1) % NUM_OF_INPUTS == 0) {
                    System.out.println();
                }
            }


            Tensor inputTensor = Tensor.create(hotEncodedInputData);

            System.out.println("här");
            Tensor tensor = MODEL.session().runner()
                    .fetch("network/output_layer")
                    .feed("network/input_layer", inputTensor)
                    .run().get(0); //.expect(Float[].class);
            System.out.println("tensor: " + tensor);

            int outputs = (int) tensor.shape()[1];

            float[][] vector;
            Direction nextMove;
            if (outputs == 3) {
                vector = (float[][]) tensor.copyTo(new float[1][3]);
                int intDir = getMaxIndex(vector[0]);

                Direction goForward = self.getCurrentDirection();
                Direction goLeft = goForward.turnLeft();
                Direction goRight = goForward.turnRight();

                switch (intDir) {
                    case 0:
                        nextMove = goLeft;
                        break;
                    case 1:
                        nextMove = goForward;
                        break;
                    case 2:
                        nextMove = goRight;
                        break;
                    default:
                        System.out.println("RHEEE");
                        nextMove = goForward;
                }
            } else {
                System.out.println("4 outputs");
                vector = (float[][]) tensor.copyTo(new float[1][4]);
                int intDir = getMaxIndex(vector[0]);

                switch (intDir) {
                    case 0:
                        nextMove = Direction.NORTH;
                        break;
                    case 1:
                        nextMove = Direction.EAST;
                        break;
                    case 2:
                        nextMove = Direction.SOUTH;
                        break;
                    case 3:
                        nextMove = Direction.WEST;
                        break;
                    default:
                        System.out.println("RHEEE");
                        nextMove = Direction.NORTH;
                }
            }

            printDirectionScores(vector, outputs);
            System.out.println("I am going: " + nextMove);

            return nextMove;

        } else {

            int[][] inputDataFlat = matrixReshape(inputData, 1, NUM_OF_INPUTS);
            float[][] inputDataFlat2 = new float[1][NUM_OF_INPUTS];
            System.out.println("here-1");

            for (int i = 0; i < inputDataFlat.length; i++) {
                int[] inputDataFlatRow = inputDataFlat[i];

                for (int j = 0; j < inputDataFlatRow.length; j++) {
                    inputDataFlat2[i][j] = inputDataFlat[i][j];
                }
            }


            printInput(inputData);

            //Tensor inputTensor = Tensor.create(inputDataFlat);
            Tensor inputTensor = Tensor.create(inputDataFlat2);

            Tensor tensor = MODEL.session().runner()
                    .fetch("network/output_layer")
                    .feed("network/input_layer", inputTensor)
                    .run().get(0); //.expect(Float[].class);


            int outputs = (int) tensor.shape()[1];

            float[][] vector;
            Direction nextMove;
            if (outputs == 3) {
                vector = (float[][]) tensor.copyTo(new float[1][3]);
                int intDir = getMaxIndex(vector[0]);

                Direction goForward = self.getCurrentDirection();
                Direction goLeft = goForward.turnLeft();
                Direction goRight = goForward.turnRight();

                switch (intDir) {
                    case 0:
                        nextMove = goLeft;
                        break;
                    case 1:
                        nextMove = goForward;
                        break;
                    case 2:
                        nextMove = goRight;
                        break;
                    default:
                        System.out.println("RHEEE");
                        nextMove = goForward;
                }
            } else {
                System.out.println("4 outputs");
                vector = (float[][]) tensor.copyTo(new float[1][4]);
                int intDir = getMaxIndex(vector[0]);

                switch (intDir) {
                    case 0:
                        nextMove = Direction.NORTH;
                        break;
                    case 1:
                        nextMove = Direction.EAST;
                        break;
                    case 2:
                        nextMove = Direction.SOUTH;
                        break;
                    case 3:
                        nextMove = Direction.WEST;
                        break;
                    default:
                        System.out.println("RHEEE");
                        nextMove = Direction.NORTH;
                }
            }

            printDirectionScores(vector, outputs);
            System.out.println("I am going: " + nextMove);

            return nextMove;
        }
    }

    private boolean isSamePosition(Position p1, Position p2) { return p1.getX() == p2.getX() && p1.getY() == p2.getY(); }

    private boolean isSameSnake(Snake s1, Snake s2) { return s1.getName().equals(s2.getName()); }

    private int getMaxIndex(float[] array) {
        int maxAt = 0;

        for (int i = 0; i < array.length; i++) {
            maxAt = array[i] > array[maxAt] ? i : maxAt;
        }

        return maxAt;
    }

    private void printInput(int[][] inputData) {
        int middleIndex = Math.floorDiv(inputData.length, 2);

        for (int i = 0; i < inputData.length; i++) {
            int[] row = inputData[i];

            for (int j = 0; j < row.length; j++) {
                int cell = (int) row[j];
                String symbol;

                switch (cell) {
                    case EMPTY:
                        symbol = ".";
                        break;
                    case SELF:
                        symbol = i == middleIndex && j == middleIndex ? "☻" : "o";
                        break;
                    case OTHER_HEAD:
                        symbol = "☺";
                        break;
                    case OTHER_BODY:
                        symbol = "o";
                        break;
                    case WALL:
                        symbol = "#";
                        break;
                    case FRUIT:
                        symbol = "X";
                        break;
                    default:
                        symbol = "?";
                }

                System.out.print(symbol + " ");
            }

            System.out.println();
        }

        System.out.println();
    }

    private void printDirectionScores(float[][] vector, int outputs) {
        String[] directions;
        if (outputs == 3) {
            directions = new String[]{"Left", "Forward", "Right"};
        } else {
            directions = new String[]{"North", "East", "South", "West"};
        }

        for (float[] v : vector) {
            for (int i = 0; i < v.length; i++) {
                System.out.println(String.format("%s: %s", directions[i], v[i]));
            }
        }
        System.out.println();
    }

    private int[][] getBoardState() {
        Board board = gameState.getBoard();
        int[][] boardState = new int[boardHeight][boardWidth];

        for (int y = 0; y < boardHeight; y++) {
            //ArrayList<Integer> row = new ArrayList<>();

            for (int x = 0; x < boardWidth; x++) {
                Position pos = new Position(x, y);
                Square square = board.getSquare(pos);
                int cell = EMPTY;

                if (square.hasSnake()) {
                    Snake snakeInSquare = square.getSnakes().get(0);
                    Position snakeHead = snakeInSquare.getHeadPosition();

                    if (snakeInSquare.isDead()) {
                        cell = WALL;
                    } else {
                        if (isSameSnake(snakeInSquare, self)) {
                            // Square contains self
                            cell = SELF;
                        } else {
                            // Square contains other snake
                            if (isSamePosition(pos, snakeHead)) { cell = OTHER_HEAD; } else { cell = OTHER_BODY; }
                        }
                    }
                } else if (square.hasWall()) {
                    cell = WALL;
                } else if (square.hasFruit()) {
                    cell = FRUIT;
                }

                //row.add(cell);
                boardState[y][x] = cell;
            }
        }

        return boardState;
    }

    private int[][] getInputData() {
        int shape = 0;           // 0 = KVADRAT, 1 = RUTER
        int boardMargin = INPUT_GRID_SIDE;  // To left and right, as well as up and down
        int radius = INPUT_GRID_SIDE / 2;
        int[][] newBoardState = new int[INPUT_GRID_SIDE][INPUT_GRID_SIDE];
        int[][] boardState = getBoardState();

        Position headPos = self.getHeadPosition();
        boolean firstVisionGridSquare = true;
        int visionGridStartX = 0;
        int visionGridStartY = 0;

        for (int y = 0; y < boardHeight + boardMargin; y++) {
            int y2 = y - boardMargin / 2;

            for (int x = 0; x < boardWidth + boardMargin; x++) {
                int x2 = x - boardMargin / 2;
                int square = WALL;  // Outside grid

                if (x2 >= 0 && x2 < boardWidth && y2 >= 0 && y2 < boardHeight) {
                    square = boardState[y2][x2];
                }

                int xDistToHead = Math.abs(x2 - headPos.getX());
                int yDistToHead = Math.abs(y2 - headPos.getY());

                boolean condition = false;
                if (shape == 1) {  // RUTER
                    // condition = ...;
                } else {  // KVADRAT
                    condition = xDistToHead <= radius  && yDistToHead <= radius;
                }

                if (condition) {
                    if (firstVisionGridSquare) {
                        visionGridStartX = x2;
                        visionGridStartY = y2;
                        firstVisionGridSquare = false;
                    }

                    newBoardState[y2 - visionGridStartY][x2 - visionGridStartX] = square;
                }
            }
        }

        System.out.println("Board state: " + Arrays.deepToString(newBoardState));
        return newBoardState;
    }

    /*
     * Input: nums =
     * [[1,2],
     * [3,4]]
     * r = 1, c = 4
     *
     * Output:
     * [[1,2,3,4]]
     */
    public int[][] matrixReshape(int[][] nums, int r, int c) {
        int totalElements = nums.length * nums[0].length;
        if (totalElements != r * c || totalElements % r != 0) {
            System.out.println("Exiting...");
            return nums;
        }
        final int[][] result = new int[r][c];
        int newR = 0;
        int newC = 0;
        for (int[] num : nums) {
            for (float i : num) {
                result[newR][newC] = (int) i;
                newC++;
                if (newC == c) {
                    newC = 0;
                    newR++;
                }
            }
        }
        return result;
    }
}
