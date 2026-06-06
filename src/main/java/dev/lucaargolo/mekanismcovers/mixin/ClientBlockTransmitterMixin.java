package dev.lucaargolo.mekanismcovers.mixin;

import dev.lucaargolo.mekanismcovers.MekanismCoversClient;
import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.block.transmitter.BlockTransmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockTransmitter.class)
public class ClientBlockTransmitterMixin {

    @Unique
    private static final Random mekanism_covers$RAND = new Random();

    @Inject(method = "getShape", at = @At("RETURN"), cancellable = true)
    private void wrapOcclusion(BlockState state, IBlockReader world, BlockPos pos, net.minecraft.util.math.shapes.ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
//        if (MekanismCoversClient.isCoverTransparentFast()) {
//            cir.setReturnValue(VoxelShapes.empty());
//            return;
//        }
        if (state.getBlock() instanceof BlockTransmitter && world.getBlockEntity(pos) instanceof TileEntityTransmitterMixed) {
            TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) world.getBlockEntity(pos);
            if (transmitter.mekanism_covers$getCoverState() != null) {
                cir.setReturnValue(VoxelShapes.block());
            }
        }
    }
}
