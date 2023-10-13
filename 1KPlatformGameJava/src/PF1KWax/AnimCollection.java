/**
 * 
 */
package PF1KWax;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * @author Warnotte Renaud
 *
 */
public class AnimCollection
{
	List<BufferedImage> bis = new ArrayList<>();
	
	public AnimCollection(String directory, String filepattern, int cpt) throws IOException
	{
		for (int i = 0; i < cpt; i++)
		{
			String filetoread = String.format("%s\\"+String.format(filepattern, i), directory);
			System.err.printf("file to read %s\r\n", filetoread);
			BufferedImage bi = ImageIO.read(new File(filetoread));
			bis.add(bi);
		}
		
	}

	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int size()
	{
		return bis.size();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public BufferedImage get(int index)
	{
		return bis.get(index%bis.size());
	}
	
	
}
