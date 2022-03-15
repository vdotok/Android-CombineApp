package com.vdotok.app.utils

import android.content.Context
import android.os.Build
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import com.vdotok.app.R
import com.vdotok.app.base.BaseViewModel
import com.vdotok.app.base.UserPreferences
import com.vdotok.app.feature.account.AccountActivity
import com.vdotok.app.feature.callHistory.CallHistory
import com.vdotok.app.feature.profile.ProfileActivity
import java.lang.reflect.Method

fun showPopMenu(context: Context, view: ImageView,viewModel: BaseViewModel){
    val popup = PopupMenu(context, view,R.style.MyPopupMenu)
    val inflater = popup.menuInflater
    inflater.inflate(R.menu.details_option_menu, popup.menu)
    popup.setOnMenuItemClickListener {item->
        menuItemClick(item,context,viewModel)
        true
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    popup.setForceShowIcon(true)
    }else{
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field[popup]
                    val classPopupHelper =
                        Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons: Method = classPopupHelper.getMethod(
                        "setForceShowIcon",
                        Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    popup.show()
}

private fun menuItemClick(item: MenuItem?, context: Context, viewModel: BaseViewModel) {
    when (item?.itemId) {
        R.id.profileOption -> {
            context.startActivity(ProfileActivity.createProfileActivity(context))
        }
        R.id.callHistoryOption -> {
            context.startActivity(CallHistory.createCallHistoryActivity(context))
        }
        R.id.logoutOption -> {
            viewModel.logout()
            UserPreferences.clearUserData()
            context.startActivity(AccountActivity.createAccountsActivity(context))
        }
    }
}
