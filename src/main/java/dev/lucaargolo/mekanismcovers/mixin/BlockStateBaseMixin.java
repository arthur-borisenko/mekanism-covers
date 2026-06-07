package dev.lucaargolo.mekanismcovers.mixin;

import mekanism.common.block.transmitter.BlockTransmitter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockVoxelShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();
    @Shadow public abstract VoxelShape getOcclusionShape(IBlockReader world, BlockPos pos);
    @Shadow public abstract VoxelShape getCollisionShape(IBlockReader world, BlockPos pos);

    // 1. Обычный хитбокс (getShape) — для выделения рамкой игрока
    @Inject(at = @At("HEAD"), method = "getShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;", cancellable = true)
    public void bypassShapeCache(IBlockReader world, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(this.getBlock().getShape((BlockState) (Object) this, world, pos, context));
        }
    }

    // 2. Хитбокс коллизий (getCollisionShape) — физика хождения сущностей
    @Inject(at = @At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;", cancellable = true)
    public void bypassCollisionShapeCache(IBlockReader world, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(this.getBlock().getCollisionShape((BlockState) (Object) this, world, pos, context));
        }
    }

    // 3. Прочность граней (getFaceOcclusionShape) — для зазора рендера соседних блоков
    @Inject(at = @At("HEAD"), method = "getFaceOcclusionShape", cancellable = true)
    public void bypassFaceOcclusionCache(IBlockReader world, BlockPos pos, Direction direction, CallbackInfoReturnable<VoxelShape> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(VoxelShapes.getFaceShape(this.getOcclusionShape(world, pos), direction));
        }
    }
    @Inject(at = @At("HEAD"), method = "getOcclusionShape", cancellable = true)
    public void transmitterOcclusionNoXray(IBlockReader world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(VoxelShapes.empty());
        }
    }
    // 4. Прозрачность (isSolidRender) — определяет, прятать ли грани соседних блоков
    @Inject(at = @At("HEAD"), method = "isSolidRender", cancellable = true)
    public void transmitterNoXray(IBlockReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(false);
        }
    }

    // 5. Пропускание небесного света (propagatesSkylightDown)
    @Inject(at = @At("HEAD"), method = "propagatesSkylightDown", cancellable = true)
    public void bypassPropagatesSkylightDownCache(IBlockReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(this.getBlock().propagatesSkylightDown((BlockState) (Object) this, world, pos));
        }
    }

    // 6. Степень гашения света внутри блока (getLightBlock)
    @Inject(at = @At("HEAD"), method = "getLightBlock", cancellable = true)
    public void bypassGetLightBlockCache(IBlockReader world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(this.getBlock().getLightBlock((BlockState) (Object) this, world, pos));
        }
    }

    // 7. Проверка на огромный хитбокс (hasLargeCollisionShape) — для стыков чанков
    @Inject(at = @At("HEAD"), method = "hasLargeCollisionShape", cancellable = true)
    public void bypassHasLargeCollisionShapeCache(CallbackInfoReturnable<Boolean> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            cir.setReturnValue(true);
        }
    }

    // 8. ДОБАВЛЕНО: Прочность грани с типом поддержки (isFaceSturdy с 4 аргументами)
    // Ключевой метод для установки механизмов, проводов и каверов на сторону блока!
    @Inject(at = @At("HEAD"), method = "isFaceSturdy(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;Lnet/minecraft/util/BlockVoxelShape;)Z", cancellable = true)
    public void bypassIsFaceSturdyCache(IBlockReader world, BlockPos pos, Direction face, BlockVoxelShape supportType, CallbackInfoReturnable<Boolean> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            // Если это кабель — полностью игнорируем cache.isFaceSturdy и считаем динамически через тип поддержки
            cir.setReturnValue(supportType.isSupporting((BlockState) (Object) this, world, pos, face));
        }
    }

    // 9. ДОБАВЛЕНО: Является ли хитбокс коллизии полным блоком (isCollisionShapeFullBlock)
    // Влияет на патфайдинг мобов и удушение сущностей внутри блока
    @Inject(at = @At("HEAD"), method = "isCollisionShapeFullBlock", cancellable = true)
    public void bypassIsCollisionShapeFullBlockCache(IBlockReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.getBlock() instanceof BlockTransmitter) {
            // Считаем на лету на основе нашего динамического хитбокса коллизий
            cir.setReturnValue(Block.isShapeFullBlock(this.getCollisionShape(world, pos)));
        }
    }
}