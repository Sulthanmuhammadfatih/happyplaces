package com.example.happyplaces

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.icu.lang.UProperty.NAME
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.CalendarContract.Calendars.NAME
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_places.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlacesActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mHappyPlaceDetails : HappyPlaceModel? = null
    private var happyPlaceID = 0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    companion object {
        private const val GALLERY = 1
        private const val Camera = 2
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3

        private const val IMAGE_DIRECTORY = "HappyPlacesImage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_places)

        supportActionBar?.setTitle("Edit Happy Place")
        mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAIL)

        updateDateInView()

        setSupportActionBar(toolbar_add)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add.setNavigationOnClickListener {
            onBackPressed()
        }

        if (!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.google_maps_key))
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)


            updateDateInView()
        }


        mHappyPlaceDetails?.let {
            happyPlaceID = it.id
            saveImageToInternalStorage = Uri.parse(it.image)
            iv_add.setImageURI(saveImageToInternalStorage)
            edt_add.setText(it.title)
            edt_decs.setText(it.description)
            edt_date.setText(it.date)
            edt_located.setText(it.location)
            mLatitude = it.latitude
            mLongitude = it.longitude

            btn_save.text = "UPDATE"
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        edt_date.setOnClickListener(this)
        tv_add_img.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        edt_located.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)
    }





    private fun isLocationEnabled(): Boolean {
        val locationManager : LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }





    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        edt_date.setText(sdf.format(cal.time).toString())
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.edt_date -> {
                DatePickerDialog(
                    this@AddHappyPlacesActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()

            }



            R.id.tv_add_img -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select Photo From Galery",
                    "Capture photo from camera"
                )
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGalery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.btn_save -> {
                when {
                    edt_add.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "please enter title", Toast.LENGTH_SHORT).show()
                    }

                    edt_decs.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter decsription", Toast.LENGTH_SHORT).show()
                    }

                    edt_located.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Plase enter location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                        val happyPlacesModel = HappyPlaceModel(
                                happyPlaceID,
                            edt_add.text.toString(),
                            saveImageToInternalStorage.toString(),
                            edt_decs.text.toString(),
                            edt_date.text.toString(),
                            edt_located.text.toString(),
                            mLongitude,
                            mLatitude

                        )

                        val dbHandler = DataBaseHandler(this)

                        if (mHappyPlaceDetails == null) {
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlacesModel)
                            if (addHappyPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val updateHappyPlace = dbHandler.updateHappyPLace(happyPlacesModel)
                            if (updateHappyPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }

                    }
                }
            }


            R.id.edt_located -> {
                try {
                    val fields = listOf(
                            Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            R.id.tv_select_current_location -> {
                if (isLocationEnabled()) {
                    Toast.makeText(this, "Your location provider is turned off. Please turn is on", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION

                            )  
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {
                                    requestNewLocationData()
                                    Toast.makeText(this@AddHappyPlacesActivity, "Location permission is granted . Now you can request for a current location",Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permission: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }

                            private fun showRationalDialogForPermissions() {
                            }
                        }).onSameThread()
                        .check()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation : Location = locationResult!!.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLatitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")


            val addressTask = GetAddresFromLatLng(this@AddHappyPlacesActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object : GetAddresFromLatLng.AddressListener {


                override fun onAddresFound(address: String) {
                    Log.e("address::", "" + address)
                    edt_located.setText(address)
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong")
                }


            })

            addressTask
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) {
                    val galleryintent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryintent, Camera)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationalDialogPermission()

            }
             fun showRationalDialogPermission() {

                AlertDialog.Builder(this@AddHappyPlacesActivity).setMessage("" +  "It Looks like you have turned off permission required" +
                        "for this feature. It can be enabled under the" + "Application settings"
                ).setPositiveButton("GO TO SETTINGS") {_,_ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("Package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }.setNegativeButton("CANCEL") { dialog, which ->  dialog.dismiss()
                }.show()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentUri = data.data
                    try {
                        val selectonImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectonImageBitmap)
                        Log.e("save Image:", "path :: $saveImageToInternalStorage")
                        iv_add.setImageBitmap(selectonImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddHappyPlacesActivity,
                            "Failed to load the image from",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else if (requestCode == Camera) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("save Image:", "path :: $saveImageToInternalStorage")
                iv_add.setImageBitmap(thumbnail)
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                val place : Place = Autocomplete.getPlaceFromIntent(data!!)
                edt_located.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "cancelled")
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap?): Uri {

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()

            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)


    }


    private fun choosePhotoFromGalery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) {

                    val galleryintent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    startActivityForResult(galleryintent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: MutableList<PermissionRequest>,
                Token: PermissionToken
            ) {
                showRationalDialogForPermission()

            }

            private fun showRationalDialogForPermission() {
                AlertDialog.Builder(this@AddHappyPlacesActivity)
                    .setMessage("" + "it loooks like you have turned off permission required" + "for this feature. it can be enabled under the " + "application ")
                    .setPositiveButton("GO TO SETTINGS") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("Packege", packageName, null)
                            intent.data = uri
                            startActivity(intent)

                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }
                    }.setNegativeButton("CANCEL") { dialog, which ->
                        dialog.dismiss()
                    }.show()
            }
        }).onSameThread().check()
    }


}