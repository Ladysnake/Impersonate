package io.github.ladysnake.impersonatest;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public final class Impersonatest implements ModInitializer {

    public static Identifier id(String path) {
        return new Identifier("paltest", path);
    }

    @Override
    public void onInitialize() {
    }

}
