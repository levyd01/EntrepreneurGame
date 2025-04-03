package com.levyd01.entrepreneurgame

import android.media.SoundPool
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.levyd01.entrepreneurgame.ui.theme.CartoonBlue
import com.levyd01.entrepreneurgame.ui.theme.CartoonGreen
import com.levyd01.entrepreneurgame.ui.theme.CartoonLightBackground
import com.levyd01.entrepreneurgame.ui.theme.CartoonRed
import com.levyd01.entrepreneurgame.ui.theme.CartoonTextPrimary

// Back end
fun stockMarketChange() : Int{
    val diceEndOfDay = (1..6).random()
    val changeEndOfDay = when(diceEndOfDay){
        1 -> MARKET_CRASH
        2 -> MARKET_LOSS
        3 -> MARKET_UNCHANGED
        4 -> MARKET_SOME_GAIN
        5 -> MARKET_GOOD_GAIN
        else -> MARKET_BULL_DAY
    }
    gameGlobal.modifyStockMarketValueBy(changeEndOfDay)
    return changeEndOfDay
}


// Front End
@Composable
fun SellSharesPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val todayPrice = gameGlobal.getStockMarketValue()
    var sellNumberOfShares by remember { mutableStateOf(0) }
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val numberShares by remember { mutableStateOf(currentPlayer.getNumberOfShares()) }
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
        MyHeading(R.string.sell_shares)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.Center, // Center content vertically
                horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(text = playerName)
                Text(
                    text = stringResource(
                        R.string.number_of_shares_owned,
                        numberShares
                    )
                )
                Text(text = stringResource(R.string.available_cash, cash))

                Spacer(modifier = Modifier.height(16.dp)) // Add some space between the row and columns

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.current_share_value,
                            todayPrice
                        )
                    )
                }
                Text(
                    text = stringResource(R.string.sell_shares),
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // "-" Button
                    ClickableSmallImage(
                        imagePainter = painterResource(id = R.drawable.minus),
                        onClick = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                            }
                            if (sellNumberOfShares > 0) {
                                sellNumberOfShares--
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Center Text displaying the number
                    Text(
                        text = sellNumberOfShares.toString(),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // "+" Button
                    ClickableSmallImage(
                        imagePainter = painterResource(id = R.drawable.plus),
                        onClick = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                            }
                            if ((sellNumberOfShares + 1) <= numberShares) {
                                sellNumberOfShares++
                            }
                        }
                    )
                }
            }
        }
        Box(
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
                            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        }
                        currentPlayer.sellShares(sellNumberOfShares)
                        endTurn(navController)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CartoonRed,
                        contentColor = CartoonTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp), // Rounded corners
                    modifier = Modifier.weight(1f, fill = false), // Equal width for both buttons,
                ) {
                    Text(text = stringResource(R.string.sell))
                }
                Button(
                    onClick = {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                        endTurn(navController)
                    },
                    modifier = Modifier.weight(1f, fill = false), // Equal width for both buttons,
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
fun EndDayPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val today by remember { mutableStateOf(gameGlobal.getDay()) }
    val todayChange by remember { mutableStateOf(stockMarketChange()) }
    val marketValue by remember { mutableStateOf(gameGlobal.getStockMarketValue()) }
    val sortedList by remember {
        mutableStateOf(playersList.sortedByDescending { it.getTotalResources() })
    }
    val sortedAssets by remember { mutableStateOf(sortedList.map{it.getTotalResources()}) }
    val playersListSize by remember { mutableStateOf(playersList.size)}
    val currentPlayer by remember { mutableStateOf(currentPlayer)}
    val currentPlayerActivityState by remember { mutableStateOf(activityState(currentPlayer))}
    val context = LocalContext.current
    val soundPool = remember { SoundPool.Builder().setMaxStreams(1).build() }
    val soundId = remember {
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
    ) {
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
                    text = stringResource(R.string.end_of_day, today),
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
                when (todayChange) {
                    MARKET_CRASH ->
                        Text(
                            text = stringResource(R.string.market_crashed),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    MARKET_LOSS ->
                        Text(
                            text = stringResource(R.string.market_loss),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    MARKET_UNCHANGED ->
                        Text(
                            text = stringResource(R.string.market_unchanged),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    MARKET_SOME_GAIN ->
                        Text(
                            text = stringResource(R.string.market_some_gain),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    MARKET_GOOD_GAIN ->
                        Text(
                            text = stringResource(R.string.market_good_gain),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    else ->
                        Text(
                            text = stringResource(R.string.market_bull_day),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                }
                Spacer(modifier = Modifier.height(16.dp)) // Manual spacer
                Text(
                    text = stringResource(
                        R.string.the_current_value_of_one_share_is,
                        marketValue
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if ((playersListSize > 1) and (sortedList.isNotEmpty())) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.current_total_assets),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    for (i in sortedList.indices) {
                        Text(
                            text = "${i+1}. ${sortedList[i].name} : ${sortedAssets[i]}",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
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
                label = R.string.next,
                onClick = {
                    if (settingsViewModel.isSoundEnabled) {
                        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                    gameGlobal.endDay()
                    when (currentPlayerActivityState) {
                        0 -> navController.navigate("RollDice")
                        1 -> navController.navigate("EntrepreneurBusy")
                        else -> entrepreneurEnd(currentPlayer, navController)
                    }
                }
            )
        }
    }
}

@Composable
fun StockMarketPage(
    settingsViewModel : SettingsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val todayPrice = gameGlobal.getStockMarketValue()
    var buyNumberOfShares by remember { mutableStateOf(0) }
    val playerName by remember { mutableStateOf(currentPlayer.name) }
    val numberShares by remember { mutableStateOf(currentPlayer.getNumberOfShares()) }
    val cash by remember { mutableStateOf(currentPlayer.getCash()) }
    val marketValue by remember { mutableStateOf(gameGlobal.getStockMarketValue()) }
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
        MyHeading(R.string.stock_market)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.stock_market), // Replace with your image resource
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            Column(
                verticalArrangement = Arrangement.Center, // Center content vertically
                horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
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
                Text(
                    text = stringResource(R.string.available_cash, cash),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                    )

                Spacer(modifier = Modifier.height(16.dp)) // Add some space between the row and columns

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.current_share_value,
                            marketValue
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = stringResource(R.string.buy_shares),
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
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                            }
                            if (buyNumberOfShares > 0) {
                                buyNumberOfShares--
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
                        text = buyNumberOfShares.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // "+" Button
                    IconButton(
                        onClick = {
                            if (settingsViewModel.isSoundEnabled) {
                                soundPool.play(soundId3, 1f, 1f, 1, 0, 1f)
                            }
                            if ((buyNumberOfShares + 1) * todayPrice <= cash) {
                                buyNumberOfShares++
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
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, // Evenly space the buttons
            verticalAlignment = Alignment.CenterVertically,  // Align buttons vertically
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            MyAnimatedButton(
                label = R.string.buy,
                onClick = {
                    if (buyNumberOfShares > 0) {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                        }
                        currentPlayer.buyShares(buyNumberOfShares)
                    } else {
                        if (settingsViewModel.isSoundEnabled) {
                            soundPool.play(soundId2, 1f, 1f, 1, 0, 1f)
                        }
                    }
                    endTurn(navController)
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
