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
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateTextContent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class ImpersonateTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nameChanges(TestContext ctx) {
        Identifier key = new Identifier("impersonatest", "key");
        GameProfile profile = new GameProfile(UUID.randomUUID(), "impersonator");
        ServerPlayerEntity player = ((ElmendorfTestContext) ctx).spawnServerPlayer(1, 0, 1);
        Text formerName = player.getDisplayName();
        Impersonator impersonator = player.getComponent(Impersonate.IMPERSONATION);
        impersonator.impersonate(key, profile);
        GameTestUtil.assertTrue("Expected player to have name \"impersonator\", was %s".formatted(player.getDisplayName()), "impersonator".equals(player.getDisplayName().getString()));
        impersonator.stopImpersonation(key);
        GameTestUtil.assertTrue("Expected player to have name %s, was %s".formatted(formerName, player.getDisplayName()), formerName.equals(player.getDisplayName()));
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void nameGetsRevealed(TestContext ctx) {
        ServerPlayerEntity player = ((ElmendorfTestContext) ctx).spawnServerPlayer(1, 0, 1);
        TextContent textContent = ImpersonateTextContent.get(player);
        ctx.getWorld().getServer().sendMessage(Text.translatable("a", textContent));
        GameTestUtil.assertTrue("Text content should be revealed", ((ImpersonateTextContent) textContent).isRevealed());
        ctx.complete();
    }
}
