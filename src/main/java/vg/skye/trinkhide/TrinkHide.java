package vg.skye.trinkhide;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.emi.trinkets.api.SlotGroup;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

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
							.then(Commands
									.literal("list")
									.executes(ctx -> {
										var player = ctx.getSource().getPlayer();
										if (player == null)
											return 0;

										var component = TrinkHideComponents.HIDDEN_TRINKETS.get(player);
										var hiddenSlots = Set.copyOf(component.getHiddenSlots());

										var groups = TrinketsApi.getPlayerSlots(player)
											.values()
											.stream()
											.sorted(Comparator.comparing(SlotGroup::getName))
											.toList();
										if (groups.isEmpty()) {
											ctx.getSource().sendFailure(Component.translatable("trinkhide.no_slots"));
											return 1;
										}

										for (var group : groups) {
											var slots = group.getSlots()
												.values()
												.stream()
												.sorted(Comparator.comparing(SlotType::getName))
												.toList();
											if (slots.isEmpty()) continue;

											ctx.getSource().sendSuccess(() -> Component.literal(group.getName() + ":"), false);

											for (var slot : slots) {
												ctx.getSource().sendSuccess(() -> {
													var slotName = getSlotName(slot);
													var message = Component.literal("  " + slotName);
													return hiddenSlots.contains(slotName)
														? message.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
														: message;
												}, false);
											}
										}

										return 0;
									})
							)
			);
		});
	}

	/** Returns the slot name used by TrinkHide to determine if a slot should be rendered or not. */
	public static String getSlotName(SlotType slot) {
		return slot.getGroup() + "/" + slot.getName();
	}
}