package io.github.pistonpoek.allayfilter;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BundleInventory extends SimpleInventory {
    public BundleInventory() {
        super(Items.BUNDLE.getDefaultStack());
    }

    private BundleContentsComponent getBundleContents() {
        return super.getStack(0).getOrDefault(DataComponentTypes.BUNDLE_CONTENTS,
                BundleContentsComponent.DEFAULT);
    }

    /**
     * Update the bundle contents of the single bundle item in this inventory.
     *
     * @param bundleContents new contents to set for the bundle item in this inventory.
     */
    private void updateBundleContents(BundleContentsComponent bundleContents) {
        ItemStack bundle = Items.BUNDLE.getDefaultStack();
        bundle.set(DataComponentTypes.BUNDLE_CONTENTS, bundleContents);
        super.setStack(0, bundle);
    }

    /**
     * Get the bundle stack for this inventory.
     *
     * @return Bundle item stack with inventory contents.
     */
    public ItemStack getBundleStack() {
        return super.getStack(0);
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot > 0) {
            return ItemStack.EMPTY;
        }
        BundleContentsComponent bundleContents = getBundleContents();
        if (bundleContents.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return bundleContents.get(0);
    }

    @Override
    public List<ItemStack> clearToList() {
        List<ItemStack> list = getBundleContents().stream()
                .filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        this.clear();
        return list;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot > 0) {
            return ItemStack.EMPTY;
        }

        BundleContentsComponent bundleContents = getBundleContents();
        ItemStack itemStack = bundleContents.get(0).split(amount);
        if (bundleContents.get(0).isEmpty()) {
            List<ItemStack> items = new ArrayList<>(bundleContents.stream().toList());
            items.removeFirst();
            bundleContents = new BundleContentsComponent(items);
        }
        updateBundleContents(bundleContents);
        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return removeStack(slot, Item.MAX_MAX_COUNT);
    }

    @Override
    public ItemStack addStack(ItemStack stack) {
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(getBundleContents());
        ItemStack itemStack = stack.copy();
        builder.add(itemStack);

        // Update the bundle item stack with the new stack added.
        updateBundleContents(builder.build());

        return itemStack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(getBundleContents());
        int amount = builder.add(stack.copy());
        return amount != 0;
    }

    @Override
    public boolean isEmpty() {
        return getBundleContents().isEmpty();
    }
}
