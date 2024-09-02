package com.example.sangeet.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Moods

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPreview(){
    Box(modifier = Modifier.background(brush = Brush.verticalGradient(listOf(Color(0xFF154360), Color.Black), startY = 0.1f, endY = 500f))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
            SearchArea()
            Spacer(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
            )
            MoodsPreview()
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(10.dp))
            SongsPreview()
        }
    }
}

@Composable
fun SongsPreview() {
    val newAmsterdam = FontFamily(
        Font(R.font.newamsterdam, FontWeight.Normal)
    )
    Text(text = "Quick Picks", modifier = Modifier
        .wrapContentWidth()
        .padding(start = 20.dp), fontSize = 40.sp, color = Color(0xFFFFf0f3f4), fontFamily = newAmsterdam)
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun MoodsPreview() {
    var moodList = remember {
        mutableStateOf<List<Moods>>(emptyList())
    }
    val newAmsterdam = FontFamily(
        Font(R.font.newamsterdam, FontWeight.Normal)
    )
    LaunchedEffect(Unit) {
        moodList.value = listOf(
            Moods("Sad", R.drawable.sad),
            Moods("Travel", R.drawable.travel),
            Moods("Classical", R.drawable.classical),
            Moods("Romance", R.drawable.romance),
            Moods("Party", R.drawable.party),
            Moods("Feel good", R.drawable.feel_good),
            Moods("Workout", R.drawable.workout),
            Moods("Rock", R.drawable.rock)
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "What's on", modifier = Modifier.padding(start = 20.dp), fontSize = 40.sp, fontWeight = FontWeight.SemiBold, fontFamily = newAmsterdam, color = Color(0xFFFFf0f3f4) )
            Text(text = "your", modifier = Modifier.padding(start = 20.dp), fontSize = 40.sp, fontWeight = FontWeight.SemiBold, fontFamily = newAmsterdam, color = Color(0xFFFFf0f3f4) )
        }
        Text(text = "Mind?", modifier = Modifier.padding(start = 5.dp), fontSize = 80.sp, fontWeight = FontWeight.SemiBold, fontFamily = newAmsterdam, color = Color(0xFFFFf0f3f4) )
    }

    LazyRow {
        items(moodList.value.size) {
            Column(verticalArrangement = Arrangement.Center) {
                Card(
                    modifier = Modifier
                        .height(280.dp)
                        .width(220.dp)
                        .padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = moodList.value[it].coverArt),
                            contentDescription = null,
                            contentScale = ContentScale.Crop, alpha = 0.83f

                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .alpha(1f),
                            text = moodList.value[it].label,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W600, color = Color(0xFFFFf0f3f4),
                            fontSize = 20.sp

                        )
                    }
                }

            }
        }
    }
}
