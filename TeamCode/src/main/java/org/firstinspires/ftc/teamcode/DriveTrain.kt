package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.modular.BaseLinearOpMode
import org.firstinspires.ftc.teamcode.modular.GamepadButton
import org.firstinspires.ftc.teamcode.modular.GamepadState
import org.firstinspires.ftc.teamcode.modular.ToggleableState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sign

@TeleOp(name = "DriveTrain")
// @Disabled
class DriveTrain : BaseLinearOpMode() {
    // kotlin does not do numeric type promotion, if the 3rd arg is just "1" than T cannot be inferred
    private val power = ToggleableState(2, 0.33, 0.67, 1.0)
    private val servoPos = ToggleableState(1, 0.0 /* dump */, 0.5 /* safe for arm lowering */, 0.225 /* catch sample*/, cycle = true)
    private lateinit var gp1: GamepadState

    override fun runOpMode() {/* Initialization */
        telemetry.msTransmissionInterval = 100

        gp1 = GamepadState(gamepad1)

        val toggleButtonMap = mapOf(
            GamepadButton(gp1, Gamepad::left_bumper) to power::left,
            GamepadButton(gp1, Gamepad::right_bumper) to power::right,
            GamepadButton(gp1, Gamepad::dpad_right) to {this.armServo.position = servoPos.value; servoPos.right()}
        )

        this.initDriveTrain()

        /* End Initialization */
        this.waitForStart()

        while (this.opModeIsActive()) {
            this.gp1.cycle()
            this.odometry.update()
            val pos = this.odometry.position;
            val data = String.format(
                Locale.US,
                "x: %.3f, y: %.3f, h: %.3f",
                pos.getX(DistanceUnit.MM),
                pos.getY(DistanceUnit.MM),
                pos.getHeading(AngleUnit.DEGREES)
            );
            telemetry.addData("pos", data);

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
                this.gp1.current.right_stick_y,
                this.gp1.current.dpad_up.toFloat() - this.gp1.current.dpad_down.toFloat()
            )

            // Magnitude of the maximum value, not velocity
            val max = abs(motorPower.maxBy { abs(it) })

            // Normalize if greater the max is greater than 1
            if (max > 1) motorPower.forEachIndexed { i, _ -> motorPower[i] /= max }

            // Update the motors with the proper power, should keep intake and arm power at a minimum of 0.5 if pressed
            this.allMotors.forEachIndexed { i, m ->
                m.power =
                    if (i < 4) {
                        motorPower[i].toDouble() * power.value
                    } else if (motorPower[i].toDouble() < 0.5) motorPower[i].sign * 0.5 else motorPower[i].toDouble() * power.value
            }

            telemetry.update()
        }

    }

    private fun Boolean.toFloat() = if (this) 1f else 0f
}
