package com.rfid.rfidreader.ui

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.Glide
import com.rfid.rfidreader.R
import com.rfid.rfidreader.data.api.ColorVariantItem
import com.rfid.rfidreader.data.api.SimilarProductItem
import com.rfid.rfidreader.model.TryOnDisplayItem
import com.rfid.rfidreader.ui.theme.RFIDREADERTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

// ─────────────────────────────────────────────────────────────────────────────
// Route entry-point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TryOnRoute(
    viewModel: TryOnViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.assistanceEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkoutEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.ratingEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onLogout()
        }
    }

    TryOnScreen(
        uiState = uiState,
        onRetry = viewModel::retryNow,
        onItemSelected = viewModel::selectItem,
        onVariantSelected = viewModel::selectVariant,
        onLocationSelected = viewModel::selectLocation,
        onSizeSelected = viewModel::selectSize,
        onBack = viewModel::clearSelection,
        // onChatClicked = viewModel::callStaffAssistance,
        onSimilarProductSelected = viewModel::selectSimilarProduct,
        // onSubmitRating = viewModel::submitRating,
        onLogout = viewModel::logout
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Main screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TryOnScreen(
    uiState: TryOnUiState,
    onRetry: () -> Unit,
    onItemSelected: (String) -> Unit,
    onVariantSelected: (ColorVariantItem) -> Unit,
    onLocationSelected: (String) -> Unit,
    onSizeSelected: (String) -> Unit,
    onBack: () -> Unit,
    onChatClicked: () -> Unit = {},
    onSimilarProductSelected: (SimilarProductItem) -> Unit = {},
    onSubmitRating: (epc: String?, sku: String?, rating: Float, feedback: String?) -> Unit = { _, _, _, _ -> },
    onLogout: () -> Unit
) {
    val context = LocalContext.current


    // FAB is only shown on the product preview screen
    val showChatFab = false // uiState.selectedItemId != null

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (showChatFab) {
                FloatingActionButton(
                    onClick = { onChatClicked() },
                    modifier = Modifier
                        .size(60.dp)
                        .padding(bottom = 8.dp, end = 8.dp),
                    shape = CircleShape,
                    containerColor = if (uiState.isCallingAssistance) Color(0xFF5ECDD0) else Color(0xFF14B8C4),
                    contentColor = Color.White,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    if (uiState.isCallingAssistance) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_bubble),
                            contentDescription = "Chat",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PantaloonsTopBanner(
                    uiState = uiState,
                    onLocationSelected = onLocationSelected,
                    onLogout = onLogout
                )

                Column(
                    modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.errorMessage?.let { message ->
                        ErrorBanner(message = message, onRetry = onRetry)
                    }

                when {
                    uiState.isLoading && uiState.filteredItems.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.filteredItems.isEmpty() -> EmptyState(onRetry = onRetry)

                    else -> {
                        val totalItems = uiState.filteredItems.size
                        val selectedItemId = uiState.selectedItemId
                        val isWelcomeVisible = totalItems > 1 && selectedItemId == null

                        if (isWelcomeVisible) {
                            // Welcome selection screen
                            WelcomeScreen(
                                items = uiState.filteredItems,
                                onItemSelected = onItemSelected,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.isLoadingVariants) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF1BB8B4)
                                )
                            }
                        } else {
                            // Product preview screen - Single unified page layout
                            val primaryItem = uiState.featuredItem ?: uiState.filteredItems.first()
                            val previewImageUrl = uiState.selectedVariant?.imageUrl ?: primaryItem.imageUrl

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White),
                                verticalArrangement = Arrangement.Top
                            ) {
                                // Back button
                                if (totalItems > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = onBack,
                                            modifier = Modifier
                                                .background(Color(0xFFF3F4F6), CircleShape)
                                                .size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                tint = Color(0xFF1BB8B4)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Back to Selection",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF4B5563),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Main product section: Images LEFT + Details RIGHT
                                UnifiedProductSection(
                                    imageUrl = previewImageUrl,
                                    item = primaryItem,
                                    variants = uiState.variants,
                                    selectedVariant = uiState.selectedVariant,
                                    isLoadingVariants = uiState.isLoadingVariants,
                                    onVariantSelected = onVariantSelected,
                                    onSizeSelected = onSizeSelected,
                                    isSubmittingRating = uiState.isSubmittingRating,
                                    hasAlreadyRated = run {
                                        val sku = (uiState.selectedVariant?.sku ?: primaryItem.sku).trim()
                                        sku.isNotEmpty() && uiState.ratedSkus.contains(sku)
                                    },
                                    onSubmitRating = { rating, feedback ->
                                        onSubmitRating(
                                            primaryItem.tagName,
                                            uiState.selectedVariant?.sku ?: primaryItem.sku,
                                            rating,
                                            feedback
                                        )
                                    }
                                )

                                // "More from Brand" section - naturally continues below
                                if (uiState.isLoadingSimilarProducts || uiState.similarProducts.isNotEmpty()) {
                                    SimilarProductsSection(
                                        brand = primaryItem.brand,
                                        products = uiState.similarProducts,
                                        isLoading = uiState.isLoadingSimilarProducts,
                                        onProductClick = onSimilarProductSelected,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                                    )
                                }

                                // Bottom spacing to ensure content isn't hidden behind chat button
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Welcome screen (product selection)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeScreen(
    items: List<TryOnDisplayItem>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(4.dp, Color(0xFF1BB8B4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        // Outer Box provides the bounded viewport; inner Column can scroll within it.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())   // ← vertical scroll enabled
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = "WELCOME TO PANTALOONS",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1BB8B4),
                    textAlign = TextAlign.Center
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "ITEMS WITH YOU",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center
                    )
                    // Row + horizontalScroll instead of LazyRow — LazyRow inside
                    // verticalScroll gives unbounded height to its children, causing a crash.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { item ->
                            WelcomeProductItem(item = item, onClick = { onItemSelected(item.id) })
                        }
                    }
                    Text(
                        text = "CHOOSE AN ITEM YOU’D LIKE TO EXPLORE",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeProductItem(item: TryOnDisplayItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            ProductImage(
                item = item,
                modifier = Modifier.size(150.dp).clip(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.brand,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PantaloonsTopBanner(
    uiState: TryOnUiState,
    onLocationSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", color = Color.Black) },
            text = { Text("Are you sure you want to logout?", color = Color.Black) },
            containerColor = Color.White,
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Black)
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1BB8B4))
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "WELCOME TO PANTALOONS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState.availableLocations.size > 1) {
                Box {
                    Surface(
                        onClick = { expanded = true },
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedLocation.ifBlank { "TRIAL ROOM" }.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        },
                        modifier = Modifier
                            .background(Color.White)
                            .widthIn(min = 160.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Show All",
                                    color = if (uiState.selectedLocation.isBlank()) Color(0xFF1BB8B4) else Color.Black,
                                    fontWeight = if (uiState.selectedLocation.isBlank()) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                expanded = false
                                onLocationSelected("")
                            }
                        )

                        uiState.availableLocations.forEach { location ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        location,
                                        color = if (uiState.selectedLocation == location) Color(0xFF1BB8B4) else Color.Black,
                                        fontWeight = if (uiState.selectedLocation == location) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    onLocationSelected(location)
                                }
                            )
                        }
                    }
                }
            } else if (uiState.selectedLocation.isNotBlank()) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.selectedLocation.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(onClick = { moreMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = { moreMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Logout", color = Color.Black) },
                        onClick = {
                            moreMenuExpanded = false
                            showLogoutDialog = true
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error / empty states
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
        border = BorderStroke(1.dp, Color(0xFFFDA4AF))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = Color(0xFF9F1239),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = onRetry) {
                Text(text = stringResource(id = R.string.action_retry))
            }
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "WELCOME TO PANTALOONS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1BB8B4),
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ──────────────────────────────────────────────────────────────��──────────────
// Unified product section (Images LEFT + Details RIGHT - Single Page Layout)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UnifiedProductSection(
    imageUrl: String?,
    item: TryOnDisplayItem,
    variants: List<ColorVariantItem>,
    selectedVariant: ColorVariantItem?,
    isLoadingVariants: Boolean,
    onVariantSelected: (ColorVariantItem) -> Unit,
    onSizeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSubmittingRating: Boolean = false,
    hasAlreadyRated: Boolean = false,
    onSubmitRating: (rating: Float, feedback: String) -> Unit = { _, _ -> }
) {
    val previewColor = selectedVariant?.color ?: item.color
    val previewSize = selectedVariant?.itemSize ?: item.size

    // Unique colors — deduplicated by normalised color string
    val uniqueColorVariants = remember(variants) {
        variants.distinctBy { it.color?.lowercase()?.trim() ?: "" }
    }

    // ALL unique sizes across every color/variant
    val allSizes = remember(variants) {
        variants
            .mapNotNull { it.itemSize?.trim()?.ifBlank { null } }
            .distinct()
            .sortedBySizeOrder()
    }

    // Sizes available only for the currently selected color
    val sizesForColor = remember(variants, selectedVariant) {
        val targetColor = selectedVariant?.color?.lowercase()?.trim()
            ?: uniqueColorVariants.firstOrNull()?.color?.lowercase()?.trim()
        variants
            .filter { it.color?.lowercase()?.trim() == targetColor }
            .mapNotNull { it.itemSize?.trim()?.ifBlank { null } }
            .toSet()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Image 1 - Left
        FeaturedFixedImage(
            imageUrl = imageUrl,
            modifier = Modifier
                .weight(0.25f)
                .height(400.dp)
        )

        // Image 2 - Center
        ZoomableProductImage(
            imageUrl = imageUrl,
            modifier = Modifier
                .weight(0.25f)
                .height(400.dp)
        )

        // Product Details - Right (50% width)
        Column(
            modifier = Modifier
                .weight(0.5f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // ─────────────────────────────────────────────────────────
            // SECTION 1: Product Information
            // ─────────────────────────────────────────────────────────
            InlineKeyValueField(label = stringResource(id = R.string.label_brand), value = item.brand)
            InlineKeyValueField(label = stringResource(id = R.string.label_product), value = item.product)

            /* // People tried this info - positioned between Product and Gender
            PeopleTriedInfo(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth()
            ) */

            // ─────────────────────────────────────────────────────────
            // SECTION 2: Product Attributes
            // ─────────────────────────────────────────────────────────
            InlineKeyValueField(label = stringResource(id = R.string.label_gender), value = item.gender)
            InlineKeyValueField(label = stringResource(id = R.string.label_color), value = previewColor)
            InlineKeyValueField(label = stringResource(id = R.string.label_size), value = previewSize)

            // ─────────────────────────────────────────────────────────
            // SECTION 3: Interactive Controls
            // ─────────────────────────────────────────────────────────
            // Color selector
            if (isLoadingVariants) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1BB8B4)
                )
            } else if (uniqueColorVariants.isNotEmpty()) {
                // SELECT COLOR section
                Text(
                    text = "SELECT COLOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uniqueColorVariants.forEach { variant ->
                        val isSelected = variant.color?.lowercase()?.trim() ==
                            selectedVariant?.color?.lowercase()?.trim()
                        ColorVariantImageCard(
                            variant = variant,
                            isSelected = isSelected,
                            onClick = { onVariantSelected(variant) }
                        )
                    }
                }

                // SELECT SIZE section
                if (allSizes.isNotEmpty()) {
                    Text(
                        text = "SELECT SIZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    WrapRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp
                    ) {
                        allSizes.forEach { size ->
                            val isAvailable = size in sizesForColor
                            SizeChip(
                                size = size,
                                isSelected = isAvailable && size == previewSize,
                                isEnabled = isAvailable,
                                onClick = { onSizeSelected(size) }
                            )
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────
            // SECTION 4: Sold Items Info
            // ─────────────────────────────────────────────────────────
            TrendingSalesInfo(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )

            // SECTION 5: Rate this Product
            // (sits between Popular Choice and "More from Brand")
            // ─────────────────────────────────────────────────────────
            /* RateThisProductSection(
                isSubmitting = isSubmittingRating,
                hasAlreadyRated = hasAlreadyRated,
                onSubmit = onSubmitRating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) */
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Featured product card (preview + color selector + size chips)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeaturedProductCard(
    item: TryOnDisplayItem,
    variants: List<ColorVariantItem>,
    selectedVariant: ColorVariantItem?,
    isLoadingVariants: Boolean,
    isFocused: Boolean,
    onVariantSelected: (ColorVariantItem) -> Unit,
    onSizeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val previewImageUrl = selectedVariant?.imageUrl ?: item.imageUrl
    val previewColor    = selectedVariant?.color    ?: item.color
    val previewSize     = selectedVariant?.itemSize ?: item.size

    // Unique colors — deduplicated by normalised color string
    val uniqueColorVariants = remember(variants) {
        variants.distinctBy { it.color?.lowercase()?.trim() ?: "" }
    }

    // ALL unique sizes across every color/variant (used to keep layout stable)
    val allSizes = remember(variants) {
        variants
            .mapNotNull { it.itemSize?.trim()?.ifBlank { null } }
            .distinct()
            .sortedBySizeOrder()
    }

    // Sizes available only for the currently selected color
    val sizesForColor = remember(variants, selectedVariant) {
        val targetColor = selectedVariant?.color?.lowercase()?.trim()
            ?: uniqueColorVariants.firstOrNull()?.color?.lowercase()?.trim()
        variants
            .filter { it.color?.lowercase()?.trim() == targetColor }
            .mapNotNull { it.itemSize?.trim()?.ifBlank { null } }
            .toSet()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.Top
    ) {
            // ── LEFT  50%: two stacked preview images ──────────────────────
            Row(
                modifier = Modifier.weight(0.50f).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                FeaturedFixedImage(
                    imageUrl = previewImageUrl,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                ZoomableProductImage(
                    imageUrl = previewImageUrl,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // ── RIGHT 50%: details + color selector + size chips ───────────
            // Box holds the bounded height; the inner Column scrolls within it.
            // Combining fillMaxHeight() + verticalScroll() on the same composable
            // causes an IllegalStateException in Compose — always split them.
            Box(
                modifier = Modifier
                    .weight(0.50f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // ── Static product info ────────────────────────────────────
                    InlineKeyValueField(label = stringResource(id = R.string.label_brand),   value = item.brand)
                    InlineKeyValueField(label = stringResource(id = R.string.label_product), value = item.product)
                    InlineKeyValueField(label = stringResource(id = R.string.label_gender),  value = item.gender)

                    // ── Dynamic fields (update from selected variant) ──────────
                    InlineKeyValueField(label = stringResource(id = R.string.label_color), value = previewColor)
                    InlineKeyValueField(label = stringResource(id = R.string.label_size),  value = previewSize)

                    // ── Color selector section ─────────────────────────────────
                    if (isLoadingVariants) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1BB8B4)
                        )
                    } else if (uniqueColorVariants.isNotEmpty()) {
                        Text(
                            text = "SELECT COLOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 1.sp
                        )

                        // Use a regular Row + horizontalScroll to avoid
                        // LazyRow-inside-verticalScroll measurement conflicts.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uniqueColorVariants.forEach { variant ->
                                val isSelected = variant.color?.lowercase()?.trim() ==
                                    selectedVariant?.color?.lowercase()?.trim()
                                ColorVariantImageCard(
                                    variant = variant,
                                    isSelected = isSelected,
                                    onClick = { onVariantSelected(variant) }
                                )
                            }
                        }

                        // ── Size chips: ALL sizes shown; unavailable = greyed ──
                        if (allSizes.isNotEmpty()) {
                            Text(
                                text = "SELECT SIZE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp
                            )

                            WrapRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalSpacing = 8.dp,
                                verticalSpacing = 8.dp
                            ) {
                                allSizes.forEach { size ->
                                    val isAvailable = size in sizesForColor
                                    SizeChip(
                                        size = size,
                                        isSelected = isAvailable && size == previewSize,
                                        isEnabled = isAvailable,
                                        onClick = { onSizeSelected(size) }
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic trending sales information
                    TrendingSalesInfo(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .fillMaxWidth()
                    )
                }
            }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// "More from Brand" similar products horizontal strip
// ──────��──────────────────────────────────────────────────────────────────────

@Composable
private fun SimilarProductsSection(
    brand: String,
    products: List<SimilarProductItem>,
    isLoading: Boolean,
    onProductClick: (SimilarProductItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep first product per color for a cleaner "More from Brand" strip.
    val uniqueProducts = remember(products) {
        products.distinctBy { it.color?.trim()?.lowercase().orEmpty() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "More from $brand",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1BB8B4),
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF1BB8B4)
                )
            }
        }

        if (uniqueProducts.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(uniqueProducts) { product ->
                    SimilarProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimilarProductCard(
    product: SimilarProductItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { imageView ->
                            Glide.with(imageView)
                                .load(product.imageUrl)
                                .placeholder(R.drawable.img_placeholder)
                                .error(R.drawable.img_error)
                                .override(260, 200)
                                .centerCrop()
                                .into(imageView)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.brand?.take(2)?.uppercase() ?: "?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // Brand name
            if (!product.brand.isNullOrBlank()) {
                Text(
                    text = product.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1BB8B4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Product name
            Text(
                text = product.product ?: product.sku ?: "-",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF111827),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Color variant image card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ColorVariantImageCard(
    variant: ColorVariantItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF14B8C4) else Color(0xFFDADADA)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 88.dp)
                .shadow(if (isSelected) 6.dp else 0.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(borderWidth, borderColor, shape)
                .clickable(onClick = onClick)
        ) {
            if (!variant.imageUrl.isNullOrBlank()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { imageView ->
                        Glide.with(imageView)
                            .load(variant.imageUrl)
                            .placeholder(R.drawable.img_placeholder)
                            .error(R.drawable.img_error)
                            .apply(FastImageLoader.requestOptions)
                            .into(imageView)
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = variant.color?.take(2)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

        }

        // Color label
        Text(
            text = variant.color?.split(",")?.firstOrNull()?.trim() ?: "-",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = if (isSelected) Color(0xFF14B8C4) else Color(0xFF6B7280),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Size chip
// ─────────────────────────────────────────────────────────────────────────────
// WrapRow — stable wrapping row (replaces experimental FlowRow)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val hSpacingPx = horizontalSpacing.roundToPx()
        val vSpacingPx = verticalSpacing.roundToPx()
        val maxWidth = constraints.maxWidth

        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        for (placeable in placeables) {
            val needed = placeable.width + if (currentRow.isEmpty()) 0 else hSpacingPx
            if (currentRow.isNotEmpty() && currentRowWidth + needed > maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += needed
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val rowHeights = rows.map { row -> row.maxOf { it.height } }
        val totalHeight = rowHeights.sum() + (rows.size - 1).coerceAtLeast(0) * vSpacingPx
        val width = constraints.maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)

        layout(width, totalHeight.coerceAtLeast(0)) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hSpacingPx
                }
                y += rowHeights[rowIndex] + vSpacingPx
            }
        }
    }
}

// ───────────────────────────────────────��─────────────────────────────────────

@Composable
private fun SizeChip(size: String, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    val bgColor = when {
        isSelected && isEnabled -> Color(0xFF1BB8B4)
        !isEnabled              -> Color(0xFFF5F5F5)
        else                    -> Color.White
    }
    val textColor = when {
        isSelected && isEnabled -> Color.White
        !isEnabled              -> Color(0xFFBDBDBD)
        else                    -> Color(0xFF374151)
    }
    val borderColor = when {
        isSelected && isEnabled -> Color(0xFF1BB8B4)
        !isEnabled              -> Color(0xFFE0E0E0)
        else                    -> Color(0xFFD1D5DB)
    }
    val borderWidth = if (isSelected && isEnabled) 2.dp else 1.dp
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .widthIn(min = 44.dp)
            .clip(shape)
            .background(bgColor)
            .border(borderWidth, borderColor, shape)
            .then(if (isEnabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = size,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected && isEnabled) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Image views
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeaturedFixedImage(imageUrl: String?, modifier: Modifier = Modifier) {
    val frameShape = RoundedCornerShape(12.dp)
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier.clip(frameShape).border(1.dp, Color(0xFFD1D5DB), frameShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No image", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
        }
        return
    }
    // key() forces AndroidView recreation when the URL changes, ensuring Glide
    // always loads the correct colour image rather than re‑using a cached view.
    key(imageUrl) {
        AndroidView(
            modifier = modifier.clip(frameShape).border(1.dp, Color(0xFFD1D5DB), frameShape),
            factory = { context ->
                ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                }
            },
            update = { imageView ->
                Glide.with(imageView)
                    .load(imageUrl)
                    .error(R.drawable.img_error)
                    .apply(FastImageLoader.requestOptions)
                    .into(imageView)
            }
        )
    }
}

@Composable
private fun ZoomableProductImage(imageUrl: String?, modifier: Modifier = Modifier) {
    val frameShape = RoundedCornerShape(12.dp)
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = modifier.clip(frameShape).border(1.dp, Color(0xFFD1D5DB), frameShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No image", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
        }
        return
    }
    // key() forces recreation on URL change — guarantees the correct colour image is shown.
    key(imageUrl) {
        AndroidView(
            modifier = modifier.clip(frameShape).border(1.dp, Color(0xFFD1D5DB), frameShape),
            factory = { context -> ZoomableImageView(context) },
            update = { zoomView ->
                Glide.with(zoomView)
                    .load(imageUrl)
                    .error(R.drawable.img_error)
                    .apply(FastImageLoader.requestOptions)
                    .into(zoomView)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Welcome-screen product thumbnail (with loading spinner)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductImage(item: TryOnDisplayItem, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    var isImageLoaded by remember { mutableStateOf(false) }
    var showSpinner   by remember { mutableStateOf(true) }

    LaunchedEffect(item.imageUrl) {
        isImageLoaded = false
        showSpinner = true
        delay(1500)
        if (!isImageLoaded) showSpinner = false
    }

    if (!item.imageUrl.isNullOrBlank()) {
        Box(modifier = modifier.clip(shape).background(Color(0xFFF3F4F6))) {
            PlaceholderContent(item = item, modifier = Modifier.fillMaxSize())
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        adjustViewBounds = true
                    }
                },
                update = { imageView ->
                    Glide.with(imageView)
                        .load(item.imageUrl)
                        .thumbnail(0.10f)
                        .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(300))
                        .apply(FastImageLoader.requestOptions)
                        .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                            override fun onResourceReady(
                                resource: android.graphics.drawable.Drawable,
                                transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                            ) {
                                imageView.setImageDrawable(resource)
                                isImageLoaded = true
                                showSpinner = false
                            }
                            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                                imageView.setImageDrawable(placeholder)
                            }
                            override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                                showSpinner = false
                            }
                        })
                }
            )
            if (showSpinner) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF1BB8B4),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(Brush.linearGradient(listOf(Color(0xFF9CA3AF), Color(0xFFD1D5DB))))
                .border(1.dp, Color(0xFFE5E7EB), shape),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderContent(item = item, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PlaceholderContent(item: TryOnDisplayItem, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.25f)) {
                Text(
                    text = item.descp.take(1).uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = item.brand.take(20),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InlineKeyValueField(label: String, value: String) {
    val text = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$label: ") }
        withStyle(SpanStyle(fontWeight = FontWeight.Normal)) { append(value) }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF111827),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Size ordering helper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Sorts a list of size strings in the conventional retail order:
 *   1. Roman/letter sizes: XS → S → M → L → XL → XXL → 2XL → 3XL
 *   2. Numeric sizes ascending (28, 30, 32 …)
 *   3. Everything else (kids / custom) alphabetically
 */
private fun List<String>.sortedBySizeOrder(): List<String> {
    val romanOrder = listOf("XS", "S", "M", "L", "XL", "XXL", "2XL", "3XL", "XXXL", "4XL")
    return sortedWith(Comparator { a, b ->
        val aU = a.uppercase()
        val bU = b.uppercase()
        val aRoman = romanOrder.indexOf(aU)
        val bRoman = romanOrder.indexOf(bU)
        val aNum = a.toIntOrNull()
        val bNum = b.toIntOrNull()
        when {
            aRoman >= 0 && bRoman >= 0 -> aRoman.compareTo(bRoman)
            aRoman >= 0 && bNum == null -> -1   // roman before numeric/kids
            bRoman >= 0 && aNum == null -> 1
            aRoman >= 0 -> -1
            bRoman >= 0 -> 1
            aNum != null && bNum != null -> aNum.compareTo(bNum)
            aNum != null -> -1   // numeric before text
            bNum != null -> 1
            else -> a.compareTo(b)
        }
    })
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 1200, heightDp = 800, name = "Selection Screen")
@Composable
private fun TryOnScreenSelectionPreview() {
    RFIDREADERTheme(dynamicColor = false) {
        TryOnScreen(
            uiState = TryOnUiState(
                items = sampleItems,
                selectedItemId = null,
                isLoading = false,
                lastUpdated = "25 May 2026, 06:15:10 PM"
            ),
            onRetry = {},
            onItemSelected = {},
            onVariantSelected = {},
            onSizeSelected = {},
            onLocationSelected = {},
            onBack = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800, name = "Product Detail")
@Composable
private fun TryOnScreenDetailPreview() {
    RFIDREADERTheme(dynamicColor = false) {
        TryOnScreen(
            uiState = TryOnUiState(
                items = sampleItems,
                selectedItemId = "1",
                isLoading = false,
                lastUpdated = "25 May 2026, 06:15:10 PM",
                availableLocations = listOf("fitting_room_1", "fitting_room_2")
            ),
            onRetry = {},
            onItemSelected = {},
            onVariantSelected = {},
            onSizeSelected = {},
            onLocationSelected = {},
            onBack = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800, name = "Empty State")
@Composable
private fun TryOnScreenEmptyPreview() {
    RFIDREADERTheme(dynamicColor = false) {
        TryOnScreen(
            uiState = TryOnUiState(
                items = emptyList(),
                isLoading = false
            ),
            onRetry = {},
            onItemSelected = {},
            onVariantSelected = {},
            onSizeSelected = {},
            onLocationSelected = {},
            onBack = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800, name = "Loading State")
@Composable
private fun TryOnScreenLoadingPreview() {
    RFIDREADERTheme(dynamicColor = false) {
        TryOnScreen(
            uiState = TryOnUiState(
                items = emptyList(),
                isLoading = true
            ),
            onRetry = {},
            onItemSelected = {},
            onVariantSelected = {},
            onSizeSelected = {},
            onLocationSelected = {},
            onBack = {},
            onLogout = {}
        )
    }
}

private val sampleItems = listOf(
    TryOnDisplayItem(
        id = "1",
        tagName = "E2801191A50300636F706D09",
        sku = "190000021031",
        product = "Pantaloon",
        descp = "Pantaloon",
        info = "MEN APPAREL • MEN APPAREL • MEN • Teal Blue",
        size = "M",
        timestamp = "25 May 2026, 06:15 PM",
        startTimeText = "25 May 2026, 06:15 PM",
        endTimeText = "25 May 2026, 06:26 PM",
        location = "fitting_room_2",
        department = "Apparel",
        duration = "00h 00m",
        imageUrl = "https://content.pantaloons.com/PT2026/8909429175783_2.jpg",
        brand = "MEN APPAREL",
        category = "MEN APPAREL",
        gender = "MEN",
        color = "Teal Blue",
        status = "IN",
        eventTimestampMillis = null
    ),
    TryOnDisplayItem(
        id = "2",
        tagName = "E2801191A50300636F70B279",
        sku = "190000021032",
        product = "Pantaloon",
        descp = "Pantaloon",
        info = "SF JEANS • MEN APPAREL • MEN • Black",
        size = "M",
        timestamp = "25 May 2026, 06:15 PM",
        startTimeText = "25 May 2026, 06:15 PM",
        endTimeText = "25 May 2026, 06:26 PM",
        location = "fitting_room_2",
        department = "Apparel",
        duration = "00h 00m",
        imageUrl = null,
        brand = "SF JEANS",
        category = "MEN APPAREL",
        gender = "MEN",
        color = "Black",
        status = "IN",
        eventTimestampMillis = null
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Trending Sales Info
// ────────────────────────────────────────────────────────────────���────────────

@Composable
private fun TrendingSalesInfo(
    modifier: Modifier = Modifier
) {
    // Generate random count between 15 and 70
    val count = remember { kotlin.random.Random.nextInt(15, 71) }
    val soldMessage = "$count sold in the past 15 days"
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Fire emoji for indicator
        Text(
            text = "🔥",
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Text(
            text = soldMessage,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827),
            lineHeight = 18.sp
        )
    }
}

/* private fun generateRandomTrendingMessage(): String {
    val random = kotlin.random.Random.Default
    
    // Random count between 10 and 60
    val count = random.nextInt(10, 61)

    // Random time period templates
    val templates = listOf(
        "$count sold in the last week",
        "$count sold in the past 15 days",
        "$count purchases in the last month",
        "Trending now • $count sold recently",
        "More than $count customers bought this recently",
        "$count people bought this in the last week",
        "Popular choice • $count sold in the past month",
        "$count customers purchased this recently"
    )
    
    return templates.random()
} */

// ─────────────────────────────────────────────────────────────────────────────
// People Tried This Info
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PeopleTriedInfo(
    modifier: Modifier = Modifier
) {
    // Generate random "people tried" message on each composition
    val triedMessage = remember {
        generateRandomTriedMessage()
    }

    // Blinking animation - smooth fade in/out effect
    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Eyes emoji for people tried indicator
        Text(
            text = "👀",
            fontSize = 18.sp,
            modifier = Modifier
                .padding(end = 8.dp)
                .alpha(alpha)
        )

        Text(
            text = triedMessage,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFC107),  // Bright yellow
            lineHeight = 18.sp,
            modifier = Modifier.alpha(alpha)
        )
    }
}

private fun generateRandomTriedMessage(): String {
    val random = kotlin.random.Random.Default

    // Random sold count between 10 and 60
    val soldCount = random.nextInt(10, 61)

    // Tried count should always be greater than sold count
    //Add minimum 10-20 extra to sold count, max 30 extra
    val extraCount = random.nextInt(10, 31)
    val triedCount = soldCount + extraCount

    // Random time period templates - tried count always greater than sold
    val templates = listOf(
        "$triedCount people tried this recently",
        "$triedCount people tried this in the past week",
        "$triedCount people tried this in the last 2 weeks",
        "$triedCount customers tried this recently",
        "$triedCount people checked this out recently",
        "$triedCount shoppers tried this in the past week"
    )

    return templates.random()
}

// ─────────────────────────────────────────────────────────────────────────────
// Rate This Product
// ─────────────────────────────────────────────────────────────────────────────

private val PantaloonsTeal = Color(0xFF1BB8B4)
private val RatingStarGold = Color(0xFFFFC107)
private val RatingStarGrey = Color(0xFFD3D3D3)

/** Shape that clips to the left [fraction] of its bounds (used for half-star overlay). */
private class LeftFractionShape(private val fraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val clamped = fraction.coerceIn(0f, 1f)
        return Outline.Rectangle(
            Rect(left = 0f, top = 0f, right = size.width * clamped, bottom = size.height)
        )
    }
}

/**
 * Interactive star supporting 0 / 0.5 / 1 fill states.
 * Tap left half → onTap(0.5f), tap right half → onTap(1.0f).
 */
@Composable
private fun HalfStar(
    fillFraction: Float,
    enabled: Boolean,
    onTap: (halfValue: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val isLeftHalf = offset.x < size.width / 2f
                    onTap(if (isLeftHalf) 0.5f else 1.0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Background: grey outline star (always shown)
        Text(
            text = "★",
            fontSize = 34.sp,
            color = RatingStarGrey,
            fontWeight = FontWeight.Normal
        )

        // Foreground: gold filled star, horizontally clipped to fillFraction
        if (fillFraction > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(LeftFractionShape(fillFraction)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    fontSize = 34.sp,
                    color = RatingStarGold,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun RateThisProductSection(
    isSubmitting: Boolean,
    hasAlreadyRated: Boolean,
    onSubmit: (rating: Float, feedback: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableStateOf(0f) }
    var feedbackText by remember { mutableStateOf("") }

    val ratingLocked = hasAlreadyRated || isSubmitting

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "RATE THIS PRODUCT",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            letterSpacing = 0.6.sp
        )

        // Interactive 5-star row with half-star support.
        // Initial state: 5 grey outline stars (gold layer = 0 fraction).
        // Tap left half of star N  → rating = N - 0.5
        // Tap right half of star N → rating = N
        Row(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (starIndex in 1..5) {
                val fraction = when {
                    selectedRating >= starIndex -> 1f
                    selectedRating >= starIndex - 0.5f -> 0.5f
                    else -> 0f
                }
                HalfStar(
                    fillFraction = fraction,
                    enabled = !ratingLocked,
                    onTap = { halfValue ->
                        // halfValue is 0.5 (left tap) or 1.0 (right tap)
                        selectedRating = (starIndex - 1) + halfValue
                    }
                )
            }
        }

        Text(
            text = "Rating: ${"%.1f".format(selectedRating)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        OutlinedTextField(
            value = feedbackText,
            onValueChange = { input ->
                if (input.length <= 200) feedbackText = input
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Share your experience (Optional)") },
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            enabled = !ratingLocked,
            supportingText = {
                Text(
                    text = "${feedbackText.length}/200",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PantaloonsTeal,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                cursorColor = PantaloonsTeal,
                focusedLabelColor = PantaloonsTeal
            )
        )

        Button(
            onClick = {
                onSubmit(selectedRating, feedbackText)
            },
            enabled = !ratingLocked && selectedRating > 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PantaloonsTeal,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFB7E6E5),
                disabledContentColor = Color.White
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (hasAlreadyRated) "You already rated this product" else "Submit Rating",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

