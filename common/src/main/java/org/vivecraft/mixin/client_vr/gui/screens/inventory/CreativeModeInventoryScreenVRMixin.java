package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenVRMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private EditBox searchBox;

    @Shadow
    private static int selectedTab;

    public CreativeModeInventoryScreenVRMixin(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;scrollOffs:F", shift = At.Shift.BEFORE), method = "refreshSearchResults")
    public void vivecraft$search(CallbackInfo ci) {
        // only add to actual search
        if (selectedTab == CreativeModeTab.TAB_SEARCH.getId()) {
            vivecraft$addCreativeSearch(this.searchBox.getValue(), this.menu.items);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;fillItemList(Lnet/minecraft/core/NonNullList;)V", shift = At.Shift.AFTER), method = "selectTab")
    public void vivecraft$fill(CreativeModeTab creativeModeTab, CallbackInfo ci) {
        vivecraft$addCreativeItems(creativeModeTab, this.menu.items);
    }

    @Unique
    private void vivecraft$addCreativeItems(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (tab == CreativeModeTab.TAB_FOOD || tab == null) {
            ItemStack itemstack = (new ItemStack(Items.PUMPKIN_PIE)).setHoverName(new TextComponent("EAT ME"));
            ItemStack itemstack1 = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER).setHoverName(new TextComponent("DRINK ME"));
            itemstack1.getTag().putInt("HideFlags", 32);
            list.add(itemstack);
            list.add(itemstack1);
        }

        if (tab == CreativeModeTab.TAB_TOOLS || tab == null) {
            ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
            boots.setHoverName(new TranslatableComponent("vivecraft.item.jumpboots"));
            boots.getTag().putBoolean("Unbreakable", true);
            boots.getTag().putInt("HideFlags", 4);
            boots.getOrCreateTagElement(ItemStack.TAG_DISPLAY).putInt(ItemStack.TAG_COLOR, 9233775);

            ItemStack claws = new ItemStack(Items.SHEARS);
            claws.setHoverName(new TranslatableComponent("vivecraft.item.climbclaws"));
            claws.getTag().putBoolean("Unbreakable", true);
            claws.getTag().putInt("HideFlags", 4);

            list.add(boots);
            list.add(claws);
        }
    }

    @Unique
    private void vivecraft$addCreativeSearch(String query, NonNullList<ItemStack> list) {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        vivecraft$addCreativeItems(null, nonnulllist);

        for (ItemStack itemstack : nonnulllist) {
            if (query.isEmpty() || itemstack.getHoverName().toString().toLowerCase().contains(query.toLowerCase())) {
                list.add(itemstack);
            }
        }
    }
}
