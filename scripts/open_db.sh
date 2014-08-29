#! /bin/bash
adb pull /data/data/org.clintonhealthaccess.lmis.app/databases/lmis_test_db
mv lmis_test_db lmis_test_db.sqlite
open -a liya lmis_test_db.sqlite
