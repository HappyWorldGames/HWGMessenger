package com.happyworldgames.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.databinding.ActivityFindUserBinding

class FindUserActivity : AppCompatActivity() {

    private lateinit var mQrResultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityFindUserBinding = ActivityFindUserBinding.inflate(layoutInflater)
        setContentView(activityFindUserBinding.root)

        activityFindUserBinding.qrView.setImageBitmap(createQR(DataBase.getCurrentUser().uid))
        activityFindUserBinding.scanQrButton.setOnClickListener {
            startScanner()
        }

        mQrResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                val result = IntentIntegrator.parseActivityResult(it.resultCode, it.data)

                if(result.contents != null) {
                    DataBase.getUserByUid(result.contents).get().addOnSuccessListener { dataSnap ->
                        try {
                        /*    val user = dataSnap.getValue(User::class.java) ?: return@addOnSuccessListener
                            DataBase.getChatsByUserUid(DataBase.getCurrentUser().uid).child(user.name).setValue(Group(user.name, user.uid))
                            DataBase.getChatsByUserUid(user.uid).child(DataBase.getCurrentUser().displayName!!).setValue(Group(
                                DataBase.getCurrentUser().displayName!!, DataBase.getCurrentUser().uid))
*/
                            Snackbar.make(activityFindUserBinding.root, getString(R.string.successful), Snackbar.LENGTH_LONG).show()
                        }catch (e: Throwable) {
                            Snackbar.make(activityFindUserBinding.root, getString(R.string.failed), Snackbar.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        Snackbar.make(activityFindUserBinding.root, getString(R.string.failed), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun createQR(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
    private fun startScanner() {
        val scanner = IntentIntegrator(this)
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        scanner.setPrompt("Scan QR")
        mQrResultLauncher.launch(scanner.createScanIntent())
    }

    private fun addChat(){
        //use phone number for name chat
    }
}