package com.wokconns.wokconns.dto

import java.io.Serializable

class UserDTO : Serializable {
    var user_id = ""
    var name = ""
    var email_id = ""
    var password = ""
    var image = ""
    var address = ""
    var office_address = ""
    var live_lat = ""
    var live_long = ""
    var role = ""
    var status = ""
    var created_at = ""
    var mobile = ""
    var referral_code = ""
    var user_referral_code = ""
    var gender = ""
    var city = ""
    var country = ""
    var updated_at = ""
    var device_type = ""
    var device_id = ""
    var device_token = ""
    var latitude = ""
    var longitude = ""
    var i_card = ""
    var country_code = ""
    var mobile_no = ""
    var bank_name = ""
    var account_no = ""
    var ifsc_code = ""
    var account_holder_name = ""
    var is_profile = 0
    var approval_status = 0

    constructor()
    constructor(
        user_id: String,
        name: String,
        email_id: String,
        password: String,
        image: String,
        address: String,
        office_address: String,
        live_lat: String,
        live_long: String,
        role: String,
        status: String,
        created_at: String,
        mobile: String,
        referral_code: String,
        user_referral_code: String,
        gender: String,
        city: String,
        country: String,
        updated_at: String,
        device_type: String,
        device_id: String,
        device_token: String,
        latitude: String,
        longitude: String,
        i_card: String,
        country_code: String,
        mobile_no: String,
        bank_name: String,
        account_no: String,
        ifsc_code: String,
        account_holder_name: String,
        is_profile: Int,
        approval_status: Int
    ) {
        this.user_id = user_id
        this.name = name
        this.email_id = email_id
        this.password = password
        this.image = image
        this.address = address
        this.office_address = office_address
        this.live_lat = live_lat
        this.live_long = live_long
        this.role = role
        this.status = status
        this.created_at = created_at
        this.mobile = mobile
        this.referral_code = referral_code
        this.user_referral_code = user_referral_code
        this.gender = gender
        this.city = city
        this.country = country
        this.updated_at = updated_at
        this.device_type = device_type
        this.device_id = device_id
        this.device_token = device_token
        this.latitude = latitude
        this.longitude = longitude
        this.i_card = i_card
        this.country_code = country_code
        this.mobile_no = mobile_no
        this.bank_name = bank_name
        this.account_no = account_no
        this.ifsc_code = ifsc_code
        this.account_holder_name = account_holder_name
        this.is_profile = is_profile
        this.approval_status = approval_status
    }

    companion object {
        fun fromArtist(current: UserDTO?, artist: ArtistDetailsDTO?): UserDTO? =
            current?.copyWith(
                name = if (artist?.name.isNullOrBlank()) current.name else artist?.name,
                email = if (artist?.email_id.isNullOrBlank()) current.email_id else artist?.email_id,
                mobile = if (artist?.mobile.isNullOrBlank()) current.mobile else artist?.mobile,
                gender = if (artist?.gender.isNullOrBlank()) current.gender else artist?.gender,
                country = if (artist?.country.isNullOrBlank()) current.country else artist?.country,
                city = if (artist?.city.isNullOrBlank()) current.city else artist?.city,
                bankName = if (artist?.bank_name.isNullOrBlank()) current.bank_name else artist?.bank_name,
                accountNo = if (artist?.account_no.isNullOrBlank()) current.account_no else artist?.account_no,
                accountHolderName = if (artist?.account_holder_name.isNullOrBlank()) current.account_holder_name else artist?.account_holder_name,
                latitude = if (artist?.latitude.isNullOrBlank()) current.latitude else artist?.latitude,
                longitude = if (artist?.longitude.isNullOrBlank()) current.longitude else artist?.longitude,
                liveLat = if (artist?.live_lat.isNullOrBlank()) current.live_lat else artist?.live_lat,
                liveLong = if (artist?.live_long.isNullOrBlank()) current.live_long else artist?.live_long,
            )
    }
}

fun UserDTO.copyWith(
    userId: String? = null,
    name: String? = null,
    email: String? = null,
    password: String? = null,
    image: String? = null,
    address: String? = null,
    officeAddress: String? = null,
    liveLat: String? = null,
    liveLong: String? = null,
    role: String? = null,
    status: String? = null,
    createdAt: String? = null,
    mobile: String? = null,
    referralCode: String? = null,
    userReferralCode: String? = null,
    gender: String? = null,
    city: String? = null,
    country: String? = null,
    updatedAt: String? = null,
    deviceType: String? = null,
    deviceId: String? = null,
    deviceToken: String? = null,
    latitude: String? = null,
    longitude: String? = null,
    i_card: String? = null,
    countryCode: String? = null,
    mobileNo: String? = null,
    bankName: String? = null,
    accountNo: String? = null,
    accountHolderName: String? = null,
    isProfile: Int? = null,
    approvalStatus: Int? = null,
): UserDTO = UserDTO(
    userId ?: this.user_id,
    name ?: this.name,
    email ?: this.email_id,
    password ?: this.password,
    image ?: this.image,
    address ?: this.address,
    officeAddress ?: this.office_address,
    liveLat ?: this.live_lat,
    liveLong ?: this.live_long,
    role ?: this.role,
    status ?: this.status,
    createdAt ?: this.created_at,
    mobile ?: this.mobile,
    referralCode ?: this.referral_code,
    userReferralCode ?: this.user_referral_code,
    gender ?: this.gender,
    city ?: this.city,
    country ?: this.country,
    updatedAt ?: this.updated_at,
    deviceType ?: this.device_type,
    deviceId ?: this.device_id,
    deviceToken ?: this.device_token,
    latitude ?: this.latitude,
    longitude ?: this.longitude,
    i_card ?: this.i_card,
    countryCode ?: this.country_code,
    mobileNo ?: this.mobile_no,
    bankName ?: this.bank_name,
    accountNo ?: this.account_no,
    ifsc_code = ifsc_code,
    accountHolderName ?: this.account_holder_name,
    isProfile ?: this.is_profile,
    approvalStatus ?: this.approval_status,
)