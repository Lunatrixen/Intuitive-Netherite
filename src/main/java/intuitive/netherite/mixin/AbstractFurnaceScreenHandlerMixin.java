package intuitive.netherite.mixin;

import intuitive.netherite.Intuneth;
import intuitive.netherite.SoulBlastingRecipeType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class AbstractFurnaceScreenHandlerMixin {

    @Shadow
    protected World world;

    @Inject(method = "isSmeltable", at = @At("RETURN"), cancellable = true)
private void onIsSmeltable(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValue()) return;

    boolean hasSoulBlastingRecipe = this.world.getRecipeManager()
            .getFirstMatch(SoulBlastingRecipeType.SOUL_BLASTING, new SimpleInventory(itemStack), this.world)
            .isPresent();

    Intuneth.LOGGER.info("isSmeltable called for: {} | soulBlasting match: {}", 
        itemStack.getItem(), hasSoulBlastingRecipe);

    if (hasSoulBlastingRecipe) {
        cir.setReturnValue(true);
    }
}
}