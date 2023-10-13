package PF1KWax;

/**
 * @author Warnotte Renaud
 *
 */
public class Case
{
	float Hauteur;
	
	public enum TypeCase {STATIC, HOLE, DYNAMIC};
	
	TypeCase type = TypeCase.STATIC;
	
	boolean isTree = false;

	public Case(float hauteur)
	{
		super();
		Hauteur = hauteur;
	}
}
