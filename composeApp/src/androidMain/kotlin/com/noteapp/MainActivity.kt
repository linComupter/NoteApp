package com.noteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.noteapp.shared.data.local.DatabaseDriverFactory
import com.noteapp.shared.di.sharedModule
import com.noteapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(applicationContext)
            modules(
                module { single { DatabaseDriverFactory(androidContext()) } },
                sharedModule,
                appModule
            )
        }

        setContent { App() }
    }
}
