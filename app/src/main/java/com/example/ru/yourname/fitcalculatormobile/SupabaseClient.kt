package com.example.ru.yourname.fitcalculatormobile

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        defaultLogLevel = LogLevel.DEBUG

        install(Auth)
        install(Postgrest)
    }
}