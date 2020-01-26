package io.github.ladysnake.impersonate.impl;

import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

public interface RecipientAwareText {
    default void impersonateResolve(CommandOutput recipient) {
        throw new UnsupportedOperationException();
    }

    default Text impersonateResolveAll(CommandOutput recipient) {
        throw new UnsupportedOperationException();
    }
}
