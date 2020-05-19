package bot;

import gameLogic.*;

import java.util.ArrayList;

public class CoolBot implements Brain {

    public Direction getNextMove(Snake snake, GameState gameState) {
        Direction goForward = snake.getCurrentDirection();
        Direction goLeft = goForward.turnLeft();
        Direction goRight = goForward.turnRight();
        Direction oppositeDir = goLeft.turnLeft();

        ArrayList<Position> fruitPositions = gameState.getFruits();
        Position headPos = snake.getHeadPosition();

        if (fruitPositions.size() > 0) {
            Position closestFruitPos = getClosestFruitPos(headPos, fruitPositions);
            ArrayList<Direction> fruitDirs = GameState.getRelativeDirections(headPos, closestFruitPos);

            for (Direction fruitDir : fruitDirs) {
                if (fruitDir.equals(oppositeDir)) {
                    continue;
                }

                if (!gameState.willCollide(snake, fruitDir)) {
                    return fruitDir;
                }
            }
        }

        boolean forwardIsLethal = gameState.willCollide(snake, goForward);
        boolean leftsLethal = gameState.willCollide(snake, goLeft);

        if (forwardIsLethal) {
            if (leftsLethal) {
                return goRight;
            } else {
                return goLeft;
            }
        }

        return goForward;
    }

    private Position getClosestFruitPos(Position headPos, ArrayList<Position> fruitPositions) {
        int shortestFruitDistance = 1000;
        Position closestFruitPos = null;

        for (Position fruitPos: fruitPositions) {
            int fruitDistance = GameState.distanceBetween(headPos, fruitPos);

            if (fruitDistance < shortestFruitDistance) {
                shortestFruitDistance = fruitDistance;
                closestFruitPos = fruitPos;
            }
        }

        return closestFruitPos;
    }
}
