package jp.techacademy.nagafuchi.yuuya.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

class Category: RealmObject(),Serializable {
    var categoryContent:String = "" //カテゴリー
    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}