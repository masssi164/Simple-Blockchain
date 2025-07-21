import os
import subprocess
import yaml


def test_gradle_help():
    result = subprocess.run(['./gradlew', 'help'], stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    assert result.returncode == 0, result.stdout.decode()


def test_npm_tests():
    cwd = os.path.join(os.getcwd(), 'ui')
    result = subprocess.run(['npm', 'test', '--', '--run'], cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    assert result.returncode == 0, result.stdout.decode()


def test_compose_peers():
    with open('docker-compose.ci.yml') as f:
        compose = yaml.safe_load(f)
    env = compose['services']['backend2']['environment']
    value = env.get('NODE_PEERS', '')
    assert value and 'backend1' in value
