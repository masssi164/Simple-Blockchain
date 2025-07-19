import os
import subprocess

from behave import given, when, then


@given('working directory "{path}"')
def step_working_directory(context, path):
    context.cwd = os.path.join(os.getcwd(), path)


@when('I run "{cmd}"')
def step_run(context, cmd):
    cwd = getattr(context, 'cwd', os.getcwd())
    context.result = subprocess.run(cmd, shell=True, cwd=cwd,
                                    stdout=subprocess.PIPE,
                                    stderr=subprocess.STDOUT)
    context.output = context.result.stdout.decode()


@then('the command should succeed')
def step_success(context):
    if context.result.returncode != 0:
        raise AssertionError(context.output)

import yaml

@when('I load the CI compose file')
def step_load_compose(context):
    with open('docker-compose.ci.yml') as f:
        context.compose = yaml.safe_load(f)

@then('backend2 should configure NODE_PEERS')
def step_check_peers(context):
    services = context.compose.get('services', {})
    backend2 = services.get('backend2', {})
    env = backend2.get('environment', {})
    value = env.get('NODE_PEERS', '')
    if not value or 'backend1' not in value:
        raise AssertionError(f"NODE_PEERS not configured correctly: {value}")

