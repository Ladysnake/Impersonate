/*
 * Impersonate
 * Copyright (C) 2020-2022 Ladysnake
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
package io.github.ladysnake.impersonatest;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.elmendorf.ElmendorfTestContext;
import io.github.ladysnake.elmendorf.GameTestUtil;
import io.github.ladysnake.elmendorf.impl.MockClientConnection;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateTextContent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.DecoratedContents;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageMetadata;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Identifier;

import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public class ImpersonateTestSuite implements FabricGameTest {

    public static final Identifier IMPERSONATION_KEY = new Identifier("impersonatest", "key");

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nameChanges(TestContext ctx) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "impersonated");
        ServerPlayerEntity player = ((ElmendorfTestContext) ctx).spawnServerPlayer(1, 0, 1);
        Text formerName = player.getDisplayName();
        Impersonator impersonator = player.getComponent(Impersonate.IMPERSONATION);
        impersonator.impersonate(IMPERSONATION_KEY, profile);
        GameTestUtil.assertTrue("Expected player to have name 'impersonated', was %s".formatted(player.getDisplayName()), "impersonated".equals(player.getDisplayName().getString()));
        impersonator.stopImpersonation(IMPERSONATION_KEY);
        GameTestUtil.assertTrue("Expected player to have name %s, was %s".formatted(formerName, player.getDisplayName()), formerName.equals(player.getDisplayName()));
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nameGetsRevealed(TestContext ctx) {
        ServerPlayerEntity player = ((ElmendorfTestContext) ctx).spawnServerPlayer(1, 0, 1);
        TextContent textContent = ImpersonateTextContent.get(player);
        ctx.getWorld().getServer().sendMessage(Text.translatable("a", MutableText.of(textContent)));
        GameTestUtil.assertTrue("Text content should be revealed", ((ImpersonateTextContent) textContent).isRevealed());
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nameInChatGetsRevealed(TestContext ctx) throws NetworkEncryptionException {
        // Do the bare minimum to simulate a legit client with a valid keypair
        KeyPair keyPair = NetworkEncryptionUtils.generateServerKeyPair();
        Signer signer = Signer.create(keyPair.getPrivate(), "SHA256withRSA");
        MessageChain.Packer messagePacker = new MessageChain().getPacker();
        LastSeenMessageList lastSeenMessages = LastSeenMessageList.EMPTY;
        ServerPlayerEntity player = new ServerPlayerEntity(
            ctx.getWorld().getServer(),
            ctx.getWorld(),
            new GameProfile(UUID.randomUUID(), "test-mock-player"),
            new PlayerPublicKey(
                new PlayerPublicKey.PublicKeyData(
                    Instant.now().plus(42, ChronoUnit.HALF_DAYS),
                    keyPair.getPublic(),
                    new byte[0] // this would absolutely be thrown out if the player logged in, but we aren't doing that
                )
            )
        );
        player.networkHandler = new ServerPlayNetworkHandler(
            ctx.getWorld().getServer(),
            new MockClientConnection(NetworkSide.CLIENTBOUND),
            player
        );
        Impersonator.get(player).impersonate(IMPERSONATION_KEY, new GameProfile(UUID.randomUUID(), "impersonated"));
        DecoratedContents text = new DecoratedContents("Hi");
        MessageMetadata metadata = MessageMetadata.of(player.getUuid());
        PlayerManager playerManager = ctx.getWorld().getServer().getPlayerManager();
        ServerPlayerEntity otherPlayer = ((ElmendorfTestContext) ctx).spawnServerPlayer(1, 0, 1);

        try {
            playerManager.getPlayerList().add(player);
            playerManager.getPlayerList().add(otherPlayer);
            playerManager.addToOperators(player.getGameProfile());
            player.networkHandler.onChatMessage(new ChatMessageC2SPacket(
                text.plain(),
                metadata.timestamp(),
                metadata.salt(),
                messagePacker.pack(signer, metadata, text, lastSeenMessages).signature(),
                false,
                new LastSeenMessageList.Acknowledgment(lastSeenMessages, Optional.empty())
            ));
            ((ElmendorfTestContext) ctx).verifyConnection(player, conn -> conn.sent(
                ChatMessageS2CPacket.class,
                chatPacket -> chatPacket.serializedParameters().name().getString()
                    .equals("impersonated(test-mock-player)"))
            );
            ((ElmendorfTestContext) ctx).verifyConnection(otherPlayer, conn -> conn.sent(
                ChatMessageS2CPacket.class,
                chatPacket -> chatPacket.serializedParameters().name().getString()
                    .equals("impersonated"))
            );
            ctx.complete();
        } finally {
            playerManager.removeFromOperators(player.getGameProfile());
            playerManager.getPlayerList().remove(player);
            playerManager.getPlayerList().remove(otherPlayer);
        }
    }
}
