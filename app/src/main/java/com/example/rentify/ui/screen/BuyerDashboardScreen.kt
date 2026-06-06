package com.example.rentify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.entity.ProductEntity
import com.example.rentify.ui.viewmodel.AuthViewModel
import com.example.rentify.ui.viewmodel.ProductViewModel
import com.example.rentify.ui.viewmodel.RentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerDashboardScreen(
    productViewModel: ProductViewModel,
    rentViewModel: RentViewModel,
    authViewModel: AuthViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val searchQuery by productViewModel.searchQuery.collectAsStateWithLifecycle()
    val cartItems by rentViewModel.cartItems.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("SEMUA") } // SEMUA, APP, GAME
    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredProducts = remember(products, selectedFilter) {
        when (selectedFilter) {
            "APLIKASI" -> products.filter { it.category == "APP" }
            "GAME" -> products.filter { it.category == "GAME" }
            else -> products
        }
    }

    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rentify Store", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Riwayat Sewa", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(containerColor = Color.Red, contentColor = Color.White) {
                                        Text(cartItems.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { authViewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2D))
            )
        },
        containerColor = Color(0xFF0F0F1A),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { productViewModel.setSearchQuery(it) },
                placeholder = { Text("Cari Game atau Aplikasi...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                listOf("SEMUA", "APLIKASI", "GAME").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Button(
                        onClick = { selectedFilter = filter },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF6366F1) else Color(0xFF252538),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(filter, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Produk tidak ditemukan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252538)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProductForDetail = product }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = if (product.category == "APP") Color(0xFF3B82F6) else Color(0xFF10B981),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = product.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = product.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = product.description,
                                        fontSize = 12.sp,
                                        color = Color.LightGray,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${numberFormat.format(product.pricePerDay)} / Hari",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                }

                                IconButton(
                                    onClick = { rentViewModel.addToCart(product.id, product.title) },
                                    modifier = Modifier
                                        .background(Color(0xFF6366F1), shape = RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Sewa", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog Modal
    selectedProductForDetail?.let { product ->
        AlertDialog(
            onDismissRequest = { selectedProductForDetail = null },
            title = {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            containerColor = Color(0xFF252538),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (product.category == "APP") Color(0xFF3B82F6) else Color(0xFF10B981),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = product.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )

                    Divider(color = Color.DarkGray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Harga Sewa", color = Color.Gray, fontSize = 13.sp)
                        Text(
                            text = "${numberFormat.format(product.pricePerDay)} / Hari",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        rentViewModel.addToCart(product.id, product.title)
                        selectedProductForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Sewa Sekarang", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForDetail = null }) {
                    Text("Kembali", color = Color.Gray)
                }
            }
        )
    }
}

