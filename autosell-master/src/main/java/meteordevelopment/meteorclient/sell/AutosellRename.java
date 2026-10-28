
package meteordevelopment.meteorclient.sell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Random;

public class AutosellRename
{
	private static final Random random = new Random();

	public void rename(AutosellSetup setup, ArrayList<String> args) {
		Thread thread = new Thread(() -> {
			MinecraftClient CLIENT = MinecraftClient.getInstance();
			PlayerEntity player = CLIENT.player;
			AutosellCycle cycle = setup.getCycle();

			// Відкрити інвентар у головному потоці
			CLIENT.execute(() -> CLIENT.setScreen(new InventoryScreen(player)));
			sleep(200);

			for (int i = 0; i < 9; ++i) {
				int slot = i;

				// Дістати предмет і вибрати слот — тільки у головному потоці
				ItemStack stack = CLIENT.submit(() -> player.getInventory().getStack(slot)).join();
				if (!stack.isEmpty()) {
					CLIENT.execute(() -> player.getInventory().setSelectedSlot(slot));

					sleep(100);

					// Команда перейменування — можна у фоні
					cycle.executeCommand("name " + generateRandomString(random.nextInt(2) + 4));

					sleep(100);

					// Клік у слот — у головному потоці
					CLIENT.execute(() -> {
						CLIENT.interactionManager.clickSlot(
								player.currentScreenHandler.syncId,
								slot + 36, 1,
								SlotActionType.THROW, player
						);
						player.getInventory().updateItems();
						player.playerScreenHandler.sendContentUpdates();
					});
				}
			}

			if (args.contains("reconnect")) {
				sleep(100);
				cycle.executeCommand("an103");
				sleep(1000);
				cycle.executeCommand("an312");
			}
		});
		thread.start();
	}


	public static String generateRandomString(int length)
	{
		String characters =
				"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < length; i++)
		{
			result
					.append(characters.charAt(random.nextInt(characters.length())));
		}
		return result.toString();
	}

	private void sleep(int milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds);
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
