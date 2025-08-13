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
import com.sun.englishlearning.databinding.FragmentMeBinding
import com.sun.englishlearning.utils.base.BaseFragment
import kotlinx.coroutines.launch

class MeFragment : BaseFragment<FragmentMeBinding>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMeBinding {
        return FragmentMeBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        auth = Firebase.auth
        credentialManager = CredentialManager.create(requireContext())
    }

    override fun initView() {
        // Remaining listener for the logout button
        viewBinding.btnSignOut.setOnClickListener {
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check user state and update UI when the fragment is displayed
        updateUI(auth.currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user!= null) {
            // User is logged in: display personal information
            viewBinding.layoutUserProfile.visibility = View.VISIBLE
            viewBinding.tvUserName.text = user.displayName
            viewBinding.tvUserEmail.text = user.email
        } else {
            // User is not logged in (rare case on this screen): hide information
            viewBinding.layoutUserProfile.visibility = View.GONE
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
