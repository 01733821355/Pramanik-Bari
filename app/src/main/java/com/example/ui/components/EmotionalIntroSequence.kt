package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.sound.IntroSoundPlayer
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.delay

@Composable
fun EmotionalIntroSequence(
    isBn: Boolean,
    onEnterApp: () -> Unit
) {
    var scenePhase by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    val zoomAnim = remember { Animatable(1.0f) }
    val progressAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val soundPlayer = remember { IntroSoundPlayer() }

    // Start background soundscape on launch, and ensure cleanup on exit
    LaunchedEffect(Unit) {
        soundPlayer.start(coroutineScope, muted = isMuted)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.stop()
        }
    }

    LaunchedEffect(Unit) {
        // Slow Ken Burns zoom effect
        zoomAnim.animateTo(
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        // Timeline transitions
        delay(1200)
        scenePhase = 1
        delay(2200)
        scenePhase = 2
        delay(2400)
        scenePhase = 3
    }

    LaunchedEffect(Unit) {
        // Auto-advance progress
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 9500, easing = LinearEasing)
        )
        soundPlayer.stop()
        onEnterApp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("emotional_intro_screen")
    ) {
        // Cinematic Background Art with Gentle Zoom
        Image(
            painter = painterResource(id = R.drawable.pramanik_emotional_intro_1793460126526),
            contentDescription = "Pramanik Bari Lineage Story",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(zoomAnim.value)
        )

        // Dark Atmospheric Gradient Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000),
                            Color(0x66000000),
                            Color(0xDD0A1C14),
                            Color(0xFA07150F)
                        )
                    )
                )
        )

        // Floating Glowing Ember Memory Particles Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val time = (progressAnim.value * 300).toInt()

            for (i in 0..18) {
                val seedX = ((i * 73 + time * 2) % width.toInt()).toFloat()
                val seedY = height - (((i * 97 + time * 3) % height.toInt()).toFloat())
                val radius = (2f + (i % 3) * 1.5f)
                val alpha = (0.25f + ((i * 17) % 50) / 100f).coerceIn(0.1f, 0.8f)

                drawCircle(
                    color = GoldAccent.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(seedX, seedY)
                )
            }
        }

        // Top Controls: Sound Toggle (On/Off) + Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sound On / Off Toggle Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isMuted) Color(0x66444444) else Color(0x880C2B1E),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMuted) Color(0x66FFFFFF) else GoldAccent
                ),
                modifier = Modifier
                    .clickable {
                        val newMuted = !isMuted
                        isMuted = newMuted
                        soundPlayer.setMuted(newMuted)
                    }
                    .testTag("intro_sound_toggle_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) Color(0xFFC0C0C0) else GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMuted) {
                            if (isBn) "সাউন্ড বন্ধ" else "Sound Off"
                        } else {
                            if (isBn) "সাউন্ড চালু" else "Sound On"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Skip Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x66000000)
            ) {
                TextButton(
                    onClick = {
                        soundPlayer.stop()
                        onEnterApp()
                    },
                    modifier = Modifier.testTag("intro_skip_btn")
                ) {
                    Text(
                        text = if (isBn) "এড়িয়ে যান" else "Skip",
                        color = Color(0xFFF0F0F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Central Emotional Narration & Crest
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Crest Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, GoldAccent, CircleShape)
                    .background(Color(0xFF0C2B1E)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pramanik_bari_icon_1793460110911),
                    contentDescription = "Pramanik Bari Crest",
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle Prologue Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x44D4AF37),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
            ) {
                Text(
                    text = if (isBn) "ঐতিহ্যের বন্ধন • শিকড়ের স্মৃতিচারণ" else "ANCESTRAL HERITAGE ARCHIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isBn) "প্রামাণিক বাড়ি" else "Pramanik Bari",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Emotional Animated Sequence Text
            AnimatedVisibility(
                visible = scenePhase >= 1,
                enter = fadeIn(tween(800)),
                exit = fadeOut()
            ) {
                Text(
                    text = if (isBn) "“শিকড়ের টানে, রক্তের বন্ধনে...\nযে পূর্বপুরুষরা গড়ে গেছেন আমাদের পরিচয়।”"
                    else "“Rooted in lineage, connected by blood...\nHonoring the ancestors who shaped our identity.”",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFE8ECE9),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = scenePhase >= 2,
                enter = fadeIn(tween(1000)),
                exit = fadeOut()
            ) {
                Text(
                    text = if (isBn) "পূর্বপুরুষদের পুণ্য স্মৃতি অম্লান থাকুক প্রজন্ম থেকে প্রজন্মে..."
                    else "May the blessings and memories of our elders flourish across generations...",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        }

        // Bottom Call to Action and Timeline Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    soundPlayer.stop()
                    onEnterApp()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.5.dp, GoldAccent, RoundedCornerShape(14.dp))
                    .testTag("btn_enter_lineage")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBn) "বংশলতিকায় প্রবেশ করুন" else "Enter Digital Family Tree",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-advance indicator
            LinearProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .clip(CircleShape),
                color = GoldAccent,
                trackColor = Color(0x33FFFFFF),
            )
        }
    }
}
