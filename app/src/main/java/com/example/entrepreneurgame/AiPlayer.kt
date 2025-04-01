package com.example.entrepreneurgame

import android.media.SoundPool
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.entrepreneurgame.ui.theme.CartoonLightBackground
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary

// Back end

enum class EntrepreneurState {
    INACTIVE,
    STARTED,
    BUSY,
    END
}

enum class ActivityType {
    ELIMINATED,
    NONE,
    SOCIAL,
    BANK,
    MARKET,
    JOB,
    ENTREPRENEUR
}

data class AiActivity (
    var cash : Int = 0,
    var shortDeposit : Int = 0,
    var longDeposit : Int = 0,
    var shares : Int = 0,
    var activityType : ActivityType = ActivityType.NONE,
    var entrepreneurState : EntrepreneurState = EntrepreneurState.INACTIVE
)

var currentAiActivity = AiActivity(
    0,
    0,
    0,
    0,
    ActivityType.NONE,
    EntrepreneurState.INACTIVE)

fun aiEndTurn(
    navController: NavController,
    showAiMoves : Boolean
){
    hasAnyDepositEnded()
    if (currentPlayer.isActiveEntrepreneur()) {
        currentPlayer.decrementDuration()
    }
    nextPlayerOrNextDay(navController, showAiMoves)
}

fun aiBank(){
    currentAiActivity.entrepreneurState = EntrepreneurState.INACTIVE
    val todayDate = gameGlobal.getDay()
    val selectLongTerm = ((gameLength - todayDate) > 12)
    val quantity : Int = maxOf((currentPlayer.getCash() - AI_CASH_RESERVE) / SUM_INIT, 0)
    if (selectLongTerm) {
        currentAiActivity.longDeposit = quantity
        makeDeposit(shortDeposits = 0, longDeposits = quantity)
    } else {
        currentAiActivity.shortDeposit = quantity
        makeDeposit(shortDeposits = quantity, longDeposits = 0)
    }
}

fun aiStockMarket(){
    currentAiActivity.entrepreneurState = EntrepreneurState.INACTIVE
    val quantity = maxOf(
        (currentPlayer.getCash() - AI_CASH_RESERVE) / gameGlobal.getStockMarketValue(),
        0
    )
    currentPlayer.buyShares(quantity)
    currentAiActivity.shares = quantity
}

fun aiEntrepreneurEnd(){
    currentAiActivity.entrepreneurState = EntrepreneurState.END
    val enterpriseLuck = (1..6).random()
    val luckEarning  = when (enterpriseLuck) {
        1, 2 -> ENT_BAD_LUCK
        3, 4 -> 0
        else -> ENT_VERY_LUCKY
    }
    val medalsEarning = currentPlayer.getMedals() * EARNING_PER_MEDAL
    val totalEarning = maxOf(currentPlayer.getInitialEarning() + luckEarning + medalsEarning, 0)
    currentPlayer.gainCash(totalEarning)
    val isAdditionalMedal = currentPlayer.addMedal()
    currentPlayer.terminateActivity()
    currentAiActivity.cash = totalEarning
}

fun aiSellForCash(){
    var numberOfShares = currentPlayer.getNumberOfShares()
    val marketValue = gameGlobal.getStockMarketValue()
    // Start by withdrawing deposits that may end today
    hasAnyDepositEnded()
    var cash = currentPlayer.getCash()
    if (cash < 0){
        // Continue by selling stocks if there is gain
        if (numberOfShares > 0){
            if (currentPlayer.getAverageShareCost() > marketValue){
                val quantityToSell : Int = minOf( numberOfShares,-cash / marketValue + 1 )
                currentPlayer.sellShares(quantityToSell)
                currentAiActivity.shares = -quantityToSell
            }
        }
    }
    cash = currentPlayer.getCash()
    var numberOfDeposits = currentPlayer.getNumberOfDeposits()
    var indicesDeposits = mutableListOf<Int>() // Deposits to remove
    var depositsValue = 0
    if (cash < 0) {
        // Continue by withdrawing short deposits
        for (i in 0..<numberOfDeposits) {
            if (!currentPlayer.hasDepositEnded(i) and !currentPlayer.getDepositType(i)) {
                depositsValue += currentPlayer.getDepositCurrentValue(i)
                indicesDeposits.add(i)
                if (depositsValue >= cash) break
            }
        }
        closePositions(0, indicesDeposits)
        currentAiActivity.shortDeposit = -indicesDeposits.size
    }
    cash = currentPlayer.getCash()
    numberOfDeposits = currentPlayer.getNumberOfDeposits()
    indicesDeposits = mutableListOf<Int>() // Deposits to remove
    if (cash < 0) {
        // Finish by withdrawing long deposits
        for (i in 0..<numberOfDeposits) {
            if (!currentPlayer.hasDepositEnded(i) and currentPlayer.getDepositType(i)) {
                depositsValue += currentPlayer.getDepositCurrentValue(i)
                indicesDeposits.add(i)
                if (depositsValue >= cash) break
            }
        }
        closePositions(0, indicesDeposits)
        currentAiActivity.longDeposit = -indicesDeposits.size
    }
    if (cash < 0){
        // Continue by selling stocks even at loss
        numberOfShares = currentPlayer.getNumberOfShares()
        if (numberOfShares > 0){
            val quantityToSell : Int = minOf( numberOfShares,-cash / marketValue + 1 )
            currentPlayer.sellShares(quantityToSell)
            currentAiActivity.shares = -quantityToSell
        }
    }
    if (cash < 0){
        // If still not enough cash, then leave game
        currentAiActivity.activityType = ActivityType.ELIMINATED
    }
}


