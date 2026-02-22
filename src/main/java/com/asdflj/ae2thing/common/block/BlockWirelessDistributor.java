package com.asdflj.ae2thing.common.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.render.WirelessOverlayRender;
import com.asdflj.ae2thing.client.textures.BlockTexture;
import com.asdflj.ae2thing.common.item.BaseItemBlock;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileWirelessDistributor;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.util.AEColor;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockWirelessDistributor extends BaseTileBlock implements IRegister<BlockWirelessDistributor> {

    public BlockWirelessDistributor() {
        super(Material.iron);
        this.setBlockName(NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        this.setBlockTextureName(AE2Thing.MODID + ":" + NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileWirelessDistributor.class);
        this.hasSubtypes = true;
    }

    @Override
    public ItemStack stack() {
        return new ItemStack(this, 1, 16);
    }

    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase player, ItemStack is) {
        super.onBlockPlacedBy(w, x, y, z, player, is);
        TileEntity b = w.getTileEntity(x, y, z);
        if (b instanceof SubBlocks tile) {
            tile.setDamage(is.getItemDamage());
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        if (world.getTileEntity(x, y, z) instanceof SubBlocks tile) {
            return new ItemStack(this, 1, tile.getDamage());
        }
        return super.getPickBlock(target, world, x, y, z, player);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
        boolean advancedToolTips) {
        toolTip.add(AEColor.values()[itemStack.getItemDamage()].toString());
        if (GuiScreen.isShiftKeyDown()) {
            toolTip.addAll(
                Arrays.asList(
                    I18n.format(NameConst.TT_WIRELESS_DISTRIBUTOR_DESC)
                        .split("\\\\n")));
        } else {
            toolTip.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }
    }

    @Override
    public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
        float hitZ) {
        TileEntity entity = w.getTileEntity(x, y, z);
        if (entity instanceof TileWirelessDistributor tileWirelessDistributor) {
            if (player.isSneaking()) {
                if (Platform.isServer()) {
                    tileWirelessDistributor.alert();
                }
            } else {
                if (Platform.isServer()) {
                    InventoryHandler.openGui(
                        player,
                        w,
                        new BlockPos(x, y, z),
                        ForgeDirection.UNKNOWN,
                        GuiType.WIRELESS_DISTRIBUTOR);
                } else {
                    WirelessOverlayRender.setWirelessDistributor(tileWirelessDistributor);
                }
            }
        }
        return true;
    }

    @Override
    public int getDamageValue(World worldIn, int x, int y, int z) {
        if (worldIn.getTileEntity(x, y, z) instanceof SubBlocks tile) {
            return tile.getDamage();
        }
        return super.getDamageValue(worldIn, x, y, z);
    }

    @Override
    public void setRenderStateByMeta(int itemDamage) {
        // inv
        if (BlockTexture.textureMap.containsKey(NameConst.BLOCK_WIRELESS_DISTRIBUTOR)) {
            BlockTexture.IconWrapper wrapper = BlockTexture.textureMap.get(NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
            IIcon icon = wrapper.get()
                .get(itemDamage);
            this.getRendererInstance()
                .setTemporaryRenderIcons(icon, icon, icon, icon, icon, icon);
        } else {
            super.setRenderStateByMeta(itemDamage);
        }
    }

    @Override
    public BlockWirelessDistributor register() {
        GameRegistry.registerBlock(this, BaseItemBlock.class, NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        GameRegistry.registerTileEntity(TileWirelessDistributor.class, NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public void getCheckedSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemStacks) {
        for (int i = 0; i < AEColor.values().length; i++) {
            itemStacks.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> arr = new ArrayList<>();
        if (world.getTileEntity(x, y, z) instanceof SubBlocks tile) {
            arr.add(new ItemStack(this, 1, tile.getDamage()));
        }
        return arr;
    }

    @Override
    public IIcon getIcon(IBlockAccess w, int x, int y, int z, int s) {
        // world
        TileEntity tile = w.getTileEntity(x, y, z);
        if (tile instanceof TileWirelessDistributor tileWirelessDistributor) {
            if (BlockTexture.textureMap.containsKey(NameConst.BLOCK_WIRELESS_DISTRIBUTOR)) {
                BlockTexture.IconWrapper wrapper = BlockTexture.textureMap.get(NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
                return wrapper.get(tileWirelessDistributor.isPowered())
                    .get(tileWirelessDistributor.getDamage());
            }
        }
        if (tile instanceof IPowerChannelState machine) {
            machine.isPowered();
        }
        return super.getIcon(w, x, y, z, s);
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public void breakBlock(World w, int x, int y, int z, Block a, int b) {
        if (Platform.isServer()) {
            TileEntity entity = w.getTileEntity(x, y, z);
            if (entity instanceof TileWirelessDistributor tileWirelessDistributor) {
                for (TileWirelessDistributor.GridConnectionWrapper wrapper : tileWirelessDistributor
                    .getConnectionWrappers()) {
                    wrapper.connection.destroy();
                }
            }
        }
        super.breakBlock(w, x, y, z, a, b);
    }
}
