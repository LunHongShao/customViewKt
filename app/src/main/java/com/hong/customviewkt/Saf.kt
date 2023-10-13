package com.hong.customviewkt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import okio.buffer
import okio.sink
import okio.source
import java.io.File

class Saf : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saf)
//        XXPermissions.with(this)
//            .permission(
//            Permission.READ_EXTERNAL_STORAGE,
//            Permission.WRITE_EXTERNAL_STORAGE
//        )
//            .request { _, allGranted ->
//                if (allGranted) {
//
//                }
//            }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent,1024)
//        val path=Environment.getExternalStoragePublicDirectory("TestFiles").absolutePath
//        val file=File(path)
//        if (!file.exists()) {
//            file.mkdir()
//        }
//        //获取文件流写入文件到File
//        val newFile = File(file.absolutePath + "/材料清单PDF.pdf")
//        if (!newFile.exists()) {
//            newFile.createNewFile()
//        }

//        val inputStream = assets.open("电子书.cer")
//        val inBuffer = inputStream.source().buffer()
//        newFile.sink(true).buffer().use {
//            it.writeAll(inBuffer)
//            inBuffer.close()
//        }

//        DocumentFile.fromFile(File(path)).createDirectory("TestFilesTest")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode==1024&&resultCode== Activity.RESULT_OK){
            resultData?.data?.let { uri ->
//                YYLogUtils.w("打开文件夹：$uri")
                DocumentFile.fromTreeUri(this, uri)
                    // 在文件夹内创建新文件夹
                    ?.createDirectory("DownloadMyFiles")
                    ?.apply {
                        // 在新文件夹内创建文件
//                        YYLogUtils.w("在新文件夹内创建文件")
                        createFile("text/plain", "test.txt")

                        // 通过文件名找到文件
                        findFile("test.txt")?.also {
                            try {
                                // 在文件中写入内容
                                contentResolver.openOutputStream(uri)?.write("hello world".toByteArray())
//                                YYLogUtils.w("在文件中写入内容完成")
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                        // 删除文件
                        // ?.delete()
                    }
                // 删除文件夹
                // ?.delete()

            }

        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, Saf::class.java)
            context.startActivity(starter)
        }
    }
}