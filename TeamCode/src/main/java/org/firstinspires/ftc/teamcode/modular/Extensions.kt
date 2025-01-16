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
// Justin's newline
