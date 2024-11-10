package com.example.a7labakotlin
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class StorageManager {

    private val sharedPreferencesName = "vehicle_prefs"
    private val vehiclesKey = "vehicle_list"

    // Сохранение списка транспортных средств в SharedPreferences
    fun saveToSharedPreferences(context: Context, vehicleList: List<Vehicle>) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Преобразуем список в JSON строку
        val gson = Gson()
        val vehicleListJson = gson.toJson(vehicleList)

        // Сохраняем строку JSON в SharedPreferences
        editor.putString(vehiclesKey, vehicleListJson)
        editor.apply()
    }

    // Получение списка транспортных средств из SharedPreferences
    fun getFromSharedPreferences(context: Context): List<Vehicle> {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

        // Получаем строку JSON
        val vehicleListJson = sharedPreferences.getString(vehiclesKey, null)

        // Если строка JSON не пуста, преобразуем ее обратно в список объектов Vehicle
        return if (vehicleListJson != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Vehicle>>() {}.type
            gson.fromJson(vehicleListJson, type)
        } else {
            emptyList()  // Возвращаем пустой список, если данных нет
        }
    }


//    // Сохранение списка транспортных средств в файл XLS
//    fun saveToXls(context: Context, vehicleList: List<Vehicle>, outputUri: Uri) {
//        try {
//            val workbook: Workbook = HSSFWorkbook()
//            val sheet = workbook.createSheet("Vehicles")
//
//            // Записываем заголовки колонок
//            val headerRow = sheet.createRow(0)
//            headerRow.createCell(0).setCellValue("Марка")
//            headerRow.createCell(1).setCellValue("Модель")
//            headerRow.createCell(2).setCellValue("Год")
//            headerRow.createCell(3).setCellValue("Тип")
//
//            // Заполняем данные из списка транспорта
//            for ((index, vehicle) in vehicleList.withIndex()) {
//                val row = sheet.createRow(index + 1)
//                row.createCell(0).setCellValue(vehicle.brand)
//                row.createCell(1).setCellValue(vehicle.model)
//                row.createCell(2).setCellValue(vehicle.year)
//                row.createCell(3).setCellValue(vehicle.type)
//            }
//
//            // Сохраняем файл по указанному URI
//            context.contentResolver.openOutputStream(outputUri).use { outputStream ->
//                workbook.write(outputStream)
//                workbook.close()
//            }
//
//            Toast.makeText(context, "Данные успешно сохранены в XLS", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(context, "Ошибка при сохранении в XLS", Toast.LENGTH_SHORT).show()
//        }
//    }

    // Сохранение списка транспортных средств в файл XLS через MediaStore
    fun saveToXlsInMediaStore(context: Context, vehicleList: List<Vehicle>, fileName: String) {
        val resolver: ContentResolver = context.contentResolver

        // Задаем параметры для сохранения файла в MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Vehicles")
        }

        // Создаем URI для нового файла
        val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri == null) {
            Toast.makeText(context, "Не удалось создать файл", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Создаем новую книгу и лист
            val workbook: Workbook = HSSFWorkbook()
            val sheet = workbook.createSheet("Vehicles")

            // Записываем заголовки колонок
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Марка")
            headerRow.createCell(1).setCellValue("Модель")
            headerRow.createCell(2).setCellValue("Год")
            headerRow.createCell(3).setCellValue("Тип")

            // Заполняем данные из списка транспортных средств
            for ((index, vehicle) in vehicleList.withIndex()) {
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(vehicle.brand)
                row.createCell(1).setCellValue(vehicle.model)
                row.createCell(2).setCellValue(vehicle.year)
                row.createCell(3).setCellValue(vehicle.type)
            }

            // Открываем выходной поток и записываем данные
            resolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
                workbook.close()
                Toast.makeText(context, "Данные успешно сохранены в XLS", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка при сохранении в MediaStore", Toast.LENGTH_SHORT).show()
        }
    }


    // Загрузка списка транспортных средств из файла XLS
    fun loadFromXls(context: Context, inputUri: Uri): List<Vehicle> {
        val vehicleList = mutableListOf<Vehicle>()
        try {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                val workbook = HSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                // Чтение данных по строкам (пропускаем заголовок)
                for (row in sheet.drop(1)) {
                    val brand = row.getCell(0).stringCellValue
                    val model = row.getCell(1).stringCellValue
                    val year = row.getCell(2).stringCellValue
                    val type = row.getCell(3).stringCellValue

                    val vehicle = Vehicle(brand, model, year, type)
                    vehicleList.add(vehicle)
                }
                workbook.close()
            }
            Toast.makeText(context, "Данные успешно загружены из XLS", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка при загрузке из XLS", Toast.LENGTH_SHORT).show()
        }
        return vehicleList
    }
}

