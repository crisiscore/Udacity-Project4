package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private lateinit var authenticationViewModel: AuthenticationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        initFirebaseAuth()
        initViewModel()

        btn_login.setOnClickListener {
            launchFirebaseAuthUI()
        }

    }

    private fun initViewModel() {
        authenticationViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        authenticationViewModel.user.observe(this) { currentUser ->
            if (currentUser != null) {
                goToReminderList()
            }
        }

    }

    private fun goToReminderList() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    private fun initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance()
    }

    private fun launchFirebaseAuthUI() {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.AppTheme)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)

    }

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract())
    { authenticationResult ->
        Log.e("AuthenticationActivity", "onActivityResult: $authenticationResult")
        authenticationViewModel.onSignInResult(authenticationResult)
    }
}
