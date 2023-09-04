package com.example.m5

import java.io.File

fun deleteMusic(filePath: String): Boolean {
    val file = File(filePath)

    if (file.exists()) {
        System.gc()
        // 删除文件
        if (file.delete()) {
            return true // 删除成功
        }
    }
    return false // 删除失败
}