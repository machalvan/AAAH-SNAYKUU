package bot;

import gameLogic.*;

public class Crazy implements Brain {
	private GameState gamestate;
	private Snake snake;

	public Direction getNextMove(Snake snake, GameState gameState) {
		this.snake = snake;
		this.gamestate = gameState;

		Direction forwardDir = snake.getCurrentDirection();
		Direction leftDir = snake.getCurrentDirection().turnLeft();
		Direction rightDir = snake.getCurrentDirection().turnRight();

		PossibleNextSquare forwardSquare = new PossibleNextSquare(forwardDir);
		PossibleNextSquare leftSquare = new PossibleNextSquare(leftDir);
		PossibleNextSquare rightSquare = new PossibleNextSquare(rightDir);

		return smallestRiskDir(forwardSquare, leftSquare, rightSquare);
	}

	public Direction smallestRiskDir(PossibleNextSquare forwardSquare, PossibleNextSquare leftSquare,
			PossibleNextSquare rightSquare) {
		if (forwardSquare.getRisk() < leftSquare.getRisk() && forwardSquare.getRisk() < rightSquare.getRisk()) {
			return forwardSquare.getDir();
			
		} else if (leftSquare.getRisk() < rightSquare.getRisk()) {
			return leftSquare.getDir();
		}

		return rightSquare.getDir();
	}

	public class PossibleNextSquare {
		private Direction hypotheticalDir;
		private int riskOfDeath;

		PossibleNextSquare(Direction hypotheticalDir) {
			this.hypotheticalDir = hypotheticalDir;

			if (gamestate.willCollide(snake, hypotheticalDir)) {
				riskOfDeath = 1;
			} else {
				riskOfDeath = 0;
			}
		}

		public Direction getDir() {
			return hypotheticalDir;
		}

		public int getRisk() {
			return riskOfDeath;
		}
	}
}
