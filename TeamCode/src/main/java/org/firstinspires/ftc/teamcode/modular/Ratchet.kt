package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.Servo

// active ~ closed
class Ratchet(val servo: Servo, val active: Double, val inactive: Double) {
    private var toggled = false;

    fun toggle() {
        toggled = !toggled
        servo.position = if (toggled) active else inactive
        Thread.sleep(500)
    }

    val engaged
        get() = toggled

    fun engage() {
        toggled = true
        servo.position = active
        Thread.sleep(500)
    }

    fun disengage() {
        toggled = false;
        servo.position = inactive
        Thread.sleep(500)
    }
}