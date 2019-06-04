package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.util.IWithID;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * @author LatvianModder
 */
public enum QuestObjectType implements IWithID
{
	NULL("null", 1, TextFormatting.BLACK),
	FILE("file", 2, TextFormatting.RED),
	CHAPTER("chapter", 4, TextFormatting.GOLD),
	QUEST("quest", 8, TextFormatting.GREEN),
	TASK("task", 16, TextFormatting.BLUE),
	VARIABLE("variable", 32, TextFormatting.DARK_PURPLE),
	REWARD("reward", 64, TextFormatting.LIGHT_PURPLE),
	REWARD_TABLE("reward_table", 128, TextFormatting.YELLOW);

	public static final NameMap<QuestObjectType> NAME_MAP = NameMap.create(NULL, values());
	public static final List<QuestObjectType> ALL_PROGRESSING = Arrays.asList(FILE, CHAPTER, FILE, TASK, VARIABLE);
	public static final List<QuestObjectType> ALL_PROGRESSING_OR_NULL = Arrays.asList(NULL, FILE, CHAPTER, FILE, TASK, VARIABLE);

	private final String id;
	private final String translationKey;
	private final int flag;
	private final TextFormatting color;

	QuestObjectType(String i, int f, TextFormatting c)
	{
		id = i;
		translationKey = "ftbquests." + id;
		flag = f;
		color = c;
	}

	@Override
	public String getID()
	{
		return id;
	}

	public String getTranslationKey()
	{
		return translationKey;
	}

	public int getFlag()
	{
		return flag;
	}

	public TextFormatting getColor()
	{
		return color;
	}
}