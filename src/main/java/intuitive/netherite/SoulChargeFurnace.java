package intuitive.netherite;

import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.BooleanProperty;

public interface SoulChargeFurnace {
  BooleanProperty SOUL_CHARGED = BooleanProperty.of("soul_charged");

  boolean isUsingSoulCharge();
  void setUsingSoulCharge(boolean value);

  public static boolean isUsingSoulCharge(BlastFurnaceBlockEntity blastFurnace) {
    return blastFurnace.getCachedState().getBlock().getTranslationKey().contains("soul_charge");
  }

  ItemStack getLastFuelSnapshot();
  void setLastFuelSnapshot(ItemStack stack);

  ItemStack getLastInputSnapshot();
  void setLastInputSnapshot(ItemStack stack);

  default ItemStack getStoredSoulCharge() {
    return ItemStack.EMPTY;
  }
}