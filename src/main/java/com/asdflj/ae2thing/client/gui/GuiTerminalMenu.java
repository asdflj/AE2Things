package com.asdflj.ae2thing.client.gui;

import static com.asdflj.ae2thing.loader.KeybindLoader.openTerminalMenu;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.nio.FloatBuffer;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.TerminalMenu;
import com.asdflj.ae2thing.client.gui.container.ContainerTerminalMenu;
import com.asdflj.ae2thing.client.render.Shader;
import com.asdflj.ae2thing.util.NameConst;

import appeng.util.Platform;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Loader;

public class GuiTerminalMenu extends GuiContainer implements INEIGuiHandler {

    private static final Shader Shader = new Shader(AE2Thing.MODID, "shaders/menu.vert", "shaders/menu.frag");
    private static final int VBO = GL15.glGenBuffers();
    private final TerminalMenu menu = new TerminalMenu();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
    public static int page = 0;
    private int maxPage = page;
    private int currentIndex = 0;
    private static final int SECTOR_COUNT = 6;
    private static final boolean hasLwjgl3 = Loader.isModLoaded("lwjgl3ify");

    public GuiTerminalMenu() {
        super(new ContainerTerminalMenu());
        this.xSize = 256;
    }

    private int calMaxPage() {
        if (menu.getItems()
            .isEmpty()) return 0;
        int page = menu.getItems()
            .size() / SECTOR_COUNT;
        if (menu.getItems()
            .size() % SECTOR_COUNT == 0 && page > 0) {
            page--;
        }
        return page;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (menu.getItems()
            .size() <= page * SECTOR_COUNT) {
            page = 0;
        }
        maxPage = calMaxPage();
        scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        float x = mc.displayWidth, y = mc.displayHeight;
        float[] vertices = { 0, 0, 0, y, x, y, x, 0 };

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length)
            .put(vertices);
        vertexBuffer.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        FloatBuffer buf1 = createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf1);
        FloatBuffer buf2 = createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf2);
        Shader.use();
        GL20.glUniformMatrix4(GL20.glGetUniformLocation(Shader.getProgram(), "modelview"), false, buf1);
        GL20.glUniformMatrix4(GL20.glGetUniformLocation(Shader.getProgram(), "projection"), false, buf2);
        GL20.glUniform2f(GL20.glGetUniformLocation(Shader.getProgram(), "iResolution"), x, y);
        Shader.clear();
    }

    @Override
    public void onGuiClosed() {}

    @Override
    public void drawDefaultBackground() {

    }

    @Override
    public void handleInput() {
        super.handleInput();
        if (!openTerminalMenu.isPressed() && !Keyboard.getEventKeyState()) {
            Minecraft.getMinecraft().thePlayer.closeScreen();
            menu.OpenTerminal(currentIndex);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        if (!hasLwjgl3) {
            // LWJGL2 reports different scroll values for every platform, 120 for one tick on Windows.
            // LWJGL3 reports the delta in exact scroll ticks.
            // Round away from zero to avoid dropping small scroll events
            if (wheel > 0) {
                wheel = (int) Platform.ceilDiv(wheel, 120);
            } else {
                wheel = -(int) Platform.ceilDiv(-wheel, 120);
            }
        }
        final int x = Mouse.getEventX() * this.width / mc.displayWidth;
        final int y = this.height - Mouse.getEventY() * this.height / mc.displayHeight - 1;
        this.mouseWheelEvent(x, y, wheel);
    }

    public void mouseWheelEvent(int x, int y, int wheel) {
        if (this.menu.getItems()
            .size() <= SECTOR_COUNT) {
            page = 0;
        }
        if (wheel == Constants.MouseWheel.NEXT.direction) {
            page = Math.max(page - 1, 0);
            this.currentIndex = -1;
        } else if (wheel == Constants.MouseWheel.PREVIEW.direction) {
            page = Math.min(page + 1, maxPage);
            this.currentIndex = -1;
        }
    }

    private short selection(int mouseX, int mouseY) {
        int x = this.width / 2, y = this.height / 2;
        short result = -1;
        if (!(mouseX == x && mouseY == y)) {
            double deltaX = x - mouseX;
            double deltaY = mouseY - y;
            double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (length < 20) {
                return -1;
            }
            double radians = Math.atan2(deltaY, deltaX);
            double degrees = Math.toDegrees(radians);
            if (degrees < 0) {
                degrees += 360;
            }
            return (short) (degrees / 60D);
        }
        return result;
    }

    public void drawItem() {
        // copy from deep seek
        GL11.glPushMatrix();
        float innerRadius = 20.0f;
        float outerRadius = 50.0f;
        int centerX = this.xSize / 2;
        int centerY = this.ySize / 2;
        float sectorSize = (float) (2 * Math.PI) / SECTOR_COUNT;
        float startAngleOffset = (float) (3 * Math.PI + Math.PI / 6);
        for (int i = 0; i < SECTOR_COUNT; i++) {
            float centerAngle = startAngleOffset + i * sectorSize;
            float iconRadius = (innerRadius + outerRadius) / 2;
            int iconX = centerX + (int) (iconRadius * Math.cos(centerAngle));
            int iconY = centerY - (int) (iconRadius * Math.sin(centerAngle));

            int ax = this.getRealIndex(i);
            if (ax == -1) continue;
            ItemStack item = menu.getTerminalItems()
                .get(ax)
                .getTargetItem();
            itemRender
                .renderItemAndEffectIntoGUI(this.fontRendererObj, mc.getTextureManager(), item, iconX - 8, iconY - 9);
        }
        GL11.glPopMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glPushMatrix();
        GL11.glScalef(this.scaledresolution.getScaleFactor(), this.scaledresolution.getScaleFactor(), 1.0f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        Shader.use();

        GL20.glUniform2f(GL20.glGetUniformLocation(Shader.getProgram(), "iMouse"), Mouse.getX(), Mouse.getY());
        GL20.glUniform1f(
            GL20.glGetUniformLocation(Shader.getProgram(), "scaleFactor"),
            this.scaledresolution.getScaleFactor());

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL_QUADS, 0, 4);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        Shader.clear();

        glDisable(GL11.GL_BLEND);
        glEnable(GL11.GL_DEPTH_TEST);

        GL11.glPopMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int rx = selection(mouseX, mouseY);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        drawItem();
        this.fontRendererObj.drawStringWithShadow(
            I18n.format(NameConst.GUI_TERMINAL_MENU_PAGE, page + 1),
            0,
            this.ySize - 20,
            0xffffff);
        this.fontRendererObj.drawStringWithShadow(
            I18n.format(NameConst.GUI_TERMINAL_MENU_MAX_PAGE, maxPage + 1),
            this.xSize - fontRendererObj.getStringWidth(I18n.format(NameConst.GUI_TERMINAL_MENU_MAX_PAGE, maxPage + 1)),
            this.ySize - 20,
            0xffffff);
        if (rx == -1) {
            this.currentIndex = -1;
            return;
        }
        this.currentIndex = getRealIndex(rx);
        if (this.currentIndex == -1) return;
        String name = menu.getTerminalItems()
            .get(this.currentIndex)
            .getDisplayName();
        this.fontRendererObj.drawStringWithShadow(
            name,
            (this.xSize / 2) - (this.fontRendererObj.getStringWidth(name) / 2),
            18,
            0xffffff);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    private int getRealIndex(int rx) {
        int ax = rx + (page * SECTOR_COUNT);
        if (ax >= menu.getTerminalItems()
            .size()) {
            return -1;
        }
        return ax;
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        currentVisibility.showNEI = false;
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return null;
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        return true;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return true;
    }
}
