package com.losfrees.hsba.simpleconnecttobluetoothmagene

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.losfrees.hsba.simpleconnecttobluetoothmagene.MainActivity.Companion.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.losfrees.hsba.simpleconnecttobluetoothmagene.MainActivity.Companion.HEART_RATE_CONTROL_POINT_CHAR_UUID
import com.losfrees.hsba.simpleconnecttobluetoothmagene.MainActivity.Companion.HEART_RATE_MEASUREMENT_CHAR_UUID
import com.losfrees.hsba.simpleconnecttobluetoothmagene.MainActivity.Companion.HEART_RATE_SERVICE_UUID
import java.util.UUID

class BluettothLeServiceLos : Service() {

   private val binder = LocalBinder()
   private var connectionState = STATE_DISCONNECTED

   private var bluetoothAdapter : BluetoothAdapter? = null
   private var bluetoothGatt: BluetoothGatt? = null


      override fun onBind(p0: Intent?): IBinder? {

        return binder
    }


      inner class LocalBinder : Binder() {
        fun getService() : BluettothLeServiceLos {
            return this@BluettothLeServiceLos
        }
    }

    fun initialize() : Boolean {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
             println(" no my adapter ! ")
            return false
        }
        return true

    }

    fun connect(address: String) : Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)

            } catch (e: Exception) {
                println(" device not found with address ")
                return false
            }

        } ?: run {

             println( " not initialize a ")
            return false
        }

        return true
    }


      private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic?, siF : Int) {
          val intent = Intent(action)
         // println(" send broadCast lo =${action}")

          if(siF == 1) {
              when (characteristic!!.uuid) {
                  HEART_RATE_MEASUREMENT_CHAR_UUID -> {

                      println(" heart are is ok ")
                      val flag = characteristic.properties
                      val format = when (flag and 0x01) {
                          0x01 -> {
                              println(" Heart rate format UINT16")
                              BluetoothGattCharacteristic.FORMAT_UINT16
                          }

                          else -> {
                              println(" Heart rate format UINT8")
                              BluetoothGattCharacteristic.FORMAT_UINT8
                          }
                      }

                      val heartRate = characteristic.getIntValue(format, 1)
                      println(" my heart rate is ${heartRate}")
                      intent.putExtra("extraData", heartRate)
                  }

                  else -> {

                      val data: ByteArray? = characteristic.value
                      if (data?.isNotEmpty() == true) {
                          val hexString: String = data.joinToString(separator = " ") {
                              String.format("%02X", it)
                          }
                          intent.putExtra("extraData", "$data\n$hexString")
                          println(" final hex LL is $data\n$hexString")
                      }
                  }

              }
          }

              sendBroadcast(intent)
      }

    fun onWriteCharacterAll(){

            bluetoothGatt?.let { gatt ->

        val characteristic = gatt.getService(HEART_RATE_SERVICE_UUID)
       .getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
        gatt.setCharacteristicNotification(characteristic, true)

       val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)

       descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
       gatt.writeDescriptor(descriptor)





            }




    }

//      fun setCharacteristicNotification(
//    characteristic: BluetoothGattCharacteristic,
//    enabled: Boolean
//    ) {
//        bluetoothGatt?.let { gatt ->
//        gatt.setCharacteristicNotification(characteristic, enabled)
//
//            println(" in notifica tion ${characteristic.uuid}")
//        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
//            println(" enable is ok ! ")
//            val descriptor = characteristic.getDescriptor(UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb"))
//            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//            gatt.writeDescriptor(descriptor)
//        }
//        } ?: run {
//            println( "BluetoothGatt not initialized")
//        }
//    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            println(" state _Connection is ok ! ")
                            connectionState = STATE_CONNECTED

                broadcastUpdate(ACTION_GATT_CONNECTED, null, 0)

            bluetoothGatt?.discoverServices()
            // successfully connected to the GATT Server
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
             println(" state _DisConnection is ok ! ")
              connectionState = STATE_DISCONNECTED

             broadcastUpdate(ACTION_GATT_DISCONNECTED, null, 0)
            // disconnected from the GATT Server
        }
    }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            println(" in ok ser dis 01 ")
            if(status == BluetoothGatt.GATT_SUCCESS){
                            println(" in ok ser dis 02 ")

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null, 0)
            }
        }

           @Deprecated("Deprecated in Java")
           override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, 1)
            }
        }

           @Deprecated("Deprecated in Java")
           override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
        ) {
               println("Chagne my data ")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, 1)
        }

        @Deprecated("Deprecated in Java", ReplaceWith(
            "super.onCharacteristicWrite(gatt, characteristic, status)",
            "android.bluetooth.BluetoothGattCallback"
        )
        )
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            println(" my write is on ")

                 bluetoothGatt?.let { gatt ->

                     val characteristic = gatt.getService(HEART_RATE_SERVICE_UUID)
                         .getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)

                     gatt.setCharacteristicNotification(characteristic, true)
                 }

        }


        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

    println(" descrip write ! ")
                        bluetoothGatt?.let{

    val characteristic =  it.getService(HEART_RATE_SERVICE_UUID).getCharacteristic(HEART_RATE_CONTROL_POINT_CHAR_UUID)
        characteristic.setValue(byteArrayOf(1,1))
       it.writeCharacteristic(characteristic)
          }

        }

    }



     override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            gatt.close()
            bluetoothGatt = null
        }
    }

    fun getSupportedGattServices(): List<BluetoothGattService>? {
      return bluetoothGatt?.services
  }


      fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            gatt.readCharacteristic(characteristic)
        } ?: run {
            println( "BluetoothGatt not initialized")
            return
        }
    }

     companion object {
        const val ACTION_GATT_CONNECTED =
            "com.losfrees.hsba.simpleconnecttobluetoothmagene.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.losfrees.hsba.simpleconnecttobluetoothmagene.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.losfrees.hsba.simpleconnecttobluetoothmagene.ACTION_GATT_SERVICES_DISCOVERED"
         const val ACTION_DATA_AVAILABLE =
             "com.losfrees.hsba.simpleconnecttobluetoothmagene.ACTION_DATA_AVAILABLE"

        // val UUID_HEART_RATE_MEASUREMENT = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }


}