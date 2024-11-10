package com.example.a7labakotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VehicleAdapter(
    private val vehicleList: MutableList<Vehicle>, // Список транспортных средств
    private val onVehicleClick: (Int) -> Unit // Лямбда-функция для обработки нажатия на элемент
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION //выбранная позиция элемента

    // ViewHolder для удерживания элементов представления транспортного средства
    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val brand: TextView = itemView.findViewById(R.id.tvBrand)
        val model: TextView = itemView.findViewById(R.id.tvModel)
        val year: TextView = itemView.findViewById(R.id.tvYear)
        val type: TextView = itemView.findViewById(R.id.tvType)

        //слушатель нажатий на элемент (позиция элемента)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVehicleClick(position)
                }
            }
        }
    }

    // Метод для создания нового ViewHolder для элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.vehicle_item, parent, false)
        return VehicleViewHolder(itemView)
    }

    // Метод для привязки данных к ViewHolder
    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicleList[position]
        holder.brand.text = "Марка: ${vehicle.brand}"
        holder.model.text = "Модель: ${vehicle.model}"
        holder.year.text = "Год: ${vehicle.year}"
        holder.type.text = "Тип: ${vehicle.type}"

        holder.itemView.setBackgroundColor(
            if (position == selectedItemPosition) {
                holder.itemView.context.getColor(R.color.selected_item)
            } else {
                holder.itemView.context.getColor(R.color.default_item)
            }
        )
    }

    override fun getItemCount(): Int {
        return vehicleList.size
    }

    // Метод для установки выбранного элемента
    fun setSelectedItem(position: Int) {
        val previousItemPosition = selectedItemPosition
        selectedItemPosition = position
        notifyItemChanged(previousItemPosition) // Обновляем предыдущий выбранный элемент
        notifyItemChanged(selectedItemPosition) // Обновляем текущий выбранный элемент
    }

    // Логика изменения элемента
    fun editItem(position: Int, updatedVehicle: Vehicle) {
        vehicleList[position] = updatedVehicle // Заменяем элемент на обновленный
        notifyItemChanged(position) // Обновляем измененный элемент
    }
}