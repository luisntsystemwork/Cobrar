#!/bin/bash

#RUTAWSFE=$HOME"/Documentos/pyafipws_prod"
RUTAWSFE="/home/libertya/pyafipws"
RUTAPYTHON="/usr/bin/python"

#cd $RUTAWSFE
cd /home/libertya/pyafipws
#$RUTAPYTHON $RUTAWSFE/wsfev1.py --archivo --debug > $RUTAWSFE/wsfev1.log 
/usr/bin/python /home/libertya/pyafipws/wsfev1.py --archivo --debug > /home/libertya/pyafipws/wsfev1.log
