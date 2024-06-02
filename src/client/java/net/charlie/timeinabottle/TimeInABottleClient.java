package net.charlie.timeinabottle;

import net.fabricmc.api.ClientModInitializer;
import net.charlie.timeinabottle.entity.AcceleratorEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TimeInABottleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(TimeInABottle.ACCELERATOR, AcceleratorEntityRenderer::new);
	}
}