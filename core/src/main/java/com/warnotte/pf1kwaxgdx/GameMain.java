package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Game;

public class GameMain extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
