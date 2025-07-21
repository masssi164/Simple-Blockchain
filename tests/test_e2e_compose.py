import subprocess
import time
import requests
import grpc

from .node_pb2 import Empty
from .node_pb2_grpc import MiningStub, ChainStub, WalletStub

COMPOSE_FILE = 'docker-compose.ci.yml'
BACKEND1_GRPC = 9090
BACKEND2_GRPC = 9091
BACKEND1_REST = 'http://localhost:3333'
BACKEND2_REST = 'http://localhost:3334'


def await_until(predicate, timeout=60, interval=2):
    end = time.time() + timeout
    while time.time() < end:
        if predicate():
            return True
        time.sleep(interval)
    return False


def wait_for_grpc(port):
    end = time.time() + 60
    while time.time() < end:
        channel = grpc.insecure_channel(f'localhost:{port}')
        try:
            grpc.channel_ready_future(channel).result(timeout=3)
            channel.close()
            return True
        except Exception:
            channel.close()
            time.sleep(3)
    return False


def compose_up():
    subprocess.run(['docker', 'compose', '-f', COMPOSE_FILE, 'up', '-d'], check=True)
    subprocess.run(['scripts/check_compose_health.sh'], check=True)


def compose_down():
    subprocess.run(['docker', 'compose', '-f', COMPOSE_FILE, 'down', '-v'], check=True)


def test_e2e_compose():
    compose_up()
    try:
        assert wait_for_grpc(BACKEND1_GRPC)
        assert wait_for_grpc(BACKEND2_GRPC)
        # mine first block via gRPC
        with grpc.insecure_channel(f'localhost:{BACKEND1_GRPC}') as ch:
            mine_stub = MiningStub(ch)
            first = mine_stub.Mine(Empty())
        # wait until backend2 sees the block via REST
        def backend2_has_block():
            try:
                r = requests.get(f'{BACKEND2_REST}/api/chain/latest', timeout=5)
                if r.ok and r.json()['height'] >= first.height:
                    return True
            except Exception:
                pass
            return False
        assert await_until(backend2_has_block)
        # send transaction via REST
        wallet = requests.get(f'{BACKEND1_REST}/api/wallet', timeout=5).json()
        tx_resp = requests.post(
            f'{BACKEND1_REST}/api/wallet/send',
            json={'recipient': wallet['address'], 'amount': 1.0},
            timeout=5,
        )
        assert tx_resp.status_code == 200
        # mine second block via gRPC
        with grpc.insecure_channel(f'localhost:{BACKEND1_GRPC}') as ch:
            mine_stub = MiningStub(ch)
            second = mine_stub.Mine(Empty())
        # verify backend2 has the second block with our tx
        def backend2_has_tx_block():
            try:
                with grpc.insecure_channel(f'localhost:{BACKEND2_GRPC}') as ch2:
                    chain = ChainStub(ch2)
                    latest = chain.Latest(Empty())
                    if latest.height >= second.height and len(latest.txList) > 1:
                        return True
            except Exception:
                pass
            return False
        assert await_until(backend2_has_tx_block)
        # wallet balance should remain positive
        with grpc.insecure_channel(f'localhost:{BACKEND1_GRPC}') as ch:
            wallet_stub = WalletStub(ch)
            info = wallet_stub.Info(Empty())
            assert info.balance > 0
    finally:
        compose_down()
