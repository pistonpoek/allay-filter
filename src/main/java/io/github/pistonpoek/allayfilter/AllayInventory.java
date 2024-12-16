package io.github.pistonpoek.allayfilter;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AllayInventory extends SimpleInventory {
    private boolean useBundleFilter = false;
    private boolean usingBundle = false;
    private final BundleInventory bundleInventory;

    public AllayInventory() {
        super(1);
        this.bundleInventory = new BundleInventory();
        this.bundleInventory.addListener(sender -> {
            if (sender instanceof BundleInventory inventory) {
                super.setStack(0, inventory.getBundleStack());
                this.markDirty();
            }
        });
    }

    /**
     * Update the inventory to use the bundle filter when possible.
     *
     * @param useBundleFilter Truth assignment, if bundle filter should be used.
     */
    public void setUseBundleFilter(boolean useBundleFilter) {
        this.useBundleFilter = useBundleFilter;
        if (this.isEmpty()) {
            this.usingBundle = useBundleFilter;
        }
    }

    @Override
    public List<ItemStack> clearToList() {
        return usingBundle ?
                bundleInventory.clearToList() :
                super.clearToList();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removedItem = usingBundle ?
                bundleInventory.removeStack(slot, amount) :
                super.removeStack(slot, amount);

        if (slot == 0 && getStack(0).isEmpty()) {
            updateBundleUsage();
        }
        return removedItem;
    }

    public ItemStack removeItem(Item item, int count) {
        return usingBundle ? bundleInventory.removeItem(item, count) : super.removeItem(item, count);
    }

    @Override
    public ItemStack addStack(ItemStack stack) {
        return usingBundle ? bundleInventory.addStack(stack) : super.addStack(stack);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (usingBundle != useBundleFilter) {
            return false;
        }

        return useBundleFilter ? this.bundleInventory.canInsert(stack) : super.canInsert(stack);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removedItem = usingBundle ? bundleInventory.removeStack(slot) : super.removeStack(slot);
        if (slot == 0 && getStack(0).isEmpty()) {
            updateBundleUsage();
        }
        return removedItem;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0 && stack == ItemStack.EMPTY) {
            updateBundleUsage();
        }
        if (usingBundle) {
            bundleInventory.setStack(slot, stack);
        } else {
            super.setStack(slot, stack);
        }
    }

    @Override
    public boolean isEmpty() {
        return usingBundle ? bundleInventory.isEmpty() : super.isEmpty();
    }

    private void updateBundleUsage() {
        usingBundle = useBundleFilter;
    }

    @Override
    public void clear() {
        if (usingBundle) {
            bundleInventory.clear();
        } else {
            super.clear();
        }
        updateBundleUsage();
    }

}
