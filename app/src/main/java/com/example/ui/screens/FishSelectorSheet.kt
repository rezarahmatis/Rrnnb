package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppSettings
import com.example.model.AquariumConfiguration
import com.example.model.AquariumValidator
import com.example.model.CompatibilityStatus
import com.example.model.FishCatalog
import com.example.model.FishCategory
import com.example.model.FishSpecies
import com.example.model.SpeciesEntry
import com.example.model.WorldCatalog
import com.example.model.WorldEnvironment
import com.example.ui.components.FishSpeciesThumbnail
import com.example.ui.theme.AbyssBlack
import com.example.ui.theme.BioluminescentCyan
import com.example.ui.theme.DeepTrenchSurface
import com.example.ui.theme.MistSilver
import com.example.ui.theme.PelagicCard
import com.example.ui.theme.PelagicCardElevated
import com.example.ui.theme.SeafoamMint
import com.example.ui.theme.SoftErrorRose
import com.example.ui.theme.SubtleGlassStroke
import com.example.ui.theme.WarmCoralGold

@Composable
fun FishSelectorSheet(
    currentWorld: WorldEnvironment,
    activeRoster: List<SpeciesEntry>,
    activeAquariumName: String?,
    savedAquariums: List<AquariumConfiguration>,
    settings: AppSettings,
    onAddSpecies: (String) -> Unit,
    onUpdateCount: (String, Int) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onRemoveSpecies: (String) -> Unit,
    onResetDefault: () -> Unit,
    onSaveAquarium: (String) -> Unit,
    onLoadAquarium: (AquariumConfiguration) -> Unit,
    onOverwriteAquarium: (AquariumConfiguration) -> Unit,
    onRenameAquarium: (Long, String) -> Unit,
    onDeleteAquarium: (Long) -> Unit,
    onToggleOverrideCompatibility: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<FishCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyCompatible by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var renamingConfig by remember { mutableStateOf<AquariumConfiguration?>(null) }

    val populationCap = AquariumValidator.maxPopulationFor(settings.performanceMode)
    val totalActiveFish = activeRoster.filter { it.enabled }.sumOf { it.count }
    val activeMap = remember(activeRoster) { activeRoster.associateBy { it.speciesId } }

    val filteredSpecies = remember(selectedCategory, searchQuery, showOnlyCompatible, currentWorld, settings.overrideCompatibility) {
        FishCatalog.species.filter { sp ->
            val catMatch = selectedCategory == null || sp.category == selectedCategory
            val compatMatch = !showOnlyCompatible || sp.isNaturallyCompatibleWith(currentWorld)
            val qMatch = searchQuery.isBlank() ||
                sp.name.contains(searchQuery, ignoreCase = true) ||
                sp.scientificName.contains(searchQuery, ignoreCase = true) ||
                sp.description.contains(searchQuery, ignoreCase = true)
            catMatch && compatMatch && qMatch
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.90f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = DeepTrenchSurface.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aquarium Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeAquariumName?.let { "$it • ${currentWorld.name}" } ?: currentWorld.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = BioluminescentCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledTonalButton(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.testTag("save_aquarium_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = BioluminescentCyan.copy(alpha = 0.18f),
                        contentColor = BioluminescentCyan
                    )
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_fish_selector_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close aquarium studio")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Population capacity bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Population (${settings.performanceMode.label})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MistSilver
                )
                Text(
                    text = "$totalActiveFish / $populationCap creatures • ${activeRoster.size} species",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (totalActiveFish >= populationCap) WarmCoralGold else BioluminescentCyan
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (totalActiveFish.toFloat() / populationCap.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = if (totalActiveFish >= populationCap) WarmCoralGold else BioluminescentCyan,
                trackColor = PelagicCardElevated
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Saved Aquariums carousel
                if (savedAquariums.isNotEmpty()) {
                    item(key = "saved_aquariums_section") {
                        Column {
                            Text(
                                text = "Saved Aquariums",
                                style = MaterialTheme.typography.titleSmall,
                                color = MistSilver
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(savedAquariums, key = { it.id }) { config ->
                                    SavedAquariumChipCard(
                                        config = config,
                                        onLoad = { onLoadAquarium(config) },
                                        onOverwrite = { onOverwriteAquarium(config) },
                                        onRename = { renamingConfig = config },
                                        onDelete = { onDeleteAquarium(config.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Active Ecosystem Roster
                item(key = "active_roster_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Species in ${currentWorld.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = onResetDefault,
                            modifier = Modifier.testTag("reset_roster_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Natural Default")
                        }
                    }
                }

                if (activeRoster.isEmpty()) {
                    item(key = "empty_roster") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PelagicCard,
                            border = BorderStroke(1.dp, SubtleGlassStroke)
                        ) {
                            Text(
                                text = "This habitat is currently tranquil and empty. Add any species below to populate your aquarium.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MistSilver,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(activeRoster, key = { "active_${it.speciesId}" }) { entry ->
                        val species = FishCatalog.getById(entry.speciesId)
                        if (species != null) {
                            ActiveSpeciesRow(
                                species = species,
                                entry = entry,
                                onIncrement = { onUpdateCount(entry.speciesId, +1) },
                                onDecrement = { onUpdateCount(entry.speciesId, -1) },
                                onToggleEnabled = { onToggleEnabled(entry.speciesId) },
                                onRemove = { onRemoveSpecies(entry.speciesId) }
                            )
                        }
                    }
                }

                // 3. Species Library Header + Controls
                item(key = "library_controls") {
                    Column {
                        HorizontalDivider(color = SubtleGlassStroke, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Species Library (${FishCatalog.species.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Combine multiple species freely in one world",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MistSilver
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fish_search_input"),
                            placeholder = { Text("Search koi, tetra, tang, manta, jellyfish…", color = MistSilver) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MistSilver) },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MistSilver)
                                    }
                                }
                            } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BioluminescentCyan,
                                unfocusedBorderColor = SubtleGlassStroke,
                                focusedContainerColor = PelagicCard.copy(alpha = 0.7f),
                                unfocusedContainerColor = PelagicCard.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("All") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BioluminescentCyan.copy(alpha = 0.22f),
                                        selectedLabelColor = BioluminescentCyan
                                    )
                                )
                            }
                            items(FishCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                    label = { Text(cat.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BioluminescentCyan.copy(alpha = 0.22f),
                                        selectedLabelColor = BioluminescentCyan
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Compatibility filters & override toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = showOnlyCompatible,
                                onClick = { showOnlyCompatible = !showOnlyCompatible },
                                label = { Text("Native to ${currentWorld.habitat.label}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SeafoamMint.copy(alpha = 0.20f),
                                    selectedLabelColor = SeafoamMint
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mix Any",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MistSilver
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = settings.overrideCompatibility,
                                    onCheckedChange = onToggleOverrideCompatibility,
                                    modifier = Modifier.testTag("override_compatibility_switch")
                                )
                            }
                        }
                    }
                }

                // 4. All Species Rows
                items(filteredSpecies, key = { "lib_${it.id}" }) { species ->
                    val existing = activeMap[species.id]
                    val compat = AquariumValidator.checkCompatibility(
                        species = species,
                        world = currentWorld,
                        overrideEnabled = settings.overrideCompatibility
                    )
                    SpeciesCatalogCard(
                        species = species,
                        activeCount = existing?.count ?: 0,
                        compatibility = compat,
                        onAdd = { onAddSpecies(species.id) }
                    )
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveOrRenameAquariumDialog(
            title = "Save Custom Aquarium",
            initialName = activeAquariumName ?: "My ${currentWorld.name}",
            confirmLabel = "Save",
            onConfirm = { name ->
                onSaveAquarium(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    renamingConfig?.let { target ->
        SaveOrRenameAquariumDialog(
            title = "Rename Aquarium",
            initialName = target.name,
            confirmLabel = "Rename",
            onConfirm = { name ->
                onRenameAquarium(target.id, name)
                renamingConfig = null
            },
            onDismiss = { renamingConfig = null }
        )
    }
}

@Composable
private fun SavedAquariumChipCard(
    config: AquariumConfiguration,
    onLoad: () -> Unit,
    onOverwrite: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val world = remember(config.worldId) { WorldCatalog.getById(config.worldId) }

    Surface(
        modifier = Modifier
            .width(204.dp)
            .clickable(onClick = onLoad)
            .testTag("saved_aquarium_${config.id}"),
        shape = RoundedCornerShape(16.dp),
        color = PelagicCardElevated,
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = world.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = BioluminescentCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${config.species.size} species • ${config.totalActiveFish} fish",
                    style = MaterialTheme.typography.labelSmall,
                    color = MistSilver
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Aquarium options", tint = MistSilver)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Load Aquarium") },
                        onClick = { menuExpanded = false; onLoad() }
                    )
                    DropdownMenuItem(
                        text = { Text("Update with Current") },
                        onClick = { menuExpanded = false; onOverwrite() }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = SoftErrorRose) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = SoftErrorRose) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSpeciesRow(
    species: FishSpecies,
    entry: SpeciesEntry,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleEnabled: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (entry.enabled) PelagicCard else PelagicCard.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, if (entry.enabled) BioluminescentCyan.copy(alpha = 0.28f) else SubtleGlassStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FishSpeciesThumbnail(
                species = species,
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = species.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (entry.enabled) MaterialTheme.colorScheme.onSurface else MistSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = species.category.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MistSilver
                )
            }

            IconButton(
                onClick = onToggleEnabled,
                modifier = Modifier.testTag("toggle_species_${species.id}")
            ) {
                Icon(
                    imageVector = if (entry.enabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (entry.enabled) "Hide species" else "Show species",
                    tint = if (entry.enabled) BioluminescentCyan else MistSilver
                )
            }

            IconButton(
                onClick = onDecrement,
                modifier = Modifier.testTag("dec_species_${species.id}")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease ${species.name}")
            }

            Text(
                text = "${entry.count}",
                style = MaterialTheme.typography.labelLarge,
                color = BioluminescentCyan,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier.testTag("inc_species_${species.id}")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase ${species.name}")
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag("remove_species_${species.id}")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove ${species.name}", tint = MistSilver)
            }
        }
    }
}

@Composable
private fun SpeciesCatalogCard(
    species: FishSpecies,
    activeCount: Int,
    compatibility: CompatibilityStatus,
    onAdd: () -> Unit
) {
    val badgeColor = when (compatibility) {
        CompatibilityStatus.NATURAL -> SeafoamMint
        CompatibilityStatus.OVERRIDE_ALLOWED -> WarmCoralGold
        CompatibilityStatus.INCOMPATIBLE -> SoftErrorRose
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PelagicCard.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FishSpeciesThumbnail(
                species = species,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = species.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (activeCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BioluminescentCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "×$activeCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = BioluminescentCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = species.scientificName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MistSilver.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = species.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MistSilver,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "${compatibility.label} • ${species.compatibleHabitats.first().label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onAdd,
                modifier = Modifier.testTag("add_species_${species.id}"),
                border = BorderStroke(
                    1.dp,
                    if (compatibility == CompatibilityStatus.INCOMPATIBLE) SoftErrorRose.copy(alpha = 0.5f) else BioluminescentCyan
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add ${species.name}",
                    tint = if (compatibility == CompatibilityStatus.INCOMPATIBLE) SoftErrorRose else BioluminescentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add",
                    color = if (compatibility == CompatibilityStatus.INCOMPATIBLE) SoftErrorRose else BioluminescentCyan
                )
            }
        }
    }
}

@Composable
private fun SaveOrRenameAquariumDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepTrenchSurface,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Aquarium name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BioluminescentCyan,
                    contentColor = AbyssBlack
                )
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
