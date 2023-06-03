package com.davidvelez.PetDay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.davidvelez.petday.R
import com.davidvelez.petday.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding : ActivityMainBinding

    private lateinit var mainActiveFragment: Fragment
    private lateinit var mainFragmentManager: FragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        setupBottomNav()
    }

    private fun setupBottomNav(){

        mainFragmentManager = supportFragmentManager
        mainFragmentManager.beginTransaction().add(R.id.hostFragment, HomeFragment()).commit()


    }
}