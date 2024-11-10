package com.example.a7labakotlin

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etBrand: EditText
    private lateinit var etModel: EditText
    private lateinit var etYear: EditText
    private lateinit var rgVehicleType: RadioGroup
    private lateinit var btnSubmit: Button
    private lateinit var gestureDetector: GestureDetectorCompat
    private val vehicleInfoList = mutableListOf<Vehicle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Главная"

        etBrand = findViewById(R.id.etBrand)
        etModel = findViewById(R.id.etModel)
        etYear = findViewById(R.id.etYear)
        rgVehicleType = findViewById(R.id.rgVehicleType)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Получение данных для редактирования, если они были переданы из другого Activity
        val vehicleToEdit = intent.getParcelableExtra<Vehicle>("vehicleToEdit")
        val isEditMode = vehicleToEdit != null //Если данные переданы, то режим включаем редактирования

        if (isEditMode) {
            etBrand.setText(vehicleToEdit?.brand)
            etModel.setText(vehicleToEdit?.model)
            etYear.setText(vehicleToEdit?.year)

            when (vehicleToEdit?.type) {
                "Седан" -> rgVehicleType.check(R.id.rbSedan)
                "Универсал" -> rgVehicleType.check(R.id.rbWagon)
                "Внедорожник" -> rgVehicleType.check(R.id.rbSuv)
            }
        }

        //Обработчик для кнопки отправки
        btnSubmit.setOnClickListener {
            submitForm(isEditMode, vehicleToEdit)
        }

        // Настройка свайпа вправо для отправки данных через GestureDetector
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener(isEditMode, vehicleToEdit))

        // Применяем свайп к кнопке отправки
        btnSubmit.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun submitForm(isEditMode: Boolean, vehicleToEdit: Vehicle?) {
        val brand = etBrand.text.toString().trim()
        val model = etModel.text.toString().trim()
        val year = etYear.text.toString().trim()

        if (brand.isEmpty() || model.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val vehicleType = when (rgVehicleType.checkedRadioButtonId) {
            R.id.rbSedan -> "Седан"
            R.id.rbWagon -> "Универсал"
            R.id.rbSuv -> "Внедорожник"
            else -> "Неизвестно"
        }

        // Создаем объект Vehicle с введенными данными
        val updatedVehicle = Vehicle(brand, model, year, vehicleType)

        if (isEditMode) {
            // Возвращаем обновленный объект обратно в InfoActivity
            val resultIntent = Intent()
            resultIntent.putExtra("updatedVehicle", updatedVehicle)
            resultIntent.putExtra("editPosition", intent.getIntExtra("editPosition", -1))
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            val storageManager = StorageManager()

            // Загружаем существующие данные из SharedPreferences
            val existingVehicles = storageManager.getFromSharedPreferences(this).toMutableList()

            // Добавляем новый транспорт в список
            existingVehicles.add(updatedVehicle)

            // Сохраняем обновленный список в SharedPreferences
            storageManager.saveToSharedPreferences(this, existingVehicles)

            // Очищаем поля формы
            etBrand.text.clear()
            etModel.text.clear()
            etYear.text.clear()
            rgVehicleType.clearCheck()
        }
    }

    //Обработчик свайпов
    private inner class SwipeGestureListener(val isEditMode: Boolean, val vehicleToEdit: Vehicle?) :
        GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2?.x?.minus(e1!!.x) ?: 0.0f
            val diffY = e2?.y?.minus(e1!!.y) ?: 0.0f
            if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    // Свайп вправо
                    submitForm(isEditMode, vehicleToEdit)
                }
                return true
            }
            return false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pageMain -> {
                return true // Оставляем на MainActivity
            }
            R.id.pageInfo -> {
                // Переход к InfoActivity с передачей списка транспорта
                val intent = Intent(this, InfoActivity::class.java)
                intent.putParcelableArrayListExtra("vehicleList", ArrayList(vehicleInfoList))
                startActivity(intent)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}