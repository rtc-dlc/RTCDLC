package net.crizo.rtcextras.client.gui;

import org.checkerframework.checker.units.qual.h;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.Mth;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;

import net.crizo.rtcextras.world.inventory.GUIGeneSequencerMenu;
import net.crizo.rtcextras.init.RtcExtrasModScreens;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

public class GUIGeneSequencerScreen extends AbstractContainerScreen<GUIGeneSequencerMenu> implements RtcExtrasModScreens.ScreenAccessor {
	private final Level world;
	private double final_drawheight = 0;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	public GUIGeneSequencerScreen(GUIGeneSequencerMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	private static final ResourceLocation texture = ResourceLocation.parse("rtc_extras:textures/screens/picturescience.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
		int w = guiGraphics.guiWidth();
		int h = guiGraphics.guiHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		{
			{
				Font font = Minecraft.getInstance().font;
				double curX = entity.getPersistentData().getDouble("BannerX");
				String text = "Yo adrian explain our friend group";
				double targX = entity.getPersistentData().getDouble("BannerXTarget");
				drawWrappedText(guiGraphics, font, text, (int) curX, ((h / 2) + 30) + -40, 190, 0xFFFFFF);
			}
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		int timer = (int) getBlockNBTNumber(world, BlockPos.containing(x, y, z), "timer");
		int drawheight = 50 * timer / 200;
		final_drawheight = Mth.lerp(0.2, final_drawheight, drawheight);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(ResourceLocation.parse("rtc_extras:textures/screens/picturescience.png"), this.leftPos + 0, this.topPos + 0, 0, 0, 176, 166, 176, 166);
		guiGraphics.blit(ResourceLocation.parse("rtc_extras:textures/screens/progressbio.png"), this.leftPos + 132, // screen X
				this.topPos + 27, // screen Y
				0, // U
				0, // V
				20, // width to draw
				(int) final_drawheight, // height to draw
				20, // texture width
				50 // texture height
		);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
	}

	private static double getBlockNBTNumber(LevelAccessor world, BlockPos pos, String tag) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null)
			return blockEntity.getPersistentData().getDouble(tag);
		return -1;
	}

	public static void drawWrappedText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int maxWidth, int color) {
		List<FormattedCharSequence> lines = font.split(Component.literal(text), maxWidth);
		int lineSpacing = 3;
		int boxHeight = (lines.size() * (font.lineHeight + lineSpacing) - lineSpacing + 10) + 16;
		int boxWidth = 224;
		int topHeight = 10;
		int bottomHeight = 10;
		int middleHeight = boxHeight - topHeight - bottomHeight;
		ResourceLocation tex = (ResourceLocation.parse("rtc_extras:textures/screens/hallelughaj.png"));
		// Top part
		guiGraphics.blit(tex, x, y, 0, 0, boxWidth, topHeight, boxWidth, 154);
		// Middle
		int currentY = y + topHeight;
		int sliceHeight = 1;
		int sourceY = topHeight;
		for (int i = 0; i < middleHeight; i += sliceHeight) {
			int drawHeight = Math.min(sliceHeight, middleHeight - i);
			guiGraphics.blit(tex, x, currentY + i, 0, sourceY, boxWidth, drawHeight, boxWidth, 154);
		}
		// Bottom
		guiGraphics.blit(tex, x, y + topHeight + middleHeight, 0, 144, boxWidth, bottomHeight, boxWidth, 154);
		for (int i = 0; i < lines.size(); i++) {
			guiGraphics.drawString(font, lines.get(i), x + 10, y + topHeight + i * (font.lineHeight + lineSpacing), color, true);
		}
	}
}