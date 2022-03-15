package com.vdotok.app.feature.callHistory.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vdotok.app.R
import com.vdotok.app.base.BaseFragment
import com.vdotok.app.databinding.CallHistoryFragmentBinding
import com.vdotok.app.extensions.ViewExtension.hide
import com.vdotok.app.feature.callHistory.adapter.CallHistoryAdapter
import com.vdotok.app.feature.callHistory.viewmodel.CallHistoryViewModel
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.network.models.LoginResponse


class CallHistoryFragment : BaseFragment<CallHistoryFragmentBinding, CallHistoryViewModel>() {
    override val getLayoutRes: Int = R.layout.call_history_fragment
    override val getViewModel: Class<CallHistoryViewModel> = CallHistoryViewModel::class.java
    var userData : LoginResponse? = null
    private lateinit var adapter: CallHistoryAdapter

    companion object {
        fun newInstance() = CallHistoryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)
        setBindingData()
        setClickListeners()
        initAdapter()
        setCallHistoryListObserver()
        return mView
    }

    private fun setCallHistoryListObserver() {
        viewModel.getCallHistoryData().observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNotEmpty())
                adapter.updateData(historyList)
        }
    }
    private fun initAdapter() {
        adapter = context?.let { CallHistoryAdapter(it,ArrayList())}!!
        binding.rcvCallList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        setCallHistoryListObserver()
    }

    private fun setClickListeners() {
        binding.customToolbar.imgArrowBack.performSingleClick {
            activity?.onBackPressed()
        }
    }

    private fun setBindingData() {
        binding.isActiveSession = viewModel.appManager.isTimerRunning
        binding.showBackIcon = true
        binding.showCheckIcon = false
        binding.showIcon = false
        binding.customToolbar.optionMenu.hide()
        binding.toolbarTitle = getString(R.string.callHistory)
    }


}