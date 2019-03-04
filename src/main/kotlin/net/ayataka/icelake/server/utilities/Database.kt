package net.ayataka.icelake.server.utilities

import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

object Database {
    private val database = KMongo.createClient().getDatabase("icelake")

    private val actions = database.getCollection<ActionData>("actions")
    private val shops = database.getCollection<ShopData>("shops")
    private val areas = database.getCollection<AreaData>("areas")

    fun getAction(code: String): ActionData? {
        return actions.findOne(ActionData::code eq code)
    }

    fun addAction(code: String, type: String, name: String) {
        removeAction(code)
        actions.insertOne(ActionData(code, type, name))
    }

    fun removeAction(code: String) {
        actions.deleteMany(ActionData::code eq code)
    }

    fun getActions(): List<ActionData> {
        return actions.find().toMutableList()
    }

    fun addShop(code: String, name: String) {
        removeShop(code)
        shops.insertOne(ShopData(code, name))
    }

    fun removeShop(code: String) {
        shops.deleteMany(ShopData::code eq code)
    }

    fun getShops(): List<ShopData> {
        return shops.find().toMutableList()
    }

    fun addArea(code: String, name: String) {
        removeArea(code)
        areas.insertOne(AreaData(code, name))
    }

    fun removeArea(code: String) {
        areas.deleteMany(AreaData::code eq code)
    }

    fun getAreas(): List<AreaData> {
        return areas.find().toMutableList()
    }
}