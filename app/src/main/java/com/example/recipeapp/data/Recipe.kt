package com.example.recipeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val ingredients: String,
    val instructions: String,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servings: Int = 1,
    val category: String = "General",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalTimeMinutes: Int
        get() = prepTimeMinutes + cookTimeMinutes
}
