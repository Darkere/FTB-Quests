package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ContainerTask;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageOpenTask extends MessageToServer
{
	private String task;

	public MessageOpenTask()
	{
	}

	public MessageOpenTask(String t)
	{
		task = t;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(task);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		QuestTask qtask = ServerQuestFile.INSTANCE.getTask(task);

		if (qtask != null && qtask.quest.canStartTasks(teamData))
		{
			openGUI(teamData.getQuestTaskData(qtask), player, null);
		}
	}

	public static void openGUI(QuestTaskData data, EntityPlayerMP player, @Nullable TileScreenCore tile)
	{
		player.getNextWindowId();
		player.closeContainer();
		player.openContainer = new ContainerTask(player, data);
		player.openContainer.windowId = player.currentWindowId;

		if (tile != null)
		{
			((ContainerTask) player.openContainer).screen = tile;
		}

		player.openContainer.addListener(player);
		new MessageOpenTaskResponse(data.task.getID(), player.currentWindowId, tile != null, tile != null ? tile.getPos() : null).sendTo(player);
		MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
	}
}