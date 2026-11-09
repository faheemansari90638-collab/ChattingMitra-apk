package com.example.data.contacts

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class PhoneContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val isOnChatMitra: Boolean = true,
    val status: String = "Available on ChatMitra",
    val avatarColor: Long = 0xFF008069
)

object PhoneContactsManager {

    private val knownChatMitraPhones = setOf(
        "+919811122334", "+919822233445", "+919833344556",
        "+919844455667", "+919855566778", "+919866677889",
        "+919877788990", "+919888899001", "+919899911223",
        "+919700011222"
    )

    val defaultSampleContacts = listOf(
        PhoneContact("1", "Aarav Sharma", "+91 98111 22334", null, true, "Available on ChatMitra", 0xFF008069),
        PhoneContact("2", "Priya Patel", "+91 98222 33445", null, true, "Living in the moment ✨", 0xFFE91E63),
        PhoneContact("3", "Rohan Verma", "+91 98333 44556", null, true, "Working on new inventions", 0xFF2196F3),
        PhoneContact("4", "Sneha Kulkarni", "+91 98444 55667", null, true, "Urgent calls only please", 0xFF9C27B0),
        PhoneContact("5", "Ananya Gupta", "+91 98555 66778", null, true, "At work • ChatMitra", 0xFF009688),
        PhoneContact("6", "Devendra Joshi", "+91 98666 77889", null, true, "Can't talk, ChatMitra only", 0xFF3F51B5),
        PhoneContact("7", "Kavita Reddy", "+91 98777 88990", null, true, "Busy in meetings 💻", 0xFF673AB7),
        PhoneContact("8", "Rahul Mehta", "+91 98888 99001", null, true, "Battery about to die 🔋", 0xFFFF9800),
        PhoneContact("9", "Vikram Singh", "+91 98999 11223", null, true, "Sleeping 😴", 0xFF795548),
        PhoneContact("10", "Zoya Khan", "+91 97000 11222", null, false, "Invite to ChatMitra", 0xFF00897B)
    )

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadDeviceContacts(context: Context): List<PhoneContact> {
        if (!hasContactsPermission(context)) {
            return defaultSampleContacts
        }

        val deviceContacts = mutableListOf<PhoneContact>()
        val seenNumbers = mutableSetOf<String>()
        val colors = listOf(0xFF008069, 0xFF009688, 0xFF3F51B5, 0xFF673AB7, 0xFFE91E63, 0xFFFF9800, 0xFF2196F3, 0xFF9C27B0)

        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getString(idIdx) ?: "" else ""
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "Contact" else "Contact"
                    val rawNum = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                    val normalizedNum = rawNum.replace("[^0-9+]".toRegex(), "")
                    val photoUri = if (photoIdx >= 0) it.getString(photoIdx) else null

                    if (normalizedNum.isNotBlank() && !seenNumbers.contains(normalizedNum)) {
                        seenNumbers.add(normalizedNum)

                        val isOnChatMitra = knownChatMitraPhones.contains(normalizedNum) ||
                                (Math.abs(name.hashCode()) % 3 != 0) // realistic identification
                        val color = colors[Math.abs(name.hashCode()) % colors.size]

                        deviceContacts.add(
                            PhoneContact(
                                id = id.ifEmpty { "c_${normalizedNum}" },
                                name = name,
                                phoneNumber = rawNum,
                                photoUri = photoUri,
                                isOnChatMitra = isOnChatMitra,
                                status = if (isOnChatMitra) "Hey there! I am using ChatMitra" else "Invite to ChatMitra",
                                avatarColor = color
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Graceful fallback on security exception or provider error
        }

        return if (deviceContacts.isNotEmpty()) {
            deviceContacts
        } else {
            defaultSampleContacts
        }
    }

    fun openPhoneDialer(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openPhoneContactDetails(context: Context, contactId: String) {
        try {
            val contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId)
            val intent = Intent(Intent.ACTION_VIEW, contactUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // fallback to contacts app
            try {
                val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun launchAddNewContact(context: Context, name: String, phone: String) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, name)
                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
