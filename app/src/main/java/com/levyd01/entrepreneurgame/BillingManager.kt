package com.levyd01.entrepreneurgame


import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class BillingManager(
    private val appContext: Context,  // Properly named parameter
    var onPurchaseUpdated: (Boolean) -> Unit
) {

    companion object {
        private const val TAG = "BillingManager"
    }

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener { billingResult, purchases ->
            Log.d(TAG, "BillingClient listener triggered")
            Log.d(TAG, "BillingResult: ${billingResult.responseCode}, ${billingResult.debugMessage}")
            Log.d(TAG, "Purchases: ${purchases?.size ?: "null"}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    Log.d(TAG, "Processing purchase: ${purchase.products}, state: ${purchase.purchaseState}")
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        Log.d(TAG, "Found unacknowledged purchase, acknowledging...")
                        // Consider acknowledging the purchase here if needed
                        onPurchaseUpdated(true)
                    }
                }
            }
        }
        .enablePendingPurchases()
        .build()

    private fun isBillingSupported(): Boolean {
        return try {
            appContext.packageManager.getPackageInfo("com.android.vending", 0) != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    fun startConnection() {
        if (!isBillingSupported()) {
            Log.e(TAG, "Billing not supported on this device")
            onPurchaseUpdated(false)
            return
        }
        Log.d(TAG, "Starting billing connection...")

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(TAG, "Billing setup finished: ${billingResult.responseCode}, ${billingResult.debugMessage}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected successfully, querying purchases...")
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing connection failed: ${billingResult.debugMessage}")
                    onPurchaseUpdated(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e(TAG, "Billing service disconnected")
            }
        })
    }

    fun queryPurchases() {
        Log.d(TAG, "Querying purchases...")
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(TAG, "Query purchases result: ${billingResult.responseCode}, ${billingResult.debugMessage}")
            Log.d(TAG, "Found ${purchases.size} purchases")
            purchases.forEach { Log.d(TAG, "Purchase: ${it.products} state: ${it.purchaseState}") }
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPurchased = purchases.any {
                    it.products.contains("unlimited_turns") &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                Log.d(TAG, "Has unlimited turns: $hasPurchased")
                onPurchaseUpdated(hasPurchased)
            } else {
                Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
                onPurchaseUpdated(false)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        Log.d(TAG, "Launching purchase flow...")
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("unlimited_turns")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
            Log.d(TAG, "Product details query result: ${billingResult.responseCode}, ${billingResult.debugMessage}")
            Log.d(TAG, "Found ${productDetailsList.size} product details")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList.first()
                Log.d(TAG, "Product details found: ${productDetails.productId}, ${productDetails.title}")

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                Log.d(TAG, "Launching billing flow...")
                val launchBillingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                Log.d(TAG, "Billing flow launch result: ${launchBillingResult.responseCode}, ${launchBillingResult.debugMessage}")
                if (launchBillingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "Failed to launch billing flow: ${launchBillingResult.debugMessage}")
                }
            } else {
                Log.e(TAG, "Failed to get product details: ${billingResult.debugMessage}")
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}