package com.example.david.kotlinmemo

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import io.realm.exceptions.RealmMigrationNeededException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_memo.view.*
import kotlin.properties.Delegates


class MainActivity: AppCompatActivity() {
	private val TAG: String = "MainActivity"
	private var realm: Realm by Delegates.notNull()
	private var realmConfig: RealmConfiguration by Delegates.notNull()
	lateinit private var mFirebaseAnalytics: FirebaseAnalytics

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val toolbar = findViewById(R.id.toolbar) as Toolbar
		setSupportActionBar(toolbar)

		fab.setOnClickListener { addMemo() }
		rvMain.setHasFixedSize(true)
		rvMain.layoutManager = LinearLayoutManager(this)

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
		initRealm()
	}

	override fun onResume() {
		super.onResume()
		rvMain.adapter = MemoAdapter(this, getMemos())
	}

	fun initRealm() {
		Realm.init(this)
		realmConfig = RealmConfiguration.Builder().build()
		try {
			realm = Realm.getInstance(realmConfig)
		} catch (e: RealmMigrationNeededException) {
			Log.e(TAG, "MigrationNeededException Path: ${e.path}")
			Realm.deleteRealm(realmConfig)
			realm = Realm.getInstance(realmConfig)
		}
	}

	fun commitRealm(title: String, content: String, date: Long) {
		realm.beginTransaction()
		val memo = realm.createObject(Memo::class.java)
		memo.title = title
		memo.content = content
		memo.date = date
		realm.commitTransaction()
	}

	fun addMemo() {
		val resource: Int = R.layout.dialog_memo
		val view = this.layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(this)
		builder.setView(view)
		builder.setPositiveButton("Save", { _, _ ->
			val title: String = view.etTitle.text.toString()
			val content: String = view.etContent.text.toString()
			if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
				Toast.makeText(this, "You must input a title and content :(", Toast.LENGTH_SHORT).show()
			} else {
				commitRealm(title, content, System.currentTimeMillis())
			}
		})
		builder.setNegativeButton("Cancel", null)
		builder.create().show()
	}

	fun getMemos(): RealmResults<Memo>? {
		return realm.where(Memo::class.java).findAllSorted("date", Sort.DESCENDING)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_settings -> return true
		}
		return super.onOptionsItemSelected(item)
	}
}
