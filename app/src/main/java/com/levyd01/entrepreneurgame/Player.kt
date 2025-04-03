package com.levyd01.entrepreneurgame

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.levyd01.entrepreneurgame.ui.theme.CartoonBlue
import com.levyd01.entrepreneurgame.ui.theme.CartoonGreen
import com.levyd01.entrepreneurgame.ui.theme.CartoonLightBackground
import com.levyd01.entrepreneurgame.ui.theme.CartoonRed

// backend

class Player (
    var name : String,
    private val human : Boolean
){
    private var cash = INIT_CASH
    var deposits = mutableListOf<BankDeposit>()
    private var shares = 0
    private var leftDuration : Int = 3
    private var initialEarning : Int = 3
    private var activeEntrepreneurId : Int? = null
    private var entrepreneurMedals = MutableList(entrepreneurCardsList.size) { 0 }
    private var averageShareCost : Float = 0.0F

    fun isPlayerHuman() : Boolean{
        return human
    }
    fun isActiveEntrepreneur() : Boolean{
        return (activeEntrepreneurId != null)
    }

    fun startEntrepreneurActivity(id : Int){
        activeEntrepreneurId = id
        leftDuration = entrepreneurCardsList[id].duration
        initialEarning = entrepreneurCardsList[id].initialEarning
    }

    fun terminateActivity(){
        activeEntrepreneurId = null
    }
    fun getInitialEarning() : Int {
        return initialEarning
    }

    fun decrementDuration(){
        leftDuration -= 1
    }
    fun getActivityLeftDuration() : Int {
        return leftDuration
    }

    fun addMedal() : Boolean {
        val id = activeEntrepreneurId ?: 0
        if (activeEntrepreneurId != null) {
            if (entrepreneurMedals[id] < MAX_MEDALS) {
                entrepreneurMedals[id] += 1
                return true
            }
        }
        return false
    }

    fun getMedals() : Int {
        val id = activeEntrepreneurId ?: 0
        if (activeEntrepreneurId != null) {
            return entrepreneurMedals[id]
        }
        else return 0
    }

    fun getAllMedals() : MutableList<Int> {
        return entrepreneurMedals
    }

    fun isHuman() : Boolean {
        return human
    }

    fun getTotalResources() : Int {
        var assets  = cash + shares * gameGlobal.getStockMarketValue()
        for (i in 0..<deposits.size){
            if (deposits[i].getType()){ // Long term
                assets += if (deposits[i].daysRemaining(gameGlobal.getDay()) < LONG_TERM_LENGTH_EARLY){
                    LONG_TERM_SUM_EARLY
                } else {
                    SUM_INIT
                }
            } else { // Short term
                assets += SUM_INIT
            }
        }
        return assets
    }

    fun buyShares (quantity : Int){
        val previousNumberOfShares = shares
        val currentShareValue = gameGlobal.getStockMarketValue()
        shares += quantity
        cash -= quantity * currentShareValue
        averageShareCost =
            (averageShareCost * previousNumberOfShares + quantity * currentShareValue) /
                    (previousNumberOfShares + quantity)
    }

    fun getAverageShareCost () : Float {
        return averageShareCost
    }

    fun sellShares (quantity : Int){
        shares -= quantity
        cash += quantity * gameGlobal.getStockMarketValue()
    }

    fun getNumberOfShares () : Int {
        return shares
    }

    fun withdrawDeposit(index : Int){
        currentPlayer.gainCash(deposits[index].currentValue())
        removeDeposit(index)
    }

    fun makeDeposit(deposit : BankDeposit){
        deposits.add(deposit)
    }

    fun getNumberOfDeposits() : Int {
        return deposits.size
    }

    fun hasDepositEnded(index : Int) : Boolean {
        return (deposits[index].daysRemaining(gameGlobal.getDay()) == 0)
    }

    fun getDepositType(index : Int) : Boolean {
        return (deposits[index].getType())
    }

    fun getDepositCurrentValue(index : Int) : Int {
        return deposits[index].currentValue()
    }

    fun removeDeposit(index : Int) {
        deposits.removeAt(index)
    }

    fun gainCash(add : Int) : Int{
        cash += add
        return cash
    }

    fun spendCash(spend : Int) : Int {
        cash -= spend
        return cash
    }

    fun getCash() : Int {
        return cash
    }
}

