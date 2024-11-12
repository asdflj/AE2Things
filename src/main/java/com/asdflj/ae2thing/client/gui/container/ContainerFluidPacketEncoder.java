package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.tile.TileFluidPacketEncoder;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerFluidPacketEncoder extends BaseNetworkContainer implements IOptionalSlotHost {

    @SideOnly(Side.CLIENT)
    private GuiTextField textField;

    @GuiSync(2)
    public long EmitterValue = 0;

    private final TileFluidPacketEncoder tile;

    private static class OptionalSlotFakeTypeOnly extends OptionalSlotFake {

        TileFluidPacketEncoder tile;

        public OptionalSlotFakeTypeOnly(IInventory inv, TileFluidPacketEncoder tile, IOptionalSlotHost containerBus,
            int idx, int x, int y, int offX, int offY, int groupNum) {
            super(inv, containerBus, idx, x, y, offX, offY, groupNum);
            this.tile = tile;
        }

        @Override
        public void putStack(ItemStack is) {
            FluidStack fluidStack = Util.getFluidFromItem(is);
            if (fluidStack != null) {
                ItemStack tmp = ItemFluidPacket.newDisplayStack(fluidStack);
                super.putStack(tmp);
                if (Platform.isServer()) {
                    tile.configureWatchers();
                }
            } else {
                super.putStack(null);
            }
        }
    }

    public ContainerFluidPacketEncoder(InventoryPlayer ip, TileFluidPacketEncoder host) {
        super(ip, host);
        tile = host;
        this.setupConfig();

        this.bindPlayerInventory(ip, 0, 184 - /* height of player inventory */ 82);
    }

    public void setLevel(final long l) {
        this.tile.setReportingValue(l);
        this.EmitterValue = l;
        this.tile.configureWatchers();
    }

    protected void setupConfig() {
        final IInventory inv = getFakeFluidInv();
        final int y = 40;
        final int x = 80 + 44;
        this.addSlotToContainer(new OptionalSlotFakeTypeOnly(inv, this.tile, this, 0, x, y, 0, 0, 0));
    }

    @SideOnly(Side.CLIENT)
    public void setTextField(final GuiTextField level) {
        this.textField = level;
        this.textField.setText(String.valueOf(this.EmitterValue));
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.EmitterValue = this.tile.getReportingValue();
        }
        super.detectAndSendChanges();

    }

    public AppEngInternalAEInventory getFakeFluidInv() {
        return (AppEngInternalAEInventory) this.tile.getInventoryByName(Constants.CONFIG_INV);
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("EmitterValue")) {
            if (this.textField != null) {
                this.textField.setText(String.valueOf(this.EmitterValue));
            }
        }
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        return true;
    }
}
