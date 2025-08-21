package net.crizo.rtcextras.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;

import java.util.Properties;
import java.util.List;

public class GeneSampleItem extends Item {
	// MCreator sometimes wires registrars that pass a ResourceLocation to the ctor.
	// Keep this overload to be compatible if needed.
	public GeneSampleItem(net.minecraft.resources.ResourceLocation ignored) {
		this(new Properties().stacksTo(1)); // unstackable
	}

	public GeneSampleItem(Properties props) {
		super(props.stacksTo(1));
	}

	private static CompoundTag getTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
		CompoundTag tag = getTag(stack);
		if (!tag.contains("rtc_gene_id")) {
			tip.add(Component.literal("Empty gene sample").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Use in a Gene Sequencer with a stable bacterium.").withStyle(ChatFormatting.DARK_GRAY));
			return;
		}
		String tier = tag.getString("rtc_gene_tier");
		String name = tag.getString("rtc_gene_name");
		String effect = tag.getString("rtc_gene_effect");
		float interf = tag.getFloat("rtc_gene_interference");
		String id = tag.getString("rtc_gene_id");
		int color = switch (tier) {
			case "common" -> 0xFFFF55; // yellow
			case "rare" -> 0xFF8888; // light red
			case "exotic" -> 0xAA00FF; // purple
			default -> 0xFFFFFF;
		};
		tip.add(Component.literal("Gene Sample").withStyle(ChatFormatting.GRAY));
		tip.add(Component.literal("Gene: " + name).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
		tip.add(Component.literal("Tier: " + tier).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
		tip.add(Component.literal(effect).withStyle(ChatFormatting.GRAY));
		// Optional debug lines
		tip.add(Component.literal("ID: " + id).withStyle(ChatFormatting.DARK_GRAY));
		tip.add(Component.literal("Interference: " + interf).withStyle(ChatFormatting.DARK_GRAY));
		// If you stored where/when it was sampled:
		if (tag.contains("rtc_sampled_count")) {
			int c = tag.getInt("rtc_sampled_count");
			tip.add(Component.literal("Sampled from cohort: x" + c).withStyle(ChatFormatting.DARK_GRAY));
		}
	}
	// No decay — nothing else needed.
}