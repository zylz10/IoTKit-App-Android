@file:Suppress("unused")

package com.example.yzd.helloworld

import android.support.v4.util.LongSparseArray
import android.util.Log
import com.cylan.jiafeigou.module.DP
import com.cylan.jiafeigou.module.DPList
import com.cylan.jiafeigou.module.DPPrimary
import org.msgpack.MessagePack
import org.msgpack.packer.Packer
import org.msgpack.template.AbstractTemplate
import org.msgpack.unpacker.Unpacker
import java.util.*

/**
 * Created by yzd on 17-12-2.
 */
object PropertySupervisor {
    private val TAG = PropertySupervisor::class.java.simpleName
    @JvmStatic
    private val properties = LongSparseArray<DP>()
    @JvmStatic
    private val msgPack = MessagePack()

    init {
        msgPack.register(DPPrimary::class.java, DPPrimaryTemplate())
        msgPack.register(DPList::class.java, DPListTemplate())
    }

    @JvmStatic
    fun <T : DP> getValue(uuid: String, msgId: Int): T {
        val key = "$uuid:$msgId".toLong()
        var retValue = properties[key]
        if (retValue == null) {
            Log.d(TAG, "PropertySupervisor.getValue:memory cache for property is miss for key:$key," +
                    "uuid:$uuid,msgId:$msgId,trying to get from disk.")
            val property = DBSupervisor.getProperty(key)
            val type = PropertyTypes.getType(msgId)
            if (property != null && type != null) {
                try {
                    val readValue = msgPack.read(property.bytes, type)
                    retValue = readValue as?DP ?: DPPrimary(readValue, property.msgId, property.version)
                    properties.put(key, retValue)
                } catch (e: Exception) {
                    Log.d(TAG, "read msgpack value error:${e.message},uuid:$uuid,msgId:$msgId,property:$property,type:$type")
                }
            }
        }
        Log.d(TAG, "PropertySupervisor.getValue:the get result for key:$key,msgId:$msgId is:$retValue")
        return retValue as T
    }

    @JvmStatic
    fun setValue(uuid: String, msgId: Int, version: Long, value: ByteArray) {
        val type = PropertyTypes.getType(msgId)
        val key = 99L

        DBSupervisor.putProperty(key, uuid, msgId, version, value)

        if (type != null) {
            try {
                val readValue = msgPack.read(value, type)
                val property: DP = readValue as? DP ?: DPPrimary(readValue, msgId, version)
                properties.put(key, property)
            } catch (e: Exception) {
                Log.d(TAG, "read msgpack value error:${e.message},uuid:$uuid,msgId:$msgId,version:$version,value:${Arrays.toString(value)}")
            }
        }
    }

    @JvmStatic
    fun packValue(value: Any): ByteArray? = try {
        msgPack.write(value)
    } catch (e: Exception) {
        Log.d(TAG, "packValue error:${e.message},value:$value")
        null
    }

    @JvmStatic
    fun <T> unpackValue(bytes: ByteArray, type: Class<T>): T? = try {
        msgPack.read(bytes, type)
    } catch (e: Exception) {
        Log.d(TAG, "unpackValue error:${e.message},bytes:${Arrays.toString(bytes)},type:$type")
        null
    }

    private class DPPrimaryTemplate : AbstractTemplate<DPPrimary<*>>() {
        override fun read(unPacker: Unpacker, to: DPPrimary<*>?, required: Boolean): DPPrimary<*> {
            return DPPrimary(unPacker.readByteArray(), 0, 0)
        }

        override fun write(packer: Packer, v: DPPrimary<*>, required: Boolean) {
            packer.write(v.value)
        }
    }

    private class DPListTemplate : AbstractTemplate<DPList>() {
        override fun read(unPacker: Unpacker, to: DPList?, required: Boolean): DPList {
            val count = unPacker.readArrayBegin()
            val dpList = DPList()
            for (x in 1..count) {
                unPacker.readArrayBegin()
                val msgId = unPacker.readInt()
                val version = unPacker.readLong()
                val bytes = unPacker.readByteArray()
                val dpPrimary = DPPrimary(bytes, msgId, version)
                dpList.add(dpPrimary)
                unPacker.readArrayEnd()
            }
            unPacker.readArrayEnd()
            return dpList
        }

        override fun write(packer: Packer, v: DPList?, required: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}