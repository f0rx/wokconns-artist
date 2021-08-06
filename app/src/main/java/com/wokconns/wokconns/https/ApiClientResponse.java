package com.wokconns.wokconns.https;

import com.wokconns.wokconns.dto.UserDTO;

public interface ApiClientResponse {
    void onResponse(boolean isSuccessful, String message, UserDTO response);
}
