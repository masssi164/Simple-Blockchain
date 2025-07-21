from behave import given, when, then
import subprocess

@given('integration tests are forced on')
def step_impl(context):
    context.gradle_cmd = [
        './gradlew',
        ':blockchain-node:test',
        '--no-daemon',
        '-Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition'
    ]

@when('the blockchain-node tests run')
def step_impl(context):
    context.result = subprocess.run(
        context.gradle_cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True
    )

@then('the build should succeed')
def step_impl(context):
    assert context.result.returncode == 0, context.result.stdout
