package com.hello_dev0ps.gbcemus22u

import java.io.File
import java.io.IOException

object ROMLoader {
    // Constantes pour les types de cartouches
    private const val ROM_ONLY = 0x00
    private const val MBC1 = 0x01
    private const val MBC1_RAM = 0x02
    private const val MBC1_RAM_BATTERY = 0x03
    private const val MBC2 = 0x05
    private const val MBC2_BATTERY = 0x06
    private const val ROM_RAM = 0x08
    private const val ROM_RAM_BATTERY = 0x09
    private const val MMM01 = 0x0B
    private const val MMM01_RAM = 0x0C
    private const val MMM01_RAM_BATTERY = 0x0D
    private const val MBC3_TIMER_BATTERY = 0x0F
    private const val MBC3_TIMER_RAM_BATTERY = 0x10
    private const val MBC3 = 0x11
    private const val MBC3_RAM = 0x12
    private const val MBC3_RAM_BATTERY = 0x13
    private const val MBC5 = 0x19
    private const val MBC5_RAM = 0x1A
    private const val MBC5_RAM_BATTERY = 0x1B
    private const val MBC5_RUMBLE = 0x1C
    private const val MBC5_RUMBLE_RAM = 0x1D
    private const val MBC5_RUMBLE_RAM_BATTERY = 0x1E

    // Constantes pour les tailles de ROM
    private const val ROM_SIZE_32KB = 0x00
    private const val ROM_SIZE_64KB = 0x01
    private const val ROM_SIZE_128KB = 0x02
    private const val ROM_SIZE_256KB = 0x03
    private const val ROM_SIZE_512KB = 0x04
    private const val ROM_SIZE_1MB = 0x05
    private const val ROM_SIZE_2MB = 0x06
    private const val ROM_SIZE_4MB = 0x07
    private const val ROM_SIZE_8MB = 0x08

    // Constantes pour les tailles de RAM
    private const val RAM_SIZE_NONE = 0x00
    private const val RAM_SIZE_2KB = 0x01
    private const val RAM_SIZE_8KB = 0x02
    private const val RAM_SIZE_32KB = 0x03
    private const val RAM_SIZE_128KB = 0x04
    private const val RAM_SIZE_64KB = 0x05

    data class ROMInfo(
        val title: String,
        val type: Int,
        val romSize: Int,
        val ramSize: Int,
        val isGBC: Boolean,
        val isSGB: Boolean,
        val isJapanese: Boolean,
        val checksum: Int
    )

    @Throws(IOException::class)
    fun loadROM(romPath: String): ByteArray {
        val file = File(romPath)
        if (!file.exists()) {
            throw IOException("Fichier ROM introuvable: $romPath")
        }

        val romData = file.readBytes()
        if (romData.size < 0x150) {
            throw IOException("ROM invalide: taille insuffisante")
        }

        val romInfo = validateROM(romData)
        println("ROM chargée: ${romInfo.title}")
        println("Type: ${getCartridgeTypeName(romInfo.type)}")
        println("Taille ROM: ${getROMSizeName(romInfo.romSize)}")
        println("Taille RAM: ${getRAMSizeName(romInfo.ramSize)}")
        println("GBC: ${romInfo.isGBC}")
        println("SGB: ${romInfo.isSGB}")
        println("Japonais: ${romInfo.isJapanese}")

        return romData
    }

