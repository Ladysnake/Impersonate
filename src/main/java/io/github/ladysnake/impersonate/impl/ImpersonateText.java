package io.github.ladysnake.impersonate.impl;

import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ImpersonateText extends LiteralText implements RecipientAwareText {
    private final String trueText;
    private final String fakedText;
    private boolean revealed;

    public static Text get(PlayerEntity player) {
        Impersonator impersonator = Impersonator.get(player);
        String fakeName = impersonator.getEditedProfile().getName();
        String trueText = String.format("%s (%s)", fakeName, player.getGameProfile().getName());
        return new ImpersonateText(trueText, fakeName, false);
    }

    public ImpersonateText(String trueText, String fakedText, boolean revealed) {
        super(fakedText);
        this.trueText = trueText;
        this.fakedText = fakedText;
        this.revealed = revealed;
    }

    @Override
    public void impersonateResolve(CommandOutput recipient) {
        revealed = !(recipient instanceof PlayerEntity) || shouldBeRevealedBy((PlayerEntity) recipient);
    }

    public static boolean shouldBeRevealedBy(PlayerEntity player) {
        return player instanceof ServerPlayerEntity
            && player.world.getGameRules().getBoolean(ImpersonateGamerules.OP_REVEAL_IMPERSONATIONS)
            && ((ServerPlayerEntity) player).server.getPlayerManager().isOperator(player.getGameProfile());
    }

    @Override
    public String asString() {
        return this.getRawString();
    }

    @Override
    public String getRawString() {
        return this.revealed ? this.trueText : this.fakedText;
    }

    @Override
    public ImpersonateText copy() {
        return new ImpersonateText(this.trueText, this.fakedText, this.revealed);
    }

}
