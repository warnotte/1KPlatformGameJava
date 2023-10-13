package PF1KWax;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Warnotte Renaud
 *
 */
public class Panel_Vue extends JPanel implements KeyListener
{

	GameState gs = null;
	
	private static boolean	_RIGHT;
	private static boolean	_LEFT;
	private static boolean	_UP;
	
	Random colorRandom = new Random();
	
	AnimCollection anim_idle = null;
	AnimCollection anim_walk = null;
	AnimCollection anim_up = null;

	private int	ANIMOFFS;
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7638481349578164085L;

	/**
	 * Create the panel.
	 * @throws IOException 
	 */
	public Panel_Vue() throws IOException
	{
		gs = new GameState();
		this.addKeyListener(this);
		this.requestFocus();
		gs.setMessage("Start");
		
		anim_idle = new AnimCollection("Idle","Idle%04d.png", 38);
		anim_walk = new AnimCollection("RunRight","%04d.png", 38);
		anim_up= new AnimCollection("Jump","Jump%04d.png", 38);
	}

	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		long time = System.currentTimeMillis()/10;
		long elapsed = System.currentTimeMillis()-gs.StartMessage;
		
		AffineTransform oldt = g2.getTransform();
		
		g.setColor(new Color(204, 238, 255));
		g.fillRect(this.getX(),this.getY(),this.getWidth(),this.getHeight());
	
		colorRandom.setSeed(1234);

		for (int i = 0; i < 64; i++)
		{
			int x = (int)(i*32+time)/2;
			x=x%660;
			drawSea(g2, x+640,(int)(298+Math.sin((i*8000+x)/50f)*5f+Math.cos((i*3000+x)/50f)*5f));
			
		}
		
		AffineTransform at = new AffineTransform();
		at.translate(200, 400);
		at.rotate(Math.toRadians(180));
		at.scale(-2, 2);
		
		
		
		g2.setTransform(at);
		
		
		g.translate(0,  50);
		
		g.translate((int)-gs.player.x+50, 0);
		
		for (int i = 0; i < gs.getLvl().cases.size(); i++)
		{
			Case c = gs.getLvl().cases.get(i);
			drawCase(g2, i*gs.getLvl().CaseWidth, c.Hauteur, c);
		}
		g.translate((int)gs.player.x-50, 0);
		
		g.setColor(Color.RED);
		g.fillRect(50-1, (int)gs.getPlayer().y, 3, 3);
		
		if (_UP)
			g.drawImage(anim_up.get(ANIMOFFS++), 50-1-16, (int)gs.getPlayer().y+32, 32, -32, this);
		else
		if ((_LEFT) || (_RIGHT))
			g.drawImage(anim_walk.get(ANIMOFFS++), 50-1-16, (int)gs.getPlayer().y+32, 32, -32, this);
		else
			g.drawImage(anim_idle.get(ANIMOFFS++), 50-1-16, (int)gs.getPlayer().y+32, 32, -32, this);
		
		
		g.translate(0,  -50);
		
