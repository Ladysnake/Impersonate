package org.ladysnake.impersonate.impl;

import net.minecraft.server.command.CommandOutput;

public interface ImpersonateCommandOutput extends CommandOutput {
    boolean impersonate$shouldRevealName();
}
