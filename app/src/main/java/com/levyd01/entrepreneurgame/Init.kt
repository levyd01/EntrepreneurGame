package com.levyd01.entrepreneurgame

import android.media.SoundPool
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.levyd01.entrepreneurgame.ui.theme.CartoonBlue
import com.levyd01.entrepreneurgame.ui.theme.CartoonLightBackground
import com.levyd01.entrepreneurgame.ui.theme.CartoonRed
import kotlinx.coroutines.launch

// Back end
fun createPlayers (
    humanPlayers : Int,
    aiPlayers : Int
){
    for (i in 0..< humanPlayers){
        playersList.add(
            Player(
                name ="Player ${i+1}",
                human = true
            )
        )
    }
    for (i in 0..< aiPlayers){
        playersList.add(
            Player(
                name ="AI Player ${i+1}",
                human = false
            )
        )
    }
    currentPlayer = playersList[0]
}

fun nextPlayerName() : Boolean {
    playerTurn++
    if (playerTurn == playersList.size){
        playerTurn = 0
        currentPlayer = playersList[0]
        return false
    }
    currentPlayer = playersList[playerTurn]
    if (currentPlayer.isPlayerHuman()) {
        return true
    } else {
        playerTurn = 0
        currentPlayer = playersList[0]
        return false
    }
}


// Front end

@Composable
fun PlayerNamePage(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var playerNameInput by remember { mutableStateOf("") }
    val playerId by remember { mutableStateOf(playerTurn + 1) }
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember { soundPool.load(context, R.raw.next, 1) }

    // Animated logo properties
    val logoAlpha = remember { Animatable(0f) }
    val logoOffsetY = remember { Animatable(-100f) }

    // Run animations when the page is composed
    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, animationSpec = tween(1000)) }
        launch { logoOffsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)) }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(CartoonLightBackground)
    ) {
        // Logo with animation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .graphicsLayer(alpha = logoAlpha.value, translationY = logoOffsetY.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth() // Ensures it takes the full width
                    .height(200.dp), // Adjust this based on your previous size
                contentScale = ContentScale.Fit // Prevents unnecessary shrinking
            )
        }

        // Name Input
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.player_enter_your_name, playerId),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = playerNameInput,
                    onValueChange = { playerNameInput = it },
                    label = { Text(stringResource(R.string.your_name)) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = CartoonLightBackground,
                        focusedIndicatorColor = CartoonRed
                    )
                )
            }
        }

        // Animated Next Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyAnimatedButton(
                label = R.string.play,
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    currentPlayer.name = if (playerNameInput.isNotEmpty()) playerNameInput else "Player $playerId"

                    if (nextPlayerName()) {
                        navController.navigate("PlayerName")
                    } else {
                        navController.navigate("RollDice")
                    }
                }
            )
        }
    }
}

@Composable
fun InitPage(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var humanPlayers by remember { mutableStateOf(gameGlobal.getHumanPlayers()) }
    var aiPlayers by remember { mutableStateOf(gameGlobal.getAiPlayers()) }
    var gameLength by remember { mutableStateOf(gameGlobal.getGameLength()) }
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember { soundPool.load(context, R.raw.next, 1) }
    val soundId1 = remember { soundPool.load(context, R.raw.click, 1) }
    var maxDaysReachedPopUp by remember { mutableStateOf(false) }
    var hasUnlimitedTurns by remember { mutableStateOf(false) }

    val billingManager = remember {
        BillingManager(context) { purchased ->
            hasUnlimitedTurns = purchased
        }
    }

    LaunchedEffect(Unit) {
        billingManager.startConnection()
    }

    // Logo Animation (Fade-in and bounce effect)
    val logoAlpha = remember { Animatable(0f) }
    val logoOffsetY = remember { Animatable(-50f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(1000))
        logoOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CartoonLightBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Animated Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxWidth() // Ensures it takes the full width
                .height(200.dp), // Adjust this based on your previous size
            contentScale = ContentScale.Fit // Prevents unnecessary shrinking
        )

        // Game basic Selections
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CartoonBlue),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerSelector("👤 Human Players", humanPlayers, {
                    if (humanPlayers > 1) {
                        humanPlayers--
                        gameGlobal.setHumanPlayers(humanPlayers)
                    }
                }, {
                    if ((humanPlayers + aiPlayers) + 1 <= MAX_PLAYERS) {
                        humanPlayers++
                        gameGlobal.setHumanPlayers(humanPlayers)
                    }
                })

                Spacer(modifier = Modifier.height(16.dp))

                PlayerSelector("🤖 AI Players", aiPlayers, {
                    if (aiPlayers > 0) {
                        aiPlayers--
                        gameGlobal.setAiPlayers(aiPlayers)
                    }
                }, {
                    if ((humanPlayers + aiPlayers) + 1 <= MAX_PLAYERS) {
                        aiPlayers++
                        gameGlobal.setAiPlayers(aiPlayers)
                    }
                })

                Spacer(modifier = Modifier.height(16.dp))

                PlayerSelector(stringResource(R.string.game_duration), gameLength, {
                    if (gameLength > 1) {
                        gameLength--
                        gameGlobal.setGameLength(gameLength)
                    }
                }, {
                    if (hasUnlimitedTurns){
                        gameLength++
                        gameGlobal.setGameLength(gameLength)
                    } else if (gameLength + 1 <= SHORT_GAME_LENGTH) {
                        gameLength++
                        gameGlobal.setAiPlayers(aiPlayers)
                    } else {
                        maxDaysReachedPopUp = true
                    }
                })
            }
        }

        if (maxDaysReachedPopUp) {
            AlertDialog(
                onDismissRequest = {
                    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    maxDaysReachedPopUp = false },
                title = { Text(stringResource(R.string.max_days_free_version)) },
                text = { Text(stringResource(R.string.to_play_more_days_purchase)) },
                confirmButton = {
                    Button(onClick = {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        maxDaysReachedPopUp = false
                    }) {
                        Text(stringResource(R.string.purchase))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        maxDaysReachedPopUp = false
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Navigation Buttons
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MyAnimatedButton(R.string.settings) {
                if (settingsViewModel.isSoundEnabled) {
                    soundPool.play(soundId1, 1f, 1f, 1, 0, 1f)
                }
                navController.navigate("Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))

            MyAnimatedButton(R.string.shop) {
                if (settingsViewModel.isSoundEnabled) {
                    soundPool.play(soundId1, 1f, 1f, 1, 0, 1f)
                }
                navController.navigate("Shop")
            }

            Spacer(modifier = Modifier.height(16.dp))

            MyAnimatedButton(R.string.next) {
                val totalPlayers = if (settingsViewModel.showAiTurns) {
                    humanPlayers + aiPlayers
                } else {
                    humanPlayers
                }
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                createPlayers(humanPlayers, aiPlayers)
                navController.navigate("PlayerName")
            }
        }
    }
}

