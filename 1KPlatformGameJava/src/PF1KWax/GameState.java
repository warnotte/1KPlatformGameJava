package PF1KWax;

import PF1KWax.Case.TypeCase;
import PF1KWax.Player.State;

/**
 * @author Warnotte Renaud
 */
public class GameState
{
	Player player = null;
	Level lvl = null;

	long previousTimer = System.currentTimeMillis();
	private int Level;

	private int length_or_difficulty = 3;
	String currentMessage = "START";
	long StartMessage = 0;
	
	/*public static void main(String args[]) throws InterruptedException
	{
		GameState gs = new GameState();
		while (true)
		{
			Thread.sleep(50);
			gs.evolue();
		}

	}*/

	public GameState()
	{
		lvl = new LevelGenerator_Simple(length_or_difficulty).generateLevel();
		player = new Player();
	}

	public void goLeft()
	{
		// Test si on px ou pas aller a gauche si oui alors vas-y.

		Case player_case = getLvl().getCase(player.getX());
		Case other_case = getLvl().getCase(player.getX() + 1);

		if (other_case != null)
		{
			if (player_case == other_case)
				this.player.x += 1;
			else
			{
				if (other_case.Hauteur - player.y < 2)
				{
					this.player.x += 1;
				}
			}
		}

	}

	public void goRight()
	{
		Case player_case = getLvl().getCase(player.getX());
		Case other_case = getLvl().getCase(player.getX() - 1);

		if (other_case != null)
		{
			if (player_case == other_case)
				this.player.x -= 1;
			else
			{
				if (other_case.Hauteur - player.y < 2)
				{
					this.player.x -= 1;
				}
			}
		}
	}

	public void goJump()
	{
		if (player.state == Player.State.IDLE)
		{
			player.startJump = System.currentTimeMillis();
			player.state = State.JUMP;
		}
	}

	public void evolue()
	{
		long actTimer = System.currentTimeMillis();
		long elapsed = actTimer - previousTimer;
		float float_elapsed = (float) elapsed;
		elapsed = actTimer - player.startJump;
		float jump_elapsed = (float) elapsed;

		Case player_case = getLvl().getCase(player.getX());

		float deltaY = player.y - player_case.Hauteur;

		if (player.y <= 0)
		{
			setMessage("DIED");
			resetPlayer();
		}
		if (this.player.x >= getLvl().getNbrCases() * getLvl().CaseWidth - getLvl().CaseWidth / 2)
		{
			setMessage("FINISHED");
			augmenteDifficulty();
			resetPlayer();
		}

		System.err.println("PY [" + player.y + "] VS CY [" + player_case.Hauteur + "]");

		// Est-ce que le player est sur un truc ou dans l'air ?
		if ((player.state == Player.State.JUMP) && (jump_elapsed < 750))
		{
			player.y += (750 - jump_elapsed) * 0.000981 * 2;
		} else if (deltaY <= 0.1)
		{
			player.state = Player.State.IDLE;
			player.y = player_case.Hauteur;
		} else
		// si le player "est au dessus de la case" et qu'il tombe.
		if (player.y > player_case.Hauteur)
		{
			player.state = Player.State.FALL;
			player.y -= float_elapsed * 0.02 * 2;
			if (player.y<player_case.Hauteur) player.y=player_case.Hauteur; // Pr pas qu'il passe a travers 
		} else
		{

			//	player.y+=float_elapsed*9.81;
		}

		if (player.state == State.IDLE)
		{
			if (player_case.type == TypeCase.DYNAMIC)
			{
				player_case.Hauteur -= 0.5;
				player.y = player_case.Hauteur;
			}

		}

		//	System.err.println("Player : "+ player);
		previousTimer = actTimer;
	}

	/**
	 * 
	 */
	private void augmenteDifficulty()
	{
		setLevel(getLevel() + 1);
		lvl = new LevelGenerator_Simple(length_or_difficulty +=3).generateLevel();
	}

	/**
	 * 
	 */
	private void resetPlayer()
	{
		player.x = 0;
		player.y = 10;
	}
	
	public synchronized Player getPlayer()
	{
		return player;
	}

	public synchronized void setPlayer(Player player)
	{
		this.player = player;
	}

	public synchronized Level getLvl()
	{
		return lvl;
	}

	public synchronized void setLvl(Level lvl)
	{
		this.lvl = lvl;
	}

	public void setMessage(String message)
	{
		StartMessage = System.currentTimeMillis();
		currentMessage = message;
	}

	public int getLevel()
	{
		return Level;
	}

	public void setLevel(int level)
	{
		Level = level;

	}

}
