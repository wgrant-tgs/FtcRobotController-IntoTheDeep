package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Gamepad

fun DcMotorEx.toggleDirection() {
    this.direction = when(this.direction) {
        DcMotorSimple.Direction.REVERSE -> DcMotorSimple.Direction.FORWARD
        DcMotorSimple.Direction.FORWARD -> DcMotorSimple.Direction.REVERSE
    }
}

fun Gamepad.right_trigger_bool(): Boolean {
    return when (this.right_trigger) {
        1f -> true
        else -> false
    }
}

// Justin's newline
