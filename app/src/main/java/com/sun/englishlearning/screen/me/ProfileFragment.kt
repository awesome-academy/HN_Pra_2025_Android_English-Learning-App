package com.sun.englishlearning.screen.me

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.databinding.FragmentProfileBinding
import com.sun.englishlearning.utils.base.BaseFragment
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(layoutInflater, container, false)

    override fun initData() {
        auth = Firebase.auth
        credentialManager = CredentialManager.create(requireContext())
    }

    override fun initView() {
        viewBinding.llLogout.setOnClickListener {
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()

        updateUI(auth.currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user!= null) {
            viewBinding.tvProfileName.text = user.displayName
            viewBinding.tvProfileEmail.text = user.email
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            auth.signOut()

            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
            }
            (activity as? MainActivity)?.navigateToLogin()
        }
    }
}