fun aiRollsDice() {
    val select = (1..6).random()
    when (select) {
        in 1..2 -> {
            currentAiActivity.activityType = ActivityType.SOCIAL
            currentAiActivity.cash = -socialCost(currentPlayer)
            if (currentPlayer.getCash() < 0) {
                aiSellForCash()
            }
        }
        3 -> {
            currentAiActivity.activityType = ActivityType.BANK
            aiBank()
        }
        4 -> {
            currentAiActivity.activityType = ActivityType.MARKET
            aiStockMarket()
        }
        5 -> {
            currentAiActivity.activityType = ActivityType.JOB
            currentAiActivity.cash = jobIncome(currentPlayer)
        }
        else -> {
            entrepreneurStart(currentPlayer)
            currentAiActivity.activityType = ActivityType.ENTREPRENEUR
            currentAiActivity.entrepreneurState = EntrepreneurState.STARTED
        }
    }
}

fun aiPlays() {
    val entrepreneurState = activityState(currentPlayer)
    currentAiActivity = AiActivity( // Used only to display AI actions in current turn
        cash = 0,
        shortDeposit = 0,
        longDeposit = 0,
        shares = 0,
        activityType = ActivityType.NONE,
        entrepreneurState = EntrepreneurState.INACTIVE
    )
    when (entrepreneurState) {
        0 -> {
            currentAiActivity.entrepreneurState = EntrepreneurState.INACTIVE
            aiRollsDice()
        }
        1 -> { currentAiActivity.entrepreneurState = EntrepreneurState.BUSY} // skip turn
        else -> aiEntrepreneurEnd()
    }
}

// Front end
@Composable
fun AiEndTurnPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier

) {
    val today by remember { mutableStateOf(gameGlobal.getDay()) }
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val cash by remember { mutableStateOf(currentPlayer.getCash()) }
    val assets by remember { mutableStateOf(currentPlayer.getTotalResources()) }
    val numberShares by remember { mutableStateOf(currentPlayer.getNumberOfShares()) }
    val numberDeposits by remember { mutableStateOf(currentPlayer.getNumberOfDeposits()) }
    val playerMedals by remember {mutableStateOf(currentPlayer.getAllMedals())}
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.money, 1)
    }
    val soundId2 = remember {
        soundPool.load(context, R.raw.next, 1)
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
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.end_of_day_for,
                    today,
                    playerName
                ),
                style = MaterialTheme.typography.headlineMedium
            )
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

                Text(text = stringResource(R.string.available_cash, cash))
                if (numberDeposits > 0) {
                    Text(
                        text = stringResource(
                            R.string.number_of_bank_deposits,
                            numberDeposits
                        )
                    )
                }
                if (numberShares > 0) {
                    Text(
                        text = stringResource(
                            R.string.number_of_shares_owned,
                            numberShares
                        )
                    )
                }
                for (i in entrepreneurCardsList.indices) {
                    if (playerMedals[i] == 1) {
                        Text(
                            text = stringResource(
                                R.string.you_have_one_medal_for_entrepreneur_activity,
                                i
                            )
                        )
                    } else if (playerMedals[i] > 1) {
                        Text(
                            text = stringResource(
                                R.string.you_have_medals_for_entrepreneur_activity,
                                playerMedals[i],
                                i
                            )
                        )
                    }
                }
                Text(
                    text = stringResource(
                        R.string.total_assets,
                        assets
                    )
                )
            }
        }
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), // Make the row span the full width of the screen
                horizontalArrangement = Arrangement.SpaceEvenly // Adjust the spacing between buttons
            ) {
                Button(
                    onClick = {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                        aiEndTurn(navController, settingsViewModel.showAiTurns)
                    },
                    modifier = Modifier.weight(
                        1f,
                        fill = false
                    ), // Equal width for both buttons,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CartoonRed,
                        contentColor = CartoonTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp), // Rounded corners
                ) {
                    Text(text = stringResource(R.string.end_turn))
                }
            }
        }
    }
}


