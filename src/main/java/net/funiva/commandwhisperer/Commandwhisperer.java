package net.funiva.commandwhisperer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.core.BlockPos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

public class Commandwhisperer implements ModInitializer {
	public static final String MOD_ID = "commandwhisperer";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static int tickCounter = 0;

	private static final Queue<QueuedCommand> pendingCommands = new LinkedList<>();

	private record QueuedCommand(String command, int sendAtTick) {}

	private static final Set<BlockPos> trackedCommandBlocks = ConcurrentHashMap.newKeySet();

	public static void trackCommandBlock(BlockPos pos) {
		trackedCommandBlocks.add(pos.immutable());
	}

	public static void queueCommand(String command, int delayTicks) {
		pendingCommands.add(new QueuedCommand(command, tickCounter + delayTicks));
	}

	public record BlockDisplaySettings(boolean seeThrough, double viewRange, double textScale) {
		public static BlockDisplaySettings defaults() {
			return new BlockDisplaySettings(false, 1.0, 0.35);
		}
	}

	public static final Map<BlockPos, BlockDisplaySettings> blockSettings = new HashMap<>();

	@Override
	public void onInitialize() {
		String greet = "Wassuuup world!";
		int greetWidth = greet.length();
		LOGGER.info(greet);
		LOGGER.info("greet is " + String.valueOf(greetWidth) + " wide");
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			LOGGER.info("Block break BEFORE fired. isClient=" + world.isClientSide() + ", blockEntity=" + blockEntity);
			if (blockEntity instanceof CommandBlockEntity) {
				String tag = "cb_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
				String killCommand = "kill @e[type=text_display,tag=" + tag + "]";
				queueCommand(killCommand, 0);
			}
			return true;
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			tickCounter++;
			if (tickCounter % 20 == 0 && client.level != null) {
				Iterator<BlockPos> iter = trackedCommandBlocks.iterator();
				while (iter.hasNext()) {
					BlockPos pos = iter.next();
					if (!(client.level.getBlockEntity(pos) instanceof CommandBlockEntity)) {
						String tag = "cb_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
						queueCommand("kill @e[type=text_display,tag=" + tag + "]", 0);
						iter.remove();
					}
				}
			}

			while (!pendingCommands.isEmpty() && pendingCommands.peek().sendAtTick() <= tickCounter) {
				QueuedCommand queued = pendingCommands.poll();
				if (client.player != null) {
					client.player.connection.sendCommand(queued.command());
				}
			}
		});

	}
}