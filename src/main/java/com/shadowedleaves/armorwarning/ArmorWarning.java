package com.shadowedleaves.armorwarning;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class ArmorWarning implements ModInitializer {
	private ItemStack lastHelmet = ItemStack.EMPTY;
	private ItemStack lastChestplate = ItemStack.EMPTY;
	private ItemStack lastLeggings = ItemStack.EMPTY;
	private ItemStack lastBoots = ItemStack.EMPTY;

	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			resetState();
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			checkArmorChange(client, EquipmentSlot.HEAD, "Helmet", Formatting.DARK_RED);
			checkArmorChange(client, EquipmentSlot.CHEST, "Chestplate", Formatting.DARK_RED);
			checkArmorChange(client, EquipmentSlot.LEGS, "Leggings", Formatting.DARK_RED);
			checkArmorChange(client, EquipmentSlot.FEET, "Boots", Formatting.DARK_RED);
		});
	}

	private void resetState() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		lastHelmet = client.player.getEquippedStack(EquipmentSlot.HEAD);
		lastChestplate = client.player.getEquippedStack(EquipmentSlot.CHEST);
		lastLeggings = client.player.getEquippedStack(EquipmentSlot.LEGS);
		lastBoots = client.player.getEquippedStack(EquipmentSlot.FEET);
	}

	private void showTitle(MinecraftClient client, String label, Formatting color) {
		Text title = Text.literal(label).setStyle(Style.EMPTY.withColor(color).withBold(true));
		client.inGameHud.setTitle(title);
		// fadeIn: 2 ticks, stay: 20 ticks (1 second), fadeOut: 2 ticks
		client.inGameHud.setTitleTicks(2, 20, 2);
	}

	private void checkArmorChange(MinecraftClient client, EquipmentSlot slot, String label, Formatting color) {
		ItemStack current = client.player.getEquippedStack(slot);
		ItemStack last;

		switch (slot) {
			case HEAD: last = lastHelmet; break;
			case CHEST: last = lastChestplate; break;
			case LEGS: last = lastLeggings; break;
			case FEET: last = lastBoots; break;
			default: return;
		}

		if (!ItemStack.areEqual(current, last)) {
			if (!last.isEmpty() && current.isEmpty()) {
				showTitle(client, label, color);
			}
			// Update last
			switch (slot) {
				case HEAD: lastHelmet = current.copy(); break;
				case CHEST: lastChestplate = current.copy(); break;
				case LEGS: lastLeggings = current.copy(); break;
				case FEET: lastBoots = current.copy(); break;
			}
		}
	}
}