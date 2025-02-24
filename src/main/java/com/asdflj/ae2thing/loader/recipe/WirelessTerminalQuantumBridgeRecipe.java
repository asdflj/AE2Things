package com.asdflj.ae2thing.loader.recipe;

import static com.asdflj.ae2thing.api.WirelessObject.hasInfinityBoosterCard;

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

public class WirelessTerminalQuantumBridgeRecipe extends ShapelessRecipes {

    public static List<ItemStack> CARDS = new ArrayList<>();
    private static final Set<Class<? extends Item>> terminalClass = new HashSet<>();
    private final ItemStack installedTerm;

    public boolean isQuantumBridgeCard(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        return CARDS.stream()
            .anyMatch(x -> Objects.equals(x.getItem(), is.getItem()));
    }

    private WirelessTerminalQuantumBridgeRecipe(ItemStack term, ItemStack card) {
        super(term, Arrays.asList(term, card));
        this.installedTerm = installQuantumBridgeCard(term);
    }

    public static void registerCard(ItemStack card, ItemStack terminal) {
        if (terminal == null || terminal.getItem() == null) return;
        CARDS.add(card);
        GameRegistry.addRecipe(new WirelessTerminalQuantumBridgeRecipe(terminal, card));
        terminalClass.add(
            terminal.getItem()
                .getClass());
    }

    public static void register(ItemStack terminal) {
        ItemStack card;
        card = GameRegistry.findItemStack("ae2wct", "infinityBoosterCard", 1);
        if (card != null) {
            registerCard(card, terminal);
        }
        card = GameRegistry.findItemStack("ae2fc", "quantum_bridge_card", 1);
        if (card != null) {
            registerCard(card, terminal);
        }
    }

    public static ItemStack getInfinityBoosterCard() {
        Optional<ItemStack> card = CARDS.stream()
            .findFirst();
        return card.orElse(null);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World w) {
        ItemStack term = inv.getStackInSlot(0);
        ItemStack infinityBoosterCard = inv.getStackInSlot(1);
        return term != null && term.getItem() != null
            && terminalClass.contains(
                term.getItem()
                    .getClass())
            && !hasInfinityBoosterCard(term)
            && isQuantumBridgeCard(infinityBoosterCard);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return installQuantumBridgeCard(inv.getStackInSlot(0));
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    private ItemStack installQuantumBridgeCard(ItemStack is) {
        is = is.copy();
        NBTTagCompound data = Platform.openNbtData(is);
        data.setBoolean(Constants.INFINITY_BOOSTER_CARD, true);
        is.setTagCompound(data);
        return is;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return installedTerm;
    }

}
