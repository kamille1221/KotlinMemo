package com.example.david.kotlinmemo

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.card_memo.view.*
import java.text.SimpleDateFormat
import java.util.*

class MemoAdapter(var context: Context, var memos: List<Memo>?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
		(holder as ViewHolder).bindMemo(memos!![position])
	}

	override fun getItemCount(): Int {
		return memos?.size ?: 0
	}

	override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
		return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_memo, parent, false))
	}

	internal inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		fun bindMemo(memo: Memo) {
			itemView.tvTitle.text = memo.title
			itemView.tvContent.text = memo.content
			itemView.tvDate.text = getDate(memo.date)
		}
	}

	fun getDate(milliSecond: Long): String {
		val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.getDefault())
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = milliSecond
		return simpleDateFormat.format(calendar.time)
	}
}
