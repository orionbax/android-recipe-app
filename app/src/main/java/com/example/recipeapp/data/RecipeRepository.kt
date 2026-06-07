package com.example.recipeapp.data

import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()

    fun getFavoriteRecipes(): Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()

    fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)

    fun getCategories(): Flow<List<String>> = recipeDao.getCategories()

    suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)

    fun observeRecipeById(id: Long): Flow<Recipe?> = recipeDao.observeRecipeById(id)

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        recipeDao.setFavorite(id, isFavorite)
}
