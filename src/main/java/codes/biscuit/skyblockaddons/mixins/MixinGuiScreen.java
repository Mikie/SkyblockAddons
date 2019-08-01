package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.BackpackInfo;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (stack.getItem().equals(Items.skull) && !SkyblockAddons.INSTANCE.getConfigValues().getDisabledFeatures().contains(Feature.SHOW_BACKPACK_PREVIEW)) {
            NBTTagCompound extraAttributes = stack.getTagCompound().getCompoundTag("ExtraAttributes");
            String id = extraAttributes.getString("id");
            if (!id.equals("")) {
                byte[] bytes = null;
                Feature.Backpack backpack = null;
                switch (id) {
                    case "SMALL_BACKPACK":
                        bytes = extraAttributes.getByteArray("small_backpack_data");
                        backpack = Feature.Backpack.SMALL;
                        break;
                    case "MEDIUM_BACKPACK":
                        bytes = extraAttributes.getByteArray("medium_backpack_data");
                        backpack = Feature.Backpack.MEDIUM;
                        break;
                    case "LARGE_BACKPACK":
                        bytes = extraAttributes.getByteArray("large_backpack_data");
                        backpack = Feature.Backpack.LARGE;
                        break;
                }
                if (bytes == null) return;
                NBTTagCompound nbtTagCompound;
                try {
                    nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                    NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);
                    int length = list.tagCount();
                    ItemStack[] items = new ItemStack[length];
                    for (int i = 0; i < length; i++) {
                        ItemStack itemStack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
                        items[i] = itemStack;
                    }
                    SkyblockAddons.INSTANCE.getUtils().setBackpackToRender(new BackpackInfo(x, y, items, backpack));
                    ci.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}