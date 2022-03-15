package com.vdotok.app.feature.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.utils.SDK_PROJECT_ID
import com.vdotok.app.databinding.FragmentSignupBinding
import com.vdotok.app.extensions.ViewExtension.hideKeyboard
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.account.viewmodel.AccountViewModel
import com.vdotok.app.feature.dashboard.Dashboard.Companion.createDashBoardActivity
import com.vdotok.app.utils.Utils
import com.vdotok.app.utils.ValidationUtils.afterTextChanged
import com.vdotok.app.utils.ValidationUtils.checkEmail
import com.vdotok.app.utils.ValidationUtils.checkPassword
import com.vdotok.app.utils.ValidationUtils.checkUserName
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.*
import com.vdotok.network.network.NetworkConnectivity.isInternetAvailable

class SignupFragment : BaseFragment<FragmentSignupBinding, AccountViewModel>() {

    private var hasEnterUsername: ObservableBoolean = ObservableBoolean(false)
    private var hasEnterEmail: ObservableBoolean = ObservableBoolean(false)
    private var hasEnterPassword: ObservableBoolean = ObservableBoolean(false)
    private var email: ObservableField<String> = ObservableField<String>()
    private var username: ObservableField<String> = ObservableField<String>()
    private var password: ObservableField<String> = ObservableField<String>()

    override val getLayoutRes: Int = R.layout.fragment_signup
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
        binding.userNameEntered = hasEnterUsername
        binding.userEmailEntered = hasEnterEmail
        binding.passwordEntered = hasEnterPassword
        binding.username = username
        binding.userEmail = email
        binding.password = password

        binding.edtEmail.afterTextChanged {
            hasEnterEmail.set(it.isNotEmpty() && binding.edtEmail.checkEmail(it))
        }

        binding.edtUserName.afterTextChanged {
            hasEnterUsername.set(it.isNotEmpty() && binding.edtUserName.checkUserName(it))
        }

        binding.edtPassword.afterTextChanged {
            hasEnterPassword.set(it.isNotEmpty() && binding.edtPassword.checkPassword(it))
        }
    }

    private fun setButtonClicks() {
        binding.btnSignUp.performSingleClick {
            activity?.hideKeyboard()
            checkUserAvailability()
        }

        binding.btnSignIn.performSingleClick {
            activity?.onBackPressed()
        }
    }

    private fun checkUserAvailability() {
        activity?.let {
            viewModel.checkEmailAlreadyExist(email.get().toString()).observe(viewLifecycleOwner) {
                when (it) {
                    is Result.Loading -> {
                        showProgress(requireContext(), getString(R.string.loading_user_availability))
                    }
                    is Result.Success -> {
                        handleCheckFullNameResponse(it.data)
                        hideProgress()
                    }
                    is Result.Failure -> {
                        hideProgress()
                        if (isInternetAvailable(this@SignupFragment.requireContext()).not())
                            binding.root.showSnackBar(getString(R.string.no_network_available))
                        else
                            binding.root.showSnackBar(it.exception.message)
                    }
                }

            }
        }
    }

    private fun handleCheckFullNameResponse(response: LoginResponse) {
        when (response.status) {
            HttpResponseCodes.SUCCESS.value -> signUp()
            else -> binding.root.showSnackBar(response.message)
        }
    }

    private fun signUp() {
        viewModel.signUp(
            SignUpModel(
                username.get().toString(), email.get().toString(),
                password.get().toString(), project_id = SDK_PROJECT_ID
            )
        ).observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    showProgress(requireContext(), getString(R.string.loading_sign_up))
                }
                is Result.Success -> {
                    handleSignupResponse(it.data, binding.root)
                    hideProgress()
                }
                is Result.Failure -> {
                    hideProgress()
                    if (isInternetAvailable(this@SignupFragment.requireContext()).not())
                        binding.root.showSnackBar(getString(R.string.no_network_available))
                    else
                        binding.root.showSnackBar(it.exception.message)
                }
            }
        }
    }

    private fun handleSignupResponse(response: LoginResponse, view: View) {
        when (response.status) {
            HttpResponseCodes.SUCCESS.value -> {
                UserPreferences.userData = response
                Utils.createMessagingConnection(response)
                startActivity(activity?.applicationContext?.let { createDashBoardActivity(it) })
                binding.root.showSnackBar("Success")
            }
            else -> {
                view.showSnackBar(response.message)
            }
        }
    }


}