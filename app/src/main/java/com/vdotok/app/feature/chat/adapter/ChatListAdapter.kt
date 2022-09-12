package com.vdotok.app.feature.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vdotok.app.R
import com.vdotok.app.databinding.ItemMessageRowBinding
import com.vdotok.app.feature.chat.clickListenerInterface.FileClickListener
import com.vdotok.app.feature.chat.viewmodel.ChatViewModel
import com.vdotok.app.utils.ViewUtils.performSingleClick
import com.vdotok.connect.models.Message
import com.vdotok.connect.models.MessageType
import com.vdotok.connect.models.ReadReceiptModel
import com.vdotok.connect.models.ReceiptType
import com.vdotok.connect.utils.ImageUtils


class ChatListAdapter(
    private val callback: FileClickListener,
    val list: ArrayList<Message>,
    val chatViewModel: ChatViewModel,
    val context: Context
) : RecyclerView.Adapter<ChatViewHolder>() {
    var items: ArrayList<Message> = ArrayList()
    var sendStatus: Boolean = false

    init {
        items.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.binding?.sendStatus = sendStatus
        holder.binding?.chatViewModel = chatViewModel

        val data = items[position]
        holder.binding?.model = data
        if (data.from == chatViewModel.getUserData().refId) {
            holder.binding?.sender = true
            holder.binding?.seenMsg = data.status == ReceiptType.SEEN.value && data.readCount > 0
        } else {
            if (data.status != ReceiptType.SEEN.value) {
                data.status = ReceiptType.SEEN.value
                chatViewModel.sendAcknowledgeMsgToGroup(data)
            }
            holder.binding?.sender = false
        }

        when (data.type) {
            MessageType.text -> {
                holder.binding?.customMessageTypeText?.messageDisplay?.text = data.content
            }
            MessageType.media -> {
                when (data.subType) {
                    0 -> {
//                        holder.binding?.customImageTypeText?.imageTypeMessage?.setImageBitmap(
//                            ImageUtils.decodeBase64(data.content))
                        Glide.with(context).load(data.content)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(holder.binding?.customImageTypeText?.imageTypeMessage!!)
                        holder.binding?.customImageTypeText?.image?.performSingleClick {
                           callback.onFileClick()
                        }
                    }
                    2 -> {
                        holder.binding?.customFileTypeText?.fileTypeDisplay?.setText(R.string.video_file)
                        holder.binding?.customFileTypeText?.file?.performSingleClick {
                            callback.onFileClick()
                        }
                    }
                    3 -> {
                        holder.binding?.customFileTypeText?.fileTypeDisplay?.setText(R.string.doc_file)
                        holder.binding?.customFileTypeText?.file?.performSingleClick {
                            callback.onFileClick()
                        }
                    }
                    1 -> {
                        holder.binding?.customFileTypeText?.fileTypeDisplay?.setText(R.string.audio_file)
                        holder.binding?.customFileTypeText?.file?.performSingleClick {
                            callback.onFileClick()
                        }
                    }
                }

            }

            else -> {
            }
        }

    }

    fun updateMessageForReceipt(model: ReadReceiptModel) {
        val item = items.firstOrNull { it.id == model.messageId }
        val position = items.indexOf(item)

        if(model.receiptType == ReceiptType.SEEN.value){
            item?.status = model.receiptType
            item?.readCount = item?.readCount?.plus(1) ?: 0

            item?.let {
                items[position] = item
                chatViewModel.appManager.updateMessageMapData(it)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return position.toLong()

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun addItem(item: Message) {
        item.date = System.currentTimeMillis()
        items.add(item)
        notifyItemInserted(itemCount - 1)
    }

    fun updateData(chatList: ArrayList<Message>) {
        items.clear()
        items.addAll(chatList)
        notifyItemRangeChanged(0, items.size)
    }

}

class ChatViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_message_row, parent, false)) {
    var binding: ItemMessageRowBinding? = null

    init {
        binding = DataBindingUtil.bind(itemView)
    }
}



