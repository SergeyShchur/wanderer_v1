package com.example.wanderer_v1.bottomnav.newentry

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.wanderer_v1.databinding.FragmentNewentryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import java.io.IOException
import java.util.Locale
import java.util.UUID

class NewEntryFragment : Fragment() {
    private lateinit var binding: FragmentNewentryBinding
    private var selectedPoint: Point? = null
    private var placemark: PlacemarkMapObject? = null
    private var selectedUri: Uri? = null

    private var enableScroll = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewentryBinding.inflate(layoutInflater, container, false)

        binding.selectImagesBtn.setOnClickListener {
            selectImage()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapYandex.setOnClickListener {
            openMap()
        }

        binding.addressTv.setOnClickListener {
            if (selectedPoint != null) {
                openMap()
            }
        }
        initializeMap()

        binding.saveEntryBtn.setOnClickListener {
            saveEntryToFirebase()
        }
    }

    private fun saveEntryToFirebase() {
        val entryId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val entryTitle = binding.entryTitle.text.toString().trim()
        val entryDescription = binding.entryDescription.text.toString().trim()
        val latitude = selectedPoint?.latitude ?: return
        val longitude = selectedPoint?.longitude ?: return
        val address = binding.addressTv.text.toString().trim()

        if (entryTitle.isEmpty() || entryDescription.isEmpty() || address.isEmpty() || selectedUri == null) {
            Toast.makeText(
                requireContext(),
                "Заполните поля: название, описание, адрес и выберите изображение",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }

        val entryData = hashMapOf(
            "userId" to userId,
            "title" to entryTitle,
            "description" to entryDescription,
            "latitude" to latitude,
            "longitude" to longitude,
            "address" to address,
            "imageUri" to entryId
        )
        FirebaseDatabase.getInstance().reference.child("Place").child(entryId).setValue(entryData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Место сохранено", Toast.LENGTH_SHORT).show()
                uploadImage(entryId)
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Не удалось сохранить место", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadImage(entryId: String) {
        selectedUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("Place/$entryId")
            storageRef.putFile(uri).addOnSuccessListener {
                Toast.makeText(requireContext(), "Изображение загружено", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedUri = uri
                binding.imageView.setImageURI(uri)
            }
        }
    }

    private fun clearFields() {
        binding.entryTitle.text.clear()
        binding.entryDescription.text.clear()
        binding.addressTv.text = "Выберите адрес"
        selectedPoint = null
        selectedUri = null
        binding.imageView.setImageURI(null)
    }

    //
    // Image select listener
    //

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                val uriList = mutableListOf<Uri>()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        uriList.add(clipData.getItemAt(i).uri)
                    }
                } else {
                    result.data?.data?.let { uriList.add(it) }
                }
            }
        }


    //
    //_____________________________ MAP SETUP ________________________________
    //

    private fun initializeMap() {
        val archangelskPoint = Point(64.539, 40.5168)
        binding.mapYandex.mapWindow.map.move(
            CameraPosition(archangelskPoint, 14.0f, 0.0f, 0.0f)
        )

        binding.mapYandex.mapWindow.map.addInputListener(object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                handleMapTap(point)
            }

            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                handleMapTap(point)
            }
        })
    }

    private fun handleMapTap(point: Point) {
        if (placemark == null) {
            placemark = binding.mapYandex.map.mapObjects.addPlacemark(point)
        } else {
            placemark?.geometry = point
        }
        selectedPoint = point

        AlertDialog.Builder(requireContext())
            .setTitle("Подтвердите местоположение")
            .setMessage("Вы хотите выбрать это место?")
            .setPositiveButton("Да") { dialog, _ ->
                binding.addressTv.text = getAddress(point.latitude, point.longitude)
                saveLocationToDatabase(point)
                hideMap()
                dialog.dismiss()
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openMap() {
        binding.mapYandex.visibility = View.VISIBLE
        binding.addressTv.visibility = View.GONE
    }

    private fun hideMap() {
        binding.mapYandex.visibility = View.GONE
        binding.addressTv.visibility = View.VISIBLE
    }

    private fun saveLocationToDatabase(point: Point) {
        // Implement your database save logic here
        Toast.makeText(
            requireContext(), "Локация сохранена: $"
                    + getAddress(point.latitude, point.longitude), Toast.LENGTH_SHORT
        ).show()
    }

    override fun onStart() {
        super.onStart()
        binding.mapYandex.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        binding.mapYandex.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.mapYandex.mapWindow.map.removeInputListener(object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {}
            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {}
        })
        super.onDestroyView()
    }

    // Метод для получения адреса по координатам
    private fun getAddress(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(requireContext(), Locale.ROOT)
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses!!.isEmpty()) {
                return addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Address not found"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("haveApiKey", true)
    }

    private fun setApiKey(savedInstanceState: Bundle?) {
        val haveApiKey = savedInstanceState?.getBoolean("haveApiKey")
            ?: false // При первом запуске приложения всегда false
        if (!haveApiKey) {
            MapKitFactory.setApiKey(MAPKIT_API_KEY) // API-ключ должен быть задан единожды перед инициализацией MapKitFactory
        }
    }

    companion object {
        const val MAPKIT_API_KEY = "7e508f31-f2d7-4f7a-a230-8cdb05de5369"
        private const val IMAGE_PICK_CODE = 1000
    }

}