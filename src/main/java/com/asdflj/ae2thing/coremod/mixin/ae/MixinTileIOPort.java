package com.asdflj.ae2thing.coremod.mixin.ae;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.tile.TileExIOPort;

import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.storage.TileIOPort;

@Mixin(TileIOPort.class)
public abstract class MixinTileIOPort extends AENetworkInvTile {

    @ModifyVariable(method = "transferContents", at = @At(value = "HEAD"), remap = false, ordinal = 0, argsOnly = true)
    private long transferContents(long itemsToMove) {
        if (this.getTile() instanceof TileExIOPort) {
            itemsToMove *= Config.exIOPortTransferContentsRate;
        }
        return itemsToMove;
    }
}
