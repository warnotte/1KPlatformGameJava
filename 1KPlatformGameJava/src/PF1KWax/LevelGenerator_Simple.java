package PF1KWax;

/**
 * @author Warnotte Renaud
 *
 */
public class LevelGenerator_Simple implements LevelGeneratorInterface
{
	int len = 25;
	
	LevelGenerator_Simple(int len)
	{
		this.len = len;
	}
	
	public Level generateLevel()
	{
		
		int MAX_HEIGHT_DELTA = 15;
		
		Level lvl = new Level();
		boolean prevHole = false;
		
		for (int i = 0; i < len; i++)
		{
			
			float h = (float) (0 + (Math.random() * 20));
			
			if ((prevHole==false) && (Math.random()<0.3))
			{
				h=0;
				prevHole=true;
			}
			else
				prevHole=false;
			
			if (h>MAX_HEIGHT_DELTA) h=MAX_HEIGHT_DELTA;
			
			if (i==0) h = 10;
			if (i==len-1) h = 10;
			
			Case ca = new Case(h);
			
			if (h>2) ca.isTree = (Math.random()>=0.5)?true:false;
			
			lvl.cases.add(ca);
			if (Math.random()>0.8)
				ca.type=ca.type.DYNAMIC;
			
			if (h==0) ca.type=ca.type.HOLE;
			
			if (i==0) ca.type=ca.type.STATIC;
			if (i==len-1) ca.type=ca.type.STATIC;
			
			
		}
		
		return lvl;
	}
}
