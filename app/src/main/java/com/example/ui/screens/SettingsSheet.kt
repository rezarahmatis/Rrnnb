package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppSettings
import com.example.model.AudioSpectrum
import com.example.model.PerformanceMode
import com.example.ui.theme.BioluminescentCyan
import com.example.ui.theme.DeepTrenchSurface
import com.example.ui.theme.MistSilver
import com.example.ui.theme.PelagicCard
import com.example.ui.theme.PelagicCardElevated
import com.example.ui.theme.SeafoamMint
import com.example.ui.theme.SubtleGlassStroke
import com.example.ui.theme.WarmCoralGold
import kotlin.math.roundToInt

@Composable
fun SettingsSheet(
    settings: AppSettings,
    audioSpectrum: AudioSpectrum,
    onPerformanceModeChange: (PerformanceMode) -> Unit,
    onAmbientModeChange: (Boolean) -> Unit,
    onFullscreenChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onAudioReactivityChange: (Float) -> Unit,
    onOverrideCompatibilityChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.86f),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sanctuary Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "OpenGL ES 3.0 rendering, immersion & audio sensitivity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MistSilver
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close settings")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Performance Mode
                item(key = "performance_section") {
                    SettingsSectionCard(title = "Performance Mode") {
                        PerformanceMode.entries.forEach { mode ->
                            val selected = settings.performanceMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPerformanceModeChange(mode) }
                                    .padding(vertical = 8.dp)
                                    .testTag("perf_mode_${mode.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { onPerformanceModeChange(mode) },
                                    colors = RadioButtonDefaults.colors(selectedColor = BioluminescentCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (selected) BioluminescentCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MistSilver
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Audio-Reactive Water & Marine Life
                item(key = "audio_reactivity_section") {
                    SettingsSectionCard(title = "Music-Reactive Environment") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtle Harmonic Response",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${(settings.audioReactivityStrength * 100f).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = BioluminescentCyan
                            )
                        }
                        Slider(
                            value = settings.audioReactivityStrength,
                            onValueChange = onAudioReactivityChange,
                            valueRange = 0f..1.5f,
                            modifier = Modifier.testTag("audio_reactivity_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = BioluminescentCyan,
                                activeTrackColor = BioluminescentCyan,
                                inactiveTrackColor = SubtleGlassStroke
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FrequencyBandBar("Bass (Currents & Rays)", audioSpectrum.bass, BioluminescentCyan)
                        Spacer(modifier = Modifier.height(6.dp))
                        FrequencyBandBar("Mid (Swimming Cadence)", audioSpectrum.mid, SeafoamMint)
                        Spacer(modifier = Modifier.height(6.dp))
                        FrequencyBandBar("Treble (Caustics & Bubbles)", audioSpectrum.treble, WarmCoralGold)
                    }
                }

                // 3. Immersive Modes
                item(key = "immersive_section") {
                    SettingsSectionCard(title = "Immersive Modes") {
                        SettingsToggleRow(
                            title = "Ambient Mode (Hide UI)",
                            subtitle = "Pure uninterrupted digital aquarium while music plays. Tap water to return.",
                            checked = settings.ambientMode,
                            onCheckedChange = onAmbientModeChange,
                            tag = "setting_ambient_switch"
                        )
                        SettingsToggleRow(
                            title = "Fullscreen Edge-to-Edge",
                            subtitle = "Hide system status and navigation bars for gallery display.",
                            checked = settings.fullscreenMode,
                            onCheckedChange = onFullscreenChange,
                            tag = "setting_fullscreen_switch"
                        )
                        SettingsToggleRow(
                            title = "Screen Always On While Playing",
                            subtitle = "Prevent display sleep while enjoying your aquatic music session.",
                            checked = settings.keepScreenOn,
                            onCheckedChange = onKeepScreenOnChange,
                            tag = "setting_keep_screen_on_switch"
                        )
                        SettingsToggleRow(
                            title = "Reduced Motion",
                            subtitle = "Slower, gentler fish currents and calmer water movement.",
                            checked = settings.reducedMotion,
                            onCheckedChange = onReducedMotionChange,
                            tag = "setting_reduced_motion_switch"
                        )
                        SettingsToggleRow(
                            title = "Tactile Ripple Haptics",
                            subtitle = "Delicate haptic pulse when touching the water surface.",
                            checked = settings.hapticFeedback,
                            onCheckedChange = onHapticChange,
                            tag = "setting_haptics_switch"
                        )
                    }
                }

                // 4. Ecosystem Rules
                item(key = "ecosystem_section") {
                    SettingsSectionCard(title = "Ecosystem Rules") {
                        SettingsToggleRow(
                            title = "Allow Cross-Habitat Mixing",
                            subtitle = "Override natural freshwater / reef / deep-sea habitat restrictions when building custom aquariums.",
                            checked = settings.overrideCompatibility,
                            onCheckedChange = onOverrideCompatibilityChange,
                            tag = "setting_override_compat_switch"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PelagicCard,
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = BioluminescentCyan
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MistSilver)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
private fun FrequencyBandBar(
    label: String,
    value: Float,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MistSilver)
            Text("${(value * 100f).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = PelagicCardElevated
        )
    }
}