		g2.setTransform(oldt);
		
		
		
		
		for (int i = 0; i < 64; i++)
		{
			int x = (int) (i*16+time);
			x=x%660;
			drawNuage(g2, x+640,(int)(200+Math.sin((i*5000+x)/50f)*10f+Math.cos((i*2000+x)/50f)*10f));
			x = (int) (i*24+time);
			x=x%660;
			drawNuage(g2, x+640,(int)(200+Math.sin((i*8000+x)/50f)*10f+Math.cos((i*3000+x)/50f)*10f));
			
		}
		for (int i = 0; i < 64; i++)
		{
			int x = (int)(i*64+time)/5;
			x=x%660;
			drawSea(g2, x+640,(int)(298+Math.sin((i*8000+x)/50f)*5f+Math.cos((i*3000+x)/50f)*5f));
			
		}
		
		
		if (elapsed<2000)
			g2.drawString(gs.currentMessage, 100, 100-elapsed/100);
		g2.drawString("Level : "+gs.getLevel(), 100, 10);
		
	}
	
	private void drawNuage(Graphics2D g, int x, int y)
	{
		g.setColor(Color.WHITE);
		Color intl = new Color(0.9f+getR(0.1f),0.9f+getR(0.1f),0.9f+getR(0.1f), 1.0f);
		Color ext =new Color(0.9f+getR(0.1f),0.9f+getR(0.1f),0.9f+getR(0.1f), 0.0f);
		
		dessineBille(g, x,y, 36, intl, ext);
		dessineBille(g, x+18,y+4, 24, intl, ext);
		dessineBille(g, x-12,y+4, 24, intl, ext);
	
	}
	private void drawSea(Graphics2D g, int x, int y)
	{
		
		Color intl = new Color(0.1f+getR(0.2f), 0.2f+getR(0.2f), 0.9f+getR(0.1f), 1.0f);
		Color ext =new Color(0.2f+getR(0.2f), 0.1f+getR(0.2f), 0.9f+getR(0.1f), 0.0f);
		
		dessineBille(g, x,y, 36, intl, ext);
		dessineBille(g, x+18,y+4, 24, intl, ext);
		dessineBille(g, x-12,y+4, 24, intl, ext);
	
	}
	
	private float getR(float f) {
		return colorRandom.nextFloat()*f;
	}

	/**
	 * @param g
	 * @param x
	 * @param y
	 */
	private void dessineBille(Graphics2D g, int x, int y, int sz, Color intl, Color ext)
	{
		Paint oldp = g.getPaint();
		RadialGradientPaint p= new RadialGradientPaint(
				new Point2D.Double(x+sz/2, y+sz/2), sz/2,
                new float[] { 0.0f, 1.0f },
                new Color[] {intl,
					
                    ext
                   });
		g.setPaint(p);
		g.fillOval(x, y, sz,sz);
		g.setPaint(oldp);
	}

	/**
	 * @param f
	 * @param hauteur
	 * @param c
	 */
	private void drawCase(Graphics2D g, float x, float y, Case c)
	{
		if ((c.type != c.type.HOLE) && (c.Hauteur > 1))
		{
			g.setColor(new Color(204, 170, 102, 255));
			g.fillRect((int) x, 0, (int) gs.getLvl().CaseWidth, (int) c.Hauteur);

			if (c.type == c.type.STATIC)
				g.setColor(Color.green);
			else
				g.setColor(Color.red);

			GradientPaint redtowhite = new GradientPaint(0, (int) c.Hauteur - 3, new Color(204, 170, 102, 255), 0, (int) c.Hauteur, g.getColor());
			g.setPaint(redtowhite);

			g.fillRect((int) x, (int) c.Hauteur - 3, (int) gs.getLvl().CaseWidth, 3);
			g.setPaint(Color.black);
		}
		
		if (c.isTree==true)
		{
			Random rand = new Random();
			rand.setSeed(c.hashCode());
			int sz = 4+rand.nextInt(8);
			g.setColor(new Color(200,127,32));
			g.fillRect((int)(x + gs.getLvl().CaseWidth/2)-1, (int)c.Hauteur, 2, sz+1);
			
			g.setColor(Color.GREEN);
			
			y = c.Hauteur+sz/2;
			g.fillOval((int)(x-sz/2 + gs.getLvl().CaseWidth/2), (int)y, sz, sz);
			
		}
	}

	public static void main(String args[]) throws IOException
	{
		JFrame frame = new JFrame();
		
		final Panel_Vue panel = new Panel_Vue();
		frame.addKeyListener(panel);
		frame.add(panel, BorderLayout.CENTER);
		frame.setSize(640,480);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		 int delay = 25; //milliseconds
		  ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		    	  System.err.println("LEFT == "+_LEFT);
		    	  System.err.println("RIGHT == "+_RIGHT);
		    	  if (_LEFT==true)
		    	  {
		    		  panel.gs.goRight();
		    	  }
		    	  if (_RIGHT==true)
		    	  {
		    		  panel.gs.goLeft();
		    	  } 
		    	  if (_UP==true)
		    	  {
		    		  panel.gs.goJump();
		    	  }
		  			
		    	  
		    	  panel.gs.evolue();
		          panel.repaint();
		      }
		  };
		  new Timer(delay, taskPerformer).start();
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	
	public void keyPressed(KeyEvent arg0)
	{
		if (arg0.getKeyCode() == arg0.VK_LEFT)
			_LEFT=true;	
		if (arg0.getKeyCode() == arg0.VK_RIGHT)
			_RIGHT=true;
		if (arg0.getKeyCode() == arg0.VK_UP)
			_UP=true;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	
	public void keyReleased(KeyEvent arg0)
	{
		if (arg0.getKeyCode() == arg0.VK_LEFT)
			_LEFT=false;
		if (arg0.getKeyCode() == arg0.VK_RIGHT)
			_RIGHT=false;
		if (arg0.getKeyCode() == arg0.VK_UP)
			_UP=false;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
}
