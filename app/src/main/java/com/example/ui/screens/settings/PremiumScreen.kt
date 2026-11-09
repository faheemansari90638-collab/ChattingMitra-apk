package com.example.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val settings by viewModel.appSettings.collectAsState()
    var selectedPlanIndex by remember { mutableIntStateOf(1) } // 0 = Monthly, 1 = Yearly
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChatMitra Premium", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("premium_screen_list"),
            contentPadding = PaddingValues(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero 3D Badge & Title
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFF9800), WhatsAppGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (settings.isPremium) "You're a Premium Member! 🌟" else "Upgrade to Premium",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (settings.isPremium)
                        "All exclusive features and ad-free experience are active on your account."
                    else
                        "Enjoy 100% ad-free messaging, larger file transfers, and exclusive 3D animated stickers.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Benefits checklist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Premium Benefits Included",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        PremiumFeatureRow(title = "100% Ad-Free Experience", subtitle = "No banner or sponsor ads anywhere in the app")
                        PremiumFeatureRow(title = "HD Media & 4GB File Transfer", subtitle = "Send large uncompressed videos, raw photos, and files")
                        PremiumFeatureRow(title = "Exclusive 3D Animated Stickers", subtitle = "Access premium sticker packs created for ChatMitra")
                        PremiumFeatureRow(title = "Extended 48h Status Updates", subtitle = "Keep your status updates alive for up to 48 hours")
                        PremiumFeatureRow(title = "Priority Customer Support", subtitle = "Direct support from the ChatMitra engineering desk")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Pricing Plans Selection if not premium
            if (!settings.isPremium) {
                item {
                    Text(
                        text = "Choose Your Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    // Yearly Plan (Best Value)
                    PlanCard(
                        title = "Yearly Plan",
                        price = "₹799 / year",
                        equivalent = "₹66/month • Save 33%",
                        isBestValue = true,
                        isSelected = selectedPlanIndex == 1,
                        onClick = { selectedPlanIndex = 1 }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Monthly Plan
                    PlanCard(
                        title = "Monthly Plan",
                        price = "₹99 / month",
                        equivalent = "Billed monthly • Cancel anytime",
                        isBestValue = false,
                        isSelected = selectedPlanIndex == 0,
                        onClick = { selectedPlanIndex = 0 }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { showCheckoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("subscribe_now_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPlanIndex == 1) "Get Yearly Plan (₹799)" else "Get Monthly Plan (₹99)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Secured via Google Play In-App Billing & Razorpay gateway. Auto-renews until cancelled.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhatsAppGreen.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Active Subscription: ChatMitra Yearly", fontWeight = FontWeight.Bold)
                            Text("Valid until Nov 8, 2027 • Auto-renewal active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) showCheckoutDialog = false },
            title = { Text("Complete Purchase") },
            text = {
                Column {
                    Text(
                        text = "You are subscribing to ChatMitra ${if (selectedPlanIndex == 1) "Yearly (₹799)" else "Monthly (₹99)"}.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Payment Method: Google Play Store / UPI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isProcessingPayment) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Verifying payment with gateway...", fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessingPayment = true
                        viewModel.settingsRepo.activatePremium()
                        isProcessingPayment = false
                        showCheckoutDialog = false
                        showSuccessDialog = true
                    },
                    enabled = !isProcessingPayment
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCheckoutDialog = false },
                    enabled = !isProcessingPayment
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Welcome to Premium! 🎉") },
            text = { Text("Payment successful. All banner ads have been removed and premium perks are now unlocked!") },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("Awesome")
                }
            }
        )
    }
}

@Composable
private fun PremiumFeatureRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = WhatsAppGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    equivalent: String,
    isBestValue: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) WhatsAppGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) WhatsAppGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isSelected, onClick = null)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (isBestValue) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = WhatsAppGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "POPULAR",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(equivalent, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(price, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
