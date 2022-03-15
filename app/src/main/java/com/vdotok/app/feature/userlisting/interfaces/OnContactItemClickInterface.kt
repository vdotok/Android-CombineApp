package com.vdotok.app.feature.userlisting.interfaces


/**
 * Created By: VdoTok
 * Date & Time: On 23/11/2021 At 6:24 PM in 2021
 */
interface OnContactItemClickInterface {
    fun onItemClick(position: Int) {}
    fun onChatIconClick(position: Int) {}
    fun onCallIconClick(position: Int) {}
    fun onVideoIconClick(position: Int) {}
    fun searchResult(position: Int) {}
}