package com.vdotok.network.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken
import com.vdotok.network.models.Participants

class DataTypeConverter {

    @TypeConverter
    fun fromString(value: String?): ArrayList<String?>? {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromContacts(value: String?): ArrayList<Participants?>? {
        val listType = object : TypeToken<ArrayList<Participants?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromContacts(list: ArrayList<Participants?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

}