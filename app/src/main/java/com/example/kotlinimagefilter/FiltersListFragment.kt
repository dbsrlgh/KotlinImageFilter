package com.example.kotlinimagefilter


import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.legacy.widget.Space
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinimagefilter.Adapter.ThumbnailAdapter
import com.example.kotlinimagefilter.Interface.FiltersListFragmentListener
import com.example.kotlinimagefilter.Utils.BitmapUtils
import com.example.kotlinimagefilter.Utils.SpaceItemDecoration
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import kotlinx.android.synthetic.main.fragment_filters_list.*
import kotlinx.android.synthetic.main.thumbnail_list_item.*
import java.lang.reflect.Type

/**
 * A simple [Fragment] subclass.
 */
class FiltersListFragment : Fragment(), FiltersListFragmentListener {

    internal var listener : FiltersListFragmentListener?=null
    internal lateinit var adapter:ThumbnailAdapter
    internal lateinit var thumbnailItemList:MutableList<ThumbnailItem>
    internal lateinit var recycler_view: RecyclerView

    fun setListener(listFragmentListener: FiltersListFragmentListener){
        this.listener = listFragmentListener
    }

    override fun onFilterSelected(filter: Filter) {
        if(listener!=null){
            listener!!.onFilterSelected(filter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val itemView =  inflater.inflate(R.layout.fragment_filters_list, container, false)

        thumbnailItemList = ArrayList()
        adapter = ThumbnailAdapter(activity!!, thumbnailItemList, this)

        recycler_view = itemView.findViewById<RecyclerView>(R.id.recycler_view)
        recycler_view.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.itemAnimator = DefaultItemAnimator()
        val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        recycler_view.addItemDecoration(SpaceItemDecoration(space))
        recycler_view.adapter=adapter

        displayImage(null)

        return itemView
    }

    fun displayImage(bitmap: Bitmap?) {
        val r = Runnable {
            val thumbImage : Bitmap?

            if(bitmap == null){
                thumbImage = BitmapUtils.getBitmapFromAssets(activity!!, MainActivity.Main.IMAGE_NAME, 100, 100)
            }

            else{
                thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
            }

            if(thumbImage == null){
                return@Runnable
            }

            ThumbnailsManager.clearThumbs()
            thumbnailItemList.clear()

            // add normal bitmap first
            val thumbnailItem = ThumbnailItem()
            thumbnailItem.image = thumbImage
            thumbnailItem.filterName = "Normal"
            ThumbnailsManager.addThumb(thumbnailItem)

            // add filter pack
            val filters = FilterPack.getFilterPack(activity!!)
            for(filter in filters){
                val item = ThumbnailItem()
                item.image = thumbImage
                item.filter = filter
                item.filterName = filter.name
                ThumbnailsManager.addThumb(item)
            }

            thumbnailItemList.addAll(ThumbnailsManager.processThumbs(activity))
            activity!!.runOnUiThread{
                adapter.notifyDataSetChanged()
            }
        }
        Thread(r).start()
    }


}
