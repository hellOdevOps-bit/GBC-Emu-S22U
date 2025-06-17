package com.hello_dev0ps.gbcemus22u

class Memory {
    // ROM : 32 Ko (peut être étendu avec des banques)
    private val rom = ByteArray(32 * 1024)
    private var romBank = 1 // Banque 0 est toujours active
    private var romBankCount = 2 // Par défaut, 2 banques (32 Ko)
    private var romType: UByte = 0u // Type de ROM (sera défini lors du chargement)

    // RAM externe (pour MBC1, MBC2, MBC3, etc.)
    private val externalRam = ByteArray(32 * 1024) // 32 Ko max
    private var ramBank = 0
    private var ramBankCount = 1
    private var ramEnabled = false
    private var bankingMode = 0 // 0 = ROM banking, 1 = RAM banking

    // VRAM : 8 Ko (peut être étendu à 16 Ko sur GBC)
    private val vram = ByteArray(8 * 1024)
    private var vramBank = 0 // Banque VRAM active
    private var vramBankCount = 1 // Par défaut, 1 banque (8 Ko)

    // WRAM : 8 Ko (peut être étendu à 32 Ko sur GBC)
    private val wram = ByteArray(8 * 1024)
    private var wramBank = 1 // Banque WRAM active
    private var wramBankCount = 2 // Par défaut, 2 banques (16 Ko)

    // ECHO RAM : 8 Ko (miroir de WRAM)
    private val echoRam = ByteArray(8 * 1024)
    // OAM : 160 octets (pour les sprites)
    private val oam = ByteArray(160)
    // I/O Registers : 128 octets
    private val ioRegisters = ByteArray(128)
    // HRAM : 127 octets
    private val hram = ByteArray(127)

    // RTC pour MBC3
    private data class RTC(
        var seconds: Int = 0,
        var minutes: Int = 0,
        var hours: Int = 0,
        var days: Int = 0,
        var control: Int = 0,
        var latchClock: Int = 0
    )
    private val rtc = RTC()
    private var rtcLatched = false
    private var rtcLatchedSeconds: Int = 0
    private var rtcLatchedMinutes: Int = 0
    private var rtcLatchedHours: Int = 0
    private var rtcLatchedDays: Int = 0
    private var rtcLatchedControl: Int = 0
    private var rtcCounter: Int = 0

    // Lecture d'un octet à une adresse donnée
    fun readByte(address: UShort): UByte {
        val addr = address.toInt()
        return when (addr) {
            // ROM Bank 0 (toujours active)
            in 0x0000..0x3FFF -> rom[addr].toUByte()
            // ROM Bank 1-N (switchable)
            in 0x4000..0x7FFF -> {
                val bankOffset = romBank * 0x4000
                if (bankOffset < rom.size) {
                    rom[bankOffset + (addr - 0x4000)].toUByte()
                } else {
                    0xFFu
                }
            }
            // RAM externe ou RTC
            in 0xA000..0xBFFF -> {
                if (ramEnabled) {
                    if (ramBank <= 0x03) {
                        val bankOffset = ramBank * 0x2000
                        if (bankOffset < externalRam.size) {
                            externalRam[bankOffset + (addr - 0xA000)].toUByte()
                        } else {
                            0xFFu
                        }
                    } else {
                        readRTC()
                    }
                } else {
                    0xFFu
                }
            }
            // VRAM
            in 0x8000..0x9FFF -> {
                val bankOffset = vramBank * 0x2000
                if (bankOffset < vram.size) {
                    vram[bankOffset + (addr - 0x8000)].toUByte()
                } else {
                    0xFFu
                }
            }
            // WRAM Bank 0
            in 0xC000..0xCFFF -> wram[addr - 0xC000].toUByte()
            // WRAM Bank 1-N
            in 0xD000..0xDFFF -> {
                val bankOffset = wramBank * 0x1000
                if (bankOffset < wram.size) {
                    wram[bankOffset + (addr - 0xD000)].toUByte()
                } else {
                    0xFFu
                }
            }
            // ECHO RAM (miroir de WRAM)
            in 0xE000..0xFDFF -> echoRam[addr - 0xE000].toUByte()
            // OAM
            in 0xFE00..0xFE9F -> oam[addr - 0xFE00].toUByte()
            // I/O Registers
            in 0xFF00..0xFF7F -> ioRegisters[addr - 0xFF00].toUByte()
            // HRAM
            in 0xFF80..0xFFFE -> hram[addr - 0xFF80].toUByte()
            // Interrupt Enable Register
            0xFFFF -> ioRegisters[0x7F].toUByte()
            else -> 0xFFu
        }
    }

