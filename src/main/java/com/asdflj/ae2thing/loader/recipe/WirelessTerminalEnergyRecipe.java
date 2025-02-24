package com.asdflj.ae2thing.loader.recipe;

import static com.asdflj.ae2thing.api.WirelessObject.hasEnergyCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.asdflj.ae2thing.api.Constants;

import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class WirelessTerminalEnergyRecipe extends ShapelessRecipes {

    private final ItemStack installedTerm;
    public static List<ItemStack> CARDS = new ArrayList<>();
    private static final Set<Class<? extends Item>> terminalClass = new HashSet<>();

    public WirelessTerminalEnergyRecipe(ItemStack term, ItemStack card) {
        super(term, Arrays.asList(term, card));
        this.installedTerm = installEnergyCard(term);
    }

    private boolean isEnergyCard(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        return CARDS.stream()
            .anyMatch(x -> Objects.equals(x.getItem(), is.getItem()));
    }

    public static void registerCard(ItemStack card, ItemStack terminal) {
        if (terminal == null || terminal.getItem() == null) return;
        CARDS.add(card);
        GameRegistry.addRecipe(new WirelessTerminalEnergyRecipe(terminal, card));
        terminalClass.add(
            terminal.getItem()
                .getClass());
    }

    public static ItemStack getEnergyCard() {
        Optional<ItemStack> card = CARDS.stream()
            .findFirst();
        return card.orElse(null);
    }

    public static void register(ItemStack terminal) {
        ItemStack card;
        card = GameRegistry.findItemStack("ae2fc", "energy_card", 1);
        if (card != null) {
            registerCard(card, terminal);
        }
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack card = inv.getStackInSlot(1);
        return term != null && term.getItem() != null
            && terminalClass.contains(
                term.getItem()
                    .getClass())
            && !hasEnergyCard(term)
            && isEnergyCard(card);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return installEnergyCard(inv.getStackInSlot(0));
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    private ItemStack installEnergyCard(ItemStack is) {
        is = is.copy();
        NBTTagCompound data = Platform.openNbtData(is);
        data.setBoolean(Constants.INFINITY_ENERGY_CARD, true);
        is.setTagCompound(data);
        return is;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return installedTerm;
    }
}
