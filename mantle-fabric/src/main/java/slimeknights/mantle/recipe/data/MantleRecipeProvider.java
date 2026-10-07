package slimeknights.mantle.recipe.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.platform.condition.ICondition;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Recipe provider supporting Mantle load conditions on recipes, saved under the {@code neoforge:conditions} key for compatibility with existing data */
public abstract class MantleRecipeProvider extends RecipeProvider {
  /** JSON key conditions are stored under */
  public static final String CONDITIONS_KEY = "neoforge:conditions";

  private final PackOutput.PathProvider recipes;
  private final PackOutput.PathProvider advancements;

  protected MantleRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
    this.recipes = output.createRegistryElementsPathProvider(Registries.RECIPE);
    this.advancements = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
  }

  @Override
  public CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider registries) {
    Set<ResourceLocation> seen = Sets.newHashSet();
    List<CompletableFuture<?>> futures = new ArrayList<>();
    RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
    buildRecipes(new ConditionalRecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (!seen.add(id)) {
          throw new IllegalStateException("Duplicate recipe " + id);
        }
        JsonElement json = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow(IllegalStateException::new);
        if (conditions.length > 0 && json instanceof JsonObject object) {
          JsonArray array = new JsonArray();
          for (ICondition condition : conditions) {
            array.add(JsonHelper.serialize(ICondition.CODEC, condition));
          }
          object.add(CONDITIONS_KEY, array);
        }
        futures.add(DataProvider.saveStable(output, json, recipes.json(id)));
        if (advancement != null) {
          futures.add(DataProvider.saveStable(output, registries, Advancement.CODEC, advancement.value(), advancements.json(advancement.id())));
        }
      }

      @Override
      public Advancement.Builder advancement() {
        return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
      }
    });
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }
}