@Composable
fun AiTurnPage (
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.next, 1)
    }
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val entrepreneurState by remember { mutableStateOf(currentAiActivity.entrepreneurState) }
    val activityType by remember { mutableStateOf(currentAiActivity.activityType) }
    val cash by remember { mutableStateOf(currentAiActivity.cash) }
    val longDeposits by remember { mutableStateOf(currentAiActivity.longDeposit)}
    val shortDeposits by remember { mutableStateOf(currentAiActivity.shortDeposit)}
    val shares by remember { mutableStateOf(currentAiActivity.shares)}

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
                    text = playerName,
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
                when (entrepreneurState) {
                    EntrepreneurState.INACTIVE -> {
                        when (activityType) {
                            ActivityType.ELIMINATED ->
                                Text(text = "Eliminated due to insufficient resources to cover expenses.")
                            ActivityType.SOCIAL -> { // Social
                                Text(text = stringResource(R.string.social_activity_cost, -cash))
                                if (-longDeposits > 1){
                                    Text(text = "${-longDeposits} long deposits were sold to pay for the activity.")
                                } else if (-longDeposits > 0){
                                    Text(text = "One long deposit was sold to pay for the activity.")
                                }
                                if (-shortDeposits > 1){
                                    Text(text = "${-shortDeposits} short deposits were sold to pay for the activity.")
                                } else if (-shortDeposits > 0){
                                    Text(text = "One short deposit was sold to pay for the activity.")
                                }
                                if (-shares > 1){
                                    Text(text = "${-shares} shares were sold to pay for the activity.")
                                } else if (-shares > 0){
                                    Text(text = "One share was sold to pay for the activity.")
                                }
                            }
                            ActivityType.BANK -> { // Bank
                                Text(text = "Bank")
                                if (longDeposits + shortDeposits == 0){
                                    Text(text = "Passed its turn.")
                                } else {
                                    if (longDeposits > 1) {
                                        Text(text = "$longDeposits long deposits were made.")
                                    } else if (longDeposits > 0) {
                                        Text(text = "One long deposit was made.")
                                    }
                                    if (shortDeposits > 1) {
                                        Text(text = "$shortDeposits short deposits were made.")
                                    } else if (shortDeposits > 0) {
                                        Text(text = "One short deposit was made.")
                                    }
                                }
                            }
                            ActivityType.MARKET -> { // Market
                                Text(text = "Stock Market")
                                if (shares == 0){
                                    Text(text = "Passed its turn.")
                                } else {
                                    if (shares > 1) {
                                        Text(text = "$shares shares were bought.")
                                    } else if (shares > 0) {
                                        Text(text = "One share was bought.")
                                    }
                                }
                            }
                            ActivityType.JOB -> { // Job
                                Text(text = stringResource(R.string.job_earned, cash))
                            }
                            else -> { // Entrepreneur
                                Text(text = "Entrepreneur activity started.")
                            }
                        }
                    }
                    EntrepreneurState.STARTED -> Text(text = "Entrepreneur activity started.")
                    EntrepreneurState.BUSY -> 
                        Text(text = stringResource(R.string.currently_busy_in_an_entrepreneur_activity))
                    else -> { // End of Entrepreneur activity
                        Text(text = stringResource(R.string.your_entrepreneurial_activity_ended_today))
                        Text(text = stringResource(R.string.you_earned, cash))
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
            MyCenteredButton(
                R.string.next,
                {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    if (activityType == ActivityType.ELIMINATED){
                        eliminatePlayer(navController)
                    } else {
                        navController.navigate("AiEndTurn")
                    }
                }
            )
        }
    }
}