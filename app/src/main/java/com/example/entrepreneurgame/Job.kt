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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonBlue
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground

data class JobCard (
    val contentId : Int,
    val salary : Int
)

val jobCardsList = listOf(
    JobCard(R.string.job_1, 8),
    JobCard(R.string.job_2, 5),
    JobCard(R.string.job_3, 6),
    JobCard(R.string.job_4, 10),
    JobCard(R.string.job_5, 7),
    JobCard(R.string.job_6, 9),
    JobCard(R.string.job_7, 4),
    JobCard(R.string.job_8, 6),
    JobCard(R.string.job_9, 8),
    JobCard(R.string.job_10, 12),
)

var pickedJobCard = jobCardsList[0]//JobCard(R.string.job_1, 6)

fun jobIncome (player : Player) : Int {
    val cardsInPack = jobCardsList.size
    pickedJobCard = jobCardsList[(0..<cardsInPack).random()]
    player.gainCash(pickedJobCard.salary)
    return pickedJobCard.salary
}

@Composable
fun JobPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
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
            soundPool.load(context, R.raw.money, 1)
        }
        MyHeading(R.string.job_activity)
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
                    text = stringResource(pickedJobCard.contentId),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(48.dp)) // Manual spacer
                Text(
                    text = stringResource(R.string.salary, pickedJobCard.salary),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.available_cash, currentPlayer.getCash()),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
