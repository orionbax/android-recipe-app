package com.example.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RecipeFilter {
    ALL, FAVORITES
}

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(RecipeFilter.ALL)
    val filter: StateFlow<RecipeFilter> = _filter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = combine(
        _searchQuery,
        _filter,
        _selectedCategory
    ) { query, filter, category ->
        Triple(query.trim(), filter, category)
    }.flatMapLatest { (query, filter, category) ->
        val baseFlow = when {
            query.isNotEmpty() -> repository.searchRecipes(query)
            filter == RecipeFilter.FAVORITES -> repository.getFavoriteRecipes()
            else -> repository.getAllRecipes()
        }
        baseFlow.map { list ->
            if (category != null && query.isEmpty() && filter != RecipeFilter.FAVORITES) {
                list.filter { it.category == category }
            } else {
                list
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: RecipeFilter) {
        _filter.value = filter
        if (filter == RecipeFilter.FAVORITES) {
            _selectedCategory.value = null
        }
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
        if (category != null) {
            _filter.value = RecipeFilter.ALL
        }
    }

    fun observeRecipe(id: Long): Flow<Recipe?> = repository.observeRecipeById(id)

    suspend fun getRecipe(id: Long): Recipe? = repository.getRecipeById(id)

    fun saveRecipe(recipe: Recipe, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (recipe.id == 0L) {
                repository.insertRecipe(recipe)
            } else {
                repository.updateRecipe(recipe)
                recipe.id
            }
            onSaved(id)
        }
    }

    fun deleteRecipe(recipe: Recipe, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
            onDeleted()
        }
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe.id, !recipe.isFavorite)
        }
    }
}

class RecipeViewModelFactory(
    private val repository: RecipeRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
