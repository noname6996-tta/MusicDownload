package com.example.musicdownload.network

import java.io.IOException
import java.lang.Exception

class NoConnectivityException : IOException() {
    override val message: String?
        get() = "No internet connection"

}