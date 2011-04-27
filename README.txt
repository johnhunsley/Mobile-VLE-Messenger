############################ Mobile VLE Messenger ############################


Overview
--------

The Mobile VLE Messenger is an Android app which allows Moodle 1.9+ users to send and
receive Moodle messages on their phone just like Sms messages but with no unit message
cost.

The Android app requires Moodle 1.9 or later and that the OK Tech Web Service Plug-in 
version 1.8 or later is installed into Moodle. 

This project is a front-end system to the Mobile VLE OKTech project, see https://github.com/johnhunsley/Mobile-VLE-OKTech
which is a SOAP client to the OK Tech WS plug-in.

In other words, to successfully implement this Android client you will need the following -

1. Moodle 1.9+ and be able to install plug-ins 
2. Enabled messaging in your Moodle installation
3. Moodle OK Tech WS plug-in installed in your Moodle installation 
4. The Mobile VLE Core project built and installed in your local maven repo (https://github.com/johnhunsley/Mobile-VLE-Core)
5. The Mobile VLE OKTech project built and installed in your local maven repo (https://github.com/johnhunsley/Mobile-VLE-OKTech)
6. If you want to push the app to your own Android phone to test it then you'll need the Android sdk from - http://developer.android.com/sdk/index.html
7. Maven installed on your local machine
8. Some branding images if you want to brand the app for your own institution


Install the Moodle plug-in
--------------------------

The VLE Mobile Messenger app requires the wspp_1.8 or later package. Install is easy but you may run into a couple of issues along the way. This tutorial should help you complete the plug-in installation smoothly.
You may get it via git at https://github.com/patrickpollet/moodlews or get the lastest archive file at http://prope.insa-lyon.fr/~ppollet/moodlews/wspp_latest.tgz

1. Lets assume you have installed moodle 1.9 or later as follows -

wwwroot = /var/www/html/moodle
dateroot = /var/www/moodledata

Both this dirs are owned by the apache user 'apache'

2. download the wspp_1.8.tgz, you can find it attached to this course. copy it into your moodle wwwroot directory.

3. unzip it using tar -zxvf wspp_1.8.tgz You'll now see a directory in the moodle dir named wspp/

4. edit moodle/admin/settings/misc.php and add the line

require("$CFG->dirroot/wspp/admin/wspp.php");

just before this line
// hidden scripts linked from elsewhere

5. There is a minor bug in the wspp/filterlib.php file. editing line 502 of script wspp/filterlib.php

change

$msg->lastname = $tmpuser->firstname;
to
$msg->lastname = $tmpuser->lastname;

6. Restart apache for good measure /etc/init.d/httpd restart

7. In a browser lets see if we can see the generated soap wsdl file, open the following in a web browser -

http://<your-moodle-domain>/wspp/wsdl_pp.php

you should get a download moodlews.wsdl, this is the web service descriptor. cancel the download, that just tells us the install is working.

8. Time to run a test from a client and see if we can get a reponse. Download the soapUI www.soapui.org, create a new project and use the url from point 7 as your wsdl url.

9. SoapUI will now generate requests for all the functions available. Open the login function request and put your moodle username and password into the XML in the request editor. Hit run and you should see a response come back with a valid client id and token.

10. At this point you might get a problem. If you havent got the php soap package installed then you'll get an empty response. If so try this -

back in your moodle/wspp/ directory run the mkclasses.sh script, if your missing the soap package you'll see this error -

PHP Fatal error: Class 'SoapClient' not found in /var/www/html/moodle/wspp/wsdl2php.php on line 105

I'm using Centos5 so I just ran -

yum install php-soap

then restarted apache. Try the request again and all should be well.



Config the build
----------------

Once you have successfully installed the Web Service plug-in into Moodle you will now need to config the app to make calls to your installation.

open the following file - Mobile-VLE-Messenger/res/raw/vlehandler.properties

This file contains 3 properties, the VLE Handler implemenation to use, which should always be -

vlehandler=com.mobilevle.oktech.VLEHandlerOKTechImpl

unless you are creating your own handler implementation for another plug-in or VLE. And the url and namespace for the SOAP end point of your Moodle installation

e.g.  

url=http://<your-domain>/moodle/wspp/service_pp.php
namespace=http://<your-domain>/moodle/wspp/wsdl

replace <your-domain> with your moodle domain and port (if not port 80)

If you wish to add your own branding then replace the following image files with suitable images of your own -

Mobile-VLE-Messenger/res/drawable/generic-background.png
Mobile-VLE-Messenger/res/drawable/mvle.png


Build and install the app
-------------------------

There are two dependencies of this project, Mobile VLE Core and Mobile VLE OKTech. You can download the source and build them yourself or
just download the jar files from the following git repos -

https://github.com/johnhunsley/Mobile-VLE-Core
https://github.com/johnhunsley/Mobile-VLE-OKTech

Build the Core project first then the OKTech, or install them both into your local maven repository using the mvn install command.

In the root directory, e.g. ~/Mobile-VLE-Messenger/ you should see the pom.xml file. From the command line you should build the app from there using the following command

mvn clean install

You will see a target/ directory created in this directory, in there you'll find the Android app as a .apk file - MobileVLEMessenger-1.0.apk you should install this on
and Android file using the Android sdk. Ensure your phone is connected to your computer with remote debugging enabled and is mounted as a drive.
Push the app to the phone using the following command 

./adb -d install -r /home/johnhunsley/check_out/Mobile_VLE_Marketing/target/Mobile_VLE_Marketing-1.0.apk

You should now be able to open the app, send and recieve messages via Moodle :-) 


Signing and publishing the app
------------------------------

If you intend to deploy this build to the Android market you will need to sign your .apk file. see - http://developer.android.com/guide/publishing/app-signing.html

You can now publish your app on the Android market so your Moodle users can download and use it. 


Job Done :-)


email john@vlemobile.com or jphunsley@gmail.com for more help  :-) 
