package com.example.sangeet.screens

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.exoplayer.MusicService
import com.example.sangeet.modifierExtension.shimmerEffect
import com.example.sangeet.viewModels.PlayerSharedViewModel
import com.example.sangeet.viewModels.UserSharedViewModel
import com.example.sangeet.viewModels.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList



@Composable
fun HomeScreen(navController: NavHostController, viewModelRef: viewModel = hiltViewModel(), userSharedViewModel: UserSharedViewModel, playerSharedViewModel: PlayerSharedViewModel, connection: ServiceConnection) {
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
                SongPreview(viewModelRef, userSharedViewModel, navController, playerSharedViewModel, connection)
            }
        }
    }
}

@Composable
fun SongPreview(
    viewModelRef: viewModel,
    userSharedViewModel: UserSharedViewModel,
    navController: NavHostController,
    playerSharedViewModel: PlayerSharedViewModel,
    connection: ServiceConnection
) {
    var isNotLoading by remember { mutableStateOf(false) }
    val songs = remember {
        mutableStateListOf<Song>()
    }
    val observedSongs by viewModelRef.songsList.observeAsState(initial = emptyList())
    val userPref by userSharedViewModel.userData.observeAsState()

    LaunchedEffect(key1 = userPref, key2 = observedSongs) {
        Log.d("SongPreview", "User preferences: ${userPref?.preferences}")
        if (userPref?.preferences?.isNotEmpty() == true) {
            Log.d("SongPreview", "Fetching songs by preferences: ${userPref?.preferences}")
            viewModelRef.fetchSongsByUserPref(userPref?.preferences ?: emptyList())
        } else {
            Log.d("SongPreview", "Fetching all songs")
            viewModelRef.fetchSongs()
        }
    }



    LaunchedEffect(key1 = observedSongs) {
        if (observedSongs.isNotEmpty()){
            songs.clear()
            songs.addAll(observedSongs)
            isNotLoading = true
        }
        else{
            songs.addAll(observedSongs)
            isNotLoading = true
        }
    }

    if (observedSongs.isNotEmpty()){
        Log.d("Observer", "$observedSongs and $songs")
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
        SongsRow(songs, isNotLoading, navController, playerSharedViewModel, connection)
    }
}

@Composable
fun SongsRow(
    songs: List<Song>,
    isNotLoading: Boolean,
    navController: NavHostController,
    playerSharedViewModel: PlayerSharedViewModel,
    connection: ServiceConnection
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        modifier = Modifier.wrapContentHeight()
    ) {
        itemsIndexed(songs) { index, song ->
            SongCard(
                song = song,
                isNotLoading = isNotLoading,
                navController = navController,
                playerSharedViewModel = playerSharedViewModel,
                connection = connection,
                songs = songs,
                index = index  // Pass index here
            )
        }
    }
}



@Composable
fun SongCard(
    song: Song,
    isNotLoading: Boolean,
    navController: NavHostController,
    playerSharedViewModel: PlayerSharedViewModel,
    connection: ServiceConnection,
    songs: List<Song>,
    index: Int
) {
    val metadataRetriever = MediaMetadataRetriever()
    song.audioUrl?.let { Log.d("Extraction", it) }
    var metadata by remember {
        mutableStateOf(song)
    }
    var bitmapCover by remember {
        mutableStateOf<Bitmap?>(null)
    }
    var songDuration by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    LaunchedEffect(key1 = metadata.audioUrl) {
        withContext(Dispatchers.IO){
            song.audioUrl?.let { Log.d("Metadata extraction", it) }
            try {
                metadataRetriever.setDataSource(song.audioUrl)
                val cover = metadataRetriever.embeddedPicture
                if (cover != null) {
                    // Convert ByteArray to Bitmap
                    bitmapCover = BitmapFactory.decodeByteArray(cover, 0, cover.size)
                }
                songDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                metadata = song.copy(
                    name = song.name,
                    album = song.album,
                    coverUrl = cover,  // This is still ByteArray, keep it for any other processing
                    genre = song.genre,
                    subgenre = song.subgenre,
                    artists = song.artists,
                    duration = songDuration
                )
            } catch (e: Exception) {
                e.message?.let { Log.d("Extraction", it) }
            } finally {
                metadataRetriever.release()
            }
        }
    }

    val painter = rememberAsyncImagePainter(bitmapCover)
    Row(modifier = Modifier
        .padding(4.dp)
        .clickable {
            val intent = Intent(navController.context, MusicService::class.java).apply {
                Log.d("Player", "Sending in information for intent $songs, $metadata")
                playerSharedViewModel.setPlayingStatus(true)
                playerSharedViewModel.sendSong(metadata)
                playerSharedViewModel.sendSongList(songs)
                playerSharedViewModel.getCurrentIndex(index)
            }
            context.startService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            navController.navigate("Player")
        }, verticalAlignment =
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
                    Image(
                        painter = painter,
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
