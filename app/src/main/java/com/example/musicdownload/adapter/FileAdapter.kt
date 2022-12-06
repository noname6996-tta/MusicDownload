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
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.download.ActionListener
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.download.Data.listDownload
import com.example.musicdownload.data.download.Utils
import com.example.musicdownload.data.model.Music
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import java.io.File

class FileAdapter(context: Context, var actionListener: ActionListener) :

    RecyclerView.Adapter<FileAdapter.ViewHolder>() {
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
    val context = context
    private val downloads = ArrayList<DownloadData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.download_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("StringFormatInvalid")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.actionButton.setOnClickListener(null)
        holder.actionButton.isEnabled = true
        val downloadData: DownloadData = downloads[position]
        var url = ""
        if (downloadData.download != null) {
            url = downloadData.download!!.url
        }
        val uri = Uri.parse(url)
        val status = downloadData.download!!.status
        val context = holder.itemView.context

        holder.titleTextView.text = list[position].name
        holder.tvArtistDownloading.text = list[position].artistName
        Glide.with(holder.itemView.context).load(list[position].image)
            .error(R.drawable.demo_img_download)
            .into(holder.itemView.findViewById(R.id.imgItemDownload))


        holder.statusTextView.text = getStatusString(status)
        var progress = downloadData.download!!.progress
        if (progress == -1) { // Download progress is undermined at the moment.
            progress = 0
        }
        holder.progressBar.progress = progress
        holder.progressTextView.text = context.getString(R.string.percent_progress, progress)
        if (downloadData.eta == 0L) {
            holder.timeRemainingTextView.text = ""
        } else {
            holder.timeRemainingTextView.setText(Utils.getETAString(context, downloadData.eta))
        }
        if (downloadData.downloadedBytesPerSecond.equals(0L)) {
            holder.downloadedBytesPerSecondTextView.text = ""
        } else {
            holder.downloadedBytesPerSecondTextView.setText(
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
                holder.actionButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                holder.actionButton.setOnClickListener { view: View? ->
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
                holder.actionButton.setImageResource(R.drawable.ic_baseline_error_outline_24)
                holder.actionButton.setOnClickListener { view: View? ->
                    holder.actionButton.isEnabled = false
                    actionListener.onRetryDownload(downloadData.download!!.id)
                }
            }
            Status.PAUSED -> {
                holder.actionButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                holder.actionButton.setOnClickListener { view: View? ->
                    holder.actionButton.isEnabled = false
                    actionListener.onResumeDownload(downloadData.download!!.id)
                }
            }
            Status.DOWNLOADING, Status.QUEUED -> {
                holder.actionButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                holder.actionButton.setOnClickListener { view: View? ->
                    holder.actionButton.isEnabled = false
                    actionListener.onPauseDownload(downloadData.download!!.id)
                }
            }
            Status.ADDED -> {
                holder.actionButton.setImageResource(R.drawable.ic_baseline_add_circle_outline_24)
                holder.actionButton.setOnClickListener { view: View? ->
                    holder.actionButton.isEnabled = false
                    actionListener.onResumeDownload(downloadData.download!!.id)
                }
            }
            else -> {}
        }

        //Set delete action
        holder.itemView.setOnLongClickListener { v: View? ->
            val uri12 = Uri.parse(downloadData.download!!.url)
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.delete_title, uri12.lastPathSegment))
                .setPositiveButton(R.string.delete) { dialog, which ->
                    actionListener.onRemoveDownload(
                        downloadData.download!!.id
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
            true
        }

        holder.imgMoreTopListened.setOnClickListener {

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

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView
        val tvArtistDownloading: TextView
        val imgItemDownload: ImageView
        val statusTextView: TextView
        val progressBar: ProgressBar
        val progressTextView: TextView
        val actionButton: ImageView
        val timeRemainingTextView: TextView
        val downloadedBytesPerSecondTextView: TextView
        val imgMoreTopListened: ImageView
        init {
            titleTextView = itemView.findViewById(R.id.titleTextView)
            statusTextView = itemView.findViewById(R.id.status_TextView)
            imgItemDownload = itemView.findViewById(R.id.imgItemDownload)
            tvArtistDownloading = itemView.findViewById(R.id.tvArtistDownloading)
            progressBar = itemView.findViewById(R.id.progressBar)
            actionButton = itemView.findViewById(R.id.actionButton)
            progressTextView = itemView.findViewById(R.id.progress_TextView)
            timeRemainingTextView = itemView.findViewById(R.id.remaining_TextView)
            downloadedBytesPerSecondTextView = itemView.findViewById(R.id.downloadSpeedTextView)
            imgMoreTopListened = itemView.findViewById(R.id.imgMoreTopListened)
        }
    }

    private fun showBottomSheetDownloadinf(position: Int){
        val bottomSheetDialogSong = BottomSheetDialog(context);
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_info_music);
        bottomSheetDialogSong.show()
        val img: ImageView = bottomSheetDialogSong.findViewById(R.id.imgBottomSheetSong)!!
        val song: TextView = bottomSheetDialogSong.findViewById(R.id.tvSongBtMusic)!!
        val singer: TextView = bottomSheetDialogSong.findViewById(R.id.tvSingerBtMusic)!!
        Glide.with(context).load(list[position].image).error(R.drawable.demo_img_download)
            .into(img)
        song.text = list[position].name
        singer.text = list[position].artistName
        val viewShare: View? = bottomSheetDialogSong.findViewById(R.id.layoutShare)
        if (viewShare == null) {

        } else {
            viewShare.setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "plain/text"
                shareIntent.putExtra(Intent.EXTRA_TEXT, list[position].audio)
                context.startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
            }
        }
    }
}