package com.example.mediaaudiosample.media

import android.Manifest.permission.MEDIA_CONTENT_CONTROL
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.os.Process
import android.util.Base64
import android.util.Log
import androidx.annotation.XmlRes
import androidx.core.app.NotificationManagerCompat
import okio.IOException
import org.xmlpull.v1.XmlPullParserException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PackageValidator(context: Context, @XmlRes xmlResId: Int) {
    private val context: Context
    private val packageManager: PackageManager

    private val certificateAllowList: Map<String, KnownCallerInfo>
    private val platformSignature: String

    private val callerChecked = mutableMapOf<String, Pair<Int, Boolean>>()

    init {
        val parser = context.resources.getXml(xmlResId)
        this.context = context.applicationContext
        this.packageManager = this.context.packageManager

        certificateAllowList = buildCertificateAllowList(parser)
        platformSignature = getSystemSignature()
    }

    fun isKnownCaller(callingPackage: String, callingUid: Int): Boolean {
        // If the caller has already been checked, return the previous result here.
        val (checkedUid, checkResult) = callerChecked[callingPackage] ?: Pair(0, false)
        if (checkedUid == callingUid) {
            return checkResult
        }


        // Build the caller info for the rest of the checks here.
        val callerPackageInfo = buildCallerInfo(callingPackage)
            ?: throw IllegalStateException("Caller wasn't found in the system?")

        // Verify that things aren't ... broken. (This test should always pass.)
        if (callerPackageInfo.uid != callingUid) {
            throw IllegalStateException("Caller's package UID doesn't match caller's actual UID?")
        }

        val callerSignature = callerPackageInfo.signature
        val isPackageInAllowList = certificateAllowList[callingPackage]?.signatures?.first {
            it.signature == callerSignature
        } != null

        val isCallerKnown = when {
            // If it's our own app making the call, allow it.
            callingUid == Process.myUid() -> true
            // If it's one of the apps on the allow list, allow it.
            isPackageInAllowList -> true
            // If the system is making the call, allow it.
            callingUid == Process.SYSTEM_UID -> true
            // If the app was signed by the same certificate as the platform itself, also allow it.
            callerSignature == platformSignature -> true

            callerPackageInfo.permissions.contains(MEDIA_CONTENT_CONTROL) -> true

            NotificationManagerCompat.getEnabledListenerPackages(this.context)
                .contains(callerPackageInfo.packageName) -> true

            // If none of the previous checks succeeded, then the caller is unrecognized.
            else -> false
        }

        if (!isCallerKnown) {
            logUnknownCaller(callerPackageInfo)
        }

        // Save our work for next time.
        callerChecked[callingPackage] = Pair(callingUid, isCallerKnown)
        return isCallerKnown
    }

    /**
     * Logs an info level message with details of how to add a caller to the allowed callers list
     * when the app is debuggable.
     */
    private fun logUnknownCaller(callerPackageInfo: CallerPackageInfo) {
        Log.i(
            TAG,
            "name:${callerPackageInfo.name}, packageName:${callerPackageInfo.packageName}, signature:${callerPackageInfo.signature}"
        )
    }

    private fun buildCallerInfo(callingPackage: String): CallerPackageInfo? {
        val packageInfo = getPackageInfo(callingPackage) ?: return null

        val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
        val uid = packageInfo.applicationInfo.uid
        val signature = getSignature(packageInfo)

        val requestedPermissions = packageInfo.requestedPermissions
        val permissionFlags = packageInfo.requestedPermissionsFlags
        val activePermissions = mutableSetOf<String>()
        requestedPermissions?.forEachIndexed { index, permission ->
            if (permissionFlags[index] and REQUESTED_PERMISSION_GRANTED != 0) {
                activePermissions += permission
            }
        }

        return CallerPackageInfo(appName, callingPackage, uid, signature, activePermissions.toSet())
    }


    @Suppress("deprecation")
    @SuppressLint("PackageManagerGetSignatures")
    private fun getPackageInfo(callingPackage: String): PackageInfo? =
        packageManager.getPackageInfo(
            callingPackage,
            PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS
        )

    @Suppress("deprecation")
    private fun getSignature(packageInfo: PackageInfo): String? =
        if (packageInfo.signatures == null || packageInfo.signatures.size != 1) {
            // Security best practices dictate that an app should be signed with exactly one (1)
            // signature. Because of this, if there are multiple signatures, reject it.
            null
        } else {
            val certificate = packageInfo.signatures[0].toByteArray()
            getSignatureSha256(certificate)
        }

    private fun buildCertificateAllowList(parser: XmlResourceParser): Map<String, KnownCallerInfo> {

        val certificateAllowList = LinkedHashMap<String, KnownCallerInfo>()
        try {
            var eventType = parser.next()
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    val callerInfo = when (parser.name) {
                        "signing_certificate" -> parseV1Tag(parser)
                        "signature" -> parseV2Tag(parser)
                        else -> null
                    }

                    callerInfo?.let { info ->
                        val packageName = info.packageName
                        val existingCallerInfo = certificateAllowList[packageName]
                        if (existingCallerInfo != null) {
                            existingCallerInfo.signatures += callerInfo.signatures
                        } else {
                            certificateAllowList[packageName] = callerInfo
                        }
                    }
                }

                eventType = parser.next()
            }
        } catch (xmlException: XmlPullParserException) {
            Log.e(TAG, "Could not read allowed callers from XML.", xmlException)
        } catch (ioException: IOException) {
            Log.e(TAG, "Could not read allowed callers from XML.", ioException)
        }

        return certificateAllowList
    }

    /**
     * Parses a v1 format tag. See allowed_media_browser_callers.xml for more details.
     */
    private fun parseV1Tag(parser: XmlResourceParser): KnownCallerInfo {
        val name = parser.getAttributeValue(null, "name")
        val packageName = parser.getAttributeValue(null, "package")
        val isRelease = parser.getAttributeBooleanValue(null, "release", false)
        val certificate = parser.nextText().replace(WHITESPACE_REGEX, "")
        val signature = getSignatureSha256(certificate)

        val callerSignature = KnownSignature(signature, isRelease)
        return KnownCallerInfo(name, packageName, mutableSetOf(callerSignature))
    }

    /**
     * Parses a v2 format tag. See allowed_media_browser_callers.xml for more details.
     */
    private fun parseV2Tag(parser: XmlResourceParser): KnownCallerInfo {
        val name = parser.getAttributeValue(null, "name")
        val packageName = parser.getAttributeValue(null, "package")

        val callerSignatures = mutableSetOf<KnownSignature>()
        var eventType = parser.next()
        while (eventType != XmlResourceParser.END_TAG) {
            val isRelease = parser.getAttributeBooleanValue(null, "release", false)
            val signature = parser.nextText().replace(WHITESPACE_REGEX, "").toLowerCase()
            callerSignatures += KnownSignature(signature, isRelease)

            eventType = parser.next()
        }

        return KnownCallerInfo(name, packageName, callerSignatures)
    }

    /**
     * Finds the Android platform signing key signature. This key is never null.
     */
    private fun getSystemSignature(): String =
        getPackageInfo(ANDROID_PLATFORM)?.let { platformInfo ->
            getSignature(platformInfo)
        } ?: throw IllegalStateException("Platform signature not found")


    private fun getSignatureSha256(certificate: String): String {
        return getSignatureSha256(Base64.decode(certificate, Base64.DEFAULT))
    }

    /**
     * Creates a SHA-256 signature given a certificate byte array.
     */
    private fun getSignatureSha256(certificate: ByteArray): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA256")
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            Log.e(TAG, "No such algorithm: $noSuchAlgorithmException")
            throw RuntimeException("Could not find SHA256 hash algorithm", noSuchAlgorithmException)
        }
        md.update(certificate)

        // This code takes the byte array generated by `md.digest()` and joins each of the bytes
        // to a string, applying the string format `%02x` on each digit before it's appended, with
        // a colon (':') between each of the items.
        // For example: input=[0,2,4,6,8,10,12], output="00:02:04:06:08:0a:0c"
        return md.digest().joinToString(":") { String.format("%02x", it) }
    }

    private data class KnownCallerInfo(
        internal val name: String,
        internal val packageName: String,
        internal val signatures: MutableSet<KnownSignature>
    )

    private data class KnownSignature(
        internal val signature: String,
        internal val release: Boolean
    )

    private data class CallerPackageInfo(
        internal val name: String,
        internal val packageName: String,
        internal val uid: Int,
        internal val signature: String?,
        internal val permissions: Set<String>
    )
}

private const val TAG = "PackageValidator"
private const val ANDROID_PLATFORM = "android"
private val WHITESPACE_REGEX = "\\s|\\n".toRegex()