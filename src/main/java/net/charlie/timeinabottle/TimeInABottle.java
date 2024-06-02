package net.charlie.timeinabottle;

import net.charlie.timeinabottle.entity.AcceleratorEntity;
import net.charlie.timeinabottle.item.TimeInABottleItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class TimeInABottle implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "timeinabottle";
    public static final Logger LOGGER = LoggerFactory.getLogger("Time in a Bottle");
	public static final ModConfig config = new ModConfig();
	public static final TimeInABottleItem TIME_IN_A_BOTTLE = Registry.register(
			Registries.ITEM,
			new Identifier(
					MOD_ID,
					"time_in_a_bottle"
			),
			new TimeInABottleItem()
	);
	public static final EntityType<AcceleratorEntity> ACCELERATOR = (EntityType<AcceleratorEntity>) Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(MOD_ID, "accelerator"),
			(EntityType<?>) FabricEntityTypeBuilder
							.create(
									SpawnGroup.MISC,
									AcceleratorEntity::new
							).dimensions(
									EntityDimensions.fixed(
											0.1f,
											0.1f
									)
							).build()
	);

	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((entries -> entries.add(new ItemStack(TIME_IN_A_BOTTLE))));
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}
}