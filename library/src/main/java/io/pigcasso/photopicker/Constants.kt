package io.pigcasso.photopicker

/**
 * @author Zhu Liang
 */

// 是否显示所有照片的相册
const val EXTRA_ALL_PHOTOS_ALBUM = "extra.SHOW_ALL_PHOTOS_ALBUM"

// 选择模式
const val EXTRA_CHOICE_MODE = "extra.CHOICE_MODE"
const val CHOICE_MODE_SINGLE = 1
const val CHOICE_MODE_MULTIPLE_UPPER_LIMIT = 2
const val CHOICE_MODE_MULTIPLE_NO_UPPER_LIMIT = 3

// 照片可选的上限数
const val EXTRA_LIMIT_COUNT = "extra.LIMIT_COUNT"
const val NO_LIMIT_COUNT = -1

const val EXTRA_COUNTABLE = "extra.COUNTABLE"

const val EXTRA_CURRENT_ITEM = "extra.CURRENT_ITEM"

const val EXTRA_PREVIEW = "extra.PREVIEW"

const val EXTRA_SELECTABLE_ALL = "extra.SELECTABLE_ALL"

/**
 * 默认显示"所有照片"的相册
 */
const val DEFAULT_ALL_PHOTOS_ALBUM = true
/**
 * 默认支持预览选中的图片
 */
const val DEFAULT_PREVIEW = true
/**
 * 默认照片可选的上限数
 */
const val DEFAULT_LIMIT_COUNT = 9
/**
 * 默认不记录选中的次序
 */
const val DEFAULT_COUNTABLE = false
/**
 * 不设上限的情况下，默认支持选中全部
 */
const val DEFAULT_SELECTABLE_ALL = true

