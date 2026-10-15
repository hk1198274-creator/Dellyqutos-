package com.example.dellyqutos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

// --- DATA MODEL ---
data class Quote(val id: Int, val text: String, val category: String)

val allQuotes = listOf(
    Quote(1, "The only way to do great work is to love what you do.\n\nमहान कार्य करने का एकमात्र तरीका यह है कि आप जो करते हैं उससे प्यार करें।", "Motivation"),
    Quote(2, "Success is not final, failure is not fatal: it is the courage to continue that counts.\n\nसफलता अंतिम नहीं है, विफलता घातक नहीं है: यह जारी रखने का साहस है जो मायने रखता है।", "Success"),
    Quote(3, "Friendship is born at that moment when one person says to another, 'What! You too?'\n\nदोस्ती उस पल पैदा होती है जब एक व्यक्ति दूसरे से कहता है, 'क्या! तुम भी?'", "Friendship"),
    Quote(4, "Life is what happens when you're busy making other plans.\n\nजीवन वह है जो तब होता है जब आप अन्य योजनाएं बनाने में व्यस्त होते हैं।", "Life"),
    Quote(5, "Love is composed of a single soul inhabiting two bodies.\n\nप्रेम एक ही आत्मा से बना है जो दो शरीरों में निवास करती है।", "Love"),
    Quote(6, "Believe you can and you're halfway there.\n\nविश्वास करें कि आप कर सकते हैं और आपने आधी मंजिल तय कर ली है।", "Motivation"),
    Quote(7, "The secret of success is to do the common thing uncommonly well.\n\nसफलता का रहस्य सामान्य चीजों को असामान्य रूप से अच्छी तरह से करना है।", "Success")
)

// --- VIEWMODEL ---
class QuotesViewModel : ViewModel() {
    var selectedCategory by mutableStateOf("Motivation")
    var favoriteIds by mutableStateOf(setOf<Int>())

    fun loadFavorites(context: Context) {
        val prefs = context.getSharedPreferences("delly_prefs", Context.MODE_PRIVATE)
        val savedFavs = prefs.getStringSet("favs", emptySet()) ?: emptySet()
        favoriteIds = savedFavs.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFavorite(id: Int, context: Context) {
        val prefs = context.getSharedPreferences("delly_prefs", Context.MODE_PRIVATE)
        val newFavs = favoriteIds.toMutableSet()
        if (newFavs.contains(id)) newFavs.remove(id) else newFavs.add(id)
        favoriteIds = newFavs
        prefs.edit().putStringSet("favs", newFavs.map { it.toString() }.toSet()).apply()
    }
}

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {} // Initialize AdMob
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DellyQutosApp()
                }
            }
        }
    }
}

// --- COMPOSE UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DellyQutosApp(viewModel: QuotesViewModel = viewModel()) {
    val context = LocalContext.current
    val categories = listOf("Motivation", "Love", "Life", "Success", "Friendship", "Random", "Favorites")

    // Load favorites from local storage on startup
    LaunchedEffect(Unit) { viewModel.loadFavorites(context) }

    val displayedQuotes = when (viewModel.selectedCategory) {
        "Random" -> allQuotes.shuffled()
        "Favorites" -> allQuotes.filter { it.id in viewModel.favoriteIds }
        else -> allQuotes.filter { it.category == viewModel.selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delly qutos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = { AdBanner() }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // Category Tabs
            LazyRow(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = viewModel.selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
                            .clickable { viewModel.selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quotes List
            if (displayedQuotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quotes found here yet!", fontSize = 18.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    items(displayedQuotes) { quote ->
                        QuoteCard(
                            quote = quote,
                            isFavorite = viewModel.favoriteIds.contains(quote.id),
                            onFavoriteClick = { viewModel.toggleFavorite(quote.id, context) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteCard(quote: Quote, isFavorite: Boolean, onFavoriteClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = quote.text,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Copy Button
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Quote", quote.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Quote Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                }
                
                // Share Button
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${quote.text}\n\n~ Shared via Delly qutos")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                }

                // Favorite Button
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

// --- ADMOB BANNER ---
@Composable
fun AdBanner() {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // NOTE: This is a Test Ad Unit ID. 
                // Replace with your real AdMob Banner ID before uploading to Play Store.
                adUnitId = "ca-app-pub-7470240417028203/5058649230" 
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
