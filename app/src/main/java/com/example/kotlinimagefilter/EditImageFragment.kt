package com.example.kotlinimagefilter


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.kotlinimagefilter.Interface.EditImageFragmentListener
import kotlinx.android.synthetic.main.fragment_edit_image.*

/**
 * A simple [Fragment] subclass.
 */
class EditImageFragment : Fragment(),SeekBar.OnSeekBarChangeListener {

    private var listener:EditImageFragmentListener?=null

    internal lateinit var seekbar_brightness:SeekBar
    internal lateinit var seekbar_saturation:SeekBar
    internal lateinit var seekbar_contrast:SeekBar

    fun resetControls(){
        seekbar_brightness.progress=100
        seekbar_saturation.progress=0
        seekbar_contrast.progress=10
    }

    fun setListener(listener: EditImageFragmentListener){
        this.listener=listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_edit_image, container, false)

        seekbar_brightness = view.findViewById<SeekBar>(R.id.seekbar_brightness)
        seekbar_saturation = view.findViewById<SeekBar>(R.id.seekbar_saturation)
        seekbar_contrast = view.findViewById<SeekBar>(R.id.seekbar_contrast)


        seekbar_brightness.max=200
        seekbar_brightness.progress=100

        seekbar_saturation.max=30
        seekbar_saturation.progress=10

        seekbar_contrast.max=20
        seekbar_contrast.progress=0

        seekbar_brightness.setOnSeekBarChangeListener(this)
        seekbar_saturation.setOnSeekBarChangeListener(this)
        seekbar_contrast.setOnSeekBarChangeListener(this)

        return view
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        var progress = progress
        if(listener != null){
            if(seekBar!!.id == R.id.seekbar_brightness){
                listener!!.onBrightnessChanged(progress-100)
            }

            else if(seekBar!!.id == R.id.seekbar_contrast){
                progress += 10
                val floatVal = .10f*progress
                listener!!.onContrastChanged(floatVal)
            }

            else if(seekBar!!.id == R.id.seekbar_saturation){
                val floatVal = .10f*progress
                listener!!.onSaturationChanged(floatVal)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        if(listener != null){
            listener!!.onEditStarted()
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if(listener != null){
            listener!!.onEditCompleted()
        }
    }


}
