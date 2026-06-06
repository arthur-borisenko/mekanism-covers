package dev.lucaargolo.mekanismcovers.mixin;

import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.block.transmitter.BlockTransmitter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();

}
