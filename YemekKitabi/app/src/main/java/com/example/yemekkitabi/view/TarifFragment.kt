package com.example.yemekkitabi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.yemekkitabi.databinding.FragmentTarifBinding
import com.example.yemekkitabi.model.Tarif
import com.example.yemekkitabi.roomdb.TarifDAO
import com.example.yemekkitabi.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permisionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel: Uri? = null
    private var secilenBitmap: Bitmap? = null
    private val mDisposable = CompositeDisposable()
    private lateinit var db : TarifDatabase
    private lateinit var tarifDao: TarifDAO
    private var secilenTarif : Tarif? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        db = Room.databaseBuilder(requireContext(), TarifDatabase::class.java, "Tarifler").build()
        tarifDao = db.tarifDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gRselSec.setOnClickListener{gorselSec(it)}
        binding.buttonSil.setOnClickListener{sil(it)}
        binding.buttonKaydet.setOnClickListener{kaydet(it)}
        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (bilgi=="yeni"){
                secilenTarif = null
                binding.buttonSil.isEnabled= false
                binding.buttonKaydet.isEnabled= true
            }else{
                binding.buttonSil.isEnabled= true
                binding.buttonKaydet.isEnabled= false
                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(tarifDao.findById(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse))

            }

        }

    }

    private fun handleResponse(tarif : Tarif){
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.gRselSec.setImageBitmap(bitmap)
        secilenTarif=tarif

    }

    fun kaydet(view: View){
        val isim = binding.isimText.text.toString()
        val malzeme = binding.malzemeText.text.toString()
        if (secilenBitmap!=null){
            val kucukBitmap =  kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim,malzeme,byteDizisi)

                mDisposable.add(tarifDao.insert(tarif)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponeInsert)
                )
        }
    }
    private fun handleResponeInsert(){

        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }


    fun sil(view: View){
        if (secilenTarif!=null){
                mDisposable.add(tarifDao.delete(secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponeInsert)
                )
    }


    }
    fun gorselSec(view: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"İzin gerekli",Snackbar.LENGTH_LONG).show()
                    permisionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }else{
                    permisionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"İzin gerekli",Snackbar.LENGTH_LONG).show()
                    permisionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }else{
                    permisionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }
    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data


                if(intentFromResult != null){
                    secilenGorsel = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >= 28 ){
                            val source = ImageDecoder.createSource(requireContext().contentResolver,secilenGorsel!!)
                            secilenBitmap= ImageDecoder.decodeBitmap(source)
                            binding.gRselSec.setImageBitmap(secilenBitmap)

                        } else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,secilenGorsel)
                            binding.gRselSec.setImageBitmap(secilenBitmap)
                        }
                    }catch (e: Exception){
                        println(e.localizedMessage)
                    }
                }
            }
        }
        permisionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }else {
                Toast.makeText(requireContext(), "İzin verilmedi", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun kucukBitmapOlustur(kullaniciSectigiBitmap: Bitmap, maximumBoyut : Int) : Bitmap{
        var width= kullaniciSectigiBitmap.width
        var height = kullaniciSectigiBitmap.height
        val bitmapOran : Double =  width.toDouble () /height.toDouble()
        if (bitmapOran > 1){
            width = maximumBoyut
            val kisaltilmisYukseklik = width/bitmapOran
            height = kisaltilmisYukseklik.toInt()
        }else{
            height = maximumBoyut
            val kisaltilmisGenislik = width*bitmapOran
            width = kisaltilmisGenislik.toInt()
        }

    return Bitmap.createScaledBitmap(kullaniciSectigiBitmap,width,height,true)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        mDisposable.clear()
    }
}