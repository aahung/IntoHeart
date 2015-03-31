#/bin/bash

rsync --progress -a --exclude node_modules ./*  root@128.199.255.194:~/intoheart/

