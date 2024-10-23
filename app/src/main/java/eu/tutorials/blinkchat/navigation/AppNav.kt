package eu.tutorials.blinkchat.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import eu.tutorials.blinkchat.data.model.ContactModel
import eu.tutorials.blinkchat.ui.screen.Chat
import eu.tutorials.blinkchat.ui.screen.Inbox
import eu.tutorials.blinkchat.ui.screen.LoginOptions
import eu.tutorials.blinkchat.ui.screen.LoginWithPhoneVerifyOTP
import eu.tutorials.blinkchat.ui.screen.LoginWithPhoneVerifyPhone
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val gson = Gson()
    var isOnChatScreen by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry) {
        isOnChatScreen = backStackEntry?.destination?.route?.startsWith(Screen.Chat.route) == true
        Log.d("AppNav", isOnChatScreen.toString())
    }


    NavHost(
        navController = navController,
        startDestination = Screen.LoginOptions.route
    ) {
        composable(Screen.LoginOptions.route) {
            LoginOptions(
                onCLickLoginButton = {
                    navController.navigate(Screen.LoginWithPhoneVerifyPhone.route)
                },
                onClickGuestButton = {}
            )
        }

        composable(Screen.LoginWithPhoneVerifyPhone.route) {
            LoginWithPhoneVerifyPhone(
                onVerifyLoginSuccessful = { verificationId, mobileNumber ->
                    navController.navigate("${Screen.LoginWithPhoneVerifyOTP.route}/$verificationId/$mobileNumber")
                }
            )
        }


        composable("${Screen.LoginWithPhoneVerifyOTP.route}/{verificationId}/{mobileNumber}") { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId")
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber")

            LoginWithPhoneVerifyOTP(
                verificationId = verificationId,
                mobileNumber = mobileNumber,
                onOTPLoginSuccessful = {
                    navController.navigate(Screen.Inbox.route)
                }
            )
        }



        composable(Screen.Inbox.route) {
            Inbox(
                onStartChatWithContact = { contactModel ->
                    val contactJson = URLEncoder.encode(gson.toJson(contactModel), "UTF-8")
                    navController.navigate("${Screen.Chat.route}/$contactJson")
                }
            )
        }

        composable("${Screen.Chat.route}/{contactJson}") { backStackEntry ->
            val contactJson = backStackEntry.arguments?.getString("contactJson")
            val contactModel = contactJson?.let { URLDecoder.decode(it, "UTF-8") }
                ?.let { gson.fromJson(it, ContactModel::class.java) }
            Chat(contactModel)
        }
    }
}

sealed class Screen(val route: String) {
    data object LoginOptions: Screen("login-options")
    data object LoginWithPhoneVerifyPhone: Screen("login-with-phone-verify-phone")
    data object LoginWithPhoneVerifyOTP: Screen("login-with-phone-verify-otp")
    data object Inbox: Screen("inbox")
    data object Chat: Screen("chat")
}