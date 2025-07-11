import time
import grpc


def wait_for_grpc(port: int, timeout: int = 60):
    """Block until a gRPC server becomes available."""
    deadline = time.time() + timeout
    while time.time() < deadline:
        channel = grpc.insecure_channel(f"localhost:{port}")
        try:
            grpc.channel_ready_future(channel).result(timeout=3)
            channel.close()
            return
        except Exception:
            channel.close()
            time.sleep(3)
    raise RuntimeError(f"gRPC service on port {port} not ready")

from node_pb2 import Empty, SendRequest
from node_pb2_grpc import MiningStub, ChainStub, WalletStub
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from behave import given, when, then

GRPC_PORT1 = 9090
GRPC_PORT2 = 9091

@given("two nodes are running")
def step_nodes_running(context):
    # nodes are started by docker-compose in the pipeline
    pass

@when("I send a transaction from node1")
def step_send_tx(context):
    wait_for_grpc(GRPC_PORT1)
    with grpc.insecure_channel(f"localhost:{GRPC_PORT1}") as channel:
        stub = WalletStub(channel)
        info = stub.Info(Empty())
        req = SendRequest(recipient=info.address, amount=1.0)
        context.tx = stub.Send(req)

@when("I mine a block on node1")
def step_mine_block(context):
    wait_for_grpc(GRPC_PORT1)
    with grpc.insecure_channel(f"localhost:{GRPC_PORT1}") as channel:
        stub = MiningStub(channel)
        context.mined = stub.Mine(Empty())

@then("node2 should synchronize the block")
def step_check_sync(context):
    target = context.mined.height
    wait_for_grpc(GRPC_PORT2)
    with grpc.insecure_channel(f"localhost:{GRPC_PORT2}") as channel:
        stub = ChainStub(channel)
        for _ in range(10):
            latest = stub.Latest(Empty())
            if latest.height >= target:
                context.latest = {
                    "height": latest.height,
                    "compactDifficultyBits": latest.compactBits,
                    "hashHex": ""  # not needed in test
                }
                return
            time.sleep(3)
    raise AssertionError("Node2 did not sync the mined block")

@then("both dashboards should load")
def step_dashboards_load(context):
    options = Options()
    options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    with webdriver.Remote(command_executor='http://localhost:4444/wd/hub', options=options) as driver:
        for port in (8081, 8082):
            for _ in range(10):
                try:
                    driver.get(f'http://localhost:{port}')
                    if 'Simple Blockchain' in driver.title:
                        driver.find_element(By.TAG_NAME, 'body')
                        break
                except Exception:
                    time.sleep(3)
            else:
                raise AssertionError(f'Dashboard on port {port} did not load')
