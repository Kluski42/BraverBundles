package net.wetnoodle.braverbundles.mixin.allay;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.wetnoodle.braverbundles.config.BBConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Allay.class)
public abstract class AllayMixin extends PathfinderMob {

    protected AllayMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * If the item the allay is looking for is inside its bundle, the allay will consider them equal when determining if the allay wants to pick the item up.
     *
     * @param original  If the items are actually equal.
     * @param itemStack The item stack to be gathered.
     * @return if the items are equal or if the target is found inside the allay's bundle.
     */
    @ModifyExpressionValue(method = "wantsToPickUp", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/allay/Allay;allayConsidersItemEqual(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean braverBundles$allayConsidersItemsEqual(boolean original, @Local(ordinal = 0, argsOnly = true) ItemStack itemStack) {
        if (!BBConfig.ALLAY_WHITELISTERS) {
            return original;
        }
        ItemStack heldStack = this.getItemInHand(InteractionHand.MAIN_HAND);
        // If not holding a bundle, return original value
        if (!heldStack.is(Items.BUNDLE)) return original;
        // if the target stack is also a bundle...
        if (original) {
            // if the bundle is empty, return true
            BundleContents bundleContents = heldStack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundleContents == null || bundleContents.isEmpty()) {
                return true;
            }
        }
        return braverBundles$isItemInBundle(heldStack, itemStack);
    }


    // Helper Methods

    /**
     * If the searched for item is inside the bundle being held by the allay, return true. Else, return false
     *
     * @param bundle    The item held in the allay's hand. If not a bundle, return false.
     * @param itemStack The item being searched for inside the bundle.
     * @return If the item being searched for was found inside the bundle.
     */
    @Unique
    private boolean braverBundles$isItemInBundle(ItemStack bundle, ItemStack itemStack) {
        BundleContents bundleContents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null && !bundleContents.isEmpty()) {
            for (ItemStack bundledItem : bundleContents.items()) {
                if (ItemStack.isSameItem(itemStack, bundledItem)) return true;
            }
        }
        return false;
    }
}
