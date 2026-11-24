package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.WorldCatalog
import com.example.model.WorldCategory
import com.example.model.WorldEnvironment
import com.example.ui.components.WorldArtThumbnail
import com.example.ui.theme.BioluminescentCyan
import com.example.ui.theme.DeepTrenchSurface
import com.example.ui.theme.MistSilver
import com.example.ui.theme.PelagicCard
import com.example.ui.theme.SubtleGlassStroke

@Composable
fun WorldSelectorSheet(
    currentWorld: WorldEnvironment,
    onSelectWorld: (WorldEnvironment) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<WorldCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredWorlds = remember(selectedCategory, searchQuery) {
        WorldCatalog.worlds.filter { world ->
            val catMatch = selectedCategory == null || world.category == selectedCategory
            val qMatch = searchQuery.isBlank() ||
                world.name.contains(searchQuery, ignoreCase = true) ||
                world.subtitle.contains(searchQuery, ignoreCase = true) ||
                world.habitat.label.contains(searchQuery, ignoreCase = true)
            catMatch && qMatch
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = DeepTrenchSurface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aquatic Worlds",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${WorldCatalog.worlds.size} living ecosystems • Instant GPU transition",
                        style = MaterialTheme.typography.bodySmall,
                        color = MistSilver
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_world_selector_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close world selector",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("world_search_input"),
                placeholder = { Text("Search reefs, lakes, abyss, ruins…", color = MistSilver) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MistSilver)
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MistSilver)
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BioluminescentCyan,
                    unfocusedBorderColor = SubtleGlassStroke,
                    focusedContainerColor = PelagicCard.copy(alpha = 0.7f),
                    unfocusedContainerColor = PelagicCard.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${WorldCatalog.worlds.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BioluminescentCyan.copy(alpha = 0.22f),
                            selectedLabelColor = BioluminescentCyan
                        )
                    )
                }
                items(WorldCategory.entries) { category ->
                    val count = WorldCatalog.worlds.count { it.category == category }
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text("${category.label} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BioluminescentCyan.copy(alpha = 0.22f),
                            selectedLabelColor = BioluminescentCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Adaptive grid of world art cards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredWorlds, key = { it.id }) { world ->
                    WorldGalleryCard(
                        world = world,
                        isSelected = world.id == currentWorld.id,
                        onClick = {
                            onSelectWorld(world)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldGalleryCard(
    world: WorldEnvironment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("world_card_${world.id}"),
        shape = RoundedCornerShape(18.dp),
        color = PelagicCard,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BioluminescentCyan else SubtleGlassStroke
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WorldArtThumbnail(
                world = world,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom editorial gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.42f to Color(0x24040C14),
                                1.0f to Color(0xEB040C14)
                            )
                        )
                    )
            )

            // Top badges: habitat + selected check
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x88040C14)
                ) {
                    Text(
                        text = world.habitat.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MistSilver,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BioluminescentCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active world",
                            tint = Color(0xFF040B12),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // World title & poetic subtitle
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = world.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = world.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MistSilver,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
