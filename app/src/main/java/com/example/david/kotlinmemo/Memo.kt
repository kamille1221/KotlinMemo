package com.example.david.kotlinmemo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Memo: RealmObject() {
	@PrimaryKey open var id: Int = -1
	open var title: String = ""
	open var content: String = ""
	open var date: Long = 0L
}
