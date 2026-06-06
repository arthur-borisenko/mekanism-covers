package dev.lucaargolo.mekanismcovers;


import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;


public class CoverItem extends Item {

    public CoverItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        World level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        TileEntity entity = level.getBlockEntity(pos);
        ItemStack stack = pContext.getItemInHand();
        Block coverBlock = getBlock(stack);
        if (entity instanceof TileEntityTransmitterMixed) {
            TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) entity;
            if (coverBlock != null && coverBlock != Blocks.AIR) {
                BlockState coverState = coverBlock.getStateForPlacement(new BlockItemUseContext(pContext));
                if (!level.isClientSide) {
                    if (transmitter.mekanism_covers$getCoverState() != null) {
                        MekanismCovers.removeCover(level, entity, state, pos, transmitter, true);
                    }
                    transmitter.mekanism_covers$setCoverState(coverState);
                    entity.setChanged();
                    stack.shrink(1);
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.getLightEngine().checkBlock(pos);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltipComponents, ITooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (pLevel != null) {
            Block coverBlock = getBlock(pStack);
            if (coverBlock == null) {
                pTooltipComponents.add(new TranslationTextComponent("text.mekanismcovers.empty").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
            } else {
                pTooltipComponents.add(coverBlock.getName().withStyle(TextFormatting.DARK_PURPLE, TextFormatting.ITALIC));
            }
        }
    }

    @Nullable
    public static Block getBlock(ItemStack stack) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains("CoverBlockItem")) {
            return null;
        } else {
            ItemStack coverBlockItem = ItemStack.of(stack.getOrCreateTag().getCompound("CoverBlockItem"));
            if (coverBlockItem.getItem() instanceof BlockItem) {
                BlockItem blockItem = (BlockItem) coverBlockItem.getItem();
                return blockItem.getBlock();
            } else {
                return null;
            }
        }
    }

}
