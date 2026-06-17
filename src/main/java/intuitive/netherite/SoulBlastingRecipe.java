package intuitive.netherite;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SoulBlastingRecipe extends AbstractCookingRecipe {

  public SoulBlastingRecipe(Identifier id, String group, CookingRecipeCategory category, Ingredient input,
      ItemStack output, float experience, int cookTime) {
    super(RecipeType.BLASTING, id, group, category, input, output, experience, cookTime);
  }

  @Override
  public ItemStack createIcon() {
    return new ItemStack(Blocks.BLAST_FURNACE);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return SoulBlastingRecipeSerializer.SOUL_BLASTING;
  }

  @Override
  public boolean matches(Inventory inventory, World world) {
    if (inventory instanceof SimpleInventory) {
      return this.getIngredients().get(0).test(inventory.getStack(0));
    }

    if (inventory instanceof AbstractFurnaceBlockEntity furnace) {
      if (furnace instanceof SoulChargeFurnace soulFurnace) {
        return soulFurnace.isUsingSoulCharge() && super.matches(inventory, world);
      }
    }

    return false;
  }
}