package com.zrq.serialportdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zrq.serialportdemo.databinding.ActivityMainBinding
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initEvent()
    }

    private lateinit var mBinding: ActivityMainBinding
    private var serialHelper: SerialHelper? = null
    private var serialHelper2: SerialHelper? = null

    private fun initEvent() {
        mBinding.apply {
            btnOpen.setOnClickListener {
                try {
                    val port = "dev/" + etSetSerial.text.toString()
                    val rate = etBaudRate.text.toString().toInt()
                    initSerialHelper(port, rate)
                    serialHelper?.open()
                    setLog("串口: \"$port\",波特率：$rate, 打开结果: ${serialHelper?.isOpen}")
                } catch (e: IOException) {
                    setLog("打开串口异常: $e")
                }
            }
            btnOpen2.setOnClickListener {
                try {
                    val port = "dev/" + etSetSerial2.text.toString()
                    val rate = etBaudRate2.text.toString().toInt()
                    initSerialHelper2(port, rate)
                    serialHelper2?.open()
                    setLog2("串口: \"$port\",波特率：$rate, 打开结果: ${serialHelper2?.isOpen}")
                } catch (e: IOException) {
                    setLog2("打开串口异常: $e")
                }
            }
            btnSendMsg.setOnClickListener {
                setLog("发送数据： 02 05 02 05 0E")
                setLog2("发送数据： 02 05 02 05 0E")
                sendData(byteArrayOf(0x02, 0x05, 0x02, 0x05, 0x0E))
                sendData2(byteArrayOf(0x02, 0x05, 0x02, 0x05, 0x0E))
            }
            btnReset.setOnClickListener {
                setLog("发送数据： 00 63 06 03 6E")
                setLog2("发送数据： 00 63 06 03 6E")
                sendData(byteArrayOf(0x00, 0x63, 0x06, 0x03, 0x6c))
                sendData2(byteArrayOf(0x00, 0x63, 0x06, 0x03, 0x6c))
            }
        }
    }

    private fun sendData(data: ByteArray) {
        if (serialHelper?.isOpen == true) {
            try {
                serialHelper?.send(data)
            } catch (e: Exception) {
                setLog("发送数据异常: $e")
            }
        } else setLog("串口未打开")
    }

    private fun sendData2(data: ByteArray) {
        if (serialHelper2?.isOpen == true) {
            try {
                serialHelper2?.send(data)
            } catch (e: Exception) {
                setLog2("发送数据异常: $e")
            }
        } else setLog2("串口未打开")
    }

    private fun initSerialHelper(port: String, rate: Int) {
        serialHelper = object : SerialHelper(port, rate) {
            override fun onDataReceived(paramComBean: ComBean) {
                try {
                    val list = paramComBean.bRec.map { it.toInt().and(0xFF) }
                    setLog("接收数据: $list")
                    Log.d(TAG, "list1: $list")
                    if (list.size >= 8) {
                        if (list[1] == 0x05 + 1 && list[2] == 0x02) {
                            //状态转二进制
                            val state = list[3].toString(2).padStart(8, '0')
                            //分度值代号
                            val divisionCode = list[4]
                            //根据代号取定义好的Map取精度
                            var division = divisionMap.getValue(divisionCode.and(0x0f))
                            if (divisionCode.shr(4) == 8) {
                                division = -division
                            }
                            divisionCode.shl(8)
                            //S1 S2 S3计算相加乘以精度 end
                            val weight = (list[5].shl(8 * 2) + list[6].shl(8 * 1) + list[7].shl(8 * 0)) * division * 1000
                            //状态第六位是1为有效数据 计算
                            if (state[6] == '1') {
                                setLog("当前重量:$weight g")
                            } else {
                                setLog("未能获取重量: 不是有效数据")
                            }
                        }
                    }
                } catch (e: Exception) {
                    setLog("解析接收数据异常: $e")
                }
            }

        }
    }

    private fun initSerialHelper2(port: String, rate: Int) {
        serialHelper2 = object : SerialHelper(port, rate) {
            override fun onDataReceived(paramComBean: ComBean) {
                try {
                    val list = paramComBean.bRec.map { it.toInt().and(0xFF) }
                    setLog2("接收数据: $list")
                    Log.d(TAG, "list1: $list")
                    if (list.size >= 8) {
                        if (list[1] == 0x05 + 1 && list[2] == 0x02) {
                            //状态转二进制
                            val state = list[3].toString(2).padStart(8, '0')
                            //分度值代号
                            val divisionCode = list[4]
                            //根据代号取定义好的Map取精度
                            var division = divisionMap.getValue(divisionCode.and(0x0f))
                            if (divisionCode.shr(4) == 8) {
                                division = -division
                            }
                            divisionCode.shl(8)
                            //S1 S2 S3计算相加乘以精度 end
                            val weight = (list[5].shl(8 * 2) + list[6].shl(8 * 1) + list[7].shl(8 * 0)) * division * 1000
                            //状态第六位是1为有效数据 计算
                            if (state[6] == '1') {
                                setLog2("当前重量:$weight g")
                            } else {
                                setLog2("未能获取重量: 不是有效数据")
                            }
                        }
                    }
                } catch (e: Exception) {
                    setLog2("解析接收数据异常: $e")
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setLog(msg: String) {
        runOnUiThread {
            mBinding.tvMsg.apply {
                text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date()) + "$msg\n" + text
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setLog2(msg: String) {
        runOnUiThread {
            mBinding.tvMsg2.apply {
                text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date()) + "$msg\n" + text
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serialHelper?.close()
        serialHelper2?.close()
    }

    companion object {
        private const val TAG = "MainActivity"
        private val divisionMap = mutableMapOf<Int, Double>().apply {
            put(0, 0.0001)
            put(1, 0.0002)
            put(2, 0.0005)
            put(3, 0.001)
            put(4, 0.002)
            put(5, 0.005)
            put(6, 0.01)
            put(7, 0.02)
            put(8, 0.05)
            put(9, 0.1)
            put(0xA, 0.2)
            put(0xB, 0.5)
            put(0xC, 1.0)
            put(0xD, 2.0)
            put(0xE, 5.0)
        }
    }
}