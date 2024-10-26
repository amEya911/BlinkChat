package eu.tutorials.blinkchat.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import eu.tutorials.blinkchat.ui.screen.LoginOptions
import eu.tutorials.blinkchat.ui.screen.LoginWithPhoneVerifyOTP
import eu.tutorials.blinkchat.ui.screen.LoginWithPhoneVerifyPhone

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthScreen.LoginOptions.route,
        route = Graph.AUTH
    ) {
        composable(AuthScreen.LoginOptions.route) {
            LoginOptions(
                onClickLoginButton = {
                    navController.navigate(AuthScreen.LoginWithPhoneVerifyPhone.route)
                },
                onClickGuestButton = {}
            )
        }

        composable(AuthScreen.LoginWithPhoneVerifyPhone.route) {
            LoginWithPhoneVerifyPhone(
                onVerifyLoginSuccessful = { verificationId, mobileNumber ->
                    navController.navigate("${AuthScreen.LoginWithPhoneVerifyOTP.route}/$verificationId/$mobileNumber")
                }
            )
        }

        composable("${AuthScreen.LoginWithPhoneVerifyOTP.route}/{verificationId}/{mobileNumber}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId")
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber")

            LoginWithPhoneVerifyOTP(
                verificationId = verificationId,
                mobileNumber = mobileNumber,
                onOTPLoginSuccessful = {
                    navController.navigate(Graph.APP) {
                        popUpTo(Graph.AUTH) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class AuthScreen(val route: String) {
    data object LoginOptions : AuthScreen("login-options")
    data object LoginWithPhoneVerifyPhone : AuthScreen("login-with-phone-verify-phone")
    data object LoginWithPhoneVerifyOTP : AuthScreen("login-with-phone-verify-otp")
}