package net.funiva.commandwhisperer.mixin;

import net.funiva.commandwhisperer.Commandwhisperer;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandBlockEditScreen.class)
public class CommandBlockEditScreenMixin {

    @Shadow @Final private CommandBlockEntity autoCommandBlock;


    @Inject(method = "populateAndSendPacket", at = @At("TAIL"))
    private void onDone(CallbackInfo ci) {
        BlockPos pos = this.autoCommandBlock.getBlockPos();
        String command = ((AbstractCommandBlockEditScreenAccessor) this).getCommandEdit().getValue();

        String tag = "cb_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
        int yPlusOne = pos.getY() + 1;
        String dataPrefix = "data merge entity @e[type=text_display,tag=" + tag + ",limit=1] {text:'";
        String dataSuffix = "'}";
        int overhead = dataPrefix.length() + dataSuffix.length();
        int maxCommandLength = 256 - overhead;
        String displayText = command.length() > maxCommandLength
                ? command.substring(0, maxCommandLength - 5) + "..."
                : command;

        String killCommand = "kill @e[type=text_display,tag=" + tag + "]";

        Commandwhisperer.BlockDisplaySettings settings = Commandwhisperer.blockSettings
                .getOrDefault(pos.immutable(), Commandwhisperer.BlockDisplaySettings.defaults());

        String summonCommand = "summon text_display " + pos.getX() + " " +
                yPlusOne + " " + pos.getZ() +
                " {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[" + settings.textScale() +"f," + settings.textScale() +"f," + settings.textScale() +"f]},billboard:\"center\",Tags:[\"" + tag + "\"]}";


        String dataCommand = dataPrefix + displayText + dataSuffix;
        String optionsCommand = "data merge entity @e[type=text_display,tag=" + tag + ",limit=1] {see_through:"
                + (settings.seeThrough() ? "1b" : "0b")
                + ",view_range:" + String.format("%.2f", settings.viewRange()) + "f}";

        Commandwhisperer.trackCommandBlock(pos.immutable());
        Commandwhisperer.queueCommand(killCommand, 0);
        Commandwhisperer.queueCommand(summonCommand, 1);
        Commandwhisperer.queueCommand(dataCommand, 2);
        Commandwhisperer.queueCommand(optionsCommand, 3);
    }
}