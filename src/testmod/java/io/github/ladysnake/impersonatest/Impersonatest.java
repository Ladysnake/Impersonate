package io.github.ladysnake.impersonatest;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class Impersonatest implements ModInitializer {

    public static Identifier id(String path) {
        return new Identifier("impersonatest", path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, id("impersonitem"), new ImpersonItem(new Item.Settings()));
    }

}
