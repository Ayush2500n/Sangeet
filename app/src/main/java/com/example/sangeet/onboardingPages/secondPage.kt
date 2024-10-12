package com.example.sangeet.onboardingPages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sangeet.R
import com.example.sangeet.screens.shimmerEffect
import com.example.sangeet.viewModels.onboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondPage(onboardingViewModel: onboardingViewModel) {
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
        var search by remember {
            mutableStateOf("Search")
        }
        var searchText by remember {
            mutableStateOf("")
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "Welcome ", fontFamily = newAmsterdam, color = Color.White, fontSize = 40.sp, modifier = Modifier.padding(20.dp))
        Text(text = "Pick Favourites!", fontFamily = newAmsterdam, color = Color.White, fontSize = 40.sp, modifier = Modifier.padding(20.dp))
        Spacer(modifier = Modifier.height(30.dp))
        SearchBar(query = search, onQueryChange = {search = it
                                                  searchText = it}, onSearch = {}, active = false, onActiveChange = {  }, modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp), leadingIcon = { Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null
        )}, colors = SearchBarDefaults.colors(containerColor = Color.Transparent, inputFieldColors = TextFieldDefaults.textFieldColors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray))) {

        }
        Spacer(modifier = Modifier.height(30.dp))
        ArtistsList(onboardingViewModel, searchText)
    }
}

@Composable
fun ArtistsList(onboardingViewModel: onboardingViewModel, searchText: String) {
    val listOfArtists by onboardingViewModel.artistsList.observeAsState(initial = emptyList())
    val checkedStates = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(Unit) {
        onboardingViewModel.getArtists()
    }

    val filteredArtists = if (searchText.isBlank()) {
        listOfArtists
    } else {
        listOfArtists.filter { artist ->
            artist.artistName.contains(searchText, ignoreCase = true)
        }
    }

    LaunchedEffect(filteredArtists) {
        checkedStates.clear()
        checkedStates.addAll(List(filteredArtists.size) { false })
    }

    val newAmsterdam = FontFamily(Font(R.font.newamsterdam, FontWeight.Normal))
    Log.d("Artists in composable", "$filteredArtists")

    LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Adaptive(200.dp)) {
        items(filteredArtists.size) { index ->
            val artist = filteredArtists[index]
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = artist.photo,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(if (checkedStates.size != 0){
                            if (checkedStates[index]) 20.dp else 10.dp
                        }else 10.dp)
                        .clickable {
                            checkedStates[index] = !checkedStates[index]
                        }
                        .clip(shape = RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = artist.artistName, color = Color.White, fontSize = 20.sp, fontFamily = newAmsterdam)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
