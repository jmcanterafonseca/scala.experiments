#!/usr/bin/python3

import subprocess
import time
import os
import glob
import re


def echo():
    print('TOB: ', tob)
    print('TOS: ', tos)
    print('Orion: ', orion)
    print('Service:', service)


if 'TOB' in os.environ:
    tob = float(os.environ['TOB'])
else:
    print('TOB not found')
    exit(1)

if 'TOS' in os.environ:
    tos = float(os.environ['TOS'])
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

files = sorted(glob.glob('/opt/list/*.dmo'))
interpreter = 'scala'
script = '/opt/app.jar'

while True:
    echo()
    for file in files:
        print('processing: ', file)
        subprocess.call([interpreter, script, file, orion, service])
        log = '/opt' + file.split('/opt/list')[1] + '.log'
        for line in open(log, 'r'):
            if not re.search('204', line):
                print('result: ! 204')
                exit(1)
            else:
                print('result: 204')
                os.remove(log)
        print('sleep:', tos)
        time.sleep(tos)
    print('sleep:', tob)
    time.sleep(tob)

