# PhotoPicker

PhotoPicker 是一个轻量级的图片选择器。实现的功能，一张图胜过千言万语：

![功能介绍](https://ws2.sinaimg.cn/large/006tKfTcgy1fqz5lrqlljj31ig11wjtx.jpg)

## 简述

虽然 GitHub 上已经有很多图片选择器，比如 [Album][Album]、[Matisse][Matisse]、[PickPhotoSample][PickPhotoSample]、[RxGalleryFinal][RxGalleryFinal] 等这些优秀的开源库。经调研这些库在多选模式下只支持指定最大可选数量，并不支持无上限的多选模式。通常这种模式的使用场景确实比较少，然而我司的一款产品就有用户反馈过这个需求。并且我司正在开发的一款产品中也需要这个功能，为了复用这个功能模块，我的 PhotoPicker 就应运而生了。

目前 PhotoPicker 还只是 **alpha** 版本，如果你有好的建议欢迎提 issue 和 pull request。

## 功能

- 支持单选、设置上限多选、无上限多选三种模式
- 轻量，没有过多的依赖
- 支持流式 API
- 支持不同的图片加载器

## 依赖

第一步：

添加下面的代码到根目录 build.gradle：

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

第二步，添加依赖到你的项目中：

```groovy
dependencies {
	implementation 'com.github.Pigcasso:PhotoPicker:0.1.1-alpha'
}
```

## 使用

PhotoPicker 的使用方法包括：单选模式、多选模式（设置上限）、多选模式（无上限）。

### 单选模式

```kotlin
PhotoPicker
	.image(this)
	.singleChoice() // 单选模式
	.allPhotosAlbum(allPhotosAlbumCheck.isChecked) // 是否显示所有图片相册
	.preview(previewCheck.isChecked) // 是否开启预览功能
	.onResult(result) // 选择完成后的回调
    .onCancel(cancel) // 取消选择后的回调
	.start()
```

### 多选模式（设置上限）

```kotlin
PhotoPicker
        .image(this)
        .multipleChoice() // 多选模式
        .upperLimit() // 设置上限
        .allPhotosAlbum(allPhotosAlbumCheck.isChecked) // 是否显示所有图片相册
        .preview(previewCheck.isChecked) // 是否开启预览功能
        .limitCount(limitCount) // 设置可选的上限数
        .countable(countableCheck.isChecked)
        .onResult(result) // 选择完成后的回调
        .onCancel(cancel) // 取消选择后的回调
        .start()
```

### 多选模式（无上限）

```kotlin
PhotoPicker
        .image(this)
        .multipleChoice() // 多选模式
        .noUpperLimit() // 无上限
        .allPhotosAlbum(allPhotosAlbumCheck.isChecked) // 是否显示所有图片相册
        .preview(previewCheck.isChecked) // 是否开启预览功能
        .countable(countableCheck.isChecked) // 是否开启有序选择图片功能
        .selectableAll(selectableAllCheck.isChecked) // 是否开启选择全部功能
        .onResult(result) // 选择完成后的回调
        .onCancel(cancel) // 取消选择后的回调
        .start()
```

## 配置

允许修改图片加载器和主题色

### 修改图片加载器

PhotoPicker 内置了一个简单的图片加载器 DefaultPhotoLoader。你也可以用`Glide`，`Picasso`等实现 。

请在项目中合适的位置（建议在 `Application` ）修改图片加载库，以 `Glide` 为例：

```kotlin
// 实现 PhotoLoader 接口
class GlidePhotoLoader : PhotoLoader {
    override fun loadPhoto(imageView: ImageView, imagePath: String, viewWidth: Int, viewHeight: Int) {
        Glide.with(imageView).load(imagePath).into(imageView)
    }
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 修改图片加载库
        PhotoPicker.photoLoader = GlidePhotoLoader()
    }
}
```

### 修改主题色

请在项目中合适的位置（建议在`Application`中）修改主题色

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        PhotoPicker.themeConfig = ThemeConfig()
                .radioCheckedColor(Color.RED)
                .bottomBarBackgroundColor(ContextCompat.getColor(this@App, R.color.colorPrimary))
                .bottomBarTextColor(Color.MAGENTA)
                .arrowDropColor(Color.CYAN)
                .checkboxColor(ContextCompat.getColor(this@App, R.color.colorAccent))
                .checkboxOutlineColor(ContextCompat.getColor(this@App, R.color.colorAccent))
    }
}
```

主题色对应的位置

|                                   |                                   |                                   |
| --------------------------------- | --------------------------------- | --------------------------------- |
| ![](../assets/theme-config-1.jpg) | ![](../assets/theme-config-2.jpg) | ![](../assets/theme-config-3.jpg) |

[Matisse]: https://github.com/zhihu/Matisse
[PickPhotoSample]: https://github.com/Werb/PickPhotoSample
[RxGalleryFinal]: https://github.com/FinalTeam/RxGalleryFinal
[Album]: https://github.com/yanzhenjie/Album