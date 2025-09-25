package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

public class PlayerGDX {
    public float x, y;
    private Texture texture;

    public PlayerGDX() {
        x = 0;
        y = 30;
        texture = new Texture("Idle/Idle0000.png"); // À remplacer par une animation plus tard
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    public void dispose() {
        texture.dispose();
    }
}
