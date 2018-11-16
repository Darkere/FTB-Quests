package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class MessageCreateObject extends MessageToServer
{
	private int parent;
	private QuestObjectBase object;
	private NBTTagCompound extra;

	public MessageCreateObject()
	{
	}

	public MessageCreateObject(QuestObjectBase o, @Nullable NBTTagCompound e)
	{
		parent = o.getParentID();
		object = o;
		extra = e;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeNBT(extra);
		data.writeInt(parent);
		data.writeByte(object.getObjectType().ordinal());
		object.writeNetData(data);
	}

	@Override
	public void readData(DataIn data)
	{
		extra = data.readNBT();
		parent = data.readInt();
		QuestObjectType type = QuestObjectType.VALUES[data.readUnsignedByte()];
		object = ServerQuestFile.INSTANCE.create(type, parent, extra == null ? new NBTTagCompound() : extra);
		object.readNetData(data);
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			object.uid = ServerQuestFile.INSTANCE.readID(0);

			if (object instanceof QuestObject)
			{
				((QuestObject) object).id = object.getCodeString();
			}

			object.onCreated();
			ServerQuestFile.INSTANCE.refreshIDMap();
			ServerQuestFile.INSTANCE.clearCachedData();
			ServerQuestFile.INSTANCE.save();
			new MessageCreateObjectResponse(object, extra).sendToAll();
		}
	}
}