    // Écriture d'un octet à une adresse donnée
    fun writeByte(address: UShort, value: UByte) {
        val addr = address.toInt()
        when (addr) {
            // ROM (lecture seule)
            in 0x0000..0x7FFF -> handleROMBanking(addr, value)
            // RAM externe ou RTC
            in 0xA000..0xBFFF -> {
                if (ramEnabled) {
                    if (ramBank <= 0x03) {
                        val bankOffset = ramBank * 0x2000
                        if (bankOffset < externalRam.size) {
                            externalRam[bankOffset + (addr - 0xA000)] = value.toByte()
                        }
                    } else {
                        writeRTC(value)
                    }
                }
            }
            // VRAM
            in 0x8000..0x9FFF -> {
                val bankOffset = vramBank * 0x2000
                if (bankOffset < vram.size) {
                    vram[bankOffset + (addr - 0x8000)] = value.toByte()
                }
            }
            // WRAM Bank 0
            in 0xC000..0xCFFF -> {
                wram[addr - 0xC000] = value.toByte()
                // Mise à jour de l'ECHO RAM
                if (addr - 0xC000 < echoRam.size) {
                    echoRam[addr - 0xC000] = value.toByte()
                }
            }
            // WRAM Bank 1-N
            in 0xD000..0xDFFF -> {
                val bankOffset = wramBank * 0x1000
                if (bankOffset < wram.size) {
                    wram[bankOffset + (addr - 0xD000)] = value.toByte()
                    // Mise à jour de l'ECHO RAM
                    if (addr - 0xD000 < echoRam.size) {
                        echoRam[addr - 0xD000] = value.toByte()
                    }
                }
            }
            // ECHO RAM (miroir de WRAM)
            in 0xE000..0xFDFF -> {
                echoRam[addr - 0xE000] = value.toByte()
                // Mise à jour de la WRAM
                if (addr - 0xE000 < wram.size) {
                    wram[addr - 0xE000] = value.toByte()
                }
            }
            // OAM
            in 0xFE00..0xFE9F -> oam[addr - 0xFE00] = value.toByte()
            // I/O Registers
            in 0xFF00..0xFF7F -> {
                ioRegisters[addr - 0xFF00] = value.toByte()
                // Gestion spéciale de certains registres
                when (addr) {
                    0xFF4F -> vramBank = value.toInt() and 0x01 // VBK
                    0xFF70 -> wramBank = value.toInt() and 0x07 // SVBK
                    0xFF46 -> handleDMA(value) // DMA
                }
            }
            // HRAM
            in 0xFF80..0xFFFE -> hram[addr - 0xFF80] = value.toByte()
            // Interrupt Enable Register
            0xFFFF -> ioRegisters[0x7F] = value.toByte()
        }
    }

    // Gestion du banking ROM
    private fun handleROMBanking(address: Int, value: UByte) {
        when (romType) {
            0x01u, 0x02u, 0x03u -> handleMBC1(address, value)
            0x05u, 0x06u -> handleMBC2(address, value)
            0x0Fu, 0x10u, 0x11u, 0x12u, 0x13u -> handleMBC3(address, value)
            0x19u, 0x1Au, 0x1Bu, 0x1Cu, 0x1Du, 0x1Eu -> handleMBC5(address, value)
        }
    }

    // Gestion du MBC1
    private fun handleMBC1(address: Int, value: UByte) {
        when (address) {
            // Activation/désactivation de la RAM
            in 0x0000..0x1FFF -> {
                ramEnabled = (value.toInt() and 0x0F) == 0x0A
            }
            // Sélection de la banque ROM (bits 0-4)
            in 0x2000..0x3FFF -> {
                val bank = value.toInt() and 0x1F
                if (bank == 0) romBank = 1 else romBank = bank
                if (romBank >= romBankCount) romBank = romBank % romBankCount
            }
            // Sélection de la banque RAM ou bits 5-6 de la banque ROM
            in 0x4000..0x5FFF -> {
                val bank = value.toInt() and 0x03
                if (bankingMode == 0) {
                    // Mode ROM banking
                    romBank = (romBank and 0x1F) or (bank shl 5)
                    if (romBank >= romBankCount) romBank = romBank % romBankCount
                } else {
                    // Mode RAM banking
                    ramBank = bank
                    if (ramBank >= ramBankCount) ramBank = ramBank % ramBankCount
                }
            }
            // Mode de banking
            in 0x6000..0x7FFF -> {
                bankingMode = value.toInt() and 0x01
            }
        }
    }

