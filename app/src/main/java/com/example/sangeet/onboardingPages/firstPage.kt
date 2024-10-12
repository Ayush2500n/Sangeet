package com.example.sangeet.onboardingPages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.R
import com.example.sangeet.dataClasses.SignInState
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPage(
    state: SignInState,
    onSignInClick: () -> Unit,
    onContinueGuestClick: () -> Unit,
    navController: NavController,
    currentUser: FirebaseUser?
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF154360), Color.Black),
                startY = 0.1f,
                endY = 500f
            )
        ),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

        Image(imageVector = ImageVector.vectorResource(id = R.drawable.sangeet_high_resolution_logo_white_transparent), contentDescription = null, modifier = Modifier.padding(20.dp))
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = {
            Log.d("GoogleSignIn", "Sign up with Google button clicked")
            onSignInClick()
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp, end = 40.dp), colors = ButtonDefaults.buttonColors(Color.White), border = BorderStroke(4.dp, color = Color.LightGray)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp), horizontalArrangement = Arrangement.Center) {
                Image(painter = painterResource(id = R.drawable.icons8_google_48), contentDescription = null, modifier = Modifier
                    .padding(end = 8.dp)
                    .size(20.dp))
                Text(text = "Sign up with Google", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {  }, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp, end = 40.dp), colors = ButtonDefaults.buttonColors(Color.DarkGray), border = BorderStroke(2.dp, color = Color.LightGray)
        ) {
            Text(text = "Sign up with OTP", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp, bottom = 5.dp))
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "or", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = {
            if (currentUser == null){
                onContinueGuestClick()
            }
            else{
                Toast.makeText(context, "You are already logged in", Toast.LENGTH_SHORT).show()
                navController.navigate("HomeScreen")
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp, end = 40.dp), colors = ButtonDefaults.buttonColors(Color.Transparent)
        ) {
            Text(text = "Continue as guest", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}