    private fun validateROM(romData: ByteArray): ROMInfo {
        // Vérification du logo Nintendo
        val nintendoLogo = byteArrayOf(
            0xCE.toByte(), 0xED.toByte(), 0x66.toByte(), 0x66.toByte(), 0xCC.toByte(), 0x0D.toByte(),
            0x00.toByte(), 0x0B.toByte(), 0x03.toByte(), 0x73.toByte(), 0x00.toByte(), 0x83.toByte(),
            0x00.toByte(), 0x0C.toByte(), 0x00.toByte(), 0x0D.toByte(), 0x00.toByte(), 0x08.toByte(),
            0x11.toByte(), 0x1F.toByte(), 0x88.toByte(), 0x89.toByte(), 0x00.toByte(), 0x0E.toByte(),
            0xDC.toByte(), 0xCC.toByte(), 0x6E.toByte(), 0xE6.toByte(), 0xDD.toByte(), 0xDD.toByte(),
            0xD9.toByte(), 0x99.toByte(), 0xBB.toByte(), 0xBB.toByte(), 0x67.toByte(), 0x63.toByte(),
            0x6E.toByte(), 0x0E.toByte(), 0xEC.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xDC.toByte(),
            0x99.toByte(), 0x9F.toByte(), 0xBB.toByte(), 0xB9.toByte(), 0x33.toByte(), 0x3E.toByte()
        )

        for (i in nintendoLogo.indices) {
            if (romData[0x104 + i] != nintendoLogo[i]) {
                throw IOException("ROM invalide: logo Nintendo incorrect")
            }
        }

        // Lecture des informations de la ROM
        val title = String(romData.slice(0x134..0x143).toByteArray()).trim { it.code == 0 }
        val type = romData[0x147].toInt() and 0xFF
        val romSize = romData[0x148].toInt() and 0xFF
        val ramSize = romData[0x149].toInt() and 0xFF
        val isGBC = (romData[0x143].toInt() and 0xFF) == 0x80 || (romData[0x143].toInt() and 0xFF) == 0xC0
        val isSGB = (romData[0x146].toInt() and 0xFF) == 0x03
        val isJapanese = (romData[0x14A].toInt() and 0xFF) == 0x00

        // Vérification de la somme de contrôle
        var checksum = 0
        for (i in 0x134..0x14C) {
            checksum = checksum - romData[i] - 1
        }
        val headerChecksum = romData[0x14D].toInt() and 0xFF
        if ((checksum and 0xFF) != headerChecksum) {
            throw IOException("ROM invalide: somme de contrôle incorrecte")
        }

        return ROMInfo(title, type, romSize, ramSize, isGBC, isSGB, isJapanese, checksum)
    }

    private fun getCartridgeTypeName(type: Int): String {
        return when (type) {
            ROM_ONLY -> "ROM Only"
            MBC1 -> "MBC1"
            MBC1_RAM -> "MBC1 + RAM"
            MBC1_RAM_BATTERY -> "MBC1 + RAM + Battery"
            MBC2 -> "MBC2"
            MBC2_BATTERY -> "MBC2 + Battery"
            ROM_RAM -> "ROM + RAM"
            ROM_RAM_BATTERY -> "ROM + RAM + Battery"
            MMM01 -> "MMM01"
            MMM01_RAM -> "MMM01 + RAM"
            MMM01_RAM_BATTERY -> "MMM01 + RAM + Battery"
            MBC3_TIMER_BATTERY -> "MBC3 + Timer + Battery"
            MBC3_TIMER_RAM_BATTERY -> "MBC3 + Timer + RAM + Battery"
            MBC3 -> "MBC3"
            MBC3_RAM -> "MBC3 + RAM"
            MBC3_RAM_BATTERY -> "MBC3 + RAM + Battery"
            MBC5 -> "MBC5"
            MBC5_RAM -> "MBC5 + RAM"
            MBC5_RAM_BATTERY -> "MBC5 + RAM + Battery"
            MBC5_RUMBLE -> "MBC5 + Rumble"
            MBC5_RUMBLE_RAM -> "MBC5 + Rumble + RAM"
            MBC5_RUMBLE_RAM_BATTERY -> "MBC5 + Rumble + RAM + Battery"
            else -> "Unknown"
        }
    }

    private fun getROMSizeName(size: Int): String {
        return when (size) {
            ROM_SIZE_32KB -> "32 KB"
            ROM_SIZE_64KB -> "64 KB"
            ROM_SIZE_128KB -> "128 KB"
            ROM_SIZE_256KB -> "256 KB"
            ROM_SIZE_512KB -> "512 KB"
            ROM_SIZE_1MB -> "1 MB"
            ROM_SIZE_2MB -> "2 MB"
            ROM_SIZE_4MB -> "4 MB"
            ROM_SIZE_8MB -> "8 MB"
            else -> "Unknown"
        }
    }

    private fun getRAMSizeName(size: Int): String {
        return when (size) {
            RAM_SIZE_NONE -> "None"
            RAM_SIZE_2KB -> "2 KB"
            RAM_SIZE_8KB -> "8 KB"
            RAM_SIZE_32KB -> "32 KB"
            RAM_SIZE_128KB -> "128 KB"
            RAM_SIZE_64KB -> "64 KB"
            else -> "Unknown"
        }
    }
}