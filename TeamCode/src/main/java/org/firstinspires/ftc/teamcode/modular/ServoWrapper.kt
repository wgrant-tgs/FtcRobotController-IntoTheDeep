package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.Servo

// active ~ closed
class ServoWrapper(val servo: Servo, val active: Double, val inactive: Double) {
    private var toggled = false;

    fun toggle() {
        toggled = !toggled
        servo.position = if (toggled) active else inactive
    }

    val engaged
        get() = toggled

    fun engage() {
        toggled = true
        servo.position = active
    }

    fun disengage() {
        toggled = false;
        servo.position = inactive
    }
}