package com.example.musicdownload.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.download.ActionListener
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.download.Data.listDownload
import com.example.musicdownload.data.download.Utils
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.DownloadItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import java.io.File

class FileAdapter(val context: Context, var actionListener: ActionListener) :
    RecyclerView.Adapter<FileAdapter.DownloadingViewHolder>(){
    companion object {
        var list = ArrayList<Music>()

        class DownloadData {
            var id = 0
            var download: Download? = null
            var eta: Long = 0L
            var downloadedBytesPerSecond: Long = 0L
            override fun hashCode(): Int {
                return id
            }

            override fun toString(): String {
                return if (download == null) {
                    ""
                } else download.toString()
            }

            override fun equals(obj: Any?): Boolean {
                return obj === this || obj is DownloadData && obj.id == id
            }
        }
    }
    private val downloads = ArrayList<DownloadData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DownloadItemBinding.inflate(inflater, parent, false)
        return DownloadingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadingViewHolder, position: Int) {
        holder.binding.actionButton.setOnClickListener(null)
        holder.binding.actionButton.isEnabled = true
        val downloadData: DownloadData = downloads[position]
        var url = ""
        if (downloadData.download != null) {
            url = downloadData.download!!.url
        }
        val uri = Uri.parse(url)
        val status = downloadData.download!!.status
        Log.e("TAG", "onBindDownloadingViewHolder: " + list[position].name)
        holder.binding.titleTextView.text = list[position].name
        holder.binding.tvArtistDownloading.text = list[position].artistName
        Glide.with(context).load(list[position].image)
            .error(R.drawable.demo_img_download)
            .into(holder.itemView.findViewById(R.id.imgItemDownload))


        holder.binding.statusTextView.text = getStatusString(status)
        var progress = downloadData.download!!.progress
        if (progress == -1) { // Download progress is undermined at the moment.
            progress = 0
        }
        holder.binding.progressBar.progress = progress
        holder.binding.progressTextView.text =
            context.getString(R.string.percent_progress, progress)
        if (downloadData.eta == 0L) {
            holder.binding.remainingTextView.text = ""
        } else {
            holder.binding.remainingTextView.setText(Utils.getETAString(context, downloadData.eta))
        }
        if (downloadData.downloadedBytesPerSecond.equals(0L)) {
            holder.binding.downloadSpeedTextView.text = ""
        } else {
            holder.binding.downloadSpeedTextView.setText(
                Utils.getDownloadSpeedString(
                    context,
                    downloadData.downloadedBytesPerSecond
                )
            )
            Log.e(
                "downloaded",
                Utils.getDownloadSpeedString(context, downloadData.downloadedBytesPerSecond)
                    .toString()
            )
        }

        when (status) {
            Status.COMPLETED -> {
                holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                holder.binding.actionButton.setOnClickListener { view: View? ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Toast.makeText(
                            context,
                            "Downloaded Path:" + downloadData.download!!.file,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    val file = File(downloadData.download!!.file)
                    val uri1 = Uri.fromFile(file)
                    val share = Intent(Intent.ACTION_VIEW)
                    share.setDataAndType(uri1, Utils.getMimeType(context, uri1))
                    context.startActivity(share)
                }
                MediaScannerConnection.scanFile(
                    context,
                    listOf(Data.path[position]).toTypedArray(),
                    null,
                    null
                )
                actionListener.onRemoveDownload(
                    downloadData.download!!.id
                )
                if (list.size > 0) {
                    if (list[position] != null) {
                        listDownload.remove(list[position])
                        list.remove(list[position])
                    }
                }


            }
            Status.FAILED -> {
                holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_error_outline_24)
                holder.binding.actionButton.setOnClickListener { view: View? ->
                    holder.binding.actionButton.isEnabled = false
                    actionListener.onRetryDownload(downloadData.download!!.id)
                }
            }
            Status.PAUSED -> {
                holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                holder.binding.actionButton.setOnClickListener { view: View? ->
                    holder.binding.actionButton.isEnabled = false
                    actionListener.onResumeDownload(downloadData.download!!.id)
                }
            }
            Status.DOWNLOADING, Status.QUEUED -> {
                holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                holder.binding.actionButton.setOnClickListener { view: View? ->
                    holder.binding.actionButton.isEnabled = false
                    actionListener.onPauseDownload(downloadData.download!!.id)
                }
            }
            Status.ADDED -> {
                holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_add_circle_outline_24)
                holder.binding.actionButton.setOnClickListener { view: View? ->
                    holder.binding.actionButton.isEnabled = false
                    actionListener.onResumeDownload(downloadData.download!!.id)
                }
            }
            else -> {}
        }

        holder.binding.imgMoreTopListened.setOnClickListener {
            val bottomSheetDialogSong = BottomSheetDialog(context);
            bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_downloading);
            bottomSheetDialogSong.show()
            val img: ImageView = bottomSheetDialogSong.findViewById(R.id.imgBottomSheetSong)!!
            val song: TextView = bottomSheetDialogSong.findViewById(R.id.tvSongBtMusic)!!
            val singer: TextView = bottomSheetDialogSong.findViewById(R.id.tvSingerBtMusic)!!
            Glide.with(img).load(R.drawable.demo_img_download)
                .error(R.drawable.demo_img_download)
                .into(img)
            song.text = list[position].name
            singer.text = list[position].artistName
            val btnRemove = bottomSheetDialogSong.findViewById<View>(R.id.viewRemoveSong)
            btnRemove?.setOnClickListener {
                val uri12 = Uri.parse(downloadData.download!!.url)
                AlertDialog.Builder(context)
                    .setMessage("Delete this download")
                    .setPositiveButton(R.string.delete) { dialog, which ->
                        actionListener.onRemoveDownload(
                            downloadData.download!!.id
                        )
                        list.remove(list[position])
                        listDownload.remove(listDownload[position])
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
                bottomSheetDialogSong.dismiss()
            }
        }
    }

    fun addDownload(download: Download) {
        var found = false
        var data: DownloadData? = null
        var dataPosition = -1
        for (i in downloads.indices) {
            val downloadData = downloads[i]
            if (downloadData.id == download.id) {
                data = downloadData
                dataPosition = i
                found = true
                break
            }
        }
        if (!found) {
            val downloadData = DownloadData()
            downloadData.id = download.id
            downloadData.download = download
            downloads.add(downloadData)
            notifyItemInserted(downloads.size - 1)
        } else {
            data!!.download = download
            notifyItemChanged(dataPosition)
        }
    }

    override fun getItemCount(): Int {
        return downloads.size
    }

    fun update(download: Download, eta: Long, downloadedBytesPerSecond: Long) {
        for (position in downloads.indices) {
            val downloadData: DownloadData = downloads[position]
            if (downloadData.id == download.id) {
                when (download.status) {
                    Status.REMOVED, Status.DELETED -> {
                        downloads.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    else -> {
                        downloadData.download = download
                        downloadData.eta = eta
                        downloadData.downloadedBytesPerSecond = downloadedBytesPerSecond
                        notifyItemChanged(position)
                    }
                }
                return
            }
        }
    }

    private fun getStatusString(status: Status): String {
        return when (status) {
            Status.COMPLETED -> "Done"
            Status.DOWNLOADING -> "Downloading"
            Status.FAILED -> "Error"
            Status.PAUSED -> "Paused"
            Status.QUEUED -> "Waiting in Queue"
            Status.REMOVED -> "Removed"
            Status.NONE -> "Not Queued"
            else -> "Unknown"
        }
    }

    inner class DownloadingViewHolder(val binding: DownloadItemBinding) : RecyclerView.ViewHolder(binding.root) {
    }


}