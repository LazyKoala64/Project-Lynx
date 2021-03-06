package handlers;

import java.io.File;
import java.util.function.Consumer;

import commands.Command;
import data.Data;
import init.InitData;
import init.Launcher;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.EventListener;

public class MessageHandler implements EventListener {

	@Override
	public void onEvent(GenericEvent event) {

		//System.out.println("DEBUG [MessageHandler.java]: " + event);

		/*
		 * Checks if the event triggered is a message type and ISN'T A BOT (you can remove it from the if statement if you wish).
		 */
		if((event instanceof MessageReceivedEvent || (InitData.acceptPriv && event instanceof PrivateMessageReceivedEvent)) && !((MessageReceivedEvent) event).getAuthor().isBot() && Launcher.initialized) {

			Guild g = null;
			char prefix; //Server's prefix... if it's even a server.
			
			try {
				g = ((MessageReceivedEvent) event).getGuild();
			} catch(IllegalStateException e) {
				return; //TODO: Support PrivateMessages??? (This is here to prevent NullPointers)
 			}
			
			if(Data.srvr_cache.get(g) == null && g != null) {
				Data.addGuild(g);
				prefix = InitData.prefix;
			} else if(g != null) {
				prefix = ((String) Data.srvr_cache.get(g).get("prefix")).charAt(0);
			} else {
				prefix = InitData.prefix;
			}
			/*
			 * Checks the message uses the defined prefix found in InitData.java (you can change the prefix if you need to)
			 */
			if(((MessageReceivedEvent) event).getMessage().getContentRaw().indexOf(prefix) == 0) {

				String fullMsg = ((MessageReceivedEvent) event).getMessage().getContentRaw().substring(1);
				ChannelType c = event instanceof PrivateMessageReceivedEvent ? ChannelType.PRIVATE : ChannelType.TEXT;

				Command cmd;
				if(!fullMsg.contains(" ")) {
					cmd = CommandHandler.getCommand(fullMsg, g);
				} else {
					cmd = CommandHandler.getCommand(fullMsg.substring(0, fullMsg.indexOf(" ")), g);
				}
				
				if(cmd == null) {
					System.out.println("WARNING: Command is null?????");
					return;
				}

				System.out.println("For server... " + g.getName() + " ... " + Data.command_cache.get(g));
				System.out.println("Grabbed... " + cmd.getName());

				if(cmd == null || (g == null && cmd.getRequirePerms() == true)) return;

				//System.out.println("DEBUG [MessageHandler.java]: " + cmd.getName());
				//System.out.println("DEBUG [MessageHandler.java]: (ChannelType) " + c);

				if(c.equals(ChannelType.PRIVATE))
					cmd.action(((MessageReceivedEvent) event).getAuthor().openPrivateChannel().complete(), fullMsg, event); //Just pass the entire thing to prevent NullPointers, each command will handle them appropriately
				else if(c.equals(ChannelType.TEXT))
					cmd.action(((MessageReceivedEvent) event).getChannel(), fullMsg, event);
			}

		}

	}

	public static Consumer<Message> sendMessage(MessageChannel chn, String s) {

		Consumer<Message> callback = (response) -> System.out.printf("[MessageHandler.java] Sent \"%s\"", response);
		try {
			chn.sendMessage(s).queue(callback);
			return callback;
		} catch(InsufficientPermissionException e) {
			System.out.println("[MessageHandler.java]: Message was not sent due to insufficient permissions!");
		} catch(IllegalArgumentException e) {
			System.out.println("[MessageHandler.java]: Message was not sent due to an empty text field!");
		}
		
		return null;

	}

	public static Consumer<Message> sendMessage(MessageChannel chn, String s, File f) {

		if(f != null) {
			Consumer<Message> callback = (response) -> System.out.printf("[MessageHandler.java] Sent \"%s\"", response);
			try {
				chn.sendMessage(s).addFile(f).queue(callback);
				return callback;
			} catch(InsufficientPermissionException e) {
				System.out.println("[MessageHandler.java]: Message was not sent due to insufficient permissions!");
			}
		} else
			System.out.println("[MessageHandler.java] Unable to send a message, file doesn't exist?");
		
		return null;

	}

	//TODO: Do this...
	public void embedMessage() {

	}

}
