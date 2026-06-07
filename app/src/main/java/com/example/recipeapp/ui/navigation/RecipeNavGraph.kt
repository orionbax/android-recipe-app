package com.example.recipeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeRepository
import com.example.recipeapp.ui.screens.AddEditRecipeScreen
import com.example.recipeapp.ui.screens.RecipeDetailScreen
import com.example.recipeapp.ui.screens.RecipeListScreen
import com.example.recipeapp.ui.viewmodel.RecipeViewModel
import com.example.recipeapp.ui.viewmodel.RecipeViewModelFactory

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{recipeId}"
    const val ADD = "add"
    const val EDIT = "edit/{recipeId}"

    fun detail(recipeId: Long) = "detail/$recipeId"
    fun edit(recipeId: Long) = "edit/$recipeId"
}

@Composable
fun RecipeNavGraph(repository: RecipeRepository) {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(Routes.LIST) {
            RecipeListScreen(
                viewModel = viewModel,
                onRecipeClick = { id ->
                    navController.navigate(Routes.detail(id))
                },
                onAddClick = {
                    navController.navigate(Routes.ADD)
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            RecipeDetailRoute(
                recipeId = recipeId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(recipeId)) },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADD) {
            AddEditRecipeScreen(
                onBack = { navController.popBackStack() },
                onSave = { recipe ->
                    viewModel.saveRecipe(recipe) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            EditRecipeRoute(
                recipeId = recipeId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun RecipeDetailRoute(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit
) {
    val recipe by viewModel.observeRecipe(recipeId).collectAsState(initial = null)

    recipe?.let { current ->
        RecipeDetailScreen(
            recipe = current,
            onBack = onBack,
            onEdit = onEdit,
            onDelete = { viewModel.deleteRecipe(current, onDeleted) },
            onToggleFavorite = { viewModel.toggleFavorite(current) }
        )
    }
}

@Composable
private fun EditRecipeRoute(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }

    LaunchedEffect(recipeId) {
        recipe = viewModel.getRecipe(recipeId)
    }

    recipe?.let { current ->
        AddEditRecipeScreen(
            existingRecipe = current,
            onBack = onBack,
            onSave = { updated ->
                viewModel.saveRecipe(updated) { onSaved() }
            }
        )
    }
}