    // Gestion du MBC2
    private fun handleMBC2(address: Int, value: UByte) {
        when (address) {
            // Activation/désactivation de la RAM et sélection de la banque ROM
            in 0x0000..0x3FFF -> {
                if (address and 0x0100 == 0) {
                    ramEnabled = (value.toInt() and 0x0F) == 0x0A
                } else {
                    val bank = value.toInt() and 0x0F
                    if (bank == 0) romBank = 1 else romBank = bank
                    if (romBank >= romBankCount) romBank = romBank % romBankCount
                }
            }
        }
    }

    // Gestion du MBC3
    private fun handleMBC3(address: Int, value: UByte) {
        when (address) {
            // Activation/désactivation de la RAM
            in 0x0000..0x1FFF -> {
                ramEnabled = (value.toInt() and 0x0F) == 0x0A
            }
            // Sélection de la banque ROM
            in 0x2000..0x3FFF -> {
                val bank = value.toInt() and 0x7F
                if (bank == 0) romBank = 1 else romBank = bank
                if (romBank >= romBankCount) romBank = romBank % romBankCount
            }
            // Sélection de la banque RAM/RTC
            in 0x4000..0x5FFF -> {
                val bank = value.toInt() and 0x0F
                if (bank <= 0x03) {
                    // Mode RAM banking
                    ramBank = bank
                    if (ramBank >= ramBankCount) ramBank = ramBank % ramBankCount
                } else {
                    // Mode RTC
                    when (bank) {
                        0x08 -> rtcBank = 0 // Seconds
                        0x09 -> rtcBank = 1 // Minutes
                        0x0A -> rtcBank = 2 // Hours
                        0x0B -> rtcBank = 3 // Days (low)
                        0x0C -> rtcBank = 4 // Days (high) + Control
                    }
                }
            }
            // Latch Clock Data
            in 0x6000..0x7FFF -> {
                if (value.toInt() == 0x00 && rtcLatchClock == 0x01) {
                    // Latch RTC data
                    rtcLatched = true
                    rtcLatchedSeconds = rtc.seconds
                    rtcLatchedMinutes = rtc.minutes
                    rtcLatchedHours = rtc.hours
                    rtcLatchedDays = rtc.days
                    rtcLatchedControl = rtc.control
                }
                rtcLatchClock = value.toInt()
            }
        }
    }

    // Gestion du MBC5
    private fun handleMBC5(address: Int, value: UByte) {
        when (address) {
            // Activation/désactivation de la RAM
            in 0x0000..0x1FFF -> {
                ramEnabled = (value.toInt() and 0x0F) == 0x0A
            }
            // Sélection de la banque ROM (bits 0-7)
            in 0x2000..0x2FFF -> {
                romBank = (romBank and 0x100) or value.toInt()
                if (romBank >= romBankCount) romBank = romBank % romBankCount
            }
            // Sélection de la banque ROM (bit 8)
            in 0x3000..0x3FFF -> {
                romBank = (romBank and 0xFF) or ((value.toInt() and 0x01) shl 8)
                if (romBank >= romBankCount) romBank = romBank % romBankCount
            }
            // Sélection de la banque RAM
            in 0x4000..0x5FFF -> {
                ramBank = value.toInt() and 0x0F
                if (ramBank >= ramBankCount) ramBank = ramBank % ramBankCount
            }
        }
    }

    // Gestion du DMA
    private fun handleDMA(value: UByte) {
        val sourceAddress = value.toInt() shl 8
        for (i in 0..0x9F) {
            val byte = readByte((sourceAddress + i).toUShort())
            writeByte((0xFE00 + i).toUShort(), byte)
        }
    }

    // Mise à jour du RTC
    private fun updateRTC(cycles: Int) {
        if ((rtc.control and 0x40) != 0) return // RTC halted

        rtcCounter += cycles
        if (rtcCounter >= 32768) { // 32768 Hz
            rtcCounter -= 32768
            rtc.seconds = (rtc.seconds + 1) % 60
            if (rtc.seconds == 0) {
                rtc.minutes = (rtc.minutes + 1) % 60
                if (rtc.minutes == 0) {
                    rtc.hours = (rtc.hours + 1) % 24
                    if (rtc.hours == 0) {
                        rtc.days = (rtc.days + 1) % 512
                        if (rtc.days == 0) {
                            rtc.control = rtc.control or 0x80 // Day counter overflow
                        }
                    }
                }
            }
        }
    }

