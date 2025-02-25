package com.asdflj.ae2thing.coremod.mixin.br;

import static blockrenderer6343.client.utils.BRUtil.getIngredients;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;

import blockrenderer6343.client.renderer.WorldSceneRenderer;
import blockrenderer6343.client.utils.BRUtil;
import codechicken.nei.recipe.GuiRecipe;

@Mixin(BRUtil.class)
public abstract class MixinBRUtil {

    @Inject(method = "neiOverlay", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ae2thing$neiOverlay(WorldSceneRenderer renderer, CallbackInfo ci) {
        List<ItemStack> ingredients = getIngredients(renderer);
        if (AE2ThingAPI.instance()
            .terminal()
            .isPatternTerminal()) {
            try {
                ImmutablePair<List<OrderStack<?>>, List<OrderStack<?>>> result = com.asdflj.ae2thing.util.BRUtil.handler
                    .handler(ingredients);
                AE2Thing.proxy.netHandler.sendToServer(
                    new CPacketTransferRecipe(
                        result.left,
                        result.right,
                        false,
                        GuiScreen.isShiftKeyDown(),
                        Constants.NEI_BR));
            } catch (Exception ignored) {}
        }
        GuiRecipe<?> currentScreen = (GuiRecipe<?>) Minecraft.getMinecraft().currentScreen;
        Minecraft.getMinecraft()
            .displayGuiScreen(currentScreen.firstGui);
        ci.cancel();
    }
}
