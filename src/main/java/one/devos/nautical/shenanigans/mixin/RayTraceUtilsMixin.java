package one.devos.nautical.shenanigans.mixin;

import com.hrznstudio.titanium.util.RayTraceUtils;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RayTraceUtils.class)
public class RayTraceUtilsMixin {
	@ModifyExpressionValue(
			method = "rayTraceVoxelShape",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private static Vec3 projectIntoSublevel(Vec3 original,
											@Local(argsOnly = true) Level level,
											@Local(argsOnly = true) BlockHitResult hit,
											@Local(argsOnly = true) float partialTicks,
											@Local(name = "vec3d") LocalRef<Vec3> eyePos) {
		SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, hit.getBlockPos());
		if (subLevel == null)
			return original;

		eyePos.set(projectAbsoluteIntoSublevel(subLevel, eyePos.get(), partialTicks));
		return projectRelativeIntoSublevel(subLevel, original, partialTicks);
	}

	@Unique
	private static Vec3 projectAbsoluteIntoSublevel(SubLevelAccess sublevel, Vec3 pos, float partialTicks) {
		Pose3dc pose = getPose(sublevel, partialTicks);
		return JOMLConversion.toMojang(pose.transformPositionInverse(JOMLConversion.toJOML(pos)));
	}

	@Unique
	private static Vec3 projectRelativeIntoSublevel(SubLevelAccess sublevel, Vec3 pos, float partialTicks) {
		Pose3dc pose = getPose(sublevel, partialTicks);
		return JOMLConversion.toMojang(pose.transformNormalInverse(JOMLConversion.toJOML(pos)));
	}

	@Unique
	private static Pose3dc getPose(SubLevelAccess subLevel, float partialTicks) {
		if (subLevel instanceof ClientSubLevelAccess clientSubLevel) {
			return clientSubLevel.renderPose(partialTicks);
		}

		return subLevel.logicalPose();
	}
}
