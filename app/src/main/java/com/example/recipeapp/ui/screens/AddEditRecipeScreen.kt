package com.example.recipeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.recipeapp.data.Recipe

private val categories = listOf(
    "Breakfast", "Lunch", "Dinner", "Salad", "Dessert", "Drinks", "Snacks", "General"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    existingRecipe: Recipe? = null,
    onBack: () -> Unit,
    onSave: (Recipe) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(existingRecipe?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(existingRecipe?.description ?: "") }
    var ingredients by rememberSaveable { mutableStateOf(existingRecipe?.ingredients ?: "") }
    var instructions by rememberSaveable { mutableStateOf(existingRecipe?.instructions ?: "") }
    var prepTime by rememberSaveable { mutableIntStateOf(existingRecipe?.prepTimeMinutes ?: 0) }
    var cookTime by rememberSaveable { mutableIntStateOf(existingRecipe?.cookTimeMinutes ?: 0) }
    var servings by rememberSaveable { mutableIntStateOf(existingRecipe?.servings ?: 1) }
    var category by rememberSaveable { mutableStateOf(existingRecipe?.category ?: "General") }

    var titleError by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (existingRecipe == null) "New Recipe" else "Edit Recipe")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isBlank()) {
                            titleError = true
                            return@IconButton
                        }
                        onSave(
                            Recipe(
                                id = existingRecipe?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                ingredients = ingredients.trim(),
                                instructions = instructions.trim(),
                                prepTimeMinutes = prepTime,
                                cookTimeMinutes = cookTime,
                                servings = servings.coerceAtLeast(1),
                                category = category,
                                isFavorite = existingRecipe?.isFavorite ?: false,
                                createdAt = existingRecipe?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Title *") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Title is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberField(
                    value = prepTime,
                    onValueChange = { prepTime = it },
                    label = "Prep (min)",
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    value = cookTime,
                    onValueChange = { cookTime = it },
                    label = "Cook (min)",
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    value = servings,
                    onValueChange = { servings = it.coerceAtLeast(1) },
                    label = "Servings",
                    modifier = Modifier.weight(1f)
                )
            }

            CategoryPicker(
                selected = category,
                onSelect = { category = it }
            )

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients") },
                placeholder = { Text("One ingredient per line") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                placeholder = { Text("Step by step instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 12
            )
        }
    }
}

@Composable
private fun NumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { text ->
            onValueChange(text.toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(4).forEach { cat ->
                androidx.compose.material3.FilterChip(
                    selected = selected == cat,
                    onClick = { onSelect(cat) },
                    label = { Text(cat) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.drop(4).forEach { cat ->
                androidx.compose.material3.FilterChip(
                    selected = selected == cat,
                    onClick = { onSelect(cat) },
                    label = { Text(cat) }
                )
            }
        }
    }
}
