/*
 * Impersonate
 * Copyright (C) 2020-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */

package org.ladysnake.impersonatest;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.PrepareSpawnTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.elmendorf.impl.MockClientConnection;
import org.ladysnake.impersonate.impl.ImpersonateGamerules;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class ImpersonateClientTest implements FabricClientGameTest {

    public static final String MOCK_PLAYER_NAME = "test-mock-player";

    @Override
    public void runTest(ClientGameTestContext context) {
        GameProfile profile = context.computeOnClient(MinecraftClient::getGameProfile);
        try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
            try (TestServerConnection connection = server.connect()) {
                server.runOnServer(s -> s.getPlayerManager().addToOperators(new PlayerConfigEntry(profile)));
                connection.getClientWorld().waitForChunksRender();
                context.runOnClient(client -> client.options.setPerspective(Perspective.THIRD_PERSON_FRONT));
                sendChatMessage(context);
                context.takeScreenshot("1_before_impersonation");
                context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as Pyrofab"));
                context.waitTicks(20);
                sendChatMessage(context);
                context.takeScreenshot("2_impersonating_pyrofab");
                context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as doctor4t"));
                context.waitTicks(20);
                sendChatMessage(context);
                context.takeScreenshot("3_impersonating_doctor4t");
                Vec3d newPlayerPos = context.computeOnClient(mc -> mc.player.getEntityPos().add(2, 0, 1));
                UUID otherPlayerId = UUID.randomUUID();
                ServerPlayerEntity otherPlayer = server.computeOnServer(s -> spawnServerPlayer(s.getOverworld(), newPlayerPos, otherPlayerId, MOCK_PLAYER_NAME));
                server.runOnServer(s ->
                    otherPlayer.networkHandler.onChatMessage(createChatMessagePacket(otherPlayerId, "Hi"))
                );
                context.waitTicks(20);
                context.takeScreenshot("4_other_player_joined");
                context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as Xiribidus " + MOCK_PLAYER_NAME));
                context.waitTicks(20);
                server.runOnServer(s ->
                    otherPlayer.networkHandler.onChatMessage(createChatMessagePacket(otherPlayerId, "Hi again"))
                );
                context.takeScreenshot("5_other_player_impersonating_xiribidus");
                server.runOnServer(s -> s.getSaveProperties().getGameRules().setValue(ImpersonateGamerules.OP_REVEAL_IMPERSONATIONS, false, s));
                server.runOnServer(s ->
                    otherPlayer.networkHandler.onChatMessage(createChatMessagePacket(otherPlayerId, "Goodbye"))
                );
                context.takeScreenshot("6_other_player_impersonating_xiribidus_no_reveal");
                server.runOnServer(s -> otherPlayer.networkHandler.onDisconnected(new DisconnectionInfo(Text.empty())));
                context.takeScreenshot("7_other_player_impersonating_xiribidus_no_reveal_disconnected");
                context.waitTicks(100);
            }
        }
    }

    private static @NotNull ChatMessageC2SPacket createChatMessagePacket(UUID otherPlayerId, String text) {
        try {
            return ImpersonateTestSuite.createChatMessagePacket(otherPlayerId, text);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerPlayerEntity spawnServerPlayer(ServerWorld world, Vec3d pos, UUID uuid, String name) {
        GameProfile profile = new GameProfile(uuid, name);
        var connection = new MockClientConnection(NetworkSide.SERVERBOUND);
        SyncedClientOptions clientOptions = SyncedClientOptions.createDefault();
        PlayerManager playerManager = world.getServer().getPlayerManager();
        PrepareSpawnTask prepareSpawnTask = new PrepareSpawnTask(world.getServer(), new PlayerConfigEntry(profile));
        prepareSpawnTask.sendPacket(packet -> {});
        prepareSpawnTask.hasFinished();
        ServerPlayerEntity mockPlayer = prepareSpawnTask.onReady(connection, ConnectedClientData.createDefault(profile, false));
        mockPlayer.setPosition(pos);
        return mockPlayer;
    }


    private static void sendChatMessage(ClientGameTestContext context) {
        context.getInput().pressKey(options -> options.chatKey);
        context.waitTick();
        context.getInput().typeChars("Hello, World!");
        context.getInput().pressKey(InputUtil.GLFW_KEY_ENTER);
    }
}
