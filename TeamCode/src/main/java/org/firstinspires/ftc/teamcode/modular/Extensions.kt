package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple

fun DcMotorEx.toggleDirection() {
    this.direction = when(this.direction) {
        DcMotorSimple.Direction.REVERSE -> DcMotorSimple.Direction.FORWARD
        DcMotorSimple.Direction.FORWARD -> DcMotorSimple.Direction.REVERSE
    }
}