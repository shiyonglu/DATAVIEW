#!/bin/bash
ipaddress=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/')
sudo sed -i "s/172.31.14.229/0.0.0.0/g;" /etc/mysql/my.cnf