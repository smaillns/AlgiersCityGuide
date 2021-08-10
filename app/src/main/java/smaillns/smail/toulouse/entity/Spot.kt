package smaillns.smail.toulouse.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.io.Serializable


@Entity(tableName = "spots")
data class Spot (
        @PrimaryKey()
            @ColumnInfo(name = "id")
            var id:Int = 1,
        @ColumnInfo(name = "name")
            var name:String = "",
        @ColumnInfo(name = "name_en")
            var name_en:String? = "",
        @ColumnInfo(name = "image")
            var image:String = "",
        @ColumnInfo(name = "lat")
            var lat:Double = 0.0,
        @ColumnInfo(name = "lng")
            var lng:Double = 0.0,
        @ColumnInfo(name = "adress")
            var adress: String = "",
        @ColumnInfo(name = "adress_en")
            var adress_en: String? = "",
        @ColumnInfo(name = "description")
            var description: String = "",
        @ColumnInfo(name = "description_en")
            var description_en: String? = "",
        @ColumnInfo(name = "short_description_fr")
            var short_description_fr:String? = "",
        @ColumnInfo(name = "short_description_en")
            var short_description_en: String? = "",
        @ColumnInfo(name = "phone")
            var phone:String? = "",
        @ColumnInfo(name = "link")
            var link:String? = "",
        @ColumnInfo(name = "distance")
            var distance : Int = 1,
        @ColumnInfo(name = "fb")
            var fb:String? = "",
        @ColumnInfo(name = "mail")
            var mail:String?= "",
        @ColumnInfo(name = "categoryId")
        var categoryId:Int? = 1,
        @ColumnInfo(name = "categoryName")
        var categoryName:String? = "",
        @ColumnInfo(name = "marker")
        var marker:String? = "",
        @ColumnInfo(name = "images")
        var images:ArrayList<String>? = null):ClusterItem, Serializable, Comparable<Spot> {





    fun getIntro(mlanguage: String):String{
        if (mlanguage == "fr")
            { if (short_description_fr != null)
                return short_description_fr!!
            }
        else
            if (short_description_en != null)
                return short_description_en!!
        return ""
    }

    fun getName(mlanguage: String):String{
        if (mlanguage == "fr")
        { if (name != null)
            return name!!
        }
        else
            if (name_en != null)
                return name_en!!
        return ""
    }



    fun getAddress(mlanguage:String):String{
        if (mlanguage == "fr") {
            if (adress != null)
                return adress
        }
        else
            if (adress_en != null)
                return adress_en!!

        return ""
    }


    fun setDistancee(distance : Int){
        this.distance = distance
    }

    fun getLatitude():Double{
        return this.lat
    }
    fun getLongitude():Double{
        return this.lng
    }

    override fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }

    override fun compareTo(spot: Spot): Int {
        return if (this.distance < spot.distance)
            -1
        else if (this.distance > spot.distance)
            1
        else
            0
    }
}