enum class PlayerType {
    NONE,
    HUMAN,
    AI
}

fun nextPlayer() : PlayerType {
    playerTurn++
    if (playerTurn == playersList.size){
        playerTurn = 0
        currentPlayer = playersList[playerTurn]
        return PlayerType.NONE
    }
    currentPlayer = playersList[playerTurn]
    return if (currentPlayer.isPlayerHuman()) {
        PlayerType.HUMAN
    } else {
        PlayerType.AI
    }
}

fun nextPlayerOrNextDay(
    navController: NavController,
    showAiMoves : Boolean
){
    val nextPlayer = nextPlayer()
    when (nextPlayer) {
        PlayerType.HUMAN -> {
            when (activityState(currentPlayer)) {
                0 -> navController.navigate("RollDice")
                1 -> navController.navigate("EntrepreneurBusy")
                else -> entrepreneurEnd(currentPlayer, navController)
            }
        }
        PlayerType.NONE -> {
            if (gameGlobal.isLastDay()){
                navController.navigate("EndGame")
            } else {
                navController.navigate("EndDay")
            }
        }
        else -> { // AI
            aiPlays()
            if (showAiMoves){
                navController.navigate("AiTurn")
            } else {
                aiEndTurn(navController, false)
            }
        }
    }
}

fun endTurn(navController: NavController){
    if (hasAnyDepositEnded()) {
        navController.navigate("EndDeposit")
    } else if (currentPlayer.isActiveEntrepreneur()) {
        currentPlayer.decrementDuration()
        navController.navigate("EndTurn")
    } else {
        navController.navigate("EndTurn")
    }
}

fun eliminatePlayer(navController: NavController) {
    val oldCurrentPlayer = currentPlayer
    val isLastPlayer : Boolean = (playerTurn == playersList.size - 1)
    if (isLastPlayer) {
        gameGlobal.endDay()
        playerTurn = 0
    }
    if (oldCurrentPlayer.isHuman()){
        val humanPlayers = gameGlobal.getHumanPlayers()
        gameGlobal.setHumanPlayers(humanPlayers - 1)
    } else {
        val aiPlayers = gameGlobal.getAiPlayers()
        gameGlobal.setAiPlayers(aiPlayers - 1)
    }
    playersList.remove(oldCurrentPlayer)
    currentPlayer = playersList[playerTurn]
    if (isLastPlayer){
        navController.navigate("EndDay")
    } else {
        navController.navigate("RollDice")
    }
}

fun checkEnoughCash(navController: NavController){
    val cash = currentPlayer.getCash()
    if (cash < 0) {
        if (currentPlayer.getTotalResources() < 0) {
            if (gameGlobal.getHumanPlayers() == 1) {
                navController.navigate("GameOver")
            }
            else {
                navController.navigate("EliminatePlayer")
            }
        } else {
            navController.navigate("InsufficientCash")
        }
    }
    else {
        endTurn(navController)
    }
}

fun coversExpense (shares : Int, deposits : Int) : Boolean {
    // First sell shares
    val soldSharesValue = shares * gameGlobal.getStockMarketValue()
    val numberOfDeposits = currentPlayer.getNumberOfDeposits()
    val cash = currentPlayer.getCash()
    var depositsValue = 0
    var countDeposits = deposits
    // First look for deposits that end today
    for (i in 0..<numberOfDeposits) {
        if (currentPlayer.hasDepositEnded(i)) {
            depositsValue += currentPlayer.getDepositCurrentValue(i)
            countDeposits--
            if (countDeposits == 0) {
                break
            }
        }
    }
    if (cash + soldSharesValue + depositsValue >= 0) {
        return true
    }
    if (countDeposits == 0) {
        return false
    }
    // Then look for short deposits because they have less penalty
    for (i in 0..<numberOfDeposits) {
        if (!currentPlayer.hasDepositEnded(i) and !currentPlayer.getDepositType(i)) {
            depositsValue += currentPlayer.getDepositCurrentValue(i)
            countDeposits--
            if (countDeposits == 0) {
                break
            }
        }
    }
    if (cash + soldSharesValue + depositsValue >= 0) {
        return true
    }
    if (countDeposits == 0) {
        return false
    }
    // Finally look for long deposits
    for (i in 0..<numberOfDeposits) {
        if (!currentPlayer.hasDepositEnded(i) and currentPlayer.getDepositType(i)) {
            depositsValue += currentPlayer.getDepositCurrentValue(i)
            countDeposits--
            if (countDeposits == 0) {
                break
            }
        }
    }
    return (cash + soldSharesValue + depositsValue >= 0)
}

