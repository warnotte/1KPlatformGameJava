package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.Animation;

public class PlayerAnimationGDX {
    public enum State { IDLE, RUN, JUMP }
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> runAnim;
    private Animation<TextureRegion> jumpAnim;
    private float stateTime = 0f;
    private State currentState = State.IDLE;
    private boolean facingRight = true;
    private float spriteWidth = 24f;
    private float spriteHeight = 32f;

    public PlayerAnimationGDX(float spriteWidth, float spriteHeight) {
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        idleAnim = loadAnimation("Idle/Idle%04d.png", 39, 0.08f);
        runAnim = loadAnimation("RunRight/%04d.png", 39, 0.06f);
        jumpAnim = loadAnimation("Jump/Jump%04d.png", 39, 0.09f);
    }

    private Animation<TextureRegion> loadAnimation(String pattern, int count, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < count; i++) {
            String path = String.format(pattern, i);
            Texture tex = new Texture(Gdx.files.internal(path));
            frames.add(new TextureRegion(tex));
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    public void update(float delta, State state, boolean facingRight) {
        if (state != currentState) {
            currentState = state;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }
        this.facingRight = facingRight;
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame;
        switch (currentState) {
            case RUN:
                frame = runAnim.getKeyFrame(stateTime);
                break;
            case JUMP:
                frame = jumpAnim.getKeyFrame(stateTime);
                break;
            case IDLE:
            default:
                frame = idleAnim.getKeyFrame(stateTime);
                break;
        }
        if (!facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        }
        return frame;
    }

    public float getSpriteWidth() { return spriteWidth; }
    public float getSpriteHeight() { return spriteHeight; }
}
