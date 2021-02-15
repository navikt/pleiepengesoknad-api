package no.nav.helse.redis

import io.lettuce.core.RedisClient
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class RedisStore constructor(
    redisClient: RedisClient
) {
    private companion object {
        val logger = LoggerFactory.getLogger(RedisStore::class.java)
    }

    private val connection = redisClient.connect()
    private val async = connection.async()!!
    fun get(key: String): String? {
        val get = async.get(key)
        val await = get.await(10, TimeUnit.SECONDS)
        if (await) {
            return get.get()
        }
        return null
    }

    fun set(key: String, value: String, expirationDate: Date): String? {
        val set = async.set(key, value)

        if (set.await(10, TimeUnit.SECONDS)) {
            val expirationSet = async.pexpireat(key, expirationDate).get()
            if (!expirationSet) throw IllegalStateException("Feilet med å sette expiry på key.")
            else logger.info("Expiration satt på key med TTL=${async.pttl(key).get()}")

            return set.get()
        }

        return null
    }


    fun update(key: String, value: String): String? {
        val set = async.set(key, value)

        if (set.await(10, TimeUnit.SECONDS)) {
            return set.get()
        }

        return null
    }

    fun delete(key: String): Boolean {
        val del = async.del(key)

        if (del.await(10, TimeUnit.SECONDS)) {
            return true
        }

        return false
    }
}
