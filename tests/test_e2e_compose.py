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
COIN_SCALE = 100_000_000


def rpc_call(url, method, params=None):
    payload = {'jsonrpc': '2.0', 'id': 1, 'method': method, 'params': params or []}
    response = requests.post(url, json=payload, timeout=5)
    response.raise_for_status()
    data = response.json()
    assert 'error' not in data, data['error']
    return data['result']


def encode_amount(amount):
    return hex(int(amount * COIN_SCALE))


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


def test_e2e_compose():
    assert wait_for_rpc(BACKEND1_RPC)
    assert wait_for_rpc(BACKEND2_RPC)

    first = rpc_call(BACKEND1_RPC, 'sb_mineBlock')

    assert await_until(lambda: latest_height(BACKEND2_RPC) >= first['height'])

    wallet = rpc_call(BACKEND1_RPC, 'sb_walletInfo')
    rpc_call(
        BACKEND1_RPC,
        'eth_sendTransaction',
        [{'to': wallet['address'], 'value': encode_amount(1.0)}],
    )

    second = rpc_call(BACKEND1_RPC, 'sb_mineBlock')

    def backend2_has_tx_block():
        try:
            tip = rpc_call(BACKEND2_RPC, 'sb_chainLatest')
            return tip['height'] >= second['height'] and len(tip.get('txList', [])) > 1
        except Exception:
            return False

    assert await_until(backend2_has_tx_block)

    info = rpc_call(BACKEND1_RPC, 'sb_walletInfo')
    assert info['confirmedBalance'] > 0