    // Lecture du RTC
    private fun readRTC(): UByte {
        if (!rtcLatched) {
            updateRTC(1) // Mise à jour avant lecture
        }
        return when (rtcBank) {
            0 -> rtc.seconds.toUByte()
            1 -> rtc.minutes.toUByte()
            2 -> rtc.hours.toUByte()
            3 -> (rtc.days and 0xFF).toUByte()
            4 -> ((rtc.days shr 8) and 0x01 or (rtc.control and 0xFE)).toUByte()
            else -> 0u
        }
    }

    // Écriture du RTC
    private fun writeRTC(value: UByte) {
        if ((rtc.control and 0x40) != 0) return // RTC halted

        when (rtcBank) {
            0 -> rtc.seconds = value.toInt() and 0x3F
            1 -> rtc.minutes = value.toInt() and 0x3F
            2 -> rtc.hours = value.toInt() and 0x1F
            3 -> rtc.days = (rtc.days and 0x100) or value.toInt()
            4 -> {
                rtc.days = (rtc.days and 0xFF) or ((value.toInt() and 0x01) shl 8)
                rtc.control = value.toInt() and 0xFE
            }
        }
    }

    // Chargement de la ROM
    fun loadROM(romData: ByteArray) {
        if (romData.size > rom.size) {
            throw IllegalArgumentException("ROM trop grande pour la mémoire disponible")
        }
        romData.copyInto(rom, 0, 0, romData.size)
        
        // Détermination du type de ROM et du nombre de banques
        romType = rom[0x147].toUByte()
        when (romType) {
            0x00u -> {
                romBankCount = 2 // ROM only
                ramBankCount = 0
            }
            0x01u, 0x02u, 0x03u -> {
                romBankCount = 32 // MBC1
                ramBankCount = if (romType == 0x03u.toUByte()) 4 else 0
            }
            0x05u, 0x06u -> {
                romBankCount = 64 // MBC2
                ramBankCount = if (romType == 0x06u.toUByte()) 1 else 0
            }
            0x08u, 0x09u -> {
                romBankCount = 2 // ROM + RAM
                ramBankCount = if (romType == 0x09u.toUByte()) 1 else 0
            }
            0x0Bu, 0x0Cu, 0x0Du -> {
                romBankCount = 32 // MMM01
                ramBankCount = if (romType == 0x0Du.toUByte()) 1 else 0
            }
            0x0Fu, 0x10u, 0x11u, 0x12u, 0x13u -> {
                romBankCount = 512 // MBC3
                ramBankCount = if (romType == 0x13u.toUByte()) 4 else 0
            }
            0x19u, 0x1Au, 0x1Bu, 0x1Cu, 0x1Du, 0x1Eu -> {
                romBankCount = 512 // MBC5
                ramBankCount = if (romType == 0x1Eu.toUByte()) 16 else 0
            }
            else -> {
                romBankCount = 2
                ramBankCount = 0
            }
        }

        // Détermination du nombre de banques VRAM
        val cgbFlag = rom[0x143].toUByte()
        vramBankCount = if (cgbFlag == 0x80u.toUByte() || cgbFlag == 0xC0u.toUByte()) 2 else 1
    }

    // Accès aux registres I/O spécifiques
    fun getLCDC(): UByte = ioRegisters[0x40].toUByte()
    fun getSTAT(): UByte = ioRegisters[0x41].toUByte()
    fun getSCY(): UByte = ioRegisters[0x42].toUByte()
    fun getSCX(): UByte = ioRegisters[0x43].toUByte()
    fun getLY(): UByte = ioRegisters[0x44].toUByte()
    fun getLYC(): UByte = ioRegisters[0x45].toUByte()
    fun getDMA(): UByte = ioRegisters[0x46].toUByte()
    fun getBGP(): UByte = ioRegisters[0x47].toUByte()
    fun getOBP0(): UByte = ioRegisters[0x48].toUByte()
    fun getOBP1(): UByte = ioRegisters[0x49].toUByte()
    fun getWY(): UByte = ioRegisters[0x4A].toUByte()
    fun getWX(): UByte = ioRegisters[0x4B].toUByte()
    fun getVBK(): UByte = vramBank.toUByte()
    fun getSVBK(): UByte = wramBank.toUByte()