fun closePositions(
    shares : Int,
    indicesDeposits : MutableList<Int>
){
    if (shares > 0) {
        currentPlayer.sellShares(shares)
    }
    val numberDeposits = indicesDeposits.size
    if (numberDeposits != 0) {
        for (i in 0..<numberDeposits) {
            currentPlayer.withdrawDeposit(indicesDeposits[i] - i)
        }
    }
}

fun sellForCash (
    shares : Int,
    deposits : Int,
    navController: NavController
){
    val numberOfDeposits = currentPlayer.getNumberOfDeposits()
    val indicesDeposits = mutableListOf<Int>() // Deposits to remove
    if (numberOfDeposits == 0) { // Only shares, no deposit
        currentPlayer.sellShares(shares)
        return
    }
    var depositsValue = 0
    var countDeposits = deposits
    // First look for deposits that end today
    for (i in 0..<numberOfDeposits) {
        if (currentPlayer.hasDepositEnded(i)) {
            depositsValue += currentPlayer.getDepositCurrentValue(i)
            indicesDeposits.add(i)
            countDeposits--
            if (countDeposits == 0) {
                closePositions(shares, indicesDeposits)
                return
            }
        }
    }
    // Then look for short deposits because they have less penalty
    for (i in 0..<numberOfDeposits) {
        if (!currentPlayer.hasDepositEnded(i) and !currentPlayer.getDepositType(i)) {
            depositsValue += currentPlayer.getDepositCurrentValue(i)
            indicesDeposits.add(i)
            countDeposits--
            if (countDeposits == 0) {
                closePositions(shares, indicesDeposits)
                return
            }
        }
    }
    // Finally look for long deposits
    for (i in 0..<numberOfDeposits) {
        if (!currentPlayer.hasDepositEnded(i) and currentPlayer.getDepositType(i)) {
            depositsValue += currentPlayer.getDepositCurrentValue(i)
            indicesDeposits.add(i)
            countDeposits--
            if (countDeposits == 0) {
                closePositions(shares, indicesDeposits)
                return
            }
        }
    }
}

