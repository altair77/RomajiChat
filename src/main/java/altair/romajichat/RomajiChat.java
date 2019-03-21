package altair.romajichat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ModConstants.MODID, name = ModConstants.NAME, version = ModConstants.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class RomajiChat
{
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModConstants.logger = event.getModLog();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onServerChat(ServerChatEvent event) {
		String sourceText = event.getMessage();

		Matcher matcher = Pattern.compile("^[a-zA-Z0-9,. '-?]+$", Pattern.DOTALL)
				.matcher(sourceText);
		if (matcher.matches() && event.getPlayer() != null) {
			new Translation().convert(event.getMessage(), (convertedText) -> sendConvertedMessage(event.getPlayer(), convertedText));
			event.setCanceled(true);
		}
	}

	private static void sendConvertedMessage(EntityPlayerMP player, String message) {
		if (player != null) {
			String componentMessage = "<" + player.getName() + "> " + message;
			String eventMessage = ":speech_balloon: " + message;
			ITextComponent textComponent = new TextComponentString(componentMessage);
			textComponent.getStyle().setColor(TextFormatting.YELLOW);
			ServerChatEvent event = new ServerChatEvent(player, eventMessage, textComponent);
			if (!MinecraftForge.EVENT_BUS.post(event)) {
				FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(textComponent, false);
			}
		}
	}
}
