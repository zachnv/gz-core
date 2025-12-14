package io.github.groundzero.utils;

import com.badlogic.gdx.Input;

/**
Input Handler
 */
public class InputHandler {

    public static boolean isKeyPressed(int key) {
        return com.badlogic.gdx.Gdx.input.isKeyPressed(key);
    }
}
