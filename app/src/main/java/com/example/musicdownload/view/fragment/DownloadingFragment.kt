package com.example.musicdownload.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.adapter.FileAdapter
import com.example.musicdownload.data.download.ActionListener
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.databinding.FragmentDownloadingBinding
import com.example.musicdownload.view.activity.MainActivity
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Downloader.FileDownloaderType
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import java.io.File
import java.lang.Long.compare
import java.util.*


class DownloadingFragment : Fragment(),ActionListener {
    companion object {
        val GROUP_ID = "listGroup".hashCode()
        val FETCH_NAMESPACE = "DownloadListActivity"
        var arrayMusicDownloading = ArrayList<Music>()
    }
    private val STORAGE_PERMISSION_CODE = 200
    private val UNKNOWN_REMAINING_TIME: Long = -1L
    private val UNKNOWN_DOWNLOADED_BYTES_PER_SECOND: Long = 0L
    private lateinit var fileAdapter: FileAdapter
    private lateinit var fetch: Fetch

    private lateinit var binding: FragmentDownloadingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(requireContext())
            .setDownloadConcurrentLimit(999999)
            .enableLogging(true)
            .setHttpDownloader(OkHttpDownloader(FileDownloaderType.PARALLEL))
            .setNamespace(FETCH_NAMESPACE)
            .build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        checkStoragePermissions()
    }

    private fun setUpViews() {

        val networkSwitch: SwitchCompat = binding.networkSwitch
        val recyclerView: RecyclerView = binding.recDownloading
        val linearLayoutManager = LinearLayoutManager(requireContext() )
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        networkSwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                fetch.setGlobalNetworkType(NetworkType.WIFI_ONLY)
            } else {
                fetch.setGlobalNetworkType(NetworkType.ALL)
            }
        }
        //
        fileAdapter = FileAdapter(requireContext(),this)
        recyclerView.adapter = fileAdapter
    }

     override fun onResume() {
        super.onResume()
         fetch.getDownloadsInGroup(GROUP_ID) { downloads: List<Download>? ->
             val list = ArrayList(downloads)
             Collections.sort(
                 list
             ) { first: Download, second: Download ->
                 compare(
                     first.created,
                     second.created
                 )
             }
             for (download in list) {
//                 fileAdapter.addDownload(download!!)
             }
         }.addListener(fetchListener)
         if (FileAdapter.list.size>0){
             binding.recDownloading.adapter = fileAdapter
             Log.e("AAAAAA",FileAdapter.list.toString())
         }
//         if (Data.path.size>0){
//             for (i in 0..Data.path.size-1){
//                 MediaScannerConnection.scanFile(
//                     requireContext(), arrayOf(Data.path[i]), null, null
//                 )
//             }
//         }
    }

    override fun onPause() {
        super.onPause()
//        fetch.removeListener(fetchListener)
    }

     override fun onDestroy() {
        super.onDestroy()
        fetch.close()
    }

    private val fetchListener: FetchListener = object : AbstractFetchListener() {
        override fun onAdded(download: Download) {
            fileAdapter.addDownload(download)
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
        }

        override fun onCompleted(download: Download) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
            if (Data.path.size>0){
                for (i in 0..Data.path.size-1){
                    MediaScannerConnection.scanFile(
                        requireContext(), arrayOf(Data.path[i]), null, null
                    )
                }
            }
        }


        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
            Toast.makeText(requireContext(),""+error.toString(),Toast.LENGTH_SHORT).show()
            Log.e("error",error.toString())
        }

        override fun onProgress(
            download: Download,
            etaInMilliseconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            fileAdapter.update(download, etaInMilliseconds, downloadedBytesPerSecond)
        }

        override fun onPaused(download: Download) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
        }

        override fun onResumed(download: Download) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
        }

        override fun onCancelled(download: Download) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
        }

        override fun onRemoved(download: Download) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
        }

        override fun onDeleted(download: Download) {
            fileAdapter.update(
                download,
                UNKNOWN_REMAINING_TIME,
                UNKNOWN_DOWNLOADED_BYTES_PER_SECOND
            )
        }
    }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enqueueDownloads()
        } else {

        }
    }

    private fun enqueueDownloads() {
        if (Data.listDownload.isEmpty()){

        } else {
            val requests: List<Request> = Data.getFetchRequestWithGroupId(
                GROUP_ID,
                requireContext()
            )
            fetch.enqueue(requests) { updatedRequests: List<Pair<Request?, Error?>?>? -> }
        }

    }

    override fun onPauseDownload(id: Int) {
        fetch.pause(id)
    }

    override fun onResumeDownload(id: Int) {
        fetch.resume(id)
    }

    override fun onRemoveDownload(id: Int) {
        fetch.remove(id)
    }

    override fun onRetryDownload(id: Int) {
        fetch.retry(id)
    }
}