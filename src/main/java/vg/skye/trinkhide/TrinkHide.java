package vg.skye.trinkhide;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class TrinkHide implements ModInitializer {
	public static final String MOD_ID = "trinkhide";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					Commands
							.literal("trinkhide")
							.then(Commands
									.literal("hide")
									.then(Commands
											.argument("slot", StringArgumentType.string())
											.executes(ctx -> {
												var slot = ctx.getArgument("slot", String.class);
												var player = ctx.getSource().getPlayer();
												if (player == null)
													return 0;
												var component = TrinkHideComponents.HIDDEN_TRINKETS.get(player);
												var hiddenSlots = component.getHiddenSlots();
												if (hiddenSlots.contains(slot)) {
													ctx.getSource().sendFailure(Component.translatable("trinkhide.already_hidden"));
													return 1;
												}
												var slots = new ArrayList<String>(hiddenSlots.size() + 1);
												slots.addAll(hiddenSlots);
												slots.add(slot);
												component.setHiddenSlots(slots);
												return 0;
											})
									)
							)
							.then(Commands
									.literal("show")
									.then(Commands
											.argument("slot", StringArgumentType.string())
											.executes(ctx -> {
												var slot = ctx.getArgument("slot", String.class);
												var player = ctx.getSource().getPlayer();
												if (player == null)
													return 0;
												var component = TrinkHideComponents.HIDDEN_TRINKETS.get(player);
												var hiddenSlots = component.getHiddenSlots();
												if (!hiddenSlots.contains(slot)) {
													ctx.getSource().sendFailure(Component.translatable("trinkhide.not_hidden"));
													return 1;
												}
												var slots = new ArrayList<String>(hiddenSlots.size() - 1);
												for (String hiddenSlot : hiddenSlots) {
													if (slot.equals(hiddenSlot))
														continue;
													slots.add(hiddenSlot);
												}
												component.setHiddenSlots(slots);
												return 0;
											})
									)
							)
			);
		});
	}
}