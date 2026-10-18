# Material 3 / Material 3 Expressive — reference projects

Reading list for designing SongMeld UI. These are **examples, not runtime dependencies**; check each project's current API versions and license before reusing code or assets.

| Repository | Useful reference |
| --- | --- |
| [AndroidX Material 3 Catalog](https://github.com/androidx/androidx/tree/androidx-main/compose/material3/material3/integration-tests/material3-catalog) | Official component catalog and scroll behaviors |
| [Compose M3 Expressive Catalog](https://github.com/emertozd/Compose-Material-3-Expressive-Catalog) | Independently runnable catalog-style examples |
| [Material 3 Expressive Catalog](https://github.com/meticha/material-3-expressive-catalog) | FAB menu, button groups, toolbars |
| [Material 3 Expressive List](https://github.com/NicosNicolaou16/Material_3_Expressive_List) | Buttons and motion examples |
| [ComposeMaterialDesign](https://github.com/muhammedeminalan/ComposeMaterialDesign) | General Material 3 Compose patterns |
| [Lune](https://github.com/MrDemonc/Lune) | Music UI inspiration |
| [Retro Music Player](https://github.com/RetroMusicPlayer/RetroMusicPlayer) | Animated vectors and bottom navigation behavior |
| [Paisa release repository](https://github.com/h4h13/paisa-app) / [older source fork](https://github.com/KDZsilva/Paisa) | Icon-picking UX patterns (Flutter, not Compose) |

SongMeld's settings app bar currently uses public `MediumTopAppBar` + `exitUntilCollapsedScrollBehavior()` rather than an internal Material 3 Expressive API. Avoid copying animation XML from other projects without checking their licenses.
