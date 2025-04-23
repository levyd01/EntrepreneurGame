package com.levyd01.entrepreneurgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.levyd01.entrepreneurgame.ui.theme.MyTypography

class MainActivity : ComponentActivity() {
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize BillingManager with a callback
        billingManager = BillingManager(this) { isPurchased ->
            hasUnlimitedTurns = isPurchased
        }

        setContent {
            MyAppTheme { // Wrap the content with the custom MaterialTheme
                val navController = rememberNavController()
                val settingsViewModel: SettingsViewModel = viewModel()
                NavigationGraph(settingsViewModel, navController, billingManager)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Start connection when activity becomes visible
        billingManager.startConnection()
    }

    override fun onStop() {
        super.onStop()
        // Consider ending the connection when activity stops
        billingManager.endConnection()
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
fun NavigationGraph(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController,
    billingManager: BillingManager
    ) {
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
            ShopPage(
                settingsViewModel,
                navController,
                billingManager
            )
        }
    }
}

