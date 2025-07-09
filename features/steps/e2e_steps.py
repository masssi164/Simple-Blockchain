import time
import requests
import jwt
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from behave import given, when, then

SECRET = "changeMeSuperSecret"
BASE_URL1 = "http://localhost:3333"
BASE_URL2 = "http://localhost:3334"

def auth_headers():
    token = jwt.encode({"sub": "ci"}, SECRET, algorithm="HS256")
    return {"Authorization": f"Bearer {token}"}

@given("two nodes are running")
def step_nodes_running(context):
    # nodes are started by docker-compose in the pipeline
    pass

@when("I mine a block on node1")
def step_mine_block(context):
    resp = requests.post(f"{BASE_URL1}/api/mining/mine", headers=auth_headers())
    resp.raise_for_status()
    context.mined = resp.json()

@then("node2 should synchronize the block")
def step_check_sync(context):
    target = context.mined["height"]
    for _ in range(10):
        resp = requests.get(f"{BASE_URL2}/api/chain/latest", headers=auth_headers())
        resp.raise_for_status()
        latest = resp.json()
        if latest["height"] >= target:
            context.latest = latest
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
            driver.get(f'http://localhost:{port}')
            assert 'Simple Blockchain' in driver.title
            driver.find_element(By.TAG_NAME, 'body')
