package com.Hello_Dev0ps.gbc_emu_s22u

import java.io.File

fun main() {
    try {
        // Vérification que la ROM existe
        val romPath = "src/main/assets/cpu_instrs.gb"
        val romFile = File(romPath)
        println("Vérification de la ROM : $romPath")
        println("La ROM existe : ${romFile.exists()}")
        
        if (romFile.exists()) {
            // Vérification de la taille de la ROM
            println("Taille de la ROM : ${romFile.length()} octets")
            
            // Lecture des premiers octets pour vérifier le header
            val romData = romFile.readBytes()
            println("Taille des données lues : ${romData.size} octets")
            
            // Vérification du type de cartouche
            val cartridgeType = romData[0x147].toUByte()
            println("Type de cartouche : 0x${cartridgeType.toString(16)}")
            
            // Vérification de la taille de la ROM
            val romSize = romData[0x148].toUByte()
            println("Taille de la ROM : 0x${romSize.toString(16)}")
            
            // Vérification de la taille de la RAM
            val ramSize = romData[0x149].toUByte()
            println("Taille de la RAM : 0x${ramSize.toString(16)}")
            
            println("\nLa ROM semble valide !")
        }
    } catch (e: Exception) {
        println("Erreur lors de la vérification de la ROM : ${e.message}")
        e.printStackTrace()
    }
} 