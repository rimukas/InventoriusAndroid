package com.rimukas.inventorius

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
//import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.fragment_result.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ResultFragment : Fragment() {
    val PREFS_NAME = "sharedPreffs"
    var myPref: SharedPreferences? = null
    lateinit var strtext: String

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        strtext = arguments?.get("invnr").toString() //.getString("edttext")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txt_invnr.text = strtext

        view.findViewById<Button>(R.id.btn_inventorizacija).setOnClickListener {
           // findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            findNavController().navigate(R.id.MainActivity)
        }



      //  val up = UserPreferences()
      //  val prefsMap = up.getPrefs(requireActivity())

        //if(prefsMap["ip"] == ""){
      //      findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
     //   }



    }
}