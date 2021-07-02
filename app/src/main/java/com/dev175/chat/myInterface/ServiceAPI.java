package com.dev175.chat.myInterface;

import com.dev175.chat.model.NotificationMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ServiceAPI {
    @POST("/fcm/send")
    Call<NotificationMessage> sendMessage(@Header("Authorization") String token, @Body NotificationMessage message);
}
