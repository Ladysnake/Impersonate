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
package io.github.ladysnake.impersonate.impl.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.ladysnake.impersonate.impl.ImpersonateTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Type;

@Mixin(Text.Serializer.class)
public class TextSerializerMixin {
    @Inject(method = "serialize(Lnet/minecraft/text/Text;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
        at = @At(value = "FIELD", target = "Lnet/minecraft/text/TextContent;EMPTY:Lnet/minecraft/text/TextContent;", opcode = Opcodes.GETSTATIC),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void serializeImpersonateTexts(Text text, Type type, JsonSerializationContext jsonSerializationContext, CallbackInfoReturnable<JsonElement> cir, JsonObject jsonObject, TextContent textContent) {
        if (textContent instanceof ImpersonateTextContent cnt) {
            jsonObject.addProperty("text", cnt.getString());
            cir.setReturnValue(jsonObject);
        }
    }
}
