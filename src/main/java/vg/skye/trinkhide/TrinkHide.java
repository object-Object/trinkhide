package vg.skye.trinkhide;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.emi.trinkets.api.SlotGroup;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

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
											.argument("slot", StringArgumentType.greedyString())
											.executes(ctx -> {
												var slot = ctx.getArgument("slot", String.class);
												var player = ctx.getSource().getPlayer();
												if (player == null)
													return 0;
												if (!validateSlotName(player, slot)) {
													ctx.getSource().sendFailure(Component.translatable("trinkhide.invalid_slot", slot));
													return 1;
												}
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
												ctx.getSource().sendSuccess(() -> Component.translatable("trinkhide.hidden", slot), false);
												return 0;
											})
									)
							)
							.then(Commands
									.literal("show")
									.then(Commands
											.argument("slot", StringArgumentType.greedyString())
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
												ctx.getSource().sendSuccess(() -> Component.translatable("trinkhide.unhidden", slot), false);
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
										var hiddenSlots = new HashSet<>(component.getHiddenSlots());

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

											ctx.getSource().sendSuccess(() -> Component.translatable("trinkhide.list.group", group.getName()), false);

											for (var slot : slots) {
												for (var index = 0; index < slot.getAmount(); index++) {
													var slotName = getSlotName(slot, index);
													ctx.getSource().sendSuccess(() -> {
														var message = Component.translatable("trinkhide.list.slot", slotName);
														return hiddenSlots.contains(slotName)
															? message.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
															: message;
													}, false);
													hiddenSlots.remove(slotName);
												}
											}
										}

										if (!hiddenSlots.isEmpty()) {
											ctx.getSource().sendSuccess(() -> Component.translatable("trinkhide.list.group.invalid_slots").withStyle(ChatFormatting.RED), false);
											for (var slotName : hiddenSlots.stream().sorted().toList()) {
												ctx.getSource().sendSuccess(() -> Component.translatable("trinkhide.list.slot", slotName).withStyle(ChatFormatting.RED), false);
											}
										}

										return 0;
									})
							)
			);
		});
	}

	/** Returns the slot name used by TrinkHide to determine if a slot should be rendered or not. */
	public static String getSlotName(SlotReference slot) {
		return getSlotName(slot.inventory().getSlotType(), slot.index());
	}

	/** Returns the slot name used by TrinkHide to determine if a slot should be rendered or not.
	 *  If the slot amount is 1, the index will not be used.
	 *  <p>
	 *  Format: `group/slot` (eg. `head/hat`) or `group/slot/index` (eg. `head/face/0`)
	 *  <p>
	 *  Throws IllegalArgumentException if the index is out of range for this slot.
	 * */
	public static String getSlotName(SlotType slot, int index) throws IllegalArgumentException {
		if (index < 0 || index >= slot.getAmount()) {
			throw new IllegalArgumentException("Slot index out of range: " + index);
		}
		var name = slot.getGroup() + "/" + slot.getName();
		return slot.getAmount() > 1
			? name + "/" + index
			: name;
	}

	/** Returns true if this slot name exists for this player. */
	public static boolean validateSlotName(ServerPlayer player, String slotName) {
		// note: this will fail if someone adds a trinket slot like "foo/bar/baz", but based on the trinkets source code I don't think that's supported?
		var parts = slotName.split("/", 3);
		if (parts.length < 2) return false;

		var group = TrinketsApi.getPlayerSlots(player).get(parts[0]);
		if (group == null) return false;

		var slot = group.getSlots().get(parts[1]);
		if (slot == null) return false;

		if (slot.getAmount() > 1) {
			if (parts.length < 3) return false;

			int index;
			try {
				index = Integer.parseInt(parts[2]);
			} catch (NumberFormatException e) {
				return false;
			}

			return index >= 0 && index < slot.getAmount();
		} else {
			// slots with amount == 1 don't have the index in their name
			return parts.length == 2;
		}
	}
}