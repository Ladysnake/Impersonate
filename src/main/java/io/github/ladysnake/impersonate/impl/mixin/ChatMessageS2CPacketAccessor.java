package io.github.ladysnake.impersonate.impl.mixin;

import net.minecraft.client.network.packet.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatMessageS2CPacket.class)
public interface ChatMessageS2CPacketAccessor {
    @Accessor
    Text getMessage();

    @Accessor
    void setMessage(Text message);
}
