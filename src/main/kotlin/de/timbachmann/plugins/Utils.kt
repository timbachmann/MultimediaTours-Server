package de.timbachmann.plugins

fun normalize(value: Double, min: Double, max: Double): Double {
    return 1 - ((value - min) / (max - min))
}