package com.warnotte.pf1kwaxgdx.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.warnotte.pf1kwaxgdx.GameMain;

public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PF1KWax GDX");
        config.setWindowedMode(800, 600);
        new Lwjgl3Application(new GameMain(), config);
    }
}
