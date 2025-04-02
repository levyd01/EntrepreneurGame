package com.example.entrepreneurgame

import android.media.SoundPool
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonBlue
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground

// back end

data class EntrepreneurCard (
    val id : Int,
    val contentId : Int,
    val initialEarning : Int,
    val duration : Int
)

val entrepreneurCardsList = listOf(
    EntrepreneurCard(0, R.string.ent_1, 5, 2),
    EntrepreneurCard(1, R.string.ent_2, 4, 2),
    EntrepreneurCard(2, R.string.ent_3, 8, 3),
    EntrepreneurCard(3, R.string.ent_4, 9, 3),
    EntrepreneurCard(4, R.string.ent_5, 10, 3),
)

fun activityState(player: Player) : Int{
    val activity = if (currentPlayer.isActiveEntrepreneur()) {
        if (currentPlayer.getActivityLeftDuration() > 1) {
            1
        } else {
            2
        }
    } else {
        0
    }
    return activity
}
var pickedEntrepreneurCard = entrepreneurCardsList[0]
fun entrepreneurStart(player : Player){
    val cardsInPack = entrepreneurCardsList.size
    pickedEntrepreneurCard = entrepreneurCardsList[(0..<cardsInPack).random()]
    player.startEntrepreneurActivity(pickedEntrepreneurCard.id)
}

var enterpriseLuck : Int = 1
var totalEarning : Int = 0
var isAdditionalMedal : Boolean = true
fun entrepreneurEnd (
    player : Player,
    navController: NavController
) {
    enterpriseLuck = (1..6).random()
    val luckEarning  = when (enterpriseLuck) {
        1, 2 -> ENT_BAD_LUCK
        3, 4 -> 0
        else -> ENT_VERY_LUCKY
    }
    val medalsEarning = player.getMedals() * EARNING_PER_MEDAL
    totalEarning = maxOf(player.getInitialEarning() + luckEarning + medalsEarning, 0)
    player.gainCash(totalEarning)
    isAdditionalMedal = player.addMedal()
    player.terminateActivity()
    navController.navigate("EntrepreneurEnd")
}

fun backgroundPicture (card : EntrepreneurCard) : Int {
    return when (card){
        entrepreneurCardsList[0] -> R.drawable.lemonade_stand
        entrepreneurCardsList[1] -> R.drawable.insectarium
        entrepreneurCardsList[2] -> R.drawable.theater
        entrepreneurCardsList[3] -> R.drawable.novel_writing
        else -> R.drawable.card_game
    }
}

// Front end

@Composable
fun EntrepreneurBusyPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp) // Apply uniform padding to all sides
            .background(CartoonLightBackground)
            .fillMaxSize()
    )
    {
        val context = LocalContext.current
        val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
        val soundId = remember {
            soundPool.load(context, R.raw.next, 1)
        }

        MyHeading(R.string.entrepreneur_activity)
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CartoonBlue, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.currently_busy_in_an_entrepreneur_activity),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Bold
                )
        }
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyAnimatedButton(
                label = R.string.next,
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    endTurn(navController)
                }
            )
        }
    }

}
@Composable
fun EntrepreneurEndPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier

) {
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.money, 1)
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp) // Apply uniform padding to all sides
            .background(CartoonLightBackground)
            .fillMaxSize()
    )
    {
        MyHeading(R.string.entrepreneur_activity)
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CartoonBlue, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.your_entrepreneurial_activity_ended_today),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(48.dp)) // Manual spacer
                when (enterpriseLuck){
                    1, 2 ->
                        Text(
                            text = stringResource(R.string.the_activity_was_not_successful_this_time_maybe_better_luck_next_time),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    3, 4 ->
                        Text(
                            text = stringResource(R.string.the_activity_obtained_medium_success_this_time),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    else ->
                        Text(
                            text = stringResource(R.string.it_was_a_major_success_everybody_was_impressed),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                }
                Spacer(modifier = Modifier.height(48.dp)) // Manual spacer
                Text(
                    text = stringResource(R.string.you_earned, totalEarning),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(48.dp)) // Manual spacer
                if (isAdditionalMedal) {
                    Text(
                        text = stringResource(R.string.you_acquired_more_experience_for_this_activity),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.it_will_help_you_earn_more_next_time_you_perform_the_same_activity),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
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
            MyAnimatedButton(
                label = R.string.next,
                onClick =
                {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    endTurn(navController)
                }
            )
        }
    }
}

@Composable
fun EntrepreneurStartPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
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
        val context = LocalContext.current
        val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
        val soundId = remember {
            soundPool.load(context, R.raw.next, 1)
        }

        MyHeading(R.string.entrepreneur_activity)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Background image
            Image(
                painter = painterResource(id = backgroundPicture(pickedEntrepreneurCard)), // Replace with your image resource
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(pickedEntrepreneurCard.contentId),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(48.dp)) // Add some space between the row and columns
                if (pickedEntrepreneurCard.duration == 2) {
                    Text(
                        text = stringResource(R.string.this_activity_will_end_tomorrow),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.this_activity_will_end_in,
                            pickedEntrepreneurCard.duration
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
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
            MyAnimatedButton(
                label = R.string.next,
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    endTurn(navController)
                }
            )
        }
    }
}