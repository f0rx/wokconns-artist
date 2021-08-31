package com.wokconns.wokconns.interfacess

interface Const {
    companion object {
        // Google Console APIs developer key
        // Replace this key with your's
//        const val DEVELOPER_KEY = "AIzaSyArfMm1YCgq6FCtxA7w_W-pOxJ_0D6GGy8"
        const val APP_NAME = "Artisan"

        //old 677440
        const val DOMAIN_URL = "https://wms.wokconns.com/"
        const val BASE_URL = DOMAIN_URL + "Webservice/"
        const val PAYMENT_FAIL = "https://wms.wokconns.com/Stripe/Payment/fail"
        const val PAYMENT_SUCCESS = "https://wms.wokconns.com/Stripe/Payment/success"
        const val MAKE_PAYMENT = "https://wms.wokconns.com/Stripe/Payment/make_payment/"
        const val PAYMENT_FAIL_Paypal = "https://wms.wokconns.com/Webservice/payufailure"
        const val PAYMENT_SUCCESS_paypal = "https://wms.wokconns.com/Webservice/payusuccess"
        const val MAKE_PAYMENT_paypal = "https://wms.wokconns.com/Webservice/paypalWallent?"
        const val PRIVACY_URL = "privacyPolicy"
        const val FAQ_URL = "faq"
        const val TERMS_URL = "termsCondition"

        /*Api Details*/
        const val LOGIN_API = "signIn"
        const val REGISTER_API = "SignUp"
        const val GET_REFERRAL_CODE_API = "getMyReferralCode"
        const val GET_CHAT_HISTORY_API = "getChatHistoryForArtist"
        const val GET_CHAT_API = "getChat"
        const val SEND_CHAT_API = "sendmsg"
        const val GET_NOTIFICATION_API = "getNotifications"
        const val GET_ARTIST_BY_ID_API = "getArtistByid"
        const val UPDATE_PROFILE_API = "editPersonalInfo"
        const val UPDATE_PROFILE_ARTIST_API = "artistPrsonalInfo"
        const val ARTIST_IMAGE_API = "artistImage"
        const val DELETE_PRODUCT = "deleteProduct"
        const val GET_ALL_CATEGORY_API = "getAllCaegory"
        const val GET_ALL_SKILLS_BY_CAT_API = "getSkillsByCategory"
        const val ADD_QUALIFICATION_API = "addQualification"
        const val ADD_PRODUCT_API = "addProduct"
        const val ADD_GALLERY_API = "addGallery"
        const val GET_INVOICE_API = "getMyInvoice"
        const val CURRENT_BOOKING_API = "getMyCurrentBooking"
        const val BOOKING_OPERATION_API = "booking_operation"
        const val DECLINE_BOOKING_API = "decline_booking"
        const val ONLINE_OFFLINE_API = "onlineOffline"
        const val UPDATE_LOCATION_API = "updateLocation"
        const val ARTIST_LOGOUT_API = "artistLogout"
        const val GET_MY_TICKET_API = "getMyTicket"
        const val GENERATE_TICKET_API = "generateTicket"
        const val GET_TICKET_COMMENTS_API = "getTicketComments"
        const val ADD_TICKET_COMMENTS_API = "addTicketComments"
        const val FORGET_PASSWORD_API = "forgotPassword"
        const val GET_APPOINTMENT_API = "getAppointment"
        const val EDIT_APPOINTMENT_API = "edit_appointment"
        const val APPOINTMENT_OPERATION_API = "appointment_operation"
        const val GET_ALL_JOB_API = "get_all_job"
        const val VERIFY_PHONE = "verifyMobile"
        const val RESEND_VERIFY_OTP_CODE = "resendMobileOtp"
        const val APPLIED_JOB_API = "applied_job"
        const val JOB_STATUS_ARTIST_API = "job_status_artist"
        const val GET_APPLIED_JOB_ARTIST_API = "get_applied_job_artist"
        const val CHANGE_COMMISSION_ARTIST_API = "changeCommissionArtist"
        const val START_JOB_API = "startJob"
        const val DELETE_PROFILE_IMAGE_API = "deleteProfileImage"
        const val MY_EARNING1_API = "myEarning1"
        const val WALLET_REQUEST_API = "walletRequest"
        const val ADD_MONEY_API = "addMoney"

        //    String GET_WALLET_HISTORY_API = "getWalletHistory";
        const val GET_WALLET_HISTORY_NEW_API = "getWalletHistoryNew"
        const val GET_WALLET_API = "getWallet"
        const val DELETE_GALLERY_API = "deleteGallery"
        const val DELETE_PRODUCT_API = "deleteProduct"
        const val UPDATE_QUALIFICATION_API = "updateQualification"
        const val DELETE_QUALIFICATION_API = "deleteQualification"
        const val GET_ALL_BOOKING_ARTIST_API = "getAllBookingArtist"
        const val GET_APPROVAL_STATUS_API = "getApprovalStatus"
        const val GET_CURRENCY_API = "getCurrency"
        const val ADD_ACCOUNT_DETAIL = "addAccountDetail"
        const val GET_ACCOUNT_DETAIL = "getAccountDetail"
        const val ARTIST_HOME_DATA = "artistHomeData"
        const val GET_PAYOUT_DATA = "getPayoutData"
        const val JOB_FILTER = "jobFilter"
        const val UPDATE_ARTISAN_RATE = "updateRate"

        /*app data*/
        const val CAMERA_ACCEPTED = "camera_accepted"
        const val STORAGE_ACCEPTED = "storage_accepted"
        const val MODIFY_AUDIO_ACCEPTED = "modify_audio_accepted"
        const val CALL_PRIVILAGE = "call_privilage"
        const val FINE_LOC = "fine_loc"
        const val CORAS_LOC = "coras_loc"
        const val CALL_PHONE = "call_phone"
        const val PAYMENT_URL = "payment_url"
        const val SURL = "surl"
        const val FURL = "furl"
        const val ARTIST_DTO = "artist_dto"
        const val CATEGORY_list = "category_list"
        const val SCREEN_TAG = "screen_tag"
        const val HISTORY_DTO = "history_dto"

        /*app data*/ /*Project Parameter*/
        const val ARTIST_ID = "artist_id"
        const val CHAT_LIST_DTO = "chat_list_dto"
        const val USER_DTO = "user_dto"
        const val IS_REGISTERED = "is_registered"
        const val IMAGE_URI_CAMERA = "image_uri_camera"
        const val DATE_FORMATE_SERVER = "EEE, MMM dd, yyyy hh:mm a" //Wed, JUL 06, 2018 04:30 pm
        const val DATE_FORMATE_TIMEZONE = "z"
        const val BROADCAST = "broadcast"
        const val LOCATION1 = "location1"
        const val PRODUCT_ID = "product_id"
        const val BOOKING_FLAG = "booking_flag"

        /*Parameter Get Artist and Search*/
        const val USER_ID = "user_id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val ID = "id"

        /*Get All History*/
        const val ROLE = "role"

        /*Send Message*/
        const val MESSAGE = "message"
        const val SEND_BY = "send_by"
        const val SENDER_NAME = "sender_name"

        /*Login Parameter*/
        const val NAME = "name"
        const val EMAIL = "email"
        const val EMAIL_ID = "email_id"
        const val OTP_CODE = "otp"
        const val PASSWORD = "password"
        const val DEVICE_TYPE = "device_type"
        const val DEVICE_TOKEN = "device_token"
        const val DEVICE_ID = "device_id"
        const val REFERRAL_CODE = "referral_code"

        /*Update Profile*/
        const val NEW_PASSWORD = "new_password"
        const val GENDER = "gender"
        const val MOBILE = "mobile"
        const val OFFICE_ADDRESS = "office_address"
        const val ADDRESS = "address"
        const val IMAGE = "image"

        /*Update Profile Artist*/
        const val CATEGORY_ID = "category_id"
        const val BIO = "bio"
        const val LOCATION = "location"
        const val PRICE = "price"
        const val ABOUT_US = "about_us"
        const val SKILLS = "skills"
        const val VIDEO_URL = "video_url"
        const val CITY = "city"
        const val COUNTRY = "country"
        const val COUNTRY_CODE = "country_code"
        const val BANNER_IMAGE = "banner_image"

        /*Get Skills*/
        const val CAT_ID = "cat_id"

        /*Update Qualification*/
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val QUALIFICATION_ID = "qualification_id"

        /*Update Qualification*/
        const val PRODUCT_NAME = "product_name"
        const val PRODUCT_IMAGE = "product_image"

        /*Booking Opreations*/
        const val BOOKING_ID = "booking_id"
        const val REQUEST = "request"

        /*Decline*/
        const val DECLINE_BY = "decline_by"
        const val DECLINE_REASON = "decline_reason"

        /*Online Offline*/
        const val IS_ONLINE = "is_online"

        /*Add Ticket*/
        const val REASON = "reason"

        /*Get Ticket*/
        const val TICKET_ID = "ticket_id"
        const val COMMENT = "comment"

        /*Edit Appointment*/
        const val APPOINTMENT_ID = "appointment_id"

        /*Book Artist*/
        const val DATE_STRING = "date_string"
        const val TIMEZONE = "timezone"

        /*Apply Job*/
        const val JOB_ID = "job_id"

        /*Job Status*/
        const val AJ_ID = "aj_id"
        const val STATUS = "status"

        /*Add Bank*/
        const val BANK_NAME = "bank_name"
        const val ACCOUNT_NUMBER = "account_no"
        const val IFSC_CODE = "ifsc_code"
        const val ACCOUNT_HOLDER_NAME = "account_holder_name"
        const val BANK_ADDRESS = "bank_address"

        /*Chat*/
        const val CHAT_TYPE = "chat_type"

        /*Payment Type*/
        const val ARTIST_COMMISSION_TYPE = "artist_commission_type"

        /*Add Money*/
        const val TXN_ID = "txn_id"
        const val ORDER_ID = "order_id"
        const val AMOUNT = "amount"
        const val CURRENCY = "currency"
        const val CURRENCY_CODE = "currency_code"
        const val CURRENCY_SYMBOL = "currency_symbol"

        /*Home*/
        const val DISTANCE = "distance"

        //webView
        const val URL = "url"
        const val HEADER = "header"

        /*Notifications Codes*/
        const val CHAT_NOTIFICATION = "10007" //both
        const val TICKET_COMMENT_NOTIFICATION = "10009" //both
        const val TICKET_STATUS_NOTIFICATION = "10015" //both
        const val WALLET_NOTIFICATION = "10010" //both
        const val DECLINE_BOOKING_ARTIST_NOTIFICATION = "10002" //both
        const val START_BOOKING_ARTIST_NOTIFICATION = "10003" //ar
        const val BRODCAST_NOTIFICATION = "10014" //both
        const val ADMIN_NOTIFICATION = "10016" //both
        const val BOOK_ARTIST_NOTIFICATION = "10001" //ar
        const val END_BOOKING_ARTIST_NOTIFICATION = "10004" //user
        const val CANCEL_BOOKING_ARTIST_NOTIFICATION = "10005"
        const val ACCEPT_BOOKING_ARTIST_NOTIFICATION = "10006" //user
        const val USER_BLOCK_NOTIFICATION = "1008"
        const val JOB_NOTIFICATION = "10011" //ar
        const val JOB_APPLY_NOTIFICATION = "10012" //user
        const val DELETE_JOB_NOTIFICATION = "10013" //ar
        const val TYPE = "type"
        const val TOPIC_ARTIST = "Artisan"
        const val LANGUAGE_SELECTION = "language_selection"
        const val VOICE_PREFERENCE = "voice_preference"
        const val VOICE_PREFERENCE_ENGLISH = "en"
        const val VOICE_PREFERENCE_ARABIC = "ar"
        const val LANGUAGE = "Language"
        const val ENGLISH_TAG = "en"
        const val ARABIC_TAG = "ar"
    }
}