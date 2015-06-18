# MobileFairPlay
MobileFairPlay (MFP) is the porting of FairPlay for Android Smartphones. MobileFairplay runs Secure-Two Party Computations (STC)s function using the Fairplay framework into Android Smartphones. The current version of MFP is built to run the InterestCast primitive in which two users, e.g. Alice and Bob, want to know if they have interests in common without disclosing out their degree of interests. You can get more details from the paper:

> Gianpiero Costantino, Fabio Martinelli, Paolo Santi, and Dario Amoruso. 2012. **An implementation of secure two-party computation for smartphones with application to privacy-preserving interest-cast**. In Proceedings of the 18th annual international conference on Mobile computing and networking (Mobicom '12). ACM, New York, NY, USA, 447-450. DOI=10.1145/2348543.2348607 http://doi.acm.org/10.1145/2348543.2348607

### Info
This version of MFP uses the Bluetooth connection between to users to run the STC function. Compared to the original Fairplay, MFP does not use TCP sockets but bluetooth ones. To run a STC session you have to scan for neighbours and then to connect with the other smartphones that has the MFP APP installed. At the end of the function, if the thwo users have similar interests you see a bluetooth transfer files between the devices that received the connection to the device that did the connection.

### Important
MobileFairPlay creates a configuration folder into the SD with PATH:
```sh
/interest/config/
```

### Code Info
The code has not been totally cleaned from comments or other things. Moreover, you can find some italian comments.

### Installation
It should be enough to import the project into Eclipse and run it as Android Application. However, keep in mind that this project was developed using Android SDK 2.2

### Cite us
If you like MFP or/and you use it for your researche please cite our paper **An implementation of secure two-party computation for smartphones with application to privacy-preserving interest-cast**

### Contacts
* Gianpiero Costantino at gianpiero.costantino@iit.cnr.it
* Fabio Martinelli at fabio.martinelli@iit.cnr.it
* Paolo Santi at paolo.santi@iit.cnr.it
* Dario Amoruso
