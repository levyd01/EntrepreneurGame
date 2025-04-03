package com.example.entrepreneurgame

import android.app.Activity
import android.media.SoundPool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue



// Front end
@Composable
fun ShopPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.click, 1)
    }
    var hasUnlimitedTurns by remember { mutableStateOf(false) }

    val billingManager = remember {
        BillingManager(context) { purchased ->
            hasUnlimitedTurns = purchased
        }
    }

    LaunchedEffect(Unit) {
        billingManager.startConnection()
    }

    Column (
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp) // Apply uniform padding to all sides
            .background(CartoonLightBackground)
            .fillMaxSize()
    )
    {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.shop),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(), // Make the row span the full width of the screen
                    horizontalArrangement = Arrangement.SpaceEvenly, // Adjust the spacing between buttons
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.unlimited_turns),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasUnlimitedTurns){
                        Text("✅")
                    } else {
                        MyAnimatedButton(
                            label = R.string.purchase,
                            onClick = {
                                if (settingsViewModel.isSoundEnabled) {
                                    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                                }
                                billingManager.launchPurchaseFlow(context as Activity)
                            }
                        )
                    }
                }
            }
        }
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyAnimatedButton(
                label = R.string.back,
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    navController.navigate("Init")
                }
            )
        }
    }
}