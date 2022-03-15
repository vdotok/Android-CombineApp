package com.vdotok.app.feature.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.databinding.FragmentLoginBinding
import com.vdotok.app.extensions.ViewExtension.hideKeyboard
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.account.viewmodel.AccountViewModel
import com.vdotok.app.feature.dashboard.Dashboard.Companion.createDashBoardActivity
import com.vdotok.app.utils.Utils.createMessagingConnection
import com.vdotok.app.utils.ValidationUtils.afterTextChanged
import com.vdotok.app.utils.ValidationUtils.checkPassword
import com.vdotok.app.utils.ValidationUtils.checkValidation
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.network.HttpResponseCodes
import com.vdotok.network.network.NetworkConnectivity.isInternetAvailable
import com.vdotok.network.network.Result

class LoginFragment : BaseFragment<FragmentLoginBinding, AccountViewModel>() {

    private var hasEnterUsername: ObservableBoolean = ObservableBoolean(false)
    private var hasEnterPassword: ObservableBoolean = ObservableBoolean(false)
    private var usernameEmail: ObservableField<String> = ObservableField<String>()
    private var password: ObservableField<String> = ObservableField<String>()

    override val getLayoutRes: Int = R.layout.fragment_login
    override val getViewModel: Class<AccountViewModel> = AccountViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)

        setupUI()
        setButtonClicks()

        return mView
    }

    private fun setupUI() {
        binding.usernameEntered = hasEnterUsername
        binding.passwordEntered = hasEnterPassword
        binding.userEmail = usernameEmail
        binding.password = password

        binding.edtEmail.afterTextChanged {
            hasEnterUsername.set(it.isNotEmpty() && binding.edtEmail.checkValidation(it))
        }

        binding.edtPassword.afterTextChanged {
            hasEnterPassword.set(it.isNotEmpty() && binding.edtPassword.checkPassword(it))
        }
    }

    private fun setButtonClicks() {
        binding.btnSignIn.performSingleClick {
            activity?.hideKeyboard()
            loginUser()
        }

        binding.btnSignUp.performSingleClick {
            Navigation.findNavController(binding.root).navigate(R.id.action_move_to_signup_user)
        }
    }

    private fun loginUser() {
        activity?.let { _ ->
            viewModel.loginUser(usernameEmail.get().toString(), password.get().toString())
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is Result.Loading -> {
                            showProgress(requireContext(), getString(R.string.loading_sign_in))
                        }
                        is Result.Success -> {
                            handleLoginResponse(it.data)
                            hideProgress()
                        }
                        is Result.Failure -> {
                            hideProgress()
                            if (isInternetAvailable(this@LoginFragment.requireContext()).not())
                                binding.root.showSnackBar(getString(R.string.no_network_available))
                            else
                                binding.root.showSnackBar(it.exception.message)
                        }
                    }
                }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        when (response.status) {
            HttpResponseCodes.SUCCESS.value -> {
                UserPreferences.userData = response
                createMessagingConnection(response)
                startActivity(activity?.applicationContext?.let { createDashBoardActivity(it) })
                binding.root.showSnackBar("Success")
            }
            else -> {
                binding.root.showSnackBar(response.message)
            }
        }
    }


}