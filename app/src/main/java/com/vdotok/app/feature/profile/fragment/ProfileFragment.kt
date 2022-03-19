package com.vdotok.app.feature.profile.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.utils.image_uri
import com.vdotok.app.utils.type
import com.vdotok.app.databinding.ProfileFragmentBinding
import com.vdotok.app.extensions.ViewExtension.hide
import com.vdotok.app.extensions.ViewExtension.showSnackBar
import com.vdotok.app.feature.profile.viewmodel.ProfileViewModel
import com.vdotok.app.utils.PermissionUtils
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.app.utils.getFileFromUri
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.ProfileImageResponse
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ProfileFragment : BaseFragment<ProfileFragmentBinding, ProfileViewModel>() {
    override val getLayoutRes: Int = R.layout.profile_fragment
    override val getViewModel: Class<ProfileViewModel> = ProfileViewModel::class.java
    var userData : LoginResponse? = null

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)
        userData = UserPreferences.userData as LoginResponse
        showProgress(requireContext(),getString(R.string.loading_profile))
        getDataResult()
        setBindingData()
        setClickListeners()
        return mView
    }

    private fun getDataResult() {
        binding.user = userData
        if (!userData?.profile_pic.isNullOrEmpty()){
            binding.imageAvailable = true
            activity?.runOnUiThread {
                context?.let {
                    binding.imageAvailable = true
                    Glide.with(it).load(userData?.profile_pic)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.imgUser.profileImage)
                }
               hideProgress()
            }
        }else{
            binding.dp = firstTwoCharName()
            hideProgress()
        }
    }

    private fun getImage(encodedImage: File) {
            activity?.let { _ ->
                userData?.authToken?.let { token ->
                    val type = RequestBody.create("type".toMediaTypeOrNull(), type)
                    val authToken = RequestBody.create("auth_token".toMediaTypeOrNull(), token)
                    val filePart = MultipartBody.Part.createFormData(
                        "uploadFile",
                        "profilePic",
                        RequestBody.create("image/*".toMediaTypeOrNull(), encodedImage))
                    viewModel.profileImage(filePart,type,authToken)
                        .observe(viewLifecycleOwner) {
                            when (it) {
                                is Result.Loading -> {
                                }
                                is Result.Success -> {
                                    handleLoginResponse(it.data)
                                }
                                is Result.Failure -> {
                                    hideProgress()
                                    if (NetworkConnectivity.isInternetAvailable(this.requireContext())
                                            .not()
                                    )
                                        binding.root.showSnackBar(getString(R.string.no_network_available))
                                    else
                                        binding.root.showSnackBar(it.exception.message)
                                }
                            }
                        }
                }
            }
    }

    private fun handleLoginResponse(data: ProfileImageResponse) {
        updateImageUri(data)
    }

    private fun updateImageUri(data: ProfileImageResponse) {
        val file = UserPreferences.userData as LoginResponse
        file.profile_pic = data.image
        UserPreferences.userData = file
    }


    private fun setClickListeners() {
        binding.customToolbar.imgArrowBack.performSingleClick {
            activity?.onBackPressed()
        }
        binding.editProfile.performSingleClick {
            PermissionUtils.getStoragePermission(
                requireContext(),
                { openGallery() }
                , {},
                this::showReadWritePermissionsRequiredDialog
            )
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForResult.launch(intent)
    }

    private var startForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val data: Intent? = activityResult.data
                setProfilePic(data?.data)
                val file = data?.data?.let { getFileFromUri(it,requireContext()) }
                if (file != null) {
                    getImage(file)
                }
            }
        }

    private fun setProfilePic(data: Uri?) {
        activity?.runOnUiThread {
            context?.let {
                binding.imageAvailable = true
                Glide.with(it).load(data)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.imgUser.profileImage)
            }
        }
    }


    private fun firstTwoCharName(): String? {
        binding.imageAvailable = false
        return if (userData?.fullName?.length!! < 2) userData?.fullName else userData?.fullName?.substring(0, 2)
    }

    private fun setBindingData() {
        binding.isActiveSession = viewModel.appManager.isTimerRunning
        binding.showBackIcon = true
        binding.showCheckIcon = false
        binding.showIcon = false
        binding.customToolbar.optionMenu.hide()
        binding.toolbarTitle = getString(R.string.profile)
        toolbarColorChange()
    }

    private fun toolbarColorChange() {
        binding.customToolbar.tvTitle.setTextColor(
            context?.let { ContextCompat.getColor(it, R.color.light_black) }?.let {
                ColorStateList.valueOf(
                    it
                )
            })
        binding.customToolbar.imgArrowBack.setColorFilter(ContextCompat.getColor(
            requireContext(),
            R.color.light_black))
    }
}