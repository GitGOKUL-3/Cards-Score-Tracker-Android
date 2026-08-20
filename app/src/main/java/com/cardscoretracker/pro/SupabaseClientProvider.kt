package com.cardscoretracker.pro

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Provides a lazily-initialized Supabase client singleton for the app.
 *
 * Credentials are injected from local.properties via BuildConfig at compile time.
 * The service_role key is NEVER used here — only the publishable (anon) key.
 *
 * Currently installs:
 *  - [Postgrest] for database access
 *  - [Auth] for future authentication support
 *
 * No tables are queried and no auth flows are triggered from this file.
 */
object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
