package PF1KWax;

/**
 * 
 * @author Warnotte Renaud
 *
 */
public class Entity {
	
	float x,y;

	public synchronized float getX()
	{
		return x;
	}

	public synchronized void setX(float x)
	{
		this.x = x;
	}

	public synchronized float getY()
	{
		return y;
	}

	public synchronized void setY(float y)
	{
		this.y = y;
	}
	
	@Override
	public String toString()
	{
		return "Entity [x=" + x + ", y=" + y + "]";
	}

}

