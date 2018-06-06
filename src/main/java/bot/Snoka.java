
package bot;

import gameLogic.Brain;
import gameLogic.Direction;
import gameLogic.GameState;
import gameLogic.Position;
import gameLogic.Snake;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author rloqvist
 */
public class Snoka implements Brain {
    
    private Snake yourSnake;
    private GameState gameState;

    public Direction getNextMove(Snake yourSnake, GameState gameState) {
        
        this.yourSnake = yourSnake;
        this.gameState = gameState;
        
        Direction currentDirection = yourSnake.getCurrentDirection();
        
        if ( gameState.getFruits().isEmpty() ) {
            currentDirection = this.getMiddleDirection(currentDirection);
        } else {
            currentDirection = this.getFruitDirection(currentDirection);
        }
        
        return this.getSafeDirection(currentDirection);
    }
    
    private Direction getMiddleDirection(Direction direction) {
        if ( this.isMovingToMiddle(direction) ) {
            return direction;
        } else if ( this.isMovingToMiddle(direction.turnLeft()) ) {
            return direction.turnLeft();
        } else {
            return direction.turnRight();
        }
    }
    
    private boolean isMovingToMiddle(Direction direction){        
        Position currPos = yourSnake.getHeadPosition();
        Position nextPos = direction.calculateNextPosition(currPos);
        
        return this.getMiddleDist(nextPos) < this.getMiddleDist(currPos);
    }
    
    private int getMiddleDist(Position position) {
        float middleX = gameState.getBoard().getWidth()/ 2;
        float middleY = gameState.getBoard().getHeight() / 2;
        
        float distX = (middleX - position.getX()) * (middleX - position.getX());
        float distY = (middleY - position.getY()) * (middleY - position.getY());
        
        return Float.floatToIntBits(distX + distY);
    }
    
    private Direction getFruitDirection(Direction direction) {
        if ( this.isMovingToFruit(direction) ) {
            return direction;
        } else if ( this.isMovingToFruit(direction.turnLeft()) ) {
            return direction.turnLeft();
        } else {
            return direction.turnRight();
        }
    }
    
    private boolean isMovingToFruit(Direction direction){
        Position currPos = yourSnake.getHeadPosition();
        Position nextPos = direction.calculateNextPosition(currPos);
        
        return this.getFruitDist(nextPos) < this.getFruitDist(currPos);
    }
    
    private int getFruitDist(Position position) {
        ArrayList<Position> fruits = gameState.getFruits();
        int shortest = gameState.getBoard().getHeight() + gameState.getBoard().getWidth();
        
        for (Position fruit : fruits) {
            int distance = position.getDistanceTo(fruit);
            if ( distance < shortest ) {
                shortest = distance;
            }
        }
        
        return shortest;
        
    }

    private Direction getSafeDirection(Direction direction) {
        Position position = yourSnake.getHeadPosition();
        if ( this.canContinue(position, direction) ) {
            return direction;
        } else if ( this.canContinue(position, direction.turnLeft()) ) {
            return direction.turnLeft();
        } else if ( this.canContinue(position, direction.turnRight()) ){
            return direction.turnRight();
        } else {
            return direction.turnLeft().turnLeft();
        }
    }

    
    private boolean canContinue(Position position, Direction direction) {
        Position nextPos = direction.calculateNextPosition(position);
        
        Set<Snake> snakes = gameState.getSnakes();
        
        for (Snake snake : snakes) {
            LinkedList<Position> segments = snake.getSegments();
            for (Position segment : segments) {
                if ( segment.equals(nextPos) ) {
                    return false;
                }
            }
        }
        
        ArrayList<Position> walls = gameState.getWalls();
        
        for (Position wall : walls) {
            if ( wall.equals(nextPos) ) {
                return false;
            }
        }
        
        return true;
    }
    
}
