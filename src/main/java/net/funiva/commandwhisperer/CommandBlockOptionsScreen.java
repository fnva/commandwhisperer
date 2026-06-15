package net.funiva.commandwhisperer;

import net.funiva.commandwhisperer.mixin.AbstractCommandBlockEditScreenAccessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Tooltip;

public class CommandBlockOptionsScreen extends Screen {

    private final Screen parent;
    private final BlockPos pos;

    private boolean seeThrough;
    private double viewRange;
    private double textScale;
    private final String savedCommandText;


    public CommandBlockOptionsScreen(Screen parent, BlockPos pos, String commandText) {
        super(Component.literal("Command Block Options"));
        this.parent = parent;
        this.pos = pos;
        this.savedCommandText = commandText;

        Commandwhisperer.BlockDisplaySettings settings = Commandwhisperer.blockSettings
                .getOrDefault(pos, Commandwhisperer.BlockDisplaySettings.defaults());
        this.seeThrough = settings.seeThrough();
        this.viewRange = settings.viewRange();
        this.textScale = settings.textScale();
    }


    @Override
    protected void init() {
        // see through toggle
        CycleButton<Boolean> seeThroughButton = this.addRenderableWidget(
                CycleButton.booleanBuilder(
                                Component.literal("Yes"),
                                Component.literal("No"),
                                this.seeThrough
                        )
                        //.displayOnlyValue()
                        .create(
                                this.width / 2 - 155, 80, 150, 20,
                                Component.literal("Seen through walls"),
                                (button, value) -> this.seeThrough = value
                        )
        );
        seeThroughButton.setTooltip(Tooltip.create(
                Component.literal("Whether text is visible through blocks")
        ));

        // view range slider
        AbstractSliderButton viewRangeSlider = this.addRenderableWidget(new AbstractSliderButton(
                this.width / 2 + 5, 80, 150, 20,
                Component.literal("Render Distance: " + String.format("%.2f", this.viewRange)),
                this.viewRange / 2.0
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.literal("Render Distance: " + String.format("%.2f", value * 2.0)));
            }

            @Override
            protected void applyValue() {
                viewRange = this.value * 2.0;
            }
        });
        viewRangeSlider.setTooltip(Tooltip.create(
                Component.literal("Render distance multiplier of the text. Text won't be rendered if the distance is more than \n\nRenderDistance * EntityDistance% * 64")
        ));

        // scale slider
        AbstractSliderButton textScaleSlider = this.addRenderableWidget(new AbstractSliderButton(
                this.width / 2 - 155, 110, 310, 20,
                Component.literal("Text Scale: " + String.format("%.2f", this.textScale)),
                this.textScale / 8.0
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.literal("View Range: " + String.format("%.2f", value * 8.0)));
            }

            @Override
            protected void applyValue() {
                textScale = this.value * 8.0;
            }
        });


        // done button
        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), button -> this.onClose())
                        .bounds(this.width / 2 - 75, 180, 150, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        Commandwhisperer.blockSettings.put(
                pos,
                new Commandwhisperer.BlockDisplaySettings(seeThrough, viewRange, textScale)
        );
        this.minecraft.setScreen(parent);
        ((AbstractCommandBlockEditScreenAccessor) parent).getCommandEdit().setValue(savedCommandText);
    }



}