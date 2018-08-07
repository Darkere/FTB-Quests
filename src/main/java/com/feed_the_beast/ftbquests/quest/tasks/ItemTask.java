package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;
import com.feed_the_beast.ftbquests.gui.ContainerItemTask;
import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends QuestTask implements Predicate<ItemStack>
{
	public static final String ID = "item";

	private final ConfigList<ConfigItemStack> items;
	private final ConfigInt count;

	public ItemTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest, nbt);
		items = new ConfigList<>(new ConfigItemStack(new ItemStack(Items.APPLE), true));

		NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);

		if (list.isEmpty())
		{
			items.add(new ConfigItemStack(new ItemStack(Items.APPLE)));
		}
		else
		{
			for (int i = 0; i < list.tagCount(); i++)
			{
				ItemStack stack = new ItemStack(list.getCompoundTagAt(i));

				if (!stack.isEmpty())
				{
					items.add(new ConfigItemStack(stack));
				}
			}
		}

		count = new ConfigInt(nbt.getInteger("count"), 1, Integer.MAX_VALUE);
	}

	@Override
	public int getMaxProgress()
	{
		return count.getInt();
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("type", "item");
		NBTTagList list = new NBTTagList();

		for (ConfigItemStack v : items)
		{
			if (!v.getStack().isEmpty())
			{
				list.appendTag(v.getStack().serializeNBT());
			}
		}

		nbt.setTag("items", list);
		nbt.setInteger("count", count.getInt());
	}

	@Override
	public Icon getIcon()
	{
		List<Icon> icons = new ArrayList<>();

		for (ConfigItemStack v : items)
		{
			Icon icon = ItemIcon.getItemIcon(v.getStack());

			if (!icon.isEmpty())
			{
				icons.add(icon);
			}
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		String name;

		if (items.list.size() == 1)
		{
			name = items.list.get(0).getStack().getDisplayName();
		}
		else
		{
			String[] s = new String[items.list.size()];

			for (int i = 0; i < s.length; i++)
			{
				s[i] = items.list.get(i).getStack().getDisplayName();
			}

			name = "[" + StringJoiner.with(", ").joinStrings(s) + "]";
		}

		if (count.getInt() > 1)
		{
			name = count.getInt() + "x " + name;
		}

		return new TextComponentString(name);
	}

	@Override
	public boolean test(ItemStack stack)
	{
		if (stack.isEmpty())
		{
			return false;
		}
		else if (stack.getCount() != 1)
		{
			stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
		}

		for (ConfigItemStack v : items)
		{
			if (ItemStack.areItemStacksEqual(stack, v.getStack()))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("items", items, new ConfigList<>(new ConfigItemStack(ItemStack.EMPTY, true)));
		group.add("count", count, new ConfigInt(1));
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<ItemTask> implements IItemHandler
	{
		public int progress = 0;

		private Data(ItemTask t, IProgressData data)
		{
			super(t, data);
		}

		@Nullable
		@Override
		public NBTBase toNBT()
		{
			return progress > 0 ? new NBTTagInt(progress) : null;
		}

		@Override
		public void fromNBT(@Nullable NBTBase nbt)
		{
			if (nbt instanceof NBTPrimitive)
			{
				progress = ((NBTPrimitive) nbt).getInt();
			}
			else
			{
				progress = 0;
			}
		}

		@Override
		public int getProgress()
		{
			return progress;
		}

		@Override
		public void resetProgress()
		{
			progress = 0;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : null;
		}

		@Override
		public ContainerTaskBase getContainer(EntityPlayer player)
		{
			return new ContainerItemTask(player, this);
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if (progress < task.count.getInt() && task.test(stack))
			{
				int add = Math.min(stack.getCount(), task.count.getInt() - progress);

				if (add > 0)
				{
					if (!simulate)
					{
						progress += add;
						data.syncTask(this);
					}

					return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - add);
				}
			}

			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return task.count.getInt();
		}
	}
}