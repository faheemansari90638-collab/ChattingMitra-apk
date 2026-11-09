package com.example.ui.screens.status

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStatusScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val colorOptions = listOf(
        0xFF008069, // Emerald
        0xFF009688, // Teal
        0xFF512DA8, // Deep Purple
        0xFFC2185B, // Rose Pink
        0xFFD84315, // Burnt Orange
        0xFF1565C0, // Ocean Blue
        0xFF37474F  // Charcoal Slate
    )

    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("") }

    val currentColorHex = colorOptions[selectedColorIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(currentColorHex))
            .testTag("create_status_screen")
    ) {
        // Top action bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    selectedColorIndex = (selectedColorIndex + 1) % colorOptions.size
                },
                modifier = Modifier.testTag("status_palette_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Change background color",
                    tint = Color.White
                )
            }
        }

        // Status text input center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            TextField(
                value = statusText,
                onValueChange = { if (it.length <= 250) statusText = it },
                placeholder = {
                    Text(
                        "Type a status...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_status_input")
            )
        }

        // Bottom color selector palette & Post FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                colorOptions.forEachIndexed { index, colorHex ->
                    val isSelected = index == selectedColorIndex
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { selectedColorIndex = index }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FloatingActionButton(
                    onClick = {
                        if (statusText.isNotBlank()) {
                            viewModel.statusRepo.addMyTextStatus(statusText.trim(), currentColorHex)
                            onBackClick()
                        }
                    },
                    containerColor = WhatsAppGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("post_status_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Post Status"
                    )
                }
            }
        }
    }
}
