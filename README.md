<h1 align="center">Music - MusicDownload</h1>

## About this app

Ứng dun nghe nhạc online, offlline, lấy dữ liệu từ api.
Có thể tạo playlist nhạc riêng, tải bài hát yêu thích

Note : Api đã không còn sử dụng được nữa

<p align="center">
<img src="/preview/Home.png"/>
</p>
<p align="center">
<img src="/preview/Playing.png"/>
</p>
<p align="center">
<img src="/preview/Downloading.png"/>
</p>
<p align="center">
<img src="/preview/Playlist.png"/>
</p>
<p align="center">
<img src="/preview/Playlist-Inside.png"/>
</p>


## Tech stack
- Minimum SDK level 24
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- Jetpack
    - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
    - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
    - DataBinding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
    - Room: Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
- Architecture
    - MVVM Architecture (View - DataBinding - ViewModel - Model)
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): Construct the REST APIs and paging network data.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.
- [Glide](https://github.com/bumptech/glide), [GlidePalette](https://github.com/florent37/GlidePalette): Loading images from network.
- [Timber](https://github.com/JakeWharton/timber): A logger with a small, extensible API.
