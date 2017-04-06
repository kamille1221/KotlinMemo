package com.example.david.kotlinmemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.card_memo.view.*
import kotlinx.android.synthetic.main.dialog_add_memo.view.*

class MemoAdapter(var mContext: Context, var mMemos: RealmResults<Memo>?, var realm: Realm, autoUpdate: Boolean = true): RealmRecyclerViewAdapter<Memo, RecyclerView.ViewHolder>(mContext, mMemos as OrderedRealmCollection<Memo>?, autoUpdate) {

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
				showMemo(memo.id, memo.title, memo.content)
			}
		}
	}

	fun <T: RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
		itemView.setOnClickListener {
			event.invoke(adapterPosition, itemViewType)
		}
		return this
	}

	fun updateRealm(id: Int, title: String, content: String, date: Long) {
		realm.beginTransaction()
		val memo = realm.where(Memo::class.java).equalTo(MemoUtils.MEMO_ID, id).findFirst()
		memo.title = title
		memo.content = content
		memo.date = date
		realm.copyToRealmOrUpdate(memo)
		realm.commitTransaction()
	}

	fun deleteRealm(id: Int) {
		realm.beginTransaction()
		val deleteResult: RealmResults<Memo> = realm.where(Memo::class.java).equalTo(MemoUtils.MEMO_ID, id).findAll()
		deleteResult.deleteAllFromRealm()
		realm.commitTransaction()
	}

	fun showMemo(id: Int, title: String, content: String) {
		val resource: Int = R.layout.dialog_edit_memo
		val view = (mContext as Activity).layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(mContext)
		builder.setTitle(mContext.getString(R.string.title_edit_memo))
		builder.setView(view)
		view.etTitle.text = SpannableStringBuilder(title)
		view.etContent.text = SpannableStringBuilder(content)
		builder.setPositiveButton(mContext.getString(R.string.edit), null)
		builder.setNeutralButton(mContext.getString(R.string.remove), null)
		builder.setNegativeButton(mContext.getString(R.string.cancel), null)
		val alertDialog: AlertDialog = builder.create()
		alertDialog.setOnShowListener { dialog ->
			val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				val newTitle: String = view.etTitle.text.toString()
				val newContent: String = view.etContent.text.toString()
				if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newContent)) {
					Toast.makeText(mContext, mContext.getString(R.string.toast_empty_memo), Toast.LENGTH_SHORT).show()
				} else {
					updateRealm(id, newTitle, newContent, System.currentTimeMillis())
					dialog.dismiss()
				}
			}
			val neutralButton: Button = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
			neutralButton.setOnClickListener {
				deleteRealm(id)
				dialog.dismiss()
			}
		}
		alertDialog.show()

	}

	internal inner class MemoHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		fun bindMemo(memo: Memo) {
			itemView.tvTitle.text = memo.title
			itemView.tvContent.text = memo.content
			itemView.tvDate.text = MemoUtils.getDate(memo.date)
		}
	}
}
