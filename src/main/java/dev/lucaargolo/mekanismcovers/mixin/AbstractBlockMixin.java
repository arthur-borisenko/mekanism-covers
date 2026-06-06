package dev.lucaargolo.mekanismcovers.mixin;

import dev.lucaargolo.mekanismcovers.MekanismCovers;
import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.block.transmitter.BlockTransmitter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1. Инжектимся в ванильный AbstractBlock.
// Все моды будут инжектиться сюда, и Mixin аккуратно выстроит их в очередь.
@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    @Inject(
            method = "onRemove(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
            at = @At("HEAD")
    )
    private void onRemoveInject(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {

        // 3. Жесткая проверка: код выполнится ТОЛЬКО если это блок из Mekanism
        if (!((Object) this instanceof BlockTransmitter)) {
            return;
        }

        // --- ТВОЯ ЛОГИКА ---
        if (!state.is(newState.getBlock()) || !newState.getFluidState().isEmpty()) {
            TileEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityTransmitterMixed) {
                TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) blockEntity;
                if (transmitter.mekanism_covers$getCoverState() != null) {
                    MekanismCovers.removeCover(world, blockEntity, state, pos, transmitter, false);
                }
            }
        }
    }
}