package com.tangem.tap.network.exchangeServices.utorg.mock.json

/**
 * Created by Anton Zhilenkov on 09.02.2023.
 */
internal val getCurrencyFull = """
[
        {
            "currency": "APT",
            "symbol": "APT",
            "chain": "APTOS",
            "display": "APT",
            "caption": "Aptos",
            "explorerTx": "https://aptoscan.com/version/",
            "explorerAddr": "https://aptoscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 1.8492,
            "depositMax": 739.6673,
            "withdrawalMin": 1.8492,
            "withdrawalMax": 739.6673,
            "addressValidator": "APT",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "ATOM",
            "symbol": "ATOM",
            "chain": "ATOM",
            "display": "ATOM",
            "caption": "Cosmos",
            "explorerTx": "https://atomscan.com/transactions/",
            "explorerAddr": "https://atomscan.com/accounts/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 1.8020,
            "depositMax": 720.7911,
            "withdrawalMin": 1.8020,
            "withdrawalMax": 720.7911,
            "addressValidator": "COSMOS",
            "precision": 4,
            "allowTag": true,
            "udKey": "crypto.ATOM.address"
        },
        {
            "currency": "AUD",
            "symbol": "AUD",
            "display": "AUD",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 43.73,
            "depositMax": 15013.68,
            "withdrawalMin": 43.73,
            "withdrawalMax": 15013.68,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "AVAXAVA",
            "symbol": "AVAX",
            "chain": "AVALANCHE",
            "display": "Avax",
            "caption": "Avalanche",
            "explorerTx": "",
            "explorerAddr": "",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 1.337428,
            "depositMax": 534.971102,
            "withdrawalMin": 1.337428,
            "withdrawalMax": 534.971102,
            "addressValidator": "ETH",
            "precision": 6,
            "allowTag": false,
            "udKey": "crypto.AVAX.address"
        },
        {
            "currency": "BNB",
            "symbol": "BNB",
            "chain": "BNB",
            "display": "BNB",
            "caption": "BEP2",
            "explorerTx": "https://explorer.binance.org/tx/",
            "explorerAddr": "https://explorer.binance.org/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 0.082738,
            "depositMax": 33.095138,
            "withdrawalMin": 0.082738,
            "withdrawalMax": 33.095138,
            "addressValidator": "BNB",
            "precision": 6,
            "allowTag": true,
            "udKey": "crypto.BNB.address"
        },
        {
            "currency": "BNBBSC",
            "symbol": "BNB",
            "chain": "BINANCE_SMART_CHAIN",
            "display": "BNB",
            "caption": "BEP20",
            "explorerTx": "https://www.bscscan.com/tx/",
            "explorerAddr": "https://www.bscscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 0.0828,
            "depositMax": 33.0951,
            "withdrawalMin": 0.0828,
            "withdrawalMax": 33.0951,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "BRL",
            "symbol": "BRL",
            "display": "BRL",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 158.08,
            "depositMax": 54293.21,
            "withdrawalMin": 158.08,
            "withdrawalMax": 54293.21,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "BTC",
            "symbol": "BTC",
            "chain": "BITCOIN",
            "display": "BTC",
            "caption": "",
            "explorerTx": "https://blockstream.info/tx/",
            "explorerAddr": "https://blockstream.info/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 528764.805415,
            "depositMax": 211505922.165820,
            "withdrawalMin": 528764.805415,
            "withdrawalMax": 211505922.165820,
            "addressValidator": "BTC",
            "precision": 6,
            "allowTag": false,
            "udKey": "crypto.BTC.address"
        },
        {
            "currency": "BUSDAVA",
            "symbol": "BUSD",
            "chain": "AVALANCHE",
            "display": "BUSD",
            "caption": "on Avalanche",
            "explorerTx": "https://snowtrace.io/tx/",
            "explorerAddr": "https://snowtrace.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6382,
            "depositMax": 10655.2775,
            "withdrawalMin": 26.6382,
            "withdrawalMax": 10655.2775,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "BUSDBSC",
            "symbol": "BUSD",
            "chain": "BINANCE_SMART_CHAIN",
            "display": "BUSD",
            "caption": "BEP20",
            "explorerTx": "https://www.bscscan.com/tx/",
            "explorerAddr": "https://www.bscscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6414,
            "depositMax": 10656.5563,
            "withdrawalMin": 26.6414,
            "withdrawalMax": 10656.5563,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false,
            "udKey": "crypto.BUSD.version.BEP20.address"
        },
        {
            "currency": "BUSDETH",
            "symbol": "BUSD",
            "chain": "ETHEREUM",
            "display": "BUSD",
            "caption": "ERC20",
            "explorerTx": "https://etherscan.io/tx/",
            "explorerAddr": "https://etherscan.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6414,
            "depositMax": 10656.5563,
            "withdrawalMin": 26.6414,
            "withdrawalMax": 10656.5563,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false,
            "udKey": "crypto.BUSD.version.ERC20.address"
        },
        {
            "currency": "CAD",
            "symbol": "CAD",
            "display": "CAD",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 40.85,
            "depositMax": 14024.33,
            "withdrawalMin": 40.85,
            "withdrawalMax": 14024.33,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "CZK",
            "symbol": "CZK",
            "display": "CZK",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 672.71,
            "depositMax": 231068.12,
            "withdrawalMin": 672.71,
            "withdrawalMax": 231068.12,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "DAIBSC",
            "symbol": "DAI",
            "chain": "BINANCE_SMART_CHAIN",
            "display": "DAI",
            "caption": "BEP20",
            "explorerTx": "https://www.bscscan.com/tx/",
            "explorerAddr": "https://www.bscscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6462,
            "depositMax": 10658.4634,
            "withdrawalMin": 26.6462,
            "withdrawalMax": 10658.4634,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "DAIEAVA",
            "symbol": "DAIE",
            "chain": "AVALANCHE",
            "display": "DAI.e",
            "caption": "on Avalanche",
            "explorerTx": "https://snowtrace.io/tx/",
            "explorerAddr": "https://snowtrace.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6052,
            "depositMax": 10642.0513,
            "withdrawalMin": 26.6052,
            "withdrawalMax": 10642.0513,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "DAIETH",
            "symbol": "DAI",
            "chain": "ETHEREUM",
            "display": "DAI",
            "caption": "ERC20",
            "explorerTx": "https://etherscan.io/tx/",
            "explorerAddr": "https://etherscan.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6436,
            "depositMax": 10657.4090,
            "withdrawalMin": 26.6436,
            "withdrawalMax": 10657.4090,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false,
            "udKey": "crypto.DAI.address"
        },
        {
            "currency": "DAIPOL",
            "symbol": "DAI",
            "chain": "POLYGON",
            "display": "DAI",
            "caption": "on Polygon",
            "explorerTx": "https://polygonscan.com/tx/",
            "explorerAddr": "https://polygonscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6436,
            "depositMax": 10657.4090,
            "withdrawalMin": 26.6436,
            "withdrawalMax": 10657.4090,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "DKK",
            "symbol": "DKK",
            "display": "DKK",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 210.64,
            "depositMax": 72348.14,
            "withdrawalMin": 210.64,
            "withdrawalMax": 72348.14,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "ETH",
            "symbol": "ETH",
            "chain": "ETHEREUM",
            "display": "ETH",
            "caption": "",
            "explorerTx": "https://ropsten.etherscan.io/tx/",
            "explorerAddr": "https://ropsten.etherscan.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 37688.767964,
            "depositMax": 15075507.185288,
            "withdrawalMin": 37688.767964,
            "withdrawalMax": 15075507.185288,
            "addressValidator": "ETH",
            "precision": 6,
            "allowTag": false,
            "udKey": "crypto.ETH.address"
        },
        {
            "currency": "EUR",
            "symbol": "EUR",
            "display": "EUR",
            "caption": "",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 27.24,
            "depositMax": 9349.53,
            "withdrawalMin": 27.24,
            "withdrawalMax": 9349.53,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "GBP",
            "symbol": "GBP",
            "display": "GBP",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 25.14,
            "depositMax": 8630.60,
            "withdrawalMin": 25.14,
            "withdrawalMax": 8630.60,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "GMTBSC",
            "symbol": "GMT",
            "chain": "BINANCE_SMART_CHAIN",
            "display": "GMT",
            "caption": "BEP20",
            "explorerTx": "https://bscscan.com/tx/",
            "explorerAddr": "https://bscscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 53.9899,
            "depositMax": 21595.9496,
            "withdrawalMin": 53.9899,
            "withdrawalMax": 21595.9496,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "GNOETH",
            "symbol": "GNO",
            "chain": "ETHEREUM",
            "display": "GNO",
            "caption": "ERC20",
            "explorerTx": "https://etherscan.io/tx/",
            "explorerAddr": "https://etherscan.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 0.2250,
            "depositMax": 89.9938,
            "withdrawalMin": 0.2250,
            "withdrawalMax": 89.9938,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false,
            "udKey": "crypto.GNO.address"
        },
        {
            "currency": "HAIBSC",
            "symbol": "HAI",
            "chain": "BINANCE_SMART_CHAIN",
            "display": "HAI",
            "caption": "on BSC",
            "explorerTx": "https://bscscan.com/tx/",
            "explorerAddr": "https://bscscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 937.548948,
            "depositMax": 375019.579098,
            "withdrawalMin": 937.548948,
            "withdrawalMax": 375019.579098,
            "addressValidator": "ETH",
            "precision": 6,
            "allowTag": false
        },
        {
            "currency": "HAIVET",
            "symbol": "HAI",
            "chain": "VECHAIN",
            "display": "HAI",
            "caption": "VET",
            "explorerTx": "https://vechainstats.com/transaction/",
            "explorerAddr": "https://vechainstats.com/account/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 949.3974,
            "depositMax": 379758.9569,
            "withdrawalMin": 949.3974,
            "withdrawalMax": 379758.9569,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "IDR",
            "symbol": "IDR",
            "display": "IDR",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 459558,
            "depositMax": 157855988,
            "withdrawalMin": 459558,
            "withdrawalMax": 157855988,
            "addressValidator": "LUHN",
            "precision": 0,
            "allowTag": false
        },
        {
            "currency": "INR",
            "symbol": "INR",
            "display": "INR",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 2511.35,
            "depositMax": 862635.95,
            "withdrawalMin": 2511.35,
            "withdrawalMax": 862635.95,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "JPY",
            "symbol": "JPY",
            "display": "JPY",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 3992,
            "depositMax": 1370884,
            "withdrawalMin": 3992,
            "withdrawalMax": 1370884,
            "addressValidator": "LUHN",
            "precision": 0,
            "allowTag": false
        },
        {
            "currency": "KZT",
            "symbol": "KZT",
            "display": "KZT",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 13843.18,
            "depositMax": 4755080.66,
            "withdrawalMin": 13843.18,
            "withdrawalMax": 4755080.66,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "MH3ETH",
            "symbol": "MH3",
            "chain": "ETHEREUM",
            "display": "NFT",
            "caption": "Meta History",
            "explorerTx": "https://rinkeby.etherscan.io/tx/",
            "explorerAddr": "https://rinkeby.etherscan.io/address/",
            "type": "CRYPTO",
            "enabled": false,
            "depositMin": 0,
            "depositMax": 0,
            "withdrawalMin": 0,
            "withdrawalMax": 0,
            "addressValidator": "ETH",
            "precision": 0,
            "allowTag": false,
            "nftContract": "0xf0192b97135abaa218bcff255b8f4b22171a7590"
        },
        {
            "currency": "MXN",
            "symbol": "MXN",
            "display": "MXN",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 574.83,
            "depositMax": 197445.63,
            "withdrawalMin": 574.83,
            "withdrawalMax": 197445.63,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "MYR",
            "symbol": "MYR",
            "display": "MYR",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 131.04,
            "depositMax": 45006.86,
            "withdrawalMin": 131.04,
            "withdrawalMax": 45006.86,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "NEAR",
            "symbol": "NEAR",
            "chain": "NEAR",
            "display": "NEAR",
            "caption": "",
            "explorerTx": "",
            "explorerAddr": "",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 10.560163,
            "depositMax": 4224.064906,
            "withdrawalMin": 10.560163,
            "withdrawalMax": 4224.064906,
            "addressValidator": "NEAR",
            "precision": 6,
            "allowTag": false,
            "udKey": "crypto.NEAR.address"
        },
        {
            "currency": "NOK",
            "symbol": "NOK",
            "display": "NOK",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 312.23,
            "depositMax": 107245.60,
            "withdrawalMin": 312.23,
            "withdrawalMax": 107245.60,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "NXUSDAVA",
            "symbol": "NXUSD",
            "chain": "AVALANCHE",
            "display": "NXUSD",
            "caption": "Avalanche",
            "explorerTx": "https://snowtrace.io/tx/",
            "explorerAddr": "https://snowtrace.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.8766,
            "depositMax": 10750.6217,
            "withdrawalMin": 26.8766,
            "withdrawalMax": 10750.6217,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "NZD",
            "symbol": "NZD",
            "display": "NZD",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 47.90,
            "depositMax": 16448.49,
            "withdrawalMin": 47.90,
            "withdrawalMax": 16448.49,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "PLN",
            "symbol": "PLN",
            "display": "PLN",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 134.06,
            "depositMax": 46047.85,
            "withdrawalMin": 134.06,
            "withdrawalMax": 46047.85,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "RIF",
            "symbol": "RIF",
            "chain": "RSK",
            "display": "RIF",
            "caption": "RSK",
            "explorerTx": "https://explorer.rsk.co/tx/",
            "explorerAddr": "https://explorer.rsk.co/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 429.65,
            "depositMax": 171859.31,
            "withdrawalMin": 429.65,
            "withdrawalMax": 171859.31,
            "addressValidator": "ETH",
            "precision": 2,
            "allowTag": false,
            "udKey": "crypto.RIF.address"
        },
        {
            "currency": "RUB",
            "symbol": "RUB",
            "display": "RUB",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 2217.40,
            "depositMax": 761666.68,
            "withdrawalMin": 2217.40,
            "withdrawalMax": 761666.68,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "SOL",
            "symbol": "SOL",
            "chain": "SOLANA",
            "display": "SOL",
            "caption": "Solana",
            "explorerTx": "https://explorer.solana.com/tx/",
            "explorerAddr": "https://explorer.solana.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 1.155094,
            "depositMax": 462.037519,
            "withdrawalMin": 1.155094,
            "withdrawalMax": 462.037519,
            "addressValidator": "SOLANA",
            "precision": 6,
            "allowTag": false,
            "udKey": "crypto.SOL.address"
        },
        {
            "currency": "TRY",
            "symbol": "TRY",
            "display": "TRY",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 572.28,
            "depositMax": 196571.08,
            "withdrawalMin": 572.28,
            "withdrawalMax": 196571.08,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "TWTBNB",
            "symbol": "TWT",
            "chain": "BNB",
            "display": "TWT",
            "caption": "BEP2",
            "explorerTx": "https://explorer.binance.org/tx/",
            "explorerAddr": "https://explorer.binance.org/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 17.4457,
            "depositMax": 6978.2637,
            "withdrawalMin": 17.4457,
            "withdrawalMax": 6978.2637,
            "addressValidator": "BNB",
            "precision": 4,
            "allowTag": true,
            "udKey": "crypto.TWT.address"
        },
        {
            "currency": "UAH",
            "symbol": "UAH",
            "display": "UAH",
            "caption": "",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 1118.00,
            "depositMax": 14005.59,
            "withdrawalMin": 1118.00,
            "withdrawalMax": 14005.59,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "USD",
            "symbol": "USD",
            "display": "USD",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 30.57,
            "depositMax": 10382.96,
            "withdrawalMin": 30.57,
            "withdrawalMax": 10382.96,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "USDCARB",
            "symbol": "USDC",
            "chain": "ARBITRUM",
            "display": "USDC",
            "caption": "on arbitrum",
            "explorerTx": "https://arbiscan.io/tx/",
            "explorerAddr": "https://arbiscan.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6382,
            "depositMax": 10655.2775,
            "withdrawalMin": 26.6382,
            "withdrawalMax": 10655.2775,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "USDCBSC",
            "symbol": "USDC",
            "chain": "BINANCE_SMART_CHAIN",
            "display": "USDC",
            "caption": "BEP20",
            "explorerTx": "https://www.bscscan.com/tx/",
            "explorerAddr": "https://www.bscscan.com/address/",
            "type": "CRYPTO",
            "enabled": false,
            "depositMin": 0,
            "depositMax": 0,
            "withdrawalMin": 0,
            "withdrawalMax": 0,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "USDCEAVA",
            "symbol": "USDCE",
            "chain": "AVALANCHE",
            "display": "USDC.e",
            "caption": "on Avalanche",
            "explorerTx": "",
            "explorerAddr": "",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6078,
            "depositMax": 10643.1155,
            "withdrawalMin": 26.6078,
            "withdrawalMax": 10643.1155,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "USDCETH",
            "symbol": "USDC",
            "chain": "ETHEREUM",
            "display": "USDC",
            "caption": "ERC20",
            "explorerTx": "https://etherscan.io/tx/",
            "explorerAddr": "https://etherscan.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6382,
            "depositMax": 10655.2775,
            "withdrawalMin": 26.6382,
            "withdrawalMax": 10655.2775,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false,
            "udKey": "crypto.USDC.address"
        },
        {
            "currency": "USDCZK",
            "symbol": "USDC",
            "chain": "ZKSYNC",
            "display": "USDC",
            "caption": "on zkSync",
            "explorerTx": "https://zkscan.io/explorer/transactions/",
            "explorerAddr": "https://zkscan.io/explorer/accounts/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6078,
            "depositMax": 10643.1155,
            "withdrawalMin": 26.6078,
            "withdrawalMax": 10643.1155,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "USDTAVA",
            "symbol": "USDT",
            "chain": "AVALANCHE",
            "display": "USDT",
            "caption": "on Avalanche",
            "explorerTx": "https://snowtrace.io/tx/",
            "explorerAddr": "https://snowtrace.io/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6649,
            "depositMax": 10665.9328,
            "withdrawalMin": 26.6649,
            "withdrawalMax": 10665.9328,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "USDTE",
            "symbol": "USDT",
            "chain": "ETHEREUM",
            "display": "USDT",
            "caption": "",
            "explorerTx": "",
            "explorerAddr": "",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.66483213,
            "depositMax": 10665.93285048,
            "withdrawalMin": 26.66483213,
            "withdrawalMax": 10665.93285048,
            "addressValidator": "ETH",
            "precision": 8,
            "allowTag": false,
            "udKey": "crypto.USDT.version.ERC20.address"
        },
        {
            "currency": "USDTPOL",
            "symbol": "USDT",
            "chain": "POLYGON",
            "display": "USDT",
            "caption": "on Polygon",
            "explorerTx": "https://polygonscan.com/tx/",
            "explorerAddr": "https://polygonscan.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6649,
            "depositMax": 10665.9328,
            "withdrawalMin": 26.6649,
            "withdrawalMax": 10665.9328,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "USDVEL",
            "symbol": "USDV",
            "chain": "VELAS_EVM",
            "display": "USDV",
            "caption": "Velas EVM",
            "explorerTx": "https://evmexplorer.velas.com/tx/",
            "explorerAddr": "https://evmexplorer.velas.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26611608.908400,
            "depositMax": 10644643563.359833,
            "withdrawalMin": 26611608.908400,
            "withdrawalMax": 10644643563.359833,
            "addressValidator": "ETH",
            "precision": 6,
            "allowTag": false
        },
        {
            "currency": "USNNEAR",
            "symbol": "USN",
            "chain": "NEAR",
            "display": "USN",
            "caption": "Near",
            "explorerTx": "https://explorer.near.org/transactions/",
            "explorerAddr": "https://explorer.near.org/accounts/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6969,
            "depositMax": 10678.7473,
            "withdrawalMin": 26.6969,
            "withdrawalMax": 10678.7473,
            "addressValidator": "NEAR",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "VLX",
            "symbol": "VLX",
            "chain": "VELAS",
            "display": "VLX",
            "caption": "Velas",
            "explorerTx": "https://explorer.velas.com/tx/",
            "explorerAddr": "https://explorer.velas.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 1021.951187,
            "depositMax": 408780.474783,
            "withdrawalMin": 1021.951187,
            "withdrawalMax": 408780.474783,
            "addressValidator": "SOLANA",
            "precision": 6,
            "allowTag": false,
            "udKey": "crypto.VLX.address"
        },
        {
            "currency": "VLXETH",
            "symbol": "VLX",
            "chain": "VELAS_EVM",
            "display": "VLX",
            "caption": "Velas EVM",
            "explorerTx": "https://evmexplorer.velas.com/tx/",
            "explorerAddr": "https://evmexplorer.velas.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 1021.9512,
            "depositMax": 408780.4747,
            "withdrawalMin": 1021.9512,
            "withdrawalMax": 408780.4747,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "WLKNSOL",
            "symbol": "WLKN",
            "chain": "SOLANA",
            "display": "WLKN",
            "caption": "Solana",
            "explorerTx": "https://explorer.solana.com/tx/",
            "explorerAddr": "https://explorer.solana.com/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 531.89,
            "depositMax": 212755.45,
            "withdrawalMin": 531.89,
            "withdrawalMax": 212755.45,
            "addressValidator": "SOLANA",
            "precision": 2,
            "allowTag": false
        },
        {
            "currency": "XDAIGNO",
            "symbol": "xDAI",
            "chain": "GNOSIS",
            "display": "xDAI",
            "caption": "Gnosis chain",
            "explorerTx": "https://blockscout.com/xdai/mainnet/tx/",
            "explorerAddr": "https://blockscout.com/xdai/mainnet/address/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 26.6782,
            "depositMax": 10671.2684,
            "withdrawalMin": 26.6782,
            "withdrawalMax": 10671.2684,
            "addressValidator": "ETH",
            "precision": 4,
            "allowTag": false
        },
        {
            "currency": "XRP",
            "symbol": "XRP",
            "chain": "RIPPLE",
            "display": "XRP",
            "caption": "Ripple",
            "explorerTx": "https://xrpscan.com/tx/",
            "explorerAddr": "https://xrpscan.com/account/",
            "type": "CRYPTO",
            "enabled": true,
            "depositMin": 67.7645,
            "depositMax": 27105.7684,
            "withdrawalMin": 67.7645,
            "withdrawalMax": 27105.7684,
            "addressValidator": "XRP",
            "precision": 4,
            "allowTag": true,
            "udKey": "crypto.XRP.address"
        },
        {
            "currency": "ZAR",
            "symbol": "ZAR",
            "display": "ZAR",
            "type": "FIAT",
            "enabled": true,
            "depositMin": 538.27,
            "depositMax": 184892.03,
            "withdrawalMin": 538.27,
            "withdrawalMax": 184892.03,
            "addressValidator": "LUHN",
            "precision": 2,
            "allowTag": false
        }
    ]
""".trimIndent()
