package lanse.lanican.mixin;

import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

//TODO - this doesn't work to the fullest extent, and its incompatible with other mods.
//@Mixin(MathHelper.class) // Only modify MathHelper (not MathConstants)
public class PiMixin {

//	@ModifyConstant(method = "*", constant = @Constant(doubleValue = 3.141592653589793))
//	private static double modifyPiDouble(double original) {
//		return 4.0;
//	}
}