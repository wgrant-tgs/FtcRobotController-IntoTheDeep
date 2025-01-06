package org.firstinspires.ftc.teamcode.modular

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.TouchSensor
import org.firstinspires.ftc.teamcode.GoBildaPinpointDriver

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseLinearOpMode : LinearOpMode() {
    protected lateinit var leftBackMotor: DcMotor
    protected lateinit var rightBackMotor: DcMotor
    protected lateinit var rightFrontMotor: DcMotor
    protected lateinit var leftFrontMotor: DcMotor
    protected lateinit var intakeMotor: DcMotor
    protected lateinit var armMotor: DcMotor
    protected lateinit var leftIntakeSpinner: DcMotor
    protected lateinit var rightIntakeSpinner: DcMotor
    protected lateinit var armServo: Servo
    protected lateinit var ratchet: Ratchet
    protected lateinit var armLimitSwitch: TouchSensor
    protected lateinit var odometry: GoBildaPinpointDriver
    protected lateinit var allMotors: Array<DcMotor>

    protected fun initDriveTrain() {
        try {
            this.leftBackMotor = this.hardwareMap.dcMotor["leftRear"]
            this.rightBackMotor = this.hardwareMap.dcMotor["rightRear"]
            this.rightFrontMotor = this.hardwareMap.dcMotor["rightFront"]
            this.leftFrontMotor = this.hardwareMap.dcMotor["leftFront"]
            this.intakeMotor = this.hardwareMap.dcMotor["intake"]
            this.leftIntakeSpinner = this.hardwareMap.dcMotor["leftIntakeSpinner"]
            this.rightIntakeSpinner = this.hardwareMap.dcMotor["rightIntakeSpinner"]
            this.armMotor = this.hardwareMap.dcMotor["arm"]
            this.armServo = this.hardwareMap.servo["armServo"]
            this.ratchet = Ratchet(this.hardwareMap.servo["ratchet"])
            this.armLimitSwitch = this.hardwareMap.touchSensor["armLimitSwitch"]

            this.leftFrontMotor.direction = DcMotorSimple.Direction.REVERSE
            this.rightFrontMotor.direction = DcMotorSimple.Direction.FORWARD
            this.leftBackMotor.direction = DcMotorSimple.Direction.REVERSE
            this.rightBackMotor.direction = DcMotorSimple.Direction.FORWARD
            this.armMotor.direction = DcMotorSimple.Direction.REVERSE
            this.armMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            this.intakeMotor.direction = DcMotorSimple.Direction.FORWARD // This is a guess, check later

            this.allMotors = arrayOf(leftFrontMotor, rightFrontMotor, leftBackMotor, rightBackMotor, intakeMotor, armMotor)
            this.allMotors.forEach {it.mode = DcMotor.RunMode.RUN_USING_ENCODER}

            this.odometry = this.hardwareMap.get(GoBildaPinpointDriver::class.java, "odometry")
            this.odometry.setOffsets(95.0, 0.0)
            this.odometry.setEncoderResolution(37.25135125)
            this.odometry.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.REVERSED
            )
            odometry.resetPosAndIMU()

            telemetry.addData("Drive Train Initialization", "Success")

        } catch (e: Exception) {
            telemetry.addData("Exception", e)
        }

        telemetry.update()
    }
}