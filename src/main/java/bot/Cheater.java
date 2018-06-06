package bot;

import gameLogic.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A cheating bot for the engine/API Snaykuu.
 *
 * @author	Marcus Hansson
 * @version 1.0
 */
public class Cheater implements Brain {
    private Snake self;
    private GameState gameState;
    private Direction prevDir;
    private Direction turnRight;
    private Direction turnLeft;
    private Direction chosenDir = null;
    private Board board;
    private Position head;

    public Direction getNextMove(Snake self, GameState gameState) {
        this.self = self;
        this.gameState = gameState;

        board = gameState.getBoard();
        prevDir = self.getCurrentDirection();
        turnRight = prevDir.turnRight();
        turnLeft = prevDir.turnLeft();
        head = self.getHeadPosition();

        ArrayList<Direction> allowedDirs = new ArrayList<>(Arrays.asList(prevDir, turnRight, turnLeft));
        ArrayList<Direction> safeDirs = getSafeDirs(allowedDirs);
        ArrayList<Direction> finalDirs = safeDirs;

        Position closestFruit = getClosestFruit(head);

        if (closestFruit != null) {
            ArrayList<Direction> closestFruitDirs = gameState.getRelativeDirections(head, closestFruit);
            ArrayList<Direction> wallFreeDirsTowardsFruit = getAllowedDirsTowardsFruit(safeDirs, closestFruitDirs);

            if (!wallFreeDirsTowardsFruit.isEmpty()) {
                finalDirs = wallFreeDirsTowardsFruit;
            }
        }

        if (!finalDirs.isEmpty()) {
            // Any will do
            chosenDir = finalDirs.get(0);
        } else {
            // Sorry, you will probably die
            chosenDir = getWallFreeDir();
        }

        Position nextPos = chosenDir.calculateNextPosition(head);

        if (board.hasSnake(nextPos)) {
            performJump();
        }

        return chosenDir;
    }

    private Direction getWallFreeDir() {
        Square leftSquare = board.getSquare(turnLeft.calculateNextPosition(head));
        Square forwardSquare = board.getSquare(prevDir.calculateNextPosition(head));

        if (!leftSquare.hasWall()) {
            return turnLeft;
        } else if (!forwardSquare.hasWall()) {
            return prevDir;
        } else {
            return turnRight;
        }
    }

    private Position getClosestFruit(Position head) {
        int distanceToClosestFruit = Integer.MAX_VALUE;
        Position closestFruit = null;
        ArrayList<Position> fruits = gameState.getFruits();

        for (Position fruit : fruits) {
            int distanceToFruit = head.getDistanceTo(fruit);

            if (distanceToFruit < distanceToClosestFruit) {
                distanceToClosestFruit = distanceToFruit;
                closestFruit = fruit;
            }
        }

        return closestFruit;
    }

    private ArrayList<Direction> getAllowedDirsTowardsFruit(ArrayList<Direction> wallFreeDirs, 
            ArrayList<Direction> dirsToClosestFruit) {
        // Return the intersection of the two lists
        ArrayList<Direction> allowedDirsTowardsFruit = new ArrayList<>();

        for (Direction dir : wallFreeDirs) {
            if (dirsToClosestFruit.contains(dir)) {
                allowedDirsTowardsFruit.add(dir);
            }
        }

        return allowedDirsTowardsFruit;
    }

    private boolean neighbourHasAliveSnakeHead(Position pos) {
        Set<Snake> allSnakes = gameState.getSnakes();

        for (Snake snake : allSnakes) {
            if (snake.getHeadPosition() != self.getHeadPosition() && !snake.isDead()) {
                Position snakeHead = snake.getSegments().get(0);

                if (pos.getNeighbours().contains(snakeHead)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean willJumpIntoWall(Direction dir, Position landingPos) {
        while (true) {
            if (board.hasWall(landingPos)) {
                return true;
            } else if (!board.hasSnake(landingPos)) {
                return neighbourHasAliveSnakeHead(landingPos);
            }

            try {
                landingPos = dir.calculateNextPosition(landingPos);
            } catch (Exception e) {
                System.err.println(e);
                return true;
            }
        }
    }

    private boolean dirIsSafe(Direction dir) {
        Position nextPos = dir.calculateNextPosition(head);

        if (board.hasWall(nextPos)) {
            return false;
        } else if (neighbourHasAliveSnakeHead(nextPos)) {
            return false;
        } else if (board.hasSnake(nextPos)) {
            try {
                Position landingPos = dir.calculateNextPosition(nextPos);

                if (willJumpIntoWall(dir, landingPos)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            
        }

        return true;
    }

    private ArrayList<Direction> getSafeDirs(ArrayList<Direction> allowedDirs) {
        ArrayList<Direction> dirsSafeFromWall = new ArrayList<>();

        for (Direction dir : allowedDirs) {
            if (dirIsSafe(dir)) {
                dirsSafeFromWall.add(dir);
            }
        }

        return dirsSafeFromWall;
    }

    private void performJump() {
        boolean performJump = true;
        LinkedList<Position> segments = self.getSegments();
        LinkedList<SnakeSegment> directionLog = self.getDrawData();
        Position headPos = head;

        try {
            while (performJump) {
                // Using reflection

                Class<?> snakeClass = self.getClass();
                Field segmentsField = snakeClass.getDeclaredField("segments");
                Field directionLogField = snakeClass.getDeclaredField("directionLog");
                Constructor<SnakeSegment> constructor = SnakeSegment.class.getDeclaredConstructor(
                    Position.class, 
                    Direction.class
                );

                segmentsField.setAccessible(true);
                directionLogField.setAccessible(true);
                constructor.setAccessible(true);

                Position nextPos = chosenDir.calculateNextPosition(headPos);
                SnakeSegment nextSS = constructor.newInstance(nextPos, chosenDir);

                segments.addFirst(nextPos);
                directionLog.addFirst(nextSS);

                segmentsField.set(self, segments);
                directionLogField.set(self, directionLog);
                
                headPos = nextPos;
                
                Position landingPos = chosenDir.calculateNextPosition(segments.get(0));
                performJump = board.hasSnake(landingPos);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
