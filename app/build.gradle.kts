plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
 id("com.google.devtools.ksp")
 id("org.jetbrains.kotlin.plugin.serialization")
}
android {
 namespace = "com.local.bookkeeping"
 compileSdk = 36
 defaultConfig {
  applicationId = "com.local.bookkeeping"
  minSdk = 26
  targetSdk = 36
  versionCode = 1
  versionName = "1.0.0"
 }
 testOptions { unitTests.isIncludeAndroidResources = true }
 buildFeatures { compose = true }
 compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2025.10.01"))
 implementation("androidx.activity:activity-compose:1.11.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
 implementation("androidx.navigation:navigation-compose:2.9.5")
 implementation("androidx.room:room-runtime:2.8.4")
 implementation("androidx.room:room-ktx:2.8.4")
 ksp("androidx.room:room-compiler:2.8.4")
 implementation("androidx.datastore:datastore-preferences:1.1.7")
 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
 implementation("com.squareup.retrofit2:retrofit:2.11.0")
 implementation("com.squareup.okhttp3:okhttp:4.12.0")
 implementation("androidx.core:core-splashscreen:1.0.1")
 testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
 testImplementation("org.robolectric:robolectric:4.16")
 testImplementation("androidx.test:core:1.7.0")
 testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
 testImplementation("junit:junit:4.13.2")
}
