import random
import uuid
import json
import requests

import websocket


def handshake(ws):
    msg = "[\"[{\\\"ticket\\\":\\\"ram macbook\\\"},{\\\"type\\\":\\\"recentCrix\\\",\\\"codes\\\":[\\\"CRIX.BITFINEX.USD-BTC\\\",\\\"CRIX.BITFLYER.JPY-BTC\\\",\\\"CRIX.OKCOIN.CNY-BTC\\\",\\\"CRIX.KRAKEN.EUR-BTC\\\",\\\"CRIX.UPBIT.KRW-BTC\\\",\\\"CRIX.UPBIT.KRW-DASH\\\",\\\"CRIX.UPBIT.KRW-ETH\\\",\\\"CRIX.UPBIT.KRW-NEO\\\",\\\"CRIX.UPBIT.KRW-BCC\\\",\\\"CRIX.UPBIT.KRW-MTL\\\",\\\"CRIX.UPBIT.KRW-LTC\\\",\\\"CRIX.UPBIT.KRW-STRAT\\\",\\\"CRIX.UPBIT.KRW-XRP\\\",\\\"CRIX.UPBIT.KRW-ETC\\\",\\\"CRIX.UPBIT.KRW-OMG\\\",\\\"CRIX.UPBIT.KRW-SNT\\\",\\\"CRIX.UPBIT.KRW-WAVES\\\",\\\"CRIX.UPBIT.KRW-PIVX\\\",\\\"CRIX.UPBIT.KRW-XEM\\\",\\\"CRIX.UPBIT.KRW-ZEC\\\",\\\"CRIX.UPBIT.KRW-XMR\\\",\\\"CRIX.UPBIT.KRW-QTUM\\\",\\\"CRIX.UPBIT.KRW-LSK\\\",\\\"CRIX.UPBIT.KRW-STEEM\\\",\\\"CRIX.UPBIT.KRW-XLM\\\",\\\"CRIX.UPBIT.KRW-ARDR\\\",\\\"CRIX.UPBIT.KRW-KMD\\\",\\\"CRIX.UPBIT.KRW-ARK\\\",\\\"CRIX.UPBIT.KRW-STORJ\\\",\\\"CRIX.UPBIT.KRW-GRS\\\",\\\"CRIX.UPBIT.KRW-VTC\\\",\\\"CRIX.UPBIT.KRW-REP\\\",\\\"CRIX.UPBIT.KRW-EMC2\\\",\\\"CRIX.UPBIT.KRW-ADA\\\",\\\"CRIX.UPBIT.KRW-SBD\\\",\\\"CRIX.UPBIT.KRW-TIX\\\",\\\"CRIX.UPBIT.KRW-POWR\\\",\\\"CRIX.UPBIT.KRW-MER\\\",\\\"CRIX.UPBIT.KRW-BTG\\\",\\\"CRIX.COINMARKETCAP.KRW-USDT\\\"]},{\\\"type\\\":\\\"crixTrade\\\",\\\"codes\\\":[\\\"CRIX.UPBIT.KRW-BTC\\\"]},{\\\"type\\\":\\\"crixOrderbook\\\",\\\"codes\\\":[\\\"CRIX.UPBIT.KRW-BTC\\\"]}]\"]";
    ws.send(msg)


def on_error(ws, error):
    print(error)


def on_close(ws):
    print("websocket closed")


def on_open(ws):
    print("websocket opened: ", ws)


class UpbitSocket:
    def __init__(self, message_callback):
        self.message_callback = message_callback

    def start(self):
        upbit_url = "wss://crix-websocket.upbit.com/sockjs/%d/%s/websocket"
        upbit_ws = upbit_url % (random.randint(100, 999), uuid.uuid4().hex[:8])
        ws = websocket.WebSocketApp(upbit_ws,
                                    on_message=self.on_message,
                                    on_error=on_error,
                                    on_close=on_close)
        ws.on_open = on_open
        ws.run_forever()

    def on_message(self, ws, message):
        if message == "o":
            handshake(ws)
        elif message.startswith("a"):
            self.message_callback(message)


class UpbitPolling:
    def __init__(self, message_callback, interval_minute=1):
        self.message_callback = message_callback
        self.interval_minute = interval_minute

    def start(self):
        url = 'https://crix-api-endpoint.upbit.com/v1/crix/candles/minutes/%d?code=%s&count=%d'
        format_url = url % (self.interval_minute, "CRIX.UPBIT.KRW-BTC", 200)
        resp = requests.get(url=format_url)
        print(resp.json())


up = UpbitPolling(None)
up.start()
