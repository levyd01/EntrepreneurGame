package com.example.entrepreneurgame

import android.media.SoundPool
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.entrepreneurgame.ui.theme.CartoonBlue
import com.example.entrepreneurgame.ui.theme.CartoonTextSecondary


/*
@Composable
fun RollDicePage(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var result by remember { mutableStateOf(1) }
    val initButtonString = stringResource(R.string.roll_dice)
    val nextButtonString = stringResource(R.string.next)
    var buttonText by remember { mutableStateOf(initButtonString) }
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val today by remember { mutableStateOf(gameGlobal.getDay()) }
    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.next, 1)
    }
    val soundId2 = remember {
        soundPool.load(context, R.raw.dice, 1)
    }

    // Animation state for the dice roll
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope() // Required for launching animations

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp) // Apply uniform padding to all sides
            .background(CartoonLightBackground)
            .fillMaxSize()
    ) {
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
                    text = stringResource(R.string.day, today),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.roll_dice_to_select_the_daily_activity))

                // Dice image with scaling animation
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = result.toString(),
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            rotationZ = rotation.value) // Apply scaling animation
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Button(
                onClick = {
                    if (buttonText == initButtonString) {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                        result = (1..6).random()
                        coroutineScope.launch {
                            scale.animateTo(1.2f, animationSpec = tween(150))
                            rotation.animateTo(360f, animationSpec = tween(300))
                            scale.animateTo(1f, animationSpec = tween(150))
                            rotation.snapTo(0f) // Reset rotation
                        }
                        buttonText = nextButtonString
                    } else {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        }

                        when (result) {
                            in 1..2 -> {
                                socialCost(currentPlayer)
                                navController.navigate("Social")
                            }
                            3 -> navController.navigate("Bank")
                            4 -> navController.navigate("StockMarket")
                            5 -> {
                                jobIncome(currentPlayer)
                                navController.navigate("Job")
                            }
                            6 -> {
                                entrepreneurStart(currentPlayer)
                                navController.navigate("EntrepreneurStart")
                            }
                        }
                    }
                },
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CartoonRed,
                    contentColor = CartoonTextPrimary
                ),
                shape = RoundedCornerShape(8.dp), // Rounded corners
            ) {
                Text(text = buttonText)
            }
        }
    }
}
*/

@Composable
fun RollDicePage(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var result by remember { mutableStateOf(1) }
    val buttonInitialText = stringResource(R.string.roll_dice)
    val buttonNextText = stringResource(R.string.next)
    var buttonText by remember { mutableStateOf(buttonInitialText) }
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val today by remember { mutableStateOf(gameGlobal.getDay()) }
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundRoll = remember { soundPool.load(context, R.raw.dice, 1) }
    val soundNext = remember { soundPool.load(context, R.raw.next, 1) }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(CartoonLightBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.day, today),
                style = MaterialTheme.typography.headlineLarge,
                color = CartoonTextPrimary
            )
            Text(
                text = playerName,
                style = MaterialTheme.typography.headlineMedium,
                color = CartoonTextSecondary
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CartoonBlue, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.roll_dice_to_select_the_daily_activity),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = result.toString(),
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            rotationZ = rotation.value
                        )
                )
            }
        }

        MyAnimatedButton(
            label = if (buttonText == buttonInitialText) R.string.roll_dice else R.string.next,
            onClick = {
                if (buttonText == buttonInitialText) {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundRoll, 1f, 1f, 1, 0, 1f)
                    }
                    result = (1..6).random()
                    coroutineScope.launch {
                        scale.animateTo(1.2f, animationSpec = tween(150))
                        rotation.animateTo(360f, animationSpec = tween(300))
                        scale.animateTo(1f, animationSpec = tween(150))
                        rotation.snapTo(0f)
                    }
                    buttonText = buttonNextText
                } else {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundNext, 1f, 1f, 1, 0, 1f)
                    }
                    when (result) {
                        in 1..2 -> {
                            socialCost(currentPlayer)
                            navController.navigate("Social")
                        }
                        3 -> navController.navigate("Bank")
                        4 -> navController.navigate("StockMarket")
                        5 -> {
                            jobIncome(currentPlayer)
                            navController.navigate("Job")
                        }
                        6 -> {
                            entrepreneurStart(currentPlayer)
                            navController.navigate("EntrepreneurStart")
                        }
                    }
                }
            }
        )
    }
}