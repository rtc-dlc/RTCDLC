package net.crizo.rtcextras.client.gui;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.GuiGraphics;

import net.crizo.rtcextras.world.inventory.GUICultivatorMenu;
import net.crizo.rtcextras.procedures.CultivatorButtonStatusProcedure;
import net.crizo.rtcextras.procedures.CultivatorButtonCountProcedure;
import net.crizo.rtcextras.network.GUICultivatorButtonMessage;
import net.crizo.rtcextras.init.RtcExtrasModScreens;

import com.mojang.blaze3d.systems.RenderSystem;

public class GUICultivatorScreen extends AbstractContainerScreen<GUICultivatorMenu> implements RtcExtrasModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	ImageButton imagebutton_art;
	ImageButton imagebutton_art1;

	public GUICultivatorScreen(GUICultivatorMenu container, Inventory inventory, Component text) {
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

	private static final ResourceLocation texture = ResourceLocation.parse("rtc_extras:textures/screens/gui_cultivator.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
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
		guiGraphics.drawString(this.font, CultivatorButtonCountProcedure.execute(world, x, y, z), 6, 34, -12829636, false);
		guiGraphics.drawString(this.font, CultivatorButtonStatusProcedure.execute(world, x, y, z), 6, 61, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_art = new ImageButton(this.leftPos + 42, this.topPos + 16, 16, 16, new WidgetSprites(ResourceLocation.parse("rtc_extras:textures/screens/art.png"), ResourceLocation.parse("rtc_extras:textures/screens/art.png")), e -> {
			int x = GUICultivatorScreen.this.x;
			int y = GUICultivatorScreen.this.y;
			if (true) {
				PacketDistributor.sendToServer(new GUICultivatorButtonMessage(0, x, y, z));
				GUICultivatorButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
				guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_art);
		imagebutton_art1 = new ImageButton(this.leftPos + 177, this.topPos + 7, 16, 16, new WidgetSprites(ResourceLocation.parse("rtc_extras:textures/screens/art.png"), ResourceLocation.parse("rtc_extras:textures/screens/art.png")), e -> {
			int x = GUICultivatorScreen.this.x;
			int y = GUICultivatorScreen.this.y;
			if (true) {
				PacketDistributor.sendToServer(new GUICultivatorButtonMessage(1, x, y, z));
				GUICultivatorButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
				guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_art1);
	}
}