package com.asdflj.ae2thing.inventory;

import net.minecraft.inventory.IInventory;

import com.asdflj.ae2thing.inventory.item.INetworkTerminal;

public interface IPatternTerminal extends INetworkTerminal {

    IInventory getInventoryByName(final String name);

    boolean isInverted();

    boolean canBeSubstitute();

    boolean isPrioritize();

    boolean isSubstitution();

    boolean shouldCombine();

    void setCraftingRecipe(final boolean craftingMode);

    void setSubstitution(boolean canSubstitute);

    void setBeSubstitute(boolean canBeSubstitute);

    void setCombineMode(boolean shouldCombine);

    void setPrioritization(boolean canPrioritize);

    void setInverted(boolean inverted);

    int getActivePage();

    void setActivePage(int activePage);

    boolean isCraftingRecipe();

    default void sortCraftingItems() {}

    void saveSettings();

    boolean hasRefillerUpgrade();
}
