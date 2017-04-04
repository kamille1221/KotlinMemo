package com.example.david.kotlinmemo

import io.realm.RealmObject

public open class Memo: RealmObject() {
	public open var title: String = ""
	public open var content: String = ""
	public open var date: Long = 0L
}
