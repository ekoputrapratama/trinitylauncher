package com.fisma.trinity.util

import android.content.Context
import android.widget.Toast
import com.fisma.trinity.Constants
import com.fisma.trinity.R
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupHelper {
  fun backupConfig(context: Context, file: String) {
    val packageManager = context.packageManager
    try {
      val p = packageManager.getPackageInfo(context.packageName, 0)
      val dataDir = p.applicationInfo.dataDir

      val fos = FileOutputStream(file)
      val bos = BufferedOutputStream(fos)
      val zos = ZipOutputStream(bos)

      addFileToZip(zos, "$dataDir/databases/home.db", "home.db")
      addFileToZip(zos, "$dataDir/shared_prefs/app.xml", "app.xml")
      Toast.makeText(context, R.string.toast_backup_success, Toast.LENGTH_SHORT).show()
      zos.flush()
      zos.close()
    } catch (e: Exception) {
      Toast.makeText(context, R.string.toast_backup_error, Toast.LENGTH_SHORT).show()
    }
  }

  fun restoreConfig(context: Context, file: String) {
    val packageManager = context.packageManager
    try {
      val p = packageManager.getPackageInfo(context.packageName, 0)
      val dataDir = p.applicationInfo.dataDir

      extractFileFromZip(file, "$dataDir/databases/home.db", "home.db")
      extractFileFromZip(file, "$dataDir/shared_prefs/app.xml", "app.xml")
      Toast.makeText(context, R.string.toast_backup_success, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Toast.makeText(context, R.string.toast_backup_error, Toast.LENGTH_SHORT).show()
    }
  }

  @Throws(Exception::class)
  fun addFileToZip(outZip: ZipOutputStream, file: String, name: String) {
    val data = ByteArray(Constants.BUFFER_SIZE)
    val fi = FileInputStream(file)
    val inputStream = BufferedInputStream(fi, Constants.BUFFER_SIZE)
    val entry = ZipEntry(name)
    outZip.putNextEntry(entry)
    var count: Int = inputStream.read(data, 0, Constants.BUFFER_SIZE)
    while (count != -1) {
      outZip.write(data, 0, count)
      count = inputStream.read(data, 0, Constants.BUFFER_SIZE)
    }
    inputStream.close()
  }

  @Throws(Exception::class)
  fun extractFileFromZip(filePath: String, file: String, name: String): Boolean {
    val inZip = ZipInputStream(BufferedInputStream(FileInputStream(filePath)))
    val data = ByteArray(Constants.BUFFER_SIZE)
    var found = false

    var ze: ZipEntry = inZip.nextEntry
    while (ze != null) {
      if (ze.name == name) {
        found = true
        // delete old file first
        val oldFile = File(file)
        if (oldFile.exists()) {
          if (!oldFile.delete()) {
            throw Exception("Could not delete $file")
          }
        }

        val outFile = FileOutputStream(file)
        var count = inZip.read(data)
        while (count != -1) {
          outFile.write(data, 0, count)
          count = inZip.read(data)
        }

        outFile.close()
        inZip.closeEntry()
      }
      ze = inZip.nextEntry
    }
    return found
  }
}
