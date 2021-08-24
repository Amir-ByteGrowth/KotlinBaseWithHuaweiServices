package com.daniyalirfan.kotlinbasewithcorutine.ui.firstfragment

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniyalirfan.kotlinbasewithcorutine.BR
import com.daniyalirfan.kotlinbasewithcorutine.R
import com.daniyalirfan.kotlinbasewithcorutine.baseclasses.BaseFragment
import com.daniyalirfan.kotlinbasewithcorutine.data.local.datastore.DataStoreProvider
import com.daniyalirfan.kotlinbasewithcorutine.data.models.PostsResponseItem
import com.daniyalirfan.kotlinbasewithcorutine.data.remote.Resource
import com.daniyalirfan.kotlinbasewithcorutine.databinding.FirstFragmentBinding
import com.daniyalirfan.kotlinbasewithcorutine.ui.firstfragment.adapter.PostsRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.first_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirstFragment : BaseFragment<FirstFragmentBinding, FirstViewModel>(), OnMapReadyCallback {

    override val layoutId: Int
        get() = R.layout.first_fragment
    override val viewModel: Class<FirstViewModel>
        get() = FirstViewModel::class.java
    override val bindingVariable: Int
        get() = BR.viewModel

    private lateinit var adapter: PostsRecyclerAdapter
    private var postsList: ArrayList<PostsResponseItem> = ArrayList()
    lateinit var dataStoreProvider: DataStoreProvider


    private var hMap: HuaweiMap? = null

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get reference to our Data Store Provider class
        dataStoreProvider = DataStoreProvider(requireContext())


        subscribeToObserveDataStore()

        //calling api
        mViewModel.fetchPostsFromApi()


        MapsInitializer.setApiKey("CwEAAAAAvyvvYLX+3252ZkIttPsw4lNF1AWDKOEdh0CKRE5/vj3EPf6U9my0/YEqZIU0npUPEAje5dmvvAjwqvmXkW68vveHkdY=")


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initHuaweiMap(savedInstanceState)

        permissionsForMaps()
        initMarkers()


        initialising()
    }


    /**
     * Initialize the Huawei Map
     */
    private fun initHuaweiMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        map_view?.apply {
            onCreate(mapViewBundle)
            getMapAsync(this@FirstFragment)
        }


    }

    override fun onMapReady(map: HuaweiMap?) {
        hMap = map
        hMap?.isMyLocationEnabled = true // Enable the my-location overlay.
        hMap?.uiSettings?.isMyLocationButtonEnabled = true // Enable the my-location icon.


    }

    override fun onStart() {
        map_view?.onStart()
        super.onStart()
    }

    override fun onStop() {
        map_view?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        hMap?.clear()
        hMap = null
        map_view?.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        map_view?.onPause()
        super.onPause()
    }

    override fun onResume() {
        map_view?.onResume()
        super.onResume()
    }

    override fun onLowMemory() {
        map_view?.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        map_view.onSaveInstanceState(mapViewBundle)
    }


    //maps permission
    private fun permissionsForMaps() {
        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
//                        locationEnabled()


                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }


            }).onSameThread()
            .check()


    }


    private fun initMarkers() {
        val markerDataList = createMarkerList()
        markerDataList.forEachIndexed { index, markerOptions ->
            val marker = hMap?.addMarker(markerOptions)
            marker?.setMarkerAnchor(0.5f, 1f) // Set marker anchor point
            marker?.tag =
                "$index Extra Info" // Set extra data with tag. This data can be a custom class
        }
        hMap?.setMarkersClustering(true) // Enable clustering
        hMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                markerDataList[0].position,
                17f
            )
        ) // Move camera to first item
    }

    fun createMarkerList(): List<MarkerOptions> {
        val markerOptions = arrayListOf<MarkerOptions>()
        val latLangList = dummyLatLangList()
        latLangList.forEachIndexed { index, latLng ->
            val options = MarkerOptions()
                .position(latLng)
                .title("$index Market Title")
                .snippet("$index snippet!")
                .clusterable(true) // Make it clusterable
            markerOptions.add(options)
        }
        return markerOptions
    }

    private fun dummyLatLangList(): ArrayList<LatLng> {
        val list = arrayListOf<LatLng>()
        list.add(LatLng(31.582045, 74.329376))
        list.add(LatLng(31.418715, 73.079109))
//        list.add(LatLng(41.031507, 29.030369))
//        list.add(LatLng(41.032527, 29.030358))
//        list.add(LatLng(41.034081, 29.030787))
//        list.add(LatLng(41.026767, 29.033660))
//        list.add(LatLng(41.027885, 29.029466))
//        list.add(LatLng(41.029577, 29.030861))
//        list.add(LatLng(41.030451, 29.028737))
//        list.add(LatLng(41.029666, 29.027503))
//        list.add(LatLng(41.028865, 29.027321))
//        list.add(LatLng(41.027934, 29.027375))
        return list
    }


    override fun subscribeToViewLiveData() {
        super.subscribeToViewLiveData()

        mViewModel.btnClick.observe(this, Observer {

            //observing data from edittext
            mViewModel.myedittext.get()?.let {

                //setting data to textview
                mViewModel.myName.set(it)

                //saving data to data store
                //Stores the values
                GlobalScope.launch {
                    dataStoreProvider.storeData(false, it)
                }
            }
        })
    }

    private fun subscribeToObserveDataStore() {

        //observing data from data store and showing
        dataStoreProvider.userNameFlow.asLiveData().observe(this, Observer {
            mViewModel.myName.set(it)
        })

    }


    private fun initialising() {

        adapter = PostsRecyclerAdapter(postsList, object : PostsRecyclerAdapter.ClickItemListener {
            override fun onClicked(position: Int) {
                Navigation.findNavController(recycler_posts)
                    .navigate(R.id.action_firstFragment_to_secondFragment)
            }

            override fun onProductLiked(position: Int, isLiked: Boolean) {
            }

        })

        recycler_posts.layoutManager = LinearLayoutManager(requireContext())
        recycler_posts.adapter = adapter

    }

    //subscribing to network live data
    override fun subscribeToNetworkLiveData() {
        super.subscribeToNetworkLiveData()

        mViewModel.postsData.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideProgressBar()
                    it.data?.let {
                        postsList.addAll(it)
                        adapter.notifyDataSetChanged()
                    }

                }
                Resource.Status.LOADING -> {
                    showProgressBar()
                }
                Resource.Status.ERROR -> {
                    hideProgressBar()

                    Snackbar.make(recycler_posts!!, it.message!!, Snackbar.LENGTH_SHORT)
                        .show()

                }
            }
        })
    }


}