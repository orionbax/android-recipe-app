package com.example.recipeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.recipeapp.data.RecipeDatabase
import com.example.recipeapp.data.RecipeRepository
import com.example.recipeapp.ui.navigation.RecipeNavGraph
import com.example.recipeapp.ui.theme.RecipeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = RecipeRepository(
            RecipeDatabase.getDatabase(applicationContext).recipeDao()
        )

        setContent {
            RecipeAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecipeNavGraph(repository = repository)
                }
            }
        }
    }
}
