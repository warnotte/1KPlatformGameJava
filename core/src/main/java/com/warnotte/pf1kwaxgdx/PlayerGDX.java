package com.warnotte.pf1kwaxgdx;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerGDX {

    public float x, y;
    public float vy = 0f; // vitesse verticale
    public boolean isJumping = false;
    public boolean movingLeft = false;
    public boolean movingRight = false;
    private PlayerAnimationGDX anim;


    public PlayerGDX() {
        x = 0;
        y = 30;
        anim = new PlayerAnimationGDX(24, 32); // Taille par défaut, modifiable
    }


    public void render(SpriteBatch batch, float delta) {
        // Choix de l’état d’animation
        PlayerAnimationGDX.State state = PlayerAnimationGDX.State.IDLE;
        if (isJumping || vy != 0) {
            state = PlayerAnimationGDX.State.JUMP;
        } else if (movingLeft || movingRight) {
            state = PlayerAnimationGDX.State.RUN;
        }
        boolean facingRight = !movingLeft || (movingRight && !movingLeft);
        anim.update(delta, state, facingRight);
        TextureRegion frame = anim.getCurrentFrame();
        float w = anim.getSpriteWidth();
        float h = anim.getSpriteHeight();
        batch.draw(frame, x - w/2, y, w, h);
    }

    public void dispose() {
        // Les textures sont gérées par PlayerAnimationGDX
    }
}
