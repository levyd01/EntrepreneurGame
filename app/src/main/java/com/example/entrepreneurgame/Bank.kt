package com.example.entrepreneurgame

import android.media.SoundPool
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.entrepreneurgame.ui.theme.CartoonBlue
import com.example.entrepreneurgame.ui.theme.CartoonGreen
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary

// back end

class BankDeposit(
    longTerm : Boolean,
    startDay : Int
){
    private val sumInit : Int = SUM_INIT
    private val lengthFinal : Int = if (longTerm) {LONG_TERM_LENGTH} else {5}
    val sumFinal : Int = if (longTerm) {LONG_TERM_SUM_FINAL} else {SHORT_TERM_SUM_FINAL}
    private val lengthEarly : Int = if (longTerm) {
        LONG_TERM_LENGTH_EARLY
    } else {
        SHORT_TERM_LENGTH_EARLY
    }
    private val sumEarly : Int = if (longTerm) {LONG_TERM_SUM_EARLY} else {sumInit}
    private val lengthFull = if (longTerm) {LONG_TERM_LENGTH_FULL} else {SHORT_TERM_LENGTH_FULL}
    private val releaseDay : Int = if (longTerm) {startDay + lengthFull} else {startDay + lengthFull}

    fun daysRemaining (today : Int) : Int {
        return releaseDay - today
    }

    fun getType () : Boolean {
        return lengthFinal == LONG_TERM_LENGTH
    }

    fun currentValue() : Int {
        val daysLeft = daysRemaining (gameGlobal.getDay())
        if (daysLeft == 0) return sumFinal
        if (daysLeft < lengthEarly){
            return sumInit
        } else {
            return sumEarly
        }
    }
}

fun makeDeposit(
    shortDeposits : Int,
    longDeposits: Int
){
    val todayDate = gameGlobal.getDay()
    for (i in 1..shortDeposits){
        currentPlayer.makeDeposit(BankDeposit(false, startDay = todayDate))
    }
    for (i in 1..longDeposits){
        currentPlayer.makeDeposit(BankDeposit(true, startDay = todayDate))
    }
    currentPlayer.spendCash((shortDeposits+longDeposits)*SUM_INIT)
}

fun hasAnyDepositEnded() : Boolean{
    val nbrDeposits = currentPlayer.getNumberOfDeposits()
    var depositEnded = false
    val elementsToRemove = mutableListOf<BankDeposit>()
    for (i in 0..<nbrDeposits){
        if (currentPlayer.hasDepositEnded(i)){
            if (currentPlayer.getDepositType(i)){
                currentPlayer.gainCash(LONG_TERM_SUM_FINAL)
            } else {
                currentPlayer.gainCash(SHORT_TERM_SUM_FINAL)
            }
            elementsToRemove.add(currentPlayer.deposits[i])
            depositEnded = true
        }
    }
    if (depositEnded){
        currentPlayer.deposits.removeAll(elementsToRemove)

    }
    return depositEnded
}

// front end

@Composable
fun EndDepositPage (
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
            soundPool.load(context, R.raw.money, 1)
        }
        MyHeading(R.string.at_the_bank)
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
                    text = stringResource(R.string.end_deposit_verbose),
                    style = MaterialTheme.typography.bodyLarge,
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
                    navController.navigate("EndTurn")
                }
            )
        }
    }
}

@Composable
fun BankPage (
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
    ) {
        var shortDepositCount by remember { mutableStateOf(0) }
        var longDepositCount by remember { mutableStateOf(0) }
        val cash by remember { mutableStateOf(currentPlayer.getCash()) }
        val context = LocalContext.current
        val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
        val soundId = remember {
            soundPool.load(context, R.raw.money, 1)
        }
        val soundId2 = remember {
            soundPool.load(context, R.raw.next, 1)
        }
        val soundId3 = remember {
            soundPool.load(context, R.raw.click, 1)
        }

        MyHeading(R.string.at_the_bank)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.available_cash, cash),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Image(
                            painter = painterResource(R.drawable.short_deposit),
                            contentDescription = "Short Deposit Card",
                            modifier = Modifier
                                .size(300.dp) // Set the size of the image

                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "-" Button
                            IconButton(
                                onClick = {
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                                    }
                                    if (shortDepositCount > 0) {
                                        shortDepositCount--
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(40.dp),
                                    tint = CartoonRed
                                )
                            }

                            // Center Text displaying the number
                            Text(
                                text = shortDepositCount.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // "+" Button
                            IconButton(
                                onClick = {
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                                    }
                                    if (SUM_INIT * (shortDepositCount + longDepositCount) + SUM_INIT <= currentPlayer.getCash()) {
                                        shortDepositCount++
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(40.dp),
                                    tint = CartoonGreen
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Image(
                            painter = painterResource(R.drawable.long_deposit),
                            contentDescription = "Long Deposit Card",
                            modifier = Modifier
                                .size(300.dp) // Set the size of the image
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "-" Button
                            IconButton(
                                onClick = {
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                                    }
                                    if (longDepositCount > 0) {
                                        longDepositCount--
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(40.dp),
                                    tint = CartoonRed
                                )
                            }

                            // Center Text displaying the number
                            Text(
                                text = longDepositCount.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // "+" Button
                            IconButton(
                                onClick = {
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                                    }
                                    if (SUM_INIT * (shortDepositCount + longDepositCount) + SUM_INIT <= currentPlayer.getCash()) {
                                        longDepositCount++
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(40.dp),
                                    tint = CartoonGreen
                                )
                            }
                        }
                    }
                }
            }
        }
        Row (
            horizontalArrangement = Arrangement.SpaceEvenly, // Evenly space the buttons
            verticalAlignment = Alignment.CenterVertically,  // Align buttons vertically
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyAnimatedButton(
                label = R.string.invest,
                onClick = {
                    if (shortDepositCount + longDepositCount > 0){
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        }
                        makeDeposit(
                            shortDeposits = shortDepositCount,
                            longDeposits = longDepositCount
                        )
                    } else {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                    }
                    navController.navigate("EndTurn")
                }
            )
            MyAnimatedButton(
                label = R.string.pass,
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                    }
                    endTurn(navController)
                }
            )
        }
    }
}

