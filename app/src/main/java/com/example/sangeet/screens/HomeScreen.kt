package com.example.sangeet.screens

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.viewModels.viewModel
import com.google.api.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun HomeScreen(navController: NavHostController, viewModelRef: viewModel = hiltViewModel()) {
    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF154360), Color.Black),
                startY = 0.1f,
                endY = 500f
            )
        )
    ) {
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

            // Scrollable content starts here
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Moods(viewModelRef, navController)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
                SongPreview(viewModelRef)
            }
        }
    }
}

@Composable
fun SongPreview(viewModelRef: viewModel) {
    var isNotLoading by remember { mutableStateOf(false) }
    val songs = remember {
        mutableStateListOf<Song>()
    }
    val observerdSongs by viewModelRef.fetchSongs().observeAsState(initial = emptyList())

    LaunchedEffect(key1 = observerdSongs) {
        if (observerdSongs.isNotEmpty()){
            songs.clear()
            songs.addAll(observerdSongs)
            isNotLoading = true
        }
        else{
            songs.addAll(observerdSongs)
            isNotLoading = true
        }
    }

    if (observerdSongs.isNotEmpty()){
        Log.d("Observer", "$observerdSongs and $songs")
    }else{
        Log.d("Observer", "Empty")
    }
    val newAmsterdam = FontFamily(
        Font(R.font.newamsterdam, FontWeight.Normal)
    )

    Text(
        text = "Quick Picks",
        modifier = Modifier
            .wrapContentWidth()
            .padding(start = 20.dp),
        fontSize = 36.sp,
        color = Color(0xFFFFf0f3f4),
        fontFamily = newAmsterdam
    )

    Spacer(modifier = Modifier.height(10.dp))

    Box(modifier = Modifier.height(300.dp)) { // Set an appropriate height for your grid
        SongsRow(songs, isNotLoading)
    }
}

@Composable
fun SongsRow(songs: List<Song>, isNotLoading: Boolean) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        modifier = Modifier.wrapContentHeight() // Let it wrap based on its content
    ) {
        items(songs) {
            SongCard(song = it, isNotLoading)
        }
    }
}



@Composable
fun SongCard(song: Song, isNotLoading: Boolean) {
    val metadataRetriever = MediaMetadataRetriever()
    Log.d("Extraction", song.audioUrl)
    var metadata by remember {
        mutableStateOf(song)
    }
    LaunchedEffect(key1 = metadata.audioUrl) {
        withContext(Dispatchers.IO){
            Log.d("Metadata extraction", song.audioUrl)
            try {
                metadataRetriever.setDataSource(song.audioUrl)
                val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                val cover = metadataRetriever.embeddedPicture
                metadata = song.copy(
                    name = title,
                    album = album,
                    coverUrl = cover
                )
            } catch (e: Exception) {
                e.message?.let { Log.d("Extraction", it) }
            } finally {
                metadataRetriever.release()
            }
        }
    }

    Row(modifier = Modifier.padding(4.dp), verticalAlignment =
    Alignment.CenterVertically) {
        Spacer(
            modifier = Modifier
                .wrapContentWidth()
                .width(20.dp)
        )
        if (isNotLoading) {
            Card(
                modifier = Modifier
                    .size(56.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (metadata.coverUrl != null) {
                    AsyncImage(
                        model = metadata.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Log.d(
                        "Lazyrow img",
                        "Image found for song: ${metadata.name}, ${metadata.coverUrl}"
                    )
                } else {
                    Log.d("Lazyrow img", "Image is null for song: ${song.name}")
                }
            }
            Column {
                Text(
                    text = metadata.name!!,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .width(160.dp),
                    color = Color.White
                )
                metadata.album?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .width(160.dp),
                        color = Color.LightGray
                    )
                }
            }
        } else {
            // Display shimmer effect directly, no nested LazyHorizontalGrid
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shimmerEffect()
            )
            Column {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .shimmerEffect()
                        .width(100.dp)
                        .height(16.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .shimmerEffect()
                        .width(50.dp)
                        .height(16.dp),
                )
            }
        }
    }
}



@Composable
fun Moods(viewModelRef: viewModel, navController: NavController) {
    Log.d("Moods", "Moods called")
    var moodList = remember {
        mutableStateOf<List<Moods>>(emptyList())
    }
    var isNotLoading by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        moodList.value = viewModelRef.getMoods()
        isNotLoading = true
    }
    val newAmsterdam = FontFamily(
        Font(R.font.newamsterdam, FontWeight.Normal)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "What's on", modifier = Modifier.padding(start = 20.dp), fontSize = 40.sp, fontWeight = FontWeight.SemiBold, fontFamily = newAmsterdam, color = Color(0xFFFFf0f3f4) )
            Text(text = "your", modifier = Modifier.padding(start = 20.dp), fontSize = 40.sp, fontWeight = FontWeight.SemiBold, fontFamily = newAmsterdam, color = Color(0xFFFFf0f3f4) )
        }
        Text(text = "Mind?", modifier = Modifier.padding(start = 5.dp), fontSize = 80.sp, fontWeight = FontWeight.SemiBold, fontFamily = newAmsterdam, color = Color(0xFFFFf0f3f4) )
    }

    LazyRow {
        if (isNotLoading) {
            items(moodList.value.size) {
                val currentCard = moodList.value[it]
                Column(verticalArrangement = Arrangement.Center) {
                    Card(
                        modifier = Modifier
                            .height(280.dp)
                            .width(220.dp)
                            .padding(20.dp)
                            .clickable {
                                val mood = Moods(
                                    label = currentCard.label,
                                    coverArt = currentCard.coverArt
                                )
                                Log.d("MoodNav", mood.label)
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "mood",
                                    mood
                                )
                                Log.d(
                                    "MoodNav",
                                    "${
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "mood",
                                            mood
                                        )
                                    }"
                                )
                                navController.navigate("MoodsSelect")
                            },
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
        else{
            items(6) {
                Box(modifier = Modifier
                    .height(280.dp)
                    .width(220.dp)
                    .padding(20.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .shimmerEffect())
            }
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed{
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2* size.width.toFloat(),
        targetValue = 2* size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )
    background(
        brush = Brush.linearGradient(
            listOf(Color(0xFF1b4f72),
                Color(0xFF3498db),
                Color(0xFF1b4f72)
            ), start = Offset(x = startOffsetX, y = 0.0f),
            end = Offset(x = startOffsetX + size.width.toFloat(), y = size.height.toFloat()))
        ).onGloballyPositioned {
        size = it.size
    }
}
@Composable
fun SearchArea() {
    Log.d("Search Area", "Search Area called")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            Card(
                modifier = Modifier
                    .size(62.dp)
                    .padding(12.dp)
                    .align(Alignment.CenterVertically)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFf0f3f4)),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.sangeet_high_resolution_logo_white_transparent),
                modifier = Modifier
                    .height(80.dp)
                    .wrapContentWidth()
                    .padding(start = 12.dp),
                contentDescription = null
            )
        }
        Card(
            modifier = Modifier
                .size(62.dp)
                .padding(12.dp)
                .align(Alignment.CenterVertically)
                .clickable { },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

        }
    }
}
