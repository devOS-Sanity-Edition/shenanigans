package one.devos.nautical.shenanigans.mixin;

import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@WrapOperation(
			method = "renderHitOutline",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
			)
	)
	private VoxelShape useDrawerSubShapes(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext ctx, Operation<VoxelShape> original) {
		if ( blockGetter instanceof Level level && state.getBlock() instanceof BasicBlock basicBlock && basicBlock.hasIndividualRenderVoxelShape()) {
			Entity entity = ctx instanceof EntityCollisionContext entityCtx ? entityCtx.getEntity() : null;
			if (entity instanceof LivingEntity living) {
				Minecraft mc = Minecraft.getInstance();
				BlockHitResult hit = (BlockHitResult) Objects.requireNonNull(mc.hitResult);
				float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(false);
				VoxelShape shape = RayTraceUtils.rayTraceVoxelShape(hit, (Level) level, living, 32, partialTicks);
				if (shape != null) {
					return shape;
				}
			}
		}

		return original.call(state, blockGetter, pos, ctx);
	}
}
