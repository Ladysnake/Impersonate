package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListS2CPacket.Entry.class)
public interface PlayerListS2CPacketEntryAccessor {
    @Accessor
    void setDisplayName(Text displayName);
    @Accessor
    void setProfile(GameProfile profile);
}
