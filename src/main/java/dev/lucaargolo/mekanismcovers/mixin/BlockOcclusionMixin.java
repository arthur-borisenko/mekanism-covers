package dev.lucaargolo.mekanismcovers.mixin;

import mekanism.common.block.transmitter.BlockTransmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockOcclusionMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void onShouldRenderFace(BlockState state, IBlockReader world, BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof BlockTransmitter) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEntityTransmitterMixed) {
                TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) tile;
                if (transmitter.mekanism_covers$getCoverState() != null) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}