    // Registres APU
    fun getNR10(): UByte = ioRegisters[0x10].toUByte() // Sweep
    fun getNR11(): UByte = ioRegisters[0x11].toUByte() // Length & Duty
    fun getNR12(): UByte = ioRegisters[0x12].toUByte() // Volume & Envelope
    fun getNR13(): UByte = ioRegisters[0x13].toUByte() // Frequency Low
    fun getNR14(): UByte = ioRegisters[0x14].toUByte() // Frequency High & Control
    fun getNR21(): UByte = ioRegisters[0x16].toUByte() // Length & Duty
    fun getNR22(): UByte = ioRegisters[0x17].toUByte() // Volume & Envelope
    fun getNR23(): UByte = ioRegisters[0x18].toUByte() // Frequency Low
    fun getNR24(): UByte = ioRegisters[0x19].toUByte() // Frequency High & Control
    fun getNR30(): UByte = ioRegisters[0x1A].toUByte() // Sound On/Off
    fun getNR31(): UByte = ioRegisters[0x1B].toUByte() // Length
    fun getNR32(): UByte = ioRegisters[0x1C].toUByte() // Volume
    fun getNR33(): UByte = ioRegisters[0x1D].toUByte() // Frequency Low
    fun getNR34(): UByte = ioRegisters[0x1E].toUByte() // Frequency High & Control
    fun getNR41(): UByte = ioRegisters[0x20].toUByte() // Length
    fun getNR42(): UByte = ioRegisters[0x21].toUByte() // Volume & Envelope
    fun getNR43(): UByte = ioRegisters[0x22].toUByte() // Polynomial
    fun getNR44(): UByte = ioRegisters[0x23].toUByte() // Control
    fun getNR50(): UByte = ioRegisters[0x24].toUByte() // Master Volume
    fun getNR51(): UByte = ioRegisters[0x25].toUByte() // Sound Output
    fun getNR52(): UByte = ioRegisters[0x26].toUByte() // Sound On/Off

    // Écriture des registres I/O spécifiques
    fun setLCDC(value: UByte) { ioRegisters[0x40] = value.toByte() }
    fun setSTAT(value: UByte) { ioRegisters[0x41] = value.toByte() }
    fun setSCY(value: UByte) { ioRegisters[0x42] = value.toByte() }
    fun setSCX(value: UByte) { ioRegisters[0x43] = value.toByte() }
    fun setLY(value: UByte) { ioRegisters[0x44] = value.toByte() }
    fun setLYC(value: UByte) { ioRegisters[0x45] = value.toByte() }
    fun setDMA(value: UByte) { ioRegisters[0x46] = value.toByte() }
    fun setBGP(value: UByte) { ioRegisters[0x47] = value.toByte() }
    fun setOBP0(value: UByte) { ioRegisters[0x48] = value.toByte() }
    fun setOBP1(value: UByte) { ioRegisters[0x49] = value.toByte() }
    fun setWY(value: UByte) { ioRegisters[0x4A] = value.toByte() }
    fun setWX(value: UByte) { ioRegisters[0x4B] = value.toByte() }
    fun setVBK(value: UByte) { vramBank = value.toInt() and 0x01 }
    fun setSVBK(value: UByte) { wramBank = value.toInt() and 0x07 }

    // Écriture des registres APU
    fun setNR10(value: UByte) { ioRegisters[0x10] = value.toByte() }
    fun setNR11(value: UByte) { ioRegisters[0x11] = value.toByte() }
    fun setNR12(value: UByte) { ioRegisters[0x12] = value.toByte() }
    fun setNR13(value: UByte) { ioRegisters[0x13] = value.toByte() }
    fun setNR14(value: UByte) { ioRegisters[0x14] = value.toByte() }
    fun setNR21(value: UByte) { ioRegisters[0x16] = value.toByte() }
    fun setNR22(value: UByte) { ioRegisters[0x17] = value.toByte() }
    fun setNR23(value: UByte) { ioRegisters[0x18] = value.toByte() }
    fun setNR24(value: UByte) { ioRegisters[0x19] = value.toByte() }
    fun setNR30(value: UByte) { ioRegisters[0x1A] = value.toByte() }
    fun setNR31(value: UByte) { ioRegisters[0x1B] = value.toByte() }
    fun setNR32(value: UByte) { ioRegisters[0x1C] = value.toByte() }
    fun setNR33(value: UByte) { ioRegisters[0x1D] = value.toByte() }
    fun setNR34(value: UByte) { ioRegisters[0x1E] = value.toByte() }
    fun setNR41(value: UByte) { ioRegisters[0x20] = value.toByte() }
    fun setNR42(value: UByte) { ioRegisters[0x21] = value.toByte() }
    fun setNR43(value: UByte) { ioRegisters[0x22] = value.toByte() }
    fun setNR44(value: UByte) { ioRegisters[0x23] = value.toByte() }
    fun setNR50(value: UByte) { ioRegisters[0x24] = value.toByte() }
    fun setNR51(value: UByte) { ioRegisters[0x25] = value.toByte() }
    fun setNR52(value: UByte) { ioRegisters[0x26] = value.toByte() }
} 