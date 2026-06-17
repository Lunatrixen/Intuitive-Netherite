package intuitive.netherite.mixin;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlock.class)
public abstract class AbstractFurnaceBlockMixin extends Block {

  private static final BooleanProperty SOUL_CHARGED = BooleanProperty.of("soul_charged");

  protected AbstractFurnaceBlockMixin(Settings settings) {
    super(settings);
  }

  @Inject(method = "appendProperties", at = @At("TAIL"))
  private void addSoulChargeProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(SOUL_CHARGED);
  }

  @Inject(method = "<init>", at = @At("TAIL"))
  private void setDefaultState(Block.Settings settings, CallbackInfo ci) {
    setDefaultState(getDefaultState().with(SOUL_CHARGED, false));
  }
}