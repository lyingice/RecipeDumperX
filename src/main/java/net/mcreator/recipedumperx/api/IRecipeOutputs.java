package net.mcreator.recipedumperx.api;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

public interface IRecipeOutputs {
    void addOutput(int slot, ItemStack stack);
    JsonObject serialize() throws RecipeDumpException;
}