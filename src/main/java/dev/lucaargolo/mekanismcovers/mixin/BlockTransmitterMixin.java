package dev.lucaargolo.mekanismcovers.mixin;

import dev.lucaargolo.mekanismcovers.MekanismCovers;
import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.block.transmitter.BlockTransmitter;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(BlockTransmitter.class)
public abstract class BlockTransmitterMixin extends BlockMekanism implements IStateFluidLoggable {

    @Shadow(remap = false) protected abstract VoxelShape getRealShape(IBlockReader world, BlockPos pos);

    protected BlockTransmitterMixin(Properties properties) {
        super(properties);
    }

    @Inject(at = @At("HEAD"), method = "use", remap = true, cancellable = true)
    public void getCoverWrenchUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit, CallbackInfoReturnable<ActionResultType> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (MekanismUtils.canUseAsWrench(stack) && player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
                if (tile instanceof TileEntityTransmitterMixed) {
                    TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) tile;
                    if (transmitter.mekanism_covers$getCoverState() != null) {
                        MekanismCovers.removeCover(world, tile, state, pos, transmitter, true);
                        cir.setReturnValue(ActionResultType.SUCCESS);
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getShape", cancellable = true)
    public void getCorrectShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        ItemStack heldItem = playerHeldItem(context);
        if (heldItem != null && heldItem.getItem() == MekanismItems.CONFIGURATOR.get()) {
            ItemConfigurator configurator = (ItemConfigurator) heldItem.getItem();
            if (configurator.getMode(heldItem) == ItemConfigurator.ConfiguratorMode.WRENCH) {
                cir.setReturnValue(getRealShape(world, pos));
            }
        }
    }

    @Unique
    private ItemStack playerHeldItem(ISelectionContext context) {
        if (context instanceof EntityCollisionContextAccessor) {
            Item item = ((EntityCollisionContextAccessor) context).getHeldItem();
            return item != null ? new ItemStack(item) : null;
        }
        return null;
    }

    @Inject(at = @At("HEAD"), method = "getRealShape", cancellable = true, remap = false)
    public void getCoverShape(IBlockReader world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
        if (tile instanceof TileEntityTransmitterMixed) {
            TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) tile;
            if (transmitter.mekanism_covers$getCoverState() != null) {
                cir.setReturnValue(VoxelShapes.block());
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        int ambientLight = super.getLightValue(state, world, pos);
        if (ambientLight != 15) {
            TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
            if (tile instanceof TileEntityTransmitterMixed) {
                TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) tile;
                BlockState coverState = transmitter.mekanism_covers$getCoverState();
                if (coverState != null) {
                    ambientLight = Math.max(ambientLight, coverState.getLightValue(world, pos));
                }
            }
        }
        return ambientLight;
    }
}