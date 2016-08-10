# cookies-app

The procedure was pretty simple, i encountered some problems though when trying to connect to the server due to certification problems.
Using OpenSSL to view the sessions details i found that there were not any intermediate issuers in chain certification and that caused 
some problems for android to trust the server. My workaround was to trust all the certificates, a procedure that is not safe but the 
shake of speed and testing and since it was not for extended use I did that. Alternatively i could have created a new Keystore and trust the 
certificate with TrustManager.

From the aspect of security, storing the token locally with sharedPreference is recommended but only if it is encrypted. Again for  our 
testing purposes the encryption is omitted.

