import json
import time

import pandas as pd
import numpy as np
import requests
import datetime
from keras.models import load_model

model = None


def initialize():
    global model
    model = load_model("./btc_model.h5")


def fetch_raw_ticks():
    url = 'http://crix-api-endpoint.upbit.com/v1/crix/candles/minutes/%d?code=%s&count=%d'
    format_url = url % (1, "CRIX.UPBIT.KRW-BTC", 200)  # 1 minute, btc, 200 records
    headers = {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36'}
    resp = requests.get(url=format_url, headers=headers)

    # Repeat if previous request was not successful
    while not resp.ok:
        time.sleep(1)
        resp = requests.get(url=format_url, headers=headers)
    return resp.json()


def window_ticks(raw_ticks, limit=10):
    df = pd.read_json(json.dumps(raw_ticks))
    df["candleDateTimeKst"] = pd.to_datetime(df["candleDateTimeKst"], infer_datetime_format=True)
    df = df.set_index("candleDateTimeKst")
    df.index = df.index.tz_localize('UTC').tz_convert('Asia/Seoul')
    df = df['openingPrice'].resample('15Min').ohlc()
    df['rateOfChange'] = df['open'].pct_change()
    return pd.DataFrame(df.iloc[-limit:])


def extract_test_data(windowed_df):
    x = np.array(windowed_df['rateOfChange'])
    x = np.reshape(x, (1, 10, 1))
    return x


def predict(x):
    global model
    y = model.predict(x)
    return float(np.reshape(y, 1)[0])


def extract_last_data(windowed_df):
    return windowed_df.iloc[-1].name, int(windowed_df.iloc[-1]['open'])


def calculate_next_data(last_price, last_timestamp, pred_y):
    next_price = last_price * (1 + pred_y)
    next_timestamp = last_timestamp + datetime.timedelta(minutes=15)
    return next_timestamp, next_price


def extract_now_data(raw_ticks):
    return raw_ticks[0]['candleDateTimeKst'], int(raw_ticks[0]['openingPrice'])


def print_result(last_data, pred_data, now_data, rate_of_change):
    # Add 15 minutes to displayed date as it shows an open price of the date.
    print("Now: \t %s \t %d " % (now_data[0], now_data[1]))
    print("Prev (Real): \t %s \t %d " % (last_data[0], last_data[1]))
    print("Next (Prediction): \t %s \t %d \t %.3f %%" % (pred_data[0], pred_data[1], rate_of_change * 100))
    print()


def start_pipeline():
    sleep_interval = 60.0
    start_time = time.time()
    print("start at: ", start_time)
    while True:
        try:
            raw_ticks = fetch_raw_ticks()
            windowed_df = window_ticks(raw_ticks)
            x = extract_test_data(windowed_df)
            pred_y = predict(x)

            last_timestamp, last_price = extract_last_data(windowed_df)
            pred_next_timestamp, pred_next_price = calculate_next_data(last_price, last_timestamp, pred_y)

            now_timestamp, now_price = extract_now_data(raw_ticks)

            print_result((last_timestamp, last_price), (pred_next_timestamp, pred_next_price),
                         (now_timestamp, now_price), pred_y)

        finally:
            time.sleep(max(sleep_interval - ((time.time() - start_time) % sleep_interval), 0))


if __name__ == "__main__":
    initialize()
    start_pipeline()
