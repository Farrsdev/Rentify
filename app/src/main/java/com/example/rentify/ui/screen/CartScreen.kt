package com.example.rentify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.ui.viewmodel.RentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    rentViewModel: RentViewModel,
    onBack: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by rentViewModel.cartItems.collectAsStateWithLifecycle()

    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    val totalPayment = cartItems.sumOf { it.productPricePerDay * it.rentDays }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Sewa", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2D))
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = Color(0xFF1E1E2D),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Peminjaman", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = numberFormat.format(totalPayment),
                                color = Color(0xFF818CF8),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                rentViewModel.checkout {
                                    onCheckoutSuccess()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Ajukan Peminjaman", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0F0F1A),
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Keranjang kamu kosong.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cartItems, key = { it.cartId }) { item ->
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
                                        color = if (item.productCategory == "APP") Color(0xFF3B82F6) else Color(0xFF10B981),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = item.productCategory,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(item.productTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        "${numberFormat.format(item.productPricePerDay)} / Hari",
                                        fontSize = 13.sp,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        "Subtotal: ${numberFormat.format(item.productPricePerDay * item.rentDays)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.rentDays > 1) {
                                                rentViewModel.updateCartItemDays(item.cartId, item.rentDays - 1)
                                            } else {
                                                rentViewModel.removeFromCart(item.cartId)
                                            }
                                        },
                                        modifier = Modifier.background(Color(0xFF1E1E2D), shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = if (item.rentDays > 1) Icons.Default.Remove else Icons.Default.Delete,
                                            contentDescription = "Kurang",
                                            tint = if (item.rentDays > 1) Color.White else Color.Red
                                        )
                                    }

                                    Text(
                                        text = "${item.rentDays} Hari",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = { rentViewModel.updateCartItemDays(item.cartId, item.rentDays + 1) },
                                        modifier = Modifier.background(Color(0xFF1E1E2D), shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

