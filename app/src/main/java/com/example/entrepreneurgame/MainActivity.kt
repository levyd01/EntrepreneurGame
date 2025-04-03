package com.example.entrepreneurgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary
import com.example.entrepreneurgame.ui.theme.MyTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme { // Wrap the content with the custom MaterialTheme
                val navController = rememberNavController()
                val settingsViewModel: SettingsViewModel = viewModel()
                NavigationGraph(settingsViewModel, navController)
            }
        }
    }
}

@Composable
fun MyAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = MyTypography, // Your custom typography
        //colorScheme = AppColorScheme, // Your custom color scheme
        content = content
    )
}


@Composable
fun NavigationGraph(settingsViewModel: SettingsViewModel, navController: NavHostController) {
    NavHost(navController = navController, startDestination = "Init") {
        composable("Init") {
            InitPage(settingsViewModel, navController)
        }
        composable("RollDice") {
            RollDicePage(settingsViewModel, navController)
        }
        composable("Social") {
            SocialPage(settingsViewModel, navController)
        }
        composable("Bank") {
            BankPage(settingsViewModel, navController)
        }
        composable("EndDeposit") {
            EndDepositPage(settingsViewModel, navController)
        }
        composable("EndTurn") {
            EndTurnPage(settingsViewModel, navController)
        }
        composable("StockMarket") {
            StockMarketPage(settingsViewModel, navController)
        }
        composable("EndDay") {
            EndDayPage(settingsViewModel, navController)
        }
        composable("SellShares") {
            SellSharesPage(settingsViewModel, navController)
        }
        composable("Job") {
            JobPage(settingsViewModel, navController)
        }
        composable("InsufficientCash") {
            InsufficientCashPage(settingsViewModel, navController)
        }
        composable("GameOver") {
            GameOverPage(settingsViewModel, navController)
        }
        composable("EliminatePlayer") {
            EliminatePlayerPage(settingsViewModel, navController)
        }
        composable("EntrepreneurStart") {
            EntrepreneurStartPage(settingsViewModel, navController)
        }
        composable("EntrepreneurEnd") {
            EntrepreneurEndPage(settingsViewModel, navController)
        }
        composable("EntrepreneurBusy") {
            EntrepreneurBusyPage(settingsViewModel, navController)
        }
        composable("PlayerName") {
            PlayerNamePage(settingsViewModel, navController)
        }
        composable("EndGame") {
            EndGamePage(settingsViewModel, navController)
        }
        composable("Settings") {
            SettingsPage(settingsViewModel, navController)
        }
        composable("AiTurn") {
            AiTurnPage(settingsViewModel, navController)
        }
        composable("AiEndTurn") {
            AiEndTurnPage(settingsViewModel, navController)
        }
        composable("Shop") {
            ShopPage(settingsViewModel, navController)
        }
    }
}

