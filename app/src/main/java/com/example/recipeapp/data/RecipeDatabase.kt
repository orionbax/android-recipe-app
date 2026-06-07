package com.example.recipeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedSampleRecipes(database.recipeDao())
                }
            }
        }
    }
}

private suspend fun seedSampleRecipes(dao: RecipeDao) {
    if (dao.getRecipeCount() > 0) return

    val samples = listOf(
        Recipe(
            title = "Classic Avocado Toast",
            description = "A quick, creamy breakfast with fresh avocado on toasted bread.",
            ingredients = "2 slices sourdough bread\n1 ripe avocado\n1 tbsp lemon juice\nSalt and pepper\nRed pepper flakes (optional)",
            instructions = "1. Toast the bread until golden.\n2. Mash avocado with lemon juice, salt, and pepper.\n3. Spread on toast and top with red pepper flakes.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 3,
            servings = 1,
            category = "Breakfast"
        ),
        Recipe(
            title = "Garlic Butter Pasta",
            description = "Simple weeknight pasta with aromatic garlic butter.",
            ingredients = "200g spaghetti\n4 cloves garlic, minced\n3 tbsp butter\n2 tbsp olive oil\nParsley, chopped\nParmesan cheese\nSalt",
            instructions = "1. Cook pasta in salted boiling water until al dente.\n2. Sauté garlic in butter and oil until fragrant.\n3. Toss pasta with garlic butter, parsley, and parmesan.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 15,
            servings = 2,
            category = "Dinner"
        ),
        Recipe(
            title = "Greek Salad",
            description = "Crisp, refreshing salad with feta and olives.",
            ingredients = "2 cucumbers\n4 tomatoes\n1 red onion\n200g feta cheese\nKalamata olives\nOlive oil\nOregano\nSalt",
            instructions = "1. Chop vegetables into bite-sized pieces.\n2. Combine in a bowl with olives and crumbled feta.\n3. Drizzle with olive oil, sprinkle oregano and salt.",
            prepTimeMinutes = 15,
            cookTimeMinutes = 0,
            servings = 4,
            category = "Salad"
        ),
        Recipe(
            title = "Banana Smoothie",
            description = "Creamy, naturally sweet smoothie for any time of day.",
            ingredients = "2 bananas\n1 cup milk\n1 tbsp honey\n1/2 tsp vanilla\nIce cubes",
            instructions = "1. Add all ingredients to a blender.\n2. Blend until smooth and creamy.\n3. Serve immediately.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 2,
            category = "Drinks"
        )
    )

    samples.forEach { dao.insertRecipe(it) }
}
