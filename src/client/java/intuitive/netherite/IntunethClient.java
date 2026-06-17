package intuitive.netherite;

import net.fabricmc.api.ClientModInitializer;

public class IntunethClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		 SoulChargeClientHandler.register();
	}
}