package com.gh0u1l5.wechatmagician.spellbook.interfaces

import java.io.File

interface IFileSystemHook {

    fun onFileDeleting(file: File) = false

    fun onFileDeleted(file: File) = Unit

    fun onFileReading(file: File) = Unit

    fun onFileWriting(file: File, overwrite: Boolean) = Unit
}