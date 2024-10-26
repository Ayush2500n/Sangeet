package com.example.sangeet.screens

import android.content.ServiceConnection
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.viewModels.PlayerSharedViewModel
import com.example.sangeet.viewModels.moodsViewModel


@Composable
fun MoodsSelect(
    viewModelRef: moodsViewModel,
    subgenre: Moods?,
    navController: NavHostController,
    playerSharedViewModel: PlayerSharedViewModel,
    connection: ServiceConnection
) {
    if (subgenre == null) {
        Log.e("MoodsSelect", "Received null subgenre")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mood data is not available")
        }
        return
    }
    val newAmsterdam = FontFamily(
        Font(R.font.newamsterdam, FontWeight.Normal)
    )
    val songs by viewModelRef.getMoodData(subgenre.label).observeAsState(initial = emptyList())
    Log.d("MoodSelect", "Songs fetched in composable $songs")

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 1f)),
        startY = 500f, endY = 900f
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Image(
            painter = painterResource(id = subgenre.coverArt),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .background(brush = gradientBrush)
        )
        Column {
            Spacer(modifier = Modifier.height(300.dp))
            Text(
                text = subgenre.label,
                fontFamily = newAmsterdam,
                fontSize = 40.sp,
                color = Color.White, modifier = Modifier.padding(start = 20.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            SongsRow(
                songs = songs,
                isNotLoading = true,
                navController,
                playerSharedViewModel,
                connection
            )
        }
    }
}
