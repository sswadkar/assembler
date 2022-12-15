package com.saraansh

import java.io.File
import java.io.FileWriter

class Assembler{
    // setting the file path to the asm file we're gonna read from
    val asmFilePath: String = "/Users/saraanshwadkar/Downloads/nand2tetris/projects/06/max/Max"

    // initializing data hashmaps
    private val destination: HashMap<String, String> = hashMapOf("" to "000","M=" to "001","D=" to "010","MD=" to "011","A=" to "100","AM=" to "101","AD=" to "110","AMD=" to "111")
    val jump: HashMap<String, String> = hashMapOf("" to "000",";JGT" to "001",";JEQ" to "010",";JGE" to "011",";JLT" to "100",";JNE" to "101",";JLE" to "110",";JMP" to "111")
    private val comp: HashMap<String, String> = hashMapOf("0" to "0101010","1" to "0111111","-1" to "0111010","D" to "0001100","A" to "0110000","M" to "1110000","!D" to "0001101","!A" to "0110001","!M" to "1110001","-D" to "0001111","-A" to "0110011","-M" to "1110011","D+1" to "0011111","A+1" to "0110111","M+1" to "1110111","D-1" to "0001110","A-1" to "0110010","M-1" to "1110010","D+A" to "0000010","D+M" to "1000010","D-A" to "0010011","D-M" to "1010011","A-D" to "0000111","M-D" to "1000111","D&A" to "00000000","D&M" to "1000000","D|A" to "0010101","D|M" to "1010101")
    private val symbols: HashMap<String, Int> = hashMapOf("SP" to 0,"LCL" to 1,"ARG" to 2,"THIS" to 3,"THAT" to 4,"SCREEN" to 16384,"KBD" to 24576,"R0" to 0,"R1" to 1,"R1" to 1,"R2" to 2,"R3" to 3,"R4" to 4,"R5" to 5,"R6" to 6,"R7" to 7,"R8" to 8,"R9" to 9,"R10" to 10,"R11" to 11,"R12" to 12,"R13" to 13,"R14" to 14,"R15" to 15)

    private val fileContents: List<String>?
    private val assemblerCommands: MutableList<String> = mutableListOf()
    private val asm: MutableList<String> = mutableListOf()

    init{
        // reading the file
        fileContents = File("$asmFilePath.asm").readLines()
    }

    fun assembler(){
        for (line in fileContents!!){
            val ln = line.replace(Regex(""""\/+.*\n|\n| *"""), "")
            if (ln != ""){
                assemblerCommands.add(ln)
            }
        }

        var lineNumber = 0
        for (command in assemblerCommands){
            val symbol = Regex("""\(.+\)""").findAll(command)
            val symbolList: List<String> = symbol.toList().map { it.value }
            if (symbolList.isNotEmpty()){
                if (symbolList[0].substring(1, symbolList[0].length) !in symbols){
                    symbols[symbolList[0].substring(1, symbolList[0].length)] = lineNumber
                    lineNumber -= 1
                }
            }
            lineNumber += 1
        }

        for (line in assemblerCommands){
            val ln = line.replace(Regex("""\(.+\)"""), "")
            if (ln != ""){
                asm.add(ln)
            }
        }

        var variableNumber = 16
        for (command in asm){
            val symbol = Regex("""@[a-zA-Z]+.*""").findAll(command)
            val symbolList: List<String> = symbol.toList().map { it.value }
            if (symbolList.isNotEmpty()) {
                symbols[symbolList[0].substring(1, symbolList[0].length)] = lineNumber
                variableNumber += 1
            }
        }

        val hackFile = FileWriter("${asmFilePath}l.hack")
        for (command in assemblerCommands){
            if (command[0] == '@'){
                var address = 0
                if (command.substring(1) in symbols){
                    address = symbols[command.substring(1)]!! + 32768
                } else {
                    address = command.substring(1).toInt() + 32768
                }
                hackFile.write("0${Integer.toBinaryString(address).substring(3)}\n")
            } else {
                val de = Regex(""".+=""").findAll(command)
                val deList: List<String> = de.toList().map { it.value }
                var d = ""
                if (deList.isNotEmpty()){
                    d = destination[deList[0]].toString()
                } else {
                    d = destination[""]!!
                }

                val x = Regex(""";.+""").findAll(command)
                val xList: List<String> = x.toList().map { it.value }
                var tempJump = ""
                if (xList.isNotEmpty()){
                    tempJump = jump[xList[0]].toString()
                } else {
                    tempJump = jump[""]!!
                }

                val c = comp[command.replace(Regex(""".+=|;.+"""), "")]
                hackFile.write("111'${c+d+tempJump}\n")
            }
        }
    }
}

fun main(){
    Assembler().assembler()
}