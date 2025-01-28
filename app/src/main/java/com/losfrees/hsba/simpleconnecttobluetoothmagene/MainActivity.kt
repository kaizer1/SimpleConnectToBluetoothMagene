package com.losfrees.hsba.simpleconnecttobluetoothmagene

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.MeasureCapabilities
import androidx.health.services.client.getCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.losfrees.hsba.simpleconnecttobluetoothmagene.co

class MainActivity : AppCompatActivity() {


    private val SCAN_PERIOD: Long = 30000 // 3 seconds
    var bluetoothGatt : BluetoothGatt? = null
    lateinit var bluetoothLeScanner : BluetoothLeScanner
    var scanning = false
    lateinit var handler : Handler
     var addressMy : String = ""
    var connected  = false
    private lateinit var buttonExit : Button


    private var bluetoothServiceL : BluettothLeServiceLos? = null

 private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {

                        println(" Connect service 0001 ")


            bluetoothServiceL = (service as BluettothLeServiceLos.LocalBinder).getService()
            bluetoothServiceL?.let { bluetooth ->
                // call functions on service to check connection and connect to devices
                if (!bluetooth.initialize()) {
                    println( "Unable to initialize Bluetooth")
                    finish()
                }

                println(" ok initialize and connect ")
                bluetooth.connect(addressMy)


            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {

            println(" disconnect service 0001 ")
            bluetoothServiceL = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // check bluetooth request

//           val gattServiceIntent = Intent(baseContext, BluettothLeServiceLos::class.java)
//            bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        buttonExit = findViewById<Button?>(R.id.button_exit)

        buttonExit.setOnClickListener {
            println(" exit all ")
              unbindService(serviceConnection)
        }

//        buttonExit = findViewById<Button>(R.id.button_exit).setOnClickListener (
//
//            println( " exit in apps ")
//
//        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                3
            )
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ),
                    3
                )
            }
        }





    val bluetoothAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
     if(bluetoothAvailable){
//          println(" ok available ! avai ")
     }

        handler = Handler()
        val bluetoothLEAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

            if(bluetoothLEAvailable){
                 bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
                scanLeDevice()
            }
        }


    private fun scanLeDevice() {
    if (!scanning) { // Stops scanning after a pre-defined scan period.
        handler.postDelayed({
            scanning = false
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@postDelayed
            }
            bluetoothLeScanner.stopScan(leScanCallback)
        }, SCAN_PERIOD)
        scanning = true
        bluetoothLeScanner.startScan(leScanCallback)
    } else {
        scanning = false
        bluetoothLeScanner.stopScan(leScanCallback)
    }
}


    private val leScanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

            if(result.device.name == "34224-1"){
            bluetoothLeScanner.stopScan(this)
            addressMy = result.device.address

                val gattServiceIntent = Intent(baseContext, BluettothLeServiceLos::class.java)
            bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
}


     private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {

                BluettothLeServiceLos.ACTION_GATT_CONNECTED -> {
                    connected = true
                }

                BluettothLeServiceLos.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                }

                BluettothLeServiceLos.ACTION_GATT_SERVICES_DISCOVERED -> {
                     displayAllServices(bluetoothServiceL?.getSupportedGattServices())
                }

                BluettothLeServiceLos.ACTION_DATA_AVAILABLE -> {
                    // println("DATA_AVAILABLE is ok ! ")
                }

            }
        }
    }


   private  fun displayAllServices(gattServices: List<BluetoothGattService>? ){

       if(gattServices == null) return


//
//       var intS = 0
//       gattServices.forEach { gattSer ->
//
//          // intS++
//           println(" my ${intS++}")
//           // my services = 6
//           // 00001800-0000-1000-8000-00805f9b34fb
//
//           println(" my service UUID  == ${gattSer.uuid.toString()}")
//           println(" Name = ${gattSer.describeContents()}")
//
//
//
//            gattSer.let { get ->
//
//
//
//
//                get.characteristics.forEach {
//
//              //   bluetoothServiceL!!.setCharacteristicNotification(it, true)
//                    println(" my uuid in descp ${it.uuid} ")
//                    println(" my descp in des ${it.descriptors.size} ")
//                    println(" my proper in dex ${it.properties} ")
//                }
//            }
//       }

         bluetoothServiceL!!.onWriteCharacterAll()

   }

    override fun onResume() {
        super.onResume()

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(), RECEIVER_NOT_EXPORTED)
        if (bluetoothServiceL != null) {
            val result = bluetoothServiceL!!.connect(addressMy)
           // Log.d("df","Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluettothLeServiceLos.ACTION_GATT_CONNECTED)
            addAction(BluettothLeServiceLos.ACTION_GATT_DISCONNECTED)
            addAction(BluettothLeServiceLos.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluettothLeServiceLos.ACTION_DATA_AVAILABLE)
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

         if(requestCode == requestCode ){
              if(grantResults.isNotEmpty()  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
              {
                  scanLeDevice()
              }
         }

    }



 companion object {
            const val RequestRerisionL = 101
            fun Any.log(message: String) {
                Log.d(this::class.java.simpleName, message)
            }


     val co1 = co()
   val HEART_RATE_SERVICE_UUID = co1.convertFromInteger(0x180D)
   val HEART_RATE_MEASUREMENT_CHAR_UUID = co1.convertFromInteger(0x2A37)
   val HEART_RATE_CONTROL_POINT_CHAR_UUID =co1.convertFromInteger(0x2A38) // ??
   val CLIENT_CHARACTERISTIC_CONFIG_UUID = co1.convertFromInteger(0x2902)

        }
}