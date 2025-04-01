package com.example.entrepreneurgame

import android.media.SoundPool
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonBlue
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary

data class SocialCard (
    val contentId : Int,
    val cost : Int,
    val creditor : Boolean //1: bank, 0: other player
)

val socialCardsList = listOf(
    SocialCard(R.string.social_1, 6, true),
    SocialCard(R.string.social_2, 3, true),
    SocialCard(R.string.social_3, 2, true),
    SocialCard(R.string.social_4, 8, true),
    SocialCard(R.string.social_5, 1, true),
    SocialCard(R.string.social_6, 3, false),
    SocialCard(R.string.social_7, 1, true),
    SocialCard(R.string.social_8, 5, true),
    SocialCard(R.string.social_9, 4, true),
    SocialCard(R.string.social_10, 7, true),
)

var pickedSocialCard = socialCardsList[0]

fun socialCost(player : Player) : Int {
    val cardsInPack = socialCardsList.size
    pickedSocialCard = socialCardsList[(0..<cardsInPack).random()]
    player.spendCash(pickedSocialCard.cost)
    val previousPlayer : Int
    if (playerTurn == 0){
        previousPlayer = playersList.size - 1
    } else {
        previousPlayer = playerTurn - 1
    }
    // If card says to give money to friend, then add money to the other player
    if ((!pickedSocialCard.creditor) and (playersList.size > 1)){
        playersList[previousPlayer].gainCash(pickedSocialCard.cost)
    }
    return pickedSocialCard.cost
}
//Front end
/*
@Composable
fun SocialPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
    val cash = currentPlayer.getCash()
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.money, 1)
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
        MyHeading(R.string.social_activity)
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val availableCash by remember { mutableStateOf(cash) }
                Text(text = stringResource(pickedSocialCard.contentId))
                Text(text = stringResource(R.string.cost, pickedSocialCard.cost))
                Spacer(modifier = Modifier.width(16.dp))
                if (availableCash < 0) {
                    Text(text = stringResource(R.string.not_enough_cash))
                } else {
                    Text(text = stringResource(R.string.available_cash, availableCash))
                }
            }
        }
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyCenteredButton(
                R.string.next,
                {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    checkEnoughCash(navController)
                }
            )
        }
    }
}
*/
@Composable
fun SocialPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
    val cash = currentPlayer.getCash()
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.money, 1)
    }

    Column (
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .background(CartoonLightBackground)
            .fillMaxSize()
    ) {
        MyHeading(R.string.social_activity)

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
                val availableCash by remember { mutableStateOf(cash) }
                Text(
                    text = stringResource(pickedSocialCard.contentId),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(48.dp)) // Manual spacer
                Text(
                    text = stringResource(R.string.cost, pickedSocialCard.cost),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (availableCash < 0) {
                    Text(
                        text = stringResource(R.string.not_enough_cash),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.available_cash, availableCash),
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
                    checkEnoughCash(navController)
                }
            )
        }
    }
}