// Front end
@Composable
fun InsufficientCashPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var sellShares by remember { mutableStateOf(0) }
    var withdrawDeposits by remember { mutableStateOf(0) }
    var insufficientSalesPopUp by remember { mutableStateOf(false) }
    val numberDepositsOwned by remember { mutableStateOf(currentPlayer.getNumberOfDeposits()) }
    val numberSharesOwned by remember { mutableStateOf(currentPlayer.getNumberOfShares()) }
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.money, 1)
    }
    val soundId2 = remember {
        soundPool.load(context, R.raw.insufficient, 1)
    }
    val soundId3 = remember {
        soundPool.load(context, R.raw.confirm, 1)
    }
    val soundId4 = remember {
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
        MyHeading(R.string.insufficient_cash)
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
                    text = stringResource(R.string.to_stay_in_the_game_you_have_to_sell_some_of_your_assets),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                    )
                Spacer(modifier = Modifier.height(16.dp))
                if (numberSharesOwned > 0) {
                    Text(
                        text = stringResource(R.string.sell_shares),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "-" Button
                        IconButton(
                            onClick = {
                                if (sellShares > 0) {
                                    sellShares--
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId4, 1f, 1f, 1, 0, 1f)
                                    }
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
                            text = sellShares.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // "+" Button
                        IconButton(
                            onClick = {
                                if (sellShares + 1 <= currentPlayer.getNumberOfShares()) {
                                    sellShares++
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId4, 1f, 1f, 1, 0, 1f)
                                    }
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
                Spacer(modifier = Modifier.height(32.dp)) // Adds space after the Row
                if (numberDepositsOwned > 0) {
                    Text(
                        text = stringResource(R.string.withdraw_deposit_before_term),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "-" Button
                        IconButton(
                            onClick = {
                                if (withdrawDeposits > 0) {
                                    withdrawDeposits--
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId4, 1f, 1f, 1, 0, 1f)
                                    }
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
                            text = withdrawDeposits.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // "+" Button
                        IconButton(
                            onClick = {
                                if (withdrawDeposits + 1 <= currentPlayer.getNumberOfDeposits()) {
                                    withdrawDeposits++
                                    if (settingsViewModel.isSoundEnabled) {
                                        soundPool.play(soundId4, 1f, 1f, 1, 0, 1f)
                                    }
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
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyAnimatedButton(
                label = R.string.next,
                onClick = {
                    if (coversExpense(sellShares, withdrawDeposits)) {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        }
                        sellForCash(sellShares, withdrawDeposits, navController)
                        endTurn(navController)
                    } else {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                        insufficientSalesPopUp = true
                    }
                }
            )
        }
        if (insufficientSalesPopUp) {
            AlertDialog(
                onDismissRequest = {
                    soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                    insufficientSalesPopUp = false },
                title = { Text(stringResource(R.string.insufficient_cash)) },
                text = { Text(stringResource(R.string.you_need_to_sell_more_shares_or_withdraw_more_deposits)) },
                confirmButton = {
                    Button(onClick = {
                        soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                        insufficientSalesPopUp = false
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                        insufficientSalesPopUp = false
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun EliminatePlayerPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
        soundPool.load(context, R.raw.eliminate, 1)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val playerName by remember { mutableStateOf(currentPlayer.name) }
                val today by remember { mutableStateOf(gameGlobal.getDay()) }
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
                    text = stringResource(
                        R.string.you_do_not_have_sufficient_money_to_pay_for_your_activities,
                        currentPlayer.name
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp)) // Manual spacer
                Text(
                    text = stringResource(R.string.you_are_eliminated_from_the_game),
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
                    eliminatePlayer(navController)
                }
            )
        }
    }
}

@Composable
fun EndTurnPage(
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(
                        R.string.end_of_day_for,
                        today,
                        playerName
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
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
                    text = stringResource(R.string.available_cash, cash),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (numberDeposits > 0) {
                    Text(
                        text = stringResource(
                            R.string.number_of_bank_deposits,
                            numberDeposits
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (numberShares > 0) {
                    Text(
                        text = stringResource(
                            R.string.number_of_shares_owned,
                            numberShares
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                for (i in entrepreneurCardsList.indices) {
                    if (playerMedals[i] == 1) {
                        Text(
                            text = stringResource(
                                R.string.you_have_one_medal_for_entrepreneur_activity,
                                entrepreneurCardsList[i].symbol
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (playerMedals[i] > 1) {
                        Text(
                            text = stringResource(
                                R.string.you_have_medals_for_entrepreneur_activity,
                                playerMedals[i],
                                i
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp)) // Manual spacer
                Text(
                    text = stringResource(
                        R.string.total_assets,
                        assets
                    ),
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
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly, // Evenly space the buttons
                verticalAlignment = Alignment.CenterVertically,  // Align buttons vertically
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                if (numberShares > 0) {
                    MyAnimatedButton(
                        label = R.string.sell_shares,
                        onClick = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                            }
                            navController.navigate("SellShares")
                        }
                    )
                }
                MyAnimatedButton(
                    label = R.string.end_turn,
                    onClick = {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                        nextPlayerOrNextDay(navController, settingsViewModel.showAiTurns)
                    }
                )
            }
        }
    }
}
