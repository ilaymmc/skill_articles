package ru.skillbranch.skillarticles

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SkillArticlesApplication : Application() {

    @Override
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}