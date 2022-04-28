package com.vdotok.app.constants


/**
 * Created By: VdoTok
 * Date & Time: On 17/11/2021 At 12:36 PM in 2021
 */

//    SDK AUTH PARAMS
const val SDK_PROJECT_ID = "Please Enter your project id here"


//    GROUP CONSTANTS
const val MAX_PARTICIPANTS = 4 // max limit is 4 so including current user we can add up to 3 more users in a group


// This error code means a local error occurred while parsing the received json.


//    File paths
const val type = "profile_pic"
const val IMAGES_DIRECTORY = "/cPass/images"
const val VIDEO_DIRECTORY = "/cPass/videos"
const val AUDIO_DIRECTORY = "/cPass/audios"
const val DOCS_DIRECTORY = "/cPass/docs"
const val CACHE_DIRECTORY_NAME = "cacheFiles"
const val directoryName: String = "Vdotok-chat"
val docMimeType = arrayOf("application/msword",
"application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .doc & .docx
"application/vnd.ms-powerpoint",
"application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .ppt & .pptx
"application/vnd.ms-excel",
"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx
"text/plain",
"application/pdf",
"application/zip",
"application/vnd.android.package-archive")