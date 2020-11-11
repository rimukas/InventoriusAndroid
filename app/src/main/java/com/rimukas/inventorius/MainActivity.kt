package com.rimukas.inventorius

//import android.R
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.budiyev.android.codescanner.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.camera_layout.*
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.spinner_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.*
import java.sql.Date
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private val MY_CAMERA_PERMISSION_CODE = 1234
    var connection: Connection? = null
    private var dataFromCamera: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // setSupportActionBar(findViewById(R.id.toolbar))

        spinner.visibility = View.VISIBLE
        fragment_result.visibility = View.GONE
        /*
        var errorChanged: String by Delegates.observable("") { _, oldValue, newValue ->
                errorMsg.text = newValue
        }
*/
        btn_inventorizacija.setOnClickListener{
            //val date: Date = Calendar.getInstance().time as Date
            val date = Date(Calendar.getInstance().timeInMillis)
            // run corutine
            GlobalScope.launch {
                updateSQLTable(dataFromCamera.toString(), date)
            }

            fragment_result.visibility = View.GONE
            camera.visibility = View.VISIBLE
            checkPermission()
        }

                btn_back.setOnClickListener {
                    fragment_result.visibility = View.GONE
                    camera.visibility = View.VISIBLE
                    checkPermission()
                }

        btn_hide_keyboard.setOnClickListener{
        // tik paslepia klaviatūrą
            val view: View = findViewById(R.id.txt_pastabos)
            hideKeyboard(view)
        }

        // neveikia, nes neišėjo permesti fokuso ant kokio nors kito View
        txt_pastabos.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            println("---------------- focus change") // DEBUG
            if (!hasFocus) {
                // code to execute when EditText loses focus
                hideKeyboard(v)
            }
        }


        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                dataFromCamera = it.text
                GlobalScope.launch {
                    executeSQL(dataFromCamera.toString())
                }


                /*
                // pass arguments to Fragment
                val bundle = Bundle()
                bundle.putString("invnr", it.text)

                val fragment: Fragment = ResultFragment()
                fragment.arguments = bundle
                val fm: FragmentManager = supportFragmentManager
                val transaction: FragmentTransaction = fm.beginTransaction()
                transaction.replace(R.id.fragment_placeholder, fragment)
                transaction.commit()
                */

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

    }



    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private suspend fun updateSQLTable(id: String, dateTime: Date) = withContext(Dispatchers.IO) {
            val query = "UPDATE iranga SET inv_data='$dateTime', pastabos='${txt_pastabos.text}' WHERE invsernr='$id'"
            val statement: Statement = connection!!.createStatement()
            //val resultSet: ResultSet = statement.executeQuery(query)
        statement.executeUpdate(query)

    }

    // run on IO Thread
    private suspend fun executeSQL(cameraString: String) = withContext(Dispatchers.IO) {
        val query = "" +
                "SELECT ir.invsernr, ir.iranga, ir.[data], ir.ilgalaikis, pr.darbuotojas, ir.pastabos, ir.inv_data\n" +
                "FROM iranga ir\n" +
                "INNER JOIN priklauso pr ON pr.invsernr=ir.invsernr\n" +
                "WHERE ir.invsernr='$cameraString'"
        val statement: Statement = connection!!.createStatement()
        //val resultSet: ResultSet = statement.executeQuery(query)
        val resultSet: ResultSet = statement.executeQuery(query)
        showSQLresult(resultSet)
    }

    // run on Main Thread
    private suspend fun showSQLresult(resultSet: ResultSet) = withContext(Dispatchers.Main) {
        if(resultSet.next()){
            camera.visibility = View.GONE
            fragment_result.visibility = View.VISIBLE

            // išvalom teksto laukelius nuo praeitų duomenų
            txt_invnr.text = "-"
            txt_iranga.text = "-"
            txt_darbuotojas.text = "-"
            txt_data.text= "-"
            txt_invdata.text = "-"
            txt_pastabos.setText("")
            // ---------------------

            txt_invnr.text = resultSet.getString("invsernr")
            txt_iranga.text = resultSet.getString("iranga")
            txt_darbuotojas.text = resultSet.getString("darbuotojas")
            resultSet.getString("data")?.let {txt_data.text=it  }
            resultSet.getDate("inv_data")?.let { txt_invdata.text = it.toString() }
            txt_pastabos.setText(resultSet.getString("pastabos"))
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode==MY_CAMERA_PERMISSION_CODE&&grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
            codeScanner.startPreview()
        } else {
            Snackbar.make(fragment_placeholder, "Negaliu skenuoti kol nebus duotas kameros leidimas", Snackbar.LENGTH_LONG).show()
           // Toast.makeText(this, "Negaliu skenuoti kol nebus duotas kameros leidimas", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_PERMISSION_CODE)
        } else
            codeScanner.startPreview()
    }




    override fun onResume() {
        super.onResume()
        if(connection == null){
            GlobalScope.launch{
                connectToSQLServer()
            }
        }
        checkPermission()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun startSettingsActivity(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }


    private suspend fun connectToSQLServer() = withContext(Dispatchers.IO) {

        val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val username = pref.getString("username", "")
        val password = pref.getString("password", "")
        val ip = pref.getString("ip", "")
        val db = pref.getString("db", "")
        //DriverManager.registerDriver(com.microsoft.sqlserver.jdbc.SQLServerDriver())
        val driver = "net.sourceforge.jtds.jdbc.Driver"
        Class.forName(driver).newInstance()
        val dbURL = "jdbc:jtds:sqlserver://${ip}/${db};user=${username};password=${password}"

        showErrorMsg("") // išvalo rodomą klaidos pranešimą

        try{
            connection = DriverManager.getConnection(dbURL)
        }
        catch (ex: SQLException){
            ex.message?.let { showErrorMsg(it) }
         //   startSettingsActivity()
        } finally {
            if(connection != null){
               hideSpinner()            }
        }
    }

    private suspend fun showErrorMsg(msg: String) = withContext(Dispatchers.Main){
        errorMsg.text = msg
    }

    private suspend fun hideSpinner() = withContext(Dispatchers.Main){
        spinner.visibility = View.GONE
        camera.visibility = View.VISIBLE
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        val id = item.itemId

        if(id == R.id.btn_action_settings){
            // start settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        return when (item.itemId) {
            R.id.btn_action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}