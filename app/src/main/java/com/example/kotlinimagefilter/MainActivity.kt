package com.example.kotlinimagefilter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.kotlinimagefilter.Adapter.ViewPagerAdapter
import com.example.kotlinimagefilter.Interface.EditImageFragmentListener
import com.example.kotlinimagefilter.Interface.FiltersListFragmentListener
import com.example.kotlinimagefilter.Utils.BitmapUtils
import com.example.kotlinimagefilter.Utils.NonSwipeableViewPager
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), FiltersListFragmentListener, EditImageFragmentListener {

    internal var originalImage: Bitmap?=null
    internal lateinit var filteredImage:Bitmap
    internal lateinit var finalImage:Bitmap

    internal lateinit var filtersListFragment:FiltersListFragment
    internal lateinit var editImageFragment: EditImageFragment

    internal var brightnessFinal = 0
    internal var saturationFinal = 1.0f
    internal var contrastFinal = 1.0f

    val SELECT_GALLARY_PERMISSION = 1000

    init {
        System.loadLibrary("NativeImageProcessor")
    }


    object Main {
        val IMAGE_NAME = "flash.jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set Toolbar
        setSupportActionBar(activity_main_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Instagram Filter"

        loadImage()
        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewpager: NonSwipeableViewPager?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        // add filter list fragment
        filtersListFragment = FiltersListFragment()
        filtersListFragment.setListener(this)

        // add edit image fragment
        editImageFragment = EditImageFragment()
        editImageFragment.setListener(this)

        adapter.addFragment(filtersListFragment, "FILTERS")
        adapter.addFragment(editImageFragment, "EDIT")
        viewpager!!.adapter=adapter
    }

    private fun loadImage() {
        originalImage = BitmapUtils.getBitmapFromAssets(this, Main.IMAGE_NAME, 300, 300)
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.setImageBitmap(originalImage)
    }

    override fun onFilterSelected(filter: Filter) {
        resetControls()
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.setImageBitmap(filter.processFilter(filteredImage))
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun resetControls() {

        if(editImageFragment != null){
            editImageFragment.resetControls()
        }

        brightnessFinal=0
        saturationFinal=1.0f
        contrastFinal=1.0f

    }

    override fun onBrightnessChanged(brightness: Int) {
        brightnessFinal = brightness
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightness))
        image_preview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onSaturationChanged(saturation: Float) {
        saturationFinal = saturation
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubfilter(saturation))
        image_preview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onContrastChanged(contrast: Float) {
        contrastFinal = contrast
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(contrast))
        image_preview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onEditStarted() {

    }

    override fun onEditCompleted() {
        val bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightnessFinal))
        myFilter.addSubFilter(SaturationSubfilter(saturationFinal))
        myFilter.addSubFilter(ContrastSubFilter(contrastFinal))
        finalImage = myFilter.processFilter(bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if(id == R.id.action_open){
            openImageFromGallery()
            return true
        }

        else if(id == R.id.action_save){
            saveImageToGallery()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveImageToGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        val path = BitmapUtils.insertImage(contentResolver, finalImage, System.currentTimeMillis().toString()+"_profile.jpg",
                            "")

                        if(!TextUtils.isEmpty(path)){
                            val snackBar = Snackbar.make(activity_main_coordinator, "Image saved to Gallery", Snackbar.LENGTH_LONG)
                                .setAction("OPEN", {
                                    openImage(path)
                                })
                            snackBar.show()
                        }

                        else {
                            val snackbar = Snackbar.make(activity_main_coordinator, "Unable to same image", Snackbar.LENGTH_LONG)
                            snackbar.show()
                        }
                    }

                    else{
                        Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).check()
    }

    private fun openImage(path: String?) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), "image/*")
        startActivity(intent)
    }

    private fun openImageFromGallery() {
        // I'm going to use Dexter to request runtime permission and process
        val withListener = Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type="image/*"
                        startActivityForResult(intent, SELECT_GALLARY_PERMISSION)
                    }

                    else{
                        Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == Activity.RESULT_OK && requestCode == SELECT_GALLARY_PERMISSION){
            val bitmap = BitmapUtils.getBitmapFromGallery(this, data!!.data!!, 800, 800)

            //clear bitmap memory
            originalImage!!.recycle()
            finalImage!!.recycle()
            filteredImage!!.recycle()

            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            filteredImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            finalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            bitmap.recycle()

            // render select image thumb
            filtersListFragment.displayImage(bitmap)
        }


        super.onActivityResult(requestCode, resultCode, data)
    }
}
