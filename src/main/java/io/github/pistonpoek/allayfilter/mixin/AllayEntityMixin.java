package io.github.pistonpoek.allayfilter.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.pistonpoek.allayfilter.AllayInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin extends PathAwareEntity {
	@Shadow protected abstract ActionResult interactMob(PlayerEntity player, Hand hand);

	@Mutable
	@Final
	@Shadow
	private SimpleInventory inventory;

	protected AllayEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void setAllayInventory(CallbackInfo callbackInfo) {
		inventory = new AllayInventory();
	}

	@ModifyExpressionValue(
			method = "canGather",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AllayEntity;areItemsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z")
	)
	private boolean modifyAreItemsEqual(boolean original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		return allayfilter$applyBundleFilter(original, stack);
	}

	/**
	 * Overwrite specified base value for bundle items by applying the bundle as filter.
	 *
	 * @param base Truth assignment to use when hand stack is not a bundle.
	 * @param stack Stack to apply bundle filter to, if hand item is a bundle.
	 * @return Truth assignment, if specified stack passes hand item as bundle or else specified base value.
	 */
	@Unique boolean allayfilter$applyBundleFilter(boolean base, ItemStack stack) {
		ItemStack handItem = this.getStackInHand(Hand.MAIN_HAND);
		if (handItem.getItem() instanceof BundleItem) {
			BundleContentsComponent bundleContents =
					handItem.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
			return allayfilter$matchesBundleFilter(bundleContents, stack);
		}
		return base;
	}

	/**
	 * Check if the specified stack is contained in the bundle contents or the bundle contents is empty.
	 *
	 * @param bundleContents Bundle contents to use as filter.
	 * @param stack Item stack to apply against the bundle filter.
	 * @return Truth assignment, if stack matches bundle contents as filter.
	 */
	@Unique
	private boolean allayfilter$matchesBundleFilter(BundleContentsComponent bundleContents, ItemStack stack) {
		if (bundleContents.isEmpty()) {
			return true;
		}

		for (ItemStack bundleItem:bundleContents.iterate()) {
			if (areItemsEqual(bundleItem, stack)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setStackInHand(Hand hand, ItemStack stack) {
		super.setStackInHand(hand, stack);
		allayfilter$updateBundleFilterUsage();
	}

	@Unique
	private void allayfilter$updateBundleFilterUsage() {
		if (inventory instanceof AllayInventory allayInventory) {
			allayInventory.setUseBundleFilter(allayfilter$useBundleFilter());
		}
	}

	@Inject(method="readCustomDataFromNbt", at=@At("TAIL"))
	public void injectReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
		allayfilter$updateBundleFilterUsage();
	}

	/**
	 * Check if allay should use bundle filter for inventory.
	 *
	 * @return Truth assignment, if bundle filter should be used.
	 */
	@Unique
	private boolean allayfilter$useBundleFilter() {
		return getStackInHand(Hand.MAIN_HAND).getItem() instanceof BundleItem;
	}

	@Shadow
	protected abstract boolean areItemsEqual(ItemStack stack1, ItemStack stack2);
}