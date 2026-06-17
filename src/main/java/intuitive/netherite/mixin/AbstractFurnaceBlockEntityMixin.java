package intuitive.netherite.mixin;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intuitive.netherite.IntunethItems;
import intuitive.netherite.SoulBlastingRecipe;
import intuitive.netherite.SoulBlastingRecipeType;
import intuitive.netherite.SoulChargeFurnace;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements SoulChargeFurnace {

  @Unique
  private static final Logger LOGGER = LogManager.getLogger("SoulChargeFurnace");

  @Unique
  private ItemStack lastFuelSnapshot = ItemStack.EMPTY;

  @Unique
  private ItemStack lastInputSnapshot = ItemStack.EMPTY;

  @Unique
  private boolean usingSoulCharge = false;

  @Unique
  private int burnTimeBeforeVanilla = 0;

  @Unique
  private ItemStack fuelSlotBeforeVanilla = ItemStack.EMPTY;

  @Override
  public ItemStack getLastFuelSnapshot() { return lastFuelSnapshot; }

  @Override
  public void setLastFuelSnapshot(ItemStack stack) { this.lastFuelSnapshot = stack; }

  @Override
  public ItemStack getLastInputSnapshot() { return lastInputSnapshot; }

  @Override
  public void setLastInputSnapshot(ItemStack stack) { this.lastInputSnapshot = stack; }

  @Override
  public boolean isUsingSoulCharge() { return usingSoulCharge; }

  @Override
  public void setUsingSoulCharge(boolean value) { this.usingSoulCharge = value; }

  @Inject(method = "tick", at = @At("HEAD"))
  private static void onTickHead(World world, BlockPos pos, BlockState state,
      AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {

    if (world.isClient) return;

    AbstractFurnaceBlockEntityMixin self = (AbstractFurnaceBlockEntityMixin)(Object) blockEntity;
    FurnaceAccessor furnace = (FurnaceAccessor) blockEntity;
    SoulChargeFurnace soulFurnace = (SoulChargeFurnace) blockEntity;
    DefaultedList<ItemStack> inventory = furnace.getInventory();

    int burnTime = furnace.getBurnTime();
    ItemStack fuelSlot = inventory.get(1);
    ItemStack input = inventory.get(0);

    self.burnTimeBeforeVanilla = burnTime;
    self.fuelSlotBeforeVanilla = fuelSlot.copy();

    if (burnTime <= 0 && !fuelSlot.isEmpty() && !input.isEmpty()) {
      SimpleInventory inputInv = new SimpleInventory(input);
      boolean hasSmeltable =
          world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, inputInv, world).isPresent() ||
          world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, inputInv, world).isPresent();

      if (hasSmeltable) {
        boolean isSoul = fuelSlot.isOf(IntunethItems.SOUL_CHARGE);
        soulFurnace.setLastFuelSnapshot(fuelSlot.copy());
        soulFurnace.setUsingSoulCharge(isSoul);
        BlockState currentState = world.getBlockState(pos);
        if (currentState.get(SoulChargeFurnace.SOUL_CHARGED) != isSoul) {
          world.setBlockState(pos, currentState.with(SoulChargeFurnace.SOUL_CHARGED, isSoul));
        }
      }
    }
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private static void onTickTail(World world, BlockPos pos, BlockState state,
      AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {

    if (world.isClient) return;

    FurnaceAccessor furnace = (FurnaceAccessor) blockEntity;
    SoulChargeFurnace soulFurnace = (SoulChargeFurnace) blockEntity;
    AbstractFurnaceBlockEntityMixin self = (AbstractFurnaceBlockEntityMixin)(Object) blockEntity;
    DefaultedList<ItemStack> inventory = furnace.getInventory();

    int burnTimeAfter = furnace.getBurnTime();
    BlockState currentState = world.getBlockState(pos);

    if (burnTimeAfter > self.burnTimeBeforeVanilla && self.burnTimeBeforeVanilla > 0) {
      soulFurnace.setLastFuelSnapshot(self.fuelSlotBeforeVanilla.copy());
    }

    // Soul Charge state
    if (burnTimeAfter > 0) {
      boolean isSoul = soulFurnace.getLastFuelSnapshot().isOf(IntunethItems.SOUL_CHARGE);
      soulFurnace.setUsingSoulCharge(isSoul);
      if (currentState.get(SoulChargeFurnace.SOUL_CHARGED) != isSoul) {
        world.setBlockState(pos, currentState.with(SoulChargeFurnace.SOUL_CHARGED, isSoul));
      }
    } else if (self.burnTimeBeforeVanilla <= 0) {
      furnace.setCookTime(0);
      soulFurnace.setUsingSoulCharge(false);
      soulFurnace.setLastFuelSnapshot(ItemStack.EMPTY);
      if (currentState.get(SoulChargeFurnace.SOUL_CHARGED)) {
        world.setBlockState(pos, currentState.with(SoulChargeFurnace.SOUL_CHARGED, false));
      }
    }

    // Last fuel
    // Smelting
    ItemStack input = inventory.get(0);
    ItemStack output = inventory.get(2);

    if (input.isEmpty()) return;

    Optional<SoulBlastingRecipe> soulBlastingRecipe = world.getRecipeManager()
        .getFirstMatch(SoulBlastingRecipeType.SOUL_BLASTING, new SimpleInventory(input), world);

    if (soulBlastingRecipe.isPresent()) {
      SoulBlastingRecipe recipe = soulBlastingRecipe.get();
      ItemStack result = recipe.getOutput(world.getRegistryManager());
      boolean canInsert = output.isEmpty() ||
          (ItemStack.canCombine(output, result) && output.getCount() + result.getCount() <= output.getMaxCount());

      if (canInsert && soulFurnace.isUsingSoulCharge()) {
        int cookTime = furnace.getCookTime();
        int cookTimeTotal = recipe.getCookTime();

        cookTime += 1;

        if (cookTime >= cookTimeTotal) {
          if (output.isEmpty()) {
            inventory.set(2, result.copy());
          } else {
            output.increment(result.getCount());
          }
          input.decrement(1);
          cookTime = 0;
        }

        furnace.setCookTime(cookTime);
        furnace.setCookTimeTotal(cookTimeTotal);
      } else if (!soulFurnace.isUsingSoulCharge()) {
        furnace.setCookTime(0);
      }
      return;
    }

    Optional<? extends AbstractCookingRecipe> smeltingRecipeOpt = world.getRecipeManager()
        .getFirstMatch(RecipeType.SMELTING, new SimpleInventory(input), world);

    smeltingRecipeOpt.ifPresent(recipe -> {
      ItemStack result = recipe.getOutput(world.getRegistryManager());
      boolean canInsert = output.isEmpty() ||
          (ItemStack.canCombine(output, result) && output.getCount() + result.getCount() <= output.getMaxCount());

      if (canInsert) {
        int cookTime = furnace.getCookTime();
        int cookTimeTotal = recipe.getCookTime();

        if (soulFurnace.isUsingSoulCharge()) {
          cookTime += 1; // +1 extra per tick = 2x speed
        }

        if (cookTime >= cookTimeTotal) {
          if (output.isEmpty()) {
            inventory.set(2, result.copy());
          } else {
            output.increment(result.getCount());
          }
          input.decrement(1);
          cookTime = 0;
        }

        furnace.setCookTime(cookTime);
        furnace.setCookTimeTotal(cookTimeTotal);
      }
    });
  }
}