#!/bin/sh

mysqldump  --column-statistics=0 -hqtrj.i234.me -P3308 -uroot -pYbkk1027 products_wukong > ./products-wukong-from-server.sql