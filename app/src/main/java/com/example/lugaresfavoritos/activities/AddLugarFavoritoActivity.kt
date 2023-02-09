package com.example.lugaresfavoritos.activities

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.lugaresfavoritos.R
import com.example.lugaresfavoritos.database.DatabaseHandler
import com.example.lugaresfavoritos.databinding.ActivityAddLugarFavoritoBinding
import com.example.lugaresfavoritos.models.LugarFavorito
import com.example.lugaresfavoritos.utils.GetAdressFromLatLng
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddLugarFavoritoActivity : AppCompatActivity(), View.OnClickListener {
    private  lateinit var binding: ActivityAddLugarFavoritoBinding

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraImageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var googleMapPlaceResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var savedUriImage: Uri? = null
    private var savedLatitude: Double = 0.0
    private var savedLongitude: Double = 0.0

    private var lugarFavoritoDetail: LugarFavorito? = null

    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLugarFavoritoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        configureGoogleMapPlaces()

        if(intent.hasExtra(MainActivity.LUGAR_FAVORITO_DETAILS)) {
            lugarFavoritoDetail = intent.getParcelableExtra(MainActivity.LUGAR_FAVORITO_DETAILS) as LugarFavorito?

            if(lugarFavoritoDetail != null) {
                supportActionBar?.title = "Editar"

                binding.etTitle.setText(lugarFavoritoDetail!!.title)
                binding.etDescription.setText(lugarFavoritoDetail!!.description)
                binding.etDate.setText(lugarFavoritoDetail!!.date)
                binding.etLocation.setText(lugarFavoritoDetail!!.location)
                savedLatitude = lugarFavoritoDetail!!.latitude
                savedLongitude = lugarFavoritoDetail!!.longitude

                savedUriImage = Uri.parse(lugarFavoritoDetail!!.image)
                binding.ivPlace.setImageURI(savedUriImage)

                binding.btnSave.text = "Atualizar"
            }
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            updateDateInView()
        }

        binding.etDate.setOnClickListener(this)
        binding.etLocation.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.tvCurrentLocation.setOnClickListener(this)

        galleryImageResultLauncher = registerOnActivityForResult(FROM_GALLERY)
        cameraImageResultLauncher = registerOnActivityForResult(FROM_CAMERA)
        googleMapPlaceResultLauncher = registerOnActivityForResult(PLACE_AUTO_COMPLETE)
    }

    private fun configureGoogleMapPlaces() {
        if(!Places.isInitialized()) {
            Places.initialize(this@AddLugarFavoritoActivity,
                resources.getString(R.string.google_maps_api_key))
        }
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.etDate -> {
                showDatePickerDialog(dateSetListener)
            }
            R.id.etLocation -> {
                showGoogleMapPlaces()
            }
            R.id.tvCurrentLocation -> {
                getCurrentUserLocation()
            }
            R.id.tvAddImage -> {
                showGetPictureDialog()
            }
            R.id.btnSave -> {
                save()
            }
        }
    }



    private fun save() {
        when {
            binding.etTitle.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Título é obrigatório", Toast.LENGTH_SHORT).show()
            }
            binding.etDescription.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Descrição é obrigatória", Toast.LENGTH_SHORT).show()
            }
            binding.etDate.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Data é obrigatória", Toast.LENGTH_SHORT).show()
            }
            binding.etLocation.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Localização é obrigatória", Toast.LENGTH_SHORT).show()
            }
            savedUriImage == null -> {
                Toast.makeText(this, "Imagem é obrigatória", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val model = LugarFavorito(
                    if (lugarFavoritoDetail != null) lugarFavoritoDetail!!.id else 0,
                    binding.etTitle.text.toString(),
                    binding.etDescription.text.toString(),
                    binding.etDate.text.toString(),
                    binding.etLocation.text.toString(),
                    savedLatitude,
                    savedLongitude,
                    savedUriImage!!.path.toString()
                )

                val dbHandler = DatabaseHandler(this)

                if (model!!.id > 0) {
                    val editCount = dbHandler.editLugarFavorito(model)

                    if (editCount > 0)
                        Toast.makeText(this, "Lugar alterado com sucesso!", Toast.LENGTH_SHORT)
                            .show()
                    else
                        setResult(RESULT_OK)
                } else {
                    val insertCount = dbHandler.addLugarFavorito(model)

                    if (insertCount > 0)
                        Toast.makeText(this, "Lugar inserido com sucesso!", Toast.LENGTH_SHORT)
                            .show()
                    else
                        setResult(RESULT_OK)
                }

                finish()
            }
        }
    }

    private fun showGetPictureDialog() {
        val pictureDialogItens = arrayOf("Selecionar da galeria", "Tirar foto")

        val pictureDialog = AlertDialog.Builder(this)
            .setTitle("Selecione a ação")
            .setItems(pictureDialogItens) { dialog, which ->
                when (which) {
                    0 -> {
                        choosePhotoFromGallery()
                    }
                    1 -> {
                        choosePhotoFromCamera()
                    }
                }
            }
            .show()
    }

    private fun showGoogleMapPlaces() {
        try {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )

            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
            ).build(this@AddLugarFavoritoActivity)

            googleMapPlaceResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showDatePickerDialog(listener: DatePickerDialog.OnDateSetListener) {
        DatePickerDialog(
            this@AddLugarFavoritoActivity,
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun choosePhotoFromGallery() {
        Dexter
            .withActivity(this)
            .withPermissions(
              READ_EXTERNAL_STORAGE,
              WRITE_EXTERNAL_STORAGE
            )
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryImageResultLauncher.launch(galleryIntent)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            })
            .onSameThread()
            .check()
    }

    private fun choosePhotoFromCamera() {
        Dexter
            .withActivity(this)
            .withPermissions(
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                CAMERA
            )
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraImageResultLauncher.launch(cameraIntent)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            })
            .onSameThread()
            .check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog
            .Builder(this@AddLugarFavoritoActivity)
            .setMessage("Permissões negadas! Você ainda pode permiti-las em Configurações do Sistema.")
            .setPositiveButton("Ir para Configurações")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.etDate.setText(sdf.format(calendar.time).toString())
    }

    private fun registerOnActivityForResult(acao: Int): ActivityResultLauncher<Intent> {
        val register =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data

                    if(data != null) {
                        try {
                            if(acao == FROM_GALLERY) {
                                val contentUri = data.data
                                var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                                savedUriImage = saveImageToInternalStorage(bitmap)
                                Log.e("Info", savedUriImage?.path.toString())
                                binding.ivPlace.setImageURI(contentUri)
                            } else if(acao == FROM_CAMERA) {
                                val contentBitmap: Bitmap = data.extras!!.get("data") as Bitmap
                                savedUriImage = saveImageToInternalStorage(contentBitmap)
                                Log.e("Info", savedUriImage?.path.toString())
                                binding.ivPlace.setImageBitmap(contentBitmap)
                            } else if(acao == PLACE_AUTO_COMPLETE) {
                                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                                binding.etLocation.setText(place.address)
                                savedLatitude = place.latLng!!.latitude
                                savedLongitude = place.latLng!!.longitude
                            }
                        } catch (e: IOException) {
                            var msgErro = ""

                            if(acao == FROM_GALLERY)
                                msgErro = "Erro ao obter foto da galeria"
                            else if(acao == FROM_CAMERA)
                                msgErro = "Erro ao obter foto da câmera"
                            else if(acao == PLACE_AUTO_COMPLETE)
                                msgErro = "Erro ao obter localização"

                            e.printStackTrace()
                            Toast.makeText(this, msgErro, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        return register
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGES_DIR, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    private fun getCurrentUserLocation() {
        if(!isLocationEnabled()) {
            Toast.makeText(this, "Localização não habilitada, favor habilite-a e tente novamente.", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
                  .withPermissions(
                      ACCESS_FINE_LOCATION
                      , ACCESS_COARSE_LOCATION
                  )
                  .withListener(object: MultiplePermissionsListener {
                      @SuppressLint("MissingPermission")
                      override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                          if(report!!.areAllPermissionsGranted()) {
                              fusedLocationProviderClient.lastLocation
                                  .addOnSuccessListener { location : Location? ->

                                      if(location != null) {
                                          savedLatitude = location.latitude
                                          savedLongitude = location.longitude

                                          getLocationFromLatLgn(savedLatitude, savedLongitude)
                                      }
                                  }
                          }
                      }
                      override fun onPermissionRationaleShouldBeShown(
                          permissions: MutableList<PermissionRequest>?,
                          token: PermissionToken?
                      ) {
                          showRationalDialogForPermissions()
                      }
                  })
                  .onSameThread()
                  .check()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback,
            Looper.myLooper()!!
        )
    }

    private fun getLocationFromLatLgn(lat: Double, lng: Double) {
        val addressTask = GetAdressFromLatLng(this@AddLugarFavoritoActivity, lat, lng)
        addressTask.setCustomAddressListener(object: GetAdressFromLatLng.AddressListener {
            override fun onAddressFound(address: String) {
                binding.etLocation.setText(address)
            }

            override fun onError() {
                Log.e("Address:: ", "onError: Um erro ocorreu ao traduzir as coordenadas para o endereço")
            }
        })

        lifecycleScope.launch(Dispatchers.IO) {
            addressTask.launchBackgroundProcessForRequest()
        }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location? = locationResult!!.lastLocation
            savedLatitude = lastLocation!!.latitude
            savedLongitude = lastLocation!!.longitude

            val addressTask = GetAdressFromLatLng(this@AddLugarFavoritoActivity, savedLatitude, savedLongitude)
            addressTask.setCustomAddressListener(object: GetAdressFromLatLng.AddressListener {
                override fun onAddressFound(address: String) {
                    binding.etLocation.setText(address)
                }

                override fun onError() {
                    Log.e("Address:: ", "onError: Um erro ocorreu ao traduzir as coordenadas para o endereço")
                }
            })

            lifecycleScope.launch(Dispatchers.IO) {
                addressTask.launchBackgroundProcessForRequest()
            }
        }
    }

    companion object {
        private const val FROM_GALLERY = 0
        private const val FROM_CAMERA = 1
        private const val IMAGES_DIR = "LugaresFavoritosImages"
        private const val PLACE_AUTO_COMPLETE = 2

    }
}