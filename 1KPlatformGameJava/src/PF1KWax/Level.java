package PF1KWax;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Warnotte Renaud
 *
 */
public class Level
{

	List<Case> cases = new ArrayList<Case>();
	
	float CaseWidth = 30;
	
	public Case getCase(float x)
	{
		int idx = (int)x/(int)CaseWidth;
		if (idx<cases.size())
		return cases.get(idx);
		return null;
	}

	/**
	 * @return
	 */
	public int getNbrCases()
	{
		return cases.size();
	}
	
}
