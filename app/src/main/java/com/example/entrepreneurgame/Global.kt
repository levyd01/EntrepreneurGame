package com.example.entrepreneurgame

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.system.exitProcess

// Back end

var playerTurn = 0
var playersList = mutableListOf<Player>()
var currentPlayer = Player("Player 1", true)

class SettingsViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    var enableMusic = false
        private set
    var isSoundEnabled by mutableStateOf(true)
        private set
    var showAiTurns by mutableStateOf(false)
        private set

    fun initializeMediaPlayer(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.music).apply {
                isLooping = true
                if (enableMusic) start()
            }
        } else if (enableMusic) {
            mediaPlayer?.start()
        }
    }

    fun toggleMediaPlayer() {
        enableMusic = !enableMusic
        if (enableMusic) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.pause()
        }
    }

    fun toggleAiTurns(){
        showAiTurns = !showAiTurns
    }

    fun toggleSound(){
        isSoundEnabled = !isSoundEnabled
    }

    fun release() {
        enableMusic = false
        mediaPlayer?.release() // Clean up when ViewModel is destroyed
    }
}

class GlobalVars (
    private var humanPlayers : Int,
    private var aiPlayers: Int,
    private var currentDay : Int,
    private var stockMarketValue  : Int,
    private var gameLength : Int
    ){

    fun reset() {
        humanPlayers  = 1
        aiPlayers = 0
        currentDay = 1
        stockMarketValue = INIT_STOCK_MARKET_VALUE
    }
    fun getStockMarketValue() : Int {
        return stockMarketValue
    }

    fun modifyStockMarketValueBy(change : Int) {
        stockMarketValue += change
        if (stockMarketValue < MIN_STOCK_MARKET_VALUE){
            stockMarketValue = MIN_STOCK_MARKET_VALUE
        }
    }

    fun setHumanPlayers(newHumanPlayers : Int)  {
        humanPlayers = newHumanPlayers
    }

    fun getHumanPlayers() : Int {
        return humanPlayers
    }

    fun setAiPlayers(newAiPlayers : Int)  {
        aiPlayers = newAiPlayers
    }

    fun getAiPlayers() : Int {
        return aiPlayers
    }

    fun endDay() {
        currentDay++
    }

    fun getDay() : Int {
        return currentDay
    }

    fun getRemainingDays() : Int {
        return gameLength - currentDay + 1
    }

    fun setGameLength(newGameLength : Int) {
        gameLength = newGameLength
    }

    fun getGameLength() : Int {
        return gameLength
    }

    fun isLastDay() : Boolean {
        return currentDay == gameLength
    }

}

var gameGlobal : GlobalVars = GlobalVars(
    1,
    0,
    1,
    INIT_STOCK_MARKET_VALUE,
    SHORT_GAME_LENGTH
)

fun resetGame(navController: NavController){
    gameGlobal.reset()
    playerTurn = 0
    playersList = mutableListOf<Player>()
    currentPlayer = Player("Player 1", true)
    navController.navigate("Init")
}

// Front end


@Composable
fun SettingsPage(
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.click, 1)
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
                    text = stringResource(R.string.settings),
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
                    Text(stringResource(R.string.music), modifier = Modifier.weight(1f))
                    LaunchedEffect(Unit) {
                        settingsViewModel.initializeMediaPlayer(context)
                    }
                    Switch(
                        checked = settingsViewModel.enableMusic,
                        onCheckedChange = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                            }
                            settingsViewModel.toggleMediaPlayer()
                            navController.navigate("Settings")
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(), // Make the row span the full width of the screen
                    horizontalArrangement = Arrangement.SpaceEvenly, // Adjust the spacing between buttons
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.sound_effects), modifier = Modifier.weight(1f))
                    Switch(
                        checked = settingsViewModel.isSoundEnabled,
                        onCheckedChange = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                            }
                            settingsViewModel.toggleSound()
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(), // Make the row span the full width of the screen
                    horizontalArrangement = Arrangement.SpaceEvenly, // Adjust the spacing between buttons
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.show_ai_turns), modifier = Modifier.weight(1f))
                    Switch(
                        checked = settingsViewModel.showAiTurns,
                        onCheckedChange = {
                            if (settingsViewModel.showAiTurns) {
                                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                            }
                            settingsViewModel.toggleAiTurns()
                        }
                    )
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
                label = R.string.done,
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


@Composable
fun EndGamePage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
    val sortedList by remember { mutableStateOf(playersList.sortedBy { it.getTotalResources() }) }
    val playersListSize by remember { mutableStateOf(playersList.size)}
    val player0Name by remember {mutableStateOf(playersList[0].name)}
    val player0Resources by remember {mutableStateOf(playersList[0].getTotalResources())}
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.next, 1)
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
                    text = stringResource(R.string.this_was_the_last_day),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
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
                if (playersListSize == 1){
                    Text(
                        text = stringResource(
                            R.string.is_the_only_player_to_reach_the_end,
                            player0Name
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.has_a_total_asset_of,
                            player0Name,
                            player0Resources
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    if (sortedList.isNotEmpty()) {
                        for (i in sortedList.indices) {
                            Text(
                                text = stringResource(
                                    R.string.has_a_total_asset_of,
                                    playersList[i].name,
                                    playersList[i].getTotalResources()
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.is_the_winner, playersList[0].name),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasUnlimitedTurns) {
                    Text(
                        text = stringResource(R.string.you_can_continue_playing_the_game_without_time_limit),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly, // Evenly space the buttons
                    verticalAlignment = Alignment.CenterVertically,  // Align buttons vertically
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    MyAnimatedButton(
                        label = R.string.restart,
                        onClick = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                            }
                            resetGame(navController)
                        }
                    )
                    if (hasUnlimitedTurns) {
                        MyAnimatedButton(
                            label = R.string.continue_play,
                            onClick = {
                                if (settingsViewModel.isSoundEnabled) {
                                    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                                }
                                navController.navigate("EndDay")
                            }
                        )
                    }
                    MyAnimatedButton(
                        label = R.string.exit,
                        onClick = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                            }
                            settingsViewModel.release()
                            exitProcess(0)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun GameOverPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.click, 1)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.game_over),
            style = MaterialTheme.typography.headlineLarge
        )
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // Make the row span the full width of the screen
            horizontalArrangement = Arrangement.SpaceEvenly // Adjust the spacing between buttons
        ) {
            Button(
                onClick = {
                    resetGame(navController)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CartoonRed,
                    contentColor = CartoonTextPrimary
                ),
                shape = RoundedCornerShape(8.dp), // Rounded corners
                modifier = Modifier.weight(1f, fill = false), // Equal width for both buttons,
            ) {
                Text(text = stringResource(R.string.restart))
            }
            Button(
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    exitProcess(0)
                          },
                modifier = Modifier.weight(1f, fill = false), // Equal width for both buttons,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CartoonRed,
                    contentColor = CartoonTextPrimary
                ),
                shape = RoundedCornerShape(8.dp), // Rounded corners
            ) {
                Text(text = stringResource(R.string.exit))
            }
        }
    }

}