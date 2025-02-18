package vg.skye.trinkhide.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.TrinketFeatureRenderer;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vg.skye.trinkhide.TrinkHide;
import vg.skye.trinkhide.TrinkHideComponents;

@Mixin(TrinketFeatureRenderer.class)
public class TrinketFeatureRendererMixin {
    @Inject(method = "lambda$render$0", at = @At("HEAD"), cancellable = true)
    private void skipRender(PoseStack matrices, ItemStack stack, SlotReference slotReference, MultiBufferSource vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, TrinketRenderer renderer, CallbackInfo ci) {
        var name = TrinkHide.getSlotName(slotReference);
        var component = TrinkHideComponents.HIDDEN_TRINKETS.getNullable(entity);
        if (component != null && component.getHiddenSlots().contains(name)) {
            ci.cancel();
        }
    }
}
