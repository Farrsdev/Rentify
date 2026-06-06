package com.example.rentify.ui.screen

import androidx.compose.foundation.background
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onNavigateToConfirmRents: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val searchQuery by productViewModel.searchQuery.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("APP") } // APP or GAME
    var pricePerDay by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rentify Admin", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onNavigateToConfirmRents) {
                        BadgedBox(badge = { }) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Konfirmasi Peminjaman", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { authViewModel.logout(onLogout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2D))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    title = ""
                    category = "APP"
                    pricePerDay = ""
                    description = ""
                    showDialog = true
                },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { productViewModel.setSearchQuery(it) },
                placeholder = { Text("Cari Aplikasi / Game...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada produk. Tambahkan sekarang!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252538)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(product.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(product.description, fontSize = 12.sp, color = Color.LightGray, maxLines = 2)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "${numberFormat.format(product.pricePerDay)} / Hari",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingProduct = product
                                            title = product.title
                                            category = product.category
                                            pricePerDay = product.pricePerDay.toInt().toString()
                                            description = product.description
                                            showDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Ubah", tint = Color.Yellow)
                                    }
                                    IconButton(onClick = { productViewModel.deleteProduct(product) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Add / Edit
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingProduct == null) "Tambah Produk" else "Edit Produk",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            containerColor = Color(0xFF252538),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Aplikasi/Game", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple Radio-like for Category Selection
                    Column {
                        Text("Kategori:", fontSize = 12.sp, color = Color.Gray)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = category == "APP",
                                    onClick = { category = "APP" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6366F1))
                                )
                                Text("Aplikasi", color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = category == "GAME",
                                    onClick = { category = "GAME" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6366F1))
                                )
                                Text("Game", color = Color.White)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pricePerDay,
                        onValueChange = { pricePerDay = it },
                        label = { Text("Harga Sewa / Hari (Rp)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = pricePerDay.toDoubleOrNull() ?: 0.0
                        if (editingProduct == null) {
                            productViewModel.addProduct(title, category, price, description) {
                                showDialog = false
                            }
                        } else {
                            productViewModel.updateProduct(editingProduct!!.id, title, category, price, description) {
                                showDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

