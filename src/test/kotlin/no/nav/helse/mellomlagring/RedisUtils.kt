package no.nav.helse.mellomlagring

import com.github.fppt.jedismock.RedisServer

internal fun RedisServer.started() = apply { start() }