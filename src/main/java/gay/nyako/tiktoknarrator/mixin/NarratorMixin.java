package gay.nyako.tiktoknarrator.mixin;

import com.mojang.text2speech.Narrator;
import gay.nyako.tiktoknarrator.NarratorTikTok;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Narrator.class)
public interface NarratorMixin {
	@Inject(at = @At("HEAD"), method = "getNarrator()Lcom/mojang/text2speech/Narrator;", cancellable = true, remap = false)
	private static void TikTokNarrator$getNarratorMixin(CallbackInfoReturnable<Narrator> cir) {
		cir.setReturnValue(new NarratorTikTok());
	}
}