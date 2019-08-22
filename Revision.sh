#!/bin/sh
# package="de.moliso.shelxle" android:versionName="0.1.1" android:versionCode="3">
FILE=AndroidManifest.xml
Version=`/usr/bin/svn info|grep Revision:|sed -e "s/Revision: //" ` 
let Version=$Version+1
#Datum=`/usr/bin/svn info|grep "Last Changed Date:"|sed -e "s/Last Changed Date://"` 
#ADatum=`grep "LastChangedDate: " $FILE|sed -e "s/[\$;\"=]//g"|sed -e "s/  QString dateLastChangedDate://"`
AlteVersion=`grep -P "android:versionCode=\"\d+\"" $FILE|sed -e "s/^.*android:versionCode=//"|sed -e "s/[\">]//g"`
let AlteVersion=$AlteVersion+0
sed -i~ -e "s/android:versionCode=\"$AlteVersion\"/android:versionCode=\"$Version\"/" $FILE
sed -i~  -E -e "s/android:versionName=\"0\.1\.[0-9]{1,}/android:versionName=\"0.1.$Version/g"  $FILE
#sed -i -e "s/$ADatum/$Datum/" $FILE
echo "s/android:versionName=\"0.1.[0-9]{1,}/android:versionName=\"0.1.$Version/g" 
echo "Revision  is: $Version and former was $AlteVersion."

