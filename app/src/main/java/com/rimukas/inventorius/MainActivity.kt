package com.rimukas.inventorius

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.spinner.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.*
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    var connection: Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        var errorChanged: String by Delegates.observable("") { _, oldValue, newValue ->
                errorMsg.text = newValue
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            if(connection != null){
                val c = connection!!.catalog
                Snackbar.make(view, "Connected $c", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }

        }

//        test_button.setOnClickListener {
//            if(connection != null){
//                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
//            }
//        }







        //pref.edit().putString("ddd", "ddd")
      //  if(txt == "") {
      //      Toast.makeText(this, "einu i settings", Toast.LENGTH_SHORT).show()
       //     startSettingsActivity()
     //   }

    }

    override fun onResume() {
        super.onResume()

        if(connection == null){
            GlobalScope.launch{
                connectToSQLServer()
            }
        }
    }

    private fun startSettingsActivity(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }


    suspend fun connectToSQLServer() = withContext(Dispatchers.IO) {

        val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val username = pref.getString("username","")
        val password = pref.getString("password", "")
        val ip = pref.getString("ip", "")
        val db = pref.getString("db", "")
        //DriverManager.registerDriver(com.microsoft.sqlserver.jdbc.SQLServerDriver())
        val driver = "net.sourceforge.jtds.jdbc.Driver"
        Class.forName(driver).newInstance()
        val dbURL = "jdbc:jtds:sqlserver://${ip}/${db};user=${username};password=${password}"
        println("-------------- trying to connect to SQL....")
        //val psw = "uaadmin/1"

        showErrorMsg("") // išvalo rodomą klaidos pranešimą

        try{
            connection = DriverManager.getConnection(dbURL)    //("jdbc:jtds:sqlserver://10.12.0.51:1433/Inventorizacija;user=ua;password=a")//(dbURL)
        }
        catch (ex: SQLException){
            ex.message?.let { showErrorMsg(it) }
            println("---------------------------- Connection failed")
         //   startSettingsActivity()
        } finally {
            if(connection != null){
               hideSpinner()
            }
        }



/*
        val query = "SELECT * FROM iranga"
        val statement: Statement = connection.createStatement()
        val resultSet: ResultSet = statement.executeQuery(query)
        println("----------------: $resultSet")
        while (resultSet.next()){
            print(resultSet.getString("invsernr") + " -> " + resultSet.getString("iranga") + " - " + resultSet.getString("pastabos"))
            println()

        }

 */
    }

    suspend fun showErrorMsg(msg: String) = withContext(Dispatchers.Main){
        errorMsg.text = msg
    }

    suspend fun hideSpinner() = withContext(Dispatchers.Main){
        spinner.visibility = View.GONE
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