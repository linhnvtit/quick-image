package com.n0te15m3.quick.cache.pool

import com.n0te15m3.quick.utils.QuickLog
import java.util.concurrent.ConcurrentHashMap

class LRUNode<T>(
    var key: String,
    var value: T?,
    var prev: LRUNode<T>? = null,
    var next: LRUNode<T>? = null
)

class LRUPool<T>(private val capacity: Int) : QuickCachePool<T> {
    private val hash: ConcurrentHashMap<String, LRUNode<T>> = ConcurrentHashMap()
    private val head: LRUNode<T> = LRUNode("head", null)
    private val tail: LRUNode<T> = LRUNode("tail", null)

    init {
        head.next = tail
        tail.prev = head
    }

    override fun get(key: String): T? {
        return try {
            if (!hash.containsKey(key)) {
                null
            } else {
                val node = hash[key]!!
                remove(node)
                add(node)
                node.value
            }
        } catch (e: Exception) {
            QuickLog.e("${e.message}")
            null
        }
    }

    override fun has(key: String): Boolean = hash.containsKey(key)

    override fun put(key: String, value: T): Pair<String, T?>? {
        try {
            val newNode = LRUNode(key, value)

            if (hash.containsKey(key)) {
                remove(hash[key]!!)
            }

            add(newNode)
            hash[key] = newNode

            if (hash.size > capacity) {
                val removeItem = hash[head.next!!.key]
                hash.remove(head.next!!.key)
                head.next = head.next!!.next
                head.next?.prev = head

                return removeItem!!.key to removeItem.value
            }

            return null
        } catch (e: Exception) {
            QuickLog.e("${e.message}")
            return null
        }
    }

    private fun remove(node: LRUNode<T>) {
        node.prev!!.next = node.next
        node.next!!.prev = node.prev
    }

    private fun add(node: LRUNode<T>) {
        val tailPrev = tail.prev
        tail.prev = node
        tailPrev!!.next = node
        node.next = tail
        node.prev = tailPrev
    }
}