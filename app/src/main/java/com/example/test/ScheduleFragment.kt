package com.example.test.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.test.R
import com.example.test.ScheduleDatabaseHelper


class ScheduleFragment : Fragment() {

    private lateinit var dbHelper: ScheduleDatabaseHelper
    private lateinit var calendarView: CalendarView
    private lateinit var addScheduleButton: Button
    private lateinit var deleteScheduleButton: Button
    private lateinit var scheduleInfoTextView: TextView
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        // 데이터베이스 초기화
        dbHelper = ScheduleDatabaseHelper(requireContext())

        // View 초기화
        calendarView = view.findViewById(R.id.calendar_view)
        addScheduleButton = view.findViewById(R.id.add_schedule_button)
        deleteScheduleButton = view.findViewById(R.id.delete_schedule_button)
        scheduleInfoTextView = view.findViewById(R.id.schedule_info_text_view)

        // 초기 선택 날짜 설정 (현재 날짜)
        selectedDate = getDateFromMillis(calendarView.date)

        // 캘린더에서 날짜 선택 이벤트 처리
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            loadScheduleForDate(selectedDate)
        }

        // 일정 추가 버튼 클릭 리스너
        addScheduleButton.setOnClickListener {
            showChecklistDialog()
        }

        // 일정 삭제 버튼 클릭 리스너
        deleteScheduleButton.setOnClickListener {
            deleteScheduleForDate(selectedDate)
        }

        // 현재 날짜의 일정 불러오기
        loadScheduleForDate(selectedDate)

        return view
    }

    // 체크리스트 다이얼로그 표시
    private fun showChecklistDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_checklist, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("일정 추가 - $selectedDate")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                saveSchedule(dialogView)
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    // 일정 저장
    private fun saveSchedule(dialogView: View) {
        val vaccination = dialogView.findViewById<CheckBox>(R.id.vaccination_check_box).isChecked
        val checkup = dialogView.findViewById<CheckBox>(R.id.checkup_check_box).isChecked
        val bath = dialogView.findViewById<CheckBox>(R.id.bath_check_box).isChecked
        val medication = dialogView.findViewById<CheckBox>(R.id.medication_check_box).isChecked
        val deworming = dialogView.findViewById<CheckBox>(R.id.deworming_check_box).isChecked
        val feeding = dialogView.findViewById<EditText>(R.id.feeding_edit_text).text.toString()
        val toilet = dialogView.findViewById<EditText>(R.id.toilet_edit_text).text.toString()
        val activity = dialogView.findViewById<EditText>(R.id.activity_edit_text).text.toString()

        val result = dbHelper.saveSchedule(
            selectedDate, vaccination, checkup, bath, medication, deworming, feeding, toilet, activity
        )
        if (result > 0) {
            Toast.makeText(requireContext(), "일정 저장 완료!", Toast.LENGTH_SHORT).show()
            loadScheduleForDate(selectedDate)
        } else {
            Toast.makeText(requireContext(), "일정 저장 실패!", Toast.LENGTH_SHORT).show()
        }
    }

    // 특정 날짜의 일정 불러오기
    private fun loadScheduleForDate(date: String) {
        val schedule = dbHelper.getScheduleByDate(date)
        if (schedule != null) {
            val vaccination = if (schedule[ScheduleDatabaseHelper.COLUMN_VACCINATION] as Boolean) "필요" else "없음"
            val checkup = if (schedule[ScheduleDatabaseHelper.COLUMN_CHECKUP] as Boolean) "필요" else "없음"
            val bath = if (schedule[ScheduleDatabaseHelper.COLUMN_BATH] as Boolean) "필요" else "없음"
            val feeding = schedule[ScheduleDatabaseHelper.COLUMN_FEEDING] as String
            val activity = schedule[ScheduleDatabaseHelper.COLUMN_ACTIVITY] as String

            scheduleInfoTextView.text = """
                날짜: $date
                예방접종: $vaccination
                정기검진: $checkup
                목욕관리: $bath
                식사관리: $feeding
                활동관리: $activity
            """.trimIndent()
        } else {
            scheduleInfoTextView.text = "스케줄 정보가 없습니다."
        }
    }

    // 특정 날짜의 일정 삭제
    private fun deleteScheduleForDate(date: String) {
        val result = dbHelper.deleteScheduleByDate(date)
        if (result > 0) {
            Toast.makeText(requireContext(), "일정 삭제 완료!", Toast.LENGTH_SHORT).show()
            loadScheduleForDate(date)
        } else {
            Toast.makeText(requireContext(), "삭제할 일정이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 밀리초 시간을 yyyy-MM-dd 형식으로 변환
    private fun getDateFromMillis(millis: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = millis
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
}
