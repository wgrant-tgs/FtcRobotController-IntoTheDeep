package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.CRServo

class Spinner(val left: CRServo, val right: CRServo) {
    fun on(inwards: Boolean) = set(if (inwards) 1.0 else -1.0)
    fun off() = set(0.0)
    fun get() = left.power == 0.0
    private fun set(num: Double) {
        left.power = num;
        right.power = num;
    }
}
