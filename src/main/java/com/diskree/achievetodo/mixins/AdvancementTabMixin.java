package com.diskree.achievetodo.mixins;

import com.diskree.achievetodo.server.AchieveToDoServer;
import com.diskree.achievetodo.advancements.AdvancementRoot;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementTabType;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {

    @Unique
    private static Map<AdvancementTabType, List<AdvancementRoot>> tabLocations  = new HashMap<>() {{
        put(AdvancementTabType.LEFT, List.of(AdvancementRoot.BIOMES, AdvancementRoot.ADVENTURE, AdvancementRoot.WEAPONRY, AdvancementRoot.HUSBANDRY, AdvancementRoot.MONSTERS));
        put(AdvancementTabType.ABOVE, List.of(AdvancementRoot.MINING, AdvancementRoot.BUILDING, AdvancementRoot.FARMING, AdvancementRoot.NETHER, AdvancementRoot.END));
        put(AdvancementTabType.RIGHT, List.of(AdvancementRoot.ACTION, AdvancementRoot.STATISTICS, AdvancementRoot.BACAP));
        put(AdvancementTabType.BELOW, List.of(AdvancementRoot.REDSTONE, AdvancementRoot.POTION, AdvancementRoot.ENCHANTING, AdvancementRoot.CHALLENGES));
    }};

    @Shadow
    @Final
    private AdvancementsScreen screen;

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void createInject(MinecraftClient client, AdvancementsScreen screen, int index, Advancement root, CallbackInfoReturnable<AdvancementTab> cir) {
        if (root == null || root.getDisplay() == null) {
            cir.setReturnValue(null);
            return;
        }
        String rootName = root.getId().getPath().split("/")[0];
        AdvancementRoot tabToAdd = AdvancementRoot.valueOf(rootName.toUpperCase());

        AdvancementTabType tabGravity = null;
        int order = 0;

        for (Map.Entry<AdvancementTabType, List<AdvancementRoot>> entry : tabLocations.entrySet()) {
            List<AdvancementRoot> tabs = entry.getValue();
            for (int i = 0; i < tabs.size(); i++) {
                AdvancementRoot tab = tabs.get(i);
                if (tab == tabToAdd) {
                    tabGravity = entry.getKey();
                    order = i;
                    break;
                }
            }
            if (tabGravity != null) {
                break;
            }
        }

        cir.setReturnValue(new AdvancementTab(client, screen, tabGravity, order, root, root.getDisplay()));
    }

    @ModifyConstant(method = "move", constant = @Constant(intValue = 234), require = 2)
    private int moveModifyWidth(int orig) {
        return screen.width - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2 - 2 * 9;
    }

    @ModifyConstant(method = "move", constant = @Constant(intValue = 113), require = 2)
    private int moveModifyHeight(int orig) {
        return screen.height - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2 - 3 * 9;
    }

    @ModifyConstant(method = "drawWidgetTooltip", constant = @Constant(intValue = 234), require = 2)
    private int drawWidgetTooltipModifyWidth(int orig) {
        return screen.width - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2 - 2 * 9;
    }

    @ModifyConstant(method = "drawWidgetTooltip", constant = @Constant(intValue = 113), require = 2)
    private int drawWidgetTooltipModifyHeight(int orig) {
        return screen.height - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2 - 3 * 9;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 234), require = 1)
    private int renderModifyWidth(int orig) {
        return screen.width - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2 - 2 * 9;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 113), require = 1)
    private int renderModifyHeight(int orig) {
        return screen.height - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2 - 3 * 9;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 117), require = 1)
    private int renderModifyOriginX(int orig) {
        return screen.width / 2 - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN - 2 * 9 / 2;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 56), require = 1)
    private int renderModifyOriginY(int orig) {
        return screen.height / 2 - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN - 3 * 9 / 2;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 15), require = 1)
    private int renderModifyTextureX(int orig) {
        return (screen.width - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2) / 16 + 1;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 8), require = 1)
    private int renderModifyTextureY(int orig) {
        return (screen.height - AchieveToDoServer.ADVANCEMENTS_SCREEN_MARGIN * 2) / 16 + 1;
    }
}
