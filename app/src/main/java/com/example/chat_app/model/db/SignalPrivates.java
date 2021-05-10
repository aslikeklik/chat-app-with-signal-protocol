package com.example.chat_app.model.db;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class SignalPrivates implements Serializable {
    String id;
    String storeMaker;
    String keyPairMaker;
}
