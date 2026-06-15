package net.funiva.commandwhisperer.mixin;

import net.funiva.commandwhisperer.CommandBlockOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommandBlockEditScreen.class)
public class CommandBlockEditScreenUIMixin {

    @Shadow @Final private CommandBlockEntity autoCommandBlock;

    @Inject(method = "init", at = @At("TAIL"))
    private void addCustomControls(CallbackInfo ci) {
        CommandBlockEditScreen screen = (CommandBlockEditScreen)(Object)this;
        ScreenAccessor accessor = (ScreenAccessor)(Object)this;

        accessor.invokeAddRenderableWidget(
                Button.builder(Component.literal("cw"), button -> {
                            String currentText = ((AbstractCommandBlockEditScreenAccessor)(Object)this).getCommandEdit().getValue();
                            Minecraft.getInstance().setScreen(
                                    new CommandBlockOptionsScreen(screen, this.autoCommandBlock.getBlockPos(), currentText)
                            );
                        })
                        .bounds(screen.width / 2 + 130, 110, 20, 20)
                        .build()
        );

        ((CommandBlockEditScreenInvoker)(Object)this).invokeEnableControls(true);
    }

}