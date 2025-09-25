
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public class GameStateGDX {
    public PlayerGDX player;
    public LevelGDX level;
    public GameStateGDX() {
        player = new PlayerGDX();
        level = new LevelGeneratorSimpleGDX(25).generateLevel();
    }
}
