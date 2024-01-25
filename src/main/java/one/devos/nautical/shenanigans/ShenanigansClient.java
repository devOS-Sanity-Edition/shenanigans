package one.devos.nautical.shenanigans;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityData;
import one.devos.nautical.shenanigans.injecteddata.InjectedEntityDataHolder;

@ClientOnly
public class ShenanigansClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientPlayNetworking.registerGlobalReceiver(InjectedEntityData.SYNC_ORDER_PACKET, (client, handler, buf, responseSender) -> {
			buf.retain();
			client.execute(() -> {
				try {
					int definitionCount = buf.readVarInt();
					for (int i = 0; i < definitionCount; i++) {
						var key = Class.forName(buf.readUtf());
						var definition = InjectedEntityData.DEFINITIONS.get(key);
						var serverIndices = new IntArrayList();
						int serverIndexCount = buf.readVarInt();
						for (int j = 0; j < serverIndexCount; j++) serverIndices.add(buf.readVarInt());
						Shenanigans.LOGGER.info("Injected data sync info for " + key.getName() + ":");
						Shenanigans.LOGGER.info("Server indices: " + serverIndices.toString());
						Shenanigans.LOGGER.info("Client indices: " + definition.indices().toString());
						definition.sort(serverIndices);
						Shenanigans.LOGGER.info("Synced client indices: " + definition.indices().toString());
					}
				} catch (Exception e) {
					Shenanigans.LOGGER.error("Failed to sync definition order with server : " + e);
				} finally {
					buf.release();
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(InjectedEntityData.SYNC_PACKET, (client, handler, buf, responseSender) -> {
			buf.retain();
			client.execute(() -> {
				try {
					var entity = client.level.getEntity(buf.readVarInt());
					if (entity != null && entity.getEntityData() instanceof InjectedEntityDataHolder holder) {
						var injectedData = holder.shenanigans$injectedData();
						if (injectedData != null) {
							int sequence = buf.readVarInt();
							for (int i = 0; i < injectedData.values.size(); i++) {
								if (((sequence >> i) & 1) != 0)
									injectedData.get(i).read(buf);
							}
						} else {
							throw new RuntimeException("[Shenanigans] Tried to sync a entity with missing injected data?!");
						}
					}
				} catch (Exception e) {
					Shenanigans.LOGGER.error("Failed to sync injected entity data : " + e);
				} finally {
					buf.release();
				}
			});
		});
	}
}
