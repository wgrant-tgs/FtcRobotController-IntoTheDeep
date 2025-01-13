package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.modular.BaseLinearOpMode
import org.firstinspires.ftc.teamcode.modular.GamepadButton
import org.firstinspires.ftc.teamcode.modular.GamepadState
import org.firstinspires.ftc.teamcode.modular.ToggleableState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sign

@TeleOp(name = "TeleOp 2025")
@Suppress("unused")
// @Disabled
class TeleOp : BaseLinearOpMode() {
    // kotlin does not do numeric type promotion, if the 3rd arg is just "1" than T cannot be inferred
    private var power = ToggleableState(2, false, 0.33, 0.67, 1.0)
    private lateinit var gp1: GamepadState
    private lateinit var gp2: GamepadState

    override fun runOpMode() {
        telemetry.msTransmissionInterval = 100

        gp1 = GamepadState(gamepad1)
        gp2 = GamepadState(this.gamepad2)

        val toggleButtonMap = mapOf(
            GamepadButton(gp1, Gamepad::left_bumper) to power::left,
            GamepadButton(gp1, Gamepad::right_bumper) to power::right,
            GamepadButton(gp2, Gamepad::dpad_right) to { bucket.position = 0.0 },
            GamepadButton(gp2, Gamepad::dpad_down) to {
                arm.power = 0.0
                ratchet.engage()
                ratchet.enableManual()
            },
            GamepadButton(gp2, Gamepad::dpad_up) to {
                ratchet.disengage()
                ratchet.disableManual()
            }
        )

        // TODO: try-catch this to print any errors / force stop the program?
        try {
            this.initHardware()
            this.telemetry.addLine("initialization successful")
        } catch (e: Exception) {
            this.telemetry.addLine("initialization failed")
            this.telemetry.addData("exception", e)
            this.requestOpModeStop()
        } finally {
            this.waitForStart()
        }

        var lastSwitch = switch.isPressed

        while (this.opModeIsActive()) {
            this.gp1.cycle()
            this.gp2.cycle()
            this.odometry.update()
            val pos = this.odometry.position
            val data = String.format(
                Locale.US,
                "x: %.3f, y: %.3f, h: %.3f",
                pos.getX(DistanceUnit.MM),
                pos.getY(DistanceUnit.MM),
                pos.getHeading(AngleUnit.DEGREES)
            )
            telemetry.addData("pos", data)

            toggleButtonMap.forEach { it.key.ifIsToggled(it.value) }

            /* Calculates motor power in accordance with the allMotors array
               and formulas found here: https://github.com/brandon-gong/ftc-mecanum
             */
            val turnPower = -this.gp1.current.right_trigger + this.gp1.current.left_trigger

            val motorPower = arrayOf(
                this.gp1.current.left_stick_y - this.gp1.current.left_stick_x - turnPower,
                this.gp1.current.left_stick_y - this.gp1.current.left_stick_x + turnPower,
                this.gp1.current.left_stick_y + this.gp1.current.left_stick_x - turnPower,
                this.gp1.current.left_stick_y + this.gp1.current.left_stick_x + turnPower,
            )

            // Magnitude of the maximum value, not velocity
            val max = abs(motorPower.maxBy { abs(it) })

            // Normalize if greater the max is greater than 1
            if (max > 1) motorPower.forEachIndexed { i, _ -> motorPower[i] /= max }

            // Update the motors with the proper power
            this.allMotors.forEachIndexed { i, m ->
                m.power = motorPower[i].toDouble() * power.value
            }


            val currSwitch = switch.isPressed

            // arm reaches bottom
            if (currSwitch && !lastSwitch) {
                arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                arm.mode = DcMotor.RunMode.RUN_USING_ENCODER
                if (!ratchet.manual()) ratchet.engage()
                Thread.sleep(500)
                bucket.position = 0.225
            }

            // going up while arm is at bottom
            if (ratchet.engaged() && (-gp2.current.right_stick_y > 0 && currSwitch || -gp2.current.right_stick_y < 0 && !currSwitch /* should never happen */)) {
                if (!ratchet.manual()) ratchet.disengage()
                Thread.sleep(500)
            }

            // arm going up
            if (ratchet.engaged()) {
                arm.power = 0.0
            } else if (-gp2.current.right_stick_y > 0 && arm.currentPosition < 7000) {
                arm.power = -gp2.current.right_stick_y * 0.75
                // going down
            } else if (-gp2.current.right_stick_y < 0 && !currSwitch) {
                bucket.position = 0.5
                arm.power = -gp2.current.right_stick_y * 0.75
            } else arm.power = 0.0

            // elevator and spinner
            elevator.power = gp2.current.left_stick_y.toDouble()
            if (gp2.current.left_stick_y.sign != 0f) spinner.on(gp2.current.left_stick_y > 0) else spinner.off()

            lastSwitch = currSwitch

            this.telemetry.addData("arm pos", arm.currentPosition)


            telemetry.update()
        }
    }

    class Spinner(val left: CRServo, val right: CRServo) {
        fun on(inwards: Boolean) = set(if (inwards) 1.0 else -1.0)
        fun off() = set(0.0)
        fun get() = left.power == 0.0
        fun set(num: Double) {
            left.power = num
            right.power = num
        }
    }

    // active ~ closed
    class ServoWrapper(val servo: Servo, val active: Double, val inactive: Double) {
        var toggled = false

        fun toggle(): Boolean {
            toggled = !toggled
            servo.position = if (toggled) active else inactive
            return toggled
        }

        fun engaged() = toggled

        fun activate() {
            toggled = true
            servo.position = active
        }

        fun inactivate() {
            toggled = false
            servo.position = inactive
        }
    }
}
