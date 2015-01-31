package net.machinemuse.numina.nei;

import codechicken.nei.ItemList;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.recipe.ShapedRecipeHandler;
import net.machinemuse.numina.recipe.ItemNameMappings;
import net.machinemuse.numina.recipe.JSONRecipe;
import net.machinemuse.numina.recipe.SimpleItemMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONRecipeHandler extends ShapedRecipeHandler {

    @Override
    public String getRecipeName()
    {
        return "JSON Shaped";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        // System.out.println("In loadCraftingRecipes(String, Object...)");
        if(outputId.equals("crafting") && getClass() == JSONRecipeHandler.class) {
            List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
            for(IRecipe irecipe : allrecipes) {
                if(irecipe instanceof JSONRecipe) {
                    CachedShapedRecipe recipe = JSONShapedRecipe((JSONRecipe) irecipe);

                    recipe.computeVisuals();
                    arecipes.add(recipe);
                }
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
        // System.out.println("In loadCraftingRecipes(ItemStack)");
        List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
        for(IRecipe irecipe : allrecipes) {
            if(NEIServerUtils.areStacksSameTypeCrafting(irecipe.getRecipeOutput(), result)) {
                if(irecipe instanceof JSONRecipe) {
                    CachedShapedRecipe recipe = JSONShapedRecipe((JSONRecipe) irecipe);

                    recipe.computeVisuals();
                    arecipes.add(recipe);
                }
            }
        }
    }
    
    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients)
    {
        // System.out.println("In loadUsageRecipes(String, Object...)");
        if(inputId.equals("crafting") && ingredients.length == 1 && getClass() == JSONRecipeHandler.class) {
            List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
            for(IRecipe irecipe : allrecipes) {
                if(irecipe instanceof JSONRecipe) {
                    CachedShapedRecipe recipe = JSONShapedRecipe((JSONRecipe) irecipe);
    
                    if (recipe.contains(recipe.ingredients, (ItemStack)ingredients[0])) {
                        recipe.computeVisuals();
                        recipe.setIngredientPermutation(recipe.ingredients, (ItemStack)ingredients[0]);
                        arecipes.add(recipe);
                    }
                }
            }
        } else {
            super.loadUsageRecipes(inputId, ingredients);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
        // System.out.println("In loadUsageRecipes(ItemStack)");
        List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
        for(IRecipe irecipe : allrecipes) {
            if(irecipe instanceof JSONRecipe) {
                CachedShapedRecipe recipe = JSONShapedRecipe((JSONRecipe) irecipe);

                if(recipe.contains(recipe.ingredients, ingredient)) {
                    recipe.computeVisuals();
                    recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                    arecipes.add(recipe);
                }
            }
        }
    }

    private static Map<String, ArrayList<ItemStack>> itemMap;

    public static ArrayList<ItemStack> getItemByUnlocalizedName(String unlocalizedName)
    {
        // System.out.println("In getItemByUnlocalizedName");
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        if (itemMap == null)
        {
            if (ItemList.items.isEmpty()) 
            {
                ItemStack stack = ItemNameMappings.getItem(unlocalizedName);
                if (stack != null)
                    result.add(stack);
            } else {
                itemMap = new HashMap<String, ArrayList<ItemStack>>();
                for (ItemStack stack : ItemList.items)
                {
                    String key = stack.getItem().getUnlocalizedName(stack);
                    if (!itemMap.containsKey(key))
                        itemMap.put(key, new ArrayList<ItemStack>());
                        
                    itemMap.get(key).add(stack);
                }
            }
        } else {
            if (itemMap.containsKey(unlocalizedName))
                result.addAll(itemMap.get(unlocalizedName));
        }
        return result;
    }

    public static List<ItemStack> getIngredient(SimpleItemMatcher cell)
    {

        List<ItemStack> result = null;
        if (cell != null) {

            if (cell.oredictName != null) {
                result = OreDictionary.getOres(cell.oredictName);
    
            } else if (cell.unlocalizedName != null) {
                if (result == null) {
                    result = getItemByUnlocalizedName(cell.unlocalizedName);
                } else {
                    ArrayList<ItemStack> t = new ArrayList<ItemStack>();
                    for (ItemStack stack : result) {
                        if (cell.unlocalizedName.equals(stack.getItem().getUnlocalizedName(stack)))
                            t.add(stack);
                    }
                            
                    result = t;
                }
            }
    
            if (cell.item != null) {
                int meta = OreDictionary.WILDCARD_VALUE;
                
                if (cell.meta != null)
                    meta = cell.meta.intValue();
                    
                if (result == null) {
                    result = new ArrayList<ItemStack>();
                    result.add(new ItemStack(cell.item, 1, meta));
                } else {
                    ArrayList<ItemStack> t = new ArrayList<ItemStack>();
                    for (ItemStack stack : result) {
                        if (stack.getItem() == cell.item && (meta == OreDictionary.WILDCARD_VALUE || meta == stack.getItemDamage()))
                            t.add(stack);
                    }
                            
                    result = t;
                }
            } else if (cell.meta != null && result != null && cell.meta.intValue() != OreDictionary.WILDCARD_VALUE) {
                ArrayList<ItemStack> t = new ArrayList<ItemStack>();
                for (ItemStack stack : result) {
                    if (cell.meta.intValue() == stack.getItemDamage())
                        t.add(stack);
                }
                        
                result = t;
            }
            // if (cell.nbt != null && result != null) {
            //     ArrayList<ItemStack> t = new ArrayList<ItemStack>();
            //     for (ItemStack stack : result) {
            //         ItemStack stack2 = stack.copy();
            //         stack2.setTagCompound(cell.nbt);
            //         t.add(stack2);
            //     }
            //     result = t;
            // }
        } else {
            result = null;
        }

        return result;
    }

    public CachedShapedRecipe JSONShapedRecipe(JSONRecipe recipe)
    {
        // System.out.println("In JSONShapedRecipe");
        int height = recipe.ingredients.length;
        int width = recipe.getWidth();
        if (height == 0 || width == 0)
            return null;

        Object[] items = new Object[height*width];

        for (int y=0; y < height; y++) {
            if (recipe.ingredients[y] != null) {
                for (int x=0; x < width; x++) {
                    items[y * width + x] = getIngredient(recipe.ingredients[y][x]);
                }
            }
        }

        return new CachedShapedRecipe(width, height, items, recipe.getRecipeOutput());
    }
}
