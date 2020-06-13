package io.github.ladysnake.impersonate.impl.mixin;

import io.github.ladysnake.impersonate.impl.RecipientAwareText;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(MutableText.class)
public interface MutableTextMixin extends Text, RecipientAwareText {
    @Override
    default Text impersonateResolveAll(CommandOutput recipient) {
        this.impersonateResolve(recipient);
        List<Text> siblings = this.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            Text sibling = siblings.get(i);
            siblings.set(i, ((RecipientAwareText) sibling).impersonateResolveAll(recipient));
        }
        return this;
    }
}
