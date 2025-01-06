package org.firstinspires.ftc.teamcode.modular

class ToggleableState<T>(
    private var idx: Int = 0, private vararg val values: T, private val cycle: Boolean = false
) {

    init {
        assert(idx in values.indices)
    }

    fun toggle() = right()

    fun left() {
        // negative mod is weird with jvm languages,
        // avoid the problem by making the number always positive
        // idx = (idx - 1 + values.size) % values.size
        if (!cycle) {
            idx = (idx - 1).coerceIn(values.indices)
        } else {
            idx--
            if (idx < 0) idx = values.lastIndex
        }
    }

    fun right() {
        // idx = (idx + 1) % values.size
        if (!cycle) {
            idx = (idx + 1).coerceIn(values.indices)
        } else {
            idx++
            if (idx > values.lastIndex) idx = 0
        }
    }

    val value: T
        get() = values[idx]
}
