package org.ladysnake.impersonate.impl.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.impersonate.impl.ImpersonateCommandOutput;
import org.ladysnake.impersonate.impl.ImpersonateTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$3")
public abstract class ServerPlayerEntity$CommandOutput implements ImpersonateCommandOutput {
    @Shadow
    @Final
    ServerPlayerEntity field_54403;

    @Override
    public boolean impersonate$shouldRevealName() {
        return ImpersonateTextContent.shouldBeRevealedBy(this.field_54403);
    }
}
