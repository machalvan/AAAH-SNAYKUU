package bot;

import gameLogic.*;

public class Gruppott implements Brain
{
	private GameState gamestate;
	private Snake self;
	public Position getNextNextPossition(Direction dir){
		return GameState.calculateNextPosition(dir, GameState.calculateNextPosition(dir,self.getHeadPosition()));
	}
	public Direction getNextMove(Snake yourSnake, GameState gamestate)
	{
		self = yourSnake;
		this.gamestate = gamestate;
		Direction direction = self.getCurrentDirection();
		Position position = self.getHeadPosition();
		//Paranoid turn-path
		if (gamestate.getBoard().isLethal(getNextNextPossition(direction))){
			if(!gamestate.getBoard().isLethal(GameState.calculateNextPosition(direction.turnLeft(), position))){
				return direction.turnLeft();
			}
		}

		if (gamestate.willCollide(self, direction))
		{
			if(gamestate.getBoard().isLethal(GameState.calculateNextPosition(direction.turnLeft(), position))){
				return direction.turnRight();
			}
			return direction.turnLeft();
		}
		if (gamestate.getBoard().isLethal(GameState.calculateNextPosition(direction,position))){
			if(!gamestate.getBoard().isLethal(GameState.calculateNextPosition(direction.turnLeft(), position))){
				return direction.turnLeft();
			}
		}

		return direction;
	}
}
