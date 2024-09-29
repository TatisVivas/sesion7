package com.example.sesion7.model

import org.json.JSONObject
import java.sql.Date

class MyLocation(val date: Date, val latitude: Double,val longitude: Double) {
    fun toJSON() : JSONObject{
        val json = JSONObject()
        json.put("date",date)
        json.put("latitude",latitude)
        json.put("longitude",longitude)
        return json
    }
}