# TODO

## Favorites
- Add `FavoritesManager` singleton (same pattern as `TrashManager` — `mutableStateListOf`)
- New swipe direction: swipe **up** → add to favorites
- Add star overlay icon on upward drag
- Add `FavoritesScreen` with `LazyVerticalGrid` (same layout as `TrashScreen`)
- Add favorites icon + badge in `SwipeScreen` TopAppBar (alongside trash)
- New route `"favorites"` in navigation

## Undo Last Action
- Add `ActionHistory` — stack of `Action(type: TRASH | SKIP | FAVORITE, item: MediaItem)`
- Record each swipe in the history
- On undo: pop from history, decrement `currentIndex`, remove from `TrashManager`/`FavoritesManager` if needed
- a floating "undo" button (arrow-back icon) visible when history is non-empty
