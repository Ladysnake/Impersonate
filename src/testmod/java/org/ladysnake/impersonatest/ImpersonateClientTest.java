package org.ladysnake.impersonatest;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestServerConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.elmendorf.impl.MockClientConnection;
import org.ladysnake.impersonate.impl.ImpersonateGamerules;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ImpersonateClientTest implements FabricClientGameTest {

    public static final String MOCK_PLAYER_NAME = "test-mock-player";

    @Override
    public void runTest(ClientGameTestContext context) {
        GameProfile profile = context.computeOnClient(MinecraftClient::getGameProfile);
        try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
            try (TestServerConnection connection = server.connect()) {
                server.runOnServer(s -> s.getPlayerManager().addToOperators(profile));
                connection.getClientWorld().waitForChunksRender();
                context.runOnClient(client -> client.options.setPerspective(Perspective.THIRD_PERSON_FRONT));
                sendChatMessage(context);
                context.takeScreenshot("1_before_impersonation", 5);
                context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as Pyrofab"));
                context.waitTicks(20);
                sendChatMessage(context);
                context.takeScreenshot("2_impersonating_pyrofab", 5);
                context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as doctor4t"));
                context.waitTicks(20);
                sendChatMessage(context);
                context.takeScreenshot("3_impersonating_doctor4t", 5);
                Vec3d newPlayerPos = context.computeOnClient(mc -> mc.player.getPos().add(2, 0, 1));
                UUID otherPlayerId = UUID.randomUUID();
                ServerPlayerEntity otherPlayer = server.computeOnServer(s -> spawnServerPlayer(s.getOverworld(), newPlayerPos, otherPlayerId, MOCK_PLAYER_NAME));
                server.runOnServer(s ->
                    otherPlayer.networkHandler.onChatMessage(createChatMessagePacket(otherPlayerId, "Hi"))
                );
                context.waitTicks(20);
                context.takeScreenshot("4_other_player_joined", 5);
                context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as Xiribidus " + MOCK_PLAYER_NAME));
                context.waitTicks(20);
                server.runOnServer(s ->
                    otherPlayer.networkHandler.onChatMessage(createChatMessagePacket(otherPlayerId, "Hi again"))
                );
                context.takeScreenshot("5_other_player_impersonating_xiribidus", 5);
                server.runOnServer(s -> s.getGameRules().get(ImpersonateGamerules.OP_REVEAL_IMPERSONATIONS).set(false, s));
                server.runOnServer(s ->
                    otherPlayer.networkHandler.onChatMessage(createChatMessagePacket(otherPlayerId, "Goodbye"))
                );
                context.takeScreenshot("6_other_player_impersonating_xiribidus_no_reveal", 5);
                server.runOnServer(s -> otherPlayer.networkHandler.onDisconnected(new DisconnectionInfo(Text.empty())));
                context.takeScreenshot("7_other_player_impersonating_xiribidus_no_reveal_disconnected", 5);
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
        ServerPlayerEntity mockPlayer = playerManager.createPlayer(profile, clientOptions);
        playerManager.onPlayerConnect(connection, mockPlayer, ConnectedClientData.createDefault(profile, false));
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
