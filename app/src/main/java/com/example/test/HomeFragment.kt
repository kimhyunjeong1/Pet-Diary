package com.example.test.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.test.PetDatabaseHelper
import com.example.test.R
import com.example.test.ScheduleDatabaseHelper

class HomeFragment : Fragment() {

    private lateinit var petInfoTextView: TextView
    private lateinit var upcomingScheduleTextView: TextView
    private lateinit var healthTipTextView: TextView
    private lateinit var profileButton: Button
    private lateinit var scheduleButton: Button
    private lateinit var petImageView: ImageView
    private lateinit var scheduleDbHelper: ScheduleDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // View 초기화
        petInfoTextView = view.findViewById(R.id.pet_info_text_view)
        upcomingScheduleTextView = view.findViewById(R.id.upcoming_schedule_text_view)
        healthTipTextView = view.findViewById(R.id.health_tip_text_view)
        profileButton = view.findViewById(R.id.profile_button)
        scheduleButton = view.findViewById(R.id.schedule_button)
        petImageView = view.findViewById(R.id.pet_image_view)

        // 데이터베이스 초기화
        scheduleDbHelper = ScheduleDatabaseHelper(requireContext())

        // 데이터 설정
        loadPetInfo()
        loadUpcomingSchedule()
        displayHealthTip()

        // 버튼 클릭 리스너
        profileButton.setOnClickListener {
            // ProfileFragment로 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        scheduleButton.setOnClickListener {
            // ScheduleFragment로 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScheduleFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    // 반려동물 정보 불러오기
    private fun loadPetInfo() {
        // PetDatabaseHelper 인스턴스 생성
        val petDbHelper = PetDatabaseHelper(requireContext())
        val pets = petDbHelper.getAllPets()

        if (pets.isNotEmpty()) {
            // 첫 번째 반려동물 정보 가져오기
            val pet = pets[0]
            val petName = pet[PetDatabaseHelper.COLUMN_NAME] ?: "이름 없음"
            val petBreed = pet[PetDatabaseHelper.COLUMN_BREED] ?: "품종 없음"
            val petBirthday = pet[PetDatabaseHelper.COLUMN_BIRTHDAY] ?: "생일 없음"
            val petPersonality = pet[PetDatabaseHelper.COLUMN_PERSONALITY] ?: "성격 정보 없음"
            val petGender = pet[PetDatabaseHelper.COLUMN_GENDER] ?: "성별 정보 없음"
            val petImageUri = pet[PetDatabaseHelper.COLUMN_IMAGE_URI]

            // TextView에 표시
            petInfoTextView.text = """
                반려동물 정보:
                이름: $petName
                품종: $petBreed
                생일: $petBirthday
                성격: $petPersonality
                성별: $petGender
            """.trimIndent()

            // 이미지 설정
            if (!petImageUri.isNullOrEmpty()) {
                val uri = Uri.parse(petImageUri)
                petImageView.setImageURI(uri)
            } else {
                petImageView.setImageResource(R.drawable.ic_placeholder) // 기본 이미지
            }
        } else {
            // 반려동물이 없을 경우 메시지 표시
            petInfoTextView.text = "등록된 반려동물이 없습니다. 프로필을 추가하세요!"
            petImageView.setImageResource(R.drawable.ic_placeholder) // 기본 이미지
        }
    }

    // 다가오는 일정 불러오기
    private fun loadUpcomingSchedule() {
        val schedules = scheduleDbHelper.getSchedulesWithinWeek()

        if (schedules.isNotEmpty()) {
            val upcomingText = StringBuilder("다가오는 일정:\n")
            for (schedule in schedules) {
                val date = schedule[ScheduleDatabaseHelper.COLUMN_DATE] as String
                val vaccination = if (schedule[ScheduleDatabaseHelper.COLUMN_VACCINATION] as Boolean) "필요" else "없음"
                val checkup = if (schedule[ScheduleDatabaseHelper.COLUMN_CHECKUP] as Boolean) "필요" else "없음"

                // 일정 정보 추가
                upcomingText.append("날짜: $date\n")
                upcomingText.append(" - 예방접종: $vaccination\n")
                upcomingText.append(" - 정기검진: $checkup\n\n")
            }
            upcomingScheduleTextView.text = upcomingText.toString()
        } else {
            upcomingScheduleTextView.text = "다가오는 일정:\n등록된 일정이 없습니다."
        }
    }

    // 건강 팁 표시 (랜덤 선택)
    private fun displayHealthTip() {
        val healthTips = listOf(
            "오늘은 반려동물에게 충분한 물을 제공하세요!",
            "주기적인 산책으로 반려동물의 건강을 지켜주세요.",
            "양질의 사료를 선택하여 건강한 식단을 제공하세요.",
            "반려동물의 귀를 정기적으로 점검하여 감염을 예방하세요."
        )
        healthTipTextView.text = "건강 팁:\n${healthTips.random()}"
    }
}
