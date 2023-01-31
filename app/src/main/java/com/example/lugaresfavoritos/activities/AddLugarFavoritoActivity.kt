package com.example.lugaresfavoritos.activities

import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lugaresfavoritos.R
import com.example.lugaresfavoritos.database.DatabaseHandler
import com.example.lugaresfavoritos.databinding.ActivityAddLugarFavoritoBinding
import com.example.lugaresfavoritos.models.LugarFavorito
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        registerOnActivityForResult(0)
        registerOnActivityForResult(1)
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.etDate -> {
                DatePickerDialog(
                    this@AddLugarFavoritoActivity,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tvAddImage -> {
                val pictureDialogItens = arrayOf("Selecionar da galeria", "Tirar foto")

                val pictureDialog = AlertDialog.Builder(this)
                    .setTitle("Selecione a ação")
                    .setItems(pictureDialogItens) { dialog, which ->
                        when(which) {
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
            R.id.btnSave -> {
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
                            if(lugarFavoritoDetail != null) lugarFavoritoDetail!!.id else 0,
                            binding.etTitle.text.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            savedLatitude,
                            savedLongitude,
                            savedUriImage!!.path.toString()
                        )

                        val dbHandler = DatabaseHandler(this)

                        if(model!!.id > 0) {
                            val editCount = dbHandler.editLugarFavorito(model)

                            if(editCount > 0)
                                Toast.makeText(this, "Lugar alterado com sucesso!", Toast.LENGTH_SHORT).show()
                            else
                                setResult(Activity.RESULT_OK)
                        } else {
                            val insertCount = dbHandler.addLugarFavorito(model)

                            if(insertCount > 0)
                                Toast.makeText(this, "Lugar inserido com sucesso!", Toast.LENGTH_SHORT).show()
                            else
                                setResult(Activity.RESULT_OK)
                        }

                        finish()
                    }
                }
            }
        }
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

    private fun registerOnActivityForResult(acao: Int) {
        var fluxo = ""

        if(acao == FROM_GALLERY) {
            fluxo = "galeria"
        } else if(acao == FROM_CAMERA) {
            fluxo = "câmera"
        } else {
            return
        }

        val register =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data

                    if(data != null) {

                        try {
                            if(fluxo == "galeria") {
                                val contentUri = data.data
                                var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                                savedUriImage = saveImageToInternalStorage(bitmap)
                                Log.e("Info", savedUriImage?.path.toString())
                                binding.ivPlace.setImageURI(contentUri)
                            } else {
                                val contentBitmap: Bitmap = data.extras!!.get("data") as Bitmap
                                savedUriImage = saveImageToInternalStorage(contentBitmap)
                                Log.e("Info", savedUriImage?.path.toString())
                                binding.ivPlace.setImageBitmap(contentBitmap)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this, "Erro ao obter foto da $fluxo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        if(acao == FROM_GALLERY) {
            galleryImageResultLauncher = register
        } else {
            cameraImageResultLauncher = register
        }
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

    companion object {
        private const val FROM_GALLERY = 0
        private const val FROM_CAMERA = 1
        private const val IMAGES_DIR = "LugaresFavoritosImages"
    }
}