package com.asdflj.ae2thing.api;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.storage.CellInventory;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;

public class MagnetObject {

    public enum Mode {
        Off,
        Inv,
        Backpack
    }

    public static String modeKey = Constants.MAGNET_MODE_KEY;
    private static final int range = Config.magnetRange;

    private final NBTTagCompound data;
    private Mode currentMode;
    private IItemList<IAEItemStack> blackList;
    private final ItemStack item;
    private CellInventory inventory;

    public static MagnetObject getMagnet(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBackpackTerminal) {
                return new MagnetObject(stack);
            }
        }
        return null;
    }

    public MagnetObject(ItemStack is) {
        this.item = is;
        this.data = Platform.openNbtData(is);
        this.currentMode = Mode.values()[data.getByte(modeKey)];
    }

    private void addBlackItems(IAEItemStack is) {
        this.getBlackList()
            .add(is);
    }

    public void clearBlackList() {
        this.blackList = AEApi.instance()
            .storage()
            .createPrimitiveItemList();
    }

    private IItemList<IAEItemStack> getBlackList() {
        if (this.blackList == null) {
            this.blackList = AEApi.instance()
                .storage()
                .createPrimitiveItemList();
        }
        return this.blackList;
    }

    public boolean injectItems(EntityPlayer player, EntityItem itemEntity) {
        try {
            if (this.inventory == null) {
                this.inventory = new CellInventory(this.item, null, player);
            }
            ItemStack stack = itemEntity.getEntityItem();
            IAEItemStack is = AEApi.instance()
                .storage()
                .createItemStack(stack);
            if (this.getBlackList()
                .findPrecise(is) != null) return false;
            IAEItemStack result = this.inventory.injectItems(is, Actionable.MODULATE, null);
            if (result != null) {
                stack.stackSize = (int) result.getStackSize();
                this.addBlackItems(result);
                return false;
            }
            return true;

        } catch (Exception ignored) {

        }
        return false;
    }

    public Mode getMode() {
        return currentMode;
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        data.setByte(modeKey, (byte) mode.ordinal());
    }

    public Mode getNextMode() {
        return Mode.values()[(this.getMode()
            .ordinal() + 1) % Mode.values().length];
    }

    public void setNextMode() {
        this.setMode(this.getNextMode());
    }

    public void doMagnet(World world, Entity entity) {
        if (!(entity instanceof EntityPlayer)) return;
        int range = MagnetObject.range;

        List<EntityItem> items = world.getEntitiesWithinAABB(
            EntityItem.class,
            AxisAlignedBB.getBoundingBox(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ)
                .expand(range, range, range));

        for (EntityItem item : items) {
            if (item.getEntityItem() == null) {
                continue;
            }
            if (item.delayBeforeCanPickup > 0) {
                item.delayBeforeCanPickup = 0;
            }
            item.motionX = 0;
            item.motionY = 0;
            item.motionZ = 0;
            item.setPosition(
                entity.posX - 0.2 + (world.rand.nextDouble() * 0.4),
                entity.posY - 0.6,
                entity.posZ - 0.2 + (world.rand.nextDouble() * 0.4));

            if (Platform.isServer() && this.isBackpack()) {
                if (this.injectItems((EntityPlayer) entity, item)) {
                    item.setDead();
                }
            }
        }
        world.playSoundAtEntity(
            entity,
            "random.orb",
            0.1F,
            0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 2F));
        if (!world.isRemote) {
            List<EntityXPOrb> xp = world.getEntitiesWithinAABB(
                EntityXPOrb.class,
                AxisAlignedBB
                    .getBoundingBox(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ)
                    .expand(4, 4, 4));
            EntityPlayer player = (EntityPlayer) entity;
            for (EntityXPOrb orb : xp) {
                if (orb.field_70532_c == 0 && orb.isEntityAlive()) {
                    if (MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(player, orb))) continue;
                    world.playSoundAtEntity(
                        player,
                        "random.orb",
                        0.1F,
                        0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
                    player.onItemPickup(orb, 1);
                    player.addExperience(orb.xpValue);
                    orb.setDead();
                }
            }
        }
    }

    public boolean isBackpack() {
        return this.getMode() == Mode.Backpack;
    }

    public boolean isInv() {
        return this.getMode() == Mode.Inv;
    }

    public boolean isOff() {
        return this.getMode() == Mode.Off;
    }
}
