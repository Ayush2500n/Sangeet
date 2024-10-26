package com.example.sangeet.screens


import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.sangeet.R
import com.example.sangeet.exoplayer.MusicService
import com.example.sangeet.viewModels.PlayerSharedViewModel
import kotlinx.coroutines.delay


@Composable
fun Player(playerSharedViewModel: PlayerSharedViewModel, service: MusicService?) {
    val playingSong by remember { derivedStateOf { playerSharedViewModel.currentSongList.toList() } }
    val index = playerSharedViewModel.currentSongIndex
    val isPlaying = playerSharedViewModel.playStatus
    var currentSongCover by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(index.value) {
        currentSongCover = index.value?.let { playingSong.getOrNull(it)?.coverUrl }
        Log.d("Player", "Cover URL at index ${index.value} is $currentSongCover")
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(currentSongCover)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxHeight(0.54f)
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(start = 20.dp, end = 20.dp)
                ) {}

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 60.dp, end = 60.dp, top = 60.dp)
                            .aspectRatio(1f)
                            .clip(shape = RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Display the song name
                    index.value?.let {
                        playingSong.getOrNull(it)?.name?.let { songName ->
                            Text(
                                text = songName,
                                fontSize = 20.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Animated scroll effect for artists text
                    val scrollState = rememberScrollState(0)
                    LaunchedEffect(scrollState.maxValue) {
                        while (true) {
                            scrollState.animateScrollTo(
                                scrollState.maxValue,
                                animationSpec = tween(durationMillis = 8000, easing = LinearEasing)
                            )
                            delay(1000L)
                            scrollState.animateScrollTo(0)
                            delay(1000L)
                        }
                    }

                    index.value?.let {
                        playingSong.getOrNull(it)?.artists?.let { artists ->
                            Text(
                                text = artists,
                                fontSize = 16.sp,
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .padding(start = 60.dp, end = 60.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .horizontalScroll(scrollState),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))

                    // Playback controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 60.dp, end = 60.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = {
                            index.value = (index.value ?: 0) - 1
                            service?.prev()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.previous),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        IconButton(onClick = {
                            isPlaying.value = !isPlaying.value
                            service?.play()
                        }) {
                            Icon(
                                painter = painterResource(id = if (isPlaying.value) R.drawable.pause else R.drawable.play),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        IconButton(onClick = {
                            index.value = (index.value ?: 0) + 1
                            service?.next()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.next),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
