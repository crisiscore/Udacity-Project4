package com.udacity.project4.authentication

import android.app.Activity.RESULT_OK
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser>
        get() = _user

    init {
        _user.value = FirebaseAuth.getInstance().currentUser
        Log.e("AuthenticationViewModel", Gson().toJson(_user.value))
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        Log.e("AuthenticationViewModel", "Sign in result ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            _user.value = FirebaseAuth.getInstance().currentUser
            Log.e("AuthenticationViewModel", "Sign in successful ${result.idpResponse?.email}")

        } else {
            Log.e("AuthenticationViewModel", "Sign in unsuccessful ${result.idpResponse?.error?.message}")
        }
    }
}