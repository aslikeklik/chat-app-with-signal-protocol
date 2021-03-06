package com.example.chat_app;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Message {
    String receiver;
    String sender;
    Long msgTimeStamp;
    String message;
}
