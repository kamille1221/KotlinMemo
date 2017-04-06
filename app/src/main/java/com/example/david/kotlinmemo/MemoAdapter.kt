package com.example.david.kotlinmemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.card_memo.view.*
import kotlinx.android.synthetic.main.dialog_add_memo.view.*

class MemoAdapter(var mContext: Context, var mMemos: RealmResults<Memo>?, autoUpdate: Boolean = true): RealmRecyclerViewAdapter<Memo, RecyclerView.ViewHolder>(mContext, mMemos as OrderedRealmCollection<Memo>?, autoUpdate) {

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
		(holder as MemoHolder).bindMemo(mMemos!![position])
	}

	override fun getItemCount(): Int {
		return mMemos?.size ?: 0
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return MemoHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_memo, parent, false)).listen { position, _ ->
			val memo = mMemos?.get(position)
			if (memo != null) {
				showMemo(memo.title, memo.content)
			}
		}
	}

	fun <T: RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
		itemView.setOnClickListener {
			event.invoke(adapterPosition, itemViewType)
		}
		return this
	}

	fun showMemo(title: String, content: String) {
		val resource: Int = R.layout.dialog_add_memo
		val view = (mContext as Activity).layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(mContext)
		builder.setTitle("edit Memo")
		builder.setView(view)
		view.etTitle.text = SpannableStringBuilder(title)
		view.etContent.text = SpannableStringBuilder(content)
		builder.setPositiveButton("Edit", null)
		builder.setNegativeButton("Cancel", null)
		builder.create().show()
	}

	internal inner class MemoHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		fun bindMemo(memo: Memo) {
			itemView.tvTitle.text = memo.title
			itemView.tvContent.text = memo.content
			itemView.tvDate.text = MemoUtils.getDate(memo.date)
		}
	}
}
