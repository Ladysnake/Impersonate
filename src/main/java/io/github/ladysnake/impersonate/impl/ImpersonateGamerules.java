package io.github.ladysnake.impersonate.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.mixin.GameRulesAccessor;
import io.github.ladysnake.impersonate.impl.mixin.RuleTypeAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.util.function.BiConsumer;

public final class ImpersonateGamerules {
    public static final GameRules.RuleKey<GameRules.BooleanRule> FAKE_CAPES =
        register("impersonate:fakeCapes", createBooleanRule(false, (server, rule) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ((PlayerImpersonator) Impersonator.get(player)).sync();
            }
        }));

    public static final GameRules.RuleKey<GameRules.BooleanRule> OP_REVEAL_IMPERSONATIONS =
        register("impersonate:opRevealImpersonations", createBooleanRule(true, (server, rule) -> {}));

    @SuppressWarnings("unchecked")
    public static GameRules.RuleType<GameRules.BooleanRule> createBooleanRule(boolean initialValue, BiConsumer<MinecraftServer, GameRules.BooleanRule> changeCallback) {
        return RuleTypeAccessor.invokeNew(BoolArgumentType::bool, type -> new GameRules.BooleanRule((GameRules.RuleType<GameRules.BooleanRule>) type, initialValue), changeCallback);
    }

    public static <T extends GameRules.Rule<T>> GameRules.RuleKey<T> register(String name, GameRules.RuleType<T> type) {
        return GameRulesAccessor.invokeRegister(name, type);
    }

    public static void init() {
        // NO-OP
    }
}
