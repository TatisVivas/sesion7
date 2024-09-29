package com.example.sesion7

import android.content.Context
import android.content.SharedPreferences.Editor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.sesion7.databinding.ActivityGoogleMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions

class GoogleMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityGoogleMapsBinding

    //sensores de luz
    private lateinit var sensorManager : SensorManager
    private var ligthSensor: Sensor? = null
    private lateinit var sensorEventListener: SensorEventListener

    //direcciones y Geocoder
    private lateinit var geocoder: Geocoder


    private var darkSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGoogleMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager //castear porque necesito el tipo de sensor manager
        ligthSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorEventListener = createSensorEventListener()

        geocoder = Geocoder(baseContext)

        binding.address.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                val address = binding.address.text.toString()
                val location = findLocation(address)
                if(location!=null) {
                    mMap.clear()
                    drawMarker(location, address, R.drawable.baseline_place_24)
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

                }
            }
            return@setOnEditorActionListener true
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun createSensorEventListener(): SensorEventListener {
        val listener : SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                //se autopregunta su estructura
                //BASECONTEXT YA NO SIRVE PARA INTROSPECCIÓN DE OBJETOS
                if(this@GoogleMapsActivity::mMap.isInitialized) {

                    if (ligthSensor != null) {
                        if (event != null) {
                            if (event.values[0] < 5000) {
                                //dark
                                mMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                        baseContext,
                                        R.raw.map_dark
                                    )
                                )

                            } else {
                                mMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                        baseContext,
                                        R.raw.map_light
                                    )
                                )
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

        }
        return listener

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val bogota = LatLng(4.63, -74.10)
        mMap.addMarker(MarkerOptions().position(bogota).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bogota))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 15f))
        mMap.uiSettings.isZoomControlsEnabled = true
        val marker = mMap.addMarker(MarkerOptions().position(bogota).title("Marker in Bogota"))
        mMap.addMarker(MarkerOptions().position(bogota)
            .title("Pontificia Universidad Javeriana")
            .snippet("Población: 8081000")
            .alpha(0.5f))
        drawMarker(LatLng(4.62894444, -74.06485), "PUJ", R.drawable.baseline_cell_tower_24)

        mMap.setOnMapLongClickListener {
            val address = this.findAddress(it)
            drawMarker(it,address,R.drawable.baseline_place_24)

        }

    }
    fun drawMarker(location : LatLng, description : String?, icon: Int){
        val addressMarker = mMap.addMarker(MarkerOptions().position(location).icon(bitmapDescriptorFromVector(this,
            icon)))!!
        if(description!=null){
            addressMarker.title=description
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
    }

    fun bitmapDescriptorFromVector(context : Context, vectorResId : Int) : BitmapDescriptor {
        val vectorDrawable : Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    override fun onResume() {
        super.onResume()
        ligthSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun findAddress (location : LatLng):String?{
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2, /*Geocoder.GeocodeListener {  }*/)
        if(addresses != null && !addresses.isEmpty()){
            val addr = addresses.get(0)
            val locname = addr.getAddressLine(0)
            return locname
        }
        return null
    }
    fun findLocation(address : String):LatLng?{
        val addresses = geocoder.getFromLocationName(address, 2)
        if(addresses != null && !addresses.isEmpty()){
            val addr = addresses.get(0)
            val location = LatLng(addr.latitude, addr.
            longitude)
            return location
        }
        return null
    }

}

