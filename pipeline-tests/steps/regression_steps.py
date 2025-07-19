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
