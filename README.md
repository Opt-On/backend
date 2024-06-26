# backend

## To Run

1. make sure you have python 3.12 installed

2. create a venv using `python -m venv ./venv`

3. start venv using `./venv/scripts/activate` or `./venv/bin/activate`

4. install python deps using `pip install -r requirements.txt`

5. start fastapi with `fastapi dev main.py`

## adding packages

Make sure you are using a venv so you dont add all the python packages you have installed to our deps

1. update requirements.txt with `pip freeze > requirements.txt`

## linting

1. use a linter pls
