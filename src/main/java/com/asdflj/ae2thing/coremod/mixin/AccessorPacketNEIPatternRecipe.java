package com.asdflj.ae2thing.coremod.mixin;

import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;

import appeng.container.AEBaseContainer;

@Mixin(value = PacketNEIPatternRecipe.class)
public interface AccessorPacketNEIPatternRecipe {

    @Accessor(remap = false)
    NBTTagCompound getOutput();

    @Accessor(remap = false)
    NBTTagCompound getInput();

    @Invoker(remap = false)
    void callCraftingTableRecipeHandler(AEBaseContainer container, PacketNEIPatternRecipe message);

}
