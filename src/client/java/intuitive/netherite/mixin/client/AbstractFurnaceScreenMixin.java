package intuitive.netherite.mixin.client;

import intuitive.netherite.SoulChargeFurnace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin extends HandledScreen<AbstractFurnaceScreenHandler> {

    private static final Identifier SOUL_FURNACE_TEXTURE =
        new Identifier("intuneth", "textures/gui/container/soul_furnace.png");
    private static final Identifier SOUL_SMOKER_TEXTURE =
        new Identifier("intuneth", "textures/gui/container/soul_smoker.png");
    private static final Identifier SOUL_SMELTING_TEXTURE =
        new Identifier("intuneth", "textures/gui/container/soul_blast_furnace.png");

    public AbstractFurnaceScreenMixin(AbstractFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Unique
    private Identifier getSoulTexture() {
        String handlerClass = handler.getClass().getSimpleName();
        if (handlerClass.contains("Smoker")) return SOUL_SMOKER_TEXTURE;
        if (handlerClass.contains("Blast")) return SOUL_SMELTING_TEXTURE;
        return SOUL_FURNACE_TEXTURE;
    }

    @Unique
    private boolean isSoulCharged() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null || mc.player == null) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(playerPos, 6, 6, 6)) {
            var blockState = world.getBlockState(pos);
            if (blockState.contains(SoulChargeFurnace.SOUL_CHARGED)
                    && blockState.get(SoulChargeFurnace.SOUL_CHARGED)
                    && world.getBlockEntity(pos) != null) {
                return true;
            }
        }
        return false;
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
        ),
        index = 0
    )
    private Identifier redirectTexture(Identifier original) {
        if (isSoulCharged()) {
            return getSoulTexture();
        }
        return original;
    }
}