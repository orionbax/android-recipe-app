# Recipe Book

A minimalist Android recipe app for saving, browsing, searching, and organizing your personal recipes. All data is stored locally on the device — no account or internet connection required.

---

## Table of Contents

1. [Overview](#overview)
2. [Screens & Navigation](#screens--navigation)
3. [Features](#features)
4. [How It Works](#how-it-works)
5. [Data Model](#data-model)
6. [Architecture](#architecture)
7. [Project Structure](#project-structure)
8. [Tech Stack](#tech-stack)
9. [Getting Started](#getting-started)
10. [Sample Data](#sample-data)

---

## Overview

**Recipe Book** lets you:

- Browse a list of your saved recipes
- Search recipes by name, ingredient, or category
- Filter by favorites or category
- View full recipe details (ingredients, instructions, timing)
- Add, edit, and delete recipes
- Mark recipes as favorites

The app uses a single-activity architecture with Jetpack Compose for the UI and Room (SQLite) for persistent local storage.

---

## Screens & Navigation

The app has **four screens** connected by a navigation graph. The home screen is always the recipe list.

```
┌─────────────────┐
│  Recipe List    │  ← Start screen (home)
│  (Home)         │
└────────┬────────┘
         │
    ┌────┼────────────────┐
    │    │                │
    ▼    ▼                ▼
┌────────┐  ┌──────────┐  ┌──────────────┐
│  Add   │  │  Detail  │  │    Edit      │
│ Recipe │  │  (view)  │──│   Recipe     │
└────────┘  └──────────┘  └──────────────┘
```

| Route | Screen | How you get there |
|-------|--------|-------------------|
| `list` | Recipe List | App launch (default) |
| `detail/{recipeId}` | Recipe Detail | Tap a recipe card on the list |
| `add` | New Recipe | Tap the **+** floating action button on the list |
| `edit/{recipeId}` | Edit Recipe | Tap the **edit** icon on the detail screen |

**Back navigation:** Every screen except the list has a back arrow in the top bar. After saving or deleting a recipe, the app automatically returns to the previous screen.

---

## Features

### 1. Recipe List (Home Screen)

**File:** `ui/screens/RecipeListScreen.kt`

The main screen where all recipes are displayed.

**What you see:**
- **Top bar** — "Recipes" title
- **Search bar** — Search across recipe titles, ingredients, and categories
- **Filter chips** — Horizontal row of filters:
  - **All** — Show every recipe (default)
  - **Favorites** — Show only favorited recipes
  - **Category chips** — Dynamically generated from your recipes (e.g. Breakfast, Dinner, Salad)
- **Recipe cards** — Scrollable list of recipes, newest first
- **Floating action button (+)** — Opens the add-recipe screen

**What each recipe card shows:**
- Title
- Description (up to 2 lines)
- Total time (prep + cook, if set)
- Servings count
- Category
- Heart icon — Tap to toggle favorite on/off

**Empty states:**
- No recipes at all → "No recipes yet. Tap + to add your first recipe."
- Favorites filter with no favorites → "No favorite recipes yet. Tap the heart on a recipe to save it."
- Search with no matches → "No recipes match your search"

---

### 2. Recipe Detail Screen

**File:** `ui/screens/RecipeDetailScreen.kt`

Full view of a single recipe.

**What you see:**
- **Top bar** with:
  - Back arrow
  - Recipe title
  - **Heart** — Toggle favorite
  - **Edit** — Open edit screen
  - **Delete** — Show delete confirmation dialog
- **Description** — Shown if not empty
- **Info badges** — Prep time, cook time, servings, category
- **Ingredients** — Each line from the ingredients field shown as a bullet point
- **Instructions** — Each line shown as a separate step/paragraph

**Delete flow:**
1. Tap the delete icon
2. A dialog asks: "Delete recipe? This will permanently remove [title]."
3. Tap **Delete** to confirm or **Cancel** to dismiss
4. On delete, the recipe is removed from the database and you return to the list

**Live updates:** The detail screen observes the database in real time. If you toggle favorite, changes appear immediately without leaving the screen.

---

### 3. Add Recipe Screen

**File:** `ui/screens/AddEditRecipeScreen.kt` (with `existingRecipe = null`)

Form for creating a new recipe.

**Fields:**

| Field | Required | Description |
|-------|----------|-------------|
| Title | Yes | Recipe name. Save is blocked if empty. |
| Description | No | Short summary (2–3 lines) |
| Prep (min) | No | Preparation time in minutes |
| Cook (min) | No | Cooking time in minutes |
| Servings | No | Number of servings (minimum 1, default 1) |
| Category | No | One of: Breakfast, Lunch, Dinner, Salad, Dessert, Drinks, Snacks, General |
| Ingredients | No | One ingredient per line |
| Instructions | No | Step-by-step instructions, one step per line |

**Actions:**
- **Back arrow** — Discard and return to list
- **Checkmark (Save)** — Validates title, saves to database, returns to list

---

### 4. Edit Recipe Screen

**File:** `ui/screens/AddEditRecipeScreen.kt` (with existing recipe loaded)

Same form as Add Recipe, but pre-filled with the current recipe data.

**Behavior:**
- Title bar shows "Edit Recipe" instead of "New Recipe"
- All fields are populated from the existing recipe
- Saving updates the record in the database (does not create a duplicate)
- Favorite status and creation date are preserved
- After save, returns to the previous screen (usually detail)

---

## How It Works

### App startup

1. `MainActivity` launches and enables edge-to-edge display.
2. `RecipeDatabase` is initialized (singleton). On first install, sample recipes are seeded.
3. `RecipeRepository` wraps database access.
4. `RecipeNavGraph` sets up navigation and a shared `RecipeViewModel`.
5. `RecipeAppTheme` applies Material 3 styling (light/dark based on system setting).
6. The **Recipe List** screen is shown.

### Search

When you type in the search bar:

1. `RecipeViewModel.setSearchQuery()` updates the query state.
2. The ViewModel runs a Room SQL query matching **title**, **ingredients**, or **category**.
3. Results update reactively via Kotlin Flow.
4. While searching, category and favorites filters are ignored — search takes priority.

### Filtering

The ViewModel combines three pieces of state: search query, filter (All/Favorites), and selected category.

**Priority order:**
1. If search query is not empty → search results only
2. Else if Favorites filter is active → favorite recipes only
3. Else → all recipes, optionally filtered by selected category

**Filter interactions:**
- Selecting **Favorites** clears any category filter
- Selecting a **category** resets filter to **All**
- Tapping an active category chip again deselects it (shows all again)
- Tapping **All** clears both favorites filter and category

### Favorites

- Tapping the heart on a list card or detail screen calls `toggleFavorite()`.
- The database updates `isFavorite` for that recipe ID.
- List and detail screens refresh automatically through Flow observers.

### Saving recipes

**New recipe (`id = 0`):**
- Room generates a new auto-increment ID
- `createdAt` is set to the current timestamp
- Recipe appears at the top of the list (sorted by newest first)

**Edit recipe (`id > 0`):**
- Room updates the existing row
- `createdAt` and `isFavorite` are kept from the original

### Ingredients & instructions format

Both fields are stored as plain text with newline-separated lines:

```
2 cups flour
1 tsp salt
3 eggs
```

The detail screen splits on newlines and displays each non-empty line separately (ingredients as bullets, instructions as paragraphs).

---

## Data Model

**File:** `data/Recipe.kt`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key, auto-generated |
| `title` | String | Recipe name |
| `description` | String | Optional summary |
| `ingredients` | String | Newline-separated ingredient list |
| `instructions` | String | Newline-separated steps |
| `prepTimeMinutes` | Int | Prep time (0 = not set) |
| `cookTimeMinutes` | Int | Cook time (0 = not set) |
| `servings` | Int | Number of servings (default 1) |
| `category` | String | Category label (default "General") |
| `isFavorite` | Boolean | Favorite flag (default false) |
| `createdAt` | Long | Creation timestamp (milliseconds) |

**Computed property:** `totalTimeMinutes` = `prepTimeMinutes + cookTimeMinutes`

### Database

- **Name:** `recipe_database`
- **Table:** `recipes`
- **Version:** 1
- **ORM:** Room with KSP code generation

**Key queries (`RecipeDao`):**
- `getAllRecipes()` — All recipes, newest first
- `getFavoriteRecipes()` — Favorites only
- `searchRecipes(query)` — LIKE search on title, ingredients, category
- `getCategories()` — Distinct category names
- `observeRecipeById(id)` — Live single-recipe observer
- `insertRecipe`, `updateRecipe`, `deleteRecipe`, `setFavorite`

---

## Architecture

The app follows a simple layered architecture:

```
┌──────────────────────────────────────┐
│              UI Layer                │
│  Screens · Components · Theme        │
│  Navigation · ViewModel              │
└─────────────────┬────────────────────┘
                  │
┌─────────────────▼────────────────────┐
│           Repository Layer           │
│         RecipeRepository             │
└─────────────────┬────────────────────┘
                  │
┌─────────────────▼────────────────────┐
│            Data Layer                │
│   RecipeDao · RecipeDatabase · Room  │
└──────────────────────────────────────┘
```

### UI Layer

| Component | Role |
|-----------|------|
| `MainActivity` | Entry point, wires database → repository → UI |
| `RecipeNavGraph` | Navigation host, shared ViewModel |
| `RecipeListScreen` | Home, search, filters, list |
| `RecipeDetailScreen` | Full recipe view, delete |
| `AddEditRecipeScreen` | Create/update form |
| `RecipeCard` | Reusable list item component |
| `RecipeViewModel` | UI state, filtering logic, CRUD actions |
| `RecipeAppTheme` | Material 3 colors, typography, light/dark |

### ViewModel state

`RecipeViewModel` holds:

- `searchQuery` — Current search text
- `filter` — `ALL` or `FAVORITES`
- `selectedCategory` — Active category filter (or null)
- `categories` — Dynamic list from database
- `recipes` — Filtered/search results (derived from above)

All list state is exposed as `StateFlow` and collected in Compose with `collectAsState()`.

### Data flow example: toggling a favorite

```
User taps heart on RecipeCard
        ↓
RecipeViewModel.toggleFavorite(recipe)
        ↓
RecipeRepository.toggleFavorite(id, !isFavorite)
        ↓
RecipeDao.setFavorite(id, isFavorite)  →  SQLite UPDATE
        ↓
Flow emits updated list / recipe
        ↓
Compose recomposes with new heart state
```

---

## Project Structure

```
app/src/main/java/com/example/recipeapp/
├── MainActivity.kt                 # App entry point
├── data/
│   ├── Recipe.kt                   # Entity / data class
│   ├── RecipeDao.kt                # Database queries
│   ├── RecipeDatabase.kt           # Room database + sample seed
│   └── RecipeRepository.kt         # Data access abstraction
└── ui/
    ├── components/
    │   └── RecipeCard.kt           # List item card
    ├── navigation/
    │   └── RecipeNavGraph.kt       # Routes and screen wiring
    ├── screens/
    │   ├── RecipeListScreen.kt     # Home screen
    │   ├── RecipeDetailScreen.kt   # Detail view
    │   └── AddEditRecipeScreen.kt  # Create / edit form
    ├── theme/
    │   ├── Color.kt                # App color palette
    │   ├── Theme.kt                # Material 3 theme
    │   └── Type.kt                 # Typography
    └── viewmodel/
        └── RecipeViewModel.kt      # Business logic & UI state
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Programming language |
| **Jetpack Compose** | Declarative UI |
| **Material 3** | Design system (components, theme) |
| **Navigation Compose** | Screen routing |
| **Room** | Local SQLite database |
| **KSP** | Room annotation processing |
| **ViewModel + StateFlow** | State management |
| **Coroutines + Flow** | Async operations and reactive data |

**Requirements:**
- Min SDK: 24 (Android 7.0)
- Target SDK: 36
- Java 11 compatibility

---

## Getting Started

### Prerequisites

- Android Studio (latest recommended)
- Android SDK
- JDK 11+

### Run the app

1. Open the project in Android Studio.
2. Connect a device or start an emulator.
3. Click **Run** (or press Shift+F10).

### Build from terminal

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

Install on a connected device:

```bash
./gradlew installDebug
```

---

## Sample Data

On **first launch** (when the database is created), four sample recipes are automatically inserted:

| Recipe | Category | Prep | Cook | Servings |
|--------|----------|------|------|----------|
| Classic Avocado Toast | Breakfast | 5 min | 3 min | 1 |
| Garlic Butter Pasta | Dinner | 5 min | 15 min | 2 |
| Greek Salad | Salad | 15 min | — | 4 |
| Banana Smoothie | Drinks | 5 min | — | 2 |

Sample data is only added once. If you delete all recipes, they are not re-inserted unless you clear app data or reinstall.

---

## Design

The app uses a warm, minimal palette:

| Color | Usage |
|-------|-------|
| Sage Green | Primary actions, accents |
| Cream / Warm White | Backgrounds and surfaces |
| Charcoal | Primary text |
| Accent Orange | Favorites, secondary highlights |

Light and dark themes are supported automatically based on the device system setting.

---

## Limitations & Notes

- **No images** — Recipes are text-only.
- **No cloud sync** — Data stays on the device.
- **No sharing/export** — Recipes cannot be shared outside the app yet.
- **Category list is fixed in the form** — Add/Edit screen offers 8 predefined categories; filter chips on the home screen are built dynamically from saved recipes.
- **Single ViewModel** — One shared ViewModel across all screens, scoped to the navigation graph.
