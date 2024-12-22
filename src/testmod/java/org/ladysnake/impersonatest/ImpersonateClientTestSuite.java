package org.ladysnake.impersonatest;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImpersonateClientTestSuite implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.waitForScreen(TitleScreen.class);
        context.clickScreenButton("menu.singleplayer");

        if (!isDirEmpty(FabricLoader.getInstance().getGameDir().resolve("saves"))) {
            context.waitForScreen(SelectWorldScreen.class);
            context.clickScreenButton("selectWorld.create");
        }

        context.waitForScreen(CreateWorldScreen.class);
        context.runOnClient(client -> CreateWorldScreen.showTestWorld(client, client.currentScreen));
        context.waitForScreen(CreateWorldScreen.class);
        context.clickScreenButton("selectWorld.create");

        context.waitFor(client -> client.player != null && !(client.currentScreen instanceof LevelLoadingScreen), 30 * SharedConstants.TICKS_PER_MINUTE);
        context.waitTicks(20); // a few more ticks for world loading

        context.runOnClient(client -> client.options.setPerspective(Perspective.THIRD_PERSON_FRONT));
        sendChatMessage(context, "before_impersonation");
        context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as Pyrofab"));
        context.waitTicks(20);
        sendChatMessage(context, "impersonating_pyrofab");
        context.runOnClient(client -> client.getNetworkHandler().sendChatCommand("impersonate disguise as doctor4t"));
        context.waitTicks(20);
        sendChatMessage(context, "impersonating_doctor4t");

        context.setScreen(() -> new GameMenuScreen(true));
        context.clickScreenButton("menu.returnToMenu");
    }

    private static void sendChatMessage(ClientGameTestContext context, String screenshotName) {
        context.getInput().pressKey(options -> options.chatKey);
        context.waitTick();
        context.getInput().typeChars("Hello, World!");
        context.getInput().pressKey(InputUtil.GLFW_KEY_ENTER);
        context.takeScreenshot(screenshotName, 5);
    }

    private static boolean isDirEmpty(Path path) {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
            return !directory.iterator().hasNext();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
