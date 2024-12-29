package com.example.test.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.test.R
import com.example.test.PetDatabaseHelper

class ProfileFragment : Fragment() {

    private lateinit var petImageView: ImageView
    private lateinit var petNameEditText: EditText
    private lateinit var petBreedEditText: EditText
    private lateinit var petBirthdayEditText: EditText
    private lateinit var petPersonalityEditText: EditText
    private lateinit var petGenderGroup: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var dbHelper: PetDatabaseHelper
    private var selectedImageUri: Uri? = null // 선택한 이미지 URI 저장
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 데이터베이스 초기화
        dbHelper = PetDatabaseHelper(requireContext())

        // View 초기화
        petImageView = view.findViewById(R.id.pet_image)
        petNameEditText = view.findViewById(R.id.pet_name)
        petBreedEditText = view.findViewById(R.id.pet_breed)
        petBirthdayEditText = view.findViewById(R.id.pet_birthday)
        petPersonalityEditText = view.findViewById(R.id.pet_personality)
        petGenderGroup = view.findViewById(R.id.pet_gender_group)
        saveButton = view.findViewById(R.id.save_button)

        // 이미지 업로드 버튼 클릭 리스너
        val uploadButton: Button = view.findViewById(R.id.upload_image_button)
        uploadButton.setOnClickListener {
            openGallery()
        }

        // 저장 버튼 클릭 리스너
        saveButton.setOnClickListener {
            savePetInfo()
        }

        return view
    }

    // 갤러리를 열어 이미지를 선택하도록 요청
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // 선택한 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data // 선택한 이미지 URI 저장
            selectedImageUri?.let { uri ->
                petImageView.setImageURI(uri) // 이미지뷰에 이미지 설정
            }
        }
    }

    // 반려동물 정보 저장
    private fun savePetInfo() {
        val petName = petNameEditText.text.toString()
        val petBreed = petBreedEditText.text.toString()
        val petBirthday = petBirthdayEditText.text.toString()
        val petPersonality = petPersonalityEditText.text.toString()

        // 선택된 성별 확인
        val selectedGenderId = petGenderGroup.checkedRadioButtonId
        val petGender = if (selectedGenderId != -1) {
            view?.findViewById<RadioButton>(selectedGenderId)?.text.toString()
        } else {
            null
        }

        if (petName.isEmpty() || petBreed.isEmpty() || petBirthday.isEmpty() || petPersonality.isEmpty() || petGender.isNullOrEmpty() || selectedImageUri == null) {
            Toast.makeText(requireContext(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 데이터베이스에 저장
        val id = dbHelper.insertPet(
            name = petName,
            breed = petBreed,
            birthday = petBirthday,
            personality = petPersonality,
            gender = petGender,
            imageUri = selectedImageUri.toString() // 이미지 URI 저장
        )

        if (id > 0) {
            Toast.makeText(requireContext(), "저장 완료!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "저장 실패!", Toast.LENGTH_SHORT).show()
        }
    }
}
