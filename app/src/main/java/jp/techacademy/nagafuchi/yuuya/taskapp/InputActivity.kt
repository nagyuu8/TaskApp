package jp.techacademy.nagafuchi.yuuya.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.DatePicker
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class InputActivity : AppCompatActivity() {
    private var mYaer =0
    private var mMonth=0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask:Task?=null

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            mYaer = year
            mMonth = month
            mDay = dayOfMonth
            val dateString =
                mYaer.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            date_button.text = dateString
        }, mYaer, mMonth, mDay)
        datePickerDialog.show()
    }
    private val mOnTimerClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener{_,hour,minute ->
        mHour = hour
        mMinute = minute
        val timeString = String.format("%02d",mHour) + ":"+ String.format("%02d",mMinute)
        times_button.text = timeString
        },mHour,mMinute,false)
        timePickerDialog.show()
    }
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        //ActionBar を設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if(supportActionBar != null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        //UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimerClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        //EXTRA_TASK から Task のidを取得して、idからTaskのインスタンスを取得する。
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK,-1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id",taskId).findFirst()
        realm.close()

        if (mTask ==null){
            //新規作成の場合
            val calender = Calendar.getInstance()
            mYaer = calender.get(Calendar.YEAR)
            mMonth = calender.get(Calendar.MONTH)
            mDay = calender.get(Calendar.DAY_OF_MONTH)
            mHour=calender.get(Calendar.HOUR_OF_DAY)
            mMinute = calender.get(Calendar.MINUTE)
        }else{
            //更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            category_edit_text.setText(mTask!!.category)

            val calender = Calendar.getInstance()
            mYaer = calender.get(Calendar.YEAR)
            mMonth = calender.get(Calendar.MONTH)
            mDay = calender.get(Calendar.DAY_OF_MONTH)
            mHour=calender.get(Calendar.HOUR_OF_DAY)
            mMinute = calender.get(Calendar.MINUTE)

            val dateString = mYaer.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d",mHour) + ":"+ String.format("%02d",mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }
    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()
        if (mTask == null) {
            //新規作成の場合
            mTask = Task()
            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                    if(taskRealmResults.max("id")!=null){
                            taskRealmResults.max("id")!!.toInt() + 1
                        }else{
                        0
                    }
            mTask!!.id = identifier
        }
        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = category_edit_text.text.toString()

        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category = category

        val calendar = GregorianCalendar(mYaer,mMonth,mDay,mHour,mMinute)
        val date = calendar.time
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()


        val resultIntent = Intent(applicationContext,TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK,mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,resultPendingIntent)
    }

}
