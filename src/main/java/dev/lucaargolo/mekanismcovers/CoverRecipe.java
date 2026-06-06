package dev.lucaargolo.mekanismcovers;


import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.MapExtendingRecipe;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.World;

import java.util.Objects;

public class CoverRecipe extends SpecialRecipe {

    public CoverRecipe(ResourceLocation pId) {
        super(pId);
    }

    public boolean matches(CraftingInventory pInv, @Nonnull World pLevel) {
        int coverQnt = 0;
        boolean coverFull = false;
        int blockQnt = 0;
        for (int slot = 0; slot < pInv.getContainerSize(); ++slot) {
            ItemStack stack = pInv.getItem(slot);
            if (!stack.isEmpty()) {
                net.minecraft.item.Item item = stack.getItem();
                if (item == MekanismCovers.COVER.get() || item == MekanismCovers.EMPTY_COVER.get()) {
                    coverQnt++;
                    coverFull = coverFull || (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("CoverBlockItem"));
                } else if (item instanceof BlockItem) {
                    Block block = ((BlockItem) item).getBlock();

                    // Проверяем, что это не блок с TileEntity
                    if (block.hasTileEntity(block.defaultBlockState())) {
                        return false;
                    }

                    // Используем дефолтное состояние
                    BlockState state = block.defaultBlockState();

                    if (block.isShapeFullBlock(state.getCollisionShape(pLevel, BlockPos.ZERO)) || block == Blocks.GLASS) {
                        blockQnt++;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return coverQnt == 1 && ((!coverFull && blockQnt == 1) || (coverFull && blockQnt == 0));
    }

    public @Nonnull ItemStack assemble(CraftingInventory pInv) {
        Block block = Blocks.AIR;
        boolean isCleanOperation = false;
        for (int slot = 0; slot < pInv.getContainerSize(); ++slot) {
            ItemStack stack = pInv.getItem(slot);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item == MekanismCovers.COVER.get() || item == MekanismCovers.EMPTY_COVER.get()) {
                    isCleanOperation = isCleanOperation || (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("CoverBlockItem"));
                } else if (item instanceof BlockItem) {
                    Block b = ((BlockItem) item).getBlock();
                    // Также проверяем здесь
                    if (!b.hasTileEntity(b.defaultBlockState())) {
                        block = b;
                    }
                }
            }
        }
        if (isCleanOperation) {
            return new ItemStack(MekanismCovers.EMPTY_COVER.get());
        } else if (block != Blocks.AIR) {
            ItemStack result = new ItemStack(MekanismCovers.COVER.get());
            result.getOrCreateTag().put("CoverBlockItem", block.asItem().getDefaultInstance().save(new CompoundNBT()));
            return result;
        }
        //Technically it should never reach this.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public @Nonnull NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInventory pContainer) {
        NonNullList<ItemStack> list = NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < list.size(); ++i) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("CoverBlockItem")) {
                ItemStack remaining = ItemStack.of(Objects.requireNonNull(stack.getTag()).getCompound("CoverBlockItem"));
                list.set(i, remaining);
            } else if (stack.getItem() instanceof BlockItem) {
                ItemStack remaining = stack.copy();
                remaining.setCount(1);
                list.set(i, remaining);
            }
        }

        return list;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return MekanismCovers.COVER_SERIALIZER.get();
    }


    public @Nonnull ItemStack getResultItem() {
        return new ItemStack(MekanismCovers.COVER.get());
    }


}
