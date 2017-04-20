package com.example.david.kotlinmemo

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import io.realm.exceptions.RealmMigrationNeededException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_add_memo.view.*
import kotlinx.android.synthetic.main.dialog_login.view.*
import kotlinx.android.synthetic.main.dialog_set_password.view.*
import kotlin.properties.Delegates


class MainActivity: AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
	private val TAG: String = this::class.java.simpleName
	private var realm: Realm by Delegates.notNull()
	private var realmConfig: RealmConfiguration by Delegates.notNull()
	private var isLocked = true
	lateinit private var mFirebaseAnalytics: FirebaseAnalytics

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val toolbar = findViewById(R.id.toolbar) as Toolbar
		setSupportActionBar(toolbar)

		srlMain.setOnRefreshListener(this)
		srlMain.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent))

		rvMain.setHasFixedSize(true)
		rvMain.layoutManager = LinearLayoutManager(this)

		fab.setOnClickListener { addMemo() }
		btnLogin.setOnClickListener { login() }

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
		MemoUtils.getSharedPreferences(this)
		initRealm()
	}

	override fun onResume() {
		super.onResume()
		lockView()
		if (!TextUtils.isEmpty(MemoUtils.getPassword()) && isLocked) {
			login()
		} else {
			rvMain.adapter = MemoAdapter(this, getMemos(), realm)
			hideRefreshing()
		}
	}

	override fun onPause() {
		super.onPause()
		if (!TextUtils.isEmpty(MemoUtils.getPassword())) {
			isLocked = true
			rvMain.adapter = null
		}
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
		val id: Int = realm.where(Memo::class.java).max(MemoUtils.MEMO_ID)?.toInt() ?: 1
		val memo = realm.createObject(Memo::class.java, id + 1)
		memo.title = title
		memo.content = content
		memo.date = date
		realm.commitTransaction()
		Toast.makeText(this, getString(R.string.toast_save_memo), Toast.LENGTH_SHORT).show()
	}

	fun addMemo() {
		val resource: Int = R.layout.dialog_add_memo
		val view = this.layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(this)
		builder.setTitle(getString(R.string.title_new_memo))
		builder.setView(view)
		builder.setPositiveButton(getString(R.string.save), null)
		builder.setNegativeButton(getString(R.string.cancel), null)
		val alertDialog: AlertDialog = builder.create()
		alertDialog.setOnShowListener { dialog ->
			val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				val title: String = view.etTitle.text.toString()
				val content: String = view.etContent.text.toString()
				if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
					Toast.makeText(this, getString(R.string.toast_empty_memo), Toast.LENGTH_SHORT).show()
				} else {
					commitRealm(title, content, System.currentTimeMillis())
					dialog.dismiss()
				}
			}
		}
		alertDialog.show()
	}

	fun getMemos(): RealmResults<Memo>? {
		return realm.where(Memo::class.java).findAllSorted(MemoUtils.MEMO_ID, Sort.DESCENDING)
	}

	fun setPassword() {
		val resource: Int = R.layout.dialog_set_password
		val dialogView = this.layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(this)
		builder.setTitle(getString(R.string.title_set_password))
		builder.setView(dialogView)
		builder.setPositiveButton(getString(R.string.save), null)
		builder.setNegativeButton(getString(R.string.cancel), null)

		var passwordType: Int = MemoUtils.getLockMethod()
		dialogView.sPassword.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				when (position) {
					0 -> {
						passwordType = 0
						dialogView.llPassword.visibility = View.VISIBLE
						dialogView.llPin.visibility = View.GONE
					}
					1 -> {
						passwordType = 1
						dialogView.llPassword.visibility = View.GONE
						dialogView.llPin.visibility = View.VISIBLE
						dialogView.etPin11.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin12.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
						dialogView.etPin12.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin13.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
						dialogView.etPin13.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin14.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
						dialogView.etPin14.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin21.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
						dialogView.etPin21.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin22.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
						dialogView.etPin22.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin23.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
						dialogView.etPin23.addTextChangedListener(object: TextWatcher {
							override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

							override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
								dialogView.etPin24.requestFocus()
							}

							override fun afterTextChanged(s: Editable) {}
						})
					}
				}
			}

			override fun onNothingSelected(parent: AdapterView<*>?) {}
		}

		val alertDialog: AlertDialog = builder.create()
		alertDialog.setOnShowListener { dialog ->
			val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				when (passwordType) {
					0 -> {
						val password1: String = dialogView.etPassword1.text.toString()
						val password2: String = dialogView.etPassword2.text.toString()
						if (TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2)) {
							if (TextUtils.isEmpty(MemoUtils.getPassword())) {
								Toast.makeText(this, getString(R.string.toast_empty_passwords), Toast.LENGTH_SHORT).show()
							} else {
								Toast.makeText(this, getString(R.string.toast_remove_password), Toast.LENGTH_SHORT).show()
								MemoUtils.setPassword(0, "")
								dialog.dismiss()
							}
						} else if (password1.length < 8) {
							Toast.makeText(this, getString(R.string.toast_short_password), Toast.LENGTH_SHORT).show()
						} else if (password1 != password2) {
							Toast.makeText(this, getString(R.string.toast_different_password), Toast.LENGTH_SHORT).show()
						} else {
							Toast.makeText(this, getString(R.string.toast_save_password), Toast.LENGTH_SHORT).show()
							MemoUtils.setPassword(0, password1)
							isLocked = true
							dialog.dismiss()
							onResume()
						}
					}
					1 -> {
						val password1000: String = dialogView.etPin11.text.toString()
						val password100: String = dialogView.etPin12.text.toString()
						val password10: String = dialogView.etPin13.text.toString()
						val password1: String = dialogView.etPin14.text.toString()
						val password2000: String = dialogView.etPin21.text.toString()
						val password200: String = dialogView.etPin22.text.toString()
						val password20: String = dialogView.etPin23.text.toString()
						val password2: String = dialogView.etPin24.text.toString()
						val pin1: String
						val pin2: String
						if (TextUtils.isEmpty(password1000) || TextUtils.isEmpty(password100) || TextUtils.isEmpty(password10) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2000) || TextUtils.isEmpty(password200) || TextUtils.isEmpty(password20) || TextUtils.isEmpty(password2)) {
							if (TextUtils.isEmpty(MemoUtils.getPassword())) {
								Toast.makeText(this, getString(R.string.toast_empty_passwords), Toast.LENGTH_SHORT).show()
							} else {
								Toast.makeText(this, getString(R.string.toast_remove_password), Toast.LENGTH_SHORT).show()
								MemoUtils.setPassword(0, "")
								dialog.dismiss()
							}
						} else {
							pin1 = password1000 + password100 + password10 + password1
							pin2 = password2000 + password200 + password20 + password2
							if (pin1 != pin2) {
								Toast.makeText(this, getString(R.string.toast_different_password), Toast.LENGTH_SHORT).show()
							} else {
								Toast.makeText(this, getString(R.string.toast_save_password), Toast.LENGTH_SHORT).show()
								MemoUtils.setPassword(1, pin1)
								isLocked = true
								dialog.dismiss()
								onResume()
							}
						}
					}
				}
			}
		}
		alertDialog.show()
	}

	fun login() {
		val resource: Int = R.layout.dialog_login
		val view = this.layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(this)
		builder.setTitle(getString(R.string.login))
		builder.setView(view)
		builder.setPositiveButton(getString(R.string.login), null)
		builder.setNegativeButton(getString(R.string.cancel), null)
		val alertDialog: AlertDialog = builder.create()
		alertDialog.setOnShowListener { dialog ->
			val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				val password: String = view.etPassword.text.toString()
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(this, getString(R.string.toast_empty_password), Toast.LENGTH_SHORT).show()
				} else if (password != MemoUtils.getPassword()) {
					Toast.makeText(this, getString(R.string.toast_incorrect_password), Toast.LENGTH_SHORT).show()
				} else {
					if (!TextUtils.isEmpty(password) && password == MemoUtils.getPassword()) {
						Toast.makeText(this, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show()
						isLocked = false
						onResume()
						dialog.dismiss()
					}
				}
			}
		}
		alertDialog.show()
	}

	fun lockView() {
		if (!TextUtils.isEmpty(MemoUtils.getPassword()) && isLocked) {
			llLocked.visibility = View.VISIBLE
			srlMain.visibility = View.GONE
			fab.visibility = View.GONE
		} else {
			llLocked.visibility = View.GONE
			srlMain.visibility = View.VISIBLE
			fab.visibility = View.VISIBLE
		}
	}

	fun showRefreshing() {
		if (!srlMain.isRefreshing) {
			srlMain.post({ srlMain.isRefreshing = true })
		}
	}

	fun hideRefreshing() {
		if (srlMain.isRefreshing) {
			srlMain.post({ srlMain.isRefreshing = false })
		}
	}

	override fun onRefresh() {
		onResume()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.set_password -> {
				if (TextUtils.isEmpty(MemoUtils.getPassword()) || !isLocked) {
					setPassword()
					return true
				}
				return false
			}
		}
		return super.onOptionsItemSelected(item)
	}
}
