# Secure Chat Application with RSA 


In this app, we covered a chat application with using RSA encryption. 

## Summary of Application
In this application, E2E Encryption is targeted by keeping the messages in the server (firebase realtime database) as ciphertext. We used RSA encryption to accomplish this.

## Generating Public and Private Keys
To provide RSA, all users must have a unique public and private key. For this, while the user is registered, we produce private public and private keys for the user.
And while producing them, we pair the keys (public and private key for a user) with each other. 
```
RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
        publicKey=Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded());
        privateKey=Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded());
```
## Key Storage
The application keeps all public keys in database,
<img src="https://github.com/zahitkaya/chat-app/blob/master/images/publicKeys.PNG"  width="100%" height="100%"/> 

And it keeps private keys in storage using shared preference. 
```
        SharedPreferences sharedPref = this.getSharedPreferences("sharedPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(userName,privateKey);
```
## Encryption
The application keeps messages in an encrypted form in the database.
```
String cipherMessage = Base64.getEncoder().encodeToString(encrypt(messageText, receiverPublicKey));
```
While encrypting the messages, it makes RSA encryption with the receiver's public key.

<img src="https://github.com/zahitkaya/chat-app/blob/master/images/encryptedMessages.PNG" width="100%" height="100%" >


## Decryption
After the application pulls ciphertext in the database, it decrypts it in the client and messages appear on the user interface.
```
            if(message.getReceiver().equals(mAuth.getCurrentUser().getEmail())){
                        plainText=decrypt(plainText,senderPrivateKey);
                    }
                    else {
                        plainText=decrypt(plainText,receiverPrivateKey);
                    }

```

<img src="https://github.com/zahitkaya/chat-app/blob/master/images/Screenshot_1616186234.png" width="100%" height="100%" >



