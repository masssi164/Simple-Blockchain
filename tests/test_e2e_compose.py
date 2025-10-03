import subprocess
import time

import pytest
import requests


def _docker_available():
    try:
        subprocess.run(['docker', 'info'], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return True
    except Exception:
        return False


if not _docker_available():
    pytest.skip('Docker not available', allow_module_level=True)


BACKEND1_RPC = 'http://localhost:3333/rpc'
BACKEND2_RPC = 'http://localhost:3334/rpc'
BACKEND1_API = 'http://localhost:3333/api'
MINER_ADDRESS = '1111111111111111111114oLvT2'


def rpc_call(url, method, params=None):
    payload = {'jsonrpc': '2.0', 'id': 1, 'method': method, 'params': params or []}
    response = requests.post(url, json=payload, timeout=5)
    response.raise_for_status()
    data = response.json()
    assert 'error' not in data, data['error']
    return data['result']


def await_until(predicate, timeout=60, interval=2):
    end = time.time() + timeout
    while time.time() < end:
        if predicate():
            return True
        time.sleep(interval)
    return False


def wait_for_rpc(url):
    end = time.time() + 60
    while time.time() < end:
        try:
            rpc_call(url, 'web3_clientVersion')
            return True
        except Exception:
            time.sleep(3)
    return False


def latest_height(url):
    block = rpc_call(url, 'sb_chainLatest')
    return block['height'] if block else -1


def utxo_balance(api_url, address=MINER_ADDRESS):
    response = requests.get(f"{api_url}/utxo", params={'address': address}, timeout=5)
    response.raise_for_status()
    outputs = response.json() or []
    return sum(item['value'] for item in outputs)


def sync_height(target_height):
    return (await_until(lambda: latest_height(BACKEND1_RPC) >= target_height) and
            await_until(lambda: latest_height(BACKEND2_RPC) >= target_height))


def test_e2e_compose():
    assert wait_for_rpc(BACKEND1_RPC)
    assert wait_for_rpc(BACKEND2_RPC)

    balance_before = utxo_balance(BACKEND1_API)

    first = rpc_call(BACKEND1_RPC, 'sb_mineBlock')
    assert sync_height(first['height'])

    balance_after_first = utxo_balance(BACKEND1_API)
    assert balance_after_first > balance_before

    second = rpc_call(BACKEND1_RPC, 'sb_mineBlock')
    assert sync_height(second['height'])

    balance_after_second = utxo_balance(BACKEND1_API)
    assert balance_after_second > balance_after_first
