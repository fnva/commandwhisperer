package net.funiva.commandwhisperer.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(AbstractCommandBlockEditScreen.class)
public interface AbstractCommandBlockEditScreenAccessor {
    @Accessor("commandEdit")
    EditBox getCommandEdit();
}

