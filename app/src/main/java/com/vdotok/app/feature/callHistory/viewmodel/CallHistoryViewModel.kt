package com.vdotok.app.feature.callHistory.viewmodel

import androidx.lifecycle.LiveData
import com.vdotok.app.base.BaseViewModel
import com.vdotok.app.models.CallHistoryData
import com.vdotok.app.models.CallHistoryDetails
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created By: VdoTok
 * Date & Time: On 22/11/2021 At 7:01 PM in 2021
 */

@HiltViewModel
class CallHistoryViewModel @Inject constructor() : BaseViewModel() {

    fun getCallHistoryData(): LiveData<List<CallHistoryData>> {
        return callHistoryDao.getImageUpdated()
    }

}