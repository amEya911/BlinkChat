package eu.tutorials.blinkchat.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import eu.tutorials.blinkchat.data.state.auth.LoginWithPhoneState
import eu.tutorials.blinkchat.ui.screen.auth.LoginOptions
import eu.tutorials.blinkchat.ui.screen.auth.LoginWithPhoneVerifyOTP
import eu.tutorials.blinkchat.ui.screen.auth.LoginWithPhoneVerifyPhone
import eu.tutorials.blinkchat.ui.screen.auth.Guest
import eu.tutorials.blinkchat.ui.viewmodel.auth.LoginWithPhoneViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    viewModel: LoginWithPhoneViewModel,
    loginState: LoginWithPhoneState
) {

    navigation(
        startDestination = AuthScreen.LoginOptions.route,
        route = Graph.AUTH
    ) {
        composable(AuthScreen.LoginOptions.route) {
            LoginOptions(
                onClickLoginButton = {
                    navController.navigate(AuthScreen.LoginWithPhoneVerifyPhone.route)
                },
                onClickGuestButton = {
                    navController.navigate(AuthScreen.GuestHome.route)
                }
            )
        }

        composable(AuthScreen.LoginWithPhoneVerifyPhone.route) {
            LoginWithPhoneVerifyPhone(
                onVerifyLoginSuccessful = { verificationId, mobileNumber ->
                    navController.navigate("${AuthScreen.LoginWithPhoneVerifyOTP.route}/$verificationId/$mobileNumber")
                },
                viewModel, loginState
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
                },
                onBackClicked = {
                    navController.popBackStack()
                },
                viewModel, loginState
            )
        }

        composable(AuthScreen.GuestHome.route) {
            Guest(
                onButtonClick = { chatRoomId, userId ->
                    navController.navigate("${AppScreen.ChatRoom.route}/$chatRoomId?id=$userId") {
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
    data object GuestHome: AuthScreen("guest-home")
}