/**
 * Generated by typeshare 1.13.2
 */

package com.wallet.core.primitives

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class LinkType(val string: String) {
	@SerialName("x")
	X("x"),
	@SerialName("discord")
	Discord("discord"),
	@SerialName("reddit")
	Reddit("reddit"),
	@SerialName("telegram")
	Telegram("telegram"),
	@SerialName("github")
	GitHub("github"),
	@SerialName("youtube")
	YouTube("youtube"),
	@SerialName("facebook")
	Facebook("facebook"),
	@SerialName("website")
	Website("website"),
	@SerialName("coingecko")
	Coingecko("coingecko"),
	@SerialName("opensea")
	OpenSea("opensea"),
	@SerialName("instagram")
	Instagram("instagram"),
	@SerialName("magiceden")
	MagicEden("magiceden"),
	@SerialName("coinmarketcap")
	CoinMarketCap("coinmarketcap"),
	@SerialName("tiktok")
	TikTok("tiktok"),
}

