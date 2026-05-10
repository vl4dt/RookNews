# Keep KMP common code
-keep class com.rooknews.domain.model.** { *; }
-keep class com.rooknews.domain.repository.** { *; }

# Keep Koin modules
-keep class * extends org.koin.core.module.Module { *; }
-keep class * extends org.koin.core.instance.InstanceType { *; }

# Keep serialization
-keep class kotlinx.serialization.** { *; }

# Ktor
-keep class io.ktor.** { *; }
