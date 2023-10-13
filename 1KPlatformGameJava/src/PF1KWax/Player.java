package PF1KWax;


/**
 * @author Warnotte Renaud
 *
 */
public class Player extends Entity
{
	
	
	public enum State {IDLE, FALL, JUMP};
	
	State state = State.IDLE;
	
	public long	startJump;

	public Player()
	{
		y=30;
	}

	@Override
	public String toString()
	{
		return "Player [x=" + x + ", y=" + y + ", state=" + state + "]";
	}
	
}
