#!/usr/bin/python3

import subprocess
import time
import os
import sys


interpreter = 'scala'
script = '/app/target/scala-2.12/DMO_NGSI-assembly-0.1.jar'
param = '/app/src/test/resources/'


def echo():
    print('TOB: ', tob)
    print('Orion: ', orion)
    print('Service:', service)


if 'TOB' in os.environ:
    tob = float(os.environ['TOB'])
else:
    print('TOB not found')
    exit(1)

if 'ORION' in os.environ:
    orion = os.environ['ORION']
else:
    print('ORION not found')
    exit(1)

if 'SERVICE' in os.environ:
    service = os.environ['SERVICE']
else:
    print('SERVICE not found')
    exit(1)


while True:
    print("processing")
    echo()
    subprocess.call([interpreter, script, param, orion, service])
    print('sleep:', tob)
    time.sleep(tob)

