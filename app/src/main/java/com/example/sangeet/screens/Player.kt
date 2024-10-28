package com.example.sangeet.screens


import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.sangeet.R
import com.example.sangeet.exoplayer.MusicService
import com.example.sangeet.viewModels.PlayerSharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@Composable
fun Player(playerSharedViewModel: PlayerSharedViewModel, service: MusicService) {
    val index by  playerSharedViewModel.currentSongIndex.observeAsState()
    val currentSong by playerSharedViewModel.currentSong.observeAsState()
    playerSharedViewModel.observeDurationUpdates(service)
    val isPlaying by playerSharedViewModel.playStatus.observeAsState()
    val currentDuration by playerSharedViewModel._currentDuration.collectAsState()
    val maxDuration by remember {
        mutableStateOf(playerSharedViewModel.maxDuration)
    }
    Log.d("Player", "durations collected are $currentDuration and $maxDuration")
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(currentSong?.let { playerSharedViewModel.extractCover(it) })
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    )
    fun formatDuration(durationMs: Float): String {
        val totalSeconds = (durationMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    val formattedCurrentDuration = remember(currentDuration) {
        currentDuration?.let { formatDuration(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp)
                .background(color = Color.Black)
                .alpha(0.5f),
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
                            .clip(shape = RoundedCornerShape(20.dp))
                            .background(color = Color.Black)
                            .alpha(0.9f),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Display the song name
                    index?.let {
                        currentSong?.name?.let {
                            Text(
                                text = it,
                                fontSize = 20.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
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

                    index?.let {
                        currentSong?.artists?.let {
                            Text(
                                text = it,
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Display the current duration
                        if (formattedCurrentDuration != null) {
                            Text(
                                text = formattedCurrentDuration,
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                        }

                        // Slider for seeking within the track
                        var sliderValue by remember { mutableStateOf(currentDuration) }
                        LaunchedEffect(currentDuration) {
                            sliderValue = currentDuration
                        }

                        LaunchedEffect(currentDuration) {
                            sliderValue = currentDuration
                        }
                        // Slider UI
                        sliderValue?.let {
                            Slider(
                                value = it,
                                onValueChange = { newValue ->
                                    sliderValue = newValue
                                },
                                onValueChangeFinished = {
                                    // Seek to new position when user releases the slider
                                    service.seekTo(sliderValue!!.toInt())
                                    if (isPlaying == true) {
                                        service.play()
                                    }
                                },
                                valueRange = 0f..(maxDuration.value ?: 0f), // Ensures a non-null range
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Display the max duration
                        maxDuration.value?.let { formatDuration(it) }?.toString()?.let {
                            Text(
                                text = it,
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                        }
                    }


                    // Playback controls
                    Row( verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = {
                            playerSharedViewModel.previousSong()
                            index?.dec()
                            service?.prev()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.previous),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        IconButton(onClick = {
                            isPlaying?.not()
                            service?.play()
                        }) {
                            Icon(
                                painter = painterResource(id = if (isPlaying == true) R.drawable.pause else R.drawable.play),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        IconButton(onClick = {
                            playerSharedViewModel.nextSong()
                            index?.inc()
                            service.next()
                            Log.d("Player", "Next button clicked, now the cover is ${currentSong?.coverUrl}")
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