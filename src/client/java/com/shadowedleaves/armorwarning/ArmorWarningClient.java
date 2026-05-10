package com.shadowedleaves.armorwarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ArmorWarningClient implements ClientModInitializer {

	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET,
	};
	private static final String[] SLOT_LABELS = { "Helmet", "Chestplate", "Leggings", "Boots" };

	private static final int TITLE_FADE_IN_TICKS = 0;
	private static final int TITLE_STAY_TICKS = 10;
	private static final int TITLE_FADE_OUT_TICKS = 0;

	private LocalPlayer trackedPlayer;
	private final ItemStack[] lastArmor = { ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY };
	private boolean snapshotReady;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.level == null) {
			clearTracking();
			return;
		}
		if (player != trackedPlayer) {
			trackedPlayer = player;
			snapshotReady = false;
		}
		if (!snapshotReady) {
			copyArmor(player, lastArmor);
			snapshotReady = true;
			return;
		}
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack previous = lastArmor[i];
			ItemStack current = player.getItemBySlot(SLOTS[i]);
			if (!previous.isEmpty() && current.isEmpty()) {
				showWarning(client, SLOT_LABELS[i]);
			}
			lastArmor[i] = current.isEmpty() ? ItemStack.EMPTY : current.copy();
		}
	}

	private static void copyArmor(LocalPlayer player, ItemStack[] into) {
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack stack = player.getItemBySlot(SLOTS[i]);
			into[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
		}
	}

	private static void showWarning(Minecraft client, String armorLabel) {
		var gui = client.gui;
		gui.clearTitles();
		gui.setTimes(TITLE_FADE_IN_TICKS, TITLE_STAY_TICKS, TITLE_FADE_OUT_TICKS);
		gui.setTitle(Component.literal("⚠ Warning! ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
		gui.setSubtitle(Component.literal(armorLabel).withStyle(ChatFormatting.RED));
	}

	private void clearTracking() {
		trackedPlayer = null;
		snapshotReady = false;
		for (int i = 0; i < lastArmor.length; i++) {
			lastArmor[i] = ItemStack.EMPTY;
		}
	}
}
