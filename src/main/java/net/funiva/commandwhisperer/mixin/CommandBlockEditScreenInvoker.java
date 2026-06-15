package net.funiva.commandwhisperer.mixin;

import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CommandBlockEditScreen.class)
public interface CommandBlockEditScreenInvoker {
    @Invoker("enableControls")
    void invokeEnableControls(boolean state);
}
