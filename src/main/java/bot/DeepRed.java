package bot;

import gameLogic.*;
import org.tensorflow.*;
import org.tensorflow.Tensor;

import java.util.ArrayList;
import java.util.Arrays;

public class DeepRed implements Brain {
	private GameState gameState;
	private Snake self;

	public Direction getNextMove(Snake self, GameState gameState) {
        this.self = self;
        this.gameState = gameState;

        System.out.println( "Hello World! I'm using tensorflow version " + TensorFlow.version() );
        String modelDir = "./smaller_input_model6";

        float[][] inputData = getInputData();
        printInput(inputData);

        SavedModelBundle model = SavedModelBundle.load(modelDir, "serve");
        Tensor inputTensor = Tensor.create(inputData);

        Tensor tensor = model.session().runner()
                .fetch("network/output_layer")
                .feed("network/input_layer", inputTensor)   // float
                //.feed("actual_move", actualMoveTensor)      // float
                .run().get(0); //.expect(Float[].class);

        System.out.println(tensor.toString());
        System.out.println(tensor.shape().toString());

        float[][] vector = (float[][]) tensor.copyTo(new float[1][3]);

        int intDir = getMaxIndex(vector[0]);
        System.out.println("intDir: " + intDir);

        for (float[] v : vector) {
            for (float w : v) {
                System.out.println("w: " + w);
            }
        }
        System.out.println();

        Direction goForward = self.getCurrentDirection();
        Direction goLeft = goForward.turnLeft();
        Direction goRight = goForward.turnRight();

        Direction nextMove;
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

        System.out.println("I am going: " + nextMove);

		return nextMove;
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

    private void printInput(float[][] inputData) {
        System.out.println("inputData");

        for (float[] row : inputData) {
            for (float cell : row) {
                String symbol = Integer.toString((int) cell);
                if (cell < 0) { symbol = "-"; };
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }

    private int[][] getBoardState() {
        final int EMPTY = 0;
        final int SELF = 1;
        final int OTHER_HEAD = 2;
        final int OTHER_BODY = 3;
        final int WALL = 4;  // or dead snake
        final int FRUIT = 5;

        Board board = gameState.getBoard();
        int height = board.getHeight();
        int width = board.getWidth();
        int[][] boardState = new int[height][width];

        for (int y = 0; y < height; y++) {
            ArrayList<Integer> row = new ArrayList<>();

            for (int x = 0; x < width; x++) {
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

                row.add(cell);
                boardState[y][x] = cell;
            }

            //boardState.add(new ArrayList<>(row));
        }

        //int[][] boardState = new ArrayList<ArrayList<Integer>>(boardState);
        //System.out.println("Board state: " + boardState.toString());
        return boardState;
    }

    private float[][] getInputData() {
        int shape = 0;           // 0 = KVADRAT, 1 = RUTER
        int inputGridSize = 9;
        int gridSideLength = (int) Math.sqrt(inputGridSize);
        int boardMargin = gridSideLength;  // To left and right, as well as up and down
        int radius = gridSideLength / 2;
        System.out.println("radius " + radius);
        System.out.println("boardMargin " + boardMargin);
        float[][] newBoardState = new float[gridSideLength][gridSideLength];
        int[][] boardState = getBoardState();
        Board board = gameState.getBoard();
        int height = board.getHeight();
        int width = board.getWidth();

        Position headPos = self.getHeadPosition();
        boolean firstVisionGridSquare = true;
        int visionGridStartX = 0;
        int visionGridStartY = 0;

        for (int y = 0; y < height + boardMargin; y++) {
            int y2 = y - boardMargin / 2;
            //System.out.println("y " + y);
            //System.out.println("y2 " + y2);
            //int[] row = new int[y2];

            for (int x = 0; x < width + boardMargin; x++) {
                int x2 = x - boardMargin / 2;
//                System.out.println("x " + x);
//                System.out.println("x2 " + x2);
                int square = 0;

                if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
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
                    //row[x2] = square;  //.add(square);

                    if (firstVisionGridSquare) {
                        visionGridStartX = x2;
                        visionGridStartY = y2;
                        firstVisionGridSquare = false;
                    }

                    newBoardState[y2 - visionGridStartY][x2 - visionGridStartX] = square;
                }
            }
        }

        System.out.println("newBoardState: " + Arrays.deepToString(newBoardState));
        float[][] newBoardState2 = matrixReshape(newBoardState, 1, 9);
        System.out.println("newBoardState2: " + Arrays.deepToString(newBoardState2));

        return newBoardState2;
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
    public float[][] matrixReshape(float[][] nums, int r, int c) {
        int totalElements = nums.length * nums[0].length;
        if (totalElements != r * c || totalElements % r != 0) {
            System.out.println("Exiting...");
            return nums;
        }
        final float[][] result = new float[r][c];
        int newR = 0;
        int newC = 0;
        for (float[] num : nums) {
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
