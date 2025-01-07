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
    private val servoPos =
        ToggleableState(1, 0.0 /* dump */, 0.5 /* safe for arm lowering */, 0.225 /* catch sample*/, cycle = true)
    private lateinit var gp1: GamepadState
    private lateinit var gp2: GamepadState

    override fun runOpMode() {/* Initialization */
        telemetry.msTransmissionInterval = 100

        gp1 = GamepadState(gamepad1)
        gp2 = GamepadState(gamepad2)

        this.ratchet.engage()
        this.armServo.position = 0.5 // Safe position

        val toggleButtonMap = mapOf(
            GamepadButton(gp1, Gamepad::left_bumper) to power::left,
            GamepadButton(gp1, Gamepad::right_bumper) to power::right,
            GamepadButton(gp2, Gamepad::dpad_right) to {
                if (!this.ratchet.isEngaged) {
                    this.armServo.position = servoPos.value
                    servoPos.right()
                }
            },
            GamepadButton(gp1, Gamepad::a) to {
                if (armLimitSwitch.isPressed) {
                    ratchet.engage()
                    armServo.position = 0.225
                }
            },
            GamepadButton(gp1, Gamepad::b) to ratchet::disengage,
            GamepadButton(gp2, Gamepad::a) to {
                if (armLimitSwitch.isPressed) {
                    ratchet.engage()
                    armServo.position = 0.225
                }
            },
            GamepadButton(gp2, Gamepad::b) to ratchet::disengage,
        )

        this.initDriveTrain()

        /* End Initialization */
        this.waitForStart()

        while (this.opModeIsActive()) {
            this.gp1.cycle()
            this.odometry.update()

            // Make arm not clip through robot (allegedly)
            if (armLimitSwitch.isPressed) {
                armMotor.power = 0.0
                ratchet.engage()
            }

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
                if (this.gp2.current.left_stick_y !in -10E-5..10E-5) this.gp2.current.left_stick_y.sign else 0f,
                if (!ratchet.isEngaged && (!armLimitSwitch.isPressed || (this.gp2.current.right_stick_y > 0))) {
                    this.gp2.current.right_stick_y.sign
                } else 0f
            )

            // Handle arm servo motor position (range is used to prevent errors from != 0)
            if (motorPower[5] !in -10E-5..10E-5) {
                armServo.position = 0.5 // Safe position for lowering / raising arm
                if (ratchet.isEngaged) ratchet.disengage()
            }

            // Spin helper motors for intake
            when {
                motorPower[4] in -10E-5..10E-5 -> {
                    leftIntakeSpinner.power = 0.0; rightIntakeSpinner.power = 0.0
                }
                motorPower[4] > 0f -> {
                    leftIntakeSpinner.power = -1.0; rightIntakeSpinner.power = 1.0
                } // Take in samples
                motorPower[4] < 0f -> {
                    leftIntakeSpinner.power = 1.0; rightIntakeSpinner.power = 1.0
                } // Eject samples
            }

            // Magnitude of the maximum value, not velocity
            val max = abs(motorPower.maxBy { abs(it) })

            // Normalize if greater the max is greater than 1
            if (max > 1) motorPower.forEachIndexed { i, _ -> motorPower[i] /= max }

            // Update the motors with the proper power, should keep intake and arm power at a minimum of +-1 if pressed
            this.allMotors.forEachIndexed { i, m ->
                m.power = if (i < 4) {
                    motorPower[i].toDouble() * power.value
                } else motorPower[i].toDouble().sign
            }

            telemetry.update()
        }

    }
}
