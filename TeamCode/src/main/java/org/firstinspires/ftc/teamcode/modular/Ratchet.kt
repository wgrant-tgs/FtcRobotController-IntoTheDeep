package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.hardware.Servo
import java.lang.Thread.sleep

class Ratchet(private val servo: Servo) {

    // Sleeping is to give the ratchet a chance to (dis)engage
    private var engaged = false // Don't know if the ratchet is not engaged at start. I hope not lol

    fun engage() {
        servo.position = 0.075
        sleep(300)
        engaged = true
    }

   fun disengage() {
        servo.position = 0.0
        sleep(300)
        engaged = false
   }

    val isEngaged: Boolean
        get() = engaged
}
