package com.example.sangeet.onboardingPages

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Artists
import com.example.sangeet.modifierExtension.animatedBorder
import com.example.sangeet.modifierExtension.shimmerEffect
import com.example.sangeet.viewModels.UserSharedViewModel
import com.example.sangeet.viewModels.onboardingViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.collections.immutable.toPersistentList
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondPage(
    onboardingViewModel: onboardingViewModel,
    navController: NavHostController,
    currentUser: FirebaseUser?,
    userSharedViewModel: UserSharedViewModel
) {
    var artistCounter by remember {
        mutableStateOf(false)
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF154360), Color.Black),
                startY = 0.1f,
                endY = 500f
            )
        )
    ){
        val newAmsterdam = FontFamily(
            Font(R.font.newamsterdam, FontWeight.Normal)
        )

        var searchText by remember {
            mutableStateOf("")
        }

        Spacer(modifier = Modifier.height(20.dp))
        if (currentUser != null) {
            Text(text = "Welcome ${currentUser.displayName?.capitalize(Locale.ROOT)?.substringBefore(" ")}!", fontFamily = newAmsterdam, color = Color.White, fontSize = 40.sp, modifier = Modifier.padding(20.dp))
        }
        Text(text = if (!artistCounter)"Pick your favourite artists" else "You may proceed further...", fontFamily = newAmsterdam, color = Color.White, fontSize = 35.sp, modifier = Modifier
            .padding(20.dp)
            .wrapContentHeight())
        Spacer(modifier = Modifier.height(20.dp))
        SearchBar(query = searchText, onQueryChange = {
                                                  searchText = it}, onSearch = {}, active = false, onActiveChange = {  }, placeholder = { Text(
            text = "Search"
        )}, modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp), leadingIcon = { Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null
        )}, colors = SearchBarDefaults.colors(containerColor = Color.Transparent, inputFieldColors = TextFieldDefaults.textFieldColors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray))) {

        }
        Spacer(modifier = Modifier.height(10.dp))
        ArtistsList(onboardingViewModel, searchText, navController, userSharedViewModel, counter = {
            artistCounter = it
        })
    }
}

@Composable
fun ArtistsList(
    onboardingViewModel: onboardingViewModel,
    searchText: String,
    navController: NavHostController,
    userSharedViewModel: UserSharedViewModel,
    counter: (Boolean) -> Unit
) {
    val isLoading by onboardingViewModel.isLoading.observeAsState(initial = true)
    val listOfArtists by onboardingViewModel.artistsList.observeAsState(initial = emptyList())
    val checkedStates = remember { mutableStateListOf<Boolean>() }
    val checkedArtist = remember { mutableStateListOf<Artists>() }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        onboardingViewModel.getArtists()
    }
    var favIndex by remember {
        mutableIntStateOf(1)
    }
    val filteredArtists = if (searchText.isBlank()) {
        listOfArtists
    } else {
        listOfArtists.filter { artist ->
            artist.artistName.contains(searchText, ignoreCase = true)
        }
    }

    LaunchedEffect(filteredArtists) {
        if (checkedStates.size != filteredArtists.size) {
            checkedStates.clear()
            checkedStates.addAll(List(filteredArtists.size) { false })
        }
    }

    val newAmsterdam = FontFamily(Font(R.font.newamsterdam, FontWeight.Normal))
    Log.d("Artists in composable", "$filteredArtists")

    Box {
        LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Adaptive(200.dp)) {
            items(filteredArtists.size) { index ->
                val artist = filteredArtists.getOrNull(index)
                if (artist != null && index < checkedStates.size) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val animatedPadding by animateDpAsState(
                            targetValue =
                            if (checkedStates.size != 0) {
                                if (checkedStates[index] || checkedArtist.contains(artist)) 20.dp else 10.dp
                            } else 10.dp,
                            animationSpec = tween(durationMillis = 300)
                        )
                        Log.d("isLoading", isLoading.toString())
                        AsyncImage(
                            model = artist.photo,
                            contentDescription = null,
                            modifier = Modifier
                                .then(
                                    if (isLoading) Modifier.shimmerEffect() else Modifier
                                )
                                .padding(
                                    animatedPadding
                                )
                                .then(
                                    if (checkedStates[index] || checkedArtist.contains(artist)) {
                                        Modifier.animatedBorder(
                                            listOf(
                                                Color(0xff2471a3),
                                                Color(0xff2874a6),
                                                Color(0xffa9cce3),
                                                Color(0xffaed6f1),
                                                Color(0xff2e4053),
                                                Color(0xff273746),
                                                Color(0xff3498db),
                                                Color(0xff5499c7),
                                                Color(0xff1a5276),
                                                Color(0xff21618c),
                                                Color(0xff1f618d),
                                                Color(0xff2874a6)
                                            ),
                                            Color.Transparent,
                                            borderWidth = 2.5.dp, shape = RoundedCornerShape(20.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    if (!checkedStates[index] && favIndex == 5) {
                                        counter(true)
                                    }else{
                                        counter(false)
                                        checkedStates[index] = !checkedStates[index]
                                        Log.d("CheckedArtist", "${checkedArtist.toList()}")
                                        if (checkedStates[index]) {
                                            if (!checkedArtist.contains(artist)) {
                                                checkedArtist.add(artist)
                                                favIndex++
                                            }
                                        } else {
                                            if (checkedArtist.contains(artist)) {
                                                checkedArtist.remove(artist)
                                                favIndex--
                                            }
                                        }
                                    }
                                }
                                .clip(shape = RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = artist.artistName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = newAmsterdam,
                            modifier = Modifier.then(if (listOfArtists.isEmpty()) Modifier.shimmerEffect() else Modifier)
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
        if (favIndex >= 5) {
            FloatingActionButton(onClick = { navController.navigate("HomeScreen")
                userSharedViewModel.setPreferences(checkedArtist, context)}, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp), containerColor = Color(0xFF154360), contentColor = Color.White) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
        FloatingActionButton(onClick = {navController.navigate("HomeScreen") }, modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(20.dp)
            .border(2.dp, color = Color.White, shape = RoundedCornerShape(20.dp)), containerColor = Color.DarkGray, contentColor = Color.White) {
            Text(text = "Skip", modifier = Modifier.padding(start = 30.dp, end = 30.dp))
        }
    }
}
