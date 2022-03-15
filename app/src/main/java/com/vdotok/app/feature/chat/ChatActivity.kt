package com.vdotok.app.feature.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.vdotok.app.R
import com.vdotok.app.base.BaseActivity
import com.vdotok.app.databinding.ActivityChatBinding
import com.vdotok.app.feature.chat.viewmodel.ChatViewModel
import com.vdotok.app.feature.dashboard.Dashboard
import com.vdotok.network.models.GroupModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : BaseActivity<ActivityChatBinding, ChatViewModel>() {

    override val getLayoutRes: Int = R.layout.activity_chat
    override val getViewModel: Class<ChatViewModel> = ChatViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setCallHeader()
        val nav =
            supportFragmentManager.findFragmentById(R.id.chat_nav_host_fragment) as NavHostFragment
        nav.navController.apply {
            Bundle().apply {
                val groupModel = intent.getParcelableExtra<GroupModel>(GROUP_MODEL)
                putParcelable(GROUP_MODEL, groupModel)
                setGraph(R.navigation.chat_nav, this)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Dashboard.createDashBoardActivity(this))
    }

    companion object {
        const val GROUP_MODEL = "GROUP_MODEL"
        fun createChatActivity(context: Context, groupModel: GroupModel) = Intent(
            context,
            ChatActivity::class.java
        ).apply {
            this.putExtra(GROUP_MODEL, groupModel)
        }

        fun createChatActivityFromCreateGroup(context: Context, groupModel: GroupModel) = Intent(
            context,
            ChatActivity::class.java
        ).apply {
            this.putExtra(GROUP_MODEL, groupModel)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }
}