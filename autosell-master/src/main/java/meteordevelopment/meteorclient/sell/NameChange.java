package meteordevelopment.meteorclient.sell;

import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public class NameChange
{
	public void nameChange(String name)
	{

		MinecraftClient client = MinecraftClient.getInstance();

		client.execute(() -> {
			if(client.getNetworkHandler() != null)
			{
				client.getNetworkHandler().getConnection()
					.disconnect(Text.of("/name Autosell."));
				new CrackedAccount(name).login();
			}else
			{
				client.disconnect(new TitleScreen(), true);
			}
		});
		return;
	}

	public void nameChange(String name, String message)
	{

		MinecraftClient client = MinecraftClient.getInstance();

		client.execute(() -> {
			if(client.getNetworkHandler() != null)
			{
				client.getNetworkHandler().getConnection()
					.disconnect(Text.of(message));
                new CrackedAccount(name).login();
			}else
			{
                new CrackedAccount(name).login();
			}
		});
		return;
	